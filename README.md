<p align="center">
  <img src="docs/icon/icon.svg" width="120" alt="FontDrop icon" />
</p>

<h1 align="center">FontDrop</h1>

<p align="center">
  <em>Drop fonts. Write instantly.</em><br/>
  A font-first note app for Android.
</p>

<p align="center">
  <a href="../../actions/workflows/ci.yml"><img alt="CI" src="../../actions/workflows/ci.yml/badge.svg" /></a>
  <a href="#license"><img alt="license" src="https://img.shields.io/badge/license-Apache%202.0-173C34" /></a>
  <img alt="platform" src="https://img.shields.io/badge/platform-Android%2026%2B-4E786C" />
  <img alt="kotlin" src="https://img.shields.io/badge/Kotlin-2.1.0-2D5A4E" />
  <img alt="compose" src="https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.12-D89C3F" />
</p>

---

## Why FontDrop

Custom fonts on Android are painful: system-level installs, locked-down settings, no easy way to try a typeface while you're writing. FontDrop flips that around — point it at a folder of `.ttf` / `.otf` files and every note you write can be typeset in any of them, instantly and per-note.

The app leans editorial: warm paper tones, calm spacing, typography as the expressive moment. See [`docs/FontDrop_Design_System.md`](docs/FontDrop_Design_System.md) for the full design spec.

## Features

- **Font folder via SAF** — pick any tree URI (Downloads, Drive, OneDrive, local); permission is persisted across restarts.
- **Live font switching** — changing the typeface in the editor is immediate, cursor-stable, and debounced to avoid reflows.
- **Font warm-up cache** — `.ttf` / `.otf` files materialize to app cache once; subsequent renders are synchronous (no `null → loaded` flash).
- **Default font** — tap a card in the Fonts library and every new note starts in that face.
- **Editor essentials** — title + body in the user's font, adjustable size (12–40 sp) and line-height presets (1.2 / 1.4 / 1.8).
- **Auto-save** — 500 ms debounce during editing + non-cancellable flush on screen exit, so nothing is lost on rotation.
- **Note list** — cards show a snippet in the chosen font with relative timestamps (`Just now`, `4h ago`, `Yesterday`, `Mar 3`).
- **Swipe to delete, undo to recover** — standard Material pattern with warm-error swipe background.
- **Share as image** — render the note to a 1080-wide PNG using the actual typeface, then hand off via `ACTION_SEND` to any app.
- **Light-mode polished, dark-mode deferred** — see *Known limitations*.

## Screenshots

_Coming soon._ Add device screenshots to `docs/screenshots/` and reference them here.

## Requirements

| | Version |
|---|---|
| Android minSdk | 26 (Android 8.0) |
| Android targetSdk | 36 |
| JDK | 17 recommended (AGP 9 built-in Kotlin runs on up to JDK 24) |
| Android Gradle Plugin | 9.1.1 |
| Kotlin | 2.1.0 (bundled with AGP 9) |
| Room | 2.7.0 (see *Known limitations* for why not 2.6.x) |

## Building

```bash
git clone https://github.com/<you>/FontDrop.git
cd FontDrop
./gradlew :app:assembleDebug
```

Install on a connected device:

```bash
./gradlew :app:installDebug
```

Run the unit-test suite:

```bash
./gradlew :app:testDebugUnitTest
```

## Project structure

```
app/src/main/kotlin/io/github/sejoung/fontdrop/
├── FontDropApplication.kt           # Application entry point, owns AppContainer
├── MainActivity.kt                  # Compose host + edge-to-edge
├── di/
│   └── AppContainer.kt              # Manual DI (lazy-initialised singletons)
├── data/
│   ├── font/
│   │   ├── FontAsset.kt             # Domain model (URI as String → pure JVM tests)
│   │   ├── FontScanner.kt           # .ttf/.otf filtering + stable IDs
│   │   ├── DocumentTreeFontFolderSource.kt  # SAF-backed file listing
│   │   ├── FontFileMaterializer.kt  # URI → cacheDir/fonts/{id}.ttf (mutex-guarded)
│   │   ├── FontFamilyCache.kt       # Map<id, FontFamily> with bounded prewarm
│   │   └── FontFolderRepository.kt  # Folder URI + defaultFontId persistence
│   ├── note/
│   │   ├── Note.kt                  # Domain model (defaults: 24sp / 1.4x)
│   │   ├── NoteEntity.kt            # Room @Entity
│   │   ├── NoteDao.kt               # Upsert + Flow observation
│   │   ├── FontDropDatabase.kt      # @Database(version = 1)
│   │   └── NoteRepository.kt        # Timestamp policy + CRUD
│   ├── prefs/
│   │   └── FontFolderPreferences.kt # DataStore (folderUri, defaultFontId)
│   └── share/
│       ├── NoteImageRenderer.kt     # Interface for testable rendering
│       └── AndroidNoteImageRenderer.kt  # Canvas + StaticLayout → PNG
├── ui/
│   ├── components/                  # Design-system primitives (AppBar, buttons, cards…)
│   ├── editor/                      # Editor + font picker sheet + toolbar
│   ├── library/                     # Font library + rememberFontFamily helper
│   ├── notes/                       # List + swipe-to-delete + undo
│   ├── navigation/                  # NavHost + bottom bar
│   ├── theme/                       # Design tokens: Color, Type, Shape, Spacing
│   └── util/                        # RelativeTime, preview-sentence constant
└── util/
    └── Clock.kt                     # Injected time for deterministic tests
```

## Architecture at a glance

- **UDF with Compose state holders.** ViewModels expose `StateFlow<UiState>`; screens collect it and dispatch intents back as method calls. No fan-out side effects.
- **Per-screen ViewModel, shared singletons via `AppContainer`.** Manual lazy-init DI keeps bootstrap visible and dependency-free.
- **Font loading is layered.** `FontFolderSource` → `FontFileMaterializer` (disk cache) → `FontFamilyCache` (memory cache + `FontFamily` construction). Every layer is behind an interface so the VMs test in pure JVM.
- **Compose-owned `TextFieldValue`.** The editor intentionally mirrors edits one-way (user → VM). State changes from the VM never reset the text field, preserving the cursor across font/size/line-height switches.
- **Non-cancellable saves.** `flushPendingSave` wraps its DB write in `withContext(NonCancellable)` so rotation or navigation can't lose an in-flight autosave.
- **Dirty tracking.** Opening a note doesn't touch `updatedAt`; only real edits flip the flag that gates persistence.
- **Light-only theming (MVP).** Many UI surfaces still hardcode ink/paper tokens; dark mode is disabled at the `FontDropTheme` level until those are migrated to `MaterialTheme.colorScheme.on*`.

## Testing

78 pure-JVM unit tests cover the stack:

- `FontScanner`, `FontFileMaterializer`, `FontFamilyCache` — filtering, I/O caching, prewarm cap, invalidation.
- `NoteRepository` — create / save / observe / timestamp policy.
- `EditorViewModel`, `NoteListViewModel`, `FontLibraryViewModel` — state transitions, autosave debounce, dirty flag, font-switch prewarm, deletion / undo, share flow.
- `RelativeTime` — 8 timezone-independent cases for the list timestamps.

Instrumented tests (Room DAO, Compose UI snapshots) are not included yet — see *Roadmap*.

## Design system

Built to a compact token set defined in `ui/theme/`:

| Tokens | Source |
|---|---|
| Color | `FontDropPalette` — Ink 900/700/500, Paper 50/100/200, Gold 400/500/600, Clay 300/500/700, warm error |
| Type | `FontDropTypography` — 12-step scale (Display / Heading / Body / Label) |
| Spacing | `FontDropSpacing` — 8 pt grid, xxs…huge |
| Shape | `FontDropRadius` — xs (6) / s (10) / m (14) / l (20) / xl (28) |

Semantic text tokens (`TextSecondary`, `TextTertiary`) were shifted one Ink step deeper than the spec to meet WCAG AA 4.5:1 on warm paper surfaces.

## Known limitations

- **AGP 9 + KSP compatibility shim.** `gradle.properties` sets `android.disallowKotlinSourceSets=false` so KSP can register generated sources under AGP 9's built-in Kotlin. Expect this opt-out to disappear once KSP migrates to `android.sourceSets`.
- **Room ≥ 2.7.0 required.** Room 2.6.x generated code trips KSP2 with `unexpected jvm signature V`. Do not downgrade.
- **Dark mode disabled.** `FontDropTheme` is pinned to the light color scheme until the remaining palette hardcodes are migrated to `MaterialTheme.colorScheme.on*`.
- **No DAO/UI integration tests.** ViewModels and repositories are covered via fakes; the actual Room queries and Compose interactions are validated manually for now.
- **Cache is unbounded on disk.** `cacheDir/fonts/` and `cacheDir/share/` are never explicitly swept; we rely on the OS cache-clear behaviour.

## Roadmap

- [ ] Full dark-mode theming
- [ ] Room DAO instrumented tests + Compose UI tests
- [ ] Font metadata (`name` table) → real family grouping, variable-font axes
- [ ] PDF export alongside PNG share
- [ ] Font tagging / favourites
- [ ] Cloud sync
- [ ] iOS

## Releasing

Use the bundled helper script — it bumps `versionCode` / `versionName` in `app/build.gradle.kts`, creates the commit and annotated tag, and pushes everything so that the [release workflow](.github/workflows/release.yml) can pick it up.

```bash
./scripts/release.sh patch             # 0.1.0 → 0.1.1
./scripts/release.sh minor             # 0.1.3 → 0.2.0
./scripts/release.sh major             # 0.4.2 → 1.0.0
./scripts/release.sh 0.2.0-beta.1      # explicit semver (incl. prereleases)
./scripts/release.sh patch --no-push   # stage the commit + tag locally, push manually
```

`patch` / `minor` / `major` read the current `versionName` from `app/build.gradle.kts` and produce a stable version — prerelease suffixes (`-beta.1`, `-rc.2`…) are only available through an explicit argument.

The script refuses to run if the working tree is dirty, if you're not on `main`, or if the tag already exists locally or on the remote. `versionCode` is derived from the number of existing `v*` tags so it increases monotonically.

Once the tag lands on GitHub, the workflow builds the debug APK, publishes it as a GitHub Release asset, and auto-generates release notes from the commits since the previous tag. Tags containing `alpha` / `beta` / `rc` / `dev` / `preview` are flagged as prereleases.

The workflow currently ships a **debug-signed** APK — good for side-loading and internal testing, not for Play Store. Production signing will require adding a release keystore via GitHub Actions secrets (`RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`) and a `signingConfigs.release { ... }` block in `app/build.gradle.kts`.

## Contributing

PRs welcome. If you're adding a feature, please also:

1. Route platform-only surfaces through an interface so the VM stays testable in pure JVM.
2. Add or update unit tests under `app/src/test/kotlin/`.
3. Use design tokens (`FontDropTheme.spacing`, `FontDropPalette`, `FontDropTheme.type`) rather than raw `dp` / `Color` / `TextStyle` values.
4. Keep the editor cursor-stable — text state lives in the Composable, VM only mirrors.

Run the full check before opening a PR:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

## License

Apache License 2.0 — see [`LICENSE`](LICENSE).

Bundled fonts are **not** distributed with the app; FontDrop only reads files you grant access to via the Android Storage Access Framework. Typeface licensing remains your responsibility.
