package com.gramayatri.app.data.model

data class RouteCardItem(
    val route: Route,
    val selectedDayKey: String,
    val lastPingReporterName: String?,
    val lastPingMinutesAgo: Long?,
    val isActive: Boolean,
    val health: RouteHealth = RouteHealth.NO_RECENT_DATA,
    val activeAlertCount: Int = 0
)
