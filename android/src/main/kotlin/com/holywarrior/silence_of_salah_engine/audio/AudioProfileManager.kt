package com.holywarrior.silence_of_salah_engine.audio

import android.content.Context
import android.media.AudioManager
import android.app.NotificationManager

object AudioProfileManager {

    private var previousRingerMode: Int? = null

    /**
     * Check if phone is currently in silent mode
     */
    fun isSilent(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT
    }


    fun hasDndAccess(context: Context): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return nm.isNotificationPolicyAccessGranted
    }

    /**
     * Switch current profile → silent
     * Saves previous state ONLY if not already silent
     */
    fun switchToSilent(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val currentMode = audioManager.ringerMode

        // If already silent, do nothing
        if (currentMode == AudioManager.RINGER_MODE_SILENT) return

        // Save previous mode
        previousRingerMode = currentMode

        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
    }

    /**
     * Restore silent → previous profile
     */
    fun restorePreviousProfile(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val previous = previousRingerMode ?: return

        audioManager.ringerMode = previous

        // Clear after restore to avoid stale state
        previousRingerMode = null
    }
}