package com.gramayatri.app.ui.home

import android.content.pm.ApplicationInfo
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.gramayatri.app.R
import com.gramayatri.app.data.model.RouteHealth
import com.gramayatri.app.data.model.Route
import com.gramayatri.app.data.model.RouteCardItem
import com.gramayatri.app.databinding.FragmentHomeBinding
import com.gramayatri.app.util.ScheduleUtils
import com.gramayatri.app.util.TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var routeAdapter: RouteAdapter
    private var allRoutes: List<Route> = emptyList()
    private var healthState = HomeHealthUiState()
    private var selectedDayKey = ScheduleUtils.getCurrentDayKey()
    private val currentDayKey = ScheduleUtils.getCurrentDayKey()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupRoutesList()
        setupScheduleFilters()
        setupAccessibilityTip()
        setupRefresh()
        observeHomeState()
        seedRoutesIfDebugBuild()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDismissedAlertHealth()
    }

    private fun setupRoutesList() {
        routeAdapter = RouteAdapter { route ->
            val args = Bundle().apply {
                putString("routeId", route.id)
                putString("selectedDayKey", selectedDayKey)
            }
            findNavController().navigate(R.id.liveTrackingFragment, args)
        }
        binding.routesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = routeAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupScheduleFilters() {
        dayButtons().forEach { (dayKey, button) ->
            button.setOnClickListener {
                selectedDayKey = dayKey
                bindRoutes(allRoutes)
                bindHealthSummary()
            }
        }
        updateFilterButtonStyles()
    }

    private fun setupRefresh() {
        binding.routesSwipeRefresh.setOnRefreshListener {
            viewModel.loadRoutes()
        }
    }

    private fun setupAccessibilityTip() {
        binding.accessibilityTipCard.visibility =
            if (viewModel.isAccessibilityTipDismissed()) View.GONE else View.VISIBLE
        binding.dismissAccessibilityTipButton.setOnClickListener {
            viewModel.dismissAccessibilityTip()
            binding.accessibilityTipCard.visibility = View.GONE
        }
    }

    private fun observeHomeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.routes.collect { routes ->
                        allRoutes = routes
                        bindRoutes(routes)
                    }
                }

                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.routesSwipeRefresh.isRefreshing = isLoading
                    }
                }

                launch {
                    viewModel.errorMessage.collect { message ->
                        updateEmptyState(routeAdapter.itemCount == 0, message)
                    }
                }

                launch {
                    viewModel.healthState.collect { state ->
                        healthState = state
                        bindRoutes(allRoutes)
                        bindHealthSummary()
                    }
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean, errorMessage: String?) {
        binding.emptyStateText.text = errorMessage ?: emptyStateForFilter()
        binding.emptyStateText.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun bindRoutes(routes: List<Route>) {
        updateFilterButtonStyles()
        val filteredRoutes = routes.filter { route ->
            ScheduleUtils.shouldShowRouteForDay(route, selectedDayKey)
        }
        val isTodayTab = selectedDayKey == currentDayKey
        val items = filteredRoutes.map { route ->
            val latestPing = healthState.latestPings[route.id]
            val latestPingMinutesAgo = latestPing?.timestamp?.let { TimeUtils.minutesAgo(it) }
            RouteCardItem(
                route = route,
                selectedDayKey = selectedDayKey,
                lastPingReporterName = latestPing?.reporterName?.takeIf { isTodayTab },
                lastPingMinutesAgo = latestPingMinutesAgo?.takeIf { isTodayTab },
                isActive = isTodayTab && latestPingMinutesAgo != null && latestPingMinutesAgo < 60,
                health = if (isTodayTab) {
                    healthState.routeHealthMap[route.id] ?: RouteHealth.NO_RECENT_DATA
                } else {
                    RouteHealth.NO_RECENT_DATA
                },
                activeAlertCount = if (isTodayTab) {
                    healthState.activeAlertCounts[route.id] ?: 0
                } else {
                    0
                }
            )
        }
        routeAdapter.submitList(items)
        updateEmptyState(items.isEmpty(), viewModel.errorMessage.value)
    }

    private fun emptyStateForFilter(): String =
        getString(
            R.string.schedule_no_routes_today,
            ScheduleUtils.dayFullDisplayLabel(requireContext(), selectedDayKey)
        )

    private fun bindHealthSummary() {
        val summaryBinding = binding.routeHealthSummary
        val visible = healthState.isVisible && allRoutes.isNotEmpty()
        summaryBinding.routeHealthCard.visibility = if (visible) View.VISIBLE else View.GONE
        if (!visible) return

        val runningCount = allRoutes.count { route ->
            ScheduleUtils.shouldShowRouteForDay(route, selectedDayKey)
        }
        summaryBinding.healthRunningText.text = getString(
            R.string.health_running_selected_day_format,
            runningCount
        )
        summaryBinding.healthAlertsText.text = getString(
            R.string.health_active_alerts_format,
            healthState.activeAlertsCount
        )
        summaryBinding.healthReportsText.text = getString(
            R.string.health_reports_today_format,
            healthState.reportsTodayCount
        )
        summaryBinding.healthLastUpdatedText.text = lastUpdatedText()
        summaryBinding.healthAsOfText.text = getString(
            R.string.health_as_of,
            SimpleDateFormat("hh:mm a", Locale.US).format(Date())
        )
    }

    private fun lastUpdatedText(): String {
        val routeName = healthState.lastUpdatedRouteName
        val minutesAgo = healthState.lastUpdatedMinutesAgo
        return if (routeName == null || minutesAgo == null) {
            getString(R.string.health_last_updated_none)
        } else {
            getString(
                R.string.health_last_updated_format,
                routeName,
                TimeUtils.formatMinutesAgo(requireContext(), minutesAgo)
            )
        }
    }

    private fun updateFilterButtonStyles() {
        val selectedColor = requireContext().getColor(R.color.green_medium)
        val unselectedColor = requireContext().getColor(R.color.surface)
        val selectedTextColor = requireContext().getColor(R.color.white)
        val unselectedTextColor = requireContext().getColor(R.color.green_primary)
        val todayUnselectedColor = requireContext().getColor(R.color.green_light)
        dayButtons().forEach { (dayKey, button) ->
            val isSelected = dayKey == selectedDayKey
            val isToday = dayKey == currentDayKey
            val backgroundColor = when {
                isSelected -> selectedColor
                isToday -> todayUnselectedColor
                else -> unselectedColor
            }
            button.backgroundTintList = ColorStateList.valueOf(backgroundColor)
            button.setTextColor(if (isSelected) selectedTextColor else unselectedTextColor)
            button.strokeColor = ColorStateList.valueOf(
                requireContext().getColor(
                    if (isToday || isSelected) R.color.green_primary else R.color.divider
                )
            )
        }
    }

    private fun dayButtons(): List<Pair<String, MaterialButton>> =
        listOf(
            "MONDAY" to binding.monDayButton,
            "TUESDAY" to binding.tueDayButton,
            "WEDNESDAY" to binding.wedDayButton,
            "THURSDAY" to binding.thuDayButton,
            "FRIDAY" to binding.friDayButton,
            "SATURDAY" to binding.satDayButton,
            "SUNDAY" to binding.sunDayButton
        )

    private fun seedRoutesIfDebugBuild() {
        val isDebuggable = requireContext().applicationInfo.flags and
            ApplicationInfo.FLAG_DEBUGGABLE != 0
        if (isDebuggable) {
            viewModel.seedRoutesIfEmpty()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.routesRecyclerView.adapter = null
        _binding = null
    }

}
