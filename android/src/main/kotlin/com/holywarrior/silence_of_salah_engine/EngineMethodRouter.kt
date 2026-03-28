package com.holywarrior.silence_of_salah_engine

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

typealias MethodHandler = (MethodCall, MethodChannel.Result) -> Unit

fun createEngineMethodRouter(actions: EngineNativeActions): Map<String, MethodHandler> {
    return mapOf(

        "getPlatformVersion" to { _, result ->
            result.success(actions.getPlatformVersion())
        },

        "startNativeTask" to { call, result ->
            actions.startNativeTask(call.arguments as? Map<*, *>)
            result.success(true)
        }

    )
}