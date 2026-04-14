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

    // ✅ Cache last text to prevent unnecessary updates
    private var lastText: String? = null
    private var lastTitle: String? = null

    fun setTitle(title: String): NotificationController {
        if (title != lastTitle) {
            builder.setContentTitle(title)
            lastTitle = title
        }
        return this
    }

    fun setText(text: String): NotificationController {
        if (text != lastText) {
            builder.setContentText(text)
            lastText = text
        }
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
        mainHandler.post {
            manager.notify(NotificationHelper.NOTIFICATION_ID, builder.build())
        }
    }

    fun build() = builder.build()
}