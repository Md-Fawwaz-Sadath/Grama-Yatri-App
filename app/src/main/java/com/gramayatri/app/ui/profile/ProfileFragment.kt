package com.gramayatri.app.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.gramayatri.app.R
import com.gramayatri.app.data.model.Route
import com.gramayatri.app.data.model.Stop
import com.gramayatri.app.data.repository.FirebaseRepository
import com.gramayatri.app.data.repository.UserPrefsRepository
import com.gramayatri.app.databinding.FragmentProfileBinding
import com.gramayatri.app.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    @Inject lateinit var repository: FirebaseRepository
    @Inject lateinit var prefsRepository: UserPrefsRepository

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var routes: List<Route> = emptyList()
    private var selectedStops: List<Stop> = emptyList()
    private var isLoading = false
    private var updatingSelection = false
    private var updatingLanguageSelection = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        binding.profileDisplayNameEditText.setText(
            prefsRepository.getDisplayName().takeUnless { it == "Anonymous" }.orEmpty()
        )
        setupLanguageSelector()
        bindSavedStop()
        setupActions()
        loadRoutes()
    }

    private fun setupActions() {
        binding.saveNameButton.setOnClickListener {
            prefsRepository.setDisplayName(binding.profileDisplayNameEditText.text?.toString().orEmpty())
            Snackbar.make(binding.root, R.string.display_name_saved, Snackbar.LENGTH_SHORT).show()
        }

        binding.saveStopButton.setOnClickListener {
            savePreferredStop()
        }

        binding.profileRouteSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (!updatingSelection) {
                        bindStopsForRoute(position)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
    }

    private fun setupLanguageSelector() {
        val languageCodes = listOf(Constants.LANGUAGE_ENGLISH, Constants.LANGUAGE_KANNADA)
        val languageNames = listOf(
            getString(R.string.language_english),
            getString(R.string.language_kannada)
        )
        binding.languageSpinner.adapter = readableSpinnerAdapter(languageNames)
        val selectedIndex = languageCodes.indexOf(prefsRepository.getLanguageCode()).coerceAtLeast(0)
        updatingLanguageSelection = true
        binding.languageSpinner.setSelection(selectedIndex)
        updatingLanguageSelection = false

        binding.languageSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (updatingLanguageSelection) return
                    val selectedLanguage = languageCodes.getOrElse(position) {
                        Constants.LANGUAGE_ENGLISH
                    }
                    if (selectedLanguage != prefsRepository.getLanguageCode()) {
                        prefsRepository.setLanguageCode(selectedLanguage)
                        Snackbar.make(
                            binding.root,
                            R.string.language_saved_restart,
                            Snackbar.LENGTH_SHORT
                        ).show()
                        binding.root.post { requireActivity().recreate() }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
    }

    private fun loadRoutes() {
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            launchLoadingWatchdog()
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.getRoutes()
                    .catch { error ->
                        Log.e(TAG, "Profile route load failed.", error)
                        setLoading(false)
                        showRouteLoadError()
                    }
                    .collect { loadedRoutes ->
                        Log.d(TAG, "Profile loaded ${loadedRoutes.size} routes.")
                        routes = loadedRoutes
                        bindRoutes()
                        setLoading(false)
                    }
            }
        }
    }

    private fun bindRoutes() {
        val routeNames = if (routes.isEmpty()) {
            listOf(getString(R.string.no_routes_try_again))
        } else {
            routes.map { it.name }
        }
        binding.profileRouteSpinner.adapter = readableSpinnerAdapter(routeNames)
        binding.profileRouteSpinner.isEnabled = routes.isNotEmpty()

        val savedRouteId = prefsRepository.getSavedRouteId()
        val routeIndex = routes.indexOfFirst { it.id == savedRouteId }.coerceAtLeast(0)
        updatingSelection = true
        binding.profileRouteSpinner.setSelection(routeIndex)
        updatingSelection = false
        bindStopsForRoute(routeIndex)
    }

    private fun bindStopsForRoute(routeIndex: Int) {
        val route = routes.getOrNull(routeIndex)
        selectedStops = route?.stops?.values?.sortedBy { it.order }.orEmpty()
        val stopNames = if (selectedStops.isEmpty()) {
            listOf(getString(R.string.no_stops_for_ping))
        } else {
            selectedStops.map { it.name }
        }
        binding.profileStopSpinner.adapter = readableSpinnerAdapter(stopNames)
        binding.profileStopSpinner.isEnabled = selectedStops.isNotEmpty()
        binding.saveStopButton.isEnabled = !isLoading && routes.isNotEmpty() && selectedStops.isNotEmpty()

        val savedStopId = prefsRepository.getSavedStopId()
        val stopIndex = selectedStops.indexOfFirst { it.id == savedStopId }.coerceAtLeast(0)
        binding.profileStopSpinner.setSelection(stopIndex)
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

    private fun savePreferredStop() {
        val route = routes.getOrNull(binding.profileRouteSpinner.selectedItemPosition) ?: return
        val stop = selectedStops.getOrNull(binding.profileStopSpinner.selectedItemPosition) ?: return
        prefsRepository.saveStop(route.id, stop.id, stop.name)
        bindSavedStop()
        Snackbar.make(binding.root, R.string.preferred_stop_saved, Snackbar.LENGTH_SHORT).show()
    }

    private fun bindSavedStop() {
        val savedStop = prefsRepository.getSavedStopName()
        val savedRoute = prefsRepository.getSavedRouteId()
        binding.savedStopText.text = if (savedStop.isNullOrBlank() || savedRoute.isNullOrBlank()) {
            getString(R.string.saved_stop_none)
        } else {
            getString(R.string.saved_stop_value, savedStop, savedRoute)
        }
    }

    private fun launchLoadingWatchdog() {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(LOADING_TIMEOUT_MS)
            if (isLoading) {
                Log.d(TAG, "Profile route load timed out waiting for Firebase emission.")
                setLoading(false)
                if (routes.isEmpty()) {
                    showRouteLoadError()
                }
            }
        }
    }

    private fun showRouteLoadError() {
        bindSavedStop()
        if (routes.isEmpty()) {
            binding.savedStopText.text = getString(R.string.no_routes_try_again)
        }
        Snackbar.make(binding.root, R.string.no_routes_try_again, Snackbar.LENGTH_LONG).show()
    }

    private fun setLoading(loading: Boolean) {
        isLoading = loading
        binding.profileProgress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.saveStopButton.isEnabled = !loading && routes.isNotEmpty() && selectedStops.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val TAG = "ProfileFragment"
        const val LOADING_TIMEOUT_MS = 8_000L
    }
}
