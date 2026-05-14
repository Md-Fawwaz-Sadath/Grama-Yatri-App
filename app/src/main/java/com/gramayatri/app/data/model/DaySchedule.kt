package com.gramayatri.app.data.model

data class DaySchedule(
    val startTimes: List<String> = emptyList(),
    val active: Boolean = true
)
