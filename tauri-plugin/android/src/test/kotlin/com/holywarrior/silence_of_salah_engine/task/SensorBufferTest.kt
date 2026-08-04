package com.holywarrior.silence_of_salah_engine.task

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SensorBufferTest {

    @Test
    fun `isFull is false until windowSize samples are collected`() {
        val buffer = SensorBuffer(windowSize = 3)
        assertFalse(buffer.isFull())

        buffer.addSample(1f, 1f, 1f)
        buffer.addSample(2f, 2f, 2f)
        assertFalse(buffer.isFull())

        buffer.addSample(3f, 3f, 3f)
        assertTrue(buffer.isFull())
    }

    @Test
    fun `oldest samples are dropped once the window is over capacity`() {
        val buffer = SensorBuffer(windowSize = 2)
        buffer.addSample(acc = 1f, gyr = 10f, mag = 100f)
        buffer.addSample(acc = 2f, gyr = 20f, mag = 200f)
        buffer.addSample(acc = 3f, gyr = 30f, mag = 300f) // pushes the first sample out

        assertTrue(buffer.isFull())
        assertArrayEquals(
            floatArrayOf(2f, 3f, /* acc */ 20f, 30f, /* gyr */ 200f, 300f /* mag */),
            buffer.toFlatArray(),
            0f
        )
    }

    @Test
    fun `toFlatArray orders values as ACC then GYR then MAG`() {
        val buffer = SensorBuffer(windowSize = 2)
        buffer.addSample(acc = 1f, gyr = 2f, mag = 3f)
        buffer.addSample(acc = 4f, gyr = 5f, mag = 6f)

        assertArrayEquals(floatArrayOf(1f, 4f, 2f, 5f, 3f, 6f), buffer.toFlatArray(), 0f)
    }

    @Test
    fun `popHalf removes half the buffer to allow overlapping windows`() {
        val buffer = SensorBuffer(windowSize = 4)
        buffer.addSample(1f, 1f, 1f)
        buffer.addSample(2f, 2f, 2f)
        buffer.addSample(3f, 3f, 3f)
        buffer.addSample(4f, 4f, 4f)
        assertTrue(buffer.isFull())

        buffer.popHalf()
        assertFalse(buffer.isFull())

        buffer.addSample(5f, 5f, 5f)
        buffer.addSample(6f, 6f, 6f)

        assertTrue(buffer.isFull())
        assertArrayEquals(
            floatArrayOf(3f, 4f, 5f, 6f, 3f, 4f, 5f, 6f, 3f, 4f, 5f, 6f),
            buffer.toFlatArray(),
            0f
        )
    }

    @Test
    fun `clear empties the buffer`() {
        val buffer = SensorBuffer(windowSize = 2)
        buffer.addSample(1f, 1f, 1f)
        buffer.addSample(2f, 2f, 2f)
        assertTrue(buffer.isFull())

        buffer.clear()

        assertFalse(buffer.isFull())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `pop rejects an out-of-range ratio`() {
        SensorBuffer(windowSize = 2).pop(ratio = 1.5f)
    }
}
