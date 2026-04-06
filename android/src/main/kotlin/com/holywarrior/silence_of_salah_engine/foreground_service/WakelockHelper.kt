package com.holywarrior.silence_of_salah_engine.foreground_service

import android.content.Context
import android.os.PowerManager

object WakeLockHelper {

    private var wakeLock: PowerManager.WakeLock? = null

    fun acquire(context: Context) {
        if (wakeLock?.isHeld == true) return

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "silence_of_salah_engine:wakelock"
        ).apply { acquire() }
    }

    fun release() {
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
    }
}