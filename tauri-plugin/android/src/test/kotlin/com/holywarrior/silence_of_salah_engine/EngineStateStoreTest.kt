package com.holywarrior.silence_of_salah_engine

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.holywarrior.silence_of_salah_engine.alarm.ScheduledAlarm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * EngineStateStore touches `Context.filesDir`, so it needs a real (or
 * Robolectric-simulated) Android runtime - these are not plain-JVM tests.
 * Robolectric gives every test method its own isolated app-data directory,
 * so no manual cleanup between tests is required.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EngineStateStoreTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `load returns fresh defaults when nothing has been persisted yet`() {
        val state = EngineStateStore.load(context)

        assertEquals(emptyList<Boolean>(), state.recentMlOutputs)
        assertNull(state.originalRingerMode)
        assertEquals(ManagedAudioState.DEFAULT, state.audioState)
        assertTrue(!state.hasEnteredSilentOnce)
        assertNull(state.shutdownDeadlineMillis)
    }

    @Test
    fun `save then load round-trips every field`() {
        val saved = EnginePersistentState(
            recentMlOutputs = listOf(true, false, true),
            originalRingerMode = 2,
            audioState = ManagedAudioState.SILENT,
            hasEnteredSilentOnce = true,
            shutdownDeadlineMillis = 123_456_789L
        )

        EngineStateStore.save(context, saved)
        val loaded = EngineStateStore.load(context)

        assertEquals(saved, loaded)
    }

    @Test
    fun `appendMlOutput keeps only the most recent ML_BUFFER_SIZE outputs`() {
        repeat(Config.ML_BUFFER_SIZE + 2) { index ->
            EngineStateStore.appendMlOutput(context, index % 2 == 0)
        }

        val state = EngineStateStore.load(context)

        assertEquals(Config.ML_BUFFER_SIZE, state.recentMlOutputs.size)
        // The oldest two outputs (index 0 and 1) should have been dropped -
        // what remains is exactly the last ML_BUFFER_SIZE values pushed in.
        val expected = (2 until Config.ML_BUFFER_SIZE + 2).map { it % 2 == 0 }
        assertEquals(expected, state.recentMlOutputs)
    }

    @Test
    fun `resetForFreshServiceStart clears the buffer and records the original ringer mode`() {
        EngineStateStore.appendMlOutput(context, true)
        EngineStateStore.appendMlOutput(context, true)

        val fresh = EngineStateStore.resetForFreshServiceStart(context, originalRingerMode = 1)

        assertEquals(emptyList<Boolean>(), fresh.recentMlOutputs)
        assertEquals(1, fresh.originalRingerMode)
        assertEquals(ManagedAudioState.DEFAULT, fresh.audioState)
        assertTrue(!fresh.hasEnteredSilentOnce)
        assertNull(fresh.shutdownDeadlineMillis)
    }

    @Test
    fun `updateAlarms persists alarms sorted by hour then minute then id`() {
        val unsorted = listOf(
            ScheduledAlarm(id = 2000, hour = 20, minute = 0),
            ScheduledAlarm(id = 512, hour = 5, minute = 12),
            ScheduledAlarm(id = 513, hour = 5, minute = 0),
        )

        val updated = EngineStateStore.updateAlarms(context, unsorted)

        assertEquals(listOf(513, 512, 2000), updated.alarms.map { it.id })
    }
}
