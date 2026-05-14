package com.gramayatri.app.ui.tracking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.gramayatri.app.R
import com.gramayatri.app.data.model.EtaStatus
import com.gramayatri.app.data.model.Route
import com.gramayatri.app.data.model.StopEta
import com.gramayatri.app.data.repository.UserPrefsRepository
import com.gramayatri.app.databinding.FragmentLiveTrackingBinding
import com.gramayatri.app.util.ScheduleUtils
import com.gramayatri.app.util.SpeechTextUtils
import com.gramayatri.app.util.TimeUtils
import com.gramayatri.app.util.TtsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LiveTrackingFragment : Fragment(R.layout.fragment_live_tracking) {

    @Inject lateinit var prefsRepository: UserPrefsRepository

    private var _binding: FragmentLiveTrackingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LiveTrackingViewModel by viewModels()
    private val stopTimelineAdapter = StopTimelineAdapter()
    private lateinit var ttsManager: TtsManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLiveTrackingBinding.bind(view)
        ttsManager = TtsManager(requireContext()) { prefsRepository.getLanguageCode() }

        observePingResult()
        setupToolbar()
        setupStopTimeline()
        setupPingButton()
        observeTrackingState()
    }

    private fun setupToolbar() {
        binding.liveTrackingToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.liveTrackingToolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_speak_tracking) {
                speak(trackingSpeakText(viewModel.uiState.value))
                true
            } else {
                false
            }
        }
    }

    private fun setupStopTimeline() {
        binding.stopsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = stopTimelineAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupPingButton() {
        binding.pingBusButton.setOnClickListener {
            val routeId = viewModel.uiState.value.routeId
            if (!viewModel.uiState.value.isLiveServiceDay) {
                Snackbar.make(
                    binding.root,
                    R.string.live_updates_today_only,
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            if (routeId.isBlank()) {
                Snackbar.make(binding.root, R.string.ping_error, Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            PingBottomSheet
                .newInstance(routeId)
                .show(parentFragmentManager, PingBottomSheet::class.java.simpleName)
        }
    }

    private fun observePingResult() {
        setFragmentResultListener(PingBottomSheet.REQUEST_KEY) { _, bundle ->
            val message = bundle.getString(PingBottomSheet.RESULT_MESSAGE)
                ?: getString(R.string.ping_sent_success)
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun observeTrackingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.liveTrackingToolbar.title = state.routeName
                    bindRouteMeta(state)
                    binding.trackingProgress.visibility =
                        if (state.isLoading) View.VISIBLE else View.GONE
                    binding.confidenceText.text =
                        state.errorMessage ?: confidenceText(state)
                    binding.pingBusButton.alpha = if (state.isLiveServiceDay) 1f else 0.65f
                    val savedStopText = savedStopEtaText(state)
                    binding.savedStopEtaText.text = savedStopText.orEmpty()
                    binding.savedStopEtaText.visibility =
                        if (savedStopText.isNullOrBlank()) View.GONE else View.VISIBLE
                    val timelineItems = state.stopEtas.map { stopEta ->
                        StopTimelineItem(
                            stopEta = stopEta,
                            isSavedStop = state.routeId == state.savedRouteId &&
                                stopEta.stop.id == state.savedStopId
                        )
                    }
                    stopTimelineAdapter.submitList(timelineItems)
                    binding.trackingEmptyText.text =
                        state.errorMessage ?: getString(R.string.tracking_no_stops)
                    binding.trackingEmptyText.visibility =
                        if (!state.isLoading && state.stopEtas.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun confidenceText(state: LiveTrackingUiState): String {
        if (!state.isLiveServiceDay) {
            return getString(
                R.string.schedule_preview_message_format,
                ScheduleUtils.dayFullDisplayLabel(requireContext(), state.selectedDayKey)
            )
        }
        val pingStop = latestPingStop(state) ?: return getString(R.string.confidence_no_recent)
        val timestamp = pingStop.pingTimestamp ?: return getString(R.string.confidence_no_recent)
        val minutesAgo = TimeUtils.minutesAgo(timestamp)
        val reporter = pingStop.reporterName.orEmpty().ifBlank {
            getString(R.string.anonymous_reporter)
        }
        val confidence = if (isTripLikelyCompleted(state)) {
            getString(R.string.eta_trip_likely_completed)
        } else {
            confidenceLabel(minutesAgo)
        }
        return getString(
            R.string.confidence_based_on_format,
            confidence,
            TimeUtils.formatMinutesAgo(requireContext(), minutesAgo),
            reporter
        )
    }

    private fun trackingSpeakText(state: LiveTrackingUiState): String {
        val routeName = SpeechTextUtils.routeNameForSpeech(requireContext(), state.routeName)
        val routePrefix = trackingRoutePrefix(state.route, routeName)
        val currentStop = latestPingStop(state)
        val savedStopText = savedStopSpeechText(state).orEmpty()
        if (currentStop == null) {
            return routePrefix + " " +
                getString(R.string.tts_route_no_report_format, savedStopText)
        }

        val timestamp = currentStop.pingTimestamp
        val confidence = if (timestamp == null) {
            getString(R.string.confidence_stale)
        } else {
            confidenceLabel(TimeUtils.minutesAgo(timestamp)).lowercase()
        }
        return routePrefix + " " + getString(
            R.string.tts_route_status_format,
            currentStop.stop.name,
            confidence,
            savedStopText
        )
    }

    private fun bindRouteMeta(state: LiveTrackingUiState) {
        val route = state.route
        if (route == null) {
            binding.routeMetaText.visibility = View.GONE
            return
        }
        val meta = listOfNotNull(
            ScheduleUtils.routeMetaLine(requireContext(), route),
            ScheduleUtils.selectedDayScheduleLine(requireContext(), route, state.selectedDayKey)
        ).joinToString("\n")
        binding.routeMetaText.text = meta
        binding.routeMetaText.visibility = if (meta.isBlank()) View.GONE else View.VISIBLE
    }

    private fun trackingRoutePrefix(route: Route?, spokenRouteName: String): String {
        if (route == null) return ""
        val operator = route.busOperator.takeIf { it.isNotBlank() }?.let {
            ScheduleUtils.displayOperator(requireContext(), it)
        }
        val busIdentity = when {
            operator != null && route.busNumber.isNotBlank() ->
                getString(R.string.tts_bus_identity_format, operator, route.busNumber)
            operator != null -> getString(R.string.tts_bus_operator_only_format, operator)
            route.busNumber.isNotBlank() -> getString(R.string.bus_number_line_format, route.busNumber)
            else -> getString(R.string.bus_label)
        }
        val schedule = ScheduleUtils.scheduleSpeechLine(requireContext(), route).orEmpty()
        return getString(R.string.tts_tracking_prefix_format, busIdentity, spokenRouteName, schedule)
    }

    private fun savedStopEtaText(state: LiveTrackingUiState): String? {
        val savedStopEta = savedStopEta(state) ?: return null
        return getString(
            R.string.saved_stop_eta_format,
            savedStopEta.stop.name,
            etaText(savedStopEta, state.stopEtas.lastOrNull()?.stop?.id == savedStopEta.stop.id)
        )
    }

    private fun savedStopSpeechText(state: LiveTrackingUiState): String? {
        val savedStopEta = savedStopEta(state) ?: return null
        return getString(
            R.string.tts_saved_stop_eta_format,
            savedStopEta.stop.name,
            etaText(savedStopEta, state.stopEtas.lastOrNull()?.stop?.id == savedStopEta.stop.id)
        )
    }

    private fun savedStopEta(state: LiveTrackingUiState): StopEta? {
        if (state.routeId != state.savedRouteId || state.savedStopId.isNullOrBlank()) return null
        return state.stopEtas.firstOrNull { it.stop.id == state.savedStopId }
    }

    private fun latestPingStop(state: LiveTrackingUiState): StopEta? =
        state.stopEtas.firstOrNull { it.status == EtaStatus.BUS_HERE }
            ?: state.stopEtas.lastOrNull { it.pingTimestamp != null }

    private fun isTripLikelyCompleted(state: LiveTrackingUiState): Boolean {
        val finalStop = state.stopEtas.lastOrNull() ?: return false
        return finalStop.status == EtaStatus.BUS_HERE ||
            (finalStop.etaMinutes != null && finalStop.etaMinutes <= 0 &&
                finalStop.status in listOf(EtaStatus.UPCOMING, EtaStatus.STALE))
    }

    private fun etaText(stopEta: StopEta, isFinalStop: Boolean): String =
        when (stopEta.status) {
            EtaStatus.BUS_HERE -> getString(R.string.eta_bus_here)
            EtaStatus.PASSED -> getString(R.string.eta_bus_may_have_reached)
            EtaStatus.UNKNOWN -> getString(R.string.eta_no_recent_ping)
            EtaStatus.STALE, EtaStatus.UPCOMING -> {
                val eta = stopEta.etaMinutes
                when {
                    eta == null -> getString(R.string.eta_unavailable)
                    eta <= 0 && isFinalStop -> getString(R.string.eta_trip_likely_completed)
                    eta <= 0 -> getString(R.string.eta_bus_may_have_reached)
                    else -> getString(R.string.eta_min_format, eta)
                }
            }
        }

    private fun confidenceLabel(minutesAgo: Long): String =
        when {
            minutesAgo < 5 -> getString(R.string.confidence_high)
            minutesAgo <= 15 -> getString(R.string.confidence_medium)
            minutesAgo <= 30 -> getString(R.string.confidence_low)
            else -> getString(R.string.confidence_stale)
        }

    private fun speak(text: String) {
        when (ttsManager.speak(text)) {
            TtsManager.SpeakResult.SPOKEN -> Unit
            TtsManager.SpeakResult.NOT_READY ->
                Snackbar.make(binding.root, R.string.tts_not_ready, Snackbar.LENGTH_SHORT).show()
            TtsManager.SpeakResult.LANGUAGE_UNAVAILABLE ->
                Snackbar.make(binding.root, R.string.tts_kannada_unavailable, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ttsManager.shutdown()
        binding.stopsRecyclerView.adapter = null
        _binding = null
    }
}
