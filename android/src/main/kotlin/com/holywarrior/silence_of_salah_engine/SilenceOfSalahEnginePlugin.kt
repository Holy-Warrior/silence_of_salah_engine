package com.holywarrior.silence_of_salah_engine

import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class SilenceOfSalahEnginePlugin : FlutterPlugin, MethodChannel.MethodCallHandler {

    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private lateinit var actions: EngineNativeActions
    private lateinit var router: Map<String, MethodHandler>

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        context = binding.applicationContext

        actions = EngineNativeActions(context)
        router = createEngineMethodRouter(actions)

        channel = MethodChannel(binding.binaryMessenger, "silence_of_salah_engine")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        router[call.method]?.invoke(call, result)
            ?: result.notImplemented()
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}