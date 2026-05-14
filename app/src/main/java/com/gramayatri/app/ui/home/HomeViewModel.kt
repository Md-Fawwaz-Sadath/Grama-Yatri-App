package com.gramayatri.app.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramayatri.app.data.model.BusAlert
import com.gramayatri.app.data.model.Ping
import com.gramayatri.app.data.model.Route
import com.gramayatri.app.data.model.dismissId
import com.gramayatri.app.data.remote.FirebaseSeeder
import com.gramayatri.app.data.repository.FirebaseRepository
import com.gramayatri.app.data.repository.UserPrefsRepository
import com.gramayatri.app.util.RouteHealthCalculator
import com.gramayatri.app.util.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FirebaseRepository,
    private val prefs: UserPrefsRepository,
    private val seeder: FirebaseSeeder
) : ViewModel() {

    private val _routes = MutableStateFlow<List<Route>>(emptyList())
    val routes: StateFlow<List<Route>> = _routes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _healthState = MutableStateFlow(HomeHealthUiState())
    val healthState: StateFlow<HomeHealthUiState> = _healthState.asStateFlow()

    private var routesJob: Job? = null
    private var healthJob: Job? = null
    private var latestHealthInputs: List<RouteHealthInput> = emptyList()

    init {
        loadRoutes()
    }

    fun loadRoutes() {
        routesJob?.cancel()
        routesJob = viewModelScope.launch {
            launchLoadingWatchdog()
            repository.getRoutes()
                .onStart {
                    Log.d(TAG, "Starting route load.")
                    _isLoading.value = true
                    _errorMessage.value = null
                }
                .catch { error ->
                    Log.e(TAG, "Route load failed.", error)
                    _errorMessage.value = "Could not load routes. Check internet and try again."
                    _isLoading.value = false
                }
                .collect { routes ->
                    Log.d(TAG, "Route load emitted ${routes.size} routes.")
                    _routes.value = routes
                    observeRouteHealth(routes)
                    _isLoading.value = false
                    _errorMessage.value = null
                }
        }
    }

    private fun launchLoadingWatchdog() {
        viewModelScope.launch {
            delay(LOADING_TIMEOUT_MS)
            if (_isLoading.value) {
                Log.d(TAG, "Route load timed out waiting for Firebase emission.")
                _isLoading.value = false
                if (_routes.value.isEmpty()) {
                    _errorMessage.value = "Could not load routes. Check internet and try again."
                }
            }
        }
    }

    fun seedRoutesIfEmpty() {
        viewModelScope.launch {
            seeder.seedRoutesIfEmpty()
                .onSuccess { didSeed ->
                    Log.d(TAG, "Debug seed checked. didSeed=$didSeed")
                }
                .onFailure { error ->
                    Log.e(TAG, "Debug seed failed.", error)
                    _errorMessage.value = error.message ?: "Unable to seed routes."
                }
        }
    }

    fun getSavedRouteId(): String? =
        prefs.getSavedRouteId()

    fun isAccessibilityTipDismissed(): Boolean =
        prefs.isAccessibilityTipDismissed()

    fun dismissAccessibilityTip() {
        prefs.setAccessibilityTipDismissed()
    }

    fun refreshDismissedAlertHealth() {
        if (latestHealthInputs.isNotEmpty()) {
            _healthState.value = buildHealthState(latestHealthInputs)
        }
    }

    private fun observeRouteHealth(routes: List<Route>) {
        healthJob?.cancel()
        if (routes.isEmpty()) {
            _healthState.value = HomeHealthUiState(isVisible = false)
            return
        }

        healthJob = viewModelScope.launch {
            val routeHealthFlows = routes.map { route ->
                combine(
                    repository.getTodayPings(route.id)
                        .catch { error ->
                            Log.e(TAG, "Ping health listener failed for ${route.id}", error)
                            emit(emptyList())
                        },
                    repository.getAlerts(route.id)
                        .catch { error ->
                            Log.e(TAG, "Alert health listener failed for ${route.id}", error)
                            emit(emptyList())
                        }
                ) { pings, alerts ->
                    RouteHealthInput(route, pings, alerts)
                }
            }

            val healthInputFlow = if (routeHealthFlows.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(routeHealthFlows) { it.toList() }
            }

            healthInputFlow
                .map { inputs ->
                    latestHealthInputs = inputs
                    buildHealthState(inputs)
                }
                .collect { state -> _healthState.value = state }
        }
    }

    private fun buildHealthState(inputs: List<RouteHealthInput>): HomeHealthUiState {
        val dismissedAlertIds = prefs.getDismissedAlertIds()
        val latestPings = inputs.associate { input ->
            input.route.id to RouteHealthCalculator.latestPing(input.pings)
        }
        val activeAlertCounts = inputs.associate { input ->
            val visibleAlerts = input.alerts.filterNot { it.dismissId() in dismissedAlertIds }
            input.route.id to RouteHealthCalculator.activeAlertsToday(visibleAlerts).size
        }
        val routeHealthMap = inputs.associate { input ->
            val routeId = input.route.id
            routeId to RouteHealthCalculator.calculate(
                latestPingTimestamp = latestPings[routeId]?.timestamp,
                activeAlertCount = activeAlertCounts[routeId] ?: 0
            )
        }
        val lastUpdatedRoute = RouteHealthCalculator.findLastUpdatedRoute(
            routes = inputs.map { it.route },
            latestPings = latestPings
        )
        val lastUpdatedPing = latestPings[lastUpdatedRoute?.id]

        return HomeHealthUiState(
            isVisible = inputs.isNotEmpty(),
            routeHealthMap = routeHealthMap,
            latestPings = latestPings,
            activeAlertCounts = activeAlertCounts,
            reportsTodayCount = RouteHealthCalculator.countReportsToday(
                inputs.associate { it.route.id to it.pings }
            ),
            activeAlertsCount = activeAlertCounts.values.sum(),
            lastUpdatedRouteName = lastUpdatedRoute?.name,
            lastUpdatedMinutesAgo = lastUpdatedPing?.timestamp?.let { TimeUtils.minutesAgo(it) },
            summary = RouteHealthCalculator.aggregateSummary(routeHealthMap)
        )
    }

    private data class RouteHealthInput(
        val route: Route,
        val pings: List<Ping>,
        val alerts: List<BusAlert>
    )

    private companion object {
        const val TAG = "HomeViewModel"
        const val LOADING_TIMEOUT_MS = 8_000L
    }
}
