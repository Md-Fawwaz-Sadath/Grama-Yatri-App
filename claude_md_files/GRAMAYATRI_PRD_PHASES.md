# GRAMA-YATRI — PHASED BUILD PROMPTS

**Version:** 1.0  
**Date:** May 2026  
**Project:** Grama-Yatri – Community-Powered Rural Bus Tracker  
**Platform:** Android (Kotlin) — Android Studio Ladybug / Panda  
**Intern:** Mohammed Fawwaz Sadath | 1BY22EC054 | BMSIT&M  
**Company:** MindMatrix  

> **How to use this file:**  
> Each phase below is a complete, copy-paste prompt for a **new** Codex / AI conversation.  
> Always attach `GRAMAYATRI_PRD_COMPLETE.md` alongside this file at the start of every phase.  
> Assume all previous phases are fully completed before starting the next one.

---

## PHASE MAP (QUICK REFERENCE)

| Phase | Name | Key Output |
|-------|------|------------|
| 1 | Foundation Setup | Android project, Hilt, Firebase wired, nav skeleton |
| 2 | Data Layer & Models | Kotlin models, FirebaseRepository, seed data |
| 3 | Home Screen | Route list, RouteAdapter, Firebase read |
| 4 | Live Tracking Screen | Stop timeline, ETA engine, real-time listener |
| 5 | Ping Flow | PingBottomSheet, write ping, ETA broadcast |
| 6 | Alerts System | AlertsScreen, ReportAlertScreen, FCM |
| 7 | Onboarding & Profile | Splash, Onboarding, ProfileScreen, saved stop |
| 8 | Offline & Polish | Offline cache, rate limiting, low-data hardening |
| 9 | GenAI (Gemini) | Cloud Function (Node.js/TS), nightly ATT refinement |
| 10 | Final QA & Launch | Testing, Crashlytics, Analytics, APK release |

---

## 1. DETAILED PHASE PROMPTS

*Each phase below is designed to be a complete, copy-paste prompt for a new AI conversation (e.g. in Codex or Cursor).  
Assume all previous phases are already completed before starting the next one.*

---

### PHASE 1: FOUNDATION SETUP

````markdown
# PHASE 1: Grama-Yatri Foundation Setup

## Context
I'm building **Grama-Yatri** — a community-powered, real-time rural bus tracker for Android.  
Reference document attached: `GRAMAYATRI_PRD_COMPLETE.md`

This phase is ONLY about **foundational setup**:
- Android project creation (Kotlin, minSdk 24, targetSdk 34)
- Hilt dependency injection
- Firebase connected (Auth, Realtime DB, FCM, Analytics)
- Jetpack Navigation skeleton (Single Activity + 3-tab bottom nav)
- Basic folder structure
- No UI screens yet — just the scaffolding

---

## Technology Stack

- Language: Kotlin
- IDE: Android Studio Ladybug / Panda
- UI: XML Layouts + Material Design 3
- Architecture: MVVM + Repository Pattern
- Navigation: Jetpack Navigation Component (Single Activity)
- DI: Hilt
- Async: Kotlin Coroutines + Flow
- Firebase: Realtime Database, Anonymous Auth, FCM, Analytics
- Build: Gradle (Kotlin DSL)

---

## Tasks for This Phase

### 1. Android Project Initialization

Create a new Android project:
- Application name: `Grama-Yatri`
- Package name: `com.gramayatri.app`
- Language: Kotlin
- Min SDK: 24 (Android 7.0)
- Target SDK: 34
- Build system: Gradle (Kotlin DSL)

Enable:
- View Binding in `build.gradle.kts` (app):
```kotlin
buildFeatures {
    viewBinding = true
}
```

---

### 2. Add Core Dependencies

In `build.gradle.kts` (app), add:

```kotlin
// Firebase BOM
implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
implementation("com.google.firebase:firebase-database-ktx")
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-messaging-ktx")
implementation("com.google.firebase:firebase-analytics-ktx")

// Hilt
implementation("com.google.dagger:hilt-android:2.51")
kapt("com.google.dagger:hilt-compiler:2.51")

// Jetpack Navigation
implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

// Lifecycle / ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0")

// RecyclerView
implementation("androidx.recyclerview:recyclerview:1.3.2")

// Material Design 3
implementation("com.google.android.material:material:1.12.0")

// ViewPager2
implementation("androidx.viewpager2:viewpager2:1.1.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")
```

In `build.gradle.kts` (project), add the Hilt plugin:
```kotlin
id("com.google.dagger.hilt.android") version "2.51" apply false
```

Apply in app module:
```kotlin
id("com.google.dagger.hilt.android")
id("kotlin-kapt")
```

---

### 3. Firebase Setup

1. Create a Firebase project named `gramayatri` in the Firebase console.
2. Add an Android app with package name `com.gramayatri.app`.
3. Download `google-services.json` and place it in `app/`.
4. Enable in Firebase console:
   - **Anonymous Authentication** (Sign-in method)
   - **Realtime Database** (Start in test mode for now)
   - **Cloud Messaging** (FCM — default)
   - **Analytics** (default)

Apply the Google Services plugin in `build.gradle.kts` (app):
```kotlin
id("com.google.gms.google-services")
```

Add to project-level build:
```kotlin
id("com.google.gms.google-services") version "4.4.1" apply false
```

---

### 4. Application Class + Hilt

Create `GramaYatriApplication.kt`:
```kotlin
@HiltAndroidApp
class GramaYatriApplication : Application()
```

Register in `AndroidManifest.xml`:
```xml
android:name=".GramaYatriApplication"
```

---

### 5. Hilt AppModule

Create `di/AppModule.kt`:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase =
        FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()
}
```

---

### 6. Single Activity + NavHost

Create `MainActivity.kt` annotated with `@AndroidEntryPoint`.

In `activity_main.xml`:
- `FragmentContainerView` as `NavHost`
- `BottomNavigationView` with 3 tabs: Home | Alerts | Profile

Create `res/navigation/nav_graph.xml` with 5 placeholder destinations:
- `splashFragment` (start destination)
- `onboardingFragment`
- `homeFragment`
- `alertsFragment`
- `profileFragment`

Create `res/menu/bottom_nav_menu.xml` with:
- `home` → ic_home icon
- `alerts` → ic_notifications icon
- `profile` → ic_person icon

Wire BottomNavigationView to NavController in MainActivity using `NavigationUI.setupWithNavController`.

---

### 7. Folder Structure

Create this package structure under `com.gramayatri.app`:

```
com.gramayatri.app/
├── GramaYatriApplication.kt
├── MainActivity.kt
├── data/
│   ├── model/          (empty, Phase 2)
│   └── repository/     (empty, Phase 2)
├── di/
│   └── AppModule.kt
├── ui/
│   ├── splash/
│   ├── onboarding/
│   ├── home/
│   ├── tracking/
│   ├── alerts/
│   └── profile/
└── util/
    └── Constants.kt    (add below)
```

Create `util/Constants.kt`:
```kotlin
object Constants {
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

    const val PING_TYPE_ON_BUS = "ON_BUS"
    const val PING_TYPE_PASSED = "PASSED"

    const val ALERT_CANCELLED = "CANCELLED"
    const val ALERT_DELAYED = "DELAYED"
    const val ALERT_INFO = "INFO"

    const val STALE_PING_THRESHOLD_MIN = 30
    const val PING_RATE_LIMIT_MIN = 5
}
```

---

### 8. Placeholder Fragments

For each destination in nav_graph, create a minimal fragment that just shows a TextView with its name:
- `SplashFragment` — shows "Splash" for now
- `OnboardingFragment` — shows "Onboarding"
- `HomeFragment` — shows "Home"
- `AlertsFragment` — shows "Alerts"
- `ProfileFragment` — shows "Profile"

Each annotated `@AndroidEntryPoint`.

---

### 9. Permissions in AndroidManifest

Add:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

---

## Deliverables

1. ✅ Android project created, builds without errors.
2. ✅ Firebase connected — `google-services.json` in place, Anonymous Auth enabled.
3. ✅ Hilt wired — `GramaYatriApplication`, `AppModule` providing Firebase instances.
4. ✅ Single Activity with BottomNavigationView + NavController.
5. ✅ `nav_graph.xml` with 5 placeholder destinations.
6. ✅ Folder structure created.
7. ✅ `Constants.kt` in place.
8. ✅ Placeholder fragments exist for all nav destinations.

---

## Success Criteria

- `./gradlew assembleDebug` passes with 0 errors.
- App launches on an emulator (API 24+) and shows the bottom navigation.
- Navigating tabs changes the displayed placeholder text.
- No Hilt or Firebase initialization crash in Logcat.
````

---

### PHASE 2: DATA LAYER & MODELS

````markdown
# PHASE 2: Data Layer & Models

## Context
Phase 1 set up the Android project, Hilt, Firebase, and navigation skeleton.

Reference document attached: `GRAMAYATRI_PRD_COMPLETE.md`

This phase implements:
- All Kotlin data model classes
- `FirebaseRepository` — all read/write operations
- `UserPrefsRepository` — SharedPreferences wrapper
- `FirebaseDataSource` — raw Firebase calls
- Seed data JSON (`routes_seed.json`) for 2 routes
- Firebase Realtime Database security rules
- Hilt module wired for repositories

No UI screens yet.

---

## Prerequisites

- Phase 1 fully completed.
- Firebase Realtime Database enabled.
- Hilt AppModule providing `FirebaseDatabase` and `FirebaseAuth` instances.

---

## Tasks for This Phase

### 1. Kotlin Data Models

Create all models in `data/model/`:

**Route.kt**
```kotlin
data class Route(
    val id: String = "",
    val name: String = "",
    val totalStops: Int = 0,
    val stops: Map<String, Stop> = emptyMap()
)
```

**Stop.kt**
```kotlin
data class Stop(
    val id: String = "",
    val name: String = "",
    val order: Int = 0,
    val attToNextMin: Int = 0
)
```

**Ping.kt**
```kotlin
data class Ping(
    val stopId: String = "",
    val stopName: String = "",
    val stopOrder: Int = 0,
    val type: String = "",
    val reporterName: String = "",
    val reporterUid: String = "",
    val timestamp: Long = 0L,
    val isActive: Boolean = true
)
```

**BusAlert.kt**
```kotlin
data class BusAlert(
    val type: String = "",
    val reason: String = "",
    val note: String = "",
    val reporterName: String = "",
    val reporterUid: String = "",
    val timestamp: Long = 0L,
    val routeId: String = "",
    val routeName: String = ""
)
```

**EtaStatus.kt**
```kotlin
enum class EtaStatus {
    BUS_HERE, UPCOMING, PASSED, UNKNOWN, STALE
}
```

**StopEta.kt**
```kotlin
data class StopEta(
    val stop: Stop,
    val etaMinutes: Int?,
    val status: EtaStatus,
    val reporterName: String?,
    val pingTimestamp: Long?
)
```

---

### 2. Seed Data JSON

Create `res/raw/routes_seed.json` with the following data for 2 routes:

Route RT001: Doddaballapur → Bengaluru
- S1: Doddaballapur (attToNextMin: 15)
- S2: Tubagere Cross (attToNextMin: 12)
- S3: Doddajala (attToNextMin: 18)
- S4: Yelahanka (attToNextMin: 10)
- S5: Hebbal (attToNextMin: 8)
- S6: Kempegowda Bus Station (attToNextMin: 0)

Route RT002: Chikkaballapur → Bengaluru
- S1: Chikkaballapur (attToNextMin: 20)
- S2: Nandi Cross (attToNextMin: 18)
- S3: Devanahalli (attToNextMin: 22)
- S4: Kempapura (attToNextMin: 12)
- S5: Kempegowda Bus Station (attToNextMin: 0)

Format exactly as shown in Section 11 of the PRD.

---

### 3. FirebaseDataSource

Create `data/remote/FirebaseDataSource.kt`:

Inject `FirebaseDatabase`. Provide these suspend functions using `suspendCoroutine` or `callbackFlow`:

- `getRoutes(): Flow<List<Route>>` — listens to `/routes` node
- `getLatestPing(routeId: String): Flow<Ping?>` — listens to `/pings/{routeId}`, returns most recent `isActive = true` ping
- `writePing(routeId: String, ping: Ping): Result<Unit>`
- `getAlerts(routeId: String): Flow<List<BusAlert>>` — listens to `/alerts/{routeId}`
- `writeAlert(routeId: String, alert: BusAlert): Result<Unit>`

All listeners must be implemented as `callbackFlow` so they can be collected as Kotlin Flows.

---

### 4. FirebaseRepository

Create `data/repository/FirebaseRepository.kt`:

Inject `FirebaseDataSource` and `FirebaseAuth`.

Expose:
- `fun getRoutes(): Flow<List<Route>>`
- `fun getLatestPing(routeId: String): Flow<Ping?>`
- `suspend fun submitPing(routeId: String, ping: Ping): Result<Unit>`
- `fun getAlerts(routeId: String): Flow<List<BusAlert>>`
- `suspend fun submitAlert(routeId: String, alert: BusAlert): Result<Unit>`
- `fun getCurrentUid(): String?` — returns `FirebaseAuth.currentUser?.uid`
- `suspend fun signInAnonymously(): Result<String>` — returns UID

Annotate `@Singleton` and inject via Hilt.

---

### 5. UserPrefsRepository

Create `data/repository/UserPrefsRepository.kt`:

Inject application `Context`. Wrap SharedPreferences for:
- `getDisplayName(): String` (default: "Anonymous")
- `setDisplayName(name: String)`
- `isFirstLaunch(): Boolean`
- `setFirstLaunchDone()`
- `getSavedRouteId(): String?`
- `getSavedStopId(): String?`
- `getSavedStopName(): String?`
- `saveStop(routeId: String, stopId: String, stopName: String)`
- `getLastPingTimestamp(): Long`
- `setLastPingTimestamp(ts: Long)`

Use `Constants.PREFS_NAME`, `Constants.KEY_*` for all keys.

---

### 6. Update Hilt AppModule

In `di/AppModule.kt`, add providers for:
- `UserPrefsRepository` (inject `@ApplicationContext`)
- `FirebaseDataSource`
- `FirebaseRepository`

All as `@Singleton`.

---

### 7. Firebase Security Rules

Set these rules in the Firebase console (Realtime Database → Rules):

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

---

### 8. Seed Firebase with Route Data

Write a one-time seeder function (can be a simple suspended function called from a debug button for now):

```kotlin
suspend fun seedRoutes(db: FirebaseDatabase)
```

- Reads `routes_seed.json` from `res/raw`
- Parses it into `Map<String, Route>`
- Writes to Firebase `/routes` node
- Should be idempotent (only seed if `/routes` is empty)

---

## Deliverables

1. ✅ All 6 data model classes created.
2. ✅ `routes_seed.json` with 2 full routes.
3. ✅ `FirebaseDataSource` with all 5 operations as Flow / suspend.
4. ✅ `FirebaseRepository` wrapping datasource + auth.
5. ✅ `UserPrefsRepository` wrapping SharedPreferences.
6. ✅ Hilt module updated to provide all repositories.
7. ✅ Firebase security rules applied.
8. ✅ Seed function can write routes to Firebase.

---

## Success Criteria

- `./gradlew assembleDebug` passes with 0 errors.
- A test call to `repository.getRoutes()` in a ViewModel collects without crash.
- `signInAnonymously()` succeeds and returns a UID visible in Logcat.
- Seed data is visible in the Firebase console under `/routes`.
````

---

### PHASE 3: HOME SCREEN (ROUTE LIST)

````markdown
# PHASE 3: Home Screen — Route List

## Context
Phases 1–2 set up the project foundation, all data models, and Firebase repository layer.

Reference document attached: `GRAMAYATRI_PRD_COMPLETE.md`

This phase implements:
- `HomeFragment` — RecyclerView showing all routes
- `HomeViewModel` — observes routes from FirebaseRepository
- `RouteAdapter` + `RouteCardItem` — route card UI
- Live indicator dot (green = active ping < 30 min, grey = stale)
- Pull-to-refresh

No ETA calculation yet — that is Phase 4.

---

## Prerequisites

- Phases 1–2 fully completed.
- `FirebaseRepository` injected and working.
- `nav_graph.xml` has `homeFragment` destination.
- Route seed data in Firebase.

---

## Tasks for This Phase

### 1. HomeViewModel

Create `ui/home/HomeViewModel.kt`:

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FirebaseRepository,
    private val prefs: UserPrefsRepository
) : ViewModel() {

    private val _routes = MutableStateFlow<List<Route>>(emptyList())
    val routes: StateFlow<List<Route>> = _routes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadRoutes()
    }

    fun loadRoutes() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getRoutes()
                .catch { /* handle error */ }
                .collect { _routes.value = it }
            _isLoading.value = false
        }
    }
}
```

---

### 2. RouteCardItem

Create `data/model/RouteCardItem.kt`:
```kotlin
data class RouteCardItem(
    val route: Route,
    val lastPingReporterName: String?,
    val lastPingMinutesAgo: Long?,
    val isActive: Boolean   // true if last ping < 30 min ago
)
```

---

### 3. item_route_card.xml Layout

Create `res/layout/item_route_card.xml`:

- MaterialCardView with 8dp corner radius, 2dp elevation, `green_light` background.
- Route name: `TextView` 16sp Medium weight.
- Stop count: `TextView` 12sp secondary text color — "X stops".
- Last ping line: `TextView` 12sp italic — "📍 Pinged X min ago by Ravi" or "No recent report".
- Live dot: `View` 10dp circle, green (`green_medium`) if active, grey if stale.
- Minimum tap target: full card is clickable, 72dp min height.
- Padding: 16dp all sides.

---

### 4. RouteAdapter

Create `ui/home/RouteAdapter.kt`:

- `ListAdapter<RouteCardItem, RouteViewHolder>` with `DiffUtil.ItemCallback`.
- Binds all fields from `RouteCardItem`.
- `onItemClick: (Route) -> Unit` lambda for tap handling.
- Show "📍 Pinged X min ago by [name]" using `TimeUtils.minutesAgo(timestamp)`.
- Show green dot if `isActive = true`, grey dot otherwise.

---

### 5. fragment_home.xml Layout

Create `res/layout/fragment_home.xml`:

- `SwipeRefreshLayout` wrapping:
  - `RecyclerView` (`LinearLayoutManager`, vertical)
- Empty state `TextView` centered: "No routes available. Pull to refresh." (hidden when list is non-empty)
- `TopAppBar` or standard toolbar: title "Grama-Yatri 🚌", `green_primary` background.

---

### 6. HomeFragment

Create `ui/home/HomeFragment.kt`:

- `@AndroidEntryPoint`
- Inject `HomeViewModel` via `by viewModels()`
- Observe `routes` StateFlow → map to `RouteCardItem` list → submit to adapter
  - Determine `isActive`: check if any ping in the last 30 min exists (for Phase 3, use a simple heuristic — mark active if `route.lastPingTime` exists and is < 30 min old; real ping listener is Phase 4)
- SwipeRefreshLayout triggers `viewModel.loadRoutes()`
- On card tap: navigate to `liveTrackingFragment` passing `routeId` as argument
- Show/hide empty state based on list size
- Detach Firebase listener in `onStop()`

---

### 7. TimeUtils

Create `util/TimeUtils.kt`:

```kotlin
object TimeUtils {
    fun minutesAgo(timestamp: Long): Long {
        return (System.currentTimeMillis() - timestamp) / 60_000
    }

    fun formatMinutesAgo(timestamp: Long?): String {
        if (timestamp == null) return "No recent report"
        val mins = minutesAgo(timestamp)
        return when {
            mins < 1 -> "Just now"
            mins < 60 -> "$mins min ago"
            else -> "${mins / 60}h ago"
        }
    }
}
```

---

### 8. Add liveTrackingFragment to NavGraph

In `nav_graph.xml`:
- Add `liveTrackingFragment` destination (placeholder fragment for now)
- Add argument: `routeId` (String, required)
- Add action from `homeFragment` → `liveTrackingFragment`

---

### 9. Color & Theme Resources

Ensure `res/values/colors.xml` has all colors from PRD Section 14:
```xml
<color name="green_primary">#1B5E20</color>
<color name="green_medium">#4CAF50</color>
<color name="green_light">#E8F5E9</color>
<color name="eta_green">#2E7D32</color>
<color name="eta_amber">#F57F17</color>
<color name="eta_red_amber">#E65100</color>
<color name="eta_grey">#9E9E9E</color>
<color name="text_primary">#212121</color>
<color name="text_secondary">#757575</color>
<color name="background">#FAFAFA</color>
<color name="surface">#FFFFFF</color>
<color name="divider">#E0E0E0</color>
```

---

## Deliverables

1. ✅ `HomeViewModel` collecting routes from Firebase as StateFlow.
2. ✅ `RouteAdapter` with DiffUtil, live dot indicator, ping attribution.
3. ✅ `item_route_card.xml` styled per PRD design system.
4. ✅ `fragment_home.xml` with SwipeRefreshLayout + RecyclerView + empty state.
5. ✅ `HomeFragment` wired to ViewModel, navigates to LiveTracking on tap.
6. ✅ `TimeUtils` for relative time formatting.
7. ✅ Color resources in `colors.xml`.
8. ✅ `liveTrackingFragment` placeholder added to NavGraph.

---

## Success Criteria

- Home screen shows 2 route cards loaded from Firebase.
- Pull-to-refresh re-fetches routes without crash.
- Empty state shows when no routes exist.
- Tapping a route card navigates to the LiveTracking placeholder screen passing the correct `routeId`.
- No memory leaks — Firebase listener detached in `onStop()`.
````

---

### PHASE 4: LIVE TRACKING SCREEN

````markdown
# PHASE 4: Live Tracking Screen — Stop Timeline + ETA Engine

## Context
Phases 1–3 implemented foundation, data layer, and the home screen.

Reference document attached: `GRAMAYATRI_PRD_COMPLETE.md`

This phase implements the **core screen** of the app:
- Vertical stop timeline RecyclerView
- `EtaCalculator` — the ETA algorithm from PRD Section 13
- `LiveTrackingViewModel` — real-time Firebase ping listener
- `StopTimelineAdapter` — renders each stop row with ETA chip
- Sticky "Your Stop" ETA header
- Confidence indicator ("Based on ping X min ago")
- FAB "PING BUS 🚌" button (bottom sheet is Phase 5)

No ping submission yet — FAB shows a placeholder toast in this phase.

---

## Prerequisites

- Phases 1–3 fully completed.
- `FirebaseRepository.getLatestPing(routeId)` returning a Flow.
- `nav_graph.xml` has `liveTrackingFragment` with `routeId` argument.
- All color resources defined.

---

## Tasks for This Phase

### 1. EtaCalculator

Create `util/EtaCalculator.kt` implementing the exact algorithm from PRD Section 13:

```kotlin
object EtaCalculator {
    fun calculateEtas(
        stops: List<Stop>,
        ping: Ping
    ): List<StopEta> {
        val pingStopOrder = ping.stopOrder
        val pingTime = ping.timestamp
        val now = System.currentTimeMillis()
        val minutesSincePing = (now - pingTime) / 60_000

        return stops.map { stop ->
            when {
                stop.order < pingStopOrder -> {
                    StopEta(stop, null, EtaStatus.PASSED, ping.reporterName, pingTime)
                }
                stop.order == pingStopOrder -> {
                    StopEta(stop, 0, EtaStatus.BUS_HERE, ping.reporterName, pingTime)
                }
                else -> {
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

    fun noDataEtas(stops: List<Stop>): List<StopEta> =
        stops.map { StopEta(it, null, EtaStatus.UNKNOWN, null, null) }
}
```

---

### 2. LiveTrackingViewModel

Create `ui/tracking/LiveTrackingViewModel.kt`:

```kotlin
@HiltViewModel
class LiveTrackingViewModel @Inject constructor(
    private val repository: FirebaseRepository,
    private val prefs: UserPrefsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val routeId: String = checkNotNull(savedStateHandle["routeId"])

    private val _route = MutableStateFlow<Route?>(null)
    val route: StateFlow<Route?> = _route

    private val _stopEtas = MutableStateFlow<List<StopEta>>(emptyList())
    val stopEtas: StateFlow<List<StopEta>> = _stopEtas

    private val _confidenceText = MutableStateFlow("No recent report — ETA estimated")
    val confidenceText: StateFlow<String> = _confidenceText

    val savedStopId: String? get() = prefs.getSavedStopId()

    init {
        loadRoute()
        listenToPings()
    }

    private fun loadRoute() { /* collect route from repository */ }

    private fun listenToPings() {
        viewModelScope.launch {
            repository.getLatestPing(routeId).collect { ping ->
                val stops = _route.value?.stops?.values?.sortedBy { it.order } ?: return@collect
                if (ping != null) {
                    _stopEtas.value = EtaCalculator.calculateEtas(stops, ping)
                    val mins = TimeUtils.minutesAgo(ping.timestamp)
                    _confidenceText.value = "Based on ping $mins min ago by ${ping.reporterName}"
                } else {
                    _stopEtas.value = EtaCalculator.noDataEtas(stops)
                    _confidenceText.value = "No recent report — ETA estimated"
                }
            }
        }
    }
}
```

---

### 3. item_stop_timeline.xml Layout

Create `res/layout/item_stop_timeline.xml`:

Each row in the vertical stepper:
- **Left column** (40dp wide): vertical line (3dp, green for passed/current, grey for upcoming) + stop node circle icon (20dp, filled for passed/current, outlined for upcoming)
- **Right column**: 
  - Stop name: 18sp Bold
  - ETA chip: rounded chip (8dp corners), background color per EtaStatus, text per ETA display rules from PRD Section 13
  - Reporter attribution: 12sp Italic secondary — "📍 Ravi · 5 min ago" (hidden if null)
- Minimum row height: 72dp
- All vector drawables (no PNGs)

ETA chip colour mapping:
- `BUS_HERE` → `eta_green` background
- `UPCOMING` < 5 min → `eta_green`
- `UPCOMING` 5–15 min → `eta_amber`
- `UPCOMING` > 15 min → `eta_red_amber`
- `PASSED` → `eta_grey`
- `STALE` → `eta_grey`, dashed border
- `UNKNOWN` → `eta_grey`, dashed border

---

### 4. StopTimelineAdapter

Create `ui/tracking/StopTimelineAdapter.kt`:

- `ListAdapter<StopEta, StopViewHolder>` with `DiffUtil`
- Bind stop name, ETA chip text + color, reporter attribution
- Draw timeline line differently for PASSED / current / UPCOMING stops
- For the user's saved stop: add a subtle highlight background to that row

---

### 5. fragment_live_tracking.xml Layout

Create `res/layout/fragment_live_tracking.xml`:

- `TopAppBar`: route name + back arrow
- **Sticky header card** (above RecyclerView): 
  - "Bus reaches YOUR STOP in ~15 min" (or "No data" if UNKNOWN)
  - `green_light` background, 4dp corner radius
  - Hide if no saved stop set
- `RecyclerView` with `LinearLayoutManager` (remaining screen)
- Confidence `TextView` at bottom of list (italic, secondary color)
- **FAB** (72dp, `green_medium`, bottom-right):
  - Icon: bus + wifi symbol (use `ic_ping.xml` vector)
  - Label: "PING BUS 🚌"
  - `contentDescription`: "Ping the bus"
  - Phase 4: clicking shows a `Snackbar`: "Ping feature coming soon!"

---

### 6. LiveTrackingFragment

Create `ui/tracking/LiveTrackingFragment.kt`:

- `@AndroidEntryPoint`
- Inject `LiveTrackingViewModel` via `by viewModels()`
- Observe `stopEtas` → submit to `StopTimelineAdapter`
- Observe `route` → set toolbar title
- Observe `confidenceText` → update confidence TextView
- Update sticky header with saved stop's ETA from the list
- Detach Firebase listener in `onStop()`
- Pull-to-refresh: re-trigger ViewModel load

---

### 7. Required Vector Drawables

Create these in `res/drawable/`:
- `ic_stop_filled.xml` — 20dp filled green circle (for passed/current stop)
- `ic_stop_outline.xml` — 20dp outlined circle (for upcoming stop)
- `ic_ping.xml` — bus + wifi symbol (for FAB)
- `ic_bus.xml` — simple bus icon (for toolbar/splash)

All must be vector drawables (no PNGs).

---

## Deliverables

1. ✅ `EtaCalculator` implementing the exact PRD algorithm.
2. ✅ `LiveTrackingViewModel` with real-time ping listener.
3. ✅ `item_stop_timeline.xml` — stop row with ETA chip + reporter attribution.
4. ✅ `StopTimelineAdapter` with DiffUtil.
5. ✅ `fragment_live_tracking.xml` with sticky header + FAB.
6. ✅ `LiveTrackingFragment` wired to ViewModel.
7. ✅ Required vector drawables created.

---

## Success Criteria

- Opening a route shows all its stops in vertical timeline order.
- When a test ping is manually inserted into Firebase, ETAs recalculate and update within 3 seconds.
- Passed stops show grey, current stop shows green "Bus is here now!", upcoming shows amber/green chips.
- Sticky header shows correct ETA for saved stop (if set).
- Confidence text updates with reporter name and time.
- FAB shows "Ping feature coming soon!" Snackbar on tap.
````

---

### PHASE 5: PING FLOW

````markdown
# PHASE 5: Ping Flow — PingBottomSheet & ETA Broadcast

## Context
Phases 1–4 set up foundation, data layer, home screen, and live tracking with read-only ETAs.

Reference document attached: `GRAMAYATRI_PRD_COMPLETE.md`

This phase implements the **core action** of the app:
- `PingBottomSheet` — modal bottom sheet for reporting bus location
- Write ping to Firebase `/pings/{routeId}`
- Immediate local ETA recalculation after ping
- Rate limiting: max 1 ping per user per route per 5 minutes
- Snackbar confirmation: "Ping sent! Helping everyone on this route 🙌"

---

## Prerequisites

- Phases 1–4 fully completed.
- `FirebaseRepository.submitPing()` implemented.
- FAB in `LiveTrackingFragment` currently showing placeholder Snackbar.
- `UserPrefsRepository.getLastPingTimestamp()` / `setLastPingTimestamp()` available.

---

## Tasks for This Phase

### 1. bottom_sheet_ping.xml Layout

Create `res/layout/bottom_sheet_ping.xml`:

- `BottomSheetDialogFragment` root: `ConstraintLayout` inside `NestedScrollView`
- Title `TextView`: "Report Bus Location" — 18sp Bold
- **Stop selector**: `HorizontalScrollView` containing `ChipGroup` — one chip per stop on the route
  - Auto-select the user's saved stop chip on open
  - If no saved stop, select first stop
- **Two large action buttons** (equal width, stacked, 56dp height minimum):
  - 🟢 "I AM ON THE BUS" — `green_medium` background
  - 🟡 "BUS JUST PASSED ME" — `eta_amber` background
- **Alert link**: `TextView` text button — "🚫 Report Cancellation / Delay" (navigates to `ReportAlertFragment`)
- "Cancel" `TextView` button — dismisses bottom sheet
- Reporter name `TextView` at bottom: "Reporting as: [name] (tap to change)"
  - Tapping opens a simple `AlertDialog` with an `EditText` to update name

---

### 2. PingBottomSheet

Create `ui/tracking/PingBottomSheet.kt` as a `BottomSheetDialogFragment`:

Receives `routeId` and `stops: List<Stop>` as arguments (pass via Bundle).

On **"I AM ON THE BUS"** tap:
1. Get selected stop from ChipGroup
2. Check rate limit:
   - Read `prefs.getLastPingTimestamp()`
   - If less than 5 minutes ago → show Snackbar: "You can ping again in X minutes." and return
3. Build `Ping` object:
   - `type = Constants.PING_TYPE_ON_BUS`
   - `stopId`, `stopName`, `stopOrder` from selected chip
   - `reporterName` from `prefs.getDisplayName()`
   - `reporterUid` from `repository.getCurrentUid()`
   - `timestamp = System.currentTimeMillis()`
   - `isActive = true`
4. Call `repository.submitPing(routeId, ping)` in a coroutine
5. On success:
   - `prefs.setLastPingTimestamp(System.currentTimeMillis())`
   - Show Snackbar on parent: "Ping sent! Helping everyone on this route 🙌"
   - Dismiss bottom sheet
6. On failure: show error Snackbar

Same logic for **"BUS JUST PASSED ME"** with `type = Constants.PING_TYPE_PASSED`.

---

### 3. Wire FAB in LiveTrackingFragment

Replace the placeholder Snackbar in `LiveTrackingFragment`:

```kotlin
binding.fabPing.setOnClickListener {
    val stops = viewModel.stopEtas.value.map { it.stop }
    val sheet = PingBottomSheet.newInstance(viewModel.routeId, stops)
    sheet.show(childFragmentManager, PingBottomSheet.TAG)
}
```

---

### 4. Firebase Write Logic in FirebaseRepository

Ensure `submitPing()` in `FirebaseRepository`:
- Writes to `/pings/{routeId}/{pushKey}` using `push()`
- Sets `isActive = true` on the new ping
- Sets `isActive = false` on any previous ping for the same route (to keep only 1 active ping per route at a time)

---

### 5. Reporter Name Edit Dialog

If user taps "Reporting as: [name]" in the bottom sheet:
- Show `AlertDialog` with `EditText` pre-filled with current display name
- On confirm: save new name to `UserPrefsRepository` and update bottom sheet TextView

---

### 6. Rate Limit Logic

In `PingBottomSheet`:
```kotlin
fun isRateLimited(): Pair<Boolean, Long> {
    val last = prefs.getLastPingTimestamp()
    val diffMin = (System.currentTimeMillis() - last) / 60_000
    return if (diffMin < Constants.PING_RATE_LIMIT_MIN) {
        Pair(true, Constants.PING_RATE_LIMIT_MIN - diffMin)
    } else {
        Pair(false, 0)
    }
}
```

---

## Deliverables

1. ✅ `bottom_sheet_ping.xml` — stop chip selector + two action buttons + reporter name.
2. ✅ `PingBottomSheet` writing pings to Firebase with rate limiting.
3. ✅ FAB in `LiveTrackingFragment` opens `PingBottomSheet`.
4. ✅ ETA updates in real-time for all users after ping is written (via existing listener in Phase 4).
5. ✅ Reporter name edit dialog works.
6. ✅ Only 1 active ping per route at a time in Firebase.

---

## Success Criteria

- Tapping FAB opens the bottom sheet with all stops on the route.
- User's saved stop is pre-selected.
- Tapping "I AM ON THE BUS" writes a ping to Firebase and updates ETAs on screen within 3 seconds.
- "BUS JUST PASSED ME" works identically with correct type.
- Rate limit blocks a second ping within 5 minutes and shows a countdown message.
- Snackbar "Ping sent! Helping everyone on this route 🙌" appears after successful ping.
````

---

### PHASE 6: ALERTS SYSTEM

````markdown
# PHASE 6: Alerts System — AlertsScreen, ReportAlertScreen & FCM

## Context
Phases 1–5 implemented foundation, data layer, home screen, live tracking, and the ping flow.

Reference document attached: `GRAMAYATRI_PRD_COMPLETE.md`

This phase implements:
- `AlertsFragment` — list of cancellation/delay alerts
- `AlertAdapter` — alert card UI
- `ReportAlertFragment` — form to raise a new alert
- FCM push notification to route subscribers when an alert is posted
- Firebase Cloud Function (Node.js) to trigger FCM on new alert writes

---

## Prerequisites

- Phases 1–5 fully completed.
- `FirebaseRepository.getAlerts()` and `submitAlert()` implemented.
- FCM enabled in Firebase console.

---

## Tasks for This Phase

### 1. item_alert_card.xml Layout

Create `res/layout/item_alert_card.xml`:

- `MaterialCardView`, 1dp elevation, white surface.
- **Left border**: 4dp colored divider — red for CANCELLED, amber for DELAYED, blue for INFO.
- Alert type icon: 🚫 / ⚠️ / ℹ️ (use vector drawables `ic_alert_cancel.xml`, `ic_alert_delay.xml`).
- Route name: 14sp Medium.
- Alert message: 14sp Normal — reason + note combined.
- Reporter line: 12sp Italic secondary — "Reported by Meena · 10 min ago".
- Dismiss `ImageButton` (X icon) — top-right corner.
- Minimum height: 80dp, padding 16dp.

---

### 2. AlertAdapter

Create `ui/alerts/AlertAdapter.kt`:

- `ListAdapter<BusAlert, AlertViewHolder>` with `DiffUtil`
- Bind all fields
- Left border color based on `alert.type`:
  - `CANCELLED` → red (`#C62828`)
  - `DELAYED` → amber (`eta_amber`)
  - `INFO` → blue (`#1565C0`)
- Dismiss button: call `onDismiss(alert)` lambda (local dismiss only — remove from visible list, do not delete from Firebase)
- Format time with `TimeUtils.formatMinutesAgo()`

---

### 3. AlertsViewModel

Create `ui/alerts/AlertsViewModel.kt`:

- Inject `FirebaseRepository` and `UserPrefsRepository`
- Expose `alerts: StateFlow<List<BusAlert>>`
- Filter: `showMyRoutesOnly: MutableStateFlow<Boolean>`
  - When true: only show alerts for `prefs.getSavedRouteId()`
  - When false: show all
- `dismissedAlerts: MutableSet<String>` — local-only set of dismissed alert push keys
- Local dismiss: add to `dismissedAlerts`, re-emit filtered list

---

### 4. fragment_alerts.xml Layout

Create `res/layout/fragment_alerts.xml`:

- `TopAppBar`: "Alerts & Cancellations"
- Filter `ChipGroup` (horizontal): "All Routes" chip | "My Routes" chip
  - Single-selection chips
- `RecyclerView` with `LinearLayoutManager`
- Empty state: "No alerts. All buses running normally ✅" — centered, visible when list is empty
- `FloatingActionButton` bottom-right: "Report Alert +" — navigates to `reportAlertFragment`

---

### 5. AlertsFragment

Create `ui/alerts/AlertsFragment.kt`:

- `@AndroidEntryPoint`, inject `AlertsViewModel` via `by viewModels()`
- Observe `alerts` → submit to `AlertAdapter`
- Filter chips toggle `viewModel.showMyRoutesOnly`
- Dismiss button in adapter → `viewModel.dismissAlert(alert)`
- FAB → navigate to `reportAlertFragment`

---

### 6. fragment_report_alert.xml Layout

Create `res/layout/fragment_report_alert.xml`:

- `TopAppBar`: "Report an Alert", back arrow
- Route selector: `AutoCompleteTextView` (Material Exposed Dropdown) listing all routes
- Alert type: `RadioGroup` with 3 options:
  - 🚫 Bus Cancelled Today
  - ⚠️ Bus Running Late
  - ℹ️ Other Information
- Reason: `AutoCompleteTextView` dropdown with options:
  - "Road blocked", "Bus breakdown", "Driver absent", "Festival/event", "Other"
- Note: `TextInputEditText` multiline, hint: "Add details (optional)"
- "Submit Alert" `Button` — full width, `green_primary` background
- All fields inside `ScrollView` for small screens

---

### 7. ReportAlertFragment + ViewModel

Create `ui/alerts/ReportAlertFragment.kt` and `AlertsViewModel` (extend it or create `ReportAlertViewModel`):

On **Submit**:
1. Validate: route selected + alert type selected
2. Build `BusAlert` object with all fields + `reporterName` from prefs + `reporterUid` from Firebase Auth
3. Call `repository.submitAlert(routeId, alert)`
4. On success: show Snackbar "Alert reported. Neighbours will be notified.", navigate back
5. On failure: show error Snackbar

---

### 8. FCM Cloud Function

Create a Firebase Cloud Function in `functions/index.ts` (Node.js / TypeScript):

```typescript
export const onAlertCreated = functions.database
    .ref('/alerts/{routeId}/{alertId}')
    .onCreate(async (snapshot, context) => {
        const alert = snapshot.val();
        const routeId = context.params.routeId;

        // Send FCM to topic for this route
        const message = {
            notification: {
                title: alert.type === 'CANCELLED'
                    ? '🚫 Bus Cancelled'
                    : alert.type === 'DELAYED'
                    ? '⚠️ Bus Delayed'
                    : 'ℹ️ Route Update',
                body: `${alert.routeName}: ${alert.reason}. Reported by ${alert.reporterName}.`
            },
            topic: `route_${routeId}`
        };
        await admin.messaging().send(message);
    });
```

Deploy with `firebase deploy --only functions`.

---

### 9. FCM Topic Subscription in Android

In `MainActivity` or during onboarding, subscribe the user to their saved route's FCM topic:
```kotlin
FirebaseMessaging.getInstance().subscribeToTopic("route_${savedRouteId}")
```

On saved stop change in Profile (Phase 7): unsubscribe old topic, subscribe new.

---

### 10. NavGraph Updates

Add to `nav_graph.xml`:
- `reportAlertFragment` destination
- Action: `alertsFragment` → `reportAlertFragment`
- Action: `pingBottomSheet` "Report Cancellation" link → `reportAlertFragment` (deep link or action)
- `liveTrackingFragment` can also navigate to `reportAlertFragment` via PingBottomSheet link

---

## Deliverables

1. ✅ `AlertsFragment` with filter chips + real-time alert list from Firebase.
2. ✅ `AlertAdapter` with colored left borders + dismiss.
3. ✅ `ReportAlertFragment` with route/type/reason/note form.
4. ✅ Alert write to Firebase triggers FCM via Cloud Function.
5. ✅ Users auto-subscribed to their route FCM topic.
6. ✅ Empty state: "No alerts. All buses running normally ✅"

---

## Success Criteria

- `AlertsFragment` shows all alerts in real time.
- "My Routes" filter shows only alerts for saved route.
- Submitting an alert writes to Firebase and the FCM Cloud Function sends a push notification.
- Dismissing an alert hides it from the list (local only).
- Empty state shows when no alerts exist.
````

---

### PHASE 7: ONBOARDING & PROFILE

````markdown
# PHASE 7: Onboarding, Splash & Profile Screen

## Context
Phases 1–6 implemented the core tracking and alerts functionality.

Reference document attached: `GRAMAYATRI_PRD_COMPLETE.md`

This phase implements:
- `SplashFragment` — first-launch routing
- `OnboardingFragment` + `OnboardingAdapter` — 3-page explainer, display name, anonymous sign-in
- `ProfileFragment` — saved stop, display name, notification toggles, about

---

## Prerequisites

- Phases 1–6 fully completed.
- `UserPrefsRepository` implemented with `isFirstLaunch()`, `setFirstLaunchDone()`, `saveStop()` etc.
- `FirebaseRepository.signInAnonymously()` working.
- FCM topic subscription logic available.

---

## Tasks for This Phase

### 1. SplashFragment

Create `ui/splash/SplashFragment.kt` and `res/layout/fragment_splash.xml`:

**Layout:**
- Green gradient background (`bg_green_gradient.xml`)
- App logo (bus icon `ic_bus.xml`) centered, 80dp
- "Grama-Yatri" wordmark below: 28sp Bold white
- Tagline: "Community Bus Tracker" 14sp white, italic

**Logic (in `onViewCreated`, 1.5s delay):**
```kotlin
lifecycleScope.launch {
    delay(1500)
    if (prefs.isFirstLaunch()) {
        findNavController().navigate(R.id.action_splash_to_onboarding)
    } else {
        findNavController().navigate(R.id.action_splash_to_home)
    }
}
```

Create `bg_green_gradient.xml` in `res/drawable/`:
```xml
<shape>
  <gradient android:startColor="#1B5E20" android:endColor="#4CAF50"
            android:angle="135" android:type="linear"/>
</shape>
```

---

### 2. OnboardingAdapter

Create `ui/onboarding/OnboardingAdapter.kt`:

`FragmentStateAdapter` for `ViewPager2` with 3 pages:
- **Page 1:** Illustration placeholder (bus icon), title: "See the bus? Ping it!", subtitle: "Spot the bus at any stop and tap Ping. Done."
- **Page 2:** Title: "Everyone gets the ETA instantly", subtitle: "Your ping calculates arrival time for all stops on the route."
- **Page 3:** Title: "Get alerts for cancellations", subtitle: "Passengers report delays and cancellations. You get notified instantly."

Each page: full-screen `ConstraintLayout`, centered illustration + title + subtitle.

---

### 3. fragment_onboarding.xml Layout

Create `res/layout/fragment_onboarding.xml`:

- `ViewPager2` (full screen except bottom strip)
- `LinearLayout` page indicator (3 dots, `TabLayout` or custom dots)
- "Skip" `TextView` button — top right
- "Next" / "Get Started" `Button` — bottom
- On page 3: show `TextInputEditText` for display name (hint: "Your name (optional, e.g. Ravi)")
- "Start Using App" `Button` on page 3 (replaces "Next")
- White background, green accent for active dot

---

### 4. OnboardingFragment

Create `ui/onboarding/OnboardingFragment.kt`:

- Wire `ViewPager2` with `OnboardingAdapter`
- "Skip" → go to last page
- "Next" → advance page; on page 3 becomes "Get Started"
- On "Start Using App":
  1. Save display name to `prefs.setDisplayName(name)` (if entered)
  2. Call `repository.signInAnonymously()` — await result
  3. `prefs.setFirstLaunchDone()`
  4. Navigate to HomeScreen
  5. Show loading indicator during sign-in

---

### 5. fragment_profile.xml Layout

Create `res/layout/fragment_profile.xml`:

- `TopAppBar`: "Profile"
- User avatar section: `ShapeableImageView` (circle) with first letter of name, `green_medium` background
- Display name: `TextInputEditText` (inline editable) + "Save" button appearing on edit
- **Saved Stop section** (MaterialCardView):
  - Title: "My Stop" with pin icon
  - Route selector: `AutoCompleteTextView` dropdown
  - Stop selector: `AutoCompleteTextView` dropdown (populates on route selection)
  - "Save My Stop" button
  - Current saved stop shown as chip: "📍 [StopName] · [RouteName]" (or "Not set")
- **Notifications** (MaterialCardView):
  - `SwitchMaterial`: "Alert notifications" (on/off)
- **About** (MaterialCardView):
  - App version `TextView`
  - "How it works" clickable row → navigates back to Onboarding
  - "Privacy Policy" row → opens simple text dialog
- "Report Alert" shortcut `Button`

---

### 6. ProfileViewModel

Create `ui/profile/ProfileViewModel.kt`:

- Inject `FirebaseRepository`, `UserPrefsRepository`
- Expose `routes: StateFlow<List<Route>>`
- `fun saveStop(routeId: String, stopId: String, stopName: String)`:
  - Calls `prefs.saveStop()`
  - Unsubscribes old FCM topic, subscribes new `route_{routeId}` topic
- `fun saveDisplayName(name: String)`: calls `prefs.setDisplayName(name)`
- `val savedStopChip: StateFlow<String>` — formatted as "📍 [name] · [route]"

---

### 7. ProfileFragment

Create `ui/profile/ProfileFragment.kt`:

- `@AndroidEntryPoint`, inject `ProfileViewModel`
- Load routes into route dropdown on resume
- Route dropdown selection populates stop dropdown with that route's stops
- On "Save My Stop": validate both selected → call `viewModel.saveStop()`
- Display name editing: show inline on focus, "Save" button on change
- Alert notifications toggle: save to prefs, subscribe/unsubscribe FCM accordingly
- "How it works" → navigate to `onboardingFragment`
- "Report Alert" shortcut → navigate to `reportAlertFragment`

---

### 8. NavGraph Updates

In `nav_graph.xml`:
- Set `splashFragment` as start destination
- Add actions:
  - `splash → onboarding`
  - `splash → home`
  - `onboarding → home`
  - `profile → onboarding` (how it works)
  - `profile → reportAlert`

---

## Deliverables

1. ✅ `SplashFragment` routing to Onboarding or Home based on first-launch flag.
2. ✅ `OnboardingFragment` with 3-page ViewPager2, display name input, anonymous sign-in.
3. ✅ `ProfileFragment` with saved stop selector, display name editor, notification toggle.
4. ✅ `ProfileViewModel` persisting preferences and managing FCM subscriptions.
5. ✅ NavGraph updated with all new actions.

---

## Success Criteria

- First launch shows splash → onboarding; subsequent launches go straight to home.
- Completing onboarding signs user in anonymously (UID visible in Firebase console).
- Profile screen saves stop correctly; HomeScreen sticky header reflects saved stop.
- FCM topic subscription changes when saved route changes.
- "How it works" from Profile re-plays onboarding pages.
````

---

### PHASE 8: OFFLINE CACHE, RATE LIMITING & LOW-DATA HARDENING

````markdown
# PHASE 8: Offline Cache, Rate Limiting & Low-Data Hardening

## Context
Phases 1–7 implemented all screens and core functionality.

Reference document attached: `GRAMAYATRI_PRD_COMPLETE.md`

This phase hardens the app for real-world rural conditions:
- Offline read-only mode — show last known ETAs from Firebase disk cache
- Rate limiting enforcement (already in ping, now also alerts)
- Firebase listener lifecycle — attach only on active screen, detach in `onStop()`
- Low-data audit: all icons as Vector Drawables, no remote images
- Network state detection — banner when offline
- Firebase disk persistence enabled

---

## Prerequisites

- Phases 1–7 fully completed.
- All screens functional.

---

## Tasks for This Phase

### 1. Firebase Disk Persistence

In `GramaYatriApplication.kt`, before any Firebase call:
```kotlin
FirebaseDatabase.getInstance().setPersistenceEnabled(true)
```

This gives free offline caching of all listened nodes.

---

### 2. Keep-Sync for Saved Route Only

In `LiveTrackingViewModel`, when route loads:
```kotlin
FirebaseDatabase.getInstance()
    .getReference("${Constants.PATH_PINGS}/${routeId}")
    .keepSynced(true)
```

In `onCleared()`, disable:
```kotlin
FirebaseDatabase.getInstance()
    .getReference("${Constants.PATH_PINGS}/${routeId}")
    .keepSynced(false)
```

For all other routes: do NOT set `keepSynced(true)` — data usage constraint.

---

### 3. Offline Banner

Create `util/NetworkMonitor.kt` using `ConnectivityManager.NetworkCallback`:
```kotlin
class NetworkMonitor(context: Context) {
    val isOnline: StateFlow<Boolean>
    // ...
}
```

Inject in `MainActivity`. Observe `isOnline`:
- If offline: show a persistent `Snackbar` / top banner: "You're offline. Showing last known data."
- Banner disappears automatically when back online.

---

### 4. Listener Lifecycle Audit

Audit all fragments:

**Rule:** Firebase listeners (`callbackFlow`) must be collected only while the view is visible.

In every fragment that has a Firebase listener, ensure:
```kotlin
// Correct pattern
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.someFlow.collect { ... }
    }
}
```

This automatically stops collection (and detaches listener) when the fragment goes to `STOPPED` state.

---

### 5. Alert Rate Limiting

Add alert rate limiting in `ReportAlertFragment` / ViewModel:
- Store last alert submission time in `UserPrefsRepository` (`KEY_LAST_ALERT_TIMESTAMP`)
- Limit: max 1 alert per user per 10 minutes
- Show message: "You submitted an alert recently. Please wait X minutes."

---

### 6. Stale ETA Decay

In `LiveTrackingViewModel`, add a periodic ticker that re-runs ETA calculation every 60 seconds against the same ping:

```kotlin
viewModelScope.launch {
    while (isActive) {
        delay(60_000)
        val currentPing = _lastPing.value ?: continue
        val stops = _route.value?.stops?.values?.sortedBy { it.order } ?: continue
        _stopEtas.value = EtaCalculator.calculateEtas(stops, currentPing)
        // Update confidence text with incremented minutes
    }
}
```

This ensures ETAs tick down in real time even without new pings.

---

### 7. Low-Data Audit Checklist

Verify the following (fix any violations):
- [ ] All icons are Vector Drawables (no `.png` or `.jpg` in `res/drawable`)
- [ ] No `Glide` / `Coil` remote image loading in MVP
- [ ] Firebase listeners attached only on `STARTED` lifecycle (see step 4)
- [ ] `keepSynced(true)` only for active screen's route
- [ ] No polling loops (use Firebase listeners only)
- [ ] RecyclerView uses `RecycledViewPool` if > 50 items expected

---

### 8. Seed Data Fallback

In `HomeViewModel`, if Firebase returns empty routes (first load with no connection):
- Read `res/raw/routes_seed.json` as fallback
- Parse and display seed routes with "No recent ping" status
- Log a warning in Logcat: "Firebase unavailable — using seed data"

---

## Deliverables

1. ✅ Firebase disk persistence enabled.
2. ✅ `keepSynced` only for user's active route.
3. ✅ Offline banner via `NetworkMonitor`.
4. ✅ All fragments use `repeatOnLifecycle(STARTED)` for Flow collection.
5. ✅ Alert rate limiting (10 min cooldown).
6. ✅ ETA decay ticker — ETAs update every 60 seconds without new pings.
7. ✅ Low-data audit passed (all icons are vectors).
8. ✅ Seed data fallback when Firebase is unavailable.

---

## Success Criteria

- App launched in airplane mode shows last known route data (or seed data).
- Offline banner appears and disappears correctly.
- ETAs tick down in real time on-screen without requiring a new ping.
- No Firebase listener leaks — verified via Android Profiler.
- APK size ≤ 5 MB.
````

---

### PHASE 9: GENAI — NIGHTLY ATT REFINEMENT (GEMINI API)

````markdown
# PHASE 9: GenAI — Nightly ATT Refinement via Gemini API

## Context
Phases 1–8 completed the full Android app.

Reference document attached: `GRAMAYATRI_PRD_COMPLETE.md`

This phase implements the **backend GenAI layer**:
- Firebase Cloud Function (Node.js / TypeScript) running nightly at 2 AM IST
- Collects 7 days of ping data per route
- Calls Gemini API with structured prompt
- Parses refined ATT values
- Updates `/routes/{routeId}/stops/{stopId}/attToNextMin` in Firebase

This is a backend-only phase — no Android changes.

---

## Prerequisites

- Firebase project created, Realtime Database has route + ping data.
- Firebase Cloud Functions set up (Blaze plan required for external API calls).
- Gemini API key available.

---

## Tasks for This Phase

### 1. Cloud Function Project Setup

In your Firebase project root:
```bash
firebase init functions
# Choose TypeScript
# Install dependencies
```

In `functions/package.json`, add:
```json
"node-fetch": "^3.3.2"
```

---

### 2. Store Gemini API Key Securely

Using Firebase environment config:
```bash
firebase functions:config:set gemini.api_key="YOUR_GEMINI_API_KEY"
```

Access in code:
```typescript
const GEMINI_API_KEY = functions.config().gemini.api_key;
```

---

### 3. Ping Data Collector Function

Create `functions/src/attRefinement.ts`:

```typescript
async function collectPingObservations(
    db: admin.database.Database,
    routeId: string,
    stops: Record<string, Stop>
): Promise<Record<string, number[]>> {
    // Fetch pings from last 7 days
    const since = Date.now() - 7 * 24 * 60 * 60 * 1000;
    const snapshot = await db.ref(`pings/${routeId}`)
        .orderByChild('timestamp')
        .startAt(since)
        .once('value');

    const pings: Ping[] = Object.values(snapshot.val() ?? {});
    const sortedPings = pings.sort((a, b) => a.timestamp - b.timestamp);
    const sortedStops = Object.values(stops).sort((a, b) => a.order - b.order);

    const observations: Record<string, number[]> = {};

    // For each consecutive stop pair, find matching ping pairs
    for (let i = 0; i < sortedStops.length - 1; i++) {
        const stopA = sortedStops[i];
        const stopB = sortedStops[i + 1];
        const key = `${stopA.id}_to_${stopB.id}`;
        observations[key] = [];

        for (let j = 0; j < sortedPings.length - 1; j++) {
            const pingA = sortedPings[j];
            const pingB = sortedPings[j + 1];
            if (pingA.stopOrder === stopA.order && pingB.stopOrder === stopB.order) {
                const diff = (pingB.timestamp - pingA.timestamp) / 60_000;
                if (diff > 0 && diff < 120) {   // sanity check: < 2 hours
                    observations[key].push(Math.round(diff));
                }
            }
        }
    }
    return observations;
}
```

---

### 4. Gemini API Call Function

```typescript
async function refineAttWithGemini(
    observations: Record<string, number[]>,
    currentAtts: Record<string, number>
): Promise<Record<string, { attMin: number; confidence: string }>> {

    const prompt = `
Given these observed travel times (in minutes) between stops:
${Object.entries(observations)
    .filter(([, obs]) => obs.length > 0)
    .map(([key, obs]) => `- ${key}: [${obs.join(', ')}] minutes (${obs.length} observations)`)
    .join('\n')}

Current stored ATT values:
${Object.entries(currentAtts).map(([key, val]) => `- ${key}: ${val} min`).join('\n')}

Respond ONLY with a JSON object. For each stop pair with observations, compute:
- attMin = round(0.7 * mean(observations) + 0.3 * currentAtt)  
- confidence = "LOW_CONFIDENCE" if variance > 50% of mean, else "HIGH"
Format:
{
  "S1_to_S2": { "attMin": 12, "confidence": "HIGH" },
  ...
}
Do not include any preamble, explanation, or markdown.`;

    const res = await fetch(
        `https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=${GEMINI_API_KEY}`,
        {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                contents: [{ parts: [{ text: prompt }] }],
                generationConfig: { temperature: 0.1 }
            })
        }
    );
    const data = await res.json();
    const text = data.candidates?.[0]?.content?.parts?.[0]?.text ?? '{}';
    const clean = text.replace(/```json|```/g, '').trim();
    return JSON.parse(clean);
}
```

---

### 5. Firebase Updater Function

```typescript
async function updateAttValues(
    db: admin.database.Database,
    routeId: string,
    stops: Record<string, Stop>,
    refined: Record<string, { attMin: number; confidence: string }>
) {
    const updates: Record<string, number> = {};
    const sortedStops = Object.values(stops).sort((a, b) => a.order - b.order);

    for (let i = 0; i < sortedStops.length - 1; i++) {
        const stopA = sortedStops[i];
        const stopB = sortedStops[i + 1];
        const key = `${stopA.id}_to_${stopB.id}`;
        if (refined[key] && refined[key].confidence === 'HIGH') {
            updates[`routes/${routeId}/stops/${stopA.id}/attToNextMin`] = refined[key].attMin;
        }
        // LOW_CONFIDENCE: skip update, keep existing value
    }
    await db.ref().update(updates);
}
```

---

### 6. Scheduled Cloud Function (Nightly 2 AM IST)

In `functions/src/index.ts`:

```typescript
export const nightlyAttRefinement = functions.pubsub
    .schedule('0 20 * * *')   // 20:30 UTC = 02:00 IST
    .timeZone('UTC')
    .onRun(async () => {
        const db = admin.database();
        const routesSnap = await db.ref('routes').once('value');
        const routes: Record<string, Route> = routesSnap.val() ?? {};

        for (const [routeId, route] of Object.entries(routes)) {
            const stops = route.stops;
            const currentAtts: Record<string, number> = {};
            const sortedStops = Object.values(stops).sort((a, b) => a.order - b.order);
            for (let i = 0; i < sortedStops.length - 1; i++) {
                const key = `${sortedStops[i].id}_to_${sortedStops[i + 1].id}`;
                currentAtts[key] = sortedStops[i].attToNextMin;
            }

            const observations = await collectPingObservations(db, routeId, stops);
            const anyObservations = Object.values(observations).some(o => o.length >= 3);
            if (!anyObservations) {
                console.log(`Route ${routeId}: not enough observations, skipping.`);
                continue;
            }

            const refined = await refineAttWithGemini(observations, currentAtts);
            await updateAttValues(db, routeId, stops, refined);
            console.log(`Route ${routeId}: ATT values updated.`);
        }
    });
```

---

### 7. Deploy

```bash
firebase deploy --only functions
```

Verify in Firebase console → Functions → `nightlyAttRefinement` is scheduled.

---

## Deliverables

1. ✅ Cloud Function `nightlyAttRefinement` deployed and scheduled for 2 AM IST.
2. ✅ Collects ping observations from last 7 days per stop pair.
3. ✅ Calls Gemini API with structured prompt (no PII).
4. ✅ Parses refined ATT values from JSON response.
5. ✅ Writes `HIGH` confidence updates back to Firebase.
6. ✅ `LOW_CONFIDENCE` pairs are skipped (existing value preserved).

---

## Success Criteria

- Function deploys without TypeScript errors.
- Manually triggering the function (via Firebase console → Functions → Test) updates ATT values in Realtime Database.
- Gemini API is called with anonymised data only (stop IDs and timestamps, no names).
- `LOW_CONFIDENCE` stop pairs are NOT overwritten.
- Function completes in under 30 seconds for 2 routes.
````

---

### PHASE 10: FINAL QA & LAUNCH

````markdown
# PHASE 10: Final QA, Crashlytics, Analytics & APK Release

## Context
Phases 1–9 completed all screens, features, and the GenAI backend.

Reference document attached: `GRAMAYATRI_PRD_COMPLETE.md`

This phase is:
- Firebase Crashlytics integration
- Firebase Analytics event tracking
- End-to-end flow testing checklist
- Performance profiling (cold start, data usage)
- ProGuard / R8 rules
- Signed release APK build

---

## Prerequisites

- Phases 1–9 fully completed.
- All screens functional and tested.
- Firebase project on Blaze plan (for Cloud Functions).

---

## Tasks for This Phase

### 1. Firebase Crashlytics

Add to `build.gradle.kts` (app):
```kotlin
implementation("com.google.firebase:firebase-crashlytics-ktx")
```

Apply plugin in `build.gradle.kts` (app):
```kotlin
id("com.google.firebase.crashlytics")
```

Add to project-level:
```kotlin
id("com.google.firebase.crashlytics") version "2.9.9" apply false
```

In `GramaYatriApplication.kt`, no extra init needed — Crashlytics starts automatically.

Set user identifier for crash reports (no PII — use anonymous UID only):
```kotlin
FirebaseCrashlytics.getInstance().setUserId(firebaseAuth.currentUser?.uid ?: "anonymous")
```

For caught exceptions in repository, log non-fatals:
```kotlin
FirebaseCrashlytics.getInstance().recordException(e)
```

---

### 2. Firebase Analytics Events

Create `util/AnalyticsHelper.kt`:

```kotlin
object AnalyticsHelper {
    fun logPingSent(analytics: FirebaseAnalytics, routeId: String, pingType: String) {
        analytics.logEvent("ping_sent") {
            param("route_id", routeId)
            param("ping_type", pingType)
        }
    }

    fun logAlertReported(analytics: FirebaseAnalytics, routeId: String, alertType: String) {
        analytics.logEvent("alert_reported") {
            param("route_id", routeId)
            param("alert_type", alertType)
        }
    }

    fun logRouteViewed(analytics: FirebaseAnalytics, routeId: String) {
        analytics.logEvent("route_viewed") {
            param("route_id", routeId)
        }
    }

    fun logOnboardingCompleted(analytics: FirebaseAnalytics) {
        analytics.logEvent("onboarding_completed", null)
    }
}
```

Call `AnalyticsHelper.logPingSent()` in `PingBottomSheet` on success.
Call `AnalyticsHelper.logAlertReported()` in `ReportAlertFragment` on success.
Call `AnalyticsHelper.logRouteViewed()` in `LiveTrackingFragment.onResume()`.
Call `AnalyticsHelper.logOnboardingCompleted()` in `OnboardingFragment` on "Start Using App".

---

### 3. End-to-End Testing Checklist

Run through the following on **two emulators simultaneously** (to test real-time sync):

**Onboarding flow:**
- [ ] First launch shows Splash → Onboarding
- [ ] Skip button goes to last page
- [ ] Anonymous sign-in succeeds, UID visible in Firebase console
- [ ] Display name saved, shown in PingBottomSheet

**Home screen:**
- [ ] 2 route cards load from Firebase
- [ ] Pull-to-refresh works
- [ ] Last ping time shown on route card
- [ ] Live dot green when ping < 30 min, grey otherwise

**Live Tracking + Ping:**
- [ ] All stops shown in correct order
- [ ] Tapping FAB opens PingBottomSheet
- [ ] "I AM ON THE BUS" at stop S2 → ETA updates for all downstream stops within 3 seconds
- [ ] ETA chip colours correct (green < 5 min, amber 5–15, red-amber > 15)
- [ ] Reporter attribution shows "📍 [name] · X min ago"
- [ ] Second ping within 5 min is blocked with correct message
- [ ] ETA ticks down every 60 seconds
- [ ] After 30 min no new ping: status changes to STALE

**Alerts:**
- [ ] Report a cancellation → shows in AlertsFragment immediately
- [ ] FCM push notification received on second emulator
- [ ] "My Routes" filter works
- [ ] Dismiss removes alert from list locally

**Profile:**
- [ ] Saved stop updates sticky header on HomeScreen
- [ ] Display name change reflected in PingBottomSheet immediately
- [ ] "How it works" replays onboarding

**Offline:**
- [ ] Toggle airplane mode → offline banner appears
- [ ] Last known ETAs still visible
- [ ] Ping FAB shows appropriate error when offline
- [ ] Restore connection → banner disappears

---

### 4. Performance Profiling

Using Android Profiler:

**Cold start target: ≤ 2 seconds on 3G**
- Measure with Network Profiler set to 3G throttling
- If > 2s: defer any non-critical Firebase calls to background

**Data usage per session target: ≤ 500 KB/day**
- Use Network Profiler to measure one session (open app, view 2 routes, send 1 ping, view alerts)
- Log total bytes: should be under 100 KB per typical session

**ETA broadcast latency target: ≤ 3 seconds**
- Measure time from ping write to UI update on second emulator

Document your results.

---

### 5. ProGuard / R8 Rules

In `proguard-rules.pro`, add:
```proguard
# Firebase Realtime Database models — keep all fields for deserialization
-keepclassmembers class com.gramayatri.app.data.model.** {
    *;
}

# Hilt
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
```

Enable R8 in `build.gradle.kts`:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
}
```

---

### 6. Signed Release APK

1. Generate a keystore:
```bash
keytool -genkey -v -keystore gramayatri-release.jks \
        -alias gramayatri -keyalg RSA -keysize 2048 -validity 10000
```

2. In `build.gradle.kts` (app), add signing config:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("gramayatri-release.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = "gramayatri"
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}
buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
    }
}
```

3. Build:
```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

---

### 7. Final Verification Checklist

- [ ] `./gradlew assembleRelease` passes with 0 errors and 0 warnings
- [ ] APK size ≤ 5 MB
- [ ] App installs and runs on API 24 emulator
- [ ] App installs and runs on API 34 emulator
- [ ] Crashlytics shows test crash resolved
- [ ] Analytics shows `onboarding_completed`, `ping_sent` events in Firebase console (DebugView)
- [ ] No Logcat errors tagged `GRAMAYATRI` or `FirebaseDatabase`
- [ ] Firebase Realtime Database rules are applied (test read/write rejection for unauthenticated requests)

---

## Deliverables

1. ✅ Firebase Crashlytics integrated, non-fatal errors logged.
2. ✅ Analytics events: `ping_sent`, `alert_reported`, `route_viewed`, `onboarding_completed`.
3. ✅ End-to-end testing checklist completed and passed.
4. ✅ Performance targets documented (cold start ≤ 2s, data ≤ 500 KB/day, ETA latency ≤ 3s).
5. ✅ ProGuard/R8 rules configured.
6. ✅ Signed release APK built.

---

## Success Criteria

- Signed APK runs on API 24 and API 34 without crashes.
- All items in the end-to-end testing checklist are ✅.
- ETA broadcast latency ≤ 3 seconds verified on two emulators.
- Crashlytics and Analytics events visible in Firebase console.

After this phase, Grama-Yatri is ready for field testing with community champions. 🚌
````

---

## APPENDIX A: FILE STRUCTURE (FINAL)

```
app/
├── src/main/
│   ├── java/com/gramayatri/app/
│   │   ├── GramaYatriApplication.kt
│   │   ├── MainActivity.kt
│   │   ├── data/
│   │   │   ├── model/
│   │   │   │   ├── Route.kt
│   │   │   │   ├── Stop.kt
│   │   │   │   ├── Ping.kt
│   │   │   │   ├── BusAlert.kt
│   │   │   │   ├── StopEta.kt
│   │   │   │   ├── EtaStatus.kt
│   │   │   │   └── RouteCardItem.kt
│   │   │   ├── repository/
│   │   │   │   ├── FirebaseRepository.kt
│   │   │   │   └── UserPrefsRepository.kt
│   │   │   └── remote/
│   │   │       └── FirebaseDataSource.kt
│   │   ├── di/
│   │   │   └── AppModule.kt
│   │   ├── ui/
│   │   │   ├── splash/SplashFragment.kt
│   │   │   ├── onboarding/
│   │   │   │   ├── OnboardingFragment.kt
│   │   │   │   └── OnboardingAdapter.kt
│   │   │   ├── home/
│   │   │   │   ├── HomeFragment.kt
│   │   │   │   ├── HomeViewModel.kt
│   │   │   │   └── RouteAdapter.kt
│   │   │   ├── tracking/
│   │   │   │   ├── LiveTrackingFragment.kt
│   │   │   │   ├── LiveTrackingViewModel.kt
│   │   │   │   ├── StopTimelineAdapter.kt
│   │   │   │   └── PingBottomSheet.kt
│   │   │   ├── alerts/
│   │   │   │   ├── AlertsFragment.kt
│   │   │   │   ├── AlertsViewModel.kt
│   │   │   │   ├── AlertAdapter.kt
│   │   │   │   └── ReportAlertFragment.kt
│   │   │   └── profile/
│   │   │       ├── ProfileFragment.kt
│   │   │       └── ProfileViewModel.kt
│   │   └── util/
│   │       ├── EtaCalculator.kt
│   │       ├── TimeUtils.kt
│   │       ├── Constants.kt
│   │       ├── AnalyticsHelper.kt
│   │       ├── NetworkMonitor.kt
│   │       └── Extensions.kt
│   ├── res/
│   │   ├── layout/           (all XML layouts)
│   │   ├── navigation/nav_graph.xml
│   │   ├── menu/bottom_nav_menu.xml
│   │   ├── drawable/         (all vector drawables)
│   │   ├── values/           (colors, strings, themes, dimens)
│   │   └── raw/routes_seed.json
│   └── AndroidManifest.xml
└── build.gradle.kts

functions/
├── src/
│   ├── index.ts
│   └── attRefinement.ts
├── package.json
└── tsconfig.json
```

---

## APPENDIX B: PHASE MAP & WHAT EACH PHASE UNLOCKS

| Phase | Unlocks |
|-------|---------|
| **1 — Foundation** | Android project, Hilt, Firebase connected, nav skeleton, `Constants.kt` |
| **2 — Data Layer** | All data models, `FirebaseRepository`, `UserPrefsRepository`, seed data, Firebase rules |
| **3 — Home Screen** | Route list with live indicator dots, ping attribution, pull-to-refresh |
| **4 — Live Tracking** | Stop timeline, ETA engine, real-time ping listener, sticky header |
| **5 — Ping Flow** | PingBottomSheet, ping write to Firebase, rate limiting, ETA broadcast |
| **6 — Alerts** | Cancellation/delay reporting, FCM push notifications, alert list with filters |
| **7 — Onboarding & Profile** | Splash routing, 3-page onboarding, anonymous sign-in, saved stop, display name |
| **8 — Hardening** | Offline cache, stale ETA decay, listener lifecycle, network banner, low-data audit |
| **9 — GenAI** | Nightly ATT refinement via Gemini API Cloud Function |
| **10 — Launch** | Crashlytics, Analytics events, QA checklist, ProGuard, signed release APK |

---

*End of GRAMAYATRI_PRD_PHASES.md*  
*Always use alongside GRAMAYATRI_PRD_COMPLETE.md. Do not start any phase without attaching both files to the Codex conversation.*
