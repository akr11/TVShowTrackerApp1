# TV Show Tracker (Kotlin Multiplatform)

Cross-platform template aligned with the **TV Show Tracker App** specification: [TVmaze public API](https://www.tvmaze.com/api), local persistence, favorites, watched progress, and continue-watching logic implemented in **shared Kotlin** with **Jetpack Compose** on Android.

## Architecture (justified)

- **UI:** Native per platform — **Jetpack Compose** on Android (this repo) / **SwiftUI** on iOS when you wire the exported `Shared` framework.
- **Pattern:** **MVI-like** — each screen has a small `*UiState` and unidirectional updates through `StateFlow` + intents on shared `*ViewModel` classes (not AndroidX `ViewModel`).
- **Dependency injection:** **Koin** defines `sharedModule` + `expect/actual platformModule()`; the Android app wraps the tree with `KoinAndroidContext`.
- **Compose integration:** `CompositionLocal` values propagate Koin + navigation into subtrees:
  - `LocalKoinGraph` → `org.koin.core.Koin` (`GlobalContext.get()`), used to resolve `factory` ViewModels (`get<SearchViewModel>()`, etc.).
  - `LocalAppNavigator` → shared `AppNavigator` implemented by `ComposeAppNavigator` (`androidApp/.../navigation/`), so UI does not thread ad-hoc lambdas for every transition.
- **Navigation contract:** `shared/.../navigation/AppNavigator.kt` is the interface; Android’s `NavController` is the only place that knows routes (`NavRoutes`).
- **UI (Android):** Navigation Compose, Coil, bottom tabs for Search / My Shows.
- **Shared (`:shared`):**
  - **Networking:** Ktor Client + kotlinx.serialization JSON (DTOs under `data.remote.dto`, mapping in `data.remote.mapper`).
  - **Persistence:** Room Multiplatform + SQLite bundled driver; platform `Room.databaseBuilder` in `androidMain` / `iosMain`; `getRoomDatabase` applies `BundledSQLiteDriver`, coroutine dispatcher, **destructive migration** for development schema changes.
  - **Repositories:** `TvShowRepository` coordinates TVmaze requests, Room DAOs, and a small **in-memory session cache** to avoid redundant network calls for non-favorite browsing.
  - **Domain:** HTML stripping, episode ordering, **next-unwatched** and **progress** calculations in pure Kotlin (`domain/`).
  - **Presentation:** MVI-style `StateFlow` ViewModels (`SearchViewModel`, `DetailViewModel`, `EpisodesViewModel`, `LibraryViewModel`) extending a lightweight shared `BaseViewModel` (not AndroidX `ViewModel`).

This split matches commercial separation of **UI / business logic / API / storage** required by the brief.

## Tech stack (multiplatform)


| Area        | Choice                                                                     |
| ----------- | -------------------------------------------------------------------------- |
| Networking  | **Ktor** Client + **kotlinx.serialization** (JSON)                         |
| Persistence | **Room** (KMP) + **sqlite-bundled**                                        |
| DI          | **Koin** (shared + `platformModule` for `AppDatabase`)                     |
| Logging     | **Napier**                                                                 |
| Concurrency | **Coroutines** / **Flow** (custom `FlowWrapper` for iOS interop if needed) |
| Android UI  | **Jetpack Compose**, **Navigation Compose**, **Coil**                      |
| iOS UI      | **SwiftUI** (consume `Shared` / SPM as in `iosApp/`)                       |


## Features vs. spec


| Requirement                                           | Implementation                                                                                                                     |
| ----------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| Search (loading / empty / error / retry / pagination) | TVmaze `search/shows` with **client-side paging** (20 per page) + debounced query (450 ms) + manual “Search now” + infinite scroll |
| Show details                                          | TVmaze `shows/{id}` + offline cache when favorited                                                                                 |
| Episodes by season                                    | TVmaze `shows/{id}/episodes`, grouped via `episodesBySeason`                                                                       |
| Watched / season watch                                | Room `watched_episodes`; bulk season toggles in repository                                                                         |
| My Shows (offline)                                    | Room `favorite_shows` + `cached_episodes`; library combines flows                                                                  |
| Continue watching                                     | `nextUnwatchedEpisode` + `watchedProgress` in `domain/ContinueWatching.kt`                                                         |
| Koin                                                  | Required modules registered in `di/AppModule.kt`                                                                                   |
| Images + fallbacks                                    | Coil `AsyncImage` (empty URL is handled by Coil; placeholders can be added per screen)                                             |


**API etiquette:** single episode list fetch per show; search is one TVmaze request then local slicing; session cache reduces repeat detail/episode fetches; 25 s Ktor timeouts.

## Modules

- `shared` — API, Room, repository, domain, ViewModels, tests.
- `androidApp` — Compose UI (`ui/` routes), `INTERNET` permission, Koin init with `androidContext`.
- `build-logic` — convention plugins.

## iOS / SPM / Xcode app

**One-click workflow (recommended):**

1. From the repo root, ensure `./gradlew` works (JDK on `PATH`).
2. Open `**iosApp/TVShowTracker.xcodeproj`** in Xcode.
3. Pick the `**TVShowTracker**` scheme (shared in the repo) and a simulator (**iOS 16+**).
4. Press **Run** (▶). The `**BuildSharedKmp`** aggregate target runs first (scheme + target dependency) and its script calls
  `sync-shared-xcframework.sh` **only while** `Shared.xcframework` is missing; Gradle can take a few minutes on the first clone.

**Manual XCFramework-only** (e.g. CI): `./iosApp/sync-shared-xcframework.sh` or  
`./gradlew :shared:assembleSharedReleaseXCFramework`.

Swift entry point hosts `**TVShowTrackerRootView()`** from `**TVShowTrackerPackage**` (`iosApp/TVShowTrackerPackage/Package.swift` — see `TVShowScreens.swift`). It mirrors the Android UX: tabs **Search** / **My shows**, show detail with favorite + progress, episode list plus season-wide watched toggles via shared `**SearchViewModel`**, `**LibraryViewModel**`, `**DetailViewModel**`, `**EpisodesViewModel**` (`IosUiBridge` + `**FlowWrapper**` bridging `StateFlow` into SwiftUI). The Xcode launcher stays under `**iosApp/TVShowTracker/**`.

## Testing strategy

**Included (shared `commonTest`):**

- `ContinueWatchingTest` — next episode ordering, progress counts, edge cases.
- `HtmlTextTest` — HTML stripping for summaries.

**Not included (by choice / scope):**

- UI / Compose tests (not required by spec).
- Full repository integration tests (would need fakes for Ktor/Room or multiplatform test harness); domain tests cover the riskiest business rules. Repository wiring is exercised by running the app.

Run unit tests:

```bash
./gradlew :shared:testDebugUnitTest
```

## Run

```bash
./gradlew :androidApp:installDebug
```

Use **Search** to find shows, open **details**, add to **My Shows** (heart), open **Episodes & seasons** to mark progress. Saved shows and episodes stay available offline after the first successful sync.