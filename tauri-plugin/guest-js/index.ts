import { invoke } from '@tauri-apps/api/core'

const PLUGIN_NAME = 'silence-of-salah-engine'

function cmd(name: string): string {
  return `plugin:${PLUGIN_NAME}|${name}`
}

/** `default` = normal ringer mode is active. `silent` = the engine switched the phone to silent. */
export type AudioState = 'default' | 'silent'

export interface ScheduleAlarmInput {
  /** Unique alarm id. Defaults to `hour * 100 + minute` when omitted. */
  id?: number
  /** 0-23 */
  hour: number
  /** 0-59 */
  minute: number
  label?: string
  /** Defaults to `true` when omitted. */
  enabled?: boolean
}

export interface ScheduledAlarm {
  id: number
  hour: number
  minute: number
  label?: string | null
  enabled: boolean
  nextTriggerAtMillis: number
  repeatDaily: boolean
}

export interface MlPrediction {
  label: number
  probability: number
  isPrayerDetected: boolean
}

export interface DecisionSnapshot {
  recentMlOutputs: boolean[]
  audioState: AudioState
  originalRingerMode: number | null
  hasEnteredSilentOnce: boolean
  shutdownDeadlineMillis: number | null
}

export interface PermissionStatus {
  exactAlarm: boolean
  dnd: boolean
  batteryOptimization: boolean
  notifications: boolean
  allGranted: boolean
}

export interface NativeStatus {
  platformVersion: string
  serviceRunning: boolean
  modelLoaded: boolean
  modelPath: string | null
  nativeModelPath: string | null
  recentMlOutputs: boolean[]
  audioState: AudioState
  currentRingerMode: number
  originalRingerMode: number | null
  shutdownDeadlineMillis: number | null
  scheduledAlarms: ScheduledAlarm[]
  permissions: PermissionStatus
}

/** Android build/release string, e.g. `"Android 15"`. */
export async function getPlatformVersion(): Promise<string> {
  return await invoke(cmd('get_platform_version'))
}

/**
 * Starts the native foreground service: registers sensors, loads the ML
 * model and begins the 100ms sensor-window inference loop.
 * No-ops (resolves `false`) if a task is already running or pending.
 */
export async function startNativeTask(reason?: string): Promise<boolean> {
  return await invoke(cmd('start_native_task'), { payload: { reason } })
}

/** Stops the foreground service and restores the original ringer mode if needed. */
export async function stopNativeTask(): Promise<boolean> {
  return await invoke(cmd('stop_native_task'))
}

/** Full diagnostic snapshot: service/model state, audio state, alarms and permissions. */
export async function getNativeStatus(): Promise<NativeStatus> {
  return await invoke(cmd('get_native_status'))
}

/**
 * Replaces all scheduled daily alarms with the given list (an empty list
 * cancels everything). Alarm ids must be unique; the exact-alarm permission
 * must already be granted or this rejects.
 */
export async function scheduleDailyAlarms(
  alarms: ScheduleAlarmInput[]
): Promise<ScheduledAlarm[]> {
  return await invoke(cmd('schedule_daily_alarms'), { payload: { alarms } })
}

export async function getScheduledAlarms(): Promise<ScheduledAlarm[]> {
  return await invoke(cmd('get_scheduled_alarms'))
}

export async function cancelAllAlarms(): Promise<boolean> {
  return await invoke(cmd('cancel_all_alarms'))
}

/**
 * Debug entry point into the on-device XGBoost-style model. `features` must
 * be a flattened `[ACC[0..149], GYR[0..149], MAG[0..149]]` window (450
 * values for the bundled model) - the model loads lazily on first call.
 */
export async function triggerMlProcessing(
  features: number[]
): Promise<MlPrediction> {
  return await invoke(cmd('trigger_ml_processing'), { payload: { features } })
}

/**
 * Feeds one boolean prediction into the 5-value rolling decision buffer and
 * returns the resulting audio-state snapshot. See the README for the exact
 * hysteresis rules.
 */
export async function submitMlDecisionOutput(
  value: boolean
): Promise<DecisionSnapshot> {
  return await invoke(cmd('submit_ml_decision_output'), { payload: { value } })
}

/** Forces silent mode without going through the decision engine. Debug only. */
export async function debugSetAudioSilent(): Promise<DecisionSnapshot> {
  return await invoke(cmd('debug_set_audio_silent'))
}

/** Restores the original ringer mode without going through the decision engine. Debug only. */
export async function debugRestoreAudioDefault(): Promise<DecisionSnapshot> {
  return await invoke(cmd('debug_restore_audio_default'))
}

export async function getPermissionStatus(): Promise<PermissionStatus> {
  return await invoke(cmd('get_permission_status'))
}

/** Opens the system "schedule exact alarms" settings screen (Android 12+). Fire-and-forget. */
export async function requestExactAlarmPermission(): Promise<boolean> {
  return await invoke(cmd('request_exact_alarm_permission'))
}

/** Opens the system "Do Not Disturb access" settings screen. Fire-and-forget. */
export async function requestDndAccess(): Promise<boolean> {
  return await invoke(cmd('request_dnd_access'))
}

/** Opens the system "ignore battery optimizations" dialog for this app. Fire-and-forget. */
export async function requestBatteryOptimization(): Promise<boolean> {
  return await invoke(cmd('request_battery_optimization'))
}

/** Requests `POST_NOTIFICATIONS` at runtime (Android 13+). Fire-and-forget. */
export async function requestNotificationPermission(): Promise<boolean> {
  return await invoke(cmd('request_notification_permission'))
}
