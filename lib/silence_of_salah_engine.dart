import 'package:flutter/services.dart';

class SilenceOfSalahEngine {
  SilenceOfSalahEngine._();

  static const MethodChannel _channel = MethodChannel(
    'silence_of_salah_engine',
  );

  static Future<String?> getPlatformVersion() async {
    try {
      return await _channel.invokeMethod<String>('getPlatformVersion');
    } on PlatformException catch (e) {
      throw Exception('getPlatformVersion failed: ${e.message}');
    }
  }

  static Future<bool> startNativeTask({Map<String, dynamic>? args}) async {
    try {
      final result = await _channel.invokeMethod<bool>('startNativeTask', args);
      return result ?? false;
    } on PlatformException catch (e) {
      throw Exception('startNativeTask failed: ${e.message}');
    }
  }

  static Future<bool> stopNativeTask() async {
    try {
      final result = await _channel.invokeMethod<bool>('stopNativeTask');
      return result ?? false;
    } on PlatformException catch (e) {
      throw Exception('stopNativeTask failed: ${e.message}');
    }
  }

  static Future<Map<Object?, Object?>?> getNativeStatus() async {
    try {
      return await _channel.invokeMethod<Map<Object?, Object?>>(
        'getNativeStatus',
      );
    } on PlatformException catch (e) {
      throw Exception('getNativeStatus failed: ${e.message}');
    }
  }

  static Future<List<Map<Object?, Object?>>> scheduleDailyAlarms(
    List<Map<String, Object?>> alarms,
  ) async {
    try {
      final result = await _channel.invokeListMethod<Object?>(
        'scheduleDailyAlarms',
        alarms,
      );
      return _castMapList(result);
    } on PlatformException catch (e) {
      throw Exception('scheduleDailyAlarms failed: ${e.message}');
    }
  }

  static Future<List<Map<Object?, Object?>>> getScheduledAlarms() async {
    try {
      final result = await _channel.invokeListMethod<Object?>(
        'getScheduledAlarms',
      );
      return _castMapList(result);
    } on PlatformException catch (e) {
      throw Exception('getScheduledAlarms failed: ${e.message}');
    }
  }

  static Future<bool> cancelAllAlarms() async {
    try {
      final result = await _channel.invokeMethod<bool>('cancelAllAlarms');
      return result ?? false;
    } on PlatformException catch (e) {
      throw Exception('cancelAllAlarms failed: ${e.message}');
    }
  }

  static Future<Map<Object?, Object?>> triggerMlProcessing({
    required List<double> features,
  }) async {
    try {
      final result = await _channel.invokeMapMethod<Object?, Object?>(
        'triggerMlProcessing',
        <String, Object?>{'features': features},
      );
      return result ?? const {};
    } on PlatformException catch (e) {
      throw Exception('triggerMlProcessing failed: ${e.message}');
    }
  }

  static Future<Map<Object?, Object?>> submitMlDecisionOutput({
    required bool value,
  }) async {
    try {
      final result = await _channel.invokeMapMethod<Object?, Object?>(
        'submitMlDecisionOutput',
        <String, Object?>{'value': value},
      );
      return result ?? const {};
    } on PlatformException catch (e) {
      throw Exception('submitMlDecisionOutput failed: ${e.message}');
    }
  }

  static Future<Map<Object?, Object?>> debugSetAudioSilent() async {
    try {
      final result = await _channel.invokeMapMethod<Object?, Object?>(
        'debugSetAudioSilent',
      );
      return result ?? const {};
    } on PlatformException catch (e) {
      throw Exception('debugSetAudioSilent failed: ${e.message}');
    }
  }

  static Future<Map<Object?, Object?>> debugRestoreAudioDefault() async {
    try {
      final result = await _channel.invokeMapMethod<Object?, Object?>(
        'debugRestoreAudioDefault',
      );
      return result ?? const {};
    } on PlatformException catch (e) {
      throw Exception('debugRestoreAudioDefault failed: ${e.message}');
    }
  }

  static Future<Map<Object?, Object?>> getPermissionStatus() async {
    try {
      final result = await _channel.invokeMapMethod<Object?, Object?>(
        'getPermissionStatus',
      );
      return result ?? const {};
    } on PlatformException catch (e) {
      throw Exception('getPermissionStatus failed: ${e.message}');
    }
  }

  static Future<bool> requestExactAlarmPermission() async {
    return _invokeBoolean('requestExactAlarmPermission');
  }

  static Future<bool> requestDndAccess() async {
    return _invokeBoolean('requestDndAccess');
  }

  static Future<bool> requestBatteryOptimization() async {
    return _invokeBoolean('requestBatteryOptimization');
  }

  static Future<bool> requestNotificationPermission() async {
    return _invokeBoolean('requestNotificationPermission');
  }

  static Future<bool> _invokeBoolean(String method) async {
    try {
      final result = await _channel.invokeMethod<bool>(method);
      return result ?? false;
    } on PlatformException catch (e) {
      throw Exception('$method failed: ${e.message}');
    }
  }

  static List<Map<Object?, Object?>> _castMapList(List<Object?>? rawList) {
    return (rawList ?? const <Object?>[])
        .whereType<Map<Object?, Object?>>()
        .toList();
  }
}
