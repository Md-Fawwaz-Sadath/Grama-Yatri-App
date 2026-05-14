package com.gramayatri.app.ui.home

import com.gramayatri.app.data.model.Ping
import com.gramayatri.app.data.model.RouteHealth
import com.gramayatri.app.data.model.RouteSummary

data class HomeHealthUiState(
    val isVisible: Boolean = false,
    val routeHealthMap: Map<String, RouteHealth> = emptyMap(),
    val latestPings: Map<String, Ping?> = emptyMap(),
    val activeAlertCounts: Map<String, Int> = emptyMap(),
    val reportsTodayCount: Int = 0,
    val activeAlertsCount: Int = 0,
    val lastUpdatedRouteName: String? = null,
    val lastUpdatedMinutesAgo: Long? = null,
    val summary: RouteSummary = RouteSummary()
)
