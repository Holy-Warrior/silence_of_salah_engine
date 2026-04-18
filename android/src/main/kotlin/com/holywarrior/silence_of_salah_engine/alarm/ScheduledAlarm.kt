package com.holywarrior.silence_of_salah_engine.alarm

data class ScheduledAlarm(
    val id: Int,
    val triggerAtMillis: Long,
    val repeatDaily: Boolean
)