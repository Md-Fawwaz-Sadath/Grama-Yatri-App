package com.gramayatri.app.ui.tracking

import com.gramayatri.app.data.model.Route
import com.gramayatri.app.data.model.StopEta

data class LiveTrackingUiState(
    val routeId: String = "",
    val selectedDayKey: String = "",
    val isLiveServiceDay: Boolean = true,
    val routeName: String = "Live Tracking",
    val route: Route? = null,
    val stopEtas: List<StopEta> = emptyList(),
    val savedRouteId: String? = null,
    val savedStopId: String? = null,
    val savedStopEtaText: String? = null,
    val confidenceText: String = "No recent report - ETA estimated",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
