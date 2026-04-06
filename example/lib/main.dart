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
  String _log = "Idle";
  String _platformVersion = "Unknown";
  bool _isLoading = false;

  Future<void> _run(Future<void> Function() action) async {
    setState(() => _isLoading = true);

    try {
      await action();
    } catch (e) {
      _appendLog("❌ ERROR: $e");
    }

    setState(() => _isLoading = false);
  }

  void _appendLog(String message) {
    setState(() {
      _log = "${DateTime.now()} → $message\n$_log";
    });
  }

  Future<void> _getPlatform() async {
    final version = await SilenceOfSalahEngine.getPlatformVersion();
    setState(() => _platformVersion = version ?? "Unknown");
    _appendLog("Platform: $version");
  }

  Future<void> _startTask() async {
    final success = await SilenceOfSalahEngine.startNativeTask();
    _appendLog("Start Task → $success");
  }

  Future<void> _stopTask() async {
    final success = await SilenceOfSalahEngine.stopNativeTask();
    _appendLog("Stop Task → $success");
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('Silence Engine Debug UI')),
        body: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            children: [
              Text("Platform: $_platformVersion"),
              const SizedBox(height: 20),
              Wrap(
                spacing: 10,
                children: [
                  ElevatedButton(
                    onPressed: _isLoading ? null : () => _run(_getPlatform),
                    child: const Text("Get Platform"),
                  ),
                  ElevatedButton(
                    onPressed: _isLoading ? null : () => _run(_startTask),
                    child: const Text("Start Task"),
                  ),
                  ElevatedButton(
                    onPressed: _isLoading ? null : () => _run(_stopTask),
                    child: const Text("Stop Task"),
                  ),
                ],
              ),
              const SizedBox(height: 20),
              if (_isLoading) const CircularProgressIndicator(),
              const SizedBox(height: 20),
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