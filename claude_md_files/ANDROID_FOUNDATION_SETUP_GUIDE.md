# Android Foundation Setup Guide

## 1. Title and Purpose

This is a reusable Android foundation setup guide for future Kotlin Android projects that use:

- Android Studio
- Kotlin
- XML layouts
- Fragment-based Navigation Component
- Firebase
- Hilt dependency injection
- KSP
- MVVM + Repository pattern

This guide is based on the final working foundation setup from the Grama-Yatri Android project. The goal is to make Phase 1 predictable in future projects: set up Gradle, XML navigation, Firebase, Hilt, KSP, package structure, and placeholder screens first, then build successfully before adding app features.

Use this file in a new project by giving it to Codex and saying:

> Follow this setup guide for Phase 1 foundation setup.

## 2. Recommended Starting Point

### Best starting point

If Android Studio offers it, start with:

- **Empty Views Activity**
- Kotlin
- XML layouts
- Single Activity

This is the cleanest starting point for XML + Fragment Navigation apps.

### If Android Studio creates a Compose project

Some Android Studio templates default to Jetpack Compose. The Grama-Yatri project started this way and was safely converted to XML + Fragment Navigation.

Safe migration approach:

1. Keep the Android project itself.
2. Stop using Compose UI in `MainActivity`.
3. Remove or ignore the default Compose `Greeting` screen.
4. Enable `viewBinding`.
5. Create `activity_main.xml`.
6. Add a `FragmentContainerView` as the NavHost.
7. Add `nav_graph.xml`.
8. Add XML placeholder fragments.
9. Remove Compose dependencies only after XML build works, if they are not needed.

Do not rewrite the whole app in Phase 1. The first goal is only: launch the app with XML navigation and placeholder screens.

## 3. Correct Package and Application Setup

Package naming matters because Firebase Android app package must match the Gradle `applicationId`.

Example rename:

- From: `com.example.myapp`
- To: `com.myproject.app`

In a modern Android project, check these places:

- `app/build.gradle.kts`
  - `namespace = "com.myproject.app"`
  - `applicationId = "com.myproject.app"`
- Kotlin package declarations
  - `package com.myproject.app`
- AndroidManifest application/activity references
  - Usually `.MainActivity` and `.AppNameApplication`
- Firebase Console Android app package name
  - Must exactly match `applicationId`
- `google-services.json`
  - Must contain the same `package_name`

Safe rename process:

1. Decide final package name before Firebase setup if possible.
2. Use Android Studio refactor tools for Kotlin package folders.
3. Update `namespace` and `applicationId`.
4. Sync Gradle.
5. Build.
6. Only then create Firebase Android app with the exact package.

If renaming touches too many files or breaks imports, keep the current package temporarily and rename before final Firebase setup.

## 4. Gradle Setup That Worked

The Grama-Yatri project builds with this version setup:

| Item | Working Version |
|---|---|
| Android Gradle Plugin | `9.0.1` |
| Kotlin | `2.0.21` |
| Hilt | `2.59.2` |
| KSP | `2.0.21-1.0.28` |
| Firebase BOM | `33.7.0` |
| Google Services Plugin | `4.4.2` |
| Navigation | `2.8.5` |
| Lifecycle ViewModel/LiveData | `2.8.7` |
| Material Components | `1.12.0` |
| Coroutines | `1.9.0` |
| RecyclerView | `1.3.2` |
| ViewPager2 | `1.1.0` |
| compileSdk | `36` with minor API level `1` |
| targetSdk | `36` |
| minSdk | `24` |

Important decisions:

- This project uses **KSP for Hilt**, not kapt.
- Earlier kapt/Hilt combinations caused Kotlin 2.x metadata errors.
- Hilt Gradle plugin, `hilt-android`, and `hilt-compiler` must use the same Hilt version.
- Do not apply the Kotlin Android plugin twice.
- Do not randomly switch Hilt versions. Pick one compatible version and use it consistently.

The root `build.gradle.kts` applies plugin aliases with `apply false`. The app module applies only the plugins it actually needs.

## 5. Version Catalog Guidance

Use `gradle/libs.versions.toml` as the single place for versions.

Clean example based on this project:

```toml
[versions]
agp = "9.0.1"
kotlin = "2.0.21"
firebaseBom = "33.7.0"
googleServices = "4.4.2"
hilt = "2.59.2"
ksp = "2.0.21-1.0.28"
navigation = "2.8.5"
lifecycle = "2.8.7"
lifecycleRuntimeKtx = "2.10.0"
recyclerview = "1.3.2"
swiperefreshlayout = "1.1.0"
material = "1.12.0"
viewpager2 = "1.1.0"
coroutines = "1.9.0"
coreKtx = "1.17.0"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }

firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-database-ktx = { group = "com.google.firebase", name = "firebase-database-ktx" }
firebase-auth-ktx = { group = "com.google.firebase", name = "firebase-auth-ktx" }
firebase-messaging-ktx = { group = "com.google.firebase", name = "firebase-messaging-ktx" }
firebase-analytics-ktx = { group = "com.google.firebase", name = "firebase-analytics-ktx" }

hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }

androidx-navigation-fragment-ktx = { group = "androidx.navigation", name = "navigation-fragment-ktx", version.ref = "navigation" }
androidx-navigation-ui-ktx = { group = "androidx.navigation", name = "navigation-ui-ktx", version.ref = "navigation" }
androidx-lifecycle-viewmodel-ktx = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version.ref = "lifecycle" }
androidx-lifecycle-livedata-ktx = { group = "androidx.lifecycle", name = "lifecycle-livedata-ktx", version.ref = "lifecycle" }
androidx-recyclerview = { group = "androidx.recyclerview", name = "recyclerview", version.ref = "recyclerview" }
androidx-swiperefreshlayout = { group = "androidx.swiperefreshlayout", name = "swiperefreshlayout" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
androidx-viewpager2 = { group = "androidx.viewpager2", name = "viewpager2", version.ref = "viewpager2" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-play-services = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-play-services", version.ref = "coroutines" }

junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
hilt-android = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

Warnings:

- Do not declare the same plugin in multiple conflicting places.
- Do not apply `org.jetbrains.kotlin.android` in both root and app module as an active plugin.
- Root file should use `apply false`; app module should apply the plugin once.
- Keep Hilt plugin and dependencies on the same version.

## 6. `app/build.gradle.kts` Pattern

Clean pattern based on the working project:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

// Place google-services.json inside app/ after creating the Firebase Android app.
// The plugin is applied only when the file exists so early Phase 1 builds do not fail.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.myproject.app"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.myproject.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.analytics.ktx)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.material)
    implementation(libs.androidx.viewpager2)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```

Why conditional Google Services helps:

- Early Phase 1 builds can succeed before Firebase is created.
- The build does not fail just because `app/google-services.json` is missing.
- After Firebase setup, place `google-services.json` in `app/` and the plugin is applied automatically.

## 7. `gradle.properties` Notes

Working project flags:

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
android.disallowKotlinSourceSets=false
```

Notes:

- `android.useAndroidX=true` is required for modern AndroidX libraries.
- `kotlin.code.style=official` is safe and standard.
- `android.nonTransitiveRClass=true` is a common modern default.
- `android.disallowKotlinSourceSets=false` may show an experimental warning:
  - `WARNING: The option setting 'android.disallowKotlinSourceSets=false' is experimental.`
  - In this project it is safe because the app builds successfully with AGP 9.
  - Keep it only if needed by the current project setup.

Do not add old compatibility hacks unless required. Avoid flags like:

- `android.builtInKotlin=false`
- `android.newDsl=false`

Those were older workaround-style flags and should not be used unless there is a verified need.

## 8. Java / JBR Build Requirement

AGP 9 requires a modern Java runtime. Use Java 21 from Android Studio's bundled JBR.

Windows JBR path:

```text
C:\Program Files\Android\Android Studio\jbr
```

Recommended PowerShell build command:

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat assembleDebug
```

Use `clean` only when needed:

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat clean assembleDebug
```

If Gradle cache locks appear, close Android Studio builds, stop duplicate terminals, or restart Android Studio. Avoid running multiple Gradle builds at the same time.

## 9. Firebase Setup Pattern

Steps:

1. Create a Firebase project.
2. Add an Android app with the exact Gradle `applicationId`.
3. Download `google-services.json`.
4. Place it directly inside the `app/` folder:

```text
app/google-services.json
```

5. Enable Firebase services as needed:
   - Anonymous Authentication
   - Realtime Database
   - Cloud Messaging
   - Analytics
6. Install required SDK tools and accept SDK licenses if Android Studio requests them.

Important:

- `google-services.json` must contain the same package name as `applicationId`.
- If `google-services.json` does not include a `firebase_url`, `FirebaseDatabase.getInstance()` may not know which Realtime Database URL to use.

In Grama-Yatri, the Realtime Database URL issue was solved by using an explicit database URL in `Constants.kt`:

```kotlin
object Constants {
    const val FIREBASE_DATABASE_URL =
        "https://your-project-id-default-rtdb.region.firebasedatabase.app"
}
```

Then `AppModule` provides FirebaseDatabase like this:

```kotlin
@Provides
@Singleton
fun provideFirebaseDatabase(): FirebaseDatabase =
    FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_URL)
```

Use this explicit URL pattern when:

- `google-services.json` has no `firebase_url`.
- The app reads from the wrong database instance.
- Firebase route data exists in Console but the app shows empty data.

## 10. Hilt Setup Pattern

### Application class

```kotlin
package com.myproject.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyProjectApplication : Application()
```

### AndroidManifest registration

```xml
<application
    android:name=".MyProjectApplication"
    ... >
</application>
```

### MainActivity

```kotlin
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    ...
}
```

### Fragments

Every fragment that injects dependencies or uses Hilt ViewModels should be annotated:

```kotlin
@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home)
```

### AppModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase =
        FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_URL)

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()
}
```

KSP/Hilt compatibility notes:

- Use `alias(libs.plugins.ksp)` in plugins.
- Use `ksp(libs.hilt.compiler)` for the Hilt compiler.
- Do not use kapt in this setup unless there is a strong reason.
- Keep Hilt plugin, runtime, and compiler versions matching.

## 11. XML + Navigation Setup Pattern

Use Single Activity architecture.

### `activity_main.xml`

```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/nav_host_fragment"
        android:name="androidx.navigation.fragment.NavHostFragment"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        app:defaultNavHost="true"
        app:navGraph="@navigation/nav_graph" />

    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/bottom_navigation"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:menu="@menu/bottom_nav_menu" />
</LinearLayout>
```

### `MainActivity.kt`

```kotlin
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        findViewById<BottomNavigationView>(R.id.bottom_navigation)
            .setupWithNavController(navController)
    }
}
```

### `nav_graph.xml`

Start with placeholder destinations:

- SplashFragment
- OnboardingFragment
- HomeFragment
- AlertsFragment
- ProfileFragment

Add feature destinations later.

### `bottom_nav_menu.xml`

The bottom nav item IDs must match destination IDs in `nav_graph.xml`.

Example:

```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@id/homeFragment"
        android:icon="@drawable/ic_home"
        android:title="@string/nav_home" />

    <item
        android:id="@id/alertsFragment"
        android:icon="@drawable/ic_alerts"
        android:title="@string/nav_alerts" />

    <item
        android:id="@id/profileFragment"
        android:icon="@drawable/ic_profile"
        android:title="@string/nav_profile" />
</menu>
```

Phase 1 checklist:

- `SplashFragment`
- `OnboardingFragment`
- `HomeFragment`
- `AlertsFragment`
- `ProfileFragment`
- `activity_main.xml`
- `nav_graph.xml`
- `bottom_nav_menu.xml`
- Placeholder fragment layouts or simple fragment XML screens

## 12. Recommended Package / Folder Structure

Use this generic structure:

```text
com.example.app/
├── AppNameApplication.kt
├── MainActivity.kt
├── data/
│   ├── model/
│   ├── remote/
│   └── repository/
├── di/
│   └── AppModule.kt
├── ui/
│   ├── splash/
│   ├── onboarding/
│   ├── home/
│   ├── alerts/
│   └── profile/
└── util/
```

Do not overfill these folders in Phase 1. Create only enough structure to support the foundation.

## 13. Common Errors and Fixes

### Error: Duplicate Kotlin plugin

Error message:

```text
Cannot add extension with name 'kotlin', as there is an extension already registered with that name.
```

Cause:

- Kotlin Android plugin was applied twice.

Fix:

- Keep Kotlin Android plugin declared in the version catalog.
- In root `build.gradle.kts`, use `apply false`.
- In app module, apply it only once if needed.
- In this project's final setup, AGP/Kotlin integration works through the configured plugin catalog and app plugin setup. Avoid duplicate manual plugin requests.

### Error: Old Hilt with Kotlin 2 metadata

Error message:

```text
Provided Metadata instance has version 2.2.0, while maximum supported version is 2.1.0.
```

Cause:

- Hilt compiler was too old for Kotlin 2.x metadata.

Fix:

- Use a newer Hilt version compatible with Kotlin 2.
- This project uses Hilt `2.59.2`.
- Use the same version for:
  - Hilt Gradle plugin
  - `hilt-android`
  - `hilt-compiler`

### Error: kapt/Hilt compatibility problems

Cause:

- kapt with older Hilt can fail with Kotlin 2.x.

Fix:

- Use KSP for Hilt:

```kotlin
alias(libs.plugins.ksp)
implementation(libs.hilt.android)
ksp(libs.hilt.compiler)
```

### Error: Java 17 vs Java 21

Cause:

- AGP 9 requires a newer Java runtime.

Fix:

- Use Android Studio bundled JBR:

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat assembleDebug
```

### Error: SDK licenses not accepted

Fix:

- Open Android Studio SDK Manager and accept licenses, or run:

```powershell
sdkmanager --licenses
```

Use the SDK Manager from the installed Android SDK path.

### Error: Missing Build Tools / SDK 36

Fix:

- Open Android Studio SDK Manager.
- Install required Android SDK Platform.
- Install required Build Tools.
- Sync Gradle again.

### Error: `google-services.json` missing

Fix options:

1. Keep Google Services plugin conditional during Phase 1.
2. Or add `app/google-services.json` after creating Firebase Android app.

Conditional pattern:

```kotlin
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}
```

### Error: Realtime Database URL null or wrong database

Symptoms:

- Firebase Console has `/routes`, but app reads empty data.
- `FirebaseDatabase.getInstance()` points to no database or wrong region.

Fix:

- Add explicit URL constant:

```kotlin
const val FIREBASE_DATABASE_URL =
    "https://your-project-id-default-rtdb.region.firebasedatabase.app"
```

- Use:

```kotlin
FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_URL)
```

### Error: Gradle cache lock / AccessDenied

Cause:

- Multiple builds running.
- Android Studio and terminal both using Gradle.
- Locked Gradle wrapper/cache files.

Fix:

- Stop duplicate Gradle builds.
- Close Android Studio build process if needed.
- Restart Android Studio.
- Use the normal local Gradle cache.
- Avoid deleting Gradle folders while builds are running.

## 14. Foundation Setup Checklist

Use this checklist for future Phase 1 work:

1. Inspect the current project first.
2. Identify whether it is Compose or XML.
3. Decide package name and `applicationId`.
4. If needed, migrate from Compose UI to XML safely.
5. Set up version catalog carefully.
6. Configure root `build.gradle.kts` with plugin aliases and `apply false`.
7. Configure `app/build.gradle.kts`.
8. Enable `viewBinding`.
9. Add Firebase dependencies through Firebase BOM.
10. Add Hilt with KSP.
11. Add Navigation, Material, Lifecycle, RecyclerView, ViewPager2, Coroutines.
12. Apply Google Services conditionally until `google-services.json` exists.
13. Create `Application` class with `@HiltAndroidApp`.
14. Register application class in Manifest.
15. Create `AppModule`.
16. Create `activity_main.xml`.
17. Create `nav_graph.xml`.
18. Create `bottom_nav_menu.xml`.
19. Create placeholder fragments:
    - Splash
    - Onboarding
    - Home
    - Alerts
    - Profile
20. Annotate MainActivity and fragments with `@AndroidEntryPoint`.
21. Wire BottomNavigationView to NavController.
22. Add required permissions:
    - `INTERNET`
    - `POST_NOTIFICATIONS` if notifications will be used.
23. Build with Android Studio JBR.
24. Run the app.
25. Confirm placeholder navigation works.
26. Only then move to Phase 2.

## 15. Future Project Phase 1 Foundation Prompt

Copy and paste this prompt in a future project:

```markdown
Start Phase 1 foundation setup only.

Before coding, read:
- claude_md_files/ANDROID_FOUNDATION_SETUP_GUIDE.md
- Any project PRD or requirements file I provide

Important:
- Do not over-generate the app.
- Do not implement feature screens or business logic.
- Implement only foundation setup.
- Inspect the current Android project first.
- Tell me whether it is Compose or XML.
- If it is Compose and the project requires XML, safely migrate to XML + Fragment Navigation.
- Use the same Gradle/Hilt/KSP/Firebase pattern from ANDROID_FOUNDATION_SETUP_GUIDE.md.
- Use KSP for Hilt, not kapt, unless there is a verified project-specific reason.
- Do not apply the Kotlin Android plugin twice.
- Keep Hilt plugin, hilt-android, and hilt-compiler versions consistent.
- Apply Google Services conditionally if google-services.json is missing.
- Set up Single Activity + NavHost + BottomNavigationView.
- Create placeholder fragments only:
  - SplashFragment
  - OnboardingFragment
  - HomeFragment
  - AlertsFragment
  - ProfileFragment
- Create clean package folders:
  - data/model
  - data/remote
  - data/repository
  - di
  - ui/*
  - util
- Add @HiltAndroidApp Application class.
- Register Application class in AndroidManifest.xml.
- Add AppModule with FirebaseAuth and FirebaseDatabase providers.
- Use explicit FirebaseDatabase URL if google-services.json does not provide firebase_url.
- Build with Android Studio bundled JBR:
  $env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat assembleDebug

If build fails:
- Show only the first real error.
- Fix the minimum required issue.
- Do not randomly change versions.

Final report:
1. Files created
2. Files modified
3. Package/applicationId
4. Gradle versions used
5. Firebase/google-services.json status
6. Whether build succeeds
7. Whether placeholder app launches
8. Whether Phase 1 is ready for Phase 2
```

## 16. Final Recommendations

- Keep Phase 1 small.
- Do not add app features in Phase 1.
- Do not build real screens before navigation works.
- Do not add repositories or models until Gradle, Hilt, Firebase, and Navigation build successfully.
- Always build locally after Phase 1.
- Always run the app and verify placeholder navigation.
- Do not start Phase 2 until the app launches with placeholder navigation.
- When errors occur, fix the first real error only.
- Avoid random dependency upgrades or downgrades.
- Keep version decisions centralized in `libs.versions.toml`.
- Keep the Firebase package name aligned with Gradle `applicationId`.

The main lesson from Grama-Yatri Phase 1 is that foundation setup should be careful, small, and verified before feature development begins.

