package com.holywarrior.silence_of_salah_engine.ml_inference

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * `XGBoostInference` has zero Android framework dependency (plain JSON +
 * math), so these run as fast plain-JVM unit tests - no emulator, no
 * Robolectric, just `./gradlew test`.
 */
class XGBoostInferenceTest {

    // A minimal, hand-built model: 3 features, one tree with a single split
    // on feature 0 at 0.5. Mirrors the exact JSON shape
    // NativeForest/TreeNode.fromJson expect.
    private val singleSplitModelJson = """
        {
          "numFeatures": 3,
          "baseMargin": 0.0,
          "trees": [
            {
              "split": "f0",
              "split_condition": 0.5,
              "yes": 1,
              "no": 2,
              "missing": 1,
              "children": [
                { "nodeid": 1, "leaf": 10.0 },
                { "nodeid": 2, "leaf": -10.0 }
              ]
            }
          ]
        }
    """.trimIndent()

    private fun writeModel(json: String): String {
        val file = File.createTempFile("xgb_test_model", ".json")
        file.deleteOnExit()
        file.writeText(json)
        return file.absolutePath
    }

    @After
    fun tearDown() {
        // XGBoostInference is a singleton (object) - reset it between tests
        // so state doesn't leak across test methods.
        XGBoostInference.dispose()
        XGBoostInference.THRESHOLD = 0.5f
    }

    @Test
    fun `predicts prayer detected when the feature falls below the split threshold`() {
        XGBoostInference.load(writeModel(singleSplitModelJson), windowSize = 1)
        assertTrue(XGBoostInference.isLoaded())

        val prediction = XGBoostInference.predictOrNull(floatArrayOf(0.0f, 0.0f, 0.0f))

        requireNotNull(prediction)
        assertEquals(1, prediction.label)
        assertTrue(prediction.isNimaz)
        assertTrue("expected probability > 0.99, got ${prediction.probability}", prediction.probability > 0.99f)
    }

    @Test
    fun `predicts no prayer when the feature is at or above the split threshold`() {
        XGBoostInference.load(writeModel(singleSplitModelJson), windowSize = 1)

        val prediction = XGBoostInference.predictOrNull(floatArrayOf(1.0f, 0.0f, 0.0f))

        requireNotNull(prediction)
        assertEquals(0, prediction.label)
        assertFalse(prediction.isNimaz)
        assertTrue("expected probability < 0.01, got ${prediction.probability}", prediction.probability < 0.01f)
    }

    @Test
    fun `predictOrNull returns null when the feature vector length is wrong`() {
        XGBoostInference.load(writeModel(singleSplitModelJson), windowSize = 1)

        val prediction = XGBoostInference.predictOrNull(floatArrayOf(0.0f, 0.0f))

        assertNull(prediction)
    }

    @Test
    fun `predictOrNull returns null when no model is loaded`() {
        assertFalse(XGBoostInference.isLoaded())
        assertNull(XGBoostInference.predictOrNull(floatArrayOf(0.0f, 0.0f, 0.0f)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `load throws when windowSize times 3 does not match the model's declared feature count`() {
        // windowSize=2 implies 2*3=6 features, but the model declares 3.
        XGBoostInference.load(writeModel(singleSplitModelJson), windowSize = 2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `load throws when the model file does not exist`() {
        XGBoostInference.load("/path/does/not/exist.json", windowSize = 1)
    }

    @Test
    fun `loadSafely returns false instead of throwing on a bad model file`() {
        val ok = XGBoostInference.loadSafely("/path/does/not/exist.json", windowSize = 1)

        assertFalse(ok)
        assertFalse(XGBoostInference.isLoaded())
    }

    @Test
    fun `dispose unloads the model`() {
        XGBoostInference.load(writeModel(singleSplitModelJson), windowSize = 1)
        assertTrue(XGBoostInference.isLoaded())

        XGBoostInference.dispose()

        assertFalse(XGBoostInference.isLoaded())
        assertNull(XGBoostInference.predictOrNull(floatArrayOf(0.0f, 0.0f, 0.0f)))
    }

    @Test
    fun `raising THRESHOLD can flip the label even for a high-confidence prediction`() {
        XGBoostInference.load(writeModel(singleSplitModelJson), windowSize = 1)
        // sigmoid(10) =~ 0.99995, so a threshold just above that flips label to 0.
        XGBoostInference.THRESHOLD = 0.999999f

        val prediction = XGBoostInference.predictOrNull(floatArrayOf(0.0f, 0.0f, 0.0f))

        requireNotNull(prediction)
        assertEquals(0, prediction.label)
    }
}
