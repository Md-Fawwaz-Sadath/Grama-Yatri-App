# Internship Review Preparation: Grama-Yatri Android Project

## 1. Project Title

**Grama-Yatri: Community-Powered Rural Bus Tracking App**

Grama-Yatri is an Android application built to help rural passengers track village buses using community reports instead of GPS hardware. The app focuses on real-time bus status, estimated arrival times, route alerts, Kannada support, voice assist, and low-data usage.

## 2. One-Minute Explanation

If the reviewer asks, "Tell me about your project", I can say:

Grama-Yatri is a community-powered rural bus tracking Android app. In many villages, buses do not have proper live tracking, and passengers often wait for hours without knowing whether the bus has already passed or is delayed. My app solves this by allowing passengers to report, or "ping", the bus when they see it or board it. That ping is stored in Firebase Realtime Database, and all users on that route immediately see updated bus status and estimated arrival times for downstream stops. The app also supports alerts for delays or cancellations, weekly schedules, bus operator and bus number details, Kannada language support, voice read-aloud, and a low-data UI suitable for rural users and low-end phones.

## 3. Problem Statement

Village bus timings are often unpredictable. Students, daily wage workers, and village commuters may reach the stop early and wait for a long time, or they may miss the bus by a few minutes and lose 2 to 3 hours waiting for the next one.

GPS-based live tracking is not always practical for small village buses because it requires hardware installation, operator cooperation, continuous connectivity, and maintenance. Many rural routes do not have formal digital tracking systems.

Grama-Yatri solves this using a community-powered model. Instead of depending on GPS hardware, the passengers themselves become the live data source. When someone sees or boards the bus, they report its current stop. The app then updates all other users with the latest bus position and estimated arrival time.

## 4. Objective

The main objectives of Grama-Yatri are:

- Provide a simple list of rural bus routes.
- Allow passengers to report bus location using a "Ping Bus" action.
- Calculate ETA using stop order and average travel time between stops.
- Show live bus status to all users on the selected route.
- Allow users to report delay, cancellation, or general route alerts.
- Save a user's preferred stop for quick reference.
- Support Kannada language for regional accessibility.
- Add Text-to-Speech voice assist for users who prefer listening.
- Keep the app simple, low-data, and usable on low-end Android phones.

## 5. Target Users

The target users are:

- Rural passengers who depend on village buses.
- Students travelling to schools or colleges.
- Daily wage workers who cannot afford long waiting times.
- Village commuters travelling to nearby towns or cities.
- Bus operators or route admins as future users for managing route data.

## 6. Core Idea / Working Concept

The core idea is that the app does not require installed GPS hardware on the bus.

Instead:

1. A user sees or boards the bus at a stop.
2. The user taps "Ping Bus".
3. The app writes the ping to Firebase Realtime Database under the selected route.
4. Other users listening to the same route receive the update in real time.
5. The app calculates ETA for upcoming stops based on average travel time.
6. Previous stops are marked as passed, the current stop shows bus presence, and future stops show estimated arrival.
7. Users can also report route alerts like delay, cancellation, or information.

This makes the system useful even without official transport data.

## 7. Major Features Implemented

### Splash and Onboarding

The app starts with a splash screen and checks whether the user is launching the app for the first time. On first launch, onboarding explains the purpose of the app and collects an optional display name.

### Profile and Display Name

The Profile screen allows the user to save or edit a display name. This name is shown when the user submits pings or alerts, so other users know who reported the update.

### Preferred Stop Saving

The user can select a preferred route and stop. This saved stop is shown in the Live Tracking screen, highlighted in the timeline, and used to show a quick ETA for the user's own stop.

### Home Route List

The Home screen reads route data from Firebase Realtime Database and displays available bus routes as cards.

### Today / Tomorrow / All Filters

The Home screen has filters for Today, Tomorrow, and All. These filters help users quickly see routes that have schedule data for the selected day.

### Weekly Schedule

Each route can include weekly schedule data. The app shows today's start times on route cards and in the Live Tracking header.

### Bus Operator and Bus Number

Routes can include operator and bus number fields, such as KSRTC and a plate number. This helps passengers identify the correct bus.

### Live Tracking Timeline

When the user opens a route, the app shows all stops in order using a vertical timeline. Each stop displays status, ETA, reporter information, and saved stop highlight if applicable.

### ETA Calculation

ETA is calculated using the bus ping stop, current time, stop order, and average travel time between stops.

### Ping Bus Flow

The Ping Bottom Sheet lets users select a stop and report either:

- I am on the bus.
- Bus just passed me.

The ping is written to Firebase and live ETA updates automatically.

### Smart Ping Rate Limit

The app prevents repeated same-stop pings within 5 minutes. It allows movement-forward pings where the bus has reached a later stop, and blocks earlier-stop pings after a later active report.

### Reporter / Source Display

Pings and alerts include reporter name and timestamp. This improves trust because users can see who reported the latest information and how recent it is.

### Alerts Reporting

Users can report route alerts such as cancellation, delay, or general information.

### Alerts Display

The Alerts screen displays reported alerts from Firebase, grouped by route filter, with reason, note, reporter, and time.

### Kannada Language Support

The app supports English and Kannada using Android string resources and a language selector in Profile.

### Voice Assist / Text-to-Speech

Speaker buttons read important route tracking and alert information aloud using Android's device TextToSpeech API. This is useful for low-literacy users or users who prefer audio guidance.

### Low-Data UI Polish

The UI uses XML layouts, vector icons, text-based cards, and no heavy maps or remote images. This keeps the app fast and low-data.

### Error and Loading Handling

The app includes loading states, empty states, and friendly error messages for route, tracking, profile, and alert screens.

## 8. Feature Explanation in Layman Terms

### Splash and Onboarding

"When the app opens for the first time, it quickly explains how the app works and asks for a name. This name is used when the user helps others by reporting bus information."

### Profile

"Profile is where the user can save their name, language, and regular bus stop."

### Preferred Stop

"If a user usually boards at Hebbal, they can save Hebbal as their stop. Then the app highlights it in the tracking screen."

### Home Route List

"This is like the main bus route list. The user chooses the route they want to track."

### Schedule Filters

"Today, Tomorrow, and All help the user quickly check which routes are running based on available schedule data."

### Weekly Schedule

"The app can show when a bus starts on each day of the week."

### Bus Operator and Number

"This helps the passenger identify whether the bus is KSRTC, private, or another type, and also shows the bus number."

### Live Tracking

"This screen shows all stops in order and tells where the bus was last reported."

### ETA Calculation

"If someone reports the bus at Stop 2, the app estimates when it may reach Stop 3, Stop 4, and so on."

### Ping Bus

"Ping Bus means reporting the current bus location for everyone on that route."

### Smart Ping Limit

"The app avoids repeated reports from the same stop so that one user cannot spam wrong updates."

### Alerts

"If the bus is cancelled, delayed, or there is important route information, users can report it and others can see it."

### Kannada

"The app supports Kannada so rural Karnataka users can understand it better."

### Voice Assist

"The speaker button reads bus status aloud, which helps users who cannot read comfortably."

### Low-Data Design

"The app avoids maps, videos, and heavy images. It only uses simple text, icons, and Firebase data."

## 9. Technical Stack

The project uses:

- Android native app development.
- Kotlin as the programming language.
- XML layouts for UI.
- MVVM architecture.
- Firebase Realtime Database for live routes, pings, and alerts.
- Firebase Anonymous Authentication for simple user identity.
- Hilt for dependency injection.
- KSP for annotation processing support in the modern build setup.
- RecyclerView for route cards, stop timeline, and alerts list.
- Jetpack Navigation Component for single-activity navigation.
- SharedPreferences for local user settings like name, language, saved stop, and ping timestamps.
- Android TextToSpeech API for Voice Assist.
- Material Components for buttons, cards, toolbar, bottom navigation, and bottom sheet UI.
- Kotlin Coroutines and Flow for asynchronous Firebase listeners.

## 10. Why Firebase Realtime Database?

I chose Firebase Realtime Database because the main use case is live updates. When one passenger sends a ping, other passengers should see the update quickly.

Firebase Realtime Database is suitable because:

- It supports real-time listeners.
- Data is stored as simple JSON.
- It is easy to structure by route ID.
- It works well for live pings and alerts.
- It reduces backend complexity for a prototype.
- It supports multiple users receiving changes almost instantly.

For this project, the data is simple and route-based, so Realtime Database fits better than building a full custom backend.

## 11. Firebase Database Structure

The main Firebase paths are:

### `/routes`

Stores route information.

Example fields:

- `id`
- `name`
- `totalStops`
- `stops`
- `stopOrder`
- `attToNextMin`
- `busOperator`
- `busNumber`
- `weeklySchedule`

### `/pings/{routeId}`

Stores bus location reports for a route.

Example fields:

- `stopId`
- `stopName`
- `stopOrder`
- `type`
- `reporterName`
- `reporterUid`
- `timestamp`
- `isActive`

### `/alerts/{routeId}`

Stores route alerts.

Example fields:

- `type`
- `reason`
- `note`
- `reporterName`
- `reporterUid`
- `timestamp`
- `routeId`
- `routeName`

### Local user preferences

User preferences are stored locally using SharedPreferences, not mainly in Firebase. Examples:

- Display name.
- Saved route ID.
- Saved stop ID.
- Saved stop name.
- Selected language.
- Last ping timestamp for rate limiting.

## 12. Architecture Explanation

The app follows MVVM architecture.

### UI Layer

The UI layer contains Fragments and Adapters.

Examples:

- `HomeFragment`
- `LiveTrackingFragment`
- `PingBottomSheet`
- `AlertsFragment`
- `ReportAlertFragment`
- `ProfileFragment`
- `RouteAdapter`
- `StopTimelineAdapter`
- `AlertAdapter`

The UI layer displays data and handles user interaction.

### ViewModel Layer

ViewModels prepare data for the UI and manage screen state.

Examples:

- `HomeViewModel`
- `LiveTrackingViewModel`
- `AlertsViewModel`

They expose UI state using Kotlin Flow or StateFlow.

### Repository Layer

The repository hides data source details from the UI.

Examples:

- `FirebaseRepository`
- `UserPrefsRepository`

The UI does not directly talk to Firebase. It calls repository functions.

### FirebaseDataSource

`FirebaseDataSource` handles direct Firebase Realtime Database communication, including reading routes, reading latest pings, submitting pings, reading alerts, and submitting alerts.

### Utility Classes

Utility classes contain reusable logic:

- `EtaCalculator` for ETA calculation.
- `ScheduleUtils` for schedule display and day filtering.
- `TimeUtils` for "minutes ago" formatting and confidence.
- `TtsManager` for Text-to-Speech.
- `SpeechTextUtils` for cleaner spoken text.
- `LocaleHelper` for language support.

### Hilt

Hilt provides dependencies like Firebase database, Firebase auth, repositories, and data sources. It reduces manual object creation and keeps the code easier to maintain.

## 13. App Flow

The normal app flow is:

1. App launches and shows Splash.
2. Splash checks whether this is the first launch.
3. If first launch, user sees Onboarding.
4. User enters optional display name.
5. App signs in anonymously using Firebase Auth.
6. User reaches Home screen.
7. Home loads routes from Firebase.
8. User selects a route.
9. Live Tracking screen shows all route stops.
10. If no ping exists, stops show no recent report.
11. User taps Ping Bus.
12. User selects stop and report type.
13. Ping is written to Firebase.
14. Latest ping listener receives update.
15. ETA is recalculated and displayed.
16. User can open Alerts screen.
17. User can report delay, cancellation, or info alert.
18. Alert appears in the alerts list.
19. User can save display name, preferred route, preferred stop, and language in Profile.
20. User can switch to Kannada.
21. User can use speaker buttons to read route status or alert content aloud.

## 14. ETA Calculation Logic

Each route has stops in order. Each stop has an average travel time to the next stop, stored as `attToNextMin`.

When a user pings the bus at a stop:

- Stops before the pinged stop are marked as passed.
- The pinged stop shows "Bus is here now".
- Future stops calculate ETA using cumulative average travel time.
- The app subtracts the time already passed since the ping.
- If ETA becomes zero or negative for a future stop, the app shows "Bus may have reached".
- If ETA is zero or negative for the final stop, the app shows "Trip likely completed".
- If there is no recent ping, the app shows an unknown or estimated state.

Confidence depends on ping age:

- Less than 5 minutes: High confidence.
- 5 to 15 minutes: Medium confidence.
- 15 to 30 minutes: Low confidence.
- More than 30 minutes: Stale report.

Simple explanation:

"The app knows the stop order and average travel time. Once someone reports the bus at one stop, the app estimates arrival times for all later stops."

## 15. Smart Ping Logic

The smart ping logic is used to keep the data reliable.

Rules:

- Same stop cannot be repeatedly pinged within 5 minutes.
- A later stop ping can be allowed because the bus may have moved forward.
- An earlier stop ping after a later active ping can be blocked or warned.
- The app stores last ping timing locally for rate limiting.

This prevents spam and reduces incorrect updates.

## 16. Alerts Logic

Users can report:

- Delay.
- Cancellation.
- General route information.

When an alert is submitted:

1. User selects route.
2. User selects alert type.
3. User adds reason or note.
4. App creates a `BusAlert` object.
5. App writes it to `/alerts/{routeId}` in Firebase.
6. Alerts screen listens to Firebase and displays the alert.

Push notifications are not implemented yet because automatic background alert broadcasting through Cloud Functions generally requires Firebase Blaze billing setup. For the current version, alerts are visible inside the app in real time.

## 17. Kannada + Voice Assist Explanation

The app supports Kannada because rural Karnataka users may not be comfortable with English.

Implementation:

- Static UI text is moved to Android string resources.
- English strings are in `res/values/strings.xml`.
- Kannada strings are in `res/values-kn/strings.xml`.
- Profile screen has a language selector.
- Selected language is saved locally.
- The app applies the selected locale.

Voice Assist:

- The app uses Android's device TextToSpeech API.
- It does not use cloud TTS.
- Live Tracking speaker reads route status, latest report, confidence, saved stop ETA, schedule, operator, and bus number.
- Alerts speaker reads alert type, route name, reason, note, reporter, and time.
- Kannada voice depends on whether the device has Kannada TTS support installed.

Simple explanation:

"I added Kannada UI and speaker read-aloud because not every rural user is comfortable reading English. The app can speak important bus information aloud using the phone's built-in speech engine."

## 18. Low-Data Design Justification

The project is designed for low-end devices and slow networks.

Low-data choices:

- No maps.
- No live GPS stream.
- No remote images.
- No video or heavy animation.
- Vector icons only.
- Simple XML layouts.
- Firebase JSON data only.
- Route cards and timelines are text-based.
- Realtime listeners are focused on required route data.

This makes the app suitable for rural users with limited internet connectivity.

## 19. Screens Explanation

### Splash

The Splash screen shows the app name and checks whether the user has completed onboarding.

### Onboarding

Onboarding explains how the app works and collects an optional display name. It also signs in the user anonymously.

### Home

Home displays all available routes, bus operator, bus number, schedule, and filter options for Today, Tomorrow, and All.

### Live Tracking

Live Tracking shows the selected route's stops in order, latest ping status, ETA, confidence, saved stop highlight, and a Ping Bus action.

### Ping Bottom Sheet

This bottom sheet lets the user select a stop and submit whether they are on the bus or the bus just passed them.

### Alerts

Alerts screen displays reported route alerts with type, route, message, reporter, time, and read-aloud speaker.

### Report Alert

Report Alert lets users submit cancellation, delay, or information alerts.

### Profile

Profile lets users save display name, app language, preferred route, and preferred stop.

## 20. Demo Script

A practical demo flow:

1. Open the app.
2. If onboarding appears, explain that first-time users can enter their display name.
3. Go to Profile and show saved name, language selector, and preferred stop.
4. Go to Home and show route cards.
5. Explain bus operator, bus number, schedule, and Today / Tomorrow / All filters.
6. Tap a route card.
7. Show Live Tracking timeline.
8. Explain no recent ping or latest ping state.
9. Tap Ping Bus.
10. Select a stop.
11. Tap "I AM ON THE BUS" or "BUS JUST PASSED ME".
12. Show that ETA updates in Live Tracking.
13. If possible, open Firebase Console and show `/pings/{routeId}` update.
14. Go to Alerts.
15. Tap Report Alert.
16. Submit a delay or info alert.
17. Show alert appears in Alerts screen.
18. Tap speaker button on Live Tracking or Alerts.
19. Switch language to Kannada from Profile.
20. Show UI changes to Kannada.
21. End by explaining limitations and future scope.

## 21. What to Say During Demo

Use simple, confident lines like these:

- "This app is designed for rural bus routes where GPS tracking is not available."
- "The main idea is that passengers become the live data source."
- "When someone sees or boards the bus, they tap Ping Bus."
- "This ping goes to Firebase Realtime Database, and other users receive the update instantly."
- "The ETA is calculated based on stop order and average travel time between stops."
- "I added a rate limit so the same stop cannot be repeatedly reported within 5 minutes."
- "This screen shows all stops in order, with current, passed, upcoming, and unknown states."
- "The saved stop is highlighted so the user can quickly identify their own stop."
- "Alerts allow users to report cancellation, delay, or route information."
- "Kannada and voice assist make the app more accessible for rural users."
- "The app avoids maps and heavy images to keep data usage low."
- "Future improvements include push notifications, admin dashboard, and better offline cache."

## 22. Common Viva / Review Questions and Answers

### 1. Why did you choose this project?

I chose this project because rural bus uncertainty is a real practical problem. Many students and workers depend on buses but do not have live tracking. I wanted to build a simple solution that works without GPS hardware.

### 2. What problem does Grama-Yatri solve?

It helps rural passengers know where the bus was last reported, estimate when it may reach their stop, and see alerts for delay or cancellation.

### 3. Is the app GPS based?

No. The current app is not GPS based. It is community-powered. Users manually report the bus location using Ping Bus.

### 4. Why not use GPS?

GPS requires hardware or driver/operator participation. Small village buses may not have tracking devices. A community-powered model is easier to deploy for a prototype.

### 5. Why Firebase?

Firebase gives real-time updates, anonymous auth, and a simple backend setup. It is suitable for live bus pings and alerts.

### 6. Why Realtime Database and not Firestore?

Realtime Database is simple JSON and works well for low-latency live listeners. My data is route-based and tree-structured, so Realtime Database is a good fit.

### 7. How does ETA calculation work?

Each stop has an order and average travel time to the next stop. When a ping is received at one stop, the app sums average travel times to future stops and subtracts the time elapsed since the ping.

### 8. What happens if there is no ping?

The app shows no recent report or unknown status. It avoids showing false exact ETA when there is no live data.

### 9. What happens if ETA becomes zero?

The app does not show confusing "~0 min". It shows "Bus may have reached" or "Trip likely completed" for the final stop.

### 10. How do multiple users get updates?

Firebase Realtime Database listeners are attached to route data, pings, and alerts. When data changes, Firebase pushes the update to connected clients.

### 11. What if a user gives a wrong ping?

A wrong ping can affect ETA. To reduce this, the app shows reporter name and time, uses confidence based on ping age, and prevents repeated same-stop spam. Future scope includes confirmation and dispute systems.

### 12. How do you prevent spam?

The app blocks repeated same-stop pings within 5 minutes and blocks earlier-stop pings after a later active ping.

### 13. What is smart ping rate limiting?

It means the app prevents unrealistic repeated reports but still allows the bus to move forward. A later stop ping can be accepted because the bus may actually have moved.

### 14. What if internet is not available?

The app handles errors and shows friendly messages. Some last known UI can remain visible if already loaded, but full offline cache can be improved in future.

### 15. Why no maps?

Maps increase data usage and require accurate GPS coordinates. This app is designed for low-data rural use, so a text-based stop timeline is more practical.

### 16. What is MVVM?

MVVM separates UI, screen state, and data logic. Fragments display UI, ViewModels prepare state, and repositories handle Firebase and local data.

### 17. What is the Repository pattern?

Repository hides where data comes from. The UI does not directly call Firebase; it calls repository functions.

### 18. What is Hilt used for?

Hilt is used for dependency injection. It provides objects like FirebaseRepository, FirebaseDataSource, FirebaseAuth, and SharedPreferences wrappers.

### 19. What is KSP?

KSP is Kotlin Symbol Processing. It supports annotation processing in Kotlin builds and is used in the modern Hilt setup.

### 20. What is Firebase Anonymous Authentication?

It allows users to use the app without email or password. Firebase still gives each user a UID for writing pings and alerts.

### 21. What is SharedPreferences used for?

SharedPreferences stores local user settings such as display name, saved stop, language, and last ping timestamp.

### 22. How is Kannada support implemented?

Kannada support uses Android string resources. English strings are in `values`, Kannada translations are in `values-kn`, and the selected language is saved locally.

### 23. How does Text-to-Speech work?

The app uses Android's TextToSpeech API. It sends a short status sentence to the device TTS engine, which reads it aloud.

### 24. Does Kannada TTS always work?

No. Kannada TTS depends on whether the device has Kannada speech support installed. The app handles unavailable support with a friendly message.

### 25. Is push notification implemented?

Not yet. Alerts are visible inside the app in real time. Push notification broadcasting through Cloud Functions is part of future scope because it needs additional Firebase setup and billing.

### 26. What is the database schema?

The main paths are `/routes`, `/pings/{routeId}`, and `/alerts/{routeId}`. User preferences are mostly local.

### 27. How do you test the app?

I test route loading, route selection, live tracking, ping submission, ETA update, alerts, profile saving, language switching, and TTS. For real-time testing, two devices or an emulator and a device can be used.

### 28. What challenges did you face?

Major challenges included Gradle compatibility, Hilt/KSP setup, Firebase configuration, Realtime Database URL issues, ETA edge cases, ping spam prevention, and Kannada/TTS limitations.

### 29. What did you learn?

I learned real-time Android development, Firebase Realtime Database listeners, MVVM, dependency injection with Hilt, Kotlin Flow, localized resources, Text-to-Speech, and low-data UI design.

### 30. How is it useful for rural users?

It gives bus information without needing GPS hardware. Users can help each other by reporting bus location and alerts, which reduces waiting uncertainty.

### 31. What makes this project different from normal bus tracking apps?

Most bus tracking apps depend on GPS or official transport data. This app works through community reports, so it can be used even where official tracking does not exist.

### 32. How does weekly schedule help?

Some rural buses do not run at the same time every day. Weekly schedule helps users check today's and tomorrow's bus timings.

### 33. Why show bus number and operator?

Rural passengers may see multiple buses. Operator and bus number help identify the correct bus.

### 34. Why show reporter name?

Reporter name increases trust and accountability. Users can judge whether the latest report seems reliable.

### 35. What is the biggest limitation of the current app?

The biggest limitation is that ETA accuracy depends on community participation and correct reports. More users improve reliability.

## 23. Challenges Faced

During development, I faced several realistic challenges:

- Setting up Gradle, Kotlin, Hilt, KSP, Firebase, and Android Studio JBR compatibility correctly.
- Resolving Firebase Realtime Database URL and configuration issues.
- Handling missing route data or old ping data without crashing.
- Designing ETA logic for edge cases like zero ETA, stale reports, and trip completion.
- Preventing repeated pings while still allowing realistic forward movement.
- Supporting Kannada UI while keeping dynamic route names unchanged.
- Handling device limitations for Kannada Text-to-Speech.
- Keeping the UI low-data and simple while still making it review-ready.

I resolved these by aligning dependency versions, separating concerns using MVVM and repositories, adding defensive UI states, and keeping the Firebase schema simple.

## 24. Limitations

Current limitations:

- No background push notification yet.
- No admin dashboard yet.
- Route data is manually seeded or admin-managed.
- ETA depends on community reports.
- Wrong reports can affect accuracy.
- Kannada TTS depends on device support.
- No GPS-based tracking yet.
- Offline support is basic and can be improved with a stronger local cache.
- No report confirmation or dispute system yet.
- No driver/operator mode yet.

## 25. Future Scope

Future enhancements:

- FCM push notifications for alerts.
- Cloud Functions for alert broadcasting.
- GenAI/Gemini-based travel time refinement using historical pings.
- Admin dashboard for route and schedule management.
- Report confirmation or dispute system.
- Better offline cache.
- More languages like Hindi, Tamil, and Telugu.
- Driver/operator mode.
- QR code at bus stops for quick route opening.
- Community reliability score for reporters.
- Audio reminders when the bus is near the saved stop.

## 26. Final Conclusion

Grama-Yatri demonstrates real-time Android development with a socially useful rural transport use case. It combines Kotlin, XML UI, Firebase Realtime Database, Firebase Anonymous Auth, MVVM architecture, Hilt dependency injection, local preferences, ETA calculation, alerts, Kannada localization, Text-to-Speech, and low-data design.

The project shows that even without GPS hardware, rural bus tracking can be made practical by using community reports. It is not just a technical prototype, but a solution designed around rural users, low network conditions, regional language needs, and accessibility.

## 27. Short Version for Quick Revision

### 10 Bullet Summary

1. Grama-Yatri is a rural bus tracking Android app.
2. It uses community reports instead of GPS hardware.
3. Users ping the bus when they see or board it.
4. Firebase Realtime Database stores pings and alerts.
5. Live Tracking calculates ETA for downstream stops.
6. Alerts allow users to report delay, cancellation, or information.
7. Profile saves display name, language, and preferred stop.
8. Kannada language and voice assist improve accessibility.
9. The app is low-data, using no maps or heavy images.
10. Future scope includes push notifications, admin dashboard, and GenAI ETA refinement.

### 5 Strongest Points to Tell Reviewer

1. The app solves a real rural mobility problem.
2. It works without GPS hardware by using community reports.
3. Firebase Realtime Database enables live updates for multiple users.
4. ETA logic is based on stop order and average travel time.
5. Kannada and voice assist make it accessible for rural users.

### 5 Limitations / Future Scope Points

1. ETA depends on user reports.
2. Push notifications are not implemented yet.
3. Route data is currently manually managed.
4. Kannada TTS depends on device support.
5. Future versions can add admin dashboard, confirmation system, and GenAI refinement.

### 1-Minute Speech

Grama-Yatri is a community-powered rural bus tracking Android app. In many villages, passengers do not know whether the bus is coming, delayed, cancelled, or already passed. GPS tracking is not always practical for small rural buses, so my app uses passenger reports. When a user sees or boards the bus, they tap Ping Bus. The ping is stored in Firebase Realtime Database, and all users on that route get updated bus status and ETA. The ETA is calculated using stop order and average travel time between stops. The app also supports route alerts, weekly schedule, bus number, saved stop, Kannada language, voice assist, and a low-data UI.

### 3-Minute Speech

Grama-Yatri is my Android internship project focused on rural bus tracking. The problem is that village bus timings are often uncertain. Students, workers, and daily commuters may wait for hours or miss the bus by a few minutes. Traditional GPS tracking is difficult for small village buses because it needs hardware and operator support.

My solution is a community-powered tracking model. The passenger becomes the live data source. When a passenger sees or boards the bus, they use the Ping Bus feature. The app writes that report to Firebase Realtime Database under the route. Other users listening to the same route receive the update in real time. The Live Tracking screen then calculates ETA for upcoming stops using stop order and average travel time between stops.

Technically, I developed it as a native Android app using Kotlin, XML layouts, MVVM architecture, Firebase Realtime Database, Firebase Anonymous Authentication, Hilt dependency injection, RecyclerView, Navigation Component, SharedPreferences, and TextToSpeech API. The app has Home, Live Tracking, Ping Bottom Sheet, Alerts, Report Alert, Profile, Onboarding, and Splash screens.

I also added practical rural-user features like Kannada language support, speaker read-aloud, bus operator, bus number, weekly schedule, saved stop highlight, smart ping rate limiting, and low-data UI polish. The app avoids maps, heavy images, videos, and animations so it can work better on low-end phones and slow networks.

The current limitations are that push notifications and admin dashboard are not implemented yet, route data is manually managed, and ETA accuracy depends on community reports. Future improvements include FCM push notifications, Cloud Functions, GenAI-based travel time refinement, admin dashboard, report confirmation, offline cache, more languages, and driver/operator mode.
