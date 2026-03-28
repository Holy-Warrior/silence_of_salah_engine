import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:silence_of_salah_engine/silence_of_salah_engine_method_channel.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  MethodChannelSilenceOfSalahEngine platform = MethodChannelSilenceOfSalahEngine();
  const MethodChannel channel = MethodChannel('silence_of_salah_engine');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (MethodCall methodCall) async {
          return '42';
        });
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
