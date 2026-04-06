package com.holywarrior.silence_of_salah_engine.foreground_service

import kotlinx.coroutines.*

object TaskRunner {

    /**
     * Start a foreground task with lifecycle management.
     *
     * @param task Task instance extending [BaseForegroundTask]
     * @param persistentController User-defined state controller
     */
    fun <T : Any> start(
        controller: ForegroundTaskController,
        task: BaseForegroundTask<T>,
        persistentController: T,
        isRecovery: Boolean = false
    ) {
        val scope = CoroutineScope(Dispatchers.Default)

        val job = scope.launch {
            try {
                // Call proper start function
                if (isRecovery) {
                    task.onRecover(controller, controller.notification, persistentController)
                } else {
                    task.onStart(controller, controller.notification, persistentController)
                }

                // Optional loop
                if (task.loopIntervalMillis > 0) {
                    while (currentCoroutineContext().isActive) {
                        task.onLoop(controller, controller.notification, persistentController)
                        delay(task.loopIntervalMillis)
                    }
                }

            } catch (e: CancellationException) {
                // expected on cancel
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                task.onDestroy(controller, controller.notification, persistentController)
            }
        }

        controller.attachJob(job)
    }
}