package com.gramayatri.app.util

object Constants {
    const val FIREBASE_DATABASE_URL =
        "https://gramayatri-6882e-default-rtdb.asia-southeast1.firebasedatabase.app"

    const val PATH_ROUTES = "routes"
    const val PATH_PINGS = "pings"
    const val PATH_ALERTS = "alerts"
    const val PATH_USER_PREFS = "userPrefs"

    const val PREFS_NAME = "gramayatri_prefs"
    const val KEY_FIRST_LAUNCH = "isFirstLaunch"
    const val KEY_DISPLAY_NAME = "user_display_name"
    const val KEY_SAVED_ROUTE_ID = "saved_route_id"
    const val KEY_SAVED_STOP_ID = "saved_stop_id"
    const val KEY_SAVED_STOP_NAME = "saved_stop_name"
    const val KEY_LAST_PING_TIMESTAMP = "last_ping_timestamp"
    const val KEY_LANGUAGE_CODE = "app_language"
    const val KEY_ACCESSIBILITY_TIP_DISMISSED = "accessibility_tip_dismissed"

    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_KANNADA = "kn"

    const val PING_TYPE_ON_BUS = "ON_BUS"
    const val PING_TYPE_PASSED = "PASSED"

    const val ALERT_CANCELLED = "CANCELLED"
    const val ALERT_DELAYED = "DELAYED"
    const val ALERT_INFO = "INFO"

    const val STALE_PING_THRESHOLD_MIN = 30
    const val PING_RATE_LIMIT_MIN = 5
}
