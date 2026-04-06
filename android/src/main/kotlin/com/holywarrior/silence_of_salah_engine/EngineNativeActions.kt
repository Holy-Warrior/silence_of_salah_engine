package com.holywarrior.silence_of_salah_engine

import android.content.Context
import android.content.Intent
import android.os.Build
import com.holywarrior.silence_of_salah_engine.foreground_service.SilenceOfSalahEngineForegroundService
import com.holywarrior.silence_of_salah_engine.task.Task
import com.holywarrior.silence_of_salah_engine.task.TaskStateController

class EngineNativeActions(private val context: Context) {

    fun getPlatformVersion(): String {
        return "Android ${Build.VERSION.RELEASE}"
    }

    fun startNativeTask(args: Map<*, *>?) {
        // Only start a new task if no task is currently active or pending
        if (SilenceOfSalahEngineForegroundService.isTaskRunning()) {
            // Task already running or pending, do nothing
            return
        }

        val intent = Intent(context, SilenceOfSalahEngineForegroundService::class.java)

        // Create a fresh Task instance and its state
        val task = Task()
        val stateController = TaskStateController()

        // Pass the task to the service using static pending fields
        SilenceOfSalahEngineForegroundService.pendingTask = task
        SilenceOfSalahEngineForegroundService.pendingStateController = stateController

        // Start the foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopNativeTask() {
        val intent = Intent(context, SilenceOfSalahEngineForegroundService::class.java)
        context.stopService(intent)
    }
}