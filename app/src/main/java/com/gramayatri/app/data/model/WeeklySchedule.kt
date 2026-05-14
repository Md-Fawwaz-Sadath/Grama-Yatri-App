package com.gramayatri.app.data.model

data class WeeklySchedule(
    val MONDAY: DaySchedule = DaySchedule(),
    val TUESDAY: DaySchedule = DaySchedule(),
    val WEDNESDAY: DaySchedule = DaySchedule(),
    val THURSDAY: DaySchedule = DaySchedule(),
    val FRIDAY: DaySchedule = DaySchedule(),
    val SATURDAY: DaySchedule = DaySchedule(),
    val SUNDAY: DaySchedule = DaySchedule()
)
