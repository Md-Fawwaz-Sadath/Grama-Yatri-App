package com.gramayatri.app.ui.tracking

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gramayatri.app.R
import com.gramayatri.app.data.model.EtaStatus
import com.gramayatri.app.data.model.StopEta
import com.gramayatri.app.databinding.ItemStopTimelineBinding
import com.gramayatri.app.util.TimeUtils

class StopTimelineAdapter :
    ListAdapter<StopTimelineItem, StopTimelineAdapter.StopTimelineViewHolder>(
        StopTimelineItemDiffCallback
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopTimelineViewHolder {
        val binding = ItemStopTimelineBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StopTimelineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StopTimelineViewHolder, position: Int) {
        holder.bind(getItem(position), position == itemCount - 1)
    }

    class StopTimelineViewHolder(
        private val binding: ItemStopTimelineBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StopTimelineItem, isLast: Boolean) {
            val stopEta = item.stopEta
            binding.stopNameText.text = stopEta.stop.name
            binding.etaText.text = stopEta.displayEta(isLast)
            binding.reporterText.text = stopEta.displayReporter()
            binding.etaText.background = createEtaBackground(stopEta.status)
            binding.stopNode.background = createNodeBackground(stopEta.status)
            binding.timelineLine.setBackgroundColor(colorForLine(stopEta.status))
            binding.timelineLine.alpha = if (isLast) 0f else 1f
            binding.savedStopLabel.visibility = if (item.isSavedStop) View.VISIBLE else View.GONE
            binding.timelineRow.background = createRowBackground(item.isSavedStop)
        }

        private fun StopEta.displayEta(isFinalStop: Boolean): String =
            when (status) {
                EtaStatus.BUS_HERE -> binding.root.context.getString(R.string.eta_bus_here)
                EtaStatus.UPCOMING -> displayFutureEta(isFinalStop)
                EtaStatus.PASSED -> binding.root.context.getString(R.string.eta_passed_simple)
                EtaStatus.STALE -> displayFutureEta(isFinalStop, isUnconfirmed = true)
                EtaStatus.UNKNOWN -> binding.root.context.getString(R.string.eta_no_recent_ping)
            }

        private fun StopEta.displayFutureEta(
            isFinalStop: Boolean,
            isUnconfirmed: Boolean = false
        ): String {
            val eta = etaMinutes
            return when {
                eta == null && isUnconfirmed -> binding.root.context.getString(R.string.eta_unconfirmed)
                eta == null -> binding.root.context.getString(R.string.eta_unavailable)
                eta <= 0 && isFinalStop -> binding.root.context.getString(R.string.eta_trip_likely_completed)
                eta <= 0 -> binding.root.context.getString(R.string.eta_bus_may_have_reached)
                isUnconfirmed -> binding.root.context.getString(R.string.eta_min_unconfirmed_format, eta)
                else -> binding.root.context.getString(R.string.eta_min_format, eta)
            }
        }

        private fun StopEta.displayReporter(): String =
            when {
                status == EtaStatus.PASSED -> {
                    binding.root.context.getString(R.string.eta_likely_passed_before_latest_report)
                }
                reporterName.isNullOrBlank() || pingTimestamp == null -> {
                    binding.root.context.getString(R.string.no_recent_report)
                }
                else -> {
                    binding.root.context.getString(
                        R.string.reported_by_format,
                        reporterName,
                        TimeUtils.formatMinutesAgo(binding.root.context, TimeUtils.minutesAgo(pingTimestamp))
                    )
                }
            }

        private fun createEtaBackground(status: EtaStatus): GradientDrawable {
            val colorRes = when (status) {
                EtaStatus.BUS_HERE -> R.color.eta_green
                EtaStatus.UPCOMING -> R.color.eta_amber
                EtaStatus.PASSED, EtaStatus.STALE, EtaStatus.UNKNOWN -> R.color.eta_grey
            }
            return GradientDrawable().apply {
                cornerRadius = 8f * binding.root.resources.displayMetrics.density
                setColor(ContextCompat.getColor(binding.root.context, colorRes))
            }
        }

        private fun createNodeBackground(status: EtaStatus): GradientDrawable {
            val colorRes = when (status) {
                EtaStatus.BUS_HERE, EtaStatus.PASSED -> R.color.green_medium
                else -> R.color.divider
            }
            return GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ContextCompat.getColor(binding.root.context, colorRes))
            }
        }

        private fun colorForLine(status: EtaStatus): Int {
            val colorRes = when (status) {
                EtaStatus.BUS_HERE, EtaStatus.PASSED -> R.color.green_medium
                else -> R.color.divider
            }
            return ContextCompat.getColor(binding.root.context, colorRes)
        }

        private fun createRowBackground(isSavedStop: Boolean): GradientDrawable {
            val colorRes = if (isSavedStop) R.color.green_light else android.R.color.transparent
            return GradientDrawable().apply {
                cornerRadius = 8f * binding.root.resources.displayMetrics.density
                setColor(ContextCompat.getColor(binding.root.context, colorRes))
                if (isSavedStop) {
                    setStroke(
                        (1.5f * binding.root.resources.displayMetrics.density).toInt(),
                        ContextCompat.getColor(binding.root.context, R.color.green_medium)
                    )
                }
            }
        }
    }

    private object StopTimelineItemDiffCallback : DiffUtil.ItemCallback<StopTimelineItem>() {
        override fun areItemsTheSame(
            oldItem: StopTimelineItem,
            newItem: StopTimelineItem
        ): Boolean =
            oldItem.stopEta.stop.id == newItem.stopEta.stop.id

        override fun areContentsTheSame(
            oldItem: StopTimelineItem,
            newItem: StopTimelineItem
        ): Boolean =
            oldItem == newItem
    }
}
