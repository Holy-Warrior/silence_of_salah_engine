package com.holywarrior.silence_of_salah_engine.foreground_service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.holywarrior.silence_of_salah_engine.task.BaseForegroundTask
import com.holywarrior.silence_of_salah_engine.task.Task
import com.holywarrior.silence_of_salah_engine.task.TaskStateController

class SilenceOfSalahEngineForegroundService : Service() {

    private var controller: ForegroundTaskController? = null
    private lateinit var notificationController: NotificationController

    companion object {
        // Static fields to pass task from EngineNativeActions
        var pendingTask: BaseForegroundTask<Any>? = null
        var pendingStateController: Any? = null

        /**
         * Returns true if a task is either pending to start or currently active.
         */
        fun isTaskRunning(): Boolean {
            return pendingTask != null || controller?.isActive() == true
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        WakeLockHelper.acquire(this)

        NotificationHelper.createChannel(this)

        notificationController = NotificationController(this)
        startForeground(
            NotificationHelper.NOTIFICATION_ID,
            notificationController.build()
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Prevent duplicate execution if task is already active
        if (controller?.isActive() == true) return START_STICKY

        controller = ForegroundTaskController(this, notificationController)

        // Determine if this is a recovery start or a normal start
        val isRecovery = (pendingTask == null)
        val task = pendingTask ?: Task() // create new task if recovering
        val stateController = pendingStateController ?: TaskStateController()

        // Start the task using TaskRunner
        TaskRunner.start(controller!!, task, stateController, isRecovery)

        // Clear pending static fields
        pendingTask = null
        pendingStateController = null

        return START_STICKY
    }

    override fun onDestroy() {
        controller?.stopTask()
        WakeLockHelper.release()
        super.onDestroy()
    }

    fun stopSelfSafely() {
        stopForeground(true)
        stopSelf()
    }
}