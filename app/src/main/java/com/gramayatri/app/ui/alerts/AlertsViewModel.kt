package com.gramayatri.app.ui.alerts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramayatri.app.data.model.BusAlert
import com.gramayatri.app.data.model.Route
import com.gramayatri.app.data.model.dismissId
import com.gramayatri.app.data.repository.FirebaseRepository
import com.gramayatri.app.data.repository.UserPrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val repository: FirebaseRepository,
    private val prefsRepository: UserPrefsRepository
) : ViewModel() {

    private val selectedRouteId = MutableStateFlow(ALL_ROUTES_ID)
    private val dismissedAlertKeys = MutableStateFlow(prefsRepository.getDismissedAlertIds())

    val uiState: StateFlow<AlertsUiState> = combine(
        repository.getRoutes(),
        selectedRouteId,
        dismissedAlertKeys
    ) { routes, selectedRoute, dismissedKeys ->
        AlertInputs(routes, selectedRoute, dismissedKeys)
    }
        .flatMapLatest { inputs -> alertsStateFor(inputs) }
        .catch { error ->
            Log.e(TAG, "Failed to load alerts.", error)
            emit(
                AlertsUiState(
                    isLoading = false,
                    errorMessage = "Could not load alerts. Check internet and try again."
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AlertsUiState(isLoading = false)
        )

    fun selectRoute(routeId: String) {
        selectedRouteId.value = routeId
    }

    fun dismissAlert(alert: BusAlert) {
        val dismissId = alert.dismissId()
        prefsRepository.dismissAlert(dismissId)
        dismissedAlertKeys.update { keys -> keys + dismissId }
    }

    private fun alertsStateFor(inputs: AlertInputs): Flow<AlertsUiState> {
        val watchedRoutes = if (inputs.selectedRouteId == ALL_ROUTES_ID) {
            inputs.routes
        } else {
            inputs.routes.filter { it.id == inputs.selectedRouteId }
        }

        if (watchedRoutes.isEmpty()) {
            return flowOf(
                AlertsUiState(
                    routes = inputs.routes,
                    alerts = emptyList(),
                    selectedRouteId = inputs.selectedRouteId,
                    isLoading = false
                )
            )
        }

        return combine(
            watchedRoutes.map { route ->
                repository.getAlerts(route.id)
                    .catch { error ->
                        Log.e(TAG, "Alert listener failed for ${route.id}", error)
                        emit(emptyList())
                    }
            }
        ) { routeAlerts ->
            routeAlerts
                .flatMap { it }
                .filterNot { it.dismissId() in inputs.dismissedAlertKeys }
                .sortedByDescending { it.timestamp }
        }.map { alerts ->
            AlertsUiState(
                routes = inputs.routes,
                alerts = alerts,
                selectedRouteId = inputs.selectedRouteId,
                isLoading = false
            )
        }
    }

    private data class AlertInputs(
        val routes: List<Route>,
        val selectedRouteId: String,
        val dismissedAlertKeys: Set<String>
    )

    companion object {
        const val ALL_ROUTES_ID = "ALL"
        private const val TAG = "AlertsViewModel"
    }
}

fun BusAlert.localKey(): String =
    dismissId()
