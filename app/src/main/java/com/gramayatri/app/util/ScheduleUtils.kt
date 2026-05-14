package com.gramayatri.app.util

import android.content.Context
import com.gramayatri.app.R
import com.gramayatri.app.data.model.Route
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ScheduleUtils {
    private val orderedDayKeys = listOf(
        "MONDAY",
        "TUESDAY",
        "WEDNESDAY",
        "THURSDAY",
        "FRIDAY",
        "SATURDAY",
        "SUNDAY"
    )

    fun weekDayKeys(): List<String> = orderedDayKeys

    fun getCurrentDayKey(): String = todayKey()

    fun getServiceDateKeyForSelectedWeekday(selectedDay: String): String? =
        if (selectedDay == getCurrentDayKey()) TimeUtils.getTodayServiceDateKey() else null

    fun todayKey(): String = dayKey(Calendar.getInstance())

    fun tomorrowKey(): String =
        dayKey(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) })

    fun hasScheduleForDay(route: Route, dayKey: String): Boolean =
        route.weeklySchedule[dayKey]?.active == true

    fun shouldShowRouteForDay(route: Route, dayKey: String): Boolean =
        route.weeklySchedule.isEmpty() || route.weeklySchedule[dayKey]?.active == true

    fun getScheduleForSelectedDay(route: Route, dayKey: String) =
        route.weeklySchedule[dayKey]

    fun startTimesForDay(route: Route, dayKey: String): List<String> =
        route.weeklySchedule[dayKey]
            ?.takeIf { it.active }
            ?.startTimes
            .orEmpty()

    fun scheduleLine(context: Context, label: String, route: Route, dayKey: String): String {
        if (route.weeklySchedule.isEmpty()) {
            return context.getString(R.string.schedule_not_available)
        }
        val schedule = getScheduleForSelectedDay(route, dayKey)
        val display = when {
            schedule == null -> context.getString(R.string.schedule_not_available)
            schedule.active && schedule.startTimes.isEmpty() -> context.getString(R.string.schedule_no_times)
            schedule.active -> schedule.startTimes.joinToString(" | ") { formatStartTime(it) }
            else -> context.getString(R.string.schedule_not_available)
        }
        return context.getString(R.string.schedule_line_format, label, display)
    }

    fun selectedDayScheduleLine(context: Context, route: Route, dayKey: String): String =
        scheduleLine(context, dayDisplayLabel(context, dayKey), route, dayKey)

    fun todayScheduleLine(context: Context, route: Route): String =
        scheduleLine(context, context.getString(R.string.filter_today), route, todayKey())

    fun tomorrowScheduleLine(context: Context, route: Route): String =
        scheduleLine(context, context.getString(R.string.filter_tomorrow), route, tomorrowKey())

    fun scheduleSpeechLine(context: Context, route: Route): String? {
        val times = startTimesForDay(route, todayKey())
        if (times.isEmpty()) return null
        return context.getString(
            R.string.tts_schedule_today_format,
            times.joinToString(", ") { formatStartTime(it) }
        )
    }

    fun routeMetaLine(context: Context, route: Route): String? {
        val parts = listOfNotNull(
            route.busOperator.takeIf { it.isNotBlank() }?.let {
                context.getString(R.string.operator_line_format, displayOperator(context, it))
            },
            route.busNumber.takeIf { it.isNotBlank() }?.let {
                context.getString(R.string.bus_number_line_format, it)
            }
        )
        return parts.takeIf { it.isNotEmpty() }?.joinToString("  |  ")
    }

    fun displayOperator(context: Context, operator: String): String =
        when (operator.uppercase(Locale.US)) {
            "KSRTC" -> context.getString(R.string.operator_ksrtc)
            "BMTC" -> context.getString(R.string.operator_bmtc)
            "PRIVATE" -> context.getString(R.string.operator_private)
            "SCHOOL" -> context.getString(R.string.operator_school)
            "OTHER" -> context.getString(R.string.operator_other)
            else -> operator
        }

    fun dayDisplayLabel(context: Context, dayKey: String): String =
        when (dayKey) {
            "MONDAY" -> context.getString(R.string.day_mon)
            "TUESDAY" -> context.getString(R.string.day_tue)
            "WEDNESDAY" -> context.getString(R.string.day_wed)
            "THURSDAY" -> context.getString(R.string.day_thu)
            "FRIDAY" -> context.getString(R.string.day_fri)
            "SATURDAY" -> context.getString(R.string.day_sat)
            "SUNDAY" -> context.getString(R.string.day_sun)
            else -> dayKey
        }

    fun dayFullDisplayLabel(context: Context, dayKey: String): String =
        when (dayKey) {
            "MONDAY" -> context.getString(R.string.day_monday)
            "TUESDAY" -> context.getString(R.string.day_tuesday)
            "WEDNESDAY" -> context.getString(R.string.day_wednesday)
            "THURSDAY" -> context.getString(R.string.day_thursday)
            "FRIDAY" -> context.getString(R.string.day_friday)
            "SATURDAY" -> context.getString(R.string.day_saturday)
            "SUNDAY" -> context.getString(R.string.day_sunday)
            else -> dayDisplayLabel(context, dayKey)
        }

    private fun dayKey(calendar: Calendar): String =
        when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "MONDAY"
            Calendar.TUESDAY -> "TUESDAY"
            Calendar.WEDNESDAY -> "WEDNESDAY"
            Calendar.THURSDAY -> "THURSDAY"
            Calendar.FRIDAY -> "FRIDAY"
            Calendar.SATURDAY -> "SATURDAY"
            else -> "SUNDAY"
        }

    fun formatStartTime(rawTime: String): String {
        val cleaned = rawTime.trim()
        return runCatching {
            val parser = SimpleDateFormat("HH:mm", Locale.US)
            val formatter = SimpleDateFormat("hh:mm a", Locale.US)
            formatter.format(parser.parse(cleaned) ?: return cleaned)
        }.getOrElse { cleaned }
    }
}
