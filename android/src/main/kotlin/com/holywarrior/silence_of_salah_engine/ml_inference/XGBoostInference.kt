package com.holywarrior.silence_of_salah_engine.ml_inference

import ml.dmlc.xgboost4j.java.Booster
import ml.dmlc.xgboost4j.java.DMatrix
import ml.dmlc.xgboost4j.java.XGBoost
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

object XGBoostInference {

    // ── State ────────────────────────────────────────────────────────────────

    @Volatile private var booster: Booster? = null
    @Volatile private var numFeatures: Int = 0

    private val isLoaded = AtomicBoolean(false)

    private const val NUM_CHANNELS = 3

    // ── Lifecycle ────────────────────────────────────────────────────────────

    /**
     * Safe model load (never crashes app)
     */
    fun loadSafely(modelPath: String, windowSize: Int = 150): Boolean {
        return try {
            if (!File(modelPath).exists()) {
                return false
            }

            val newBooster = XGBoost.loadModel(modelPath)

            synchronized(this) {
                booster?.dispose()
                booster = newBooster
                numFeatures = windowSize * NUM_CHANNELS
                isLoaded.set(true)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            isLoaded.set(false)
            false
        }
    }

    /**
     * Strict load (optional use)
     */
    fun load(modelPath: String, windowSize: Int = 150) {
        require(File(modelPath).exists()) { "Model file not found: $modelPath" }

        val newBooster = XGBoost.loadModel(modelPath)

        synchronized(this) {
            booster?.dispose()
            booster = newBooster
            numFeatures = windowSize * NUM_CHANNELS
            isLoaded.set(true)
        }
    }

    fun isLoaded(): Boolean = isLoaded.get()

    fun dispose() {
        synchronized(this) {
            booster?.dispose()
            booster = null
            isLoaded.set(false)
        }
    }

    // ── Inference ────────────────────────────────────────────────────────────

    /**
     * Safe inference — NEVER crashes
     */
    fun predictOrNull(features: FloatArray): PredictionResult? {
        val b = booster ?: return null

        return try {
            if (features.size != numFeatures) return null

            val matrix = DMatrix(features, 1, numFeatures.toLong(), Float.NaN)
            val rawProb = b.predict(matrix)[0][0]
            matrix.dispose()

            val label = if (rawProb >= THRESHOLD) 1 else 0
            PredictionResult(label, rawProb)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Strict inference (kept for internal/debug use)
     */
    fun predict(features: FloatArray): PredictionResult {
        val b = booster
            ?: error("Model not loaded. Call load() first.")

        require(features.size == numFeatures)

        val matrix = DMatrix(features, 1, numFeatures.toLong(), Float.NaN)
        val rawProb = b.predict(matrix)[0][0]
        matrix.dispose()

        val label = if (rawProb >= THRESHOLD) 1 else 0
        return PredictionResult(label, rawProb)
    }

    // ── Config ───────────────────────────────────────────────────────────────

    var THRESHOLD: Float = 0.5f

    // ── Data class ───────────────────────────────────────────────────────────

    data class PredictionResult(val label: Int, val probability: Float) {
        val isNimaz: Boolean get() = label == 1

        override fun toString(): String {
            return "PredictionResult(isNimaz=$isNimaz, probability=${"%.4f".format(probability)})"
        }
    }
}