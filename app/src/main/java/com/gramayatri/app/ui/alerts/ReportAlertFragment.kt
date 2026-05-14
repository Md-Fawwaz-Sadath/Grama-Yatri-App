package com.gramayatri.app.ui.alerts

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.gramayatri.app.R
import com.gramayatri.app.data.model.BusAlert
import com.gramayatri.app.data.model.Route
import com.gramayatri.app.data.repository.FirebaseRepository
import com.gramayatri.app.data.repository.UserPrefsRepository
import com.gramayatri.app.databinding.FragmentReportAlertBinding
import com.gramayatri.app.util.Constants
import com.gramayatri.app.util.ScheduleUtils
import com.gramayatri.app.util.TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReportAlertFragment : Fragment(R.layout.fragment_report_alert) {

    @Inject lateinit var repository: FirebaseRepository
    @Inject lateinit var prefsRepository: UserPrefsRepository

    private var _binding: FragmentReportAlertBinding? = null
    private val binding get() = _binding!!

    private var routes: List<Route> = emptyList()
    private var isSubmitting = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReportAlertBinding.bind(view)

        setupToolbar()
        setupReasonSpinner()
        setupSubmitButton()
        loadRoutes()
    }

    private fun setupToolbar() {
        binding.reportAlertToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupReasonSpinner() {
        binding.reasonSpinner.adapter = readableSpinnerAdapter(
            listOf(
                getString(R.string.reason_road_blocked),
                getString(R.string.reason_bus_breakdown),
                getString(R.string.reason_driver_absent),
                getString(R.string.reason_festival_event),
                getString(R.string.reason_other)
            )
        )
    }

    private fun setupSubmitButton() {
        binding.submitAlertButton.setOnClickListener {
            submitAlert()
        }
    }

    private fun loadRoutes() {
        setSubmitting(true)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.getRoutes()
                    .catch {
                        setSubmitting(false)
                        showError(getString(R.string.alert_submit_error))
                    }
                    .collect { loadedRoutes ->
                        routes = loadedRoutes
                        bindRoutes()
                        setSubmitting(false)
                    }
            }
        }
    }

    private fun bindRoutes() {
        val routeNames = if (routes.isEmpty()) {
            listOf(getString(R.string.no_routes_for_alert))
        } else {
            routes.map { it.name }
        }
        binding.routeSpinner.adapter = readableSpinnerAdapter(routeNames)
        binding.routeSpinner.isEnabled = routes.isNotEmpty()
        binding.submitAlertButton.isEnabled = routes.isNotEmpty() && !isSubmitting
    }

    private fun readableSpinnerAdapter(items: List<String>): ArrayAdapter<String> =
        object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            items
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

    private fun submitAlert() {
        if (isSubmitting) return

        val selectedRoute = routes.getOrNull(binding.routeSpinner.selectedItemPosition)
        if (selectedRoute == null) {
            showError(getString(R.string.no_routes_for_alert))
            return
        }

        setSubmitting(true)
        viewLifecycleOwner.lifecycleScope.launch {
            val uid = repository.getCurrentUid()
                ?: repository.signInAnonymously().getOrElse { error ->
                    setSubmitting(false)
                    showError(error.message ?: getString(R.string.alert_submit_error))
                    return@launch
                }

            val alert = BusAlert(
                type = selectedAlertType(),
                reason = binding.reasonSpinner.selectedItem?.toString().orEmpty(),
                note = binding.noteEditText.text?.toString()?.trim().orEmpty(),
                reporterName = prefsRepository.getDisplayName(),
                reporterUid = uid,
                timestamp = System.currentTimeMillis(),
                routeId = selectedRoute.id,
                routeName = selectedRoute.name,
                serviceDateKey = TimeUtils.getTodayServiceDateKey(),
                serviceDayKey = ScheduleUtils.getCurrentDayKey()
            )

            repository.submitAlert(selectedRoute.id, alert)
                .onSuccess {
                    setFragmentResult(
                        REQUEST_KEY,
                        bundleOf(RESULT_MESSAGE to getString(R.string.alert_sent_success))
                    )
                    findNavController().navigateUp()
                }
                .onFailure { error ->
                    setSubmitting(false)
                    showError(error.message ?: getString(R.string.alert_submit_error))
                }
        }
    }

    private fun selectedAlertType(): String =
        when (binding.alertTypeGroup.checkedRadioButtonId) {
            R.id.cancelled_radio -> Constants.ALERT_CANCELLED
            R.id.delayed_radio -> Constants.ALERT_DELAYED
            R.id.info_radio -> Constants.ALERT_INFO
            else -> Constants.ALERT_INFO
        }

    private fun setSubmitting(submitting: Boolean) {
        isSubmitting = submitting
        binding.reportAlertProgress.visibility = if (submitting) View.VISIBLE else View.GONE
        binding.submitAlertButton.isEnabled = !submitting && routes.isNotEmpty()
        binding.routeSpinner.isEnabled = !submitting && routes.isNotEmpty()
        binding.reasonSpinner.isEnabled = !submitting
        binding.noteEditText.isEnabled = !submitting
        binding.cancelledRadio.isEnabled = !submitting
        binding.delayedRadio.isEnabled = !submitting
        binding.infoRadio.isEnabled = !submitting
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val REQUEST_KEY = "report_alert_result"
        const val RESULT_MESSAGE = "message"
    }
}
