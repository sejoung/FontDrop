package io.github.sejoung.fontdrop.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class RelativeTimeTest {

    private val zone: ZoneId = ZoneOffset.UTC

    @Test
    fun `under one minute shows Just now`() {
        val now = dateTime(2026, 4, 22, 12, 0)
        val then = now.minusSeconds(30)
        assertEquals("Just now", RelativeTime.format(now.toMillis(), then.toMillis(), zone))
    }

    @Test
    fun `under one hour shows minutes ago`() {
        val now = dateTime(2026, 4, 22, 12, 30)
        val then = now.minusMinutes(5)
        assertEquals("5m ago", RelativeTime.format(now.toMillis(), then.toMillis(), zone))
    }

    @Test
    fun `same calendar day shows hours ago`() {
        val now = dateTime(2026, 4, 22, 20, 0)
        val then = dateTime(2026, 4, 22, 6, 0)
        assertEquals("14h ago", RelativeTime.format(now.toMillis(), then.toMillis(), zone))
    }

    @Test
    fun `previous calendar day labelled as Yesterday even when under 24 hours have passed`() {
        val now = dateTime(2026, 4, 22, 1, 0)
        val then = dateTime(2026, 4, 21, 23, 0)
        assertEquals("Yesterday", RelativeTime.format(now.toMillis(), then.toMillis(), zone))
    }

    @Test
    fun `earlier this year shows month and day`() {
        val now = dateTime(2026, 4, 22, 9, 0)
        val then = dateTime(2026, 1, 3, 9, 0)
        assertEquals("Jan 3", RelativeTime.format(now.toMillis(), then.toMillis(), zone))
    }

    @Test
    fun `prior year includes the year`() {
        val now = dateTime(2026, 4, 22, 9, 0)
        val then = dateTime(2025, 12, 31, 9, 0)
        assertEquals("Dec 31, 2025", RelativeTime.format(now.toMillis(), then.toMillis(), zone))
    }

    @Test
    fun `future timestamps fall back to Just now`() {
        val now = dateTime(2026, 4, 22, 9, 0)
        val then = now.plusMinutes(10)
        assertEquals("Just now", RelativeTime.format(now.toMillis(), then.toMillis(), zone))
    }

    @Test
    fun `zero timestamp returns em dash`() {
        val now = dateTime(2026, 4, 22, 9, 0)
        assertEquals("—", RelativeTime.format(now.toMillis(), 0L, zone))
    }

    private fun dateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int) =
        LocalDateTime.of(year, month, day, hour, minute)

    private fun LocalDateTime.toMillis(): Long =
        toInstant(ZoneOffset.UTC).toEpochMilli()
}
