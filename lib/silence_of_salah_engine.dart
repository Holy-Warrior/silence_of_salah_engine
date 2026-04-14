import 'package:flutter/services.dart';

class SilenceOfSalahEngine {
  SilenceOfSalahEngine._();

  static const MethodChannel _channel =
      MethodChannel('silence_of_salah_engine');

  // -------------------------------
  // Public API
  // -------------------------------

  static Future<String?> getPlatformVersion() async {
    try {
      return await _channel.invokeMethod<String>('getPlatformVersion');
    } on PlatformException catch (e) {
      throw Exception("getPlatformVersion failed: ${e.message}");
    }
  }

  static Future<bool> startNativeTask({Map<String, dynamic>? args}) async {
    try {
      final result =
          await _channel.invokeMethod<bool>('startNativeTask', args);
      return result ?? false;
    } on PlatformException catch (e) {
      throw Exception("startNativeTask failed: ${e.message}");
    }
  }

  static Future<bool> stopNativeTask() async {
    try {
      final result = await _channel.invokeMethod<bool>('stopNativeTask');
      return result ?? false;
    } on PlatformException catch (e) {
      throw Exception("stopNativeTask failed: ${e.message}");
    }
  }

  static Future<Map<Object?, Object?>?> getNativeStatus() async {
    try {
      return await _channel.invokeMethod<Map<Object?, Object?>>('getNativeStatus');
    } on PlatformException catch (e) {
      throw Exception("getNativeStatus failed: ${e.message}");
    }
  }
}
