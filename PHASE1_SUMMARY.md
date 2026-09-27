# NEXUS — Phase 1 Summary

**Status:** Complete · **Commit:** `3d99603` (master, root commit) · **Date:** 2026-09-27
**Scope:** Production-quality Android phone/dialer UI prototype with mock data — design system first, no real telephony.

---

## 1. Objective (master prompt)

Build **NEXUS**, a premium futuristic Android phone/dialer app (Kotlin + Jetpack Compose), Phase 1 only:

- Seven screens: **Home, People (Orbital Contacts), Contact Profile, Dialer, Incoming Call, Active Call, Activity**.
- Design language: AMOLED-black / near-black, **one** accent color (lime `#CBFF4D`), dark/obsidian/light themes on a single token system, large typography, circular geometry, restrained glow/blur, no neon/gradients/Material-generic look.
- Core concept: *people are nodes; communication is the connection* — orbital contact system, Communication DNA timeline, spatial hierarchy, meaningful motion (fast, purposeful, reduced-motion aware), intentional haptics.
- Usability wins over gimmicks: conventional 3×4 keypad, bottom dock acceptable if visually integrated, touch targets ≥ 48dp, accessibility (content descriptions, no color-only signaling, semantics/custom actions), 60fps targets, ~360dp width support.
- Architecture: clean separation (core / feature / data / telephony), business logic outside composables, reusable `Nexus*` components, realistic mock data.
- **No fake telephony pretending to work** — call flow is UI-driven via `CallSessionController` commands only. Phases 2–4 (real contacts, telephony, intelligence) out of scope.

---

## 2. Toolchain & Project Setup

| Item | Choice |
|---|---|
| AGP | 9.4.1 |
| Gradle wrapper | 9.6.0 |
| Kotlin | Built-in (no `kotlin-android` plugin); KGP pinned **2.3.20** via root buildscript classpath for the Compose plugin |
| Compose | BOM **2026.03.01** (UI 1.11.2) |
| SDKs | compileSdk 37 · targetSdk 36 · **minSdk 29** · Java 17 |
| Navigation | navigation-compose 2.10.2 |
| Icons | material-icons-extended |
| DI | **Manual `AppContainer`** (no Hilt) · Persistence: none (no Room) |
| applicationId | `com.nexus.phone` (debug: `com.nexus.phone.debug`) · namespace `com.nexus.app` |

Environment notes:

- `local.properties` uses **forward slashes** (`sdk.dir=C:/Users/nairy/AppData/Local/Android/Sdk`) — mandatory on Windows.
- Build command: `.\gradlew.bat :app:assembleDebug --console=plain` (PowerShell, daemon warm ~8–15s incremental, config cache reused).
- Screenshot capture must use `cmd /c "adb exec-out screencap -p > file.png"` — **PowerShell `>` corrupts binary output**.

---

## 3. Architecture

```
com.nexus
├── app/          NexusApp (shell: nav graph, dock, call-command router), MainActivity, Routes
├── core/
│   ├── design/   11 reusable Nexus* components
│   ├── theme/    Color · Type · Dimensions · Theme (token system)
│   ├── animation/ NexusMotion (easings, durations, staggers)
│   ├── haptics/  NexusHaptics (tick/confirm/press, reduced-motion aware)
│   ├── ui/       Modifiers (nexusClickable), TimeFormat
│   └── di/       AppContainer
├── data/
│   ├── model/    Contact · CallRecord · ResolvedCall · MessageRecord · CommunicationStats
│   ├── contacts/ ContactRepository / MockContactRepository (T9 matching) / MockData
│   └── calls/    CallLogRepository / MockCallLogRepository
├── feature/      home · people · contact · dialer · call · activity (screens + ViewModels)
└── telephony/    CallSessionController (interface) + MockCallSessionController (state machine)
```

### Key architectural rules

1. **All navigation decisions live in `NexusApp.kt`.** Screens emit intents (`onCallBack`, `onAnswer`…); the call session emits `CallCommand`s (`ShowIncoming` → incoming route, `ShowActive` → active, `Dismiss` → pop if top, `Finish` → dismiss call screens then land on the contact profile unless already there). Phase 3 can swap mock telephony without touching feature UI.
2. **Business logic lives in ViewModels**, never composables: `viewModel(factory = XViewModel.factory(container))`; `ContactViewModel` keyed `contact-$contactId`.
3. **Manual DI** via `AppContainer` passed into `NexusApp(container)`.
4. **Semantics/accessibility** baked into components: roles, content descriptions, `onClick`, custom actions (orbit nodes expose "Call" to TalkBack), no color-only signaling.

---

## 4. Design System

### 4.1 Color tokens (`core/theme/Color.kt`)

- Accent lime **`#CBFF4D`**, `accentContent`/`accentText` on-accent **`#0A0A0C`**.
- Light theme `accentText` **`#4E7A00`** (darkened for AA contrast on paper).
- Danger **`#FF5A4D`** — used **only** for missed calls and decline/end-call.
- Three palettes on one token set: **Dark (AMOLED)**, **Obsidian**, **Light (paper)** — resolved by `NexusThemeMode`, system default until an in-app picker is added.

### 4.2 Typography & dimensions

- `Type.kt`: large display-first scale (`headline`, `title`, `subhead`, `meta`, `micro`…).
- `Dimensions.kt`: spacing scale (`x1…x6`, `gutter`), radii (`pill`, `lg`), sizes — **touch targets ≥ 48dp** (`touchMin`), `actionLg = 76dp`, `dockHeight`, `dockClearance`.

### 4.3 Motion (`core/animation/NexusMotion`)

- Easings: `emphasized`, `standard`, `decelerate`, `accelerate`.
- Durations: press 100 · quick 160 · **standardMs 240** · relaxed 320 · cinematic 480.
- Staggers: `listStagger 26` · `radialStagger 34`.
- Helpers: `quickSpec()` / `standardSpec()` (renamed from `quick`/`standard` to avoid collision with the easing vals).
- **Reduced motion** provided via `LocalReducedMotion`; content is never gated behind animation.

### 4.4 Haptics (`NexusHaptics`)

- Intent-based: `tick()` (arm/disarm), `confirm()` (call fired), press feedback — wired through `LocalNexusHaptics`.

### 4.5 Components (11 files in `core/design/`)

| Component | Purpose |
|---|---|
| `NexusGlassSurface` / `NexusGlass` | Fake frosted glass — 2-stop translucent gradient + hairline border + top edge highlight. **Deliberately not RenderEffect blur** (perf). |
| `NexusAvatar` | Circular initials/photo avatar, sizes `avatarXs…avatarLg` |
| `NexusButtons` | `NexusActionButton` with styles: Accent / Glass / Outline |
| `NexusChip` / `SegmentedToggle` / `ChipRow` | Filters and view switching |
| `NexusDialKey` | Circular keypad key (digit + letters), 3×4 grid |
| `NexusCallControl` / `TextAction` | Active-call grid controls |
| `NexusTimeline` | Communication DNA events (solid= incoming, hollow = outgoing, red = missed, diamond = message) |
| `NexusTextField` / `NexusSearchField` | Inputs with `nexusClickable` support |
| `NexusContactNode` | Orbit node (avatar + label + semantics) |
| `NexusDock` | Floating glass pill, 4 areas, sliding accent indicator |
| `NexusSwipeToAnswer` | Swipe-to-answer with tap fallback for accessibility |

---

## 5. The Seven Screens

### 5.1 Home (`feature/home/HomeScreen.kt`)
Header greeting, "interactions today" glass card, **PRIORITY PEOPLE** row, recent activity feed, prototype **simulate-incoming-call** trigger, dock.

### 5.2 People — Orbital (`feature/people/PeopleScreen.kt`) — signature screen
- Pure function **`orbitSlots()`**: 3 rings, tiers by weight rank (0 inner, 1–2 inner, 3–5 mid, 6+ outer), ring radii `outer × 0.64/0.82/1.0`, staggered start angles.
- Canvas draws rings, spokes (you ↔ each person), and the central **well** ("YOU") with a breathing animation.
- **Gesture grammar:** tap = open profile · drag-into-well = call · long-press = call.
- Hand-rolled **single `orbitGestures` pointerInput** so one touch can never fire two intents; interrupted drag cleans up **without firing** (`onDragCancel`).
- Direct manipulation: raw offset tracks the finger, a sprung offset trails visually; arming the well triggers `haptics.tick()`, release fires `haptics.confirm()` + call.
- **Orbit/List segmented toggle** — List view = searchable alphabetical rows (T9 search field, `NoMatches` empty state).

### 5.3 Contact Profile (`feature/contact/ContactProfileScreen.kt`)
Hero avatar, name/number, copy-number action (clipboard), FAVORITE/CALL/MESSAGE actions, 4-stat grid (**142 calls · 04:12 avg · 214 messages · 14 this week**) separated by hairline fade dividers, "last interaction / missed" row, **COMMUNICATION DNA** timeline with legend and event count. Dock hidden here.

### 5.4 Dialer (`feature/dialer/DialerScreen.kt`)
- **Conventional 3×4 keypad** with circular keys — usability won. The radial "black-hole" concept was **documented as rejected** and survives only as entry choreography.
- **`BirthOnEnter`**: keys bloom from the grid center outward — delay = `hypot` distance from center × `radialStagger`, once per screen entry.
- Number display with backspace; **dynamic T9 match row** (avatar + name + number + inline call button).
- Call button state: **Outline at rest** ("Call. Enter a number first.") → **Accent when digits exist** — state reads before you touch it.
- Long-press `0` appends `+`.

### 5.5 Incoming Call (`feature/contact/IncomingCallScreen.kt`)
Radial wash canvas, caller identity, **swipe-to-answer** (with explicit tap fallback: *"Swipe the button to the right, or tap"*), decline in danger red only. Resolves the contact via `container`.

### 5.6 Active Call (`feature/call/ActiveCallScreen.kt`)
Identity block, live timer, **2×3 control grid** (Mute · Keypad · Video · Bluetooth · Hold · Add call · Profile), danger-colored end-call — the only red on screen.

### 5.7 Activity (`feature/activity/ActivityScreen.kt`)
Day groups (**TODAY / YESTERDAY**), filter chips (ALL · MISSED · INCOMING · OUTGOING), search, per-event rows with call-back actions, contact avatars.

### 5.8 App shell (`app/NexusApp.kt`)
- `Routes` object; NavHost: `home / people / dial / activity / contact/{id} / incoming / active`.
- Theme mode state, haptics + reduced-motion `CompositionLocalProvider`s, background `Surface` inside theme.
- `CallCommand` collection → navigation (see §3.1).
- **Dock** in `AnimatedVisibility` (slide + fade): visible on the four areas, hidden on contact/call screens.
- Navigation helpers: `navigateSingleTop`, `gotoArea` (`popUpTo(home) { saveState }` + `launchSingleTop` + `restoreState`), `dismissIfTop`, `dismissCallScreens`.
- Call helpers: `callContact`, `callFrom`, `callDialString` — the single place UI intents become session commands.

---

## 6. Mock Data

- **11 contacts** (Rahul Menon, Mom, Dad, Priya Nair, Amit Sharma, Zoya Khan, Sneha, Raj Kulkarni, Rakesh Iyer, Vikram Rao, Studio) with realistic Indian numbers, labels, favorites, weekly-interaction weights.
- **30 call records** (incoming/outgoing/missed with timestamps), **12 messages** — enough to populate Activity day groups, Contact DNA timelines, and Home feeds.
- `MockCallLogRepository` + `MockContactRepository` (T9 matching: digits → names via letter mapping).

---

## 7. Telephony Boundary (no fake telephony)

```
CallSessionController (interface)
├── session: StateFlow<CallSession?>        // Ringing → Connecting → Active → Ended
├── commands: Flow<CallCommand>             // ShowIncoming | ShowActive | Dismiss | Finish(contactId?)
├── startOutgoing(contactId, number, displayName)
├── simulateIncoming(contactId)             // prototype trigger
├── accept() / decline() / end()
└── elapsedSeconds: StateFlow<Int>

MockCallSessionController — in-memory state machine only.
```

Nothing pretends to place a real call; the entire flow is UI-driven by `CallCommand`s routed in `NexusApp`.

---

## 8. Verification

### 8.1 Build
- First `assembleDebug` **BUILD SUCCESSFUL** (44s) after initial compile-error fixes.
- Second build after writing all screens: 8 errors → fixed:
  - Column `horizontalArrangement` → `horizontalAlignment` (DialerScreen)
  - Missing `animateFloat` import; `longPressTimeout` → `longPressTimeoutMillis` (PeopleScreen)
  - **`density` shadowing** in PeopleScreen Canvas (outer `LocalDensity` val shadowed `DrawScope.density`) → `1.dp.toPx()`
  - **Missing closing brace in `NexusApp.kt`** — turned every top-level helper into a "local function" (21 errors from one brace) → brace added.
- Final: **BUILD SUCCESSFUL**, only 2 deprecation warnings (`Icons.Rounded.Backspace` → AutoMirrored, `LocalClipboardManager` → `LocalClipboard`).

### 8.2 On-emulator review (Pixel 3a · API 34 · swiftshader)

All seven screens screenshot-reviewed in **dark AMOLED** mode; key flows exercised end-to-end via adb:

| Flow | Result |
|---|---|
| Dock navigation across all four areas | ✅ indicator slides, semantics report `selected` |
| **Orbit drag-to-call** (dragged Rahul's node into the well) | ✅ haptic arm → outgoing call launched |
| Orbit tap → contact profile | ✅ |
| People Orbit ⇄ List toggle | ✅ |
| Dialer T9 typing (9-8-7-6-5) | ✅ match row "Rahul Menon" + Call button Outline → Accent |
| Dialer call → Active → End (no match digits) | ✅ dismisses to prior screen |
| Simulate incoming → Incoming Call | ✅ radial wash, decline/answer |
| **Swipe-to-answer** | ✅ → Active Call with running timer |
| Active Call → End (contactId=rahul) | ✅ `Finish` routed to Rahul's profile |
| Back from profile → dock returns | ✅ |

Notes discovered during review:

- Emulator defaulted to **light night mode** → app correctly rendered the paper theme (light theme verified working); switched AVD to `cmd uimode night yes` for the primary dark review.
- Red status-bar icons are system-side, not app colors.
- PowerShell `>` corrupts `adb exec-out` binary output — always use `cmd /c` redirection.

---

## 9. Design Decisions Log (prompt compliance)

| Prompt requirement | Decision / evidence |
|---|---|
| One accent color | Lime `#CBFF4D` only; danger `#FF5A4D` restricted to missed/decline/end |
| No gradients/neon/Material look | Glass faked with translucent gradient + hairline; no Material bars/cards |
| Conventional keypad | 3×4 grid kept; radial "black-hole" **rejected and documented**, retained only as entry choreography |
| Bottom dock acceptable if integrated | Floating glass pill w/ sliding accent indicator — visually part of the system |
| Motion meaningful & reduced-motion aware | NexusMotion easings/durations; `LocalReducedMotion` gates staggers/washes, content never hidden |
| Intentional haptics | `NexusHaptics` intents (arm tick, call confirm, press) |
| ≥48dp targets, accessibility | `touchMin`, semantics roles/contentDescriptions/custom actions, tap fallback on swipe-to-answer |
| No fake telephony | `CallSessionController` boundary; mock state machine; all routing via `CallCommand` |
| Business logic outside composables | ViewModels per feature; pure functions for geometry (`orbitSlots`) |

---

## 10. Deliverables & Repo State

- **Commit `3d99603`** on `master` — 61 files, ~6,545 lines:
  - Scaffold: settings/root build scripts, version catalog, wrapper, manifest, resources
  - Data layer (models, mock repositories, mock datasets)
  - Telephony boundary (interface + mock)
  - Design system (theme, motion, haptics, 11 components)
  - 6 feature packages (7 screens + 6 ViewModels)
  - App shell (NexusApp, MainActivity)
- `.gitignore` excludes `build/`, `.gradle`, `local.properties`, `.idea`.
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk` (install on `com.nexus.phone.debug`).
- Review screenshots retained in `C:\Users\nairy\AppData\Local\Temp\opencode\` (`dark.png`, `people_orbit.png`, `people_list.png`, `dial_typed.png`, `incoming2.png`, `answered.png`, `finish_profile.png`, …).

---

## 11. Known Follow-ups (not blockers)

1. **Light-theme dock contrast** — white glass pill on paper background is soft; strengthen border/shadow.
2. **Deprecations** — migrate `Icons.Rounded.Backspace` → `Icons.AutoMirrored.Rounded.Backspace`; `LocalClipboardManager` → `LocalClipboard`.
3. **In-app theme switcher UI** — `NexusThemeMode` plumbing exists (Dark/Obsidian/Light), no picker yet.
4. **Performance pass** — 60fps verification (Macrobenchmark / jank stats) not yet run.
5. **Phases 2–4** untouched by design: real contacts, telephony, intelligence.
