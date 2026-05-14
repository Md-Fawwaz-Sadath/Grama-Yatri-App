package com.gramayatri.app.ui.tracking

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramayatri.app.data.model.EtaStatus
import com.gramayatri.app.data.model.Ping
import com.gramayatri.app.data.model.Route
import com.gramayatri.app.data.model.StopEta
import com.gramayatri.app.data.repository.FirebaseRepository
import com.gramayatri.app.data.repository.UserPrefsRepository
import com.gramayatri.app.util.EtaCalculator
import com.gramayatri.app.util.ScheduleUtils
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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@HiltViewModel
class LiveTrackingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FirebaseRepository,
    private val prefsRepository: UserPrefsRepository
) : ViewModel() {

    private val routeId: String = savedStateHandle["routeId"] ?: ""
    private val selectedDayKey: String =
        (savedStateHandle["selectedDayKey"] ?: "").ifBlank { ScheduleUtils.getCurrentDayKey() }
    private val isLiveServiceDay: Boolean = selectedDayKey == ScheduleUtils.getCurrentDayKey()

    private val _uiState = MutableStateFlow(
        LiveTrackingUiState(
            routeId = routeId,
            selectedDayKey = selectedDayKey,
            isLiveServiceDay = isLiveServiceDay,
            isLoading = true
        )
    )
    val uiState: StateFlow<LiveTrackingUiState> = _uiState.asStateFlow()
    private var trackingJob: Job? = null

    init {
        loadTracking()
    }

    private fun loadTracking() {
        if (routeId.isBlank()) {
            _uiState.value = LiveTrackingUiState(
                routeId = routeId,
                selectedDayKey = selectedDayKey,
                isLiveServiceDay = isLiveServiceDay,
                routeName = "Route unavailable",
                confidenceText = "Route details unavailable",
                isLoading = false,
                errorMessage = "Could not load this route. Check internet and try again."
            )
            return
        }

        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            launchLoadingWatchdog()
            combine(
                repository.getRoutes(),
                repository.getLatestPing(routeId)
            ) { routes, latestPing ->
                val route = routes.firstOrNull { it.id == routeId }
                buildUiState(route, latestPing.takeIf { isLiveServiceDay })
            }
                .onStart {
                    Log.d(TAG, "Loading tracking for routeId=$routeId")
                    _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                }
                .catch { error ->
                    Log.e(TAG, "Failed to load tracking for routeId=$routeId", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Could not load this route. Check internet and try again."
                    )
                }
                .collect { state ->
                    Log.d(
                        TAG,
                        "Tracking loaded for routeId=$routeId, stops=${state.stopEtas.size}"
                    )
                    _uiState.value = state
                }
        }
    }

    private fun launchLoadingWatchdog() {
        viewModelScope.launch {
            delay(LOADING_TIMEOUT_MS)
            val state = _uiState.value
            if (state.isLoading) {
                Log.d(TAG, "Tracking load timed out waiting for Firebase emission.")
                _uiState.value = state.copy(
                    isLoading = false,
                    errorMessage = if (state.stopEtas.isEmpty()) {
                        "Could not load this route. Check internet and try again."
                    } else {
                        state.errorMessage
                    }
                )
            }
        }
    }

    private fun buildUiState(route: Route?, latestPing: Ping?): LiveTrackingUiState {
        if (route == null) {
            return LiveTrackingUiState(
                routeId = routeId,
                selectedDayKey = selectedDayKey,
                isLiveServiceDay = isLiveServiceDay,
                routeName = "Route not found",
                stopEtas = emptyList(),
                confidenceText = "Route details unavailable",
                isLoading = false,
                errorMessage = "Could not load this route. Check internet and try again."
            )
        }

        val stops = route.stops.values.sortedBy { it.order }
        val stopEtas = if (latestPing == null) {
            EtaCalculator.unknownEtas(stops)
        } else {
            EtaCalculator.calculateEtas(stops, latestPing)
        }
        val savedRouteId = prefsRepository.getSavedRouteId()
        val savedStopId = prefsRepository.getSavedStopId()
        val isTripLikelyComplete = latestPing != null && stopEtas.lastOrNull()?.let { finalStop ->
            latestPing.stopOrder >= finalStop.stop.order ||
                (finalStop.status in listOf(EtaStatus.UPCOMING, EtaStatus.STALE) &&
                    (finalStop.etaMinutes ?: Int.MAX_VALUE) <= 0)
        } == true

        return LiveTrackingUiState(
            routeId = route.id,
            selectedDayKey = selectedDayKey,
            isLiveServiceDay = isLiveServiceDay,
            routeName = route.name,
            route = route,
            stopEtas = stopEtas,
            savedRouteId = savedRouteId,
            savedStopId = savedStopId,
            savedStopEtaText = stopEtas.savedStopEtaText(route.id, savedRouteId, savedStopId),
            confidenceText = if (isLiveServiceDay) {
                latestPing.toConfidenceText(isTripLikelyComplete)
            } else {
                "Schedule preview - live updates are available only for today's service"
            },
            isLoading = false,
            errorMessage = null
        )
    }

    private fun Ping?.toConfidenceText(isTripLikelyComplete: Boolean): String {
        if (this == null) {
            return "No recent report - ETA estimated"
        }
        val reporter = reporterName.ifBlank { "a passenger" }
        val minutesAgo = TimeUtils.minutesAgo(timestamp)
        val confidence = when {
            minutesAgo < 5 -> "High confidence"
            minutesAgo <= 15 -> "Medium confidence"
            minutesAgo <= 30 -> "Low confidence"
            else -> "Stale report"
        }
        val prefix = if (isTripLikelyComplete) {
            "Trip likely completed"
        } else {
            confidence
        }
        return "$prefix - based on ping ${TimeUtils.formatMinutesAgo(minutesAgo)} by $reporter"
    }

    private fun List<StopEta>.savedStopEtaText(
        routeId: String,
        savedRouteId: String?,
        savedStopId: String?
    ): String? {
        if (routeId != savedRouteId || savedStopId.isNullOrBlank()) return null
        val savedStopEta = firstOrNull { it.stop.id == savedStopId } ?: return null
        val etaText = when (savedStopEta.status) {
            EtaStatus.BUS_HERE -> "Bus is here now"
            EtaStatus.UPCOMING -> {
                val eta = savedStopEta.etaMinutes
                if (eta == null) {
                    "ETA unavailable"
                } else if (eta <= 0) {
                    "Bus may have reached"
                } else {
                    "~$eta min"
                }
            }
            EtaStatus.PASSED -> "Bus has passed"
            EtaStatus.STALE -> {
                val eta = savedStopEta.etaMinutes
                if (eta != null && eta <= 0) "Bus may have reached" else "ETA unconfirmed"
            }
            EtaStatus.UNKNOWN -> "No recent ping"
        }
        return "Your saved stop: ${savedStopEta.stop.name} - $etaText"
    }

    private companion object {
        const val TAG = "LiveTrackingViewModel"
        const val LOADING_TIMEOUT_MS = 8_000L
    }
}
