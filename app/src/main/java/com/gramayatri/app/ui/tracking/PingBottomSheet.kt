package com.gramayatri.app.ui.tracking

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.gramayatri.app.R
import com.gramayatri.app.data.model.Ping
import com.gramayatri.app.data.model.Stop
import com.gramayatri.app.data.repository.FirebaseRepository
import com.gramayatri.app.data.repository.UserPrefsRepository
import com.gramayatri.app.databinding.BottomSheetPingBinding
import com.gramayatri.app.util.Constants
import com.gramayatri.app.util.ScheduleUtils
import com.gramayatri.app.util.TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PingBottomSheet : BottomSheetDialogFragment(R.layout.bottom_sheet_ping) {

    @Inject lateinit var repository: FirebaseRepository
    @Inject lateinit var prefsRepository: UserPrefsRepository

    private var _binding: BottomSheetPingBinding? = null
    private val binding get() = _binding!!

    private val routeId: String by lazy {
        requireArguments().getString(ARG_ROUTE_ID).orEmpty()
    }
    private var stops: List<Stop> = emptyList()
    private var latestActivePing: Ping? = null
    private var isSubmitting = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = BottomSheetPingBinding.bind(view)

        binding.reportingAsText.text = getString(
            R.string.reporting_as,
            prefsRepository.getDisplayName()
        )
        binding.cancelButton.setOnClickListener { dismiss() }
        binding.onBusButton.setOnClickListener {
            submitPing(Constants.PING_TYPE_ON_BUS)
        }
        binding.passedMeButton.setOnClickListener {
            submitPing(Constants.PING_TYPE_PASSED)
        }

        loadStops()
        observeLatestPing()
    }

    private fun loadStops() {
        setSubmitting(true)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.getRoutes()
                    .catch {
                        setSubmitting(false)
                        showError(getString(R.string.ping_error))
                    }
                    .collect { routes ->
                        stops = routes
                            .firstOrNull { it.id == routeId }
                            ?.stops
                            ?.values
                            ?.sortedBy { it.order }
                            .orEmpty()
                        bindStops()
                        setSubmitting(false)
                    }
            }
        }
    }

    private fun bindStops() {
        val stopNames = if (stops.isEmpty()) {
            listOf(getString(R.string.no_stops_for_ping))
        } else {
            stops.map { it.name }
        }
        binding.stopSpinner.adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            stopNames
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as? TextView)?.apply {
                    setTextColor(requireContext().getColor(R.color.text_primary))
                    textSize = 16f
                    setPadding(12, 0, 12, 0)
                }
                return view
            }
        }.apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.stopSpinner.isEnabled = stops.isNotEmpty()
        binding.onBusButton.isEnabled = stops.isNotEmpty()
        binding.passedMeButton.isEnabled = stops.isNotEmpty()

        val savedStopId = prefsRepository.getSavedStopId()
        val savedRouteId = prefsRepository.getSavedRouteId()
        val savedIndex = stops.indexOfFirst { stop ->
            savedRouteId == routeId && stop.id == savedStopId
        }
        binding.stopSpinner.setSelection(savedIndex.coerceAtLeast(0))
    }

    private fun observeLatestPing() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.getLatestPing(routeId)
                    .catch { latestActivePing = null }
                    .collect { ping -> latestActivePing = ping }
            }
        }
    }

    private fun submitPing(type: String) {
        if (isSubmitting) return

        val stop = stops.getOrNull(binding.stopSpinner.selectedItemPosition) ?: return
        val now = System.currentTimeMillis()
        val lastPingAt = prefsRepository.getLastPingTimestamp(routeId)
        val minutesSinceLastPing = (now - lastPingAt) / 60_000
        val latestPing = latestActivePing

        if (latestPing != null && stop.order < latestPing.stopOrder) {
            showError(getString(R.string.ping_earlier_stop_blocked))
            return
        }

        if (latestPing != null &&
            stop.order == latestPing.stopOrder &&
            TimeUtils.minutesAgo(latestPing.timestamp) < Constants.PING_RATE_LIMIT_MIN
        ) {
            showError(getString(R.string.ping_same_stop_rate_limited))
            return
        }

        if (latestPing == null &&
            lastPingAt > 0 &&
            minutesSinceLastPing < Constants.PING_RATE_LIMIT_MIN
        ) {
            showError(getString(R.string.ping_rate_limited))
            return
        }

        setSubmitting(true)
        viewLifecycleOwner.lifecycleScope.launch {
            val uid = repository.getCurrentUid()
                ?: repository.signInAnonymously().getOrElse { error ->
                    setSubmitting(false)
                    showError(error.message ?: getString(R.string.ping_error))
                    return@launch
                }

            val ping = Ping(
                stopId = stop.id,
                stopName = stop.name,
                stopOrder = stop.order,
                type = type,
                reporterName = prefsRepository.getDisplayName(),
                reporterUid = uid,
                timestamp = now,
                serviceDateKey = TimeUtils.getTodayServiceDateKey(),
                serviceDayKey = ScheduleUtils.getCurrentDayKey(),
                isActive = true
            )

            repository.submitPing(routeId, ping)
                .onSuccess {
                    prefsRepository.setLastPingTimestamp(routeId, now)
                    prefsRepository.saveStop(routeId, stop.id, stop.name)
                    setFragmentResult(
                        REQUEST_KEY,
                        bundleOf(RESULT_MESSAGE to getString(R.string.ping_sent_success))
                    )
                    dismiss()
                }
                .onFailure { error ->
                    setSubmitting(false)
                    showError(error.message ?: getString(R.string.ping_error))
                }
        }
    }

    private fun setSubmitting(submitting: Boolean) {
        isSubmitting = submitting
        binding.pingProgress.visibility = if (submitting) View.VISIBLE else View.GONE
        binding.onBusButton.isEnabled = !submitting && stops.isNotEmpty()
        binding.passedMeButton.isEnabled = !submitting && stops.isNotEmpty()
        binding.stopSpinner.isEnabled = !submitting && stops.isNotEmpty()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val REQUEST_KEY = "ping_result"
        const val RESULT_MESSAGE = "message"
        private const val ARG_ROUTE_ID = "routeId"

        fun newInstance(routeId: String): PingBottomSheet =
            PingBottomSheet().apply {
                arguments = bundleOf(ARG_ROUTE_ID to routeId)
            }
    }
}
