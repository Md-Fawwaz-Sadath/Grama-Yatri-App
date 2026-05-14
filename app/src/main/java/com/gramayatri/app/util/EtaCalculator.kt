package com.gramayatri.app.util

import com.gramayatri.app.data.model.EtaStatus
import com.gramayatri.app.data.model.Ping
import com.gramayatri.app.data.model.Stop
import com.gramayatri.app.data.model.StopEta

object EtaCalculator {
    fun calculateEtas(
        stops: List<Stop>,
        ping: Ping
    ): List<StopEta> {
        val orderedStops = stops.sortedBy { it.order }
        val pingStopOrder = ping.stopOrder
        val pingTime = ping.timestamp
        val now = System.currentTimeMillis()
        val minutesSincePing = ((now - pingTime) / 60_000L).coerceAtLeast(0L).toInt()

        return orderedStops.map { stop ->
            when {
                stop.order < pingStopOrder -> {
                    StopEta(stop, null, EtaStatus.PASSED, null, null)
                }

                stop.order == pingStopOrder -> {
                    StopEta(stop, 0, EtaStatus.BUS_HERE, ping.reporterName, pingTime)
                }

                else -> {
                    val cumulativeAtt = orderedStops
                        .filter { it.order >= pingStopOrder && it.order < stop.order }
                        .sumOf { it.attToNextMin }
                    val eta = (cumulativeAtt - minutesSincePing).coerceAtLeast(0)
                    val status = if (minutesSincePing > Constants.STALE_PING_THRESHOLD_MIN) {
                        EtaStatus.STALE
                    } else {
                        EtaStatus.UPCOMING
                    }
                    StopEta(stop, eta, status, ping.reporterName, pingTime)
                }
            }
        }
    }

    fun unknownEtas(stops: List<Stop>): List<StopEta> =
        stops.sortedBy { it.order }.map { stop ->
            StopEta(
                stop = stop,
                etaMinutes = null,
                status = EtaStatus.UNKNOWN,
                reporterName = null,
                pingTimestamp = null
            )
        }
}
