# GRAMA-YATRI — WEEKLY SCHEDULE AND ROUTE HEALTH DASHBOARD UPGRADE

**Version:** 1.0  
**Date:** May 2026  
**Document Type:** Companion Enhancement Specification — does NOT replace or modify `GRAMAYATRI_PRD_COMPLETE.md`, `GRAMAYATRI_PRD_PHASES.md`, or `GRAMAYATRI_FEATURE_UPGRADES.md`  
**Platform:** Android (Kotlin, XML Layouts, Firebase Realtime Database, MVVM + Repository, Hilt/KSP)

---

## IMPORTANT NOTICE FOR CODEX

This document specifies two targeted feature upgrades for the Grama-Yatri Android application. It is a companion to the existing PRD and feature upgrade documents. Do NOT rewrite, replace, or contradict anything in those files.

When implementing phases from this document:
- Implement **one phase at a time**. Do not mix Phase 8.9 and Phase 9.0 in a single implementation pass.
- Build and verify each phase independently before moving to the next.
- Keep all existing Firebase paths intact. Only extend the schema as specified.
- Keep the low-data, fast-loading design principle at all times.
- Do not add Google Maps, Cloud Functions, FCM push notifications, Gemini/GenAI, or demo simulation.
- Do not redesign existing screens. Extend them as specified.

---

## 1. PURPOSE

This document specifies two enhancement features for the Grama-Yatri app:

**Feature 1 — Proper Weekly Schedule:** The existing Today/Tomorrow/All day filter on the Home screen will be expanded into a full seven-day weekly timetable view with weekday-specific route information. This makes the app suitable for real rural bus operations where different buses operate on different days of the week.

**Feature 2 — Route Health Dashboard:** A lightweight summary view that communicates how active and reliable each route is based on existing Firebase data — no new infrastructure required. This makes the app more professional and informative for passengers, coordinators, and internship evaluators.

Both features maintain the app's core design constraints: low data usage, fast loading, no maps, no heavy animations, and accessibility for rural users.

---

## 2. CURRENT PROBLEM WITH EXISTING WEEKLY SCHEDULE

The current Home screen implements a basic three-state day filter: **Today**, **Tomorrow**, and **All**. While this provides a starting point, it is insufficient for real rural bus operations for the following reasons:

- Rural bus routes often operate on day-specific schedules. A bus that runs Monday through Friday may not run on Saturday, or may run with a different start time on Sunday.
- The Today/Tomorrow model does not allow a user to plan ahead beyond one day. A student who wants to know if the bus runs on Sunday before making weekend plans cannot do so.
- The filter labels "Today" and "Tomorrow" are relative and do not help a user understand the weekly pattern of a route.
- There is no way to distinguish between a day when a bus does not run versus a day when simply no data has been entered.

The upgrade specified in this document replaces the Today/Tomorrow/All filter with a proper **Monday through Sunday tab view** that represents the actual weekly timetable, auto-selects the current day on launch, and shows only routes active on the selected day.

---

## 3. UPGRADE GOALS

| Goal | Description |
|---|---|
| Weekday tabs | Replace Today/Tomorrow/All with Mon–Sun tabs that default to the current day |
| Day-specific routes | Show only routes active on the selected weekday tab |
| Day-specific times | Show the correct start times for each route on the selected day |
| Persistent schedule | Weekly schedule data lives in Firebase and repeats every week without being overwritten |
| Operational data separation | Pings and alerts are live operational data; schedule is static reference data — they must not interfere |
| Route Health visibility | Provide a lightweight per-route and aggregate health summary using existing Firebase data |
| Low-data compliance | No charts, no maps, no heavy libraries; everything calculated locally |
| Multilingual | All new UI text must have English and Kannada string resources |

---

## 4. FEATURE 1 — PROPER WEEKLY SCHEDULE

### 4.1 Objective

Expand the Home screen's day filter into a full seven-day weekly timetable. Each weekday tab shows the routes active on that day with their day-specific start times. The current day is automatically selected when the app opens. The schedule repeats weekly and is never automatically overwritten by the app.

---

### 4.2 User Flow

1. User opens the app. The Home screen loads.
2. A horizontal row of weekday chips/tabs is displayed at the top of the route list section:  
   `Mon | Tue | Wed | Thu | Fri | Sat | Sun`
3. The current day of the week is automatically selected and visually highlighted.
4. The route list below shows only routes whose `weeklySchedule/{DAY}/active` field is `true` for the selected day.
5. Each route card shows the start time(s) for that specific day.
6. If the user taps a different day tab, the route list updates to show routes and times for that day.
7. If no routes are active on the selected day, an empty state message is shown.
8. The user can tap any route card to open Live Tracking as usual — the weekly schedule data does not affect Live Tracking.
9. The selected tab does not persist between app sessions. On next launch, the current day is selected again automatically.

---

### 4.3 UI Requirements

**Weekday Chips/Tabs:**
- Implement as a `HorizontalScrollView` containing a row of `MaterialButton` chips or a `TabLayout` with short day labels.
- Short labels: `Mon`, `Tue`, `Wed`, `Thu`, `Fri`, `Sat`, `Sun`
- In Kannada mode: `ಸೋಮ`, `ಮಂಗ`, `ಬುಧ`, `ಗುರು`, `ಶುಕ್ರ`, `ಶನಿ`, `ಭಾನು`
- Selected tab: Primary Green background (`#2D6A4F`) with white text.
- Unselected tab: Light grey background with dark text.
- Chips should be scrollable horizontally on small screens — all seven must fit without clipping even on a 360dp wide screen.
- Today's tab should have a subtle dot or underline indicator in addition to the selected highlight to distinguish it from a manually selected tab.

**Route Card for Weekly Schedule View:**

Each card on the Home screen should show:

```
┌──────────────────────────────────────────────┐
│  🚌 KA-43 F-1021          KSRTC    [speaker] │
│  Doddaballapur → Bengaluru                   │
│  ──────────────────────────────────────────  │
│  Wednesday: 06:30 AM · 05:30 PM              │
│  Last reported: 8 min ago          LIVE ●    │
│  [1 active alert]                            │
└──────────────────────────────────────────────┘
```

- **Route name:** 15sp Medium, Primary Green.
- **Bus operator + number:** 11sp, grey.
- **Day + start times:** 12sp, dark grey. Format: `{DayName}: {time1} · {time2}`. If no start times defined, show `No scheduled times listed`.
- **Last reported:** 11sp, grey. Shows ping staleness as before.
- **LIVE indicator:** Green pulsing dot if ping is fresh (< 60 min).
- **Alert chip:** Small red chip showing alert count — only visible if active alerts exist.
- **Speaker button:** Top right, 48dp touch target.
- If `active = false` for the selected day: **do not show the card at all** for that day (filtered out).
- If `weeklySchedule` node is missing entirely for a route: show the card but display `Schedule not available` in the times row. Do not crash or hide the card.

**Empty State:**
- When no routes are active on the selected day, show:
  - An illustration-free empty state text card.
  - English: `"No routes scheduled for {DayName}. Check another day or contact your route coordinator."`
  - Kannada: corresponding translated string.

---

### 4.4 Data Requirements

**What the ViewModel needs to expose:**
- `selectedDay: MutableStateFlow<DayOfWeek>` — current selected weekday tab, initialized to `Calendar.getInstance().get(Calendar.DAY_OF_WEEK)`.
- `routesForSelectedDay: StateFlow<List<RouteWithDaySchedule>>` — filtered list of routes active on the selected day, each carrying the start times for that day.
- `latestEtaPerRoute: StateFlow<Map<String, EtaResult?>>` — for the LIVE indicator and last ping time on cards.
- `activeAlertCountPerRoute: StateFlow<Map<String, Int>>` — count of active alerts per route.

**Model additions:**

```kotlin
data class DaySchedule(
    val startTimes: List<String> = emptyList(),
    val active: Boolean = false
)

data class WeeklySchedule(
    val MONDAY: DaySchedule = DaySchedule(),
    val TUESDAY: DaySchedule = DaySchedule(),
    val WEDNESDAY: DaySchedule = DaySchedule(),
    val THURSDAY: DaySchedule = DaySchedule(),
    val FRIDAY: DaySchedule = DaySchedule(),
    val SATURDAY: DaySchedule = DaySchedule(),
    val SUNDAY: DaySchedule = DaySchedule()
)

data class RouteWithDaySchedule(
    val route: Route,
    val scheduleForDay: DaySchedule?,
    val latestEta: EtaResult?,
    val activeAlertCount: Int
)
```

**Utility — current day mapping:**

```kotlin
fun Calendar.toFirebaseDayKey(): String {
    return when (this.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY    -> "MONDAY"
        Calendar.TUESDAY   -> "TUESDAY"
        Calendar.WEDNESDAY -> "WEDNESDAY"
        Calendar.THURSDAY  -> "THURSDAY"
        Calendar.FRIDAY    -> "FRIDAY"
        Calendar.SATURDAY  -> "SATURDAY"
        Calendar.SUNDAY    -> "SUNDAY"
        else -> "MONDAY"
    }
}
```

---

### 4.5 Firebase Schema

Add a `weeklySchedule` node under each route. This node is **reference data** — it represents the fixed weekly timetable and should never be automatically overwritten by the app. Only an admin updating Firebase Console should change it.

```json
{
  "routes": {
    "route_001": {
      "meta": {
        "name": "Doddaballapur → Bengaluru",
        "totalStops": 5,
        "active": true
      },
      "busOperator": "KSRTC",
      "busNumber": "KA-43 F-1021",
      "weeklySchedule": {
        "MONDAY":    { "startTimes": ["06:30", "17:30"], "active": true  },
        "TUESDAY":   { "startTimes": ["06:30", "17:30"], "active": true  },
        "WEDNESDAY": { "startTimes": ["06:30", "17:30"], "active": true  },
        "THURSDAY":  { "startTimes": ["06:30", "17:30"], "active": true  },
        "FRIDAY":    { "startTimes": ["06:30", "17:30"], "active": true  },
        "SATURDAY":  { "startTimes": ["07:00"],           "active": true  },
        "SUNDAY":    { "startTimes": [],                  "active": false }
      },
      "stops": { ... },
      "etas":  { ... },
      "pings": { ... },
      "alerts": { ... }
    }
  }
}
```

**Important schema rules:**
- `weeklySchedule` is a sibling of `stops`, `etas`, `pings`, and `alerts` — it does NOT live inside any of those nodes.
- `busOperator` and `busNumber` remain at the route root level as before — do not move them.
- `active: false` on a day means the bus does not run that day. The app should filter these routes out from the day's list.
- `startTimes: []` (empty array) with `active: true` means the bus runs that day but no specific times are listed — show `No scheduled times listed` in the card.
- `startTimes: []` with `active: false` means no service — filter the route out entirely.
- If the `weeklySchedule` node is absent for a route, the route card is still shown but the times row shows `Schedule not available`. This ensures backward compatibility.

**What NOT to change in Firebase:**
- `/routes/{routeId}/stops` — untouched.
- `/routes/{routeId}/etas` — untouched (live operational data).
- `/routes/{routeId}/pings` — untouched (live operational data).
- `/routes/{routeId}/alerts` — untouched (live operational data).

---

### 4.6 Kotlin/Android Implementation Notes

**Repository changes — `RouteRepository.kt`:**

Add a method to fetch the weekly schedule for all routes:

```kotlin
fun observeWeeklySchedules(): LiveData<Map<String, WeeklySchedule>> {
    val liveData = MutableLiveData<Map<String, WeeklySchedule>>()
    FirebaseDatabase.getInstance()
        .getReference("routes")
        .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val scheduleMap = mutableMapOf<String, WeeklySchedule>()
                snapshot.children.forEach { routeSnapshot ->
                    val routeId = routeSnapshot.key ?: return@forEach
                    val schedule = routeSnapshot.child("weeklySchedule")
                        .getValue(WeeklySchedule::class.java)
                    if (schedule != null) {
                        scheduleMap[routeId] = schedule
                    }
                }
                liveData.postValue(scheduleMap)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    return liveData
}
```

**ViewModel changes — `RouteViewModel.kt`:**

- Add `selectedDay: MutableStateFlow<String>` initialized to `Calendar.getInstance().toFirebaseDayKey()`.
- Add a derived `routesForSelectedDay` that combines routes + weekly schedules + selectedDay and filters for `active == true` on that day.
- Expose `selectedDay` as a `StateFlow` so the fragment can observe it and update the chip selection.

**Fragment changes — `RouteSelectionFragment.kt`:**

- Replace the Today/Tomorrow/All chip group with a `HorizontalScrollView` containing a `ChipGroup` with seven chips.
- On fragment creation, read the current day and programmatically check the matching chip.
- On chip selection change, post the selected day key to `RouteViewModel.selectedDay`.
- Observe `routesForSelectedDay` and submit the filtered list to the adapter.
- Show/hide empty state based on whether the list is empty.

**Adapter changes — `RouteSelectionAdapter.kt`:**

- `RouteSelectionAdapter` should now accept `List<RouteWithDaySchedule>` instead of `List<Route>`.
- Update `bind()` to render day-specific start times from `scheduleForDay?.startTimes`.
- Format start times as `"06:30 AM · 05:30 PM"` using `SimpleDateFormat("hh:mm a")`.
- Render the LIVE indicator and alert chip using the existing logic.

**Navigation Component:**

No changes to `nav_graph.xml` — the weekly schedule is a Home screen upgrade, not a new screen.

---

### 4.7 Kannada Support

Add all new strings to both `res/values/strings.xml` (English) and `res/values-kn/strings.xml` (Kannada).

**English strings to add:**

```xml
<string name="day_mon">Mon</string>
<string name="day_tue">Tue</string>
<string name="day_wed">Wed</string>
<string name="day_thu">Thu</string>
<string name="day_fri">Fri</string>
<string name="day_sat">Sat</string>
<string name="day_sun">Sun</string>
<string name="schedule_no_routes_today">No routes scheduled for %1$s. Check another day or contact your route coordinator.</string>
<string name="schedule_no_times">No scheduled times listed</string>
<string name="schedule_not_available">Schedule not available</string>
<string name="today_indicator">Today</string>
```

**Kannada strings to add (`values-kn/strings.xml`):**

```xml
<string name="day_mon">ಸೋಮ</string>
<string name="day_tue">ಮಂಗ</string>
<string name="day_wed">ಬುಧ</string>
<string name="day_thu">ಗುರು</string>
<string name="day_fri">ಶುಕ್ರ</string>
<string name="day_sat">ಶನಿ</string>
<string name="day_sun">ಭಾನು</string>
<string name="schedule_no_routes_today">%1$s ದಿನ ಯಾವುದೇ ಮಾರ್ಗವಿಲ್ಲ. ಮತ್ತೊಂದು ದಿನ ಪರೀಕ್ಷಿಸಿ.</string>
<string name="schedule_no_times">ಸಮಯ ನಮೂದಿಸಿಲ್ಲ</string>
<string name="schedule_not_available">ವೇಳಾಪಟ್ಟಿ ಲಭ್ಯವಿಲ್ಲ</string>
<string name="today_indicator">ಇಂದು</string>
```

---

### 4.8 Testing Checklist — Feature 1

- [ ] On launch, the chip matching today's day of the week is selected and highlighted.
- [ ] Tapping a different day chip filters the route list to show only routes with `active: true` for that day.
- [ ] Start times shown on each card match the `startTimes` array for the selected day.
- [ ] A route with `active: false` for the selected day does not appear in the list.
- [ ] A route missing `weeklySchedule` entirely still appears with `Schedule not available` in the times row.
- [ ] A route with `active: true` but `startTimes: []` shows `No scheduled times listed`.
- [ ] Empty state message appears when no routes are active for the selected day.
- [ ] Selecting a route card still opens Live Tracking correctly.
- [ ] Switching to Kannada: day chips show Kannada labels, empty state and times rows show Kannada text.
- [ ] On a new app launch the next day, the correct new day is auto-selected (not yesterday's selection).
- [ ] Schedule data in Firebase is NOT modified or deleted by any user action in the app.
- [ ] Existing pings and alerts are unaffected by the schedule feature.
- [ ] Horizontal scroll works correctly on a 360dp screen — all seven chips accessible.

---

### 4.9 Success Criteria — Feature 1

- The current weekday tab is selected automatically on every app launch with zero manual input.
- Route cards correctly show day-specific start times matching the Firebase `weeklySchedule` data.
- Routes not active on the selected day are completely absent from the list.
- The schedule data in Firebase is never modified by the app itself.
- All new UI strings have English and Kannada translations.
- The feature adds no more than 5 KB to average data usage per session.

---

## 5. FEATURE 2 — ROUTE HEALTH DASHBOARD

### 5.1 Objective

Add a lightweight summary section to the Home screen (or a separate accessible screen) that communicates how active and reliable the routes are — using only data already available from Firebase. No new Firebase nodes, no Cloud Functions, no charts. Health is calculated entirely on the device from existing `/routes`, `/etas`, `/pings`, and `/alerts` data.

This feature makes the app significantly more professional for a demo and more useful for a route coordinator or experienced commuter who wants a quick overview before diving into individual routes.

---

### 5.2 User Flow

**Option A — Summary header above route list (recommended for simplicity):**

1. User opens the Home screen.
2. Above the weekday chips and route list, a compact summary card is shown.
3. The card shows aggregate figures: routes running today, active alerts, reports today, most recently updated route.
4. Below the route list, each route card already shows its own health chip (Good / Moderate / Attention Needed / No Recent Data).
5. No separate screen needed — all health information is visible on the Home screen.

**Option B — Separate Route Health screen (if Home is too cluttered):**

1. A `Route Health` menu option is added to the Home screen toolbar menu.
2. Tapping it opens `RouteHealthFragment`.
3. The fragment shows the aggregate summary at the top and a per-route health list below.
4. Back navigation returns to Home.

Implement Option A first. Only implement Option B if Option A makes the Home screen feel crowded after testing on a real device.

---

### 5.3 UI Requirements

**Aggregate summary card (top of Home, above weekday chips):**

```
┌──────────────────────────────────────────────┐
│  Route Health Summary          [as of 07:45] │
│  ─────────────────────────────────────────── │
│  Running today: 3 routes                     │
│  Active alerts: 1                            │
│  Reports today: 7                            │
│  Last update: Doddaballapur Rt · 4 min ago   │
└──────────────────────────────────────────────┘
```

- Card elevation: 1dp.
- Background: `#F5F5F5` (light grey card surface).
- Left accent border: `4dp` wide, Primary Green.
- Title: `Route Health Summary` — 13sp Bold, Navy.
- Timestamp: `as of HH:MM` — 11sp, grey, right-aligned.
- Each data row: label (grey, 11sp) + value (dark, 12sp Bold) side by side.
- Card is hidden (`View.GONE`) if route data has not loaded yet — show only when data is available.

**Per-route health chip on each route card:**

Add a small chip to each route card in the bottom-right area:

| Health State | Label | Chip Color |
|---|---|---|
| Good | `Good` | Green `#40916C`, white text |
| Moderate | `Moderate` | Amber `#854F0B` on `#FAEEDA` |
| Attention Needed | `Attention` | Terracotta `#993C1D` on `#FAECE7` |
| No Recent Data | `No Data` | Grey `#5F5E5A` on `#F1EFE8` |

- Chip height: 20dp. Font size: 10sp Bold.
- Chip appears in the bottom-right corner of the route card, replacing no existing element — it is additive.
- `contentDescription` must be set for accessibility: `"Route health: Good"` etc.

---

### 5.4 Data Requirements

No new Firebase reads are needed. All health data comes from nodes already being read:

| Data Point | Source |
|---|---|
| Routes running today | Filter routes by `weeklySchedule/{TODAY}/active == true` |
| Active alerts count | Count `resolved == false` entries across all `/routes/{id}/alerts/` |
| Reports today | Count pings in `/routes/{id}/pings/` with timestamp within the last 24 hours |
| Last updated route | Find the route with the most recent ping timestamp across all routes |
| Per-route health | Calculated locally — see Section 5.5 |

All of this data is already observed by `RouteViewModel`. The health calculation is a pure function that runs on the already-loaded data — no additional Firebase listeners are required.

---

### 5.5 Health Calculation Logic

Health is calculated per route using a `RouteHealthCalculator` utility object in `utils/RouteHealthCalculator.kt`.

**Health states:**

```kotlin
enum class RouteHealth {
    GOOD,
    MODERATE,
    ATTENTION_NEEDED,
    NO_RECENT_DATA
}
```

**Calculation rules (evaluated in order — first match wins):**

| Condition | Health State |
|---|---|
| Has at least 1 active (unresolved) alert | `ATTENTION_NEEDED` |
| No ping in the last 60 minutes | `NO_RECENT_DATA` |
| Most recent ping is < 5 minutes old AND no active alert | `GOOD` |
| Most recent ping is 5–30 minutes old AND no active alert | `MODERATE` |
| Most recent ping is 30–60 minutes old AND no active alert | `MODERATE` |
| Any other case | `NO_RECENT_DATA` |

**Implementation:**

```kotlin
object RouteHealthCalculator {

    fun calculate(
        latestPingTimestamp: Long?,
        activeAlertCount: Int
    ): RouteHealth {
        if (activeAlertCount > 0) return RouteHealth.ATTENTION_NEEDED
        if (latestPingTimestamp == null) return RouteHealth.NO_RECENT_DATA

        val ageMinutes = (System.currentTimeMillis() - latestPingTimestamp) / 60_000L
        return when {
            ageMinutes < 5  -> RouteHealth.GOOD
            ageMinutes < 60 -> RouteHealth.MODERATE
            else            -> RouteHealth.NO_RECENT_DATA
        }
    }

    fun aggregateSummary(
        routeHealthMap: Map<String, RouteHealth>
    ): RouteSummary {
        val good      = routeHealthMap.values.count { it == RouteHealth.GOOD }
        val moderate  = routeHealthMap.values.count { it == RouteHealth.MODERATE }
        val attention = routeHealthMap.values.count { it == RouteHealth.ATTENTION_NEEDED }
        val noData    = routeHealthMap.values.count { it == RouteHealth.NO_RECENT_DATA }
        return RouteSummary(good, moderate, attention, noData)
    }
}

data class RouteSummary(
    val goodCount: Int,
    val moderateCount: Int,
    val attentionCount: Int,
    val noDataCount: Int
)
```

**Reports today calculation:**

```kotlin
fun countReportsToday(pings: List<Ping>): Int {
    val startOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    return pings.count { it.timestamp >= startOfToday }
}
```

**Last updated route:**

```kotlin
fun findLastUpdatedRoute(
    routes: List<Route>,
    latestPings: Map<String, Long?>
): Route? {
    return routes.maxByOrNull { latestPings[it.routeId] ?: 0L }
}
```

All of these functions are pure and testable — no Firebase access, no context required.

---

### 5.6 Firebase Usage

No new Firebase paths are created for Route Health. All data is derived from:

- `/routes/{id}/weeklySchedule/{DAY}/active` — already read for Feature 1.
- `/routes/{id}/etas/{stopId}/timestamp` — already read for Live Tracking.
- `/routes/{id}/pings/{pingId}/timestamp` — already available.
- `/routes/{id}/alerts/{alertId}/resolved` — already read for Alerts screen.

The ViewModel combines these into health data purely in-memory. **No Firebase writes occur as part of Route Health.**

---

### 5.7 Kannada Support

Add all new Route Health strings to both `res/values/strings.xml` and `res/values-kn/strings.xml`.

**English strings to add:**

```xml
<string name="route_health_title">Route Health Summary</string>
<string name="health_running_today">Running today</string>
<string name="health_active_alerts">Active alerts</string>
<string name="health_reports_today">Reports today</string>
<string name="health_last_updated">Last updated</string>
<string name="health_good">Good</string>
<string name="health_moderate">Moderate</string>
<string name="health_attention">Attention</string>
<string name="health_no_data">No Data</string>
<string name="health_as_of">as of %1$s</string>
<string name="health_routes">routes</string>
<string name="health_min_ago">%1$d min ago</string>
```

**Kannada strings to add:**

```xml
<string name="route_health_title">ಮಾರ್ಗ ಆರೋಗ್ಯ ಸಾರಾಂಶ</string>
<string name="health_running_today">ಇಂದು ಚಲಿಸುತ್ತಿದೆ</string>
<string name="health_active_alerts">ಸಕ್ರಿಯ ಎಚ್ಚರಿಕೆಗಳು</string>
<string name="health_reports_today">ಇಂದಿನ ವರದಿಗಳು</string>
<string name="health_last_updated">ಕೊನೆಯ ನವೀಕರಣ</string>
<string name="health_good">ಉತ್ತಮ</string>
<string name="health_moderate">ಮಧ್ಯಮ</string>
<string name="health_attention">ಗಮನ ಬೇಕು</string>
<string name="health_no_data">ಡೇಟಾ ಇಲ್ಲ</string>
<string name="health_as_of">%1$s ರಂತೆ</string>
<string name="health_routes">ಮಾರ್ಗಗಳು</string>
<string name="health_min_ago">%1$d ನಿಮಿಷ ಹಿಂದೆ</string>
```

---

### 5.8 Testing Checklist — Feature 2

- [ ] Route Health summary card appears above weekday chips once route data is loaded.
- [ ] Card is hidden (`View.GONE`) before data loads — no flash of empty values.
- [ ] `Running today` count matches the number of route cards shown in the current day tab.
- [ ] `Active alerts` count matches total unresolved alerts across all routes.
- [ ] `Reports today` count matches pings with timestamps after midnight today.
- [ ] `Last updated` shows the correct route name and ping age.
- [ ] Timestamp `as of HH:MM` updates correctly when data refreshes.
- [ ] Each route card shows the correct health chip (Good / Moderate / Attention / No Data).
- [ ] A route with an active unresolved alert shows `Attention` — not `Good` or `Moderate`.
- [ ] A route with no pings in the last 60 minutes shows `No Data`.
- [ ] A route with a ping 3 minutes ago and no alerts shows `Good`.
- [ ] A route with a ping 20 minutes ago and no alerts shows `Moderate`.
- [ ] Health chip colors are correct in both light mode and dark mode.
- [ ] All labels display in Kannada when Kannada is the selected language.
- [ ] `RouteHealthCalculator` functions produce correct output for edge cases: null timestamp, zero alerts, timestamp exactly at boundary minutes.
- [ ] No additional Firebase reads are triggered by the health feature — confirm using Android Studio Network Profiler.

---

### 5.9 Success Criteria — Feature 2

- Route Health summary card is always accurate and consistent with the route list shown.
- Per-route health chips correctly reflect each route's ping age and alert status.
- All health calculations happen client-side — zero new Firebase paths, zero new Firebase reads.
- English and Kannada strings are present for all health-related UI text.
- The feature adds zero data usage beyond what is already read for the route list.
- A 5-minute demo session shows the health summary updating automatically as new pings arrive.

---

## 6. SUGGESTED IMPLEMENTATION PHASE MAP

### Phase 8.9 — Proper Weekly Schedule Tabs

**Objective:** Replace the Today/Tomorrow/All filter with a full Monday–Sunday weekday tab view.

**Key tasks:**
- Add `weeklySchedule` data to Firebase Console for all demo routes (manually seed before implementation).
- Add `DaySchedule.kt` and `WeeklySchedule.kt` model files.
- Update `RouteRepository.kt` to parse `weeklySchedule` from Firebase.
- Update `RouteViewModel.kt` to expose `selectedDay` and `routesForSelectedDay`.
- Update `RouteSelectionFragment.kt` to render weekday chips and observe the filtered list.
- Update `RouteSelectionAdapter.kt` to accept `RouteWithDaySchedule` and render start times.
- Add all English and Kannada string resources for weekday labels and schedule states.
- Add empty state view to `fragment_route_selection.xml`.
- Build and verify. Test by switching tabs and confirming correct route filtering.

**Files likely affected:**
- `DaySchedule.kt` — new model
- `WeeklySchedule.kt` — new model
- `RouteWithDaySchedule.kt` — new model
- `Route.kt` — add `weeklySchedule: WeeklySchedule?` field
- `RouteRepository.kt` — parse weekly schedule
- `RouteViewModel.kt` — add selectedDay, routesForSelectedDay
- `RouteSelectionFragment.kt` — weekday chips UI and observer
- `RouteSelectionAdapter.kt` — render day-specific data
- `fragment_route_selection.xml` — chips + empty state
- `item_route_card.xml` — start times row
- `strings.xml` + `values-kn/strings.xml`

**Firebase changes:** Add `weeklySchedule` node under each route in Firebase Console (manually, not by code).

**Success gate before proceeding to Phase 9.0:**  
Today's day tab is auto-selected on launch. Tapping a different day shows only routes active that day. Schedule data in Firebase is unmodified by the app.

---

### Phase 9.0 — Route Health Dashboard

**Objective:** Add the aggregate health summary card and per-route health chips using existing Firebase data.

**Key tasks:**
- Add `RouteHealth.kt` enum.
- Add `RouteSummary.kt` data class.
- Add `RouteHealthCalculator.kt` utility object with all calculation functions.
- Update `RouteViewModel.kt` to expose `routeHealthMap`, `routeSummary`, `reportsToday`, `lastUpdatedRoute`.
- Update `RouteSelectionFragment.kt` to observe health data and bind to summary card.
- Update `RouteSelectionAdapter.kt` to render health chip on each route card.
- Add `view_route_health_summary.xml` as an included/merged layout for the summary card.
- Update `item_route_card.xml` to add the health chip.
- Add all English and Kannada string resources for health labels.
- Build and verify. Test all four health states with appropriate Firebase data.

**Files likely affected:**
- `RouteHealth.kt` — new enum
- `RouteSummary.kt` — new data class
- `RouteHealthCalculator.kt` — new utility
- `RouteViewModel.kt` — add health LiveData
- `RouteSelectionFragment.kt` — observe and bind summary card
- `RouteSelectionAdapter.kt` — add health chip to bind()
- `view_route_health_summary.xml` — new layout for summary card
- `item_route_card.xml` — add health chip
- `strings.xml` + `values-kn/strings.xml`

**Firebase changes:** None.

**Success gate before proceeding to Phase 9.1:**  
Summary card shows correct counts. Health chips show correct state for each route. No new Firebase reads triggered.

---

### Phase 9.1 — Final QA and Demo Preparation

**Objective:** Full end-to-end verification of all features from Phases 8.9 and 9.0 together with all prior phases.

**Key tasks:**
- Seed Firebase with complete weekly schedule data for both demo routes.
- Verify auto-selection of current day on every fresh launch.
- Verify health chips and summary card accuracy with both fresh and stale ping data.
- Verify Kannada mode for all new strings.
- Verify empty state when a day has no active routes.
- Run on a physical device (Redmi or Samsung budget range) — confirm no layout clipping on small screens.
- Confirm APK size remains under 5 MB.
- Confirm cold start time remains under 3 seconds.
- Update `README.md` with instructions for seeding weekly schedule data in Firebase Console.

**Files likely affected:** No new files — QA and documentation only.

**Success gate:** All Phase 8.9 and Phase 9.0 success criteria met simultaneously on a real device.

---

## 7. IMPORTANT EDGE CASES

### 7.1 Missing Schedule Data

A route may exist in Firebase without a `weeklySchedule` node. This will happen for any routes seeded before Phase 8.9 was implemented.

**Handling:**
- `RouteRepository` must use `?.getValue(WeeklySchedule::class.java)` — never force-unwrap.
- If `weeklySchedule` is null, the route is shown in the list regardless of selected day (safe fallback).
- The start times row shows `Schedule not available` — not a crash, not a blank.
- Health calculation treats a route with no `weeklySchedule` as having no schedule constraint — still healthy if it has recent pings.

---

### 7.2 No Routes on Selected Day

When all routes have `active: false` for the selected day (e.g. Sunday with no service):

- The RecyclerView is hidden.
- An empty state `TextView` is shown with the message: `"No routes scheduled for Sunday. Check another day or contact your route coordinator."`
- The Route Health summary card is still visible but shows `Running today: 0`.

---

### 7.3 Old Pings

Pings from previous days or sessions may exist in Firebase. These are expected and should not be deleted by the app.

**Handling:**
- Pings are already treated as stale if their timestamp is more than 60 minutes old (existing logic).
- For health calculation, treat any ping older than 60 minutes as not contributing to `GOOD` or `MODERATE` status.
- `Reports today` on the summary card counts only pings with timestamps after today's midnight — old pings from yesterday are excluded automatically.
- Do NOT delete old pings — they are historical records. Only an admin should clean up Firebase data.

---

### 7.4 Old Alerts

Alerts that were posted days or weeks ago and never resolved may exist in Firebase.

**Handling:**
- The health calculation uses `resolved == false` to identify active alerts — if an old unresolved alert exists, it will correctly show as `ATTENTION_NEEDED` until resolved.
- This is intentional: an unresolved alert is still relevant until someone marks it resolved.
- The `Active alerts` count on the summary card reflects total unresolved alerts across all routes.
- Filtering alerts by date is NOT implemented in this upgrade — it is listed as future scope.

---

### 7.5 Different Languages

When the user switches between English and Kannada in Profile:

- All weekday chip labels change immediately via `LanguageManager.applyLanguage()` + `recreate()`.
- All health chip labels, summary card labels, and empty state messages change immediately.
- Start times (e.g. `06:30 AM`) are formatted using `Locale.ENGLISH` regardless of app language — time format should remain consistent for rural users.

---

### 7.6 Firebase Route Data Updates

If a route coordinator updates `weeklySchedule` data directly in Firebase Console while a user has the app open:

- The `ValueEventListener` on the routes node will fire and the UI will update automatically.
- No app restart is required.
- This is the expected behaviour — Firebase real-time sync handles it natively.

---

## 8. FUTURE SCOPE

The following items are intentionally excluded from this upgrade but are documented here for future reference.

| Item | Notes |
|---|---|
| Admin dashboard | A web or Android screen allowing route coordinators to edit weekly schedules, add/remove routes, and update bus details without accessing Firebase Console directly. |
| Auto cleanup of stale pings and alerts | A scheduled job (WorkManager or Cloud Functions) that deletes pings older than 7 days and alerts older than 30 days. Excluded from this upgrade because automatic deletion carries risk — excluded from this upgrade. |
| Push notifications for alerts | FCM broadcast to route subscribers when a new alert is posted. Requires Firebase Blaze plan. |
| Offline cache improvements | Caching the weekly schedule in Room DB so it is available offline. Currently the schedule requires an active Firebase connection. |
| Maps as optional future feature | If government APIs or GPS hardware becomes available on buses, a map view could be added as an optional screen. Explicitly excluded from this upgrade. |
| Voice Assist reading Route Health summary | The TTS `VoiceAssistHelper` could read the health summary aloud. Deferred to future enhancement. |
| Filtering alerts by date | Showing only alerts from the last 7 days rather than all historical unresolved alerts. Currently all unresolved alerts contribute to health state. |

---

## 9. CODEX PROMPT GUIDELINES

When using this document as the basis for Codex implementation prompts, follow these rules:

**Reference this file explicitly:**  
Begin each implementation prompt with:  
`"Refer to claude_md_files/GRAMAYATRI_WEEKLY_SCHEDULE_AND_ROUTE_HEALTH_UPGRADE.md for the specification. Implement Phase [X] only."`

**One phase per prompt:**  
Do not ask Codex to implement Phase 8.9 and Phase 9.0 in the same prompt. Each phase must be built and verified independently. The success gate for each phase must be confirmed before the next phase prompt is issued.

**Specify what NOT to change:**  
Always remind Codex:
- Do not modify `weeklySchedule` data in Firebase from within the app.
- Do not delete pings or alerts.
- Do not add maps, Cloud Functions, FCM, or Gemini.
- Do not redesign screens not mentioned in the phase.

**Reference existing files:**  
Codex should be aware of and consistent with:
- `GRAMAYATRI_PRD_COMPLETE.md` — overall architecture, screen map, Firebase structure.
- `GRAMAYATRI_FEATURE_UPGRADES.md` — Phase 8.5–8.8 features already implemented.
- This file — Phases 8.9, 9.0, 9.1.

**Verify before declaring done:**  
Each phase prompt should end with:  
`"After implementing, confirm the success criteria listed in Section [4.9 / 5.9] of GRAMAYATRI_WEEKLY_SCHEDULE_AND_ROUTE_HEALTH_UPGRADE.md are met before marking the phase complete."`

**Example prompt for Phase 8.9:**

```
Refer to claude_md_files/GRAMAYATRI_WEEKLY_SCHEDULE_AND_ROUTE_HEALTH_UPGRADE.md.
Implement Phase 8.9: Proper Weekly Schedule Tabs only.
Do not implement Phase 9.0 or any other phase.
Do not modify Firebase data from within the app.
Do not add maps, Cloud Functions, or FCM.
After implementing, confirm the success criteria in Section 4.9 are met.
```

**Example prompt for Phase 9.0:**

```
Phase 8.9 is complete and verified.
Refer to claude_md_files/GRAMAYATRI_WEEKLY_SCHEDULE_AND_ROUTE_HEALTH_UPGRADE.md.
Implement Phase 9.0: Route Health Dashboard only.
All health calculations must happen client-side using existing Firebase data.
Do not create new Firebase paths.
After implementing, confirm the success criteria in Section 5.9 are met.
```

---

*This document is a companion to `GRAMAYATRI_PRD_COMPLETE.md`, `GRAMAYATRI_PRD_PHASES.md`, and `GRAMAYATRI_FEATURE_UPGRADES.md`. It does not replace any of those documents.*  
*Prepared by: Mohammed Fawwaz Sadath (1BY22EC054) | BMS Institute of Technology | MindMatrix Internship 2026*
