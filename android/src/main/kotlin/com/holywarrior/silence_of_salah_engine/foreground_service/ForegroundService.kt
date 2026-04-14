package com.holywarrior.silence_of_salah_engine.foreground_service

override fun onCreate() {
    super.onCreate()
    Log.d(TAG, "onCreate")

    // ❌ REMOVED:
    // WakeLockHelper.acquire(this)

    NotificationHelper.createChannel(this)

    notificationController = NotificationController(this)
    startForeground(
        NotificationHelper.NOTIFICATION_ID,
        notificationController.build()
    )
    Log.d(TAG, "Foreground notification posted")
}

override fun onDestroy() {
    Log.d(TAG, "onDestroy")

    controller?.stopTask()

    // ❌ REMOVED:
    // WakeLockHelper.release()

    super.onDestroy()
}