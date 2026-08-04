import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:silence_of_salah_engine/silence_of_salah_engine.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const channel = MethodChannel('silence_of_salah_engine');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (MethodCall methodCall) async {
          switch (methodCall.method) {
            case 'getPlatformVersion':
              return 'Android 42';
            case 'startNativeTask':
              return true;
            case 'stopNativeTask':
              return true;
            case 'getNativeStatus':
              return <String, Object?>{
                'serviceRunning': true,
                'modelLoaded': true,
              };
            case 'scheduleDailyAlarms':
              return <Object?>[
                <String, Object?>{
                  'id': 530,
                  'hour': 5,
                  'minute': 30,
                  'repeatDaily': true,
                },
              ];
            case 'getScheduledAlarms':
              return <Object?>[
                <String, Object?>{
                  'id': 530,
                  'hour': 5,
                  'minute': 30,
                  'repeatDaily': true,
                },
              ];
            case 'cancelAllAlarms':
              return true;
            case 'debugSetAudioSilent':
              return <String, Object?>{'audioState': 'silent'};
            case 'debugRestoreAudioDefault':
              return <String, Object?>{'audioState': 'default'};
            default:
              return null;
          }
        });
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  test('getPlatformVersion proxies through the method channel', () async {
    expect(await SilenceOfSalahEngine.getPlatformVersion(), 'Android 42');
  });

  test('startNativeTask proxies through the method channel', () async {
    expect(await SilenceOfSalahEngine.startNativeTask(), isTrue);
  });

  test('stopNativeTask proxies through the method channel', () async {
    expect(await SilenceOfSalahEngine.stopNativeTask(), isTrue);
  });

  test('getNativeStatus returns the native diagnostics map', () async {
    expect(
      await SilenceOfSalahEngine.getNativeStatus(),
      containsPair('modelLoaded', true),
    );
  });

  test('scheduleDailyAlarms proxies through the method channel', () async {
    final alarms = await SilenceOfSalahEngine.scheduleDailyAlarms([
      {'id': 530, 'hour': 5, 'minute': 30},
    ]);
    expect(alarms.single, containsPair('repeatDaily', true));
  });

  test('getScheduledAlarms proxies through the method channel', () async {
    final alarms = await SilenceOfSalahEngine.getScheduledAlarms();
    expect(alarms.single, containsPair('id', 530));
  });

  test('cancelAllAlarms proxies through the method channel', () async {
    expect(await SilenceOfSalahEngine.cancelAllAlarms(), isTrue);
  });

  test('debugSetAudioSilent proxies through the method channel', () async {
    expect(
      await SilenceOfSalahEngine.debugSetAudioSilent(),
      containsPair('audioState', 'silent'),
    );
  });

  test('debugRestoreAudioDefault proxies through the method channel', () async {
    expect(
      await SilenceOfSalahEngine.debugRestoreAudioDefault(),
      containsPair('audioState', 'default'),
    );
  });
}
