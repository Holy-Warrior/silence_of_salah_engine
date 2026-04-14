package com.holywarrior.silence_of_salah_engine.foreground_service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.holywarrior.silence_of_salah_engine.ml_inference.ModelAssetInstaller
import com.holywarrior.silence_of_salah_engine.sensors.SensorsManager
import com.holywarrior.silence_of_salah_engine.task.Task
import com.holywarrior.silence_of_salah_engine.task.TaskStateController

class SilenceOfSalahEngineForegroundService : Service() {

    private var controller: ForegroundTaskController? = null
    private lateinit var notificationController: NotificationController

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        NotificationHelper.createChannel(this)
        notificationController = NotificationController(this)
        startForeground(
            NotificationHelper.NOTIFICATION_ID,
            notificationController.build()
        )

        SensorsManager.initialize(applicationContext)
        val modelPath = ModelAssetInstaller.ensureInstalled(applicationContext)
        val loaded = ModelAssetInstaller.loadModel(modelPath)

        notificationController
            .setTitle("Silence of Salah")
            .setText(
                if (loaded) {
                    "Model ready. Waiting for sensor window..."
                } else {
                    "Model load failed. Check plugin logs."
                }
            )
            .update()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand startId=$startId flags=$flags")

        if (controller?.isActive() == true) {
            return START_STICKY
        }

        val task = pendingTask ?: Task()
        val stateController = pendingStateController ?: TaskStateController()
        pendingTask = null
        pendingStateController = null

        val taskController = ForegroundTaskController(this, notificationController)
        controller = taskController

        TaskRunner.start(
            controller = taskController,
            task = task,
            persistentController = stateController,
            isRecovery = intent?.getBooleanExtra(EXTRA_RECOVERY, false) == true
        )

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "onTaskRemoved")
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        controller?.stopTask()
        controller = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    fun stopSelfSafely() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    companion object {
        private const val TAG = "SilenceFgService"
        const val EXTRA_RECOVERY = "recovery"

        @Volatile
        private var taskActive: Boolean = false

        @JvmStatic
        internal var pendingTask: Task? = null

        @JvmStatic
        internal var pendingStateController: TaskStateController? = null

        @JvmStatic
        fun isTaskRunning(): Boolean = taskActive || pendingTask != null

        @JvmStatic
        fun setTaskActive(active: Boolean) {
            taskActive = active
        }
    }
}
