package com.holywarrior.silence_of_salah_engine.alarm

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ScheduledAlarmTest {

    // A fixed mid-month date (June 15) so day-rollover assertions never hit
    // a month/year boundary edge case.
    private fun fixedMillis(hour: Int, minute: Int, day: Int = 15): Long {
        return Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @Test
    fun `nextTriggerAtMillis stays today when the time has not passed yet`() {
        val now = fixedMillis(hour = 10, minute = 0)
        val alarm = ScheduledAlarm(id = 1, hour = 12, minute = 30)

        val next = alarm.nextTriggerAtMillis(now)
        val cal = Calendar.getInstance().apply { timeInMillis = next }

        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, cal.get(Calendar.MINUTE))
    }

    @Test
    fun `nextTriggerAtMillis rolls over to tomorrow when the time has already passed`() {
        val now = fixedMillis(hour = 14, minute = 0)
        val alarm = ScheduledAlarm(id = 1, hour = 12, minute = 30)

        val next = alarm.nextTriggerAtMillis(now)
        val cal = Calendar.getInstance().apply { timeInMillis = next }

        assertTrue(next > now)
        assertEquals(16, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, cal.get(Calendar.MINUTE))
    }

    @Test
    fun `nextTriggerAtMillis rolls over when the time matches exactly (boundary is exclusive)`() {
        val now = fixedMillis(hour = 12, minute = 30)
        val alarm = ScheduledAlarm(id = 1, hour = 12, minute = 30)

        val next = alarm.nextTriggerAtMillis(now)
        val cal = Calendar.getInstance().apply { timeInMillis = next }

        assertEquals(16, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `toMap contains every field and always reports repeatDaily true`() {
        val alarm = ScheduledAlarm(id = 512, hour = 5, minute = 12, label = "Fajr", enabled = false)

        val map = alarm.toMap(nextTriggerAtMillis = 999L)

        assertEquals(512, map["id"])
        assertEquals(5, map["hour"])
        assertEquals(12, map["minute"])
        assertEquals("Fajr", map["label"])
        assertEquals(false, map["enabled"])
        assertEquals(999L, map["nextTriggerAtMillis"])
        assertEquals(true, map["repeatDaily"])
    }

    @Test
    fun `toJson and fromJson round-trip identically`() {
        val alarm = ScheduledAlarm(id = 1300, hour = 13, minute = 0, label = "Dhuhr", enabled = true)

        val restored = ScheduledAlarm.fromJson(alarm.toJson())

        assertEquals(alarm, restored)
    }

    @Test
    fun `fromJson defaults a missing label to null and a missing enabled to true`() {
        val json = JSONObject().apply {
            put("id", 1)
            put("hour", 5)
            put("minute", 0)
        }

        val alarm = ScheduledAlarm.fromJson(json)

        assertNull(alarm.label)
        assertTrue(alarm.enabled)
    }

    @Test
    fun `fromJson treats a blank label the same as a missing one`() {
        val json = JSONObject().apply {
            put("id", 1)
            put("hour", 5)
            put("minute", 0)
            put("label", "")
        }

        val alarm = ScheduledAlarm.fromJson(json)

        assertNull(alarm.label)
    }
}
