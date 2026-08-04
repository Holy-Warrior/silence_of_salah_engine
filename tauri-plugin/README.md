# tauri-plugin-silence-of-salah-engine

A Tauri v2 mobile plugin port of [`silence_of_salah_engine`](https://github.com/Holy-Warrior/silence_of_salah_engine),
the Kotlin/Flutter plugin that detects salah from on-device motion-sensor ML
inference and silences the phone automatically.

**Android only** - same as the original. The plugin drives a persistent
foreground service, exact daily alarms, on-device sensors and ringer-mode
control, none of which have an iOS or desktop equivalent. Every command
compiles fine on every platform, but only does something real on Android -
everywhere else it resolves to `Error::UnsupportedPlatform`. That means you
can register the plugin unconditionally:

```rust
// src-tauri/src/lib.rs
tauri::Builder::default()
    .plugin(tauri_plugin_silence_of_salah_engine::init())
    // ...
```

or, if you'd rather not register a no-op plugin on desktop at all, gate it
yourself with `#[cfg(target_os = "android")]`. Either way, `cargo check` /
`cargo test` work on any machine - you don't need an Android toolchain just
to build or unit-test the Rust side.

## What actually changed vs. the original plugin

The original plugin is ~2,100 lines of Kotlin, of which only the top-level
plugin-registration glue (`SilenceOfSalahEnginePlugin.kt` +
`EngineMethodRouter.kt`, Flutter's `FlutterPlugin` / `MethodChannel`) was
Flutter-specific. Everything else - state persistence, the alarm scheduler,
the foreground service and its coroutine task loop, the pure-Kotlin
XGBoost-style inference engine, the sensor manager, the 5-value hysteresis
decision engine, and the permission helpers - has **zero** Flutter
dependency and is reused here basically unmodified (only two hard-coded
`"flutter"` diagnostic-reason strings were changed to `"tauri"`).

What was rewritten is the glue layer, following Tauri's real mobile-plugin
conventions:

| Original (Flutter)                                  | This plugin (Tauri v2)                                   |
| ----------------------------------------------------- | ---------------------------------------------------------- |
| `SilenceOfSalahEnginePlugin.kt` (`FlutterPlugin`, `ActivityAware`, `MethodChannel.MethodCallHandler`) | `SilenceEnginePlugin.kt` (`@TauriPlugin`, extends `Plugin`) |
| `EngineMethodRouter.kt` (hand-rolled `Map<String, MethodHandler>`) | `@Command`-annotated functions, dispatched by Tauri's `Invoke` reflection |
| `MethodChannel` (Dart) | Rust `#[tauri::command]`s in `src/commands.rs`, forwarding via `PluginHandle::run_mobile_plugin` (`src/mobile.rs`), consumed from JS via `guest-js/index.ts` |
| Dart-side `Future<T>` API | Same method names, camelCase, returning `Promise<T>` from `guest-js` |
| No capability system | Tauri's ACL - see `permissions/default.toml` and `permissions/debug.toml` |

Because Tauri always hands the plugin a single, stable `Activity` for its
whole lifetime, `SilenceEnginePlugin.kt` also drops the
attach/detach/reattach dance the Flutter version needed to rebuild
`EngineNativeActions` around a changing, sometimes-null `Activity`.

## Install

```toml
# src-tauri/Cargo.toml
[dependencies]
tauri-plugin-silence-of-salah-engine = { path = "../tauri-plugin-silence-of-salah-engine" }
```

```json
// package.json
{
  "dependencies": {
    "tauri-plugin-silence-of-salah-engine-api": "workspace:*"
  }
}
```

Grant the commands your window needs in a capability file, e.g.:

```json
// src-tauri/capabilities/default.json
{
  "identifier": "default",
  "windows": ["main"],
  "permissions": [
    "silence-of-salah-engine:default",
    "silence-of-salah-engine:debug"
  ]
}
```

No manual `AndroidManifest.xml` edits are needed - the plugin's manifest
(foreground service declaration, alarm/boot receivers, and the
`FOREGROUND_SERVICE_HEALTH`, `SCHEDULE_EXACT_ALARM`, `POST_NOTIFICATIONS`,
`WAKE_LOCK`, etc. permissions) is merged into your app automatically by the
Android Gradle Plugin's manifest merger when you run `cargo tauri android
build` / `cargo tauri android dev`, exactly like it was for the Flutter
plugin.

The 1.5 MB bundled model (`android/src/main/assets/models/model_100ms_xgb_native.json`)
is copied over from the original repo, so the plugin works out of the box.

## Usage

```ts
import {
  startNativeTask,
  stopNativeTask,
  getNativeStatus,
  scheduleDailyAlarms,
  getScheduledAlarms,
  cancelAllAlarms,
  triggerMlProcessing,
  submitMlDecisionOutput,
  debugSetAudioSilent,
  debugRestoreAudioDefault,
  getPermissionStatus,
  requestExactAlarmPermission,
  requestDndAccess,
  requestBatteryOptimization,
  requestNotificationPermission,
} from 'tauri-plugin-silence-of-salah-engine-api'

// 1. Make sure the required permissions are granted first.
const permissions = await getPermissionStatus()
if (!permissions.allGranted) {
  if (!permissions.exactAlarm) await requestExactAlarmPermission()
  if (!permissions.notifications) await requestNotificationPermission()
  if (!permissions.batteryOptimization) await requestBatteryOptimization()
  if (!permissions.dnd) await requestDndAccess()
  // each of these opens a system settings screen and returns immediately -
  // re-check getPermissionStatus() when the app resumes.
}

// 2. Schedule daily prayer-time alarms that (re)start the engine.
await scheduleDailyAlarms([
  { hour: 5, minute: 12, label: 'Fajr' },
  { hour: 13, minute: 0, label: 'Dhuhr' },
  { hour: 16, minute: 30, label: 'Asr' },
  { hour: 19, minute: 5, label: 'Maghrib' },
  { hour: 20, minute: 35, label: 'Isha' },
])

// 3. Or start the sensor/ML foreground service manually.
await startNativeTask('manual_start')

// 4. Poll status (service state, model state, audio state, alarms, permissions).
const status = await getNativeStatus()

// ...and stop it.
await stopNativeTask()
```

### Command reference

| Function | Returns | Notes |
| --- | --- | --- |
| `getPlatformVersion()` | `string` | e.g. `"Android 15"` |
| `startNativeTask(reason?)` | `boolean` | Starts sensors + the 100ms inference loop. No-ops if already running. |
| `stopNativeTask()` | `boolean` | Stops the service, restores ringer mode if it was silenced. |
| `getNativeStatus()` | `NativeStatus` | Full diagnostic snapshot. |
| `scheduleDailyAlarms(alarms)` | `ScheduledAlarm[]` | Replaces all alarms. Requires the exact-alarm permission. |
| `getScheduledAlarms()` | `ScheduledAlarm[]` | |
| `cancelAllAlarms()` | `boolean` | |
| `triggerMlProcessing(features)` | `MlPrediction` | Debug entry point into the model directly; `features` is a flattened 450-value `[ACC, GYR, MAG]` window. |
| `submitMlDecisionOutput(value)` | `DecisionSnapshot` | Feeds one prediction into the hysteresis buffer (see below). |
| `debugSetAudioSilent()` / `debugRestoreAudioDefault()` | `DecisionSnapshot` | Bypasses the decision engine. Debug only - gated behind the `silence-of-salah-engine:debug` permission set. |
| `getPermissionStatus()` | `PermissionStatus` | |
| `requestExactAlarmPermission()` / `requestDndAccess()` / `requestBatteryOptimization()` / `requestNotificationPermission()` | `boolean` | Each opens the relevant system screen/dialog and resolves immediately - re-poll `getPermissionStatus()` afterward. |

### The decision engine, unchanged

`submitMlDecisionOutput` feeds each boolean prediction into a rolling buffer
of the last `Config.ML_BUFFER_SIZE` (5) outputs and applies the original's
exact hysteresis:

- **default → silent**: needs *all 5* recent outputs `true` the first time
  the engine ever goes silent in a session; after that, *any* `true` in the
  buffer is enough to (re-)enter silent. This makes the very first
  transition deliberately conservative and later re-entries responsive.
- **silent → default**: as soon as *none* of the 5 recent outputs are
  `true`, ringer mode is restored and a 10-minute shutdown deadline is set;
  if a `true` reappears before the deadline, it's cancelled and the service
  keeps running.

## Testing

There are three layers, each catching different kinds of mistakes, and each
needs progressively more setup:

### 1. Rust unit tests - no Android toolchain needed

`src/models.rs`, `src/error.rs` and `src/desktop.rs` all compile and run on
any machine with a Rust toolchain. These check the JSON wire shape (camelCase
field names, optionality) matches what the Kotlin side actually sends/expects,
and that every command fails gracefully off-Android.

```bash
cargo test
```

### 2. Kotlin unit tests - JDK + Android SDK, no emulator

`android/src/test/kotlin` has two kinds of tests:

- Plain JUnit tests for the framework-free logic: `XGBoostInferenceTest`
  (the tree-ensemble math, using a tiny synthetic model), `SensorBufferTest`
  (window/overlap logic), `ScheduledAlarmTest` (next-trigger-time math).
- Robolectric tests for the `Context`-dependent pieces, running against a
  simulated Android runtime on the JVM (no emulator): `EngineStateStoreTest`
  (the JSON-file persistence layer) and `MlDecisionEngineTest` - the most
  important test in the whole suite, since it exercises the exact hysteresis
  rules described below end-to-end, including asserting the real ringer mode
  actually changes.

These need a full Tauri Android project around them to resolve the
`:tauri-android` module the plugin's `android/build.gradle.kts` depends on -
see "End-to-end" below to generate it, then from `examples/tauri-app/src-tauri/gen/android`:

```bash
./gradlew projects            # confirm the exact autogenerated module name first
./gradlew :<module>:testDebugUnitTest
```

`<module>` is almost always `tauri-plugin-silence-of-salah-engine`, but the
Tauri CLI derives it from the crate name at `cargo tauri android init` time,
so it's worth confirming with `./gradlew projects` rather than assuming.

### 3. End-to-end, on a real device or emulator

`examples/tauri-app` is a minimal app (plain HTML/JS, no bundler) with a
button for every command. This is the real test - unit tests can't exercise
actual sensors, actual `AlarmManager` alarms, or actual ringer-mode changes.

```bash
cd examples/tauri-app/src-tauri
cargo tauri android init   # first time only
cargo tauri android dev    # builds, installs, and streams logs from a connected device/emulator
```

See the chat for the full prerequisite/setup command sequence.

### Building the JS bindings package

Only needed if you change `guest-js/index.ts` and want to consume the
compiled `dist-js` output (the example app calls `invoke()` directly and
doesn't need this):

```bash
npm install
npm run build
```

## License

MIT. The original repository does not specify a license (its `LICENSE`
file is a placeholder) - replace this with whatever the upstream project
settles on.
