package com.holywarrior.silence_of_salah_engine.foreground_service

import kotlinx.coroutines.Job

class ForegroundTaskController(
    private val service: SilenceOfSalahEngineForegroundService,
    val notification: NotificationController
) {

    private var job: Job? = null

    fun attachJob(job: Job) {
        this.job = job
    }

    /**
     * Safely stops the task by cancelling the coroutine job and stopping the service.
     */
    fun stopTask() {
        job?.cancel() // coroutine cancellation
        service.stopSelfSafely()
    }

    fun isActive(): Boolean = job?.isActive == true
}