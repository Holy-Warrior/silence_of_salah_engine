package com.holywarrior.silence_of_salah_engine.foreground_service

import kotlinx.coroutines.*

object TaskRunner {

    fun <T : Any> start(
        controller: ForegroundTaskController,
        task: BaseForegroundTask<T>,
        persistentController: T,
        isRecovery: Boolean = false
    ) {
        val scope = CoroutineScope(Dispatchers.Default)

        val job = scope.launch {
            try {
                // IMPORTANT: wake lock now belongs to task execution lifecycle
                controller.acquireWakeLock()

                if (isRecovery) {
                    task.onRecover(controller, controller.notification, persistentController)
                } else {
                    task.onStart(controller, controller.notification, persistentController)
                }

                if (task.loopIntervalMillis > 0) {
                    while (currentCoroutineContext().isActive) {
                        task.onLoop(controller, controller.notification, persistentController)
                        delay(task.loopIntervalMillis)
                    }
                }

            } catch (e: CancellationException) {
                // normal shutdown
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                task.onDestroy(controller, controller.notification, persistentController)
                controller.releaseWakeLock()
            }
        }

        controller.attachJob(job)
    }
}