package com.gramayatri.app.data.model

data class Route(
    val id: String = "",
    val name: String = "",
    val totalStops: Int = 0,
    val stops: Map<String, Stop> = emptyMap(),
    val busOperator: String = "",
    val busNumber: String = "",
    val weeklySchedule: Map<String, DaySchedule> = emptyMap()
)
