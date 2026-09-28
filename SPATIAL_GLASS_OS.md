# NEXUS — SPATIAL GLASS OS

> **"A futuristic communication operating system built from liquid glass, layered depth, light, and subtle 3D spatial environments."**

---

## 1. Product Vision & Visual Philosophy

NEXUS is an operating system experience for personal human communication. Rather than presenting a flat collection of traditional Android screens, NEXUS renders relationships as living spatial entities connected by light, depth, and glass physics.

### Visual Hierarchy

1. **Content** — Names, digits, status, timestamps, and intent are the most immediate visual layer.
2. **Glass Material** — Frosted, refractive, physical surfaces with top-down specular light catches and hairline borders that hold content.
3. **Spatial Depth** — Multi-tiered elevation layering (dock $\rightarrow$ cards $\rightarrow$ HUD $\rightarrow$ active call overlay).
4. **3D Environment** — Subdued volumetric spatial background with gentle dust motes and orbiting liquid glass orbs.
5. **Motion** — Purposeful, spring-damped kinetic physics; never distracting, fully respects reduced motion.
6. **Decoration** — Kept to a minimum; every visual filament, glow, or caustic carries functional or semantic meaning.

---

## 2. Color System & Design Tokens

### Foundations
| Token | Hex | Role |
| :--- | :--- | :--- |
| **Background (Default)** | `#0B0C10` | Deep near-black graphite foundation with cool slate undertones |
| **Background (Gradient Start)** | `#0D0F16` | Top atmospheric graphite gradient stop |
| **Background (Gradient End)** | `#08090D` | Bottom atmospheric graphite gradient stop |
| **Obsidian Background** | `#000000` | True pitch-black mode for pure AMOLED power conservation |
| **Text Primary** | `#F0F1F6` | Soft white high-contrast text |
| **Text Secondary** | `#8E91A0` | Muted cool gray for subheads, metadata, and secondary status |
| **Text Tertiary** | `#565967` | Low-contrast graphite gray for auxiliary timestamps and borders |
| **Accent Primary** | `#829FFF` | Restrained cool electric blue / icy violet range |
| **Accent Glow** | `#829FFF` (18% alpha) | Volumetric back-glow for active interactions |
| **Danger / Missed** | `#FF5A5A` | Restrained coral red with specular halo for missed events & end call |

### Contextual Light Tints
Contacts and interaction types are contextually tinted across translucent glass surfaces:
* **Violet:** `#C0A6FF` (Inner Orbit / Favorites)
* **Blue:** `#78B7FF` (Core Work / Direct Collaborators)
* **Amber:** `#FFB668` (Recent frequent signals)
* **Jade:** `#72D2B0` (Active voice sessions)
* **Lavender:** `#D6C6FF` (Family constellation)
* **Azure:** `#8BB5FF` (General communication network)

---

## 3. Four-Tier Glass Material System (`NexusGlassSurface`)

Rather than relying on expensive, battery-draining real-time GPU blur passes, NEXUS implements a **physical edge-lit glass system** using multi-stop linear gradients, top specular highlights, hairline borders, and elevation shadow offsets:

```
┌────────────────────────────────────────────────────────┐  ◄── Top Specular Light Highlight (0.5dp)
│                                                        │
│  [ Glass Surface Gradient: rgba(255,255,255, 0.08) ]   │
│                          ↓                             │
│  [ Glass Surface Gradient: rgba(255,255,255, 0.03) ]   │
│                                                        │
└────────────────────────────────────────────────────────┘  ◄── Hairline Base Border (rgba(255,255,255, 0.07))
   ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░     ◄── Ambient Depth Shadow (Elevation Offset)
```

### Material Tiers

| Tier | Surface Alpha | Border Specular | Application |
| :--- | :--- | :--- | :--- |
| **Primary** | `8%` $\rightarrow$ `3%` | `14%` $\rightarrow$ `5%` | Standard background cards, profile overview, events list |
| **Secondary** | `12%` $\rightarrow$ `5%` | `20%` $\rightarrow$ `8%` | Elevated items, slider tracks, dialer keypad container |
| **Floating** | `16%` $\rightarrow$ `7%` | `28%` $\rightarrow$ `10%` | Highest depth: Floating Priority card, incoming call overlay, HUD |
| **Minimal** | `5%` $\rightarrow$ `2%` | `10%` $\rightarrow$ `4%` | Filter pills, status badges, tiny chips |

---

## 4. Liquid Glass Orb System

In Spatial Glass OS, contacts are no longer heavy stone planets. They are **Translucent Liquid Glass Orbs** featuring physical optics:

* **Refractive Fresnel Rim:** High-intensity grazing angle rim light highlighting spherical curvature.
* **Internal Fluid Caustic:** Subtle rotating caustic shimmer simulating light refraction through fluid.
* **Top Specular Catch:** Crisp specular light glint from the environment's primary light source.
* **Translucent Volumetric Falloff:** Soft center transparency revealing ambient depth behind the sphere.
* **Orbit Vectors & Constellations:** Hairline glass filaments linking related people in social clusters.

### GLSL Zero-Allocation Shaders
The 3D OpenGL ES 2.0 / 3.0 renderer computes these properties per-pixel without any garbage collection or memory allocations:
```glsl
// Liquid Glass Orb Fragment Shader excerpt
vec3 N = normalize(vNormal);
vec3 V = normalize(vViewVec);
float NdotV = max(dot(N, V), 0.0);
float fresnel = pow(1.0 - NdotV, 2.8);

// Specular environment glint
vec3 lightDir = normalize(vec3(0.3, 0.8, 0.6));
vec3 H = normalize(lightDir + V);
float spec = pow(max(dot(N, H), 0.0), 32.0);

// Fluid Caustic Modulation
float caustic = sin(vTexCoord.x * 12.0 + uTime * 1.5) * cos(vTexCoord.y * 12.0 + uTime * 1.2) * 0.12;

vec3 finalColor = uColor.rgb * (0.35 + caustic) + fresnel * uColor.rgb * 0.85 + vec3(spec * 0.7);
float finalAlpha = clamp(uColor.a * (0.25 + fresnel * 0.75 + spec * 0.5), 0.0, 1.0);
```

---

## 5. Screen Architectures

### 1. UNIVERSE (Home Command Screen)
* **Glass Header:** Soft white time display, daily interaction telemetry, and subtle quick actions.
* **Floating Priority Card:** Displays highest-affinity contacts connected by an active network filament with a traveling light pulse.
* **Events Timeline Card:** Chronological communication history with luminous rail and shape-coded markers (solid incoming, hollow outgoing, red halo missed).
* **Floating Dial Trigger:** Liquid glass action button docked in the ergonomic thumb corner.

### 2. ORBIT (3D Spatial Environment)
* **Zero-Engine Overhead:** Pure Android SDK OpenGL ES 2.0/3.0.
* **6-DOF Navigation:** Drag to pan orbit azimuth/elevation, pinch to dolly zoom, raycast tap to select.
* **Floating Glass HUD:** Selecting an orb deploys a floating glass card with contact identity, communication recency, and direct call/message triggers.
* **Perspective Switcher:** Seamless toggle between **Universe (3D)**, **Orbit (2D)**, and **List** modes.

### 3. SIGNAL (Dialer)
* **Precision Frosted Keypad:** Frosted glass digit keys with dual alphanumeric glyphs and tactile response.
* **Radar Wavefront Canvas:** Emits concentric wave rings on keypress.
* **T9 Predictive Matching:** Real-time contact filtering based on dialed digits.
* **Carrier Lock Card:** Appears when dialed digits identify a known node.

### 4. EVENTS (Activity History)
* **Unified Spatial Timeline:** Vertical glass rail linking chronological communication events.
* **Liquid Glass Orb Thumbnails:** Each row features a miniature liquid glass orb tinted to the contact's affinity color.
* **Frosted Filter Chips:** Quick categorization (`ALL`, `MISSED`, `CALLS`, `MESSAGES`).

### 5. CONTACT PROFILE
* **Identity Glass Card:** Hero `LiquidGlassOrb` with 3D concentric orbit rings, name, phone number, and favorite badge.
* **Glass Communication Statistics:** Grid displaying Total Calls, Average Call Duration, Total Messages, and Weekly Frequency, segmented by hairline vertical glass dividers.
* **Communication DNA:** Full chronological audit log with distinct glyph markers.

### 6. INCOMING CALL (Primary Wow Experience)
* **Continuous Material Transition:** Ambient background deepens, illuminating the primary glass layer.
* **Hero Liquid Glass Orb:** Contact orb emerges with a calm, breathing atmospheric pulse.
* **Liquid Glass Thumb Slider:** Ergonomic swipe-to-answer control with a translucent glass thumb, charging luminous wash, and haptic snap threshold.

### 7. ACTIVE CALL (Shared Space)
* **Shared Orbital Plane:** Two connected liquid glass orbs (YOU Core + Contact Orb) linked by an animated fluid harmonic energy stream with pulsing energy beads.
* **Floating Glass Control Tray:** Precision glass toggles for Mute, Speaker, Keypad, Add Call, and Hold.
* **In-Call DTMF Sliding Keypad:** Frosted glass keypad for automated telephone trees with live digit accumulator.

---

## 6. Thinking Orbs Native Integration (`NexusOrb`)

NEXUS natively integrates the visual language, 3D projection mathematics, and particle dynamics of Jakub Antalik's open-source `thinking-orbs` project (MIT License) under the **Spatial Glass OS** philosophy:
> **LIQUID GLASS + LIGHT + PARTICLES**

Rather than generic AI spinners, `NexusOrb` transforms the orb into a physical, living communication primitive where internal fluid caustics, Fibonacci lattices, laser filaments, and contextual tints represent genuine telephony and relationship states.

### 8 Semantic Communication States
| State | Semantic Role | Mathematical / Visual Behavior |
| :--- | :--- | :--- |
| `Idle` | Standby / Universe / Settled Profile | Concentric Fibonacci rings, calm breathing period, low filament density, subtle caustic shift |
| `Connecting` | Signal acquisition / Outgoing call / Reconnect | Dynamic Fibonacci web, circulating orbital particles with speed proportional to signal acquisition |
| `Searching` | T9 search active / Directory filter | Focused directional filaments sweeping across sphere, particle density biasing toward query vector |
| `Solving` | Target lock acquired / Resolving caller | Counter-rotating filament rings, quarter-turn solve cycles, rhythmic core luminosity pulse |
| `Listening` | Active voice session / Remote party speaking | 3D surface caustics react to simulated audio amplitude, subtle expansion of particle field |
| `Composing` | Message composition / Quick response selection | Multi-strand braided ribbon filaments weaving across poles, undulating ribbon phase |
| `Responding` | Incoming call alert / Incoming transmission | Outward particle emissions, brightened core luminosity, propagating filament wave fronts |
| `Shaping` | Screen transition / Contact selection | Arc-length morphing between geometric profiles (sphere $\rightarrow$ oblate $\rightarrow$ prolate $\rightarrow$ relaxed) |

### Zero-Allocation Rendering Pipeline
* **Flat Primitive Buffers (`OrbFrameBuffer`):** All 3D coordinates, projected 2D coordinates, alphas, and radii are stored in contiguous `FloatArray`s and `IntArray`s pre-allocated at initialization.
* **In-Place Insertion Sort:** Particle indices are sorted back-to-front using depth ascending insertion sort directly on primitive arrays, maintaining $O(N)$ efficiency for nearly-sorted frames with **0 bytes GC allocation per frame**.
* **Accessibility & Battery Conservation:** Under `reducedMotion` (or `LocalReducedMotion.current`), animations halt and park deterministically at `t = 0.6f`, avoiding continuous battery drain.

### Microbenchmark & Performance Verification
Benchmarked with 1,000 frames on Android VM / JUnit:
| Benchmark | Target | Measured Result | Margin |
| :--- | :--- | :--- | :--- |
| **1 Orb** | $< 3.0\text{ ms}$ | **$0.1248\text{ ms}$** | **$24\times$ faster** |
| **3 Orbs** | $< 6.0\text{ ms}$ | **$0.1271\text{ ms}$** | **$46\times$ faster** |
| **10 Orbs** | $< 14.0\text{ ms}$ | **$0.7177\text{ ms}$** | **$19\times$ faster** |
| **GC Allocations** | $0\text{ B / frame}$ | **$0\text{ B / frame}$** | **Exact match** |

---

## 7. Performance & Quality Guarantees

* **60–120 FPS Sustained Rendering:** Native OpenGL ES and Compose Canvas loops guarantee zero object allocations in hot draw paths.
* **Zero Runtime Blur Overhead:** Physical multi-stop gradient highlights emulate frosted glass optics without the 15ms GPU cost of `RenderEffect.createBlurEffect`.
* **Battery Conservation:** True Obsidian dark mode achieves near-zero power draw on AMOLED displays.
* **Accessibility:** WCAG AA contrast compliance across all text layers; touch targets $\ge 48\text{dp}$; complete support for `reducedMotion`.
* **Open Source Attribution:** Includes full attribution to Jakub Antalik for `thinking-orbs` animation principles under the MIT License.

