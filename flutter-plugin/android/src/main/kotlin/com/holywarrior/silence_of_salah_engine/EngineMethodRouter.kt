package com.holywarrior.silence_of_salah_engine

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

typealias MethodHandler = (MethodCall, MethodChannel.Result) -> Unit

fun createEngineMethodRouter(actions: EngineNativeActions): Map<String, MethodHandler> {
    return mapOf(
        "getPlatformVersion" to { _, result ->
            runCatching { actions.getPlatformVersion() }
                .onSuccess(result::success)
                .onFailure { result.error("PLATFORM_VERSION_ERROR", it.message, null) }
        },
        "startNativeTask" to { call, result ->
            runCatching {
                actions.startNativeTask(call.arguments as? Map<*, *>)
                true
            }.onSuccess(result::success)
                .onFailure { result.error("START_TASK_ERROR", it.message, null) }
        },
        "stopNativeTask" to { _, result ->
            runCatching {
                actions.stopNativeTask()
                true
            }.onSuccess(result::success)
                .onFailure { result.error("STOP_TASK_ERROR", it.message, null) }
        },
        "getNativeStatus" to { _, result ->
            runCatching { actions.getNativeStatus() }
                .onSuccess(result::success)
                .onFailure { result.error("STATUS_ERROR", it.message, null) }
        },
        "scheduleDailyAlarms" to { call, result ->
            runCatching {
                @Suppress("UNCHECKED_CAST")
                actions.scheduleDailyAlarms(call.arguments as? List<Map<String, Any?>> ?: emptyList())
            }.onSuccess(result::success)
                .onFailure { result.error("SCHEDULE_ALARMS_ERROR", it.message, null) }
        },
        "getScheduledAlarms" to { _, result ->
            runCatching { actions.getScheduledAlarms() }
                .onSuccess(result::success)
                .onFailure { result.error("GET_ALARMS_ERROR", it.message, null) }
        },
        "cancelAllAlarms" to { _, result ->
            runCatching {
                actions.cancelAllAlarms()
                true
            }.onSuccess(result::success)
                .onFailure { result.error("CANCEL_ALARMS_ERROR", it.message, null) }
        },
        "triggerMlProcessing" to { call, result ->
            runCatching {
                @Suppress("UNCHECKED_CAST")
                val args = call.arguments as? Map<String, Any?>
                val features = args?.get("features") as? List<Number>
                    ?: throw IllegalArgumentException("features must be provided as a numeric list.")
                actions.triggerMlProcessing(features)
            }.onSuccess(result::success)
                .onFailure { result.error("TRIGGER_ML_ERROR", it.message, null) }
        },
        "submitMlDecisionOutput" to { call, result ->
            runCatching {
                @Suppress("UNCHECKED_CAST")
                val args = call.arguments as? Map<String, Any?>
                val value = args?.get("value") as? Boolean
                    ?: throw IllegalArgumentException("value must be provided as a boolean.")
                actions.submitMlDecisionOutput(value)
            }.onSuccess(result::success)
                .onFailure { result.error("SUBMIT_ML_OUTPUT_ERROR", it.message, null) }
        },
        "debugSetAudioSilent" to { _, result ->
            runCatching { actions.debugSetAudioSilent() }
                .onSuccess(result::success)
                .onFailure { result.error("DEBUG_AUDIO_SILENT_ERROR", it.message, null) }
        },
        "debugRestoreAudioDefault" to { _, result ->
            runCatching { actions.debugRestoreAudioDefault() }
                .onSuccess(result::success)
                .onFailure { result.error("DEBUG_AUDIO_DEFAULT_ERROR", it.message, null) }
        },
        "getPermissionStatus" to { _, result ->
            runCatching { actions.getPermissionStatus() }
                .onSuccess(result::success)
                .onFailure { result.error("PERMISSION_STATUS_ERROR", it.message, null) }
        },
        "requestExactAlarmPermission" to { _, result ->
            runCatching {
                actions.requestExactAlarmPermission()
                true
            }.onSuccess(result::success)
                .onFailure { result.error("REQUEST_EXACT_ALARM_ERROR", it.message, null) }
        },
        "requestDndAccess" to { _, result ->
            runCatching {
                actions.requestDndAccess()
                true
            }.onSuccess(result::success)
                .onFailure { result.error("REQUEST_DND_ERROR", it.message, null) }
        },
        "requestBatteryOptimization" to { _, result ->
            runCatching {
                actions.requestBatteryOptimizationIgnore()
                true
            }.onSuccess(result::success)
                .onFailure { result.error("REQUEST_BATTERY_ERROR", it.message, null) }
        },
        "requestNotificationPermission" to { _, result ->
            runCatching {
                actions.requestNotificationPermission()
                true
            }.onSuccess(result::success)
                .onFailure { result.error("REQUEST_NOTIFICATION_ERROR", it.message, null) }
        }
    )
}
