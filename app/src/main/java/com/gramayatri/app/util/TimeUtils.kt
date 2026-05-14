package com.gramayatri.app.util

import android.content.Context
import com.gramayatri.app.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeUtils {
    fun getTodayServiceDateKey(): String =
        serviceDateKey(System.currentTimeMillis())

    fun serviceDateKey(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(timestamp))

    fun isTimestampToday(timestamp: Long): Boolean =
        timestamp > 0L && serviceDateKey(timestamp) == getTodayServiceDateKey()

    fun startOfTodayMillis(): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    fun minutesAgo(timestamp: Long): Long =
        ((System.currentTimeMillis() - timestamp) / 60_000L).coerceAtLeast(0L)

    fun formatMinutesAgo(minutesAgo: Long): String =
        when (minutesAgo) {
            0L -> "just now"
            1L -> "1 min ago"
            else -> "$minutesAgo min ago"
        }

    fun formatMinutesAgo(context: Context, minutesAgo: Long): String =
        when (minutesAgo) {
            0L -> context.getString(R.string.time_just_now)
            1L -> context.getString(R.string.time_one_min_ago)
            else -> context.getString(R.string.time_minutes_ago, minutesAgo)
        }
}
