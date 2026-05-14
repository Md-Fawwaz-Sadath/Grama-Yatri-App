package com.gramayatri.app.data.model

data class BusAlert(
    val alertId: String = "",
    val type: String = "",
    val reason: String = "",
    val note: String = "",
    val reporterName: String = "",
    val reporterUid: String = "",
    val timestamp: Long = 0L,
    val routeId: String = "",
    val routeName: String = "",
    val serviceDateKey: String = "",
    val serviceDayKey: String = ""
)

fun BusAlert.dismissId(): String =
    alertId.ifBlank { "$routeId-$timestamp-$reporterUid-$type" }
