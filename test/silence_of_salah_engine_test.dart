import 'package:flutter_test/flutter_test.dart';
import 'package:silence_of_salah_engine/silence_of_salah_engine.dart';
import 'package:silence_of_salah_engine/silence_of_salah_engine_platform_interface.dart';
import 'package:silence_of_salah_engine/silence_of_salah_engine_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockSilenceOfSalahEnginePlatform
    with MockPlatformInterfaceMixin
    implements SilenceOfSalahEnginePlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final SilenceOfSalahEnginePlatform initialPlatform = SilenceOfSalahEnginePlatform.instance;

  test('$MethodChannelSilenceOfSalahEngine is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelSilenceOfSalahEngine>());
  });

  test('getPlatformVersion', () async {
    SilenceOfSalahEngine silenceOfSalahEnginePlugin = SilenceOfSalahEngine();
    MockSilenceOfSalahEnginePlatform fakePlatform = MockSilenceOfSalahEnginePlatform();
    SilenceOfSalahEnginePlatform.instance = fakePlatform;

    expect(await silenceOfSalahEnginePlugin.getPlatformVersion(), '42');
  });
}
