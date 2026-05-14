package com.gramayatri.app.data.repository

import android.content.Context
import com.gramayatri.app.util.Constants

class UserPrefsRepository(
    context: Context
) {
    private val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    fun getDisplayName(): String =
        prefs.getString(Constants.KEY_DISPLAY_NAME, null).orEmpty().ifBlank { "Anonymous" }

    fun setDisplayName(name: String) {
        prefs.edit().putString(Constants.KEY_DISPLAY_NAME, name.trim()).apply()
    }

    fun isFirstLaunch(): Boolean =
        prefs.getBoolean(Constants.KEY_FIRST_LAUNCH, true)

    fun setFirstLaunchDone() {
        prefs.edit().putBoolean(Constants.KEY_FIRST_LAUNCH, false).apply()
    }

    fun getSavedRouteId(): String? =
        prefs.getString(Constants.KEY_SAVED_ROUTE_ID, null)

    fun getSavedStopId(): String? =
        prefs.getString(Constants.KEY_SAVED_STOP_ID, null)

    fun getSavedStopName(): String? =
        prefs.getString(Constants.KEY_SAVED_STOP_NAME, null)

    fun saveStop(routeId: String, stopId: String, stopName: String) {
        prefs.edit()
            .putString(Constants.KEY_SAVED_ROUTE_ID, routeId)
            .putString(Constants.KEY_SAVED_STOP_ID, stopId)
            .putString(Constants.KEY_SAVED_STOP_NAME, stopName)
            .apply()
    }

    fun getLanguageCode(): String =
        prefs.getString(Constants.KEY_LANGUAGE_CODE, Constants.LANGUAGE_ENGLISH)
            ?: Constants.LANGUAGE_ENGLISH

    fun setLanguageCode(languageCode: String) {
        val supportedLanguage = when (languageCode) {
            Constants.LANGUAGE_KANNADA -> Constants.LANGUAGE_KANNADA
            else -> Constants.LANGUAGE_ENGLISH
        }
        prefs.edit().putString(Constants.KEY_LANGUAGE_CODE, supportedLanguage).apply()
    }

    fun isAccessibilityTipDismissed(): Boolean =
        prefs.getBoolean(Constants.KEY_ACCESSIBILITY_TIP_DISMISSED, false)

    fun setAccessibilityTipDismissed() {
        prefs.edit().putBoolean(Constants.KEY_ACCESSIBILITY_TIP_DISMISSED, true).apply()
    }

    fun getLastPingTimestamp(): Long =
        prefs.getLong(Constants.KEY_LAST_PING_TIMESTAMP, 0L)

    fun setLastPingTimestamp(ts: Long) {
        prefs.edit().putLong(Constants.KEY_LAST_PING_TIMESTAMP, ts).apply()
    }

    fun getLastPingTimestamp(routeId: String): Long =
        prefs.getLong(lastPingKey(routeId), 0L)

    fun setLastPingTimestamp(routeId: String, ts: Long) {
        prefs.edit()
            .putLong(Constants.KEY_LAST_PING_TIMESTAMP, ts)
            .putLong(lastPingKey(routeId), ts)
            .apply()
    }

    fun getDismissedAlertIds(): Set<String> =
        prefs.getStringSet(KEY_DISMISSED_ALERT_IDS, emptySet()).orEmpty()

    fun dismissAlert(alertId: String) {
        if (alertId.isBlank()) return
        val updatedIds = getDismissedAlertIds() + alertId
        prefs.edit().putStringSet(KEY_DISMISSED_ALERT_IDS, updatedIds).apply()
    }

    fun isAlertDismissed(alertId: String): Boolean =
        alertId.isNotBlank() && alertId in getDismissedAlertIds()

    fun clearDismissedAlerts() {
        prefs.edit().remove(KEY_DISMISSED_ALERT_IDS).apply()
    }

    private fun lastPingKey(routeId: String): String =
        "${Constants.KEY_LAST_PING_TIMESTAMP}_$routeId"

    private companion object {
        const val KEY_DISMISSED_ALERT_IDS = "dismissed_alert_ids"
    }
}
