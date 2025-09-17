# LovAi — DESIGN.md

> **Goal**: Translate the screenshots into precise UX/UI behavior, flows, data models, and rules—ready for engineers to implement in **Java + XML** using our tech stack. Treat this as the single source of truth for product behavior in v1.

---

## 1) Product Overview

LovAi helps couples plan dates, discover places, save photo moments, and manage reminders/integrations. The app has four bottom tabs:

1. **Home** — weather + calendar + AI suggested plans.
2. **Discover** — search & filter cute places/food/cafés/fun.
3. **Moments** — upload and browse photo memories grouped by event.
4. **Settings** — account, integrations, privacy, notifications.

Tone: warm, positive, pastel pink theme with playful emoji; microcopy is short and supportive (e.g., “Great for walking!”).

---

## 2) Navigation & Information Architecture

* Single-activity app with bottom navigation to **Home / Discover / Moments / Settings**.
* Each tab is a fragment; Navigation Component handles deep links and back stack.
* Top app bars are minimal; content is card-driven with chips and lists.
* Deep links: `lovai://home`, `lovai://discover?q=...`, `lovai://moments/{groupId}`, `lovai://settings`.

---

## 3) Screen Specifications & Acceptance Criteria

### 3.1 Home

**Components**

* **Header card**: couple avatar, app name, subtitle “Together Forever”, counter tile “X days together”.

  * *Logic*: Days Together = `today - couple.startDate` (inclusive). Counter updates at midnight via WorkManager.
* **Weather card**: icon + temp + condition line + right-aligned chip advice (e.g., “Great for walking! ☁️”).

  * *Advice rules*: see §7 Recommendation Rules (WeatherAdvice).
* **Monthly calendar**: shows current month (scrollable by month). Markers:

  * Small circular avatar on days with **moments**.
  * Highlighted pill on **anniversary/special** dates.
  * Tap to select a date (state saved in VM); long-press → quick action “Add Moment”.
* **Selected-day summary**: small weather preview + “Perfect for outdoor dates!” chip.
* **Weekend suggestions** block: header “Weekend looks great! 🌟” + badge “AI Suggested”. Items are tappable rows.

**Empty & Error States**

* No internet → cached weather + banner “You’re offline”.
* No moments this month → show educational hint to add first moment.

**Acceptance**

* Calendar marks load from Room within 150ms; weather cached ≤30 min; suggestions list uses recommendation rules; tapping an item opens its detail in Discover.

---

### 3.2 Discover

**Hero search** with placeholder “Search for cute places… 🔍”.

* Debounced (300ms) updates `DiscoverViewModel.query`.
* Filter chips (persisted): **Distance** (≤ km), **Price** (FREE/\$/\$\$/\$\$\$), **Favorites** (on/off).
* Category tabs: **Places**, **Food**, **Cafés**, **Fun** (change `category` filter).
* Results: card with icon, **name (bold)**, **subtitle**, ⭐ rating, distance, price indicator, and fee tag (e.g., Free, \$5).
* Paging: infinite scroll or Load More.

**Empty & Error States**

* No results → show friendly empty with CTA to widen filters.
* Offline → show last cached results + offline banner.

**Acceptance**

* Filter state round-trips on process death; tapping a result shows place detail (MVP can reuse a generic detail sheet).

---

### 3.3 Moments

**Upload New Photos ✨** CTA (top card)

* Action opens system picker (multi-select). Permission flows for Android 13+ `READ_MEDIA_IMAGES`.
* On selection: create **Moment Group** with title, location, date (today by default; editable); enqueue background thumbnailing and optional cloud upload (if Drive connected).

**Event groups**

* Section card with: title (e.g., “Sakura Festival 🌸”), location pin + name, date, 2×2 photo grid tiles, ♥ counters, Share / Cloud icons.
* Tap a tile → photo viewer; long-press → select mode for share/delete.
* Cloud icon → upload group to Drive (if connected) or prompt to connect.

**States**

* Upload queue with retries (exponential backoff). Progress shown subtly on cards.

**Acceptance**

* Selecting 4 photos creates a grid immediately with cached thumbs; share action exposes Android Sharesheet.

---

### 3.4 Settings / Config

Sections:

* **Account**: Profile Settings, Couple Profile (two names, start date).
* **Integrations**: Google Drive / Maps / Shopee / Grab with status dots (green=connected, gray=disconnected). Tap → connect/disconnect flow; persist tokens securely.
* **Privacy & Security**: policy page, clear caches, revoke tokens.
* **Notifications**: switches for Date Reminders and Anniversary Alerts.

**Acceptance**

* Toggling reminders schedules/cancels WorkManager jobs; connect flows update status inside 2s with optimistic UI.

---

## 4) Data Model (Domain)

```
User { id, name, avatarUrl }
CoupleProfile { id, partnerA, partnerB, startDate, anniversaryDates[] }
Weather { condition, tempC, tempF, adviceText, icon }
Place { id, name, subtitle, rating, distanceKm, priceTier, category, isFavorite }
Recommendation { id, title, tags[], placeId?, score, reason }
Moment { id, eventTitle, locationName, date, photos[], likes }
Integration { id=[GOOGLE_DRIVE, MAPS, SHOPEE, GRAB], status=[CONNECTED,DISCONNECTED], capabilities[] }
Preference { distanceKmMax, priceTierMax, notificationsEnabled, language }
CalendarMark { date, type=[ANNIVERSARY, MOMENT, SPECIAL] }
```

* Lists (e.g., photos) are stored as URIs; Room uses `@TypeConverter`.
* Distance displayed in km/mi based on locale; temperature °C/°F toggle.

---

## 5) API Contracts (High Level)

**WeatherApi**

* `GET /weather/current?lat&lng` → `{ condition, tempC, tempF, icon }`
* `GET /weather/day?lat&lng&date` → as above

**PlacesApi**

* `GET /places/search?q&category&distanceKmMax&priceTier&page` → `[PlaceDTO]`

**DriveApi (optional)**

* `POST /drive/upload` (multipart) → `{ driveFileId }`

**Notes**: Real providers (OpenWeather, Google Places, Google Drive) are wrapped by adapters to the shapes above.

---

## 6) ViewModel Contracts (Behavioral)

**HomeViewModel**

* `LiveData<HomeHeaderUi> header`
* `LiveData<WeatherUi> weather`
* `LiveData<MonthUi> calendar`
* `LiveData<List<RecommendationUi>> recs`
* `void onDateSelected(LocalDate d)`; `void refresh()`

**DiscoverViewModel**

* `MutableLiveData<String> query`
* `MutableLiveData<FilterUi> filter`
* `LiveData<PagedList<PlaceUi>> results`
* `void toggleFavorite(String placeId)`

**MomentsViewModel**

* `LiveData<List<MomentGroupUi>> groups`
* `void addPhotos(List<Uri> uris, String eventTitle, String location)`
* `void uploadGroup(String groupId)`

**SettingsViewModel**

* `LiveData<CoupleProfileUi> couple`
* `LiveData<List<IntegrationUi>> integrations`
* `void connect(IntegrationId id)` / `void disconnect(IntegrationId id)`

---

## 7) Recommendation & Advice Rules

**7.1 Weather Advice (chip text)**

```
if (tempC between 18 and 26 and condition in [Clear, Clouds]) → "Great for walking! ☁️"
if (condition == Rain) → "Cozy café day ☔"
if (tempC > 30) → "Best after sunset 🌇"
if (tempC < 10) → "Bundle up for a warm drink 🧣"
```

**7.2 Weekend Suggestions (AI Suggested)**

* Compute **ContextFeatures**: `{ weekendFlag, currentWeather, maxDistanceKm, priceTierMax, favorites[], history[] }`.
* Score for each candidate Place:

```
score = w1*weatherFit + w2*distanceFit + w3*priceFit + w4*favAffinity + w5*novelty
```

* Show top 3; include short `reason` (e.g., “Sunny + your Saturday history”). Cache for 24h or until filters change.

---

## 8) Offline & Error Handling

* **Weather**: last-success cache (≤30m) + toast on failure.
* **Discover**: serve cached results by query; banner “Offline”.
* **Moments**: queue uploads with WorkManager; retries with exponential backoff.
* **Settings**: optimistic toggles; revert with snackbar if failed.

---

## 9) Theming & Design Tokens

* **Palette**: soft pink background (#FFECF4), primary accents deeper pink, neutral grays for text.
* **Elevation**: subtle MaterialCardView; large radii (cards 24dp, chips 16dp).
* **Typography**: Title 28–32sp; Section 20sp; Body 14–16sp; use increased line-height for readability.
* **Components**: Chips (filter), Pills (advice), Badges (AI Suggested), Rounded Calendar cells, Emoji-enhanced labels.
* **XML Styles**: `Theme.LovAi.Light`, `TextAppearance.LovAi.Title`, `Widget.LovAi.Card`, `Widget.LovAi.Chip`, `Widget.LovAi.CalendarCell`.

---

## 10) Accessibility & i18n

* Content descriptions for all icons; Touch targets ≥44dp; Focus order follows visual order.
* High-contrast text on pink backgrounds; avoid color-only meanings (use icons/labels).
* Localizations: `en` and `vi` maintained in parallel; numbers/dates formatted via `java.time` and `Locale`.

---

## 11) Notifications

* **Date Reminders**: daily at 9am local; notify selected items (e.g., planned dates, anniversary countdown).
* **Anniversary Alerts**: schedule based on `CoupleProfile.startDate` and custom `anniversaryDates`.
* Channels: `reminders` and `announcements` with proper importance.
* Tapping notification deep links to relevant screen.

---

## 12) Telemetry (Optional, behind consent)

Events: `screen_view`, `search_submit`, `filter_change`, `place_open`, `favorite_toggle`, `moment_upload_start/success/fail`, `integration_connect`, `settings_toggle_notification`.

---

## 13) Definition of Done (per feature)

* All acceptance criteria met; error & empty states implemented.
* Tests: ViewModel unit tests + at least one Espresso happy-path per screen.
* Lint clean (no new warnings); a11y pass (TalkBack basic flow).
* Strings externalized; resources optimized; release build shrunk with R8.

---

## 14) Open Questions & Future Work

* Place **detail screen** (MVP can use a modal sheet).
* Real Drive upload vs. simple share (pick based on user priority).
* Map view and route-to-place (requires Maps SDK + intents to Grab if supported).
* Photo reactions/comments (social features) — out of scope v1.

---

## 15) Appendices

**A. XML Layout Naming**: `fragment_home.xml`, `fragment_discover.xml`, `fragment_moments.xml`, `fragment_settings.xml`, `item_place.xml`, `item_moment_photo.xml`, `view_filter_chip.xml`.

**B. Color Tokens (example)**

```
colorPrimary = #FFBBD6
colorOnPrimary = #31111D
colorSurface = #FFECF4
colorOnSurface = #1E1B1F
```

**C. User Copy (microcopy)**

* Weather tips: “Great for walking!”, “Cozy café day”, “Perfect for outdoor dates!”
* Empty states: “No cute places yet—try widening distance.” / “No moments yet—start with your latest date!”
