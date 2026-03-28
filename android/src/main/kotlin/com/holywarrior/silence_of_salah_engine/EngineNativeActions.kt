package com.holywarrior.silence_of_salah_engine

import android.content.Context
import android.os.Build

class NativeHandler(private val context: Context) {

    fun getPlatformVersion(): String {
        return "Android ${Build.VERSION.RELEASE}"
    }

    fun startNativeTask(args: Map<*, *>?) {
        // TODO: your logic
    }
}