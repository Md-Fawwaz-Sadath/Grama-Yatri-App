package com.gramayatri.app.util

import com.gramayatri.app.data.model.BusAlert
import com.gramayatri.app.data.model.Ping
import com.gramayatri.app.data.model.Route
import com.gramayatri.app.data.model.RouteHealth
import com.gramayatri.app.data.model.RouteSummary

object RouteHealthCalculator {
    fun calculate(latestPingTimestamp: Long?, activeAlertCount: Int): RouteHealth {
        if (activeAlertCount > 0) return RouteHealth.ATTENTION_NEEDED
        if (latestPingTimestamp == null) return RouteHealth.NO_RECENT_DATA

        val ageMinutes = TimeUtils.minutesAgo(latestPingTimestamp)
        return when {
            ageMinutes < 5 -> RouteHealth.GOOD
            ageMinutes <= 60 -> RouteHealth.MODERATE
            else -> RouteHealth.NO_RECENT_DATA
        }
    }

    fun aggregateSummary(routeHealthMap: Map<String, RouteHealth>): RouteSummary =
        RouteSummary(
            goodCount = routeHealthMap.values.count { it == RouteHealth.GOOD },
            moderateCount = routeHealthMap.values.count { it == RouteHealth.MODERATE },
            attentionCount = routeHealthMap.values.count { it == RouteHealth.ATTENTION_NEEDED },
            noDataCount = routeHealthMap.values.count { it == RouteHealth.NO_RECENT_DATA }
        )

    fun latestPing(pings: List<Ping>): Ping? =
        pings.filter { it.isForTodayService() }
            .maxByOrNull { it.timestamp }

    fun activeAlertsToday(alerts: List<BusAlert>): List<BusAlert> =
        alerts.filter { it.isForTodayService() }

    fun countReportsToday(pingsByRoute: Map<String, List<Ping>>): Int =
        pingsByRoute.values.flatten().count { it.isForTodayService() }

    fun findLastUpdatedRoute(routes: List<Route>, latestPings: Map<String, Ping?>): Route? =
        routes.maxByOrNull { latestPings[it.id]?.timestamp ?: 0L }
            ?.takeIf { latestPings[it.id] != null }
}

private fun Ping.isForTodayService(): Boolean =
    when {
        serviceDateKey.isNotBlank() -> serviceDateKey == TimeUtils.getTodayServiceDateKey()
        else -> TimeUtils.isTimestampToday(timestamp)
    }

private fun BusAlert.isForTodayService(): Boolean =
    when {
        serviceDateKey.isNotBlank() -> serviceDateKey == TimeUtils.getTodayServiceDateKey()
        else -> TimeUtils.isTimestampToday(timestamp)
    }
