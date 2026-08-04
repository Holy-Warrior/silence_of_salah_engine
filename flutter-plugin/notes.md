# Example App Buttons

This file describes the debug buttons in `example/lib/main.dart` in simple terms.

## Service

- `Refresh Status`: Pull the latest native status, permission state, alarm list, and audio state from Android.
- `Start Service`: Starts the native foreground/background service without depending on the Dart VM after launch.
- `Stop Service`: Requests a full native stop. The task is cancelled, `onDestroy` runs for cleanup, and the service is expected to die cleanly afterward.

## Permissions

- `Exact Alarm`: Opens Android settings for exact-alarm permission when the device requires it.
- `DND Access`: Opens Do Not Disturb policy access settings so the plugin can change the ringer mode.
- `Battery Ignore`: Opens the battery-optimization exemption screen for better background reliability.
- `Notifications`: Requests the notification runtime permission on Android 13+.

## Audio Debug

- `Force Silent`: Immediately switches the device audio profile to silent and updates the plugin's persisted state to match.
- `Restore Default`: Restores the saved pre-silent audio mode and marks the plugin state as back to default.

## Daily Alarm

- `Schedule Daily`: Creates one daily exact alarm using the `Hour` and `Minute` fields.
- `Cancel Alarms`: Removes all alarms that were scheduled by the plugin.
- `Hour`: The alarm hour in 24-hour format.
- `Minute`: The alarm minute.

## ML Debug

- `Run ML Inference`: Runs the native ML model once using a synthetic feature list for quick pipeline testing.
- `Submit TRUE`: Feeds a manual `true` decision sample into the decision engine to test the silent-mode path.
- `Submit FALSE`: Feeds a manual `false` decision sample into the decision engine to test the restore/default path.

## Status Cards

- `Service running`: Whether the native task/service is currently active or pending.
- `Model loaded`: Whether the Android ML model is loaded.
- `Audio state`: The plugin's persisted view of whether it is in `default` or `silent`.
- `Current ringer mode`: The device's actual current Android ringer mode integer.
- `Recent ML outputs`: The latest 5 boolean ML outputs stored by the decision engine.
- `Shutdown deadline`: The delayed-stop timestamp set after a silent-to-default transition.
- `Permission status`: Snapshot of exact alarm, DND, battery optimization, and notification permission state.
- `Scheduled alarms`: The current daily alarms persisted by the plugin.

## Log Panel

- The black log panel records button actions and returned debug data so it is easier to verify behavior while testing.
- Use `adb logcat | findstr SilenceEngine` for the full native logs.
