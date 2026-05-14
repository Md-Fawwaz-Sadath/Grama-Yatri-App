package com.gramayatri.app.ui.alerts

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
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
import com.gramayatri.app.data.model.BusAlert
import com.gramayatri.app.data.model.Route
import com.gramayatri.app.data.repository.UserPrefsRepository
import com.gramayatri.app.databinding.FragmentAlertsBinding
import com.gramayatri.app.util.Constants
import com.gramayatri.app.util.SpeechTextUtils
import com.gramayatri.app.util.TimeUtils
import com.gramayatri.app.util.TtsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlertsFragment : Fragment(R.layout.fragment_alerts) {

    @Inject lateinit var prefsRepository: UserPrefsRepository

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlertsViewModel by viewModels()
    private lateinit var alertAdapter: AlertAdapter
    private lateinit var ttsManager: TtsManager

    private var routeFilterIds: List<String> = emptyList()
    private var updatingFilter = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAlertsBinding.bind(view)
        ttsManager = TtsManager(requireContext()) { prefsRepository.getLanguageCode() }

        observeReportResult()
        setupAlertsList()
        setupReportButton()
        observeAlerts()
    }

    private fun observeReportResult() {
        setFragmentResultListener(ReportAlertFragment.REQUEST_KEY) { _, bundle ->
            val message = bundle.getString(ReportAlertFragment.RESULT_MESSAGE)
                ?: getString(R.string.alert_sent_success)
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupAlertsList() {
        alertAdapter = AlertAdapter(
            onDismissClick = { alert -> viewModel.dismissAlert(alert) },
            onSpeakClick = { alert -> speak(alertSpeakText(alert)) }
        )
        binding.alertsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = alertAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupReportButton() {
        binding.reportAlertButton.setOnClickListener {
            findNavController().navigate(R.id.reportAlertFragment)
        }
    }

    private fun observeAlerts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    bindRouteFilter(state.routes, state.selectedRouteId)
                    alertAdapter.submitList(state.alerts)
                    binding.alertsProgress.visibility =
                        if (state.isLoading) View.VISIBLE else View.GONE
                    binding.alertsEmptyText.text =
                        state.errorMessage ?: getString(R.string.alerts_empty_state)
                    binding.alertsEmptyText.visibility =
                        if (!state.isLoading && state.alerts.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun bindRouteFilter(routes: List<Route>, selectedRouteId: String) {
        val newFilterIds = listOf(AlertsViewModel.ALL_ROUTES_ID) + routes.map { it.id }
        if (newFilterIds == routeFilterIds) {
            val selectedIndex = routeFilterIds.indexOf(selectedRouteId).coerceAtLeast(0)
            if (binding.routeFilterSpinner.selectedItemPosition != selectedIndex) {
                updatingFilter = true
                binding.routeFilterSpinner.setSelection(selectedIndex)
                updatingFilter = false
            }
            return
        }

        routeFilterIds = newFilterIds
        val routeNames = listOf(getString(R.string.all_routes)) + routes.map { it.name }
        binding.routeFilterSpinner.adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            routeNames
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as? TextView)?.apply {
                    setTextColor(requireContext().getColor(R.color.text_primary))
                    textSize = 16f
                    setPadding(8, 0, 8, 0)
                }
                return view
            }
        }.apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.routeFilterSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (!updatingFilter) {
                        viewModel.selectRoute(routeFilterIds.getOrElse(position) {
                            AlertsViewModel.ALL_ROUTES_ID
                        })
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }

        val selectedIndex = routeFilterIds.indexOf(selectedRouteId).coerceAtLeast(0)
        updatingFilter = true
        binding.routeFilterSpinner.setSelection(selectedIndex)
        updatingFilter = false
    }

    private fun alertSpeakText(alert: BusAlert): String {
        val typeText = when (alert.type) {
            Constants.ALERT_CANCELLED -> getString(R.string.alert_display_cancelled)
            Constants.ALERT_DELAYED -> getString(R.string.alert_display_delayed)
            Constants.ALERT_INFO -> getString(R.string.alert_display_info)
            else -> alert.type.ifBlank { getString(R.string.alert_display_default) }
        }
        val routeName = SpeechTextUtils.routeNameForSpeech(requireContext(), alert.routeName)
        val details = listOf(alert.reason, alert.note)
            .filter { it.isNotBlank() }
            .joinToString(". ")
            .ifBlank { typeText }
        val reporter = alert.reporterName.ifBlank { getString(R.string.anonymous_reporter) }
        val age = TimeUtils.formatMinutesAgo(requireContext(), TimeUtils.minutesAgo(alert.timestamp))
        return getString(R.string.tts_alert_format, typeText, routeName, details, reporter, age)
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
        binding.alertsRecyclerView.adapter = null
        _binding = null
    }
}
