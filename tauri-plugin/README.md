# silence_of_salah_engine

`silence_of_salah_engine` is a Flutter plugin with an Android-native foreground/background service that keeps running independently of Dart once started. It is built to run sensor-driven ML inference on Android, switch the device audio profile when prayer is detected, and let Flutter manage alarms and debugging through a method-channel API.

## Features

- Starts and stops a native Android service from Flutter.
- Continues running without requiring the Dart VM after service startup.
- Loads a native JSON ML model and performs on-device inference.
- Persists the latest 5 ML boolean outputs and native decision state.
- Switches the device audio profile between default and silent.
- Schedules daily exact alarms that can start the service autonomously.
- Includes a debug-focused example app.

## Platform Support

- Android: supported
- iOS: not implemented

## How It Works

1. Flutter calls `startNativeTask()`.
2. Android starts `SilenceOfSalahEngineForegroundService`.
3. The service registers sensors, loads the native JSON model, and starts a coroutine loop.
4. Sensor windows are converted into features and passed to the native inference engine.
5. The decision engine stores the latest 5 boolean outputs.
6. Audio changes are applied in Android native code.
7. Daily alarms can later start the service again without reopening Flutter.

## Decision Engine Rules

- The plugin stores the latest 5 ML boolean outputs.
- On the first default-to-silent transition, all 5 values must be `true`.
- After the first successful silent transition, any `true` in the recent 5-value buffer is enough to keep or enter silent mode.
- If none of the recent values are `true`, the plugin restores the original device audio mode.
- After a silent-to-default transition, a 10-minute delayed shutdown timer is started.
- If that timer expires without returning to silent mode, the service stops itself.

## Flutter API

Import:

```dart
import 'package:silence_of_salah_engine/silence_of_salah_engine.dart';
```

### Service

```dart
await SilenceOfSalahEngine.startNativeTask(
  args: {'reason': 'manual_start'},
);

await SilenceOfSalahEngine.stopNativeTask();

final status = await SilenceOfSalahEngine.getNativeStatus();
```

### Daily Alarms

```dart
await SilenceOfSalahEngine.scheduleDailyAlarms([
  {
    'id': 530,
    'hour': 5,
    'minute': 30,
    'label': 'Fajr',
    'enabled': true,
  },
]);

final alarms = await SilenceOfSalahEngine.getScheduledAlarms();
await SilenceOfSalahEngine.cancelAllAlarms();
```

Alarm constraints:

- `id` must be unique.
- `hour` must be in `0..23`.
- `minute` must be in `0..59`.
- Exact alarm permission must be granted where Android requires it.

### ML Debug

```dart
final inference = await SilenceOfSalahEngine.triggerMlProcessing(
  features: List<double>.filled(450, 0.0),
);

final decision = await SilenceOfSalahEngine.submitMlDecisionOutput(
  value: true,
);
```

### Audio Debug

```dart
await SilenceOfSalahEngine.debugSetAudioSilent();
await SilenceOfSalahEngine.debugRestoreAudioDefault();
```

### Permissions

```dart
final permissions = await SilenceOfSalahEngine.getPermissionStatus();

await SilenceOfSalahEngine.requestExactAlarmPermission();
await SilenceOfSalahEngine.requestDndAccess();
await SilenceOfSalahEngine.requestBatteryOptimization();
await SilenceOfSalahEngine.requestNotificationPermission();
```

## Native Status Map

`getNativeStatus()` returns a diagnostic map that can include:

- `serviceRunning`
- `modelLoaded`
- `nativeModelPath`
- `recentMlOutputs`
- `audioState`
- `currentRingerMode`
- `originalRingerMode`
- `shutdownDeadlineMillis`
- `scheduledAlarms`
- `permissions`

This map is intended for debugging and testing.

## Alarm Validation Notes

The Flutter API and native Android layer now support daily alarms correctly through the public methods:

- `scheduleDailyAlarms()`
- `getScheduledAlarms()`
- `cancelAllAlarms()`

The native layer also:

- rejects duplicate alarm IDs
- persists scheduled alarms
- reschedules the next day after a trigger
- restores alarms after boot when possible

For final device validation, schedule an alarm a few minutes ahead and check:

- the alarm appears in the returned scheduled alarm list
- Android grants exact alarm permission if required
- the receiver starts the service when the alarm fires
- `adb logcat | findstr SilenceEngine` shows the expected alarm and service logs

## Memory Behavior

The task loop has been kept memory-friendly for long-running execution:

- sensor buffers are fixed-size and reused
- sensor samples are updated in place instead of cloning arrays on each event
- sensors now use `SENSOR_DELAY_GAME` instead of `SENSOR_DELAY_FASTEST`
- listeners are unregistered and cached values are cleared on shutdown
- the service uses a single coroutine loop with explicit cleanup
- only the native JSON model is used at runtime

## Model Assets

The legacy non-native `.model` artifact is no longer used by runtime loading. The plugin now loads only:

- `android/src/main/assets/models/model_100ms_xgb_native.json`

The duplicate binary `.model` copies were removed from:

- `resources/model_100ms_xgb.model`
- `android/src/main/assets/models/model_100ms_xgb.model`

## Example App

The example app is intended for debugging. It includes controls for:

- starting and stopping the service
- scheduling and cancelling daily alarms
- requesting permissions
- forcing audio profile changes
- running ML and decision-engine debug calls

See [notes.md](notes.md) for a short button-by-button reference.

## Required Android Capabilities

The Android implementation depends on:

- foreground service support
- wake lock permission
- notification permission
- Do Not Disturb access
- exact alarm permission
- battery-optimization handling for background reliability

## Verification

The current plugin state has been checked with:

- `flutter test`
- `flutter analyze`
- `flutter build apk --debug` in `example/`

## Running The Example

```bash
cd example
flutter run
```

## Development Checks

```bash
flutter test
flutter analyze
cd example
flutter build apk --debug
```
