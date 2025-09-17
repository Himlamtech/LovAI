# LovAi — TECHSTACK.md (Updated for API 36)

> **Goal**: Define a concrete, buildable Android tech stack for the LovAi couples app, implemented with **Java + XML**, using **Clean Architecture + MVVM + Repository**. Updated for **compileSdk/targetSdk 36**. This document is the contract for setup, dependencies, module layout, build, testing, CI/CD, security, and operations.

---

## 1) Platform & Targets

* **Android**: minSdk **36**, **compileSdk 36**, **targetSdk 36**.
* **Language**: **Java 25** Kotlin/Compose **not used**.
* **UI**: XML layouts + Material Components.
* **Architecture**: Clean Architecture → layers: `app` (UI/DI), `domain` (use cases + entities), `data` (repos + APIs + DB), `core` (utils/theme).
* **Navigation**: Single-activity (`MainActivity`) + Jetpack Navigation Component (fragments per tab).
* **Performance**: 60fps goal, list diffing, image caching, background work via WorkManager.

---

## 2) Module Layout

```
com.lovai
 ├─ app/                    # entrypoint, DI wiring, navigation
 ├─ core/                   # base classes, utils, theme
 ├─ domain/                 # pure Java: models + use cases
 └─ data/                   # Retrofit, Room, repositories, mappers
```

**Gradle Modules**

* `:app` — Android application, Fragments/Activities, XML layouts, ViewModels (MVVM), Hilt entry.
* `:domain` — `model` POJOs + `usecase` interactors (no Android deps).
* `:data` — `api` (Retrofit interfaces), `db` (Room), `repo` (Repository implementations), `mapper` (DTO↔domain), `datasource` (local/remote), caching.
* `:core` — `ui` base classes, extensions, `util` (DateTime, Resource/Either), theme tokens (colors, dimens, styles), logging.

---

## 3) Dependencies (Java-compatible)

**Jetpack**

* AppCompat, Fragment, Activity KTX (Java friendly), Material Components
* Lifecycle: ViewModel, LiveData
* Navigation Component (fragment + UI)
* Room (runtime, compiler via annotationProcessor)
* WorkManager
* Paging (optional for Discover results)
* Data Binding / View Binding (enable both; Data Binding for simple binds)
* **core-splashscreen** (optional modern splash, API 31+)

**Networking**

* Retrofit2 + OkHttp3
* Converter: Gson (or Moshi if team prefers)
* OkHttp Interceptors: logging, auth, offline-cache

**DI**

* Hilt (Dagger) — `hilt-android`, `hilt-compiler` (annotationProcessor)

**Images**

* Glide (with lifecycle-aware loading)

**Location & Maps**

* Google Play Services Location
* Google Maps SDK (if map views are added later)

**Storage & Security**

* Room (SQLite)
* EncryptedSharedPreferences (AndroidX Security library)
* **Photo Picker API (API 33+)** for images in Moments (preferred over storage permissions)

**Crash & Analytics (optional)**

* Firebase Crashlytics / Analytics (guarded by build flavor)

**Testing**

* JUnit4/5, Mockito, Robolectric, Espresso, Truth/Hamcrest

---

## 4) Build & Tooling

* **AGP**: Latest LTS.
* **Java version** set via Gradle toolchains.
* **Build Variants**: `dev`, `staging`, `prod` with `BuildConfig` flags (e.g., `USE_MOCKS`, endpoint base URLs).
* **Static Analysis**: Android Lint (fatal on CI for `prod`), SpotBugs (optional).
* **ProGuard/R8**: shrink, optimize, keep rules for Hilt/Glide/Room/Retrofit models.
* **Signing**: CI-keystore for `staging`, secure Play keystore for `prod`.

**Gradle (app) — key snippets**

```groovy
android {
  namespace "com.lovai"
  compileSdk 36
  defaultConfig {
    minSdk 36
    targetSdk 36
    vectorDrawables.useSupportLibrary = true
  }
  buildFeatures { viewBinding true; dataBinding true }
  packagingOptions { resources.excludes += ["META-INF/*"] }
}

java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }
```

---

## 5) Manifest & Permissions (API 36 compliance)

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" tools:targetApi="33" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" tools:targetApi="33" />
```

* **Notifications**: request `POST_NOTIFICATIONS` at runtime (API 33+).
* **Media**: prefer **Photo Picker API**; fall back to `READ_MEDIA_IMAGES` only if required.
* **Location**: ask for coarse first; fine only if needed.
* **Exported components**: every `Activity`, `Service`, `Receiver` with intent-filters must set `android:exported="true|false"`.
* **Package visibility**: restrict `queries` to external apps you integrate with (Maps/Grab).

---

## 6) Target‑SDK behavioral notes

* **Foreground services**: avoid unless mandatory. Use **WorkManager** for uploads and reminders.
* **Exact alarms**: request only if necessary (e.g., anniversaries). Otherwise stick with WorkManager.
* **Edge-to-edge**: enable via `WindowCompat.setDecorFitsSystemWindows(window, false)`.

---

## 7) External Services & Abstractions

* **Weather**: OpenWeatherMap or equivalent.
* **Places**: Google Places API or mock.
* **Drive**: Google Drive REST / Android Sharesheet fallback.
* **Shopee / Grab**: stubs; expose status only.

Repositories unchanged (see original TECHSTACK.md).

---

## 8) Testing Matrix

* Emulators: **API 36** (target), **API 33/34** (permissions), **API 28**, **API 24** (min).
* Run: `./gradlew testDebugUnitTest connectedDevDebugAndroidTest`.

---

## 9) CI/CD Updates

* Ensure CI has **cmdline-tools**, **platforms;android-36**, **build-tools** latest.
* Cache Gradle + AVD snapshots.

---

## 10) Migration Checklist

* [x] compileSdk/targetSdk = 36
* [x] Runtime permission flows handled (notifications, media, location)
* [x] Exported components declared
* [x] Foreground services avoided; WorkManager jobs configured
* [x] Notification channels created; deep links working
* [x] Room migrations tested
* [x] Lint clean; Play prelaunch report clean
