# GRAMA-YATRI — COMPLETE PRODUCT REQUIREMENTS DOCUMENT (PRD)

**Version:** 1.0  
**Date:** May 2026  
**Project:** Grama-Yatri – Community-Powered Rural Bus Tracker  
**Platform:** Android (Kotlin) — Android Studio Ladybug / Panda  
**Intern:** Mohammed Fawwaz Sadath | 1BY22EC054 | BMSIT&M  
**Company:** MindMatrix  

> **This file is the single source of truth for the entire app.**  
> Give this file to Codex at the start of every phase conversation.

---

## TABLE OF CONTENTS

1. [Executive Summary](#1-executive-summary)
2. [Problem Statement](#2-problem-statement)
3. [Vision & Objectives](#3-vision--objectives)
4. [Scope](#4-scope)
5. [User Personas](#5-user-personas)
6. [App Screens & Navigation](#6-app-screens--navigation)
7. [Detailed Screen Specifications](#7-detailed-screen-specifications)
8. [Functional Requirements](#8-functional-requirements)
9. [Non-Functional Requirements](#9-non-functional-requirements)
10. [Technical Architecture](#10-technical-architecture)
11. [Firebase Data Schema](#11-firebase-data-schema)
12. [GenAI Integration](#12-genai-integration)
13. [ETA Calculation Logic](#13-eta-calculation-logic)
14. [UI/UX Design System](#14-uiux-design-system)
15. [Folder & File Structure](#15-folder--file-structure)
16. [Success Criteria & KPIs](#16-success-criteria--kpis)
17. [Risk Analysis](#17-risk-analysis)

---

## 1. EXECUTIVE SUMMARY

Grama-Yatri is a **community-powered, real-time bus tracking Android app** for rural India. It targets daily-wage labourers and students who depend on unpredictable village bus services.

There is no GPS hardware on the bus. There is no government data feed. The passengers **are** the data source. When anyone sees or boards the bus at any stop, they tap **"Ping"** — and the app instantly calculates and broadcasts an ETA to every downstream stop for all users on that route.

A nightly **GenAI layer** (Gemini API via Cloud Function) refines the average travel time between stops, detects anomalies, and summarises cancellation alerts into clean push notifications.

The app is built for **low-end Android devices on 2G/3G**, using Firebase Realtime Database for sub-second sync, Firebase Cloud Messaging for alerts, and a vertical stepper timeline UI. It must work with under 500 KB of data per day.

---

## 2. PROBLEM STATEMENT

Village bus schedules in rural Karnataka are entirely informal. Consequences:

- Passengers miss the bus by 2 minutes and wait **3+ hours** for the next one.
- No mechanism to report cancellations or delays.
- Existing apps (KSRTC, Google Maps) do **not** cover last-mile rural routes.
- Direct economic harm — a missed bus = lost half-day wage for a daily worker.

**Core Question:** How can rural commuters create and share a real-time bus timetable using only their smartphones, with zero dependency on government or operator data?

---

## 3. VISION & OBJECTIVES

**Vision:** Make rural bus transit as predictable as city metros — powered entirely by the community that uses it.

| ID  | Objective |
|-----|-----------|
| O1  | Any passenger can Ping bus arrival in ≤ 2 taps |
| O2  | Live ETA cascades to all downstream stops within 3 seconds of a Ping |
| O3  | Cancellation/delay alerts broadcast via push notification with reporter name |
| O4  | App loads in < 2 s on a 3G connection |
| O5  | GenAI refines average travel-time estimates nightly |
| O6  | Zero mandatory sign-up friction (anonymous auth, display name optional) |

---

## 4. SCOPE

### In Scope
- Android app (minSdk 24, targetSdk 34, Kotlin)
- Firebase Realtime Database — live ping sync
- Firebase Authentication — Anonymous Auth (no email/password required)
- Firebase Cloud Messaging — push notifications for alerts
- Firebase Analytics — basic event tracking
- Gemini API (Cloud Function, nightly) — GenAI ETA refinement
- Pre-seeded route + stop data with average travel times
- Vertical stepper / timeline UI for route view
- Offline read-only mode (last known ETAs shown)

### Out of Scope
- iOS app
- Live GPS tracking of the bus (requires operator hardware)
- Payment / ticketing
- GTFS / government data integration
- Web dashboard (Phase 2)

---

## 5. USER PERSONAS

### Persona 1 — Ravi (Daily-Wage Labourer)
- Age: 32 | Device: Budget Android Go, 3G SIM
- Goal: Reach city factory by 8 AM without guessing
- Pain: Misses bus → loses half-day wage
- Tech comfort: Low — needs large buttons, minimal text
- Key need: One-tap Ping, loud ETA alert, works on slow data

### Persona 2 — Priya (College Student)
- Age: 19 | Device: Mid-range Android, 4G SIM
- Goal: Catch first bus to college without wasting time at stop
- Pain: Arrives late to lectures, exam stress
- Tech comfort: Medium — uses WhatsApp, YouTube
- Key need: Live route timeline, cancellation alerts, reporter attribution

### Persona 3 — Meena (Homemaker / Community Reporter)
- Age: 45 | Device: Budget Android, 2G/3G
- Goal: Help neighbours know when the bus is coming
- Pain: No way to share information beyond word-of-mouth
- Tech comfort: Very low — needs the simplest possible UI
- Key need: Single-tap Ping, large text, Kannada language support (Phase 2)

---

## 6. APP SCREENS & NAVIGATION

### Total Screens: 8

```
SplashScreen
    └── OnboardingScreen (first launch only)
            └── HomeScreen (Route List)
                    ├── LiveTrackingScreen (Route Timeline + ETA)
                    │       └── PingBottomSheet (modal)
                    ├── AlertsScreen (Cancellations & Delays)
                    └── ProfileScreen (User settings)
                            └── ReportAlertScreen (raise cancellation)
```

### Navigation Architecture
- **Single Activity** with Jetpack Navigation Component
- **Bottom Navigation Bar** with 3 tabs: Home | Alerts | Profile
- LiveTrackingScreen opened from HomeScreen via route card tap
- PingBottomSheet is a modal bottom sheet on LiveTrackingScreen
- ReportAlertScreen opened from ProfileScreen or Alerts FAB

---

## 7. DETAILED SCREEN SPECIFICATIONS

---

### Screen 1 — SplashScreen

**Purpose:** App cold-start, Firebase init check, route to Onboarding or Home.

**UI Elements:**
- App logo (bus icon + "Grama-Yatri" wordmark) centered
- Tagline: "Community Bus Tracker"
- Green gradient background

**Logic:**
- Check SharedPreferences for `isFirstLaunch`
- If true → navigate to OnboardingScreen
- If false → navigate to HomeScreen
- Duration: max 2 seconds

**File:** `SplashActivity.kt` or `SplashFragment.kt`

---

### Screen 2 — OnboardingScreen

**Purpose:** First-launch explanation, collect display name (optional), anonymous Firebase Auth sign-in.

**UI Elements:**
- 3-page ViewPager2 with illustrations:
  - Page 1: "See the bus? Ping it!" (illustration of bus at stop)
  - Page 2: "Everyone on the route gets the ETA instantly"
  - Page 3: "Get alerts for cancellations and delays"
- Page indicators (dots)
- "Skip" text button top-right
- "Next" / "Get Started" button bottom
- On last page: EditText for display name (hint: "Your name (optional, e.g. Ravi)")
- "Start Using App" button

**Logic:**
- Call `FirebaseAuth.signInAnonymously()`
- Save display name to SharedPreferences key `user_display_name`
- Save `isFirstLaunch = false`
- Navigate to HomeScreen

**File:** `OnboardingFragment.kt`, `OnboardingAdapter.kt`

---

### Screen 3 — HomeScreen (Route List)

**Purpose:** Show all available bus routes. User taps a route to open LiveTrackingScreen.

**UI Elements:**
- TopAppBar: "Grama-Yatri 🚌" title, search icon (Phase 2)
- RecyclerView of RouteCards, each showing:
  - Route name (e.g., "Doddaballapur → Bengaluru")
  - Number of stops
  - Last ping time (e.g., "Pinged 5 min ago by Ravi")
  - Live indicator dot (green = active ping < 30 min, grey = stale)
- Empty state: "No routes available. Pull to refresh."
- Pull-to-refresh (SwipeRefreshLayout)
- FAB: Not on this screen

**Data source:** Firebase Realtime Database `/routes` node (read once on load, then listen for last ping updates)

**File:** `HomeFragment.kt`, `RouteAdapter.kt`, `RouteCardItem.kt`

---

### Screen 4 — LiveTrackingScreen (Route Timeline + ETA)

**Purpose:** The core screen. Shows the vertical timeline of all stops on a route with live ETAs.

**UI Elements:**
- TopAppBar: Route name, back arrow
- **Vertical Stepper / Timeline RecyclerView** — each row is a stop:
  - Stop icon: ● (filled green = bus passed/current), ○ (hollow = upcoming), ✗ (skipped/unknown)
  - Vertical line connecting stops (green up to current stop, grey after)
  - Stop name (large text, 18sp+)
  - ETA chip: "~12 min" (amber) / "Bus here now" (green) / "Passed 8 min ago" (grey) / "Unknown" (grey dashed)
  - Reporter attribution below ETA: "📍 Ravi · 14 min ago"
  - Distance from user's saved stop (optional, Phase 2)
- **Sticky header** showing user's saved stop ETA prominently: "Bus reaches YOUR VILLAGE in ~15 min"
- **FAB — "PING BUS 🚌"** — large, prominent, always visible (72dp)
  - Tapping opens PingBottomSheet
- Confidence indicator: "Based on ping 14 min ago" or "No recent ping — ETA estimated"
- Pull-to-refresh

**Data source:** Firebase Realtime Database `/pings/{routeId}` — real-time listener

**File:** `LiveTrackingFragment.kt`, `StopTimelineAdapter.kt`, `StopItem.kt`

---

### Screen 5 — PingBottomSheet (Modal)

**Purpose:** User reports bus location. This is the core action of the app.

**UI Elements:**
- Bottom sheet (peeks at 50% screen height)
- Title: "Report Bus Location"
- Stop selector: Horizontal chip group or Spinner showing all stops on the route
  - Auto-selects the user's saved stop
- Two large action buttons (equal width, stacked):
  - 🟢 **"I AM ON THE BUS"** — reports bus is at this stop right now
  - 🟡 **"BUS JUST PASSED ME"** — reports bus departed from this stop
- Alert option: "🚫 Report Cancellation / Delay" (text button, opens ReportAlertScreen)
- "Cancel" text button
- User name shown: "Reporting as: Ravi (tap to change)"

**Logic on submit:**
1. Write ping to `/pings/{routeId}` with: stopId, type (ON_BUS / PASSED), reporterName, timestamp
2. Trigger local ETA recalculation for all downstream stops
3. Show snackbar: "Ping sent! Helping everyone on this route 🙌"
4. Dismiss bottom sheet

**File:** `PingBottomSheet.kt`

---

### Screen 6 — AlertsScreen

**Purpose:** Show all user-reported cancellations and delays for routes the user cares about.

**UI Elements:**
- TopAppBar: "Alerts & Cancellations"
- Filter chips: "All Routes" | "My Routes"
- RecyclerView of AlertCards:
  - Alert icon: 🚫 Cancelled / ⚠️ Delayed / ℹ️ Info
  - Route name
  - Alert message (e.g., "Morning bus cancelled — road blocked at Doddaballapur junction")
  - Reporter name + timestamp
  - Dismiss button (local only, hides from user's view)
- Empty state: "No alerts. All buses running normally ✅"
- FAB: "Report Alert +" (opens ReportAlertScreen)

**Data source:** Firebase Realtime Database `/alerts/{routeId}` — real-time listener

**File:** `AlertsFragment.kt`, `AlertAdapter.kt`, `AlertItem.kt`

---

### Screen 7 — ReportAlertScreen

**Purpose:** User raises a cancellation, delay, or info alert for a route.

**UI Elements:**
- TopAppBar: "Report an Alert", back arrow
- Route selector: Spinner / dropdown
- Alert type selector: Radio group
  - 🚫 Bus Cancelled Today
  - ⚠️ Bus Running Late
  - ℹ️ Other Information
- Reason (optional): Spinner with options: "Road blocked", "Bus breakdown", "Driver absent", "Festival/event", "Other"
- Note (optional): EditText multi-line, hint: "Add details (optional)"
- "Submit Alert" button (primary, full-width)

**Logic on submit:**
1. Write to `/alerts/{routeId}` with: type, reason, note, reporterName, timestamp
2. Trigger FCM push to all subscribers of this route (via Cloud Function)
3. Show success snackbar, navigate back

**File:** `ReportAlertFragment.kt`

---

### Screen 8 — ProfileScreen

**Purpose:** User settings, saved stop, display name, about.

**UI Elements:**
- TopAppBar: "Profile"
- User avatar: Bus icon with first letter of name (generated)
- Display name: editable inline (tap to edit)
- **Saved Stop section:**
  - Route selector → Stop selector
  - "Save My Stop" button
  - Saved stop shown as chip: "📍 Yelahanka · Route RT001"
- **Notification settings:**
  - Toggle: "Alert notifications" (on/off)
  - Toggle: "ETA reminders" (Phase 2)
- **About section:**
  - App version
  - "How it works" (opens OnboardingScreen again)
  - "Privacy Policy" (opens WebView or simple text)
- "Report Alert" shortcut button

**File:** `ProfileFragment.kt`

---

## 8. FUNCTIONAL REQUIREMENTS

| ID    | Requirement | Priority |
|-------|-------------|----------|
| FR-01 | User can view all available routes on HomeScreen | Must Have |
| FR-02 | User can tap a route and see the full stop timeline | Must Have |
| FR-03 | User can Ping the bus in ≤ 2 taps from LiveTrackingScreen | Must Have |
| FR-04 | Ping calculates ETA for all downstream stops immediately | Must Have |
| FR-05 | ETA updates propagate to all users on route within 3 seconds | Must Have |
| FR-06 | Ping shows reporter name + time (e.g., "Reported by Ravi · 5 min ago") | Must Have |
| FR-07 | ETA confidence degrades after 30 min without a new ping | Must Have |
| FR-08 | User can report a cancellation/delay alert | Must Have |
| FR-09 | FCM push notification sent to route subscribers on alert | Must Have |
| FR-10 | User can save their home stop for quick ETA on HomeScreen | Should Have |
| FR-11 | Anonymous Firebase Auth — no email/password required | Must Have |
| FR-12 | App shows last known data when offline (read-only cache) | Should Have |
| FR-13 | GenAI refines ATT values nightly via Cloud Function | Should Have |
| FR-14 | Onboarding flow on first launch (3-page explainer) | Should Have |
| FR-15 | User can set/change their display name | Should Have |
| FR-16 | Pull-to-refresh on HomeScreen and LiveTrackingScreen | Should Have |
| FR-17 | Rate-limit pings: max 1 ping per user per route per 5 minutes | Should Have |
| FR-18 | Alerts can be dismissed locally by the user | Could Have |
| FR-19 | Filter alerts by "My Routes" | Could Have |
| FR-20 | Kannada language localisation | Could Have (Phase 2) |

---

## 9. NON-FUNCTIONAL REQUIREMENTS

| Category | Requirement |
|----------|-------------|
| Performance | ETA broadcast latency ≤ 3 s; cold-start ≤ 2 s on 3G |
| Data Usage | Full session ≤ 500 KB/day; route timeline screen ≤ 15 KB |
| Availability | Firebase SLA ≥ 99.9%; app shows cached data when offline |
| Scalability | Architecture supports 50 routes, 10,000 concurrent users |
| Security | Firebase Rules: only authenticated users can write pings/alerts |
| Accessibility | Min tap target 48dp; body text ≥ 16sp; high-contrast |
| Compatibility | Android API 24 (Android 7.0) and above |
| Battery | Firebase listener uses keep-synced only on active screen |

---

## 10. TECHNICAL ARCHITECTURE

### Tech Stack

| Layer | Technology | Purpose |
|-------|------------|---------|
| Language | Kotlin | Android development |
| IDE | Android Studio Panda (Ladybug) | Build & review |
| UI | XML Layouts + Material Design 3 | Screens and components |
| Navigation | Jetpack Navigation Component | Single-activity nav |
| Architecture | MVVM + Repository Pattern | Clean separation of concerns |
| Real-time DB | Firebase Realtime Database | Sub-second ping sync |
| Auth | Firebase Anonymous Auth | No-friction sign-in |
| Push | Firebase Cloud Messaging (FCM) | Alert notifications |
| Analytics | Firebase Analytics | Event tracking |
| Storage | SharedPreferences | User prefs (name, saved stop) |
| GenAI | Gemini API (via Firebase Cloud Function) | Nightly ATT refinement |
| Dependency Injection | Hilt | ViewModel and repo injection |
| Async | Kotlin Coroutines + Flow | Firebase listeners as Flow |
| Image Loading | Glide (if needed) | Route/stop thumbnails |
| Build | Gradle (Kotlin DSL) | Project build |

### Architecture Pattern: MVVM + Repository

```
UI Layer (Fragments + Activity)
    ↕ observes LiveData / StateFlow
ViewModel Layer (per screen)
    ↕ calls
Repository Layer (FirebaseRepository)
    ↕ reads/writes
Firebase Realtime Database + FCM
```

### Dependency Injection with Hilt

```
@HiltAndroidApp — GramaYatriApplication
@AndroidEntryPoint — All Fragments and Activity
@HiltViewModel — All ViewModels
@Singleton — FirebaseRepository, FirebaseAuth instance
```

### Key Dependencies (build.gradle.kts — app)

```kotlin
// Firebase
implementation(platform("com.google.firebase:firebase-bom:33.x.x"))
implementation("com.google.firebase:firebase-database-ktx")
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-messaging-ktx")
implementation("com.google.firebase:firebase-analytics-ktx")

// Hilt
implementation("com.google.dagger:hilt-android:2.51")
kapt("com.google.dagger:hilt-compiler:2.51")

// Jetpack
implementation("androidx.navigation:navigation-fragment-ktx:2.7.x")
implementation("androidx.navigation:navigation-ui-ktx:2.7.x")
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.x")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.x")
implementation("androidx.recyclerview:recyclerview:1.3.x")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.x")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.x")

// Material Design 3
implementation("com.google.android.material:material:1.12.x")

// ViewPager2 (Onboarding)
implementation("androidx.viewpager2:viewpager2:1.1.x")
```

---

## 11. FIREBASE DATA SCHEMA

### Realtime Database JSON Structure

```json
{
  "routes": {
    "RT001": {
      "id": "RT001",
      "name": "Doddaballapur → Bengaluru",
      "totalStops": 6,
      "stops": {
        "S1": {
          "id": "S1",
          "name": "Doddaballapur Bus Stand",
          "order": 1,
          "attToNextMin": 0
        },
        "S2": {
          "id": "S2",
          "name": "Tubagere Cross",
          "order": 2,
          "attToNextMin": 12
        },
        "S3": {
          "id": "S3",
          "name": "Doddajala",
          "order": 3,
          "attToNextMin": 9
        },
        "S4": {
          "id": "S4",
          "name": "Yelahanka",
          "order": 4,
          "attToNextMin": 18
        },
        "S5": {
          "id": "S5",
          "name": "Hebbal",
          "order": 5,
          "attToNextMin": 8
        },
        "S6": {
          "id": "S6",
          "name": "Kempegowda Bus Station",
          "order": 6,
          "attToNextMin": 0
        }
      }
    },
    "RT002": {
      "id": "RT002",
      "name": "Chikkaballapur → Bengaluru",
      "totalStops": 5,
      "stops": { "...": "..." }
    }
  },

  "pings": {
    "RT001": {
      "-pingUniqueId1": {
        "stopId": "S2",
        "stopName": "Tubagere Cross",
        "stopOrder": 2,
        "type": "ON_BUS",
        "reporterName": "Ravi",
        "reporterUid": "firebase-anon-uid-abc",
        "timestamp": 1746000000000,
        "isActive": true
      }
    }
  },

  "alerts": {
    "RT001": {
      "-alertUniqueId1": {
        "type": "CANCELLED",
        "reason": "Road blocked",
        "note": "Road blocked at Doddaballapur junction",
        "reporterName": "Meena",
        "reporterUid": "firebase-anon-uid-xyz",
        "timestamp": 1746003600000,
        "routeId": "RT001",
        "routeName": "Doddaballapur → Bengaluru"
      }
    }
  },

  "userPrefs": {
    "{firebaseUid}": {
      "displayName": "Ravi",
      "savedRouteId": "RT001",
      "savedStopId": "S3",
      "savedStopName": "Doddajala",
      "alertsEnabled": true,
      "lastPingTimestamp": 1746000000000
    }
  }
}
```

### Firebase Security Rules

```json
{
  "rules": {
    "routes": {
      ".read": true,
      ".write": false
    },
    "pings": {
      "$routeId": {
        ".read": true,
        ".write": "auth != null"
      }
    },
    "alerts": {
      "$routeId": {
        ".read": true,
        ".write": "auth != null"
      }
    },
    "userPrefs": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

### Data Models (Kotlin)

```kotlin
data class Route(
    val id: String = "",
    val name: String = "",
    val totalStops: Int = 0,
    val stops: Map<String, Stop> = emptyMap()
)

data class Stop(
    val id: String = "",
    val name: String = "",
    val order: Int = 0,
    val attToNextMin: Int = 0   // Average Travel Time to NEXT stop in minutes
)

data class Ping(
    val stopId: String = "",
    val stopName: String = "",
    val stopOrder: Int = 0,
    val type: String = "",       // "ON_BUS" or "PASSED"
    val reporterName: String = "",
    val reporterUid: String = "",
    val timestamp: Long = 0L,
    val isActive: Boolean = true
)

data class BusAlert(
    val type: String = "",       // "CANCELLED", "DELAYED", "INFO"
    val reason: String = "",
    val note: String = "",
    val reporterName: String = "",
    val reporterUid: String = "",
    val timestamp: Long = 0L,
    val routeId: String = "",
    val routeName: String = ""
)

data class StopEta(
    val stop: Stop,
    val etaMinutes: Int?,        // null = unknown
    val status: EtaStatus,
    val reporterName: String?,
    val pingTimestamp: Long?
)

enum class EtaStatus {
    BUS_HERE, UPCOMING, PASSED, UNKNOWN, STALE
}
```

---

## 12. GENAI INTEGRATION

### Overview
A Firebase Cloud Function runs **every night at 2 AM IST** and calls the Gemini API to refine the `attToNextMin` values stored in `/routes/{routeId}/stops/{stopId}`.

### What the Cloud Function does

**Step 1 — Collect ping data from the past 7 days**
```
For each route, for each consecutive stop pair (S1→S2, S2→S3, …):
  - Find pairs of pings where ping at Sk was followed by ping at Sk+1
  - Compute actual travel time = ping2.timestamp - ping1.timestamp (in minutes)
  - Collect all such observations
```

**Step 2 — Call Gemini API**
Send a structured prompt:
```
Given these observed travel times (in minutes) between stops on route RT001:
- Tubagere → Doddajala: [11, 13, 10, 14, 9, 12] minutes (6 observations)
- Doddajala → Yelahanka: [17, 19, 18, 22, 15] minutes (5 observations)

Current stored ATT values:
- Tubagere → Doddajala: 12 min
- Doddajala → Yelahanka: 18 min

Respond ONLY with a JSON object updating the ATT values using a weighted average
(70% new observations, 30% existing value). Flag any stop pair with >50% variance
as LOW_CONFIDENCE. Format:
{
  "S2_to_S3": { "attMin": 11, "confidence": "HIGH" },
  "S3_to_S4": { "attMin": 19, "confidence": "LOW_CONFIDENCE" }
}
```

**Step 3 — Update Firebase**
Write refined ATT values back to `/routes/{routeId}/stops/{stopId}/attToNextMin`.

### Privacy
- No PII sent to Gemini API — only stop IDs, route IDs, and numeric timestamps.
- Cloud Function is written in Node.js (TypeScript).

---

## 13. ETA CALCULATION LOGIC

### Algorithm (runs client-side in Kotlin on every new ping)

```kotlin
fun calculateEtas(
    stops: List<Stop>,          // ordered list, S1..Sn
    ping: Ping                  // latest active ping
): List<StopEta> {
    val pingStopOrder = ping.stopOrder
    val pingTime = ping.timestamp
    val now = System.currentTimeMillis()
    val minutesSincePing = (now - pingTime) / 60_000

    return stops.map { stop ->
        when {
            stop.order < pingStopOrder -> {
                // Bus already passed this stop
                StopEta(stop, null, EtaStatus.PASSED, ping.reporterName, pingTime)
            }
            stop.order == pingStopOrder -> {
                // Bus is at / just left this stop
                StopEta(stop, 0, EtaStatus.BUS_HERE, ping.reporterName, pingTime)
            }
            else -> {
                // Calculate cumulative ATT from ping stop to this stop
                val cumulativeAtt = stops
                    .filter { it.order >= pingStopOrder && it.order < stop.order }
                    .sumOf { it.attToNextMin }
                val remainingMin = cumulativeAtt - minutesSincePing.toInt()
                val eta = remainingMin.coerceAtLeast(0)
                val status = if (minutesSincePing > 30) EtaStatus.STALE else EtaStatus.UPCOMING
                StopEta(stop, eta, status, ping.reporterName, pingTime)
            }
        }
    }
}
```

### ETA Display Rules

| EtaStatus | Display | Colour |
|-----------|---------|--------|
| BUS_HERE | "Bus is here now!" | Green |
| UPCOMING (< 5 min) | "~3 min" | Green |
| UPCOMING (5–15 min) | "~12 min" | Amber |
| UPCOMING (> 15 min) | "~25 min" | Red-amber |
| PASSED | "Passed ~8 min ago" | Grey |
| STALE | "~20 min (unconfirmed)" | Grey dashed |
| UNKNOWN | "No report yet" | Grey dashed |

---

## 14. UI/UX DESIGN SYSTEM

### Colour Palette

```xml
<!-- Primary -->
<color name="green_primary">#1B5E20</color>       <!-- Deep green — headers, buttons -->
<color name="green_medium">#4CAF50</color>         <!-- Active stop, FAB -->
<color name="green_light">#E8F5E9</color>          <!-- Card backgrounds -->

<!-- Status -->
<color name="eta_green">#2E7D32</color>            <!-- Bus here / < 5 min -->
<color name="eta_amber">#F57F17</color>            <!-- 5–15 min -->
<color name="eta_red_amber">#E65100</color>        <!-- > 15 min -->
<color name="eta_grey">#9E9E9E</color>             <!-- Passed / stale -->

<!-- Neutrals -->
<color name="text_primary">#212121</color>
<color name="text_secondary">#757575</color>
<color name="background">#FAFAFA</color>
<color name="surface">#FFFFFF</color>
<color name="divider">#E0E0E0</color>
```

### Typography

```xml
<!-- Stop name -->        sp=18, fontWeight=Bold
<!-- ETA value -->        sp=22, fontWeight=Bold
<!-- Reporter attribution --> sp=12, fontStyle=Italic, color=text_secondary
<!-- Route name -->       sp=16, fontWeight=Medium
<!-- Body text -->        sp=14, fontWeight=Normal
<!-- Caption -->          sp=12, fontWeight=Normal
```

### Component Guidelines

- **FAB Ping Button:** 72dp, `colorPrimary` green, icon: bus + wifi symbol
- **Stop Timeline Line:** 3dp width, green up to current stop, grey (#E0E0E0) for upcoming
- **Stop Node Icon:** 20dp filled circle for passed/current, 20dp outlined circle for upcoming
- **ETA Chip:** Rounded chip (8dp corners), coloured background per EtaStatus
- **Alert Card:** Left border 4dp coloured by alert type, white card, drop shadow 2dp
- **Minimum tap target:** 48dp for all interactive elements
- **Card elevation:** 2dp for route cards, 1dp for stop rows

### Low-Data Guidelines

- All icons: Vector Drawable (no PNGs)
- No remote images in MVP
- Firebase listener: only attach on active screen, detach in `onStop()`
- Keep-sync: disabled by default, enabled only for user's saved route

---

## 15. FOLDER & FILE STRUCTURE

```
app/
├── src/
│   └── main/
│       ├── java/com/gramayatri/app/
│       │   ├── GramaYatriApplication.kt        # @HiltAndroidApp
│       │   ├── MainActivity.kt                 # Single activity, NavHost
│       │   │
│       │   ├── data/
│       │   │   ├── model/
│       │   │   │   ├── Route.kt
│       │   │   │   ├── Stop.kt
│       │   │   │   ├── Ping.kt
│       │   │   │   ├── BusAlert.kt
│       │   │   │   ├── StopEta.kt
│       │   │   │   └── EtaStatus.kt
│       │   │   ├── repository/
│       │   │   │   ├── FirebaseRepository.kt   # All Firebase reads/writes
│       │   │   │   └── UserPrefsRepository.kt  # SharedPreferences wrapper
│       │   │   └── remote/
│       │   │       └── FirebaseDataSource.kt   # Raw Firebase calls
│       │   │
│       │   ├── di/
│       │   │   └── AppModule.kt                # Hilt module — Firebase, prefs
│       │   │
│       │   ├── ui/
│       │   │   ├── splash/
│       │   │   │   └── SplashFragment.kt
│       │   │   ├── onboarding/
│       │   │   │   ├── OnboardingFragment.kt
│       │   │   │   └── OnboardingAdapter.kt
│       │   │   ├── home/
│       │   │   │   ├── HomeFragment.kt
│       │   │   │   ├── HomeViewModel.kt
│       │   │   │   └── RouteAdapter.kt
│       │   │   ├── tracking/
│       │   │   │   ├── LiveTrackingFragment.kt
│       │   │   │   ├── LiveTrackingViewModel.kt
│       │   │   │   ├── StopTimelineAdapter.kt
│       │   │   │   └── PingBottomSheet.kt
│       │   │   ├── alerts/
│       │   │   │   ├── AlertsFragment.kt
│       │   │   │   ├── AlertsViewModel.kt
│       │   │   │   ├── AlertAdapter.kt
│       │   │   │   └── ReportAlertFragment.kt
│       │   │   └── profile/
│       │   │       ├── ProfileFragment.kt
│       │   │       └── ProfileViewModel.kt
│       │   │
│       │   └── util/
│       │       ├── EtaCalculator.kt            # ETA logic (pure function)
│       │       ├── TimeUtils.kt                # "5 min ago" formatting
│       │       ├── Constants.kt                # Firebase paths, keys
│       │       └── Extensions.kt              # Kotlin extension functions
│       │
│       ├── res/
│       │   ├── layout/
│       │   │   ├── activity_main.xml
│       │   │   ├── fragment_splash.xml
│       │   │   ├── fragment_onboarding.xml
│       │   │   ├── fragment_home.xml
│       │   │   ├── fragment_live_tracking.xml
│       │   │   ├── fragment_alerts.xml
│       │   │   ├── fragment_report_alert.xml
│       │   │   ├── fragment_profile.xml
│       │   │   ├── bottom_sheet_ping.xml
│       │   │   ├── item_route_card.xml
│       │   │   ├── item_stop_timeline.xml
│       │   │   └── item_alert_card.xml
│       │   ├── navigation/
│       │   │   └── nav_graph.xml
│       │   ├── menu/
│       │   │   └── bottom_nav_menu.xml
│       │   ├── drawable/
│       │   │   ├── ic_bus.xml
│       │   │   ├── ic_ping.xml
│       │   │   ├── ic_stop_filled.xml
│       │   │   ├── ic_stop_outline.xml
│       │   │   ├── ic_alert_cancel.xml
│       │   │   ├── ic_alert_delay.xml
│       │   │   └── bg_green_gradient.xml
│       │   ├── values/
│       │   │   ├── colors.xml
│       │   │   ├── strings.xml
│       │   │   ├── themes.xml
│       │   │   └── dimens.xml
│       │   └── raw/
│       │       └── routes_seed.json            # Seed data for offline/first load
│       │
│       ├── AndroidManifest.xml
│       └── google-services.json                # Firebase config (DO NOT COMMIT)
│
├── build.gradle.kts (app)
├── build.gradle.kts (project)
├── settings.gradle.kts
└── google-services.json
```

### Constants.kt

```kotlin
object Constants {
    // Firebase paths
    const val PATH_ROUTES = "routes"
    const val PATH_PINGS = "pings"
    const val PATH_ALERTS = "alerts"
    const val PATH_USER_PREFS = "userPrefs"

    // SharedPreferences
    const val PREFS_NAME = "gramayatri_prefs"
    const val KEY_FIRST_LAUNCH = "isFirstLaunch"
    const val KEY_DISPLAY_NAME = "user_display_name"
    const val KEY_SAVED_ROUTE_ID = "saved_route_id"
    const val KEY_SAVED_STOP_ID = "saved_stop_id"
    const val KEY_SAVED_STOP_NAME = "saved_stop_name"

    // Ping types
    const val PING_TYPE_ON_BUS = "ON_BUS"
    const val PING_TYPE_PASSED = "PASSED"

    // Alert types
    const val ALERT_CANCELLED = "CANCELLED"
    const val ALERT_DELAYED = "DELAYED"
    const val ALERT_INFO = "INFO"

    // ETA thresholds
    const val STALE_PING_THRESHOLD_MIN = 30
    const val PING_RATE_LIMIT_MIN = 5
}
```

---

## 16. SUCCESS CRITERIA & KPIs

| KPI | Target | How to Measure |
|-----|--------|----------------|
| ETA broadcast latency | ≤ 3 seconds | Firebase listener timestamp delta |
| Ping → ETA accuracy | ± 5 minutes | Actual arrival vs predicted ETA |
| Reporter attribution | 100% of pings show reporter name | UI audit |
| App cold-start (3G) | ≤ 2 seconds | Android Profiler |
| Data per session | ≤ 500 KB/day | Network profiler |
| Cancellation alert delivery | ≤ 10 seconds via FCM | FCM delivery report |
| Crash-free sessions | ≥ 99% | Firebase Crashlytics |

---

## 17. RISK ANALYSIS

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Poor 2G/3G connectivity | High | High | Offline cache with last known ETAs |
| Low initial adoption (no pings) | High | High | Seed with NGO/SHG community champions; show "No recent report" clearly |
| Ping spam / fake reports | Medium | Medium | Rate-limit: 1 ping/5 min per UID per route |
| Firebase cost at scale | Low | Medium | Firebase Spark (free) sufficient for MVP; monitor |
| Inaccurate ATT seed data | Medium | Medium | GenAI nightly refinement corrects over time |
| Anonymous auth abuse | Low | Low | Firebase rules + rate limiting |
| App crash on old Android (API 24) | Medium | High | Test on API 24 emulator in every phase |

---

*End of GRAMAYATRI_PRD_COMPLETE.md*
*This is the single source of truth. Do not modify without updating the version number.*
