package com.holywarrior.silence_of_salah_engine

import android.app.Activity
import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class SilenceOfSalahEnginePlugin :
    FlutterPlugin,
    MethodChannel.MethodCallHandler,
    ActivityAware {

    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private var activity: Activity? = null

    private lateinit var actions: EngineNativeActions
    private lateinit var router: Map<String, MethodHandler>

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        context = binding.applicationContext

        actions = EngineNativeActions(context) // temp, will update when activity attaches
        router = createEngineMethodRouter(actions)

        channel = MethodChannel(binding.binaryMessenger, Config.METHOD_CHANNEL_NAME)
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        router[call.method]?.invoke(call, result)
            ?: result.notImplemented()
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    // ─────────────────────────────────────────────
    // ActivityAware
    // ─────────────────────────────────────────────

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity

        // 🔥 Recreate actions with activity support
        actions = EngineNativeActions(context, activity)
        router = createEngineMethodRouter(actions)
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
        actions = EngineNativeActions(context, activity)
        router = createEngineMethodRouter(actions)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }
}
