package com.holywarrior.silence_of_salah_engine.foreground_service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.holywarrior.silence_of_salah_engine.task.Task
import com.holywarrior.silence_of_salah_engine.task.TaskStateController

class SilenceOfSalahEngineForegroundService : Service() {

    private var controller: ForegroundTaskController? = null
    private lateinit var notificationController: NotificationController

    companion object {
        private const val TAG = "SilenceEngineService"

        // Static fields to pass task from EngineNativeActions
        var pendingTask: BaseForegroundTask<TaskStateController>? = null
        var pendingStateController: TaskStateController? = null

        @Volatile
        private var isTaskActive: Boolean = false

        internal fun setTaskActive(active: Boolean) {
            isTaskActive = active
        }

        /**
         * Returns true if a task is either pending to start or currently active.
         */
        fun isTaskRunning(): Boolean {
            return pendingTask != null || isTaskActive
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        WakeLockHelper.acquire(this)

        NotificationHelper.createChannel(this)

        notificationController = NotificationController(this)
        startForeground(
            NotificationHelper.NOTIFICATION_ID,
            notificationController.build()
        )
        Log.d(TAG, "Foreground notification posted")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand startId=$startId flags=$flags pendingTask=${pendingTask != null} active=${controller?.isActive() == true}")

        if (controller?.isActive() == true) return START_STICKY

        controller = ForegroundTaskController(this, notificationController)

        val isRecovery = pendingTask == null
        val task = pendingTask ?: Task()
        val stateController = pendingStateController ?: TaskStateController()

        TaskRunner.start(controller!!, task, stateController, isRecovery)
        Log.d(TAG, "TaskRunner.start invoked. isRecovery=$isRecovery")

        pendingTask = null
        pendingStateController = null

        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        controller?.stopTask()
        WakeLockHelper.release()
        super.onDestroy()
    }

    fun stopSelfSafely() {
        Log.d(TAG, "stopSelfSafely")
        stopForeground(true)
        stopSelf()
    }
}
