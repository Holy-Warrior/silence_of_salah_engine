package com.holywarrior.silence_of_salah_engine

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

typealias MethodHandler = (MethodCall, MethodChannel.Result) -> Unit

fun createEngineMethodRouter(actions: EngineNativeActions): Map<String, MethodHandler> {
    return mapOf(

        "getPlatformVersion" to { _, result ->
            try {
                result.success(actions.getPlatformVersion())
            } catch (e: Exception) {
                result.error("PLATFORM_VERSION_ERROR", e.message, null)
            }
        },

        "startNativeTask" to { call, result ->
            try {
                actions.startNativeTask(call.arguments as? Map<*, *>)
                result.success(true)
            } catch (e: Exception) {
                result.error("START_TASK_ERROR", e.message, null)
            }
        },

        "stopNativeTask" to { _, result ->
            try {
                actions.stopNativeTask()
                result.success(true)
            } catch (e: Exception) {
                result.error("STOP_TASK_ERROR", e.message, null)
            }
        },

        "getNativeStatus" to { _, result ->
            try {
                result.success(actions.getNativeStatus())
            } catch (e: Exception) {
                result.error("STATUS_ERROR", e.message, null)
            }
        },

        "getPermissionStatus" to { _, result ->
            try {
                result.success(actions.getPermissionStatus())
            } catch (e: Exception) {
                result.error("PERMISSION_STATUS_ERROR", e.message, null)
            }
        },

        "requestExactAlarmPermission" to { _, result ->
            try {
                actions.requestExactAlarmPermission()
                result.success(true)
            } catch (e: Exception) {
                result.error("REQUEST_EXACT_ALARM_ERROR", e.message, null)
            }
        },

        "requestDndAccess" to { _, result ->
            try {
                actions.requestDndAccess()
                result.success(true)
            } catch (e: Exception) {
                result.error("REQUEST_DND_ERROR", e.message, null)
            }
        },

        "requestBatteryOptimization" to { _, result ->
            try {
                actions.requestBatteryOptimizationIgnore()
                result.success(true)
            } catch (e: Exception) {
                result.error("REQUEST_BATTERY_ERROR", e.message, null)
            }
        },

        "requestNotificationPermission" to { _, result ->
            try {
                actions.requestNotificationPermission()
                result.success(true)
            } catch (e: Exception) {
                result.error("REQUEST_NOTIFICATION_ERROR", e.message, null)
            }
        }

    )
}
