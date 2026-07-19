# Emojis — Claude Context

Light Phone III app for browsing, searching, and copying emojis. Built natively in Kotlin/Jetpack
Compose against the [Light SDK](https://github.com/lightphone/light-sdk), targeting the LP3 only.

## Stack

- Kotlin, Jetpack Compose, against `light-sdk`'s `sdk:client`/`sdk:ui`/`sdk:shared` modules
- Gradle (Kotlin DSL), AGP, KSP (for the SDK's `@InitialScreen`/`@EntryPoint` registry)
- `kotlinx.serialization` for DataStore-persisted JSON
- Tool id: `com.zacksimpson.emojis` (see `tool/lighttool.toml`)

## Repo layout

`light-sdk` is a **git submodule** at the repo root, pinned to a specific commit — a **local build
dependency only**. The official Light build server extracts and builds only the `tool/` subtree
from this repo; everything else (including the submodule) is ignored server-side. Root-level
`settings.gradle.kts` points the SDK's Gradle modules (`:sdk:client`, `:sdk:ui`, `:sdk:shared`,
`:lint-rules`) and the plugin (`includeBuild`) at the submodule, and includes our own `:tool`
module — the same technique `light-sdk` itself uses for its `examples/*` modules with custom
`projectDir`s.

```
tool/                     — the actual app (this is the only thing Light's build server sees)
  lighttool.toml          — tool id/label/version/permissions
  build.gradle.kts
  src/main/kotlin/com/zacksimpson/emojis/
  src/main/res/drawable/  — custom (non-LightIcons) vector assets
light-sdk/                — git submodule, local build dep only, never shipped
settings.gradle.kts, build.gradle.kts, gradle.properties, gradle/  — root Gradle scaffold
sdk/.gitkeep              — placeholder so Gradle's structural `:sdk` parent project resolves
```

## Key conventions

- **No gray colors, no dividers** — Light Phone design language is strictly black/white. Never use
  `lighten = true` on `LightText`, `LightThemeColors.*.contentSecondary`, or any custom gray.
- **No comments** unless the why is non-obvious.
- Scaling: use the SDK's own `Float.gridUnitsAsDp()` / `Float.designVerticalPxToSp()` /
  `Float.designVerticalPxToDp()` (from `com.thelightphone.sdk.ui`) — these already normalize to
  the LP3's grid, matching every other Light tool.
- Custom icons not in `LightIcons` (e.g. the Recents tab icon) need **two pre-colored vector
  drawables** (`_white`/`_black`), since `LightBarButton.Icon` renders via a plain `Image`, not
  the tint-aware `LightIcon` composable — unlike `LightBarButton.LightIcon`, it won't auto-tint.
- `LightIcons.TOGGLE_ON`/`TOGGLE_OFF` render backwards from their names (checked the raw vector
  paths directly): `TOGGLE_ON`'s knob sits on the *left* of the track, `TOGGLE_OFF`'s on the
  *right*. `ToggleSwitch.kt` deliberately inverts the mapping to compensate. Their intended size
  is `2f` (matches `LightIcon`'s own default and how the SDK's icon gallery renders every icon).

## Architecture

- **Single screen, mode-switching, not multi-screen navigation.** `EmojiToolScreen` is the app's
  only `@InitialScreen`; `EmojiToolViewModel` holds an `EmojiMode` (`Grid`/`Search`/`Recents`/
  `Settings`) that swaps rendered content. `LightScreen.navigateTo()` pushes a real back-stack
  entry, so using it for tab-like switching between our four "screens" would grow the stack every
  time the user tapped between them — this way it doesn't. `navigateTo` is unused; every drill-in
  is a mode switch with a back/close button instead.
- **Cross-cutting state lives in top-level singleton objects**, not the ViewModel — `LightScreen`
  gives every screen instance a fresh `ViewModelStore`, so state that must outlive a single
  screen's ViewModel can't live there:
  - `SelectionStore` — in-memory only (matches the original RN app; never persisted).
  - `RecentsStore` — DataStore-backed (usage counts, recency, sort mode, the "show top used
    preview" setting), behind an authenticator-example-style `getInstance(dataStoreProvider)`
    lazy singleton, since it needs `SealedLightContext.dataStore`, which is only reachable from
    inside a `LightScreen`.
- **`EmojiCell`'s tap handling is a raw `pointerInput` gesture**, not `Modifier.clickable`:
  `clickable` delays/suppresses its press interaction inside scrollable containers (to avoid a
  ripple flash during scroll flings), so a fast tap inside the `LazyVerticalGrid` never reported
  `isPressed` in time for the press-scale animation to render. The gesture mirrors the LP3
  keyboard's own `keyInput` modifier (`awaitFirstDown` → `waitForUpOrCancellation`).
- **Search is a custom screen, not `LightTextInputEditor`** — that component is a submit-only flow
  (type, hit search/Return, see results on a different screen), which doesn't fit live-as-you-type
  filtering. Built from lower-level primitives instead: `LightEmbeddedLp3Keyboard` + our own
  `Lp3RepeatableKeyboardCallback` implementation (the SDK's own `TextInputKeyboardCallback` is
  `internal` to `sdk:ui`, not reusable from `:tool`) driving a `TextFieldState` that the results
  grid and query-text display both read directly.
- **`EMOJI_INDEX` (the ~1800-entry search index) is split across 13 `EmojiIndexPart*.kt` files.**
  Kotlin merges every top-level `val` initializer in one file into a single `<clinit>`, and one
  file with all ~1800 entries blows the JVM's 64KB per-method bytecode limit. The RN app's
  original `utils/emojiData.ts` (and the one-time Node script that converted it) are gone now
  that the rewrite is the only thing in this repo — regenerate by hand or from repo history if the
  emoji set ever needs updating. Do not hand-edit the generated files.
- **Clipboard has no SDK support yet.** `EmojiToolViewModel.copySelection()` is a stub with a
  `TODO(clipboard)` — Light's internal team is aware and working on it. Wire in the real call
  (following the shape of `LightServiceMethod.SetRingtone`) once it ships.

## Dev workflow

```bash
git submodule update --init          # first clone only
./gradlew :tool:installDebug         # fast iteration — default for most changes
./gradlew :tool:installRelease       # verify real feel/perf before calling something done
```

**Always verify perf-sensitive changes (scrolling, animations, tap responsiveness) against a
release build, not debug.** Compose debug builds carry substantial tracing/tooling overhead that
R8 strips in release — on the LP3's hardware this is the difference between ~60% janky frames and
~1%. A debug-only perf complaint that reproduces in release is a real bug; one that doesn't is
just debug-build overhead.

Needs a GitHub PAT with `read:packages` scope for the `lp3keyboard` GitHub Packages dependency —
either `GH_PACKAGES_USER`/`GH_PACKAGES_TOKEN` env vars or `gpr.user`/`gpr.key` in a (gitignored)
`local.properties`.

## Build / release

`tool/lighttool.toml` holds the tool id, label, version, and permissions — Light's build server
generates the manifest from it; there's no hand-written `AndroidManifest.xml`. Bump `versionCode`
for every build submitted to Light.
