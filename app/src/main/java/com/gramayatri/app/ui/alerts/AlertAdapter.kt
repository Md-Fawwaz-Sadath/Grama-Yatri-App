package com.gramayatri.app.ui.alerts

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gramayatri.app.R
import com.gramayatri.app.data.model.BusAlert
import com.gramayatri.app.databinding.ItemAlertCardBinding
import com.gramayatri.app.util.Constants
import com.gramayatri.app.util.TimeUtils

class AlertAdapter(
    private val onDismissClick: (BusAlert) -> Unit,
    private val onSpeakClick: (BusAlert) -> Unit
) : ListAdapter<BusAlert, AlertAdapter.AlertViewHolder>(AlertDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemAlertCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlertViewHolder(binding, onDismissClick, onSpeakClick)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AlertViewHolder(
        private val binding: ItemAlertCardBinding,
        private val onDismissClick: (BusAlert) -> Unit,
        private val onSpeakClick: (BusAlert) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(alert: BusAlert) {
            binding.alertTypeText.text = alert.type.toDisplayType()
            binding.routeNameText.text = alert.routeName.ifBlank { alert.routeId }
            binding.alertMessageText.text = alert.toMessage()
            binding.reporterText.text = alert.toReporterLine()
            binding.dismissButton.setOnClickListener { onDismissClick(alert) }
            binding.speakAlertButton.setOnClickListener { onSpeakClick(alert) }

            binding.alertAccent.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(alert.type.toAccentColor(binding.root.context))
            }
        }

        private fun BusAlert.toMessage(): String =
            when {
                note.isNotBlank() && reason.isNotBlank() -> "$reason - $note"
                note.isNotBlank() -> note
                reason.isNotBlank() -> reason
                else -> type.toDisplayType()
            }

        private fun BusAlert.toReporterLine(): String {
            val reporter = reporterName.ifBlank { "Anonymous" }
            return binding.root.context.getString(
                R.string.alert_reported_by_format,
                reporter,
                TimeUtils.formatMinutesAgo(
                    binding.root.context,
                    TimeUtils.minutesAgo(timestamp)
                )
            )
        }

        private fun String.toDisplayType(): String =
            when (this) {
                Constants.ALERT_CANCELLED -> binding.root.context.getString(R.string.alert_display_cancelled)
                Constants.ALERT_DELAYED -> binding.root.context.getString(R.string.alert_display_delayed)
                Constants.ALERT_INFO -> binding.root.context.getString(R.string.alert_display_info)
                else -> ifBlank { binding.root.context.getString(R.string.alert_display_default) }
            }

        private fun String.toAccentColor(context: android.content.Context): Int {
            val colorRes = when (this) {
                Constants.ALERT_CANCELLED -> R.color.eta_red_amber
                Constants.ALERT_DELAYED -> R.color.eta_amber
                Constants.ALERT_INFO -> R.color.green_medium
                else -> R.color.eta_grey
            }
            return ContextCompat.getColor(context, colorRes)
        }
    }

    private object AlertDiffCallback : DiffUtil.ItemCallback<BusAlert>() {
        override fun areItemsTheSame(oldItem: BusAlert, newItem: BusAlert): Boolean =
            oldItem.localKey() == newItem.localKey()

        override fun areContentsTheSame(oldItem: BusAlert, newItem: BusAlert): Boolean =
            oldItem == newItem
    }
}
