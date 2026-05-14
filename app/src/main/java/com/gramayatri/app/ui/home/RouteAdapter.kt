package com.gramayatri.app.ui.home

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gramayatri.app.R
import com.gramayatri.app.data.model.RouteHealth
import com.gramayatri.app.data.model.Route
import com.gramayatri.app.data.model.RouteCardItem
import com.gramayatri.app.databinding.ItemRouteCardBinding
import com.gramayatri.app.util.ScheduleUtils
import com.gramayatri.app.util.TimeUtils

class RouteAdapter(
    private val onItemClick: (Route) -> Unit
) : ListAdapter<RouteCardItem, RouteAdapter.RouteViewHolder>(RouteDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val binding = ItemRouteCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RouteViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RouteViewHolder(
        private val binding: ItemRouteCardBinding,
        private val onItemClick: (Route) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RouteCardItem) {
            binding.routeNameText.text = item.route.name
            binding.stopCountText.text = binding.root.context.getString(
                R.string.stop_count_format,
                item.route.totalStops
            )
            bindOperator(item.route)
            bindHealth(item)
            binding.scheduleText.text = ScheduleUtils.selectedDayScheduleLine(
                binding.root.context,
                item.route,
                item.selectedDayKey
            )
            binding.lastPingText.text = buildLastPingText(item)
            binding.alertCountText.visibility =
                if (item.activeAlertCount > 0) android.view.View.VISIBLE else android.view.View.GONE
            binding.alertCountText.text = binding.root.context.getString(
                R.string.health_route_alert_count_format,
                item.activeAlertCount
            )
            binding.liveDot.background = createLiveDot(item.isActive)
            binding.root.setOnClickListener { onItemClick(item.route) }
        }

        private fun bindOperator(route: Route) {
            val context = binding.root.context
            val operator = route.busOperator.takeIf { it.isNotBlank() }
            binding.operatorText.visibility = if (operator == null) android.view.View.GONE else android.view.View.VISIBLE
            binding.operatorText.text = operator?.let {
                context.getString(
                    R.string.operator_line_format,
                    ScheduleUtils.displayOperator(context, it)
                )
            }

            val busNumber = route.busNumber.takeIf { it.isNotBlank() }
            binding.busNumberText.visibility = if (busNumber == null) android.view.View.GONE else android.view.View.VISIBLE
            binding.busNumberText.text = busNumber?.let {
                context.getString(R.string.bus_number_line_format, it)
            }
        }

        private fun buildLastPingText(item: RouteCardItem): String {
            val reporterName = item.lastPingReporterName
            val minutesAgo = item.lastPingMinutesAgo
            return if (reporterName != null && minutesAgo != null) {
                binding.root.context.getString(
                    R.string.route_pinged_by_format,
                    TimeUtils.formatMinutesAgo(binding.root.context, minutesAgo),
                    reporterName
                )
            } else {
                binding.root.context.getString(R.string.no_recent_report)
            }
        }

        private fun bindHealth(item: RouteCardItem) {
            val context = binding.root.context
            val (labelRes, colorRes) = when (item.health) {
                RouteHealth.GOOD -> R.string.health_good to R.color.green_medium
                RouteHealth.MODERATE -> R.string.health_moderate to R.color.eta_amber
                RouteHealth.ATTENTION_NEEDED -> R.string.health_attention to R.color.terracotta
                RouteHealth.NO_RECENT_DATA -> R.string.health_no_data to R.color.eta_grey
            }
            binding.healthChipText.text = context.getString(labelRes)
            binding.healthChipText.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = context.resources.displayMetrics.density * 14
                setColor(ContextCompat.getColor(context, colorRes))
            }
        }

        private fun createLiveDot(isActive: Boolean): GradientDrawable {
            val colorRes = if (isActive) R.color.green_medium else R.color.eta_grey
            return GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ContextCompat.getColor(binding.root.context, colorRes))
            }
        }
    }

    private object RouteDiffCallback : DiffUtil.ItemCallback<RouteCardItem>() {
        override fun areItemsTheSame(oldItem: RouteCardItem, newItem: RouteCardItem): Boolean =
            oldItem.route.id == newItem.route.id

        override fun areContentsTheSame(oldItem: RouteCardItem, newItem: RouteCardItem): Boolean =
            oldItem == newItem
    }
}
