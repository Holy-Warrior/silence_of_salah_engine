import 'dart:async';

import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
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
  String _log = 'Idle';
  String _platformVersion = 'Unknown';
  Map<Object?, Object?> _nativeStatus = const {};
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
    super.dispose();
  }

  Future<void> _bootstrap() async {
    await _run(() async {
      await _requestNotificationPermission();
      await _getPlatform();
      await _refreshStatus();
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

  Future<void> _requestNotificationPermission() async {
    final status = await Permission.notification.request();
    _appendLog('Notification permission -> $status');
  }

  Future<void> _getPlatform() async {
    final version = await SilenceOfSalahEngine.getPlatformVersion();
    setState(() => _platformVersion = version ?? 'Unknown');
    _appendLog('Platform: $version');
  }

  Future<void> _refreshStatus() async {
    final status = await SilenceOfSalahEngine.getNativeStatus();
    setState(() => _nativeStatus = status ?? const {});
    _appendLog('Status: $_nativeStatus');
  }

  Future<void> _startTask() async {
    final success = await SilenceOfSalahEngine.startNativeTask();
    _appendLog('Start Task -> $success');
    _statusTimer?.cancel();
    _statusTimer = Timer.periodic(const Duration(seconds: 2), (_) {
      unawaited(_refreshStatus());
    });
    await _refreshStatus();
  }

  Future<void> _stopTask() async {
    final success = await SilenceOfSalahEngine.stopNativeTask();
    _appendLog('Stop Task -> $success');
    _statusTimer?.cancel();
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

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('Silence Engine Debug UI')),
        body: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Platform: $_platformVersion'),
              const SizedBox(height: 16),
              Wrap(
                spacing: 10,
                runSpacing: 10,
                children: [
                  ElevatedButton(
                    onPressed: _isLoading ? null : () => _run(_getPlatform),
                    child: const Text('Get Platform'),
                  ),
                  ElevatedButton(
                    onPressed: _isLoading ? null : () => _run(_refreshStatus),
                    child: const Text('Get Status'),
                  ),
                  ElevatedButton(
                    onPressed: _isLoading ? null : () => _run(_startTask),
                    child: const Text('Start Task'),
                  ),
                  ElevatedButton(
                    onPressed: _isLoading ? null : () => _run(_stopTask),
                    child: const Text('Stop Task'),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              _statusTile('Service running', _nativeStatus['serviceRunning']),
              _statusTile('Model loaded', _nativeStatus['modelLoaded']),
              _statusTile('Model path', _nativeStatus['modelPath']),
              _statusTile('Native model path', _nativeStatus['nativeModelPath']),
              if (_isLoading) ...[
                const SizedBox(height: 16),
                const Center(child: CircularProgressIndicator()),
              ],
              const SizedBox(height: 16),
              const Text(
                'Notification should keep updating with prayer label/probability while the service runs, even after closing the Flutter UI.',
              ),
              const SizedBox(height: 16),
              Expanded(
                child: Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(10),
                  color: Colors.black,
                  child: SingleChildScrollView(
                    child: Text(
                      _log,
                      style: const TextStyle(
                        color: Colors.green,
                        fontFamily: 'monospace',
                      ),
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
