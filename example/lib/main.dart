import 'dart:async';

import 'package:flutter/material.dart';
import 'package:silence_of_salah_engine/silence_of_salah_engine.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final TextEditingController _hourController = TextEditingController(
    text: '5',
  );
  final TextEditingController _minuteController = TextEditingController(
    text: '30',
  );

  String _log = 'Idle';
  String _platformVersion = 'Unknown';
  Map<Object?, Object?> _nativeStatus = const {};
  Map<Object?, Object?> _permissionStatus = const {};
  List<Map<Object?, Object?>> _scheduledAlarms = const [];
  bool _isLoading = false;
  Timer? _statusTimer;

  @override
  void initState() {
    super.initState();
    unawaited(_bootstrap());
  }

  @override
  void dispose() {
    _statusTimer?.cancel();
    _hourController.dispose();
    _minuteController.dispose();
    super.dispose();
  }

  Future<void> _bootstrap() async {
    await _run(() async {
      await _getPlatform();
      await _refreshStatus();
      _startPolling();
    });
  }

  void _startPolling() {
    _statusTimer?.cancel();
    _statusTimer = Timer.periodic(const Duration(seconds: 3), (_) {
      unawaited(_refreshStatus());
    });
  }

  Future<void> _run(Future<void> Function() action) async {
    setState(() => _isLoading = true);

    try {
      await action();
    } catch (e) {
      _appendLog('ERROR: $e');
    } finally {
      if (mounted) {
        setState(() => _isLoading = false);
      }
    }
  }

  void _appendLog(String message) {
    if (!mounted) return;
    setState(() {
      _log = '${DateTime.now()} -> $message\n$_log';
    });
  }

  Future<void> _getPlatform() async {
    final version = await SilenceOfSalahEngine.getPlatformVersion();
    setState(() => _platformVersion = version ?? 'Unknown');
    _appendLog('Platform: $version');
  }

  Future<void> _refreshStatus() async {
    final status = await SilenceOfSalahEngine.getNativeStatus();
    final permissions = await SilenceOfSalahEngine.getPermissionStatus();
    final alarms = await SilenceOfSalahEngine.getScheduledAlarms();
    if (!mounted) return;
    setState(() {
      _nativeStatus = status ?? const {};
      _permissionStatus = permissions;
      _scheduledAlarms = alarms;
    });
  }

  Future<void> _startTask() async {
    final success = await SilenceOfSalahEngine.startNativeTask(
      args: <String, dynamic>{'reason': 'example_app'},
    );
    _appendLog('Start service -> $success');
    await _refreshStatus();
  }

  Future<void> _stopTask() async {
    final success = await SilenceOfSalahEngine.stopNativeTask();
    _appendLog('Stop service -> $success');
    await _refreshStatus();
  }

  Future<void> _scheduleAlarm() async {
    final hour = int.parse(_hourController.text);
    final minute = int.parse(_minuteController.text);
    final alarms = await SilenceOfSalahEngine.scheduleDailyAlarms(
      <Map<String, Object?>>[
        <String, Object?>{
          'id': hour * 100 + minute,
          'hour': hour,
          'minute': minute,
          'label': 'Example alarm',
        },
      ],
    );
    _appendLog('Scheduled alarms -> $alarms');
    await _refreshStatus();
  }

  Future<void> _clearAlarms() async {
    final success = await SilenceOfSalahEngine.cancelAllAlarms();
    _appendLog('Cancel alarms -> $success');
    await _refreshStatus();
  }

  Future<void> _triggerMlInference() async {
    final features = List<double>.filled(450, 0.0);
    final result = await SilenceOfSalahEngine.triggerMlProcessing(
      features: features,
    );
    _appendLog('ML inference -> $result');
    await _refreshStatus();
  }

  Future<void> _submitMlValue(bool value) async {
    final result = await SilenceOfSalahEngine.submitMlDecisionOutput(
      value: value,
    );
    _appendLog('Decision sample ($value) -> $result');
    await _refreshStatus();
  }

  Future<void> _requestExactAlarmPermission() async {
    final success = await SilenceOfSalahEngine.requestExactAlarmPermission();
    _appendLog('Requested exact alarm permission -> $success');
  }

  Future<void> _requestDnd() async {
    final success = await SilenceOfSalahEngine.requestDndAccess();
    _appendLog('Requested DND access -> $success');
  }

  Future<void> _requestBatteryOptimization() async {
    final success = await SilenceOfSalahEngine.requestBatteryOptimization();
    _appendLog('Requested battery optimization ignore -> $success');
  }

  Future<void> _requestNotifications() async {
    final success = await SilenceOfSalahEngine.requestNotificationPermission();
    _appendLog('Requested notification permission -> $success');
  }

  Future<void> _setSilentAudio() async {
    final result = await SilenceOfSalahEngine.debugSetAudioSilent();
    _appendLog('Debug audio -> silent $result');
    await _refreshStatus();
  }

  Future<void> _restoreDefaultAudio() async {
    final result = await SilenceOfSalahEngine.debugRestoreAudioDefault();
    _appendLog('Debug audio -> default $result');
    await _refreshStatus();
  }

  Widget _statusTile(String label, Object? value) {
    return Container(
      width: double.infinity,
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: const Color(0xFFF3F5F7),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text('$label: ${value ?? "n/a"}'),
    );
  }

  Widget _sectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8, top: 12),
      child: Text(
        title,
        style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('Silence Engine Debug UI')),
        body: SafeArea(
          child: Stack(
            children: [
              ListView(
                padding: const EdgeInsets.all(16),
                children: [
                  Text('Platform: $_platformVersion'),
                  _sectionTitle('Service'),
                  Wrap(
                    spacing: 10,
                    runSpacing: 10,
                    children: [
                      ElevatedButton(
                        onPressed: _isLoading
                            ? null
                            : () => _run(_refreshStatus),
                        child: const Text('Refresh Status'),
                      ),
                      ElevatedButton(
                        onPressed: _isLoading ? null : () => _run(_startTask),
                        child: const Text('Start Service'),
                      ),
                      ElevatedButton(
                        onPressed: _isLoading ? null : () => _run(_stopTask),
                        child: const Text('Stop Service'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  _statusTile(
                    'Service running',
                    _nativeStatus['serviceRunning'],
                  ),
                  _statusTile('Model loaded', _nativeStatus['modelLoaded']),
                  _statusTile('Audio state', _nativeStatus['audioState']),
                  _statusTile(
                    'Current ringer mode',
                    _nativeStatus['currentRingerMode'],
                  ),
                  _statusTile(
                    'Recent ML outputs',
                    _nativeStatus['recentMlOutputs'],
                  ),
                  _statusTile(
                    'Shutdown deadline',
                    _nativeStatus['shutdownDeadlineMillis'],
                  ),
                  _sectionTitle('Permissions'),
                  Wrap(
                    spacing: 10,
                    runSpacing: 10,
                    children: [
                      ElevatedButton(
                        onPressed: _isLoading
                            ? null
                            : () => _run(_requestExactAlarmPermission),
                        child: const Text('Exact Alarm'),
                      ),
                      ElevatedButton(
                        onPressed: _isLoading ? null : () => _run(_requestDnd),
                        child: const Text('DND Access'),
                      ),
                      ElevatedButton(
                        onPressed: _isLoading
                            ? null
                            : () => _run(_requestBatteryOptimization),
                        child: const Text('Battery Ignore'),
                      ),
                      ElevatedButton(
                        onPressed: _isLoading
                            ? null
                            : () => _run(_requestNotifications),
                        child: const Text('Notifications'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  _statusTile('Permission status', _permissionStatus),
                  _sectionTitle('Audio Debug'),
                  Wrap(
                    spacing: 10,
                    runSpacing: 10,
                    children: [
                      ElevatedButton(
                        onPressed: _isLoading
                            ? null
                            : () => _run(_setSilentAudio),
                        child: const Text('Force Silent'),
                      ),
                      ElevatedButton(
                        onPressed: _isLoading
                            ? null
                            : () => _run(_restoreDefaultAudio),
                        child: const Text('Restore Default'),
                      ),
                    ],
                  ),
                  _sectionTitle('Daily Alarm'),
                  Row(
                    children: [
                      Expanded(
                        child: TextField(
                          controller: _hourController,
                          keyboardType: TextInputType.number,
                          decoration: const InputDecoration(labelText: 'Hour'),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: TextField(
                          controller: _minuteController,
                          keyboardType: TextInputType.number,
                          decoration: const InputDecoration(
                            labelText: 'Minute',
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Wrap(
                    spacing: 10,
                    runSpacing: 10,
                    children: [
                      ElevatedButton(
                        onPressed: _isLoading
                            ? null
                            : () => _run(_scheduleAlarm),
                        child: const Text('Schedule Daily'),
                      ),
                      ElevatedButton(
                        onPressed: _isLoading ? null : () => _run(_clearAlarms),
                        child: const Text('Cancel Alarms'),
                      ),
                    ],
                  ),
                  _statusTile('Scheduled alarms', _scheduledAlarms),
                  _sectionTitle('ML Debug'),
                  Wrap(
                    spacing: 10,
                    runSpacing: 10,
                    children: [
                      ElevatedButton(
                        onPressed: _isLoading
                            ? null
                            : () => _run(_triggerMlInference),
                        child: const Text('Run ML Inference'),
                      ),
                      ElevatedButton(
                        onPressed: _isLoading
                            ? null
                            : () => _run(() => _submitMlValue(true)),
                        child: const Text('Submit TRUE'),
                      ),
                      ElevatedButton(
                        onPressed: _isLoading
                            ? null
                            : () => _run(() => _submitMlValue(false)),
                        child: const Text('Submit FALSE'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  const Text(
                    'Use adb logcat | findstr SilenceEngine to inspect lifecycle, alarm, ML, WakeLock, and audio logs.',
                  ),
                  const SizedBox(height: 16),
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(10),
                    color: Colors.black,
                    child: SelectableText(
                      _log,
                      style: const TextStyle(
                        color: Colors.green,
                        fontFamily: 'monospace',
                      ),
                    ),
                  ),
                ],
              ),
              if (_isLoading)
                const Positioned.fill(
                  child: ColoredBox(
                    color: Color(0x22000000),
                    child: Center(child: CircularProgressIndicator()),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}
