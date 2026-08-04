package com.holywarrior.silence_of_salah_engine.task

import android.content.Context
import android.media.AudioManager
import androidx.test.core.app.ApplicationProvider
import com.holywarrior.silence_of_salah_engine.Config
import com.holywarrior.silence_of_salah_engine.audio.AudioProfileManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config as RobolectricConfig

/**
 * This is the single most important piece of business logic in the whole
 * plugin: the hysteresis rules that decide when the phone actually goes
 * silent (and back). Ported byte-for-byte from the original Flutter plugin -
 * these tests exist to prove that porting didn't change the behavior.
 */
@RunWith(RobolectricTestRunner::class)
@RobolectricConfig(sdk = [34])
class MlDecisionEngineTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private lateinit var engine: MlDecisionEngine

    @Before
    fun setUp() {
        engine = MlDecisionEngine(context)
        engine.prepareFreshSession()
    }

    @Test
    fun `stays in default state until the buffer is entirely true for the first time`() {
        // ML_BUFFER_SIZE is 5 - the first 4 true predictions must not go silent yet.
        repeat(Config.ML_BUFFER_SIZE - 1) {
            val snapshot = engine.handlePrediction(true)
            assertEquals("default", snapshot["audioState"])
        }
    }

    @Test
    fun `enters silent once the buffer is entirely true for the first time`() {
        repeat(Config.ML_BUFFER_SIZE - 1) { engine.handlePrediction(true) }

        val snapshot = engine.handlePrediction(true)

        assertEquals("silent", snapshot["audioState"])
        assertEquals(true, snapshot["hasEnteredSilentOnce"])
        assertNull(snapshot["shutdownDeadlineMillis"])
    }

    @Test
    fun `entering silent actually changes the device ringer mode, and restores it afterward`() {
        val original = AudioProfileManager.getCurrentRingerMode(context)

        repeat(Config.ML_BUFFER_SIZE) { engine.handlePrediction(true) }
        assertEquals(AudioManager.RINGER_MODE_SILENT, AudioProfileManager.getCurrentRingerMode(context))

        repeat(Config.ML_BUFFER_SIZE) { engine.handlePrediction(false) }
        assertEquals(original, AudioProfileManager.getCurrentRingerMode(context))
    }

    @Test
    fun `leaving silent sets a shutdown deadline`() {
        repeat(Config.ML_BUFFER_SIZE) { engine.handlePrediction(true) }

        var snapshot: Map<String, Any?> = emptyMap()
        repeat(Config.ML_BUFFER_SIZE) { snapshot = engine.handlePrediction(false) }

        assertEquals("default", snapshot["audioState"])
        assertNotNull(snapshot["shutdownDeadlineMillis"])
    }

    @Test
    fun `a single true is enough to re-enter silent once the session has been silent before`() {
        // First entry requires a fully-true buffer (the conservative path).
        repeat(Config.ML_BUFFER_SIZE) { engine.handlePrediction(true) }
        // A run of false predictions clears the buffer and restores default.
        repeat(Config.ML_BUFFER_SIZE) { engine.handlePrediction(false) }

        // Because hasEnteredSilentOnce is now true, a single true is enough
        // this time - no need to wait for the whole buffer to fill again.
        val snapshot = engine.handlePrediction(true)

        assertEquals("silent", snapshot["audioState"])
        assertNull(snapshot["shutdownDeadlineMillis"])
    }

    @Test
    fun `snapshot reflects whatever was last persisted without requiring a new prediction`() {
        repeat(Config.ML_BUFFER_SIZE) { engine.handlePrediction(true) }

        val snapshot = engine.snapshot()

        assertEquals("silent", snapshot["audioState"])
    }
}
