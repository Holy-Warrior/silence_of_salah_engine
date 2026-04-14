package com.holywarrior.silence_of_salah_engine.task

import com.holywarrior.silence_of_salah_engine.foreground_service.*
import com.example.sensormanager.SensorsManager
import java.util.concurrent.atomic.AtomicBoolean

class Task : BaseForegroundTask<TaskStateController>() {

    override val loopIntervalMillis: Long = 100

    // 🔒 TRUE thread-safe lock
    private val isInferenceRunning = AtomicBoolean(false)

    private var skippedInferenceCount = 0
    private var lastInferenceStartTime = 0L

    private val INFERENCE_TIMEOUT_MS = 5_000L

    override suspend fun onStart(
        taskController: ForegroundTaskController,
        notificationController: NotificationController,
        stateController: TaskStateController
    ) {
        notificationController
            .setTitle("Silence of Salah")
            .setText("Task started")
            .update()

        SensorsManager.start()
    }

    override suspend fun onRecover(
        taskController: ForegroundTaskController,
        notificationController: NotificationController,
        stateController: TaskStateController
    ) {
        notificationController
            .setTitle("Silence of Salah")
            .setText("Resuming task...")
            .update()

        SensorsManager.start()
    }

    override suspend fun onLoop(
        taskController: ForegroundTaskController,
        notificationController: NotificationController,
        stateController: TaskStateController
    ) {
        val buffer = stateController.sensorBuffer

        // ✅ Sample
        val (acc, gyr, mag) = SensorsManager.getLatestMagnitudes()
        buffer.addSample(acc, gyr, mag)

        if (!buffer.isFull()) return

        val now = System.currentTimeMillis()

        // 🧠 Watchdog check
        if (isInferenceRunning.get()) {
            val elapsed = now - lastInferenceStartTime

            if (elapsed > INFERENCE_TIMEOUT_MS) {
                // ⚠️ Force unlock (rare case)
                isInferenceRunning.set(false)
                skippedInferenceCount = 0
            } else {
                skippedInferenceCount++
                return
            }
        }

        // 🔒 Atomic lock acquisition
        if (!isInferenceRunning.compareAndSet(false, true)) {
            skippedInferenceCount++
            return
        }

        lastInferenceStartTime = now

        try {
            if (!XGBoostInference.isLoaded()) {
                return
            }
            val features = buffer.toFlatArray()
            val prediction = XGBoostInference.predictOrNull(features) ?: return

            notificationController
                .setText(
                    "Pred: ${prediction.label}, Prob: ${prediction.probability}, Skips: $skippedInferenceCount"
                )
                .update()

            skippedInferenceCount = 0

            buffer.popHalf()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isInferenceRunning.set(false)
        }
    }

    override suspend fun onDestroy(
        taskController: ForegroundTaskController,
        notificationController: NotificationController,
        stateController: TaskStateController
    ) {
        SensorsManager.stop()

        stateController.sensorBuffer.clear()

        notificationController
            .setText("Task finished")
            .update()
    }
}

class TaskStateController {
    val sensorBuffer = SensorBuffer(windowSize = 150)
}