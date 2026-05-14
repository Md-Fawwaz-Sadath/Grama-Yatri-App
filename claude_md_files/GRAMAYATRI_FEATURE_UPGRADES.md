# GRAMA-YATRI — FEATURE UPGRADES & ENHANCEMENTS DOCUMENT

**Version:** 1.0  
**Date:** May 2026  
**Project:** Grama-Yatri — Community-Powered Village Bus Tracker  
**Document Type:** Companion Enhancement Document — NOT a replacement of `GRAMAYATRI_PRD_COMPLETE.md`  
**Platform:** Android (Kotlin, XML Layouts, Firebase Realtime Database, MVVM + Repository, Hilt/KSP)

---

## ⚠️ IMPORTANT NOTICE FOR CODEX

This document is a **companion** to `GRAMAYATRI_PRD_COMPLETE.md`.  
It does **not** replace, modify, or override anything in the original PRD.  
The original PRD Phases 0–7 must be fully completed before starting any phase in this document.  
All features here are **enhancements** — they make the app more realistic, accessible, and impressive for rural Karnataka users, but they are not required for the base MVP.

When implementing phases from this document, always:
- Keep the existing MVVM + Repository architecture.
- Keep all existing Firebase paths intact — only extend the schema, never break existing nodes.
- Keep UI low-data — no maps, no heavy images, no animations unless explicitly specified.
- Test each phase independently before moving to the next.

---

## PURPOSE

The Grama-Yatri MVP (Phases 0–7) delivers the core bus tracking experience. However, to make the app truly useful for rural Karnataka commuters — many of whom have low digital literacy, speak only Kannada, and use entry-level Android phones on 2G — several targeted enhancements are needed.

This document specifies those enhancements in detail, organized into implementation phases compatible with Codex prompting.

---

## UPGRADE PHASE MAP

| Phase | Title | Key Features |
|---|---|---|
| Phase 8.5 | Smart Tracking Polish | ETA edge cases, confidence levels, saved stop highlight |
| Phase 8.6 | Kannada + Voice Assist | Multilingual support, Text-to-Speech read-aloud |
| Phase 8.7 | Weekly Schedule + Bus Plate Number | Day-based schedules, bus identifier display |
| Phase 8.8 | Low-Data UI Polish | Cards, typography, stepper, icons — no weight added |
| Phase 10 | Final QA & Demo Preparation | End-to-end testing, demo seed data, README |

> Phase 10 supersedes the original Phase 7 polish tasks and expands them for the fully enhanced app.

---

## UPGRADE FEATURE OVERVIEW

| ID | Feature | Phase | Priority |
|---|---|---|---|
| U01 | ETA edge case handling ("~0 min", trip completed) | 8.5 | P0 |
| U02 | Ping confidence level indicator | 8.5 | P1 |
| U03 | Saved stop highlight in Live Tracking | 8.5 | P0 |
| U04 | Saved stop ETA header card | 8.5 | P1 |
| U05 | Kannada language support | 8.6 | P0 |
| U06 | Language selector in Profile screen | 8.6 | P0 |
| U07 | Voice Assist / Read Aloud (Android TTS) | 8.6 | P1 |
| U08 | Kannada TTS with graceful fallback | 8.6 | P1 |
| U09 | Weekly schedule per route per day | 8.7 | P0 |
| U10 | Today's schedule shown on Home | 8.7 | P0 |
| U11 | Tomorrow / All Days schedule view | 8.7 | P2 |
| U12 | Bus plate number on route cards | 8.7 | P1 |
| U13 | Bus plate number on Live Tracking header | 8.7 | P1 |
| U14 | Smarter ping rate limiting | 8.7 | P1 |
| U15 | Splash screen polish | 8.8 | P2 |
| U16 | Home route card redesign | 8.8 | P1 |
| U17 | Live Tracking stepper polish | 8.8 | P1 |
| U18 | Ping bottom sheet polish | 8.8 | P2 |
| U19 | Alerts card redesign | 8.8 | P2 |
| U20 | Profile screen layout polish | 8.8 | P2 |

---

## PHASE 8.5 — SMART TRACKING POLISH

### Objective

Eliminate confusing or misleading ETA display states. Make the Route Timeline screen reliable and trustworthy even when data is old, incomplete, or when the bus has already passed a stop.

---

### Features

#### U01 — ETA Edge Case Handling

**Problem:** When a calculated ETA results in 0 minutes or a past time, showing "~0 min" is confusing and erodes user trust.

**Rules to implement in `EtaCalculator.kt` and `TimelineAdapter.kt`:**

| Condition | Display Text |
|---|---|
| ETA is more than 3 minutes in the future | `"~X min"` |
| ETA is within 0–3 minutes | `"Bus arriving soon 🚌"` |
| ETA is 0 or in the past (non-final stop) | `"Bus may have reached"` |
| ETA is 0 or in the past (final stop) | `"Trip likely completed"` |
| No ping in last 60 minutes | `"—"` (dash, no ETA) |
| Offline / stale cache | `"Last known: [time]"` |

**Implementation note:** The check happens in the adapter's `bind()` function, not in EtaCalculator. EtaCalculator always returns raw timestamps. The adapter applies display rules.

---

#### U02 — Ping Confidence Level Indicator

**Problem:** A ping from 28 minutes ago is much less reliable than one from 2 minutes ago. Users have no way to judge this.

**Rules:**

| Ping Age | Confidence Label | Color |
|---|---|---|
| < 5 minutes | `"High confidence"` | Green `#40916C` |
| 5–15 minutes | `"Medium confidence"` | Amber `#F4A261` |
| 15–30 minutes | `"Low confidence"` | Orange `#E76F51` |
| > 30 minutes | `"Stale report"` | Grey `#9E9E9E` |

**Display:** Shown as a small chip/badge below the reporter attribution text on the active ping stop only. Not shown on every stop — only on the stop that triggered the latest ping.

**Implementation:** Add a `getConfidenceLabel(timestampMillis: Long): ConfidenceLevel` function in `TimeUtils.kt`. `ConfidenceLevel` is a sealed class with four states. Adapter maps state to color and string resource.

---

#### U03 — Saved Stop Highlight in Live Tracking

**Problem:** Users have a "home stop" saved in SharedPreferences, but it looks the same as all other stops in the timeline. Users have to scan the list to find their stop.

**Implementation:**
- In `TimelineAdapter.kt`, check if `stop.stopId == SharedPrefHelper.getHomeStopId()`.
- If true: render the stop row with a distinct background tint (`#E8F5E9` — light green) and a small label `"📍 Your saved stop"` in green text below the stop name.
- This label does NOT appear on any other stop.

---

#### U04 — Saved Stop ETA Header Card

**Problem:** The user's stop may be far down the list and they have to scroll to find their ETA.

**Implementation:**
- Add a `CardView` at the top of `RouteTimelineFragment`, between the alert banner and the RecyclerView.
- This card is only visible if a saved stop exists AND a live ETA exists for that stop.
- Card content:
  - Label: "Your stop — [Stop Name]"
  - ETA: Large bold text — e.g. `"~14 min"` or `"Bus arriving soon!"`
  - Reporter: `"Reported by Ravi K. · 4 min ago"`
- Card is hidden (`View.GONE`) when:
  - No home stop is saved.
  - No live ETA exists for the saved stop.
  - ETA is stale (> 60 min).

---

### Files Likely Affected

- `EtaCalculator.kt` — add `getDisplayText(etaMillis: Long): String` helper
- `TimeUtils.kt` — add `getConfidenceLevel(timestampMillis: Long): ConfidenceLevel`
- `TimelineAdapter.kt` — apply display rules, confidence badge, saved stop highlight
- `RouteTimelineFragment.kt` — add saved stop header card logic
- `fragment_route_timeline.xml` — add header CardView
- `item_timeline_stop.xml` — add confidence chip, saved stop label
- `strings.xml` — add all new string resources
- `colors.xml` — add confidence level colors

### Firebase Schema Changes

None. All logic is client-side.

### Testing Checklist

- [ ] ETA of 0 minutes shows "Bus may have reached" (not "~0 min")
- [ ] Final stop at 0 minutes shows "Trip likely completed"
- [ ] High confidence shown for pings < 5 minutes old
- [ ] Stale report shown for pings > 30 minutes old
- [ ] Saved stop row has green tint and "Your saved stop" label
- [ ] Header card shows correct ETA and hides when no data
- [ ] Header card is hidden when no home stop is saved
- [ ] Header card disappears when ETA goes stale

### Success Criteria

- No `"~0 min"` ever appears anywhere in the app.
- Confidence badges are visible and correctly colored on the active ping stop.
- Saved stop is immediately identifiable without scrolling.
- Header card ETA matches the stop's ETA in the timeline.

---

## PHASE 8.6 — KANNADA + VOICE ASSIST

### Objective

Make Grama-Yatri accessible to rural Karnataka users who are more comfortable in Kannada and to users who cannot read at all by adding Text-to-Speech read-aloud functionality using the Android device's built-in TTS engine.

---

### Features

#### U05 — Kannada Language Support

**Scope for MVP enhancement:** English (default) and Kannada. Future: Hindi, Tamil, Telugu.

**Implementation approach:**
- Use Android's standard string resources system.
- Create `res/values/strings.xml` (English — already exists).
- Create `res/values-kn/strings.xml` (Kannada translations).
- All UI text must use `@string/` references — no hardcoded English strings anywhere in the app.
- App language is set programmatically (not relying solely on system locale) because many rural users have their phone in English but want the app in Kannada.

**Key strings to translate (Kannada — `values-kn/strings.xml`):**

All existing strings must have Kannada equivalents. Priority strings:

```xml
<!-- Example entries — translator fills in actual Kannada text -->
<string name="app_name">ಗ್ರಾಮ-ಯಾತ್ರಿ</string>
<string name="ping_on_bus">ನಾನು ಬಸ್‌ನಲ್ಲಿದ್ದೇನೆ</string>
<string name="ping_passed_me">ಬಸ್ ನನ್ನನ್ನು ದಾಟಿಹೋಯಿತು</string>
<string name="bus_arriving_soon">ಬಸ್ ಶೀಘ್ರದಲ್ಲೇ ಬರಲಿದೆ 🚌</string>
<string name="no_live_data">ಲೈವ್ ಮಾಹಿತಿ ಇಲ್ಲ</string>
<string name="reported_by">ವರದಿ ಮಾಡಿದವರು</string>
<string name="your_saved_stop">📍 ನಿಮ್ಮ ಉಳಿಸಿದ ನಿಲ್ದಾಣ</string>
<string name="trip_completed">ಪ್ರಯಾಣ ಮುಗಿದಿರಬಹುದು</string>
<string name="bus_may_have_reached">ಬಸ್ ತಲುಪಿರಬಹುದು</string>
<string name="post_alert">ಎಚ್ಚರಿಕೆ ಪೋಸ್ಟ್ ಮಾಡಿ</string>
<string name="select_route">ನಿಮ್ಮ ಮಾರ್ಗವನ್ನು ಆಯ್ಕೆ ಮಾಡಿ</string>
```

**Language application:**  
Use `AppCompatDelegate.setApplicationLocales()` (API 33+) with fallback to `Locale` + `Configuration` approach for older API levels.

Create a `LanguageManager.kt` utility:
```kotlin
object LanguageManager {
    fun applyLanguage(context: Context, languageCode: String) {
        // "en" for English, "kn" for Kannada
        // Use AppCompatDelegate on API 33+, Configuration on older
    }

    fun getSavedLanguage(context: Context): String {
        return SharedPrefHelper.getLanguage(context) ?: "en"
    }
}
```

Call `LanguageManager.applyLanguage()` in:
- `Application.onCreate()`
- `MainActivity.onCreate()` (before `setContentView`)

---

#### U06 — Language Selector in Profile Screen

**Profile screen** (add if not already present — or add as a settings section within the existing Profile/Name Setup):

- A `Spinner` or `RadioGroup` with two options: `English` | `ಕನ್ನಡ`
- On selection: save to `SharedPreferences` via `SharedPrefHelper.saveLanguage(code)` → call `LanguageManager.applyLanguage()` → call `recreate()` on the Activity to apply immediately.
- Current selection shown as the default/selected item when screen opens.

**SharedPreferences key:** `app_language` (String — `"en"` or `"kn"`)

---

#### U07 — Voice Assist / Read Aloud (Android TTS)

**What this is NOT:** This is NOT microphone input, speech recognition, or voice commands.  
**What this IS:** A speaker button that reads important bus status information aloud using the device's built-in Text-to-Speech engine.

**Where speaker buttons appear:**

| Screen | Location | What is read |
|---|---|---|
| Route Selection (Home) | Each route card — speaker icon (top right of card) | Route name + last ping info |
| Route Timeline (Live Tracking) | Below the saved stop header card | Full status sentence |
| Notifications / Alerts | Each alert card — speaker icon | Alert message + reporter |

**Read-aloud text format:**

For Route Timeline / Live Tracking:
```
"Bus on [Route Name] was last reported at [Stop Name] 
by [Reporter Name], [X] minutes ago. 
It may reach [Your Saved Stop Name] in approximately [Y] minutes."
```

For Route Cards on Home:
```
"[Route Name]. Last pinged [X] minutes ago."
```

For Alert Cards:
```
"Alert: [Alert Message]. Reported by [Reporter Name]."
```

**Implementation — `VoiceAssistHelper.kt`:**

```kotlin
class VoiceAssistHelper(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isReady = false

    fun initialize(onReady: () -> Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isReady = true
                onReady()
            }
        }
    }

    fun speak(text: String, languageCode: String = "en") {
        if (!isReady) return
        val locale = if (languageCode == "kn") Locale("kn", "IN") else Locale.ENGLISH
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || 
            result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Handle gracefully — see U08
            return
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
```

- Initialize in Fragment's `onViewCreated()`.
- Call `shutdown()` in `onDestroyView()`.
- Speaker button icon: `ic_volume_up` from Material Icons (vector drawable — no image files).
- Button size: 24dp icon, 48dp touch target minimum.

---

#### U08 — Kannada TTS with Graceful Fallback

**Problem:** Not all Android devices have Kannada TTS data installed. Attempting to use Kannada TTS without the language pack results in silence or an error.

**Detection logic:** After `tts.setLanguage(Locale("kn", "IN"))`, check the return value:
- `LANG_MISSING_DATA` or `LANG_NOT_SUPPORTED` → Kannada TTS not available.

**Fallback behavior:**
- Show a non-blocking `Snackbar` (not a Dialog — do not interrupt the user):
  ```
  "Kannada voice not available on this device. 
   Please update Google Text-to-Speech in Play Store."
  ```
- Fall back to English TTS for that session.
- Do NOT crash. Do NOT show an error dialog. The app continues working normally.
- Snackbar has an action button: `"Update"` → opens Play Store to the Google Text-to-Speech app page.

**Play Store deep link:**
```kotlin
val intent = Intent(Intent.ACTION_VIEW, 
    Uri.parse("market://details?id=com.google.android.tts"))
```

### Files Likely Affected

- `res/values/strings.xml` — ensure all strings use resources (audit for hardcoded strings)
- `res/values-kn/strings.xml` — **new file** — all Kannada translations
- `LanguageManager.kt` — **new file**
- `SharedPrefHelper.kt` — add `saveLanguage()`, `getLanguage()`
- `Application.kt` (or `GramaYatriApp.kt`) — call `LanguageManager.applyLanguage()` on init
- `MainActivity.kt` — apply language before `setContentView`
- `ProfileFragment.kt` (or settings section) — add language selector UI
- `VoiceAssistHelper.kt` — **new file**
- `RouteSelectionAdapter.kt` — add speaker button to route cards
- `RouteTimelineFragment.kt` — add speaker button below saved stop header card
- `NotificationsAdapter.kt` — add speaker button to alert cards
- `item_route_card.xml` — add speaker `ImageButton`
- `fragment_route_timeline.xml` — add speaker button near header card
- `item_alert_card.xml` — add speaker `ImageButton`

### Firebase Schema Changes

None. Language and TTS are fully client-side features.

### Testing Checklist

- [ ] App displays in English by default on first launch
- [ ] Switching to Kannada in Profile → all visible UI text changes to Kannada
- [ ] Language preference persists after app restart
- [ ] English TTS speaks route card status correctly when speaker button tapped
- [ ] English TTS speaks Live Tracking status correctly
- [ ] English TTS speaks alert card content correctly
- [ ] Kannada TTS works if device has Kannada language pack installed
- [ ] Snackbar with "Update" link appears when Kannada TTS is unavailable
- [ ] Tapping "Update" opens Google TTS in Play Store
- [ ] `VoiceAssistHelper.shutdown()` called in `onDestroyView()` — no memory leak
- [ ] Speaker buttons have 48dp touch targets

### Success Criteria

- A user who selects Kannada sees the entire app in Kannada text.
- Speaker buttons on all three designated screens correctly read the relevant status aloud.
- Kannada TTS failure is handled gracefully with a Snackbar — no crash, no dialog blocking the UI.
- Language selection survives app kill and restart.

---

## PHASE 8.7 — WEEKLY SCHEDULE + BUS PLATE NUMBER

### Objective

Add awareness of day-based bus schedules (some routes run differently on different days of the week) and show the bus plate number/identifier so users can recognize the correct bus at the stop.

---

### Features

#### U09 — Weekly Schedule Per Route Per Day

**Problem:** Some rural bus routes run on different start times on different days. Monday–Saturday may have a 6:00 AM departure, but Sunday may only have an 8:00 AM departure, or no service at all.

**Data model — add to Firebase under each route:**

```json
"schedule": {
  "MON": { "departures": ["06:00", "12:30", "17:00"], "active": true },
  "TUE": { "departures": ["06:00", "12:30", "17:00"], "active": true },
  "WED": { "departures": ["06:00", "12:30", "17:00"], "active": true },
  "THU": { "departures": ["06:00", "12:30", "17:00"], "active": true },
  "FRI": { "departures": ["06:00", "12:30", "17:00"], "active": true },
  "SAT": { "departures": ["06:00", "14:00"], "active": true },
  "SUN": { "departures": ["08:00"], "active": false }
}
```

> **Note:** Only the **departure time from the first stop** is stored here. ETA handles intermediate stop timings. `active: false` means no service that day.

**Data model — Kotlin:**
```kotlin
data class DaySchedule(
    val departures: List<String>,  // e.g. ["06:00", "12:30"]
    val active: Boolean
)

data class WeeklySchedule(
    val MON: DaySchedule,
    val TUE: DaySchedule,
    val WED: DaySchedule,
    val THU: DaySchedule,
    val FRI: DaySchedule,
    val SAT: DaySchedule,
    val SUN: DaySchedule
)
```

---

#### U10 — Today's Schedule on Home Screen

**Display on Route Selection / Home screen route cards:**

Each route card should show, below the route name:
- **If today has service:** `"Today: 6:00 AM · 12:30 PM · 5:00 PM"`
- **If today has no service:** `"No service today"` (shown in grey/muted text)
- **If schedule data is not available:** Do not show anything — cards fall back to original MVP design gracefully.

Today's day of week is determined using `Calendar.getInstance().get(Calendar.DAY_OF_WEEK)`.

---

#### U11 — Tomorrow / All Days Schedule View (Optional / P2)

A simple expandable section or secondary screen accessible via a "View Full Schedule" link on the route card. Shows a table:

```
Day       | Departures
----------|------------------
Monday    | 6:00 AM, 12:30 PM, 5:00 PM
Tuesday   | 6:00 AM, 12:30 PM, 5:00 PM
...
Sunday    | No service
```

This is P2 — implement only if Phases 8.5, 8.6, and the rest of 8.7 are complete.

---

#### U12 — Bus Plate Number on Route Cards

**Firebase — add to route meta:**
```json
"meta": {
  "name": "Doddaballapur → Bengaluru",
  "totalStops": 5,
  "active": true,
  "busPlate": "KA-43 F-1021"
}
```

**Display on route cards:** A small row below the route name:
```
🚌 KA-43 F-1021
```

Use a bus icon (Material `directions_bus` vector drawable) + the plate number in 12sp grey text.  
If `busPlate` is null or empty in Firebase → do not show the row. Card layout should handle this gracefully.

---

#### U13 — Bus Plate Number on Live Tracking Header

**Display on `RouteTimelineFragment` toolbar/header:**

Below the route name in the toolbar or as a small subtitle:
```
KA-43 F-1021  ·  LIVE ●
```

If no plate number → show only the LIVE indicator. No empty space or placeholder.

---

#### U14 — Smarter Ping Rate Limiting

**Problem:** The MVP has a simple "one ping per 5 minutes" rule. This is too blunt — it blocks valid updates when the bus moves forward, and it doesn't prevent spamming the same stop multiple times.

**New rules (implemented client-side in `PingViewModel.kt`):**

| Scenario | Action |
|---|---|
| User tries to ping the SAME stop within 5 minutes of a ping from that stop | Block with message: `"This stop was already reported recently. Wait a few minutes."` |
| User tries to ping a LATER stop, even within 5 minutes of any previous ping | Allow — bus has moved forward, this is valid |
| User tries to ping an EARLIER stop when a later stop already has an active ping | Block with warning: `"A later stop was already reported. Pinging an earlier stop would confuse others."` |
| User tries to ping any stop when no active ping exists | Always allow |

**Implementation:**  
Store the last ping's `stopIndex` and `timestamp` in SharedPreferences (updated after every successful ping):
- `last_ping_stop_index` (Int)
- `last_ping_timestamp` (Long)

In `PingViewModel.submitPing()`, check these values before writing to Firebase.

**Feedback messages:** Use `Snackbar` — never block with a Dialog for rate limiting.

### Files Likely Affected

- `Route.kt` — add `schedule: WeeklySchedule?` and update `busPlate` in meta
- `DaySchedule.kt` — **new model file**
- `WeeklySchedule.kt` — **new model file**
- `RouteRepository.kt` — parse schedule and bus plate from Firebase
- `RouteViewModel.kt` — expose `todaySchedule: LiveData<DaySchedule?>` and `busPlate`
- `RouteSelectionAdapter.kt` — render today's departures and bus plate on cards
- `RouteTimelineFragment.kt` — show bus plate in header
- `PingViewModel.kt` — add smarter rate limit logic
- `SharedPrefHelper.kt` — add `saveLastPingStopIndex()`, `saveLastPingTimestamp()` etc.
- `item_route_card.xml` — add schedule row and bus plate row
- `fragment_route_timeline.xml` — add bus plate subtitle in header area
- `strings.xml` + `values-kn/strings.xml` — add schedule-related strings

### Firebase Schema Changes

**Add to each route's `meta` node:**
```json
"busPlate": "KA-43 F-1021"
```

**Add new `schedule` node under each route:**
```json
"schedule": {
  "MON": { "departures": ["06:00", "12:30", "17:00"], "active": true },
  "TUE": { "departures": ["06:00", "12:30", "17:00"], "active": true },
  "WED": { "departures": ["06:00", "12:30", "17:00"], "active": true },
  "THU": { "departures": ["06:00", "12:30", "17:00"], "active": true },
  "FRI": { "departures": ["06:00", "12:30", "17:00"], "active": true },
  "SAT": { "departures": ["06:00", "14:00"], "active": true },
  "SUN": { "departures": ["08:00"], "active": false }
}
```

> Existing `stops`, `etas`, `pings`, and `alerts` nodes are **not affected**.

### Testing Checklist

- [ ] Today's departure times shown correctly on route cards (e.g. Monday shows Monday schedule)
- [ ] "No service today" shown in grey when `active: false`
- [ ] Route cards show no schedule row if schedule node is missing from Firebase (graceful fallback)
- [ ] Bus plate `KA-43 F-1021` appears on route card below route name
- [ ] Bus plate appears in Live Tracking header
- [ ] Bus plate row hidden if `busPlate` is null or empty
- [ ] Pinging same stop within 5 minutes shows Snackbar block
- [ ] Pinging a later stop within 5 minutes is allowed
- [ ] Pinging an earlier stop when a later active ping exists shows warning Snackbar
- [ ] Rate limit state resets after 5 minutes

### Success Criteria

- Route cards clearly show today's bus departure times without cluttering the card.
- Bus plate number visible on both the home card and live tracking header.
- Smart ping rules prevent spam while allowing realistic bus movement pings.
- No crashes when schedule data is absent from Firebase.

---

## PHASE 8.8 — LOW-DATA UI POLISH

### Objective

Improve the visual quality and usability of all screens without adding any data weight. The app must remain fast on 2G and low-end phones (512MB RAM, Android 6.0+). No maps, no remote images, no heavy animations.

---

### Features

#### U15 — Splash Screen Polish

**Current:** Basic logo + name.  
**Target:** Slightly more refined. Same data weight (zero network calls on splash).

- Centered layout with the bus vector illustration (custom SVG/XML vector drawable — no PNG/JPEG).
- App name `"Grama-Yatri"` in bold, Primary Green (`#2D6A4F`), 28sp.
- Tagline `"Community Bus Tracker"` in 14sp grey.
- A thin animated horizontal progress bar (indeterminate, Material style) at the bottom — shows while checking SharedPreferences.
- No splash video, no Lottie animations, no remote images.

---

#### U16 — Home Route Card Redesign

**Current:** Simple RecyclerView rows.  
**Target:** Material `CardView` with consistent elevation, padding, and information hierarchy.

**Card layout structure (top to bottom):**
```
┌─────────────────────────────────────────┐
│ 🚌 KA-43 F-1021            [speaker 🔊] │
│ Doddaballapur → Bengaluru               │
│ 12 stops                                │
│ ─────────────────────────────────────── │
│ Today: 6:00 AM · 12:30 PM · 5:00 PM    │
│ Last pinged: 8 min ago         LIVE ●   │
└─────────────────────────────────────────┘
```

- Card elevation: `2dp`
- Card corner radius: `8dp`
- Internal padding: `16dp`
- Route name: `16sp Medium`, Primary Green
- Bus plate + stop count: `12sp Regular`, grey
- Schedule row: `12sp Regular`, dark grey
- Last ping row: `12sp Regular`, grey with green LIVE dot if < 60 min
- No route thumbnail image — vector bus icon only

---

#### U17 — Live Tracking Vertical Stepper Polish

**Current:** Basic RecyclerView with a custom item decoration line.  
**Target:** Cleaner visual hierarchy. Same performance.

- Circle node diameter: `16dp`
- Connecting line width: `2dp`
- Line color: `#BDBDBD` (grey) for all stops; `#2D6A4F` (green) for stops the bus has already passed
- Active stop circle: `24dp`, filled green, with a subtle ripple on tap
- Stop name: `15sp Medium`
- ETA text: `18sp Bold`, Primary Green — largest text in the row, glanceable
- Reporter attribution: `11sp Regular`, `#9E9E9E` grey — subdued, not competing with ETA
- Confidence badge: small `TextView` with rounded background — inline with attribution
- Row divider: no hard dividers — use vertical padding (`12dp` top + bottom) for breathing room

---

#### U18 — Ping Bottom Sheet Polish

**Current:** Basic bottom sheet with two buttons.  
**Target:** Cleaner, more confidence-inspiring design.

- Sheet handle: standard Material handle bar, `4dp` height, `32dp` wide, grey, centered
- Title: `"Report Bus Location"` — `17sp Bold`, centered
- Reporter label: `"Reporting as: Ravi K."` — `13sp`, grey, centered, below title
- Stop selector: Material `TextInputLayout` with dropdown style (`ExposedDropdownMenu`) — no plain `Spinner`
- Two buttons — full width, rounded corners (`8dp`), `52dp` height:
  - `"I AM ON THE BUS"` — filled, Primary Green background, white text, `🚌` icon left
  - `"BUS JUST PASSED ME"` — outlined, Terracotta border, Terracotta text, `👋` icon left
- Loading state: replace button text with `CircularProgressIndicator` (small, inline) during Firebase write
- Gap between buttons: `12dp`
- Bottom padding: `24dp` (safe area for gesture navigation bars)

---

#### U19 — Alerts Card Redesign

**Current:** Basic list rows for alerts.  
**Target:** Distinct card design that communicates urgency clearly.

- `CardView` with `4dp` left border accent:
  - ACTIVE alert: `#E76F51` (Terracotta) left border
  - RESOLVED alert: `#40916C` (Green) left border
- Alert icon: `⚠️` for ACTIVE, `✅` for RESOLVED — Material vector icons
- Alert message: `14sp Medium`, dark text
- Reporter + timestamp: `11sp Regular`, grey
- Status badge (top right of card): small rounded chip — `"ACTIVE"` red or `"RESOLVED"` green
- Speaker button: `🔊` icon, 48dp touch target, top-right after badge
- Resolved alerts visually muted (0.7 alpha on card) — still visible but clearly secondary

---

#### U20 — Profile Screen Layout Polish

**If a Profile / Settings screen doesn't yet exist, create one accessible from the Route Timeline toolbar menu.**

**Profile screen sections:**

```
┌─────────────────────────────────────────┐
│  👤  Your Profile                       │
├─────────────────────────────────────────┤
│  Display Name        [Ravi K.]  [Edit]  │
│  Home Stop           [Doddaballapur]    │
├─────────────────────────────────────────┤
│  App Language        [ English ▾ ]      │
├─────────────────────────────────────────┤
│  About Grama-Yatri                      │
│  Version 1.0 · MindMatrix Internship    │
└─────────────────────────────────────────┘
```

- Display Name: tappable → `AlertDialog` with an `EditText` to update name → saves to SharedPreferences.
- Home Stop: tappable → opens a stop selector `AlertDialog` with all stops from the selected route → saves to SharedPreferences.
- Language selector: `Spinner` / `AutoCompleteTextView` dropdown → applies immediately via `LanguageManager`.
- No heavy graphics. Clean `ConstraintLayout` with `MaterialDivider` between sections.

### Files Likely Affected

- `activity_splash.xml` — redesign splash layout
- `item_route_card.xml` — full redesign per U16 spec
- `item_timeline_stop.xml` — stepper node and row polish per U17 spec
- `fragment_ping_bottom_sheet.xml` — full redesign per U18 spec
- `item_alert_card.xml` — full redesign per U19 spec
- `fragment_profile.xml` — **new file** (or `activity_profile.xml`)
- `ProfileFragment.kt` — **new file** (or update if exists)
- `TimelineItemDecoration.kt` — update line colors for past vs future segments
- `colors.xml` — verify all color values defined and consistent
- `dimens.xml` — define shared dimension constants (card radius, padding, elevation)
- `strings.xml` + `values-kn/strings.xml` — add any new UI text

### Firebase Schema Changes

None. All Phase 8.8 changes are purely UI/client-side.

### Testing Checklist

- [ ] Splash screen shows vector illustration, app name, tagline, and progress bar
- [ ] Splash does not make any network call
- [ ] Route cards use CardView with elevation and correct layout hierarchy
- [ ] Bus plate and schedule visible on cards (requires Phase 8.7 data)
- [ ] Timeline stepper has correct node sizes, line widths, and color states
- [ ] ETA text is the largest text in each timeline row
- [ ] Ping bottom sheet uses ExposedDropdownMenu for stop selector
- [ ] Both ping buttons are full-width with icons
- [ ] Loading spinner replaces button text during Firebase write
- [ ] Alert cards show left border, status badge, and speaker button
- [ ] ACTIVE alerts use Terracotta color, RESOLVED use Green
- [ ] Profile screen accessible from toolbar menu
- [ ] Display name editable and change persists
- [ ] Home stop selectable from route stops list
- [ ] All screens tested on a 5-inch screen (480×854 dp) — no overflow, no clipped elements
- [ ] All screens tested with Kannada text — layouts accommodate longer text gracefully

### Success Criteria

- App visually feels polished and purposeful — not a student project, a real tool.
- No layout overflow or clipping on small screens.
- APK size remains under 5 MB after all UI changes.
- Cold start time remains under 3 seconds on a low-end device.
- Zero remote images or Lottie animations anywhere.

---

## PHASE 10 — FINAL QA & DEMO PREPARATION

### Objective

Full end-to-end testing of all original MVP features plus all enhancement features. Prepare the app for the MindMatrix internship demo and evaluation.

---

### Pre-Demo Checklist

#### Firebase Seed Data Verification

Before the demo, manually verify in Firebase Console that the following data exists:

- [ ] At least **2 routes** are present under `/routes/`
- [ ] Each route has **5 stops** with correct `avgTimeToNextMin` values
- [ ] Each route has `busPlate` set in `meta`
- [ ] Each route has a `schedule` node with at least MON–SAT entries
- [ ] At least **1 pre-seeded ping** exists per route with a recent timestamp (update manually right before demo)
- [ ] At least **1 active alert** exists on one route for demo purposes
- [ ] Firebase Security Rules are set to the MVP open rules from the PRD

#### Device Setup

- [ ] Demo device 1 (primary): real Android phone (Redmi or Samsung budget)
  - App installed from APK
  - Route selected, user name set to `"Demo User"`
  - Home stop set to the 3rd stop on the demo route
- [ ] Demo device 2 (secondary): Android emulator in Android Studio
  - Same app, different user name: `"Ravi K."`
  - Same route selected

#### Two-Device Live Demo Flow

1. Open app on **Device 1** (primary phone). Show Route Timeline — "No Live Data" state.
2. On **Device 2** (emulator), open Ping Bottom Sheet → select Stop 2 → tap "I AM ON THE BUS".
3. Watch **Device 1** update ETAs in real-time within 2 seconds. Highlight "Reported by Ravi K." attribution.
4. Show saved stop header card on Device 1 — "Your stop: [Stop Name] · ~14 min".
5. Post a cancellation alert from Device 2 → show red banner appears on Device 1 instantly.
6. Show Notifications screen on Device 1.
7. Demonstrate language switch to Kannada on Device 1 → show UI in Kannada.
8. Tap speaker button → TTS reads status aloud.

#### Feature Coverage Checklist

**Original MVP (Phases 0–7):**
- [ ] Route Selection loads from Firebase
- [ ] Route Timeline shows all stops with ETA
- [ ] Ping Bottom Sheet submits ping and ETA updates on second device
- [ ] Reporter attribution shows on every ETA
- [ ] Cancellation alert posts and appears as red banner
- [ ] Notifications screen shows alert history
- [ ] Offline fallback shows cached data with "No live data" banner
- [ ] Swipe to refresh works

**Enhancement Features (Phases 8.5–8.8):**
- [ ] No "~0 min" shown anywhere
- [ ] Confidence badge shows on active ping stop
- [ ] Saved stop highlighted with green tint in timeline
- [ ] Saved stop header card shows correct ETA
- [ ] App language switches between English and Kannada
- [ ] Speaker button reads status aloud (English)
- [ ] Today's schedule visible on route cards
- [ ] Bus plate number visible on cards and Live Tracking header
- [ ] Smart ping rate limiting works (same-stop block, later-stop allow)
- [ ] All screens visually polished per Phase 8.8 specs

#### Performance Checks

- [ ] APK size: run `./gradlew assembleRelease` → verify output < 5 MB
- [ ] Cold start: time from tap to Route Timeline visible < 3 seconds on physical device
- [ ] Firebase ETA propagation: ping → second device update < 2 seconds
- [ ] Offline mode: airplane mode ON → app shows cached ETAs < 1 second

#### Code Quality

- [ ] No hardcoded strings in any Kotlin or XML file — all use `@string/` resources
- [ ] All Firebase listeners removed in `onDestroyView()` — no listener leaks
- [ ] `VoiceAssistHelper.shutdown()` called in all relevant `onDestroyView()` methods
- [ ] No `TODO` comments remaining in any P0 feature code
- [ ] `README.md` updated with full setup instructions including Firebase seeding steps

### Success Criteria

- Full demo runs for 5 minutes without a crash.
- Two-device ETA update demo works reliably on the first attempt.
- Evaluators can clearly see: live data, attribution, alerts, language switch, voice assist, weekly schedule, and bus plate number.
- App feels like a finished product, not a prototype.

---

## FUTURE ENHANCEMENTS (Planned — Not in Current Build)

These features are acknowledged and logged here for future reference. They must NOT be implemented during the current internship build.

| # | Feature | Description | Reason Deferred |
|---|---|---|---|
| FE01 | FCM via Cloud Functions | Trigger FCM push notifications automatically when an alert is posted to Firebase, without manual Firebase Console intervention | Requires Firebase Blaze plan (paid) or Cloud Functions setup beyond internship scope |
| FE02 | Gemini / GenAI Travel Time Refinement | Nightly job that analyses historical ping timestamps and refines `avgTimeToNextMin` values using ML | Requires historical data volume and GenAI API integration beyond MVP |
| FE03 | Admin Dashboard | Web or Android screen for route coordinators to add/edit routes, stops, and schedules without touching Firebase Console | Significant scope — separate project phase |
| FE04 | Community Ping Confirmation | Allow users to "confirm" or "dispute" an existing ping — builds reliability scoring system | Requires user identity layer beyond display names |
| FE05 | Offline Cache Improvements | Smart cache invalidation, route-level cache expiry, background sync worker | Adds complexity — Room DB offline fallback in MVP is sufficient for demo |
| FE06 | Hindi / Tamil / Telugu Support | Extend multilingual support beyond English and Kannada | Requires translation resources and additional QA |
| FE07 | WhatsApp ETA Share | Share current ETA as a WhatsApp message for non-app users | Requires `Intent.ACTION_SEND` integration and WhatsApp API policies review |
| FE08 | Audio Alerts for Approaching Bus | Automatic TTS announcement when bus ETA for saved stop drops below 5 minutes | Requires background service or WorkManager + foreground notification complexity |

---

## APPENDIX — SHARED PREFERENCES KEYS (Updated)

| Key | Type | Set In | Purpose |
|---|---|---|---|
| `user_name` | String | Name Setup | Display name for ping attribution |
| `selected_route_id` | String | Route Selection | Auto-navigate to last route on launch |
| `home_stop_id` | String | Profile Screen | Pre-select stop in Ping Bottom Sheet + save stop highlight |
| `fcm_subscribed_route` | String | Route Selection | Track active FCM topic subscription |
| `app_language` | String | Profile Screen | `"en"` or `"kn"` — selected app language |
| `last_ping_stop_index` | Int | Ping Submit | Smart rate limiting — last pinged stop index |
| `last_ping_timestamp` | Long | Ping Submit | Smart rate limiting — last ping time |

---

## APPENDIX — COLOR REFERENCE (Complete)

| Token | Hex | Usage |
|---|---|---|
| Primary Green | `#2D6A4F` | Route names, active elements, primary brand |
| Accent Green | `#52B788` | FAB, primary CTA buttons, LIVE indicator background |
| Live Pulse Green | `#40916C` | Pulsing LIVE dot, filled stop node, past segment line |
| Warning Terracotta | `#E76F51` | Alerts, cancellations, "Bus Passed Me" button |
| High Confidence | `#40916C` | Confidence badge — high |
| Medium Confidence | `#F4A261` | Confidence badge — medium |
| Low Confidence | `#E76F51` | Confidence badge — low |
| Stale Report | `#9E9E9E` | Confidence badge — stale |
| Background | `#FFFFFF` | Screen background |
| Card Background | `#F5F5F5` | Card and surface backgrounds |
| Text Primary | `#1B1B1B` | Stop names, route names, primary content |
| Text Secondary | `#6B7280` | Attribution, timestamps, helper text |
| Text Disabled | `#BDBDBD` | Muted / stale content |
| Timeline Line | `#BDBDBD` | Future stop connecting line |
| Timeline Line Past | `#40916C` | Past stop connecting line (bus already passed) |

---

*This document is a companion to `GRAMAYATRI_PRD_COMPLETE.md` and does not replace it.*  
*All enhancements in this document assume Phases 0–7 of the original PRD are fully implemented.*  
*Prepared by: Mohammed Fawwaz Sadath (1BY22EC054) | BMS Institute of Technology | MindMatrix Internship 2026*
