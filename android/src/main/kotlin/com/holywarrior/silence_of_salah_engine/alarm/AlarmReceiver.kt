package com.holywarrior.silence_of_salah_engine.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.holywarrior.silence_of_salah_engine.foreground_service.SilenceOfSalahEngineForegroundService

object AlarmExecutionRegistry {

    private var action: ((Context) -> Unit)? = null

    /**
     * Register what should run when alarm fires
     */
    fun register(actionBlock: (Context) -> Unit) {
        action = actionBlock
    }

    fun execute(context: Context) {
        action?.invoke(context)
    }
}

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {

        // 🔥 Execute custom logic
        AlarmExecutionRegistry.execute(context)

        // 🔥 Start foreground service
        val serviceIntent = Intent(
            context,
            SilenceOfSalahEngineForegroundService::class.java
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}