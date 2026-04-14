package com.holywarrior.silence_of_salah_engine.foreground_service

import kotlinx.coroutines.Job

class ForegroundTaskController(
    private val service: SilenceOfSalahEngineForegroundService,
    val notification: NotificationController
) {

    private var job: Job? = null

    private var wakeLockHeld = false

    fun attachJob(job: Job) {
        this.job = job
        SilenceOfSalahEngineForegroundService.setTaskActive(true)

        job.invokeOnCompletion {
            SilenceOfSalahEngineForegroundService.setTaskActive(false)
            releaseWakeLock() // safety fallback
        }
    }

    /**
     * Call this when task actually starts running logic
     */
    fun acquireWakeLock() {
        if (wakeLockHeld) return
        WakeLockHelper.acquire(service)
        wakeLockHeld = true
    }

    /**
     * Safe release (idempotent)
     */
    fun releaseWakeLock() {
        if (!wakeLockHeld) return
        WakeLockHelper.release()
        wakeLockHeld = false
    }

    fun stopTask() {
        job?.cancel()
        releaseWakeLock()
        service.stopSelfSafely()
    }

    fun isActive(): Boolean = job?.isActive == true
}
