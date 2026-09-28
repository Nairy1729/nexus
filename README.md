# NEXUS — Futuristic Personal Phone & Communication Interface

<p align="center">
  <img src="screenshots/phase1_5_universe_clean.png" width="280" alt="3D Universe" />
  <img src="screenshots/phase1_5_planet_focus.png" width="280" alt="Planet Focus & HUD" />
  <img src="screenshots/phase1_5_active_call_shared_orbit.png" width="280" alt="Shared Orbit Call" />
</p>

> **"People are celestial bodies; communication is gravitational connection."**

**NEXUS** is an Android personal communication interface built from the ground up with **Kotlin**, **Jetpack Compose**, and a hardware-accelerated **OpenGL ES 2.0 / 3.0** spatial engine. It rethinks the mobile dialer as a living, spatial communication graph — minimal, AMOLED-focused, ultra-responsive, and purposeful without sacrificing daily usability.

---

## What's New in Phase 1.5: 3D Spatial Universe

### 🪐 1. Real-Time 3D OpenGL ES Universe
- **Zero Third-Party Engine Overhead:** Built purely with Android SDK OpenGL ES 2.0/3.0. +0 KB binary bloat, zero GC allocations per frame, running at 60–120 FPS.
- **Central Luminous Core:** You occupy the gravitational center of your communication universe.
- **Procedural Planetary Bodies:** Contacts render as 3D celestial bodies with custom GLSL Fresnel rim lighting, atmospheric glow, and surface latitude banding.
- **Dynamic Orbital Shells:** Contacts dynamically orbit across 3 depth shells ($r_1, r_2, r_3$) earned by interaction frequency and recency.
- **Constellation Filaments:** Favorites (`FAMILY`, `CORE`) are bound by geometric 3D energy lines.
- **Missed Call Eclipses:** Unreturned contacts (e.g. Zoya) appear in an eclipse state with an active reddish corona.
- **Cinematic 6-DOF Gestures:** Drag-to-rotate, pinch-to-zoom, and screen-to-world raycasting tap-to-focus with smooth exponential camera damping.
- **3-Way Perspective Toggle:** Effortlessly switch between **Universe (3D)**, **Orbit (2D)**, and **List** modes.

### 📡 2. Signal Acquisition Dialer
- **Radar Wavefront Ripples:** Keypad emissions produce concentric wave pulses across the radar canvas.
- **Emitted Tone Tracking:** Live signal telemetry counter with tactile feedback.
- **Signal Locking:** Instant carrier lock card with direct call trigger when digits match a known node.

### ⚡ 3. Shared Orbit & In-Call Telephony
- **Incoming Signal:** `INCOMING SIGNAL // ENTERING ORBIT` full-screen incoming radar experience with ergonomic thumb-zone slider.
- **Shared Orbit:** Active calls link YOU and the CALLER in a shared orbital plane bound by an active energy beam.
- **In-Call DTMF Touch Tones:** Sliding keypad for automated phone trees with real-time digit accumulator.
- **Post-Call Landings:** Calls automatically transition into the recipient's Communication DNA history.

---

## Design System

- **Accent Identity:** Single electric lime `#CBFF4D` accent on deep true AMOLED blacks (`#000000`).
- **Safety Semantics:** Danger red (`#FF5A4D`) reserved exclusively for missed events, eclipse coronas, and call decline.
- **Glassmorphism:** Frosted translucent glass HUDs, pill buttons, and floating dock with subtle borders.
- **Ergonomics & Accessibility:** Strict compliance with $\ge 48\text{dp}$ touch targets, semantic roles, content descriptions, and reduced motion awareness.

---

## Visual Showcase (Phase 1.5)

| 3D Universe Overview | Planet Focus & DNA HUD | 2D Orbit (Preserved) |
|:---:|:---:|:---:|
| <img src="screenshots/phase1_5_universe_clean.png" width="220"/> | <img src="screenshots/phase1_5_planet_focus.png" width="220"/> | <img src="screenshots/phase1_5_orbit_2d.png" width="220"/> |

| Signal Acquisition Dialer | Incoming Signal Orbit | Shared Orbit Active Call |
|:---:|:---:|:---:|
| <img src="screenshots/phase1_5_dialer_acquiring.png" width="220"/> | <img src="screenshots/phase1_5_incoming_signal.png" width="220"/> | <img src="screenshots/phase1_5_active_call_shared_orbit.png" width="220"/> |

| Command Center (Home) | Alphabetical Directory | Communication DNA Profile |
|:---:|:---:|:---:|
| <img src="screenshots/phase1_5_home.png" width="220"/> | <img src="screenshots/phase1_5_list.png" width="220"/> | <img src="screenshots/phase1_5_ended.png" width="220"/> |

---

## Architecture & Tech Stack

```
com.nexus
├── app/                 NexusApp (shell, navigation graph, floating dock, call router)
├── core/
│   ├── spatial/         Phase 1.5 3D Universe Engine
│   │   ├── model/       UniverseState, SpatialContactMapper, CelestialBody, Constellations
│   │   ├── renderer/    UniverseGLRenderer, GLShaders (Fresnel/Corona), Mesh, ShaderUtil
│   │   └── ui/          UniverseView (Compose AndroidView bridge & 6-DOF touch controller)
│   ├── design/          Reusable design system components (NexusDock, NexusButton, NexusChip)
│   ├── theme/           Unified token system (Color, Type, Radii, Spacing, Sizes)
│   ├── animation/       NexusMotion (easing specs, durations, staggers)
│   ├── haptics/         NexusHaptics (intent-driven tactile feedback)
│   └── di/              AppContainer (manual dependency injection)
├── data/
│   ├── model/           Domain models (Contact, CallRecord, ResolvedCall, etc.)
│   ├── contacts/        ContactRepository & MockData
│   └── calls/           CallLogRepository
├── feature/             Screen implementations (home, people, contact, dialer, call, activity)
└── telephony/           CallSessionController (state machine & navigation command router)
```

- **Kotlin & Jetpack Compose (Compose BOM 2026.03.01)**
- **Android OpenGL ES 2.0 / 3.0 via GLSurfaceView**
- **Android Gradle Plugin (AGP) 9.4.1**
- **Android Navigation Compose**
- **Material Icons Extended**
- **Min SDK 29 / Target SDK 36 / Compile SDK 37**

---

## Building & Running

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Nairy1729/nexus.git
   cd nexus
   ```

2. **Build and install with Gradle:**
   ```bash
   ./gradlew assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Launch the application:**
   ```bash
   adb shell am start -n com.nexus.phone.debug/com.nexus.app.MainActivity
   ```

---

## Documentation

For a detailed walkthrough of the 3D celestial mapping, shaders, zero-allocation renderer, and mathematical models, refer to [PHASE1_5_SUMMARY.md](PHASE1_5_SUMMARY.md).
