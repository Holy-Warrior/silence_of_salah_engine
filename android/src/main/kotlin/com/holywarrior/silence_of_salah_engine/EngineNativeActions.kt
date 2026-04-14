package com.holywarrior.silence_of_salah_engine

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.holywarrior.silence_of_salah_engine.foreground_service.SilenceOfSalahEngineForegroundService
import com.holywarrior.silence_of_salah_engine.ml_inference.ModelAssetInstaller
import com.holywarrior.silence_of_salah_engine.ml_inference.XGBoostInference
import com.holywarrior.silence_of_salah_engine.task.Task
import com.holywarrior.silence_of_salah_engine.task.TaskStateController

class EngineNativeActions(private val context: Context) {

    fun getPlatformVersion(): String {
        return "Android ${Build.VERSION.RELEASE}"
    }

    fun startNativeTask(args: Map<*, *>?) {
        Log.d(TAG, "startNativeTask called with args=$args")

        if (SilenceOfSalahEngineForegroundService.isTaskRunning()) {
            Log.d(TAG, "Task start skipped because a task is already active or pending")
            return
        }

        val intent = Intent(context, SilenceOfSalahEngineForegroundService::class.java)
        val task = Task()
        val stateController = TaskStateController()

        SilenceOfSalahEngineForegroundService.pendingTask = task
        SilenceOfSalahEngineForegroundService.pendingStateController = stateController

        try {
            val componentName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }

            Log.d(TAG, "Foreground service start requested. component=$componentName")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service", e)
            SilenceOfSalahEngineForegroundService.pendingTask = null
            SilenceOfSalahEngineForegroundService.pendingStateController = null
            throw e
        }
    }

    fun stopNativeTask() {
        Log.d(TAG, "stopNativeTask called")
        val intent = Intent(context, SilenceOfSalahEngineForegroundService::class.java)
        context.stopService(intent)
    }

    fun getNativeStatus(): Map<String, Any?> {
        return mapOf(
            "platformVersion" to getPlatformVersion(),
            "serviceRunning" to SilenceOfSalahEngineForegroundService.isTaskRunning(),
            "modelLoaded" to XGBoostInference.isLoaded(),
            "modelPath" to ModelAssetInstaller.installedModelPath,
            "nativeModelPath" to ModelAssetInstaller.installedNativeModelPath
        )
    }

    companion object {
        private const val TAG = "SilenceEngineNative"
    }
}
