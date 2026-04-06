import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:silence_of_salah_engine/silence_of_salah_engine.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('native foreground task smoke test', (WidgetTester tester) async {
    final String? version = await SilenceOfSalahEngine.getPlatformVersion();
    expect(version, isNotNull);
    expect(version!.isNotEmpty, true);

    final bool started = await SilenceOfSalahEngine.startNativeTask();
    expect(started, true);

    await Future<void>.delayed(const Duration(seconds: 5));

    final bool stopped = await SilenceOfSalahEngine.stopNativeTask();
    expect(stopped, true);
  });
}
