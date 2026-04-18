package com.holywarrior.silence_of_salah_engine.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object AlarmScheduler {

    private const val REQUEST_CODE_BASE = 5000

    private val alarms = mutableListOf<ScheduledAlarm>()

    /**
     * Schedule a set of alarms for a day
     */
    fun scheduleAlarms(
        context: Context,
        timesInMillis: List<Long>,
        repeatDaily: Boolean
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        timesInMillis.forEachIndexed { index, time ->

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("alarm_id", REQUEST_CODE_BASE + index)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_BASE + index,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (repeatDaily) {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent
                )
            }

            alarms.add(
                ScheduledAlarm(
                    id = REQUEST_CODE_BASE + index,
                    triggerAtMillis = time,
                    repeatDaily = repeatDaily
                )
            )
        }
    }

    /**
     * Retrieve all scheduled alarms
     */
    fun getAlarms(): List<ScheduledAlarm> {
        return alarms.toList()
    }

    /**
     * Disable all alarms
     */
    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarms.forEach {
            val intent = Intent(context, AlarmReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                it.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
        }

        alarms.clear()
    }
}