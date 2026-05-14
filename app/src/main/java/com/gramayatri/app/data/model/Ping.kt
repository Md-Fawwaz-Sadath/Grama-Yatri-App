package com.gramayatri.app.data.model

data class Ping(
    val stopId: String = "",
    val stopName: String = "",
    val stopOrder: Int = 0,
    val type: String = "",
    val reporterName: String = "",
    val reporterUid: String = "",
    val timestamp: Long = 0L,
    val serviceDateKey: String = "",
    val serviceDayKey: String = "",
    val isActive: Boolean = true
)
