package io.github.sejoung.fontdrop.ui.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object RelativeTime {
    private val monthDayFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
    private val monthDayYearFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)

    fun format(
        nowMillis: Long,
        timestampMillis: Long,
        zone: ZoneId = ZoneId.systemDefault(),
    ): String {
        if (timestampMillis <= 0L) return "—"
        val delta = nowMillis - timestampMillis
        return when {
            delta < 0 -> "Just now"
            delta < ONE_MINUTE -> "Just now"
            delta < ONE_HOUR -> "${delta / ONE_MINUTE}m ago"
            sameCalendarDay(nowMillis, timestampMillis, zone) -> "${delta / ONE_HOUR}h ago"
            isYesterday(nowMillis, timestampMillis, zone) -> "Yesterday"
            sameYear(nowMillis, timestampMillis, zone) ->
                Instant.ofEpochMilli(timestampMillis).atZone(zone).toLocalDate().format(monthDayFormatter)
            else ->
                Instant.ofEpochMilli(timestampMillis).atZone(zone).toLocalDate().format(monthDayYearFormatter)
        }
    }

    private fun localDate(millis: Long, zone: ZoneId): LocalDate =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    private fun sameCalendarDay(now: Long, then: Long, zone: ZoneId): Boolean =
        localDate(now, zone) == localDate(then, zone)

    private fun isYesterday(now: Long, then: Long, zone: ZoneId): Boolean =
        localDate(now, zone).minusDays(1) == localDate(then, zone)

    private fun sameYear(now: Long, then: Long, zone: ZoneId): Boolean =
        localDate(now, zone).year == localDate(then, zone).year

    private const val ONE_MINUTE = 60_000L
    private const val ONE_HOUR = 60 * ONE_MINUTE
}
