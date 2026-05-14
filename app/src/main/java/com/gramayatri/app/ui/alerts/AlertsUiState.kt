package com.gramayatri.app.ui.alerts

import com.gramayatri.app.data.model.BusAlert
import com.gramayatri.app.data.model.Route

data class AlertsUiState(
    val routes: List<Route> = emptyList(),
    val alerts: List<BusAlert> = emptyList(),
    val selectedRouteId: String = AlertsViewModel.ALL_ROUTES_ID,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
