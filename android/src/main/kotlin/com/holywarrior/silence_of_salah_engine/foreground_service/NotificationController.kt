package com.holywarrior.silence_of_salah_engine.foreground_service

import android.app.NotificationManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import android.widget.RemoteViews

class NotificationController(
    private val context: Context
) {

    private val manager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val builder = NotificationCompat.Builder(
        context,
        NotificationHelper.CHANNEL_ID
    ).apply {
        setContentTitle("Silence of Salah")
        setContentText("Starting...")
        setSmallIcon(android.R.drawable.ic_lock_silent_mode)
        setOngoing(true)
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    fun setTitle(title: String): NotificationController {
        builder.setContentTitle(title)
        return this
    }

    fun setText(text: String): NotificationController {
        builder.setContentText(text)
        return this
    }

    fun addAction(icon: Int, title: String, pendingIntent: android.app.PendingIntent): NotificationController {
        builder.addAction(icon, title, pendingIntent)
        return this
    }

    fun setCustomView(remoteViews: RemoteViews): NotificationController {
        builder.setCustomContentView(remoteViews)
        return this
    }

    fun update() {
        // Ensure update always happens on main thread
        mainHandler.post {
            manager.notify(NotificationHelper.NOTIFICATION_ID, builder.build())
        }
    }

    fun build() = builder.build()
}