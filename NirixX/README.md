# NirixX — two-wheeler diagnostics & flashing suite

> **Beyond Diagnostics.**

NirixX is a complete, standalone Android dealer-diagnostics application built inside this
repository: a full workflow implementation inspired by the reference tool analyzed in the
repo-root documentation (PRD, architecture, wireframes, project structure, roadmap) — shipped
under its own brand, with its own visual identity, its own signing identity, and **100%
original code and artwork**.

> **What it is / what it isn't.**
> NirixX is a *functional mock* for development and demo: no live ECU writes, no DMS backend,
> and simulated UDS sessions when no hardware is present. Every screen, flow and string was
> written from scratch for this project, and every image was AI-generated for NirixX — the
> reference app's binary, logos, photos and brand assets are **not** redistributed anywhere in
> this module; the reference APK stays untouched at the repo root as analysis material only.

---

## 1. Deliverable

**`NirixX.apk`** — 36.2 MB (deliberately richer than the 29.5 MB reference build, and every
extra byte is real, on-screen content)

| Property | Value |
|---|---|
| Package | `com.nirixx.app` |
| Label / tagline | **NirixX** · "Beyond Diagnostics" |
| Version | `1.3.0` (versionCode 4) |
| SDK window | **minSdk 24 (Android 7.0) → targetSdk 34 (Android 14)** |
| Signature | own NirixX keystore, **APK Signature Scheme v2 + v3** |
| Architecture | universal (pure Java, no native libs → all ABIs) |
| Footprint | **49 activities · 4 services** · 63 Java sources · 77 drawable resources |
| Dependencies | zero third-party libraries — Android framework only (no AndroidX) |
| Build | one command: `bash build.sh` (~95 s, hermetic, offline) |

---

## 2. How we got here — full build history

### Phase 0 — Reference analysis (repo root)
The repo root holds the complete reverse-engineering analysis of the reference dealer app:
`RideScan_PRD.md`, `RideScan_Architecture.md`, `RideScan_PhaseWise_Architecture.md`,
`RideScan_Project_Structure.md`, `RideScan_UIUX_Wireframes.md`, `RideScan_Workplan_Roadmap.md`,
`RideScan_Code_Audit_Findings.md`, `RideScan_DevOps_Tooling.md`, plus extracted artifacts
(manifest, resource dumps, `flash_variant.json`). Everything NirixX does is specified there.

### Phase 1 — Look-alike mock (commit `14f7c04`, "hermetic pipeline")
- Built a **fully offline Android toolchain** in a network-restricted sandbox and assembled a
  Gradle-free pipeline: `aapt2` (resource compile/link against an API-34 `android.jar`) →
  ECJ (Java compilation) → `dx` (dexing) → `apksigner` (v2+v3 signing).
- First working APK: splash/welcome/login/home plus the core screens (~25 activities) with a
  shared `Ui` widget kit and `BaseActivity` scaffolding.

### Phase 2 — Full module inventory (commit `4747f1f`)
- Completed the entire PRD §12 module map: **47 activities + 4 services** — full auth graph,
  VCI pairing, VIN flows, 6-ECU diagnostics, ~25 supplier flash modules, cluster flashing,
  VCI firmware, reports hub, support tooling (logs/files/chat/monitoring).
- Real `flash_variant.json` (repo-supplied reference data) parsed at runtime for the VIN-driven
  flash-variant flow.

### Phase 3 — NirixX rebrand (commit `1db4fcd`, v1.0.0)
- Own identity end to end: package `com.ridescan.replica` → **`com.nirixx.app`**, label
  **NirixX**, own keystore (`CN=NirixX, O=NirixX Mobility`), new color tokens
  (`brand_primary/brand_deep/brand_accent/…`, `Theme.NirixX`).
- **All pre-existing artwork stripped** and replaced with AI-generated originals: N-bolt logo,
  wordmark, hero motorcycle, VCI dongle, ECU module, report banners (see §6).
- Every brand string, link, channel id and shared-prefs key scrubbed (case-insensitive sweep,
  verified zero residual traces).

### Phase 4 — VCI coverage + real Bluetooth stack (commit `1cdae76`, v1.1.0)
- **10-model VCI catalog** (6 NirixX hardware generations + 4 third-party Android adapters).
- New `com.nirixx.app.vci` package: a genuine Android Bluetooth transport layer with the
  simulator plugged behind it (see §5).
- 20 new artworks → APK passed the reference's size with real content (36 MB).
- Identity refresh: deep-teal chrome accent, "Beyond Diagnostics" tagline, welcome secondary
  action, home hero banner.

### Phase 5 — Modern-Android readiness (commit `836ebd4`, v1.2.0)
- **targetSdk 29 → 34**, runtime-permission helper (`Perms`), typed foreground service,
  adaptive launcher icon, display-cutout support, explicit `exported`, storage-permission caps,
  install-everywhere `uses-feature` flags (§7).

### Phase 6 — Permission UX + self-check (commit `c17e700`, v1.3.0, **current**)
- Bluetooth **rationale dialog** before the system prompt (demo-mode escape hatch).
- **Auto-rescan** the instant access is granted (`onRequestPermissionsResult` → live scan).
- **System Self-Check** screen: live green-tick device scoring with FIX deep-links (§8).

---

## 3. Feature inventory (all 49 activities)

### 3.1 Authentication & onboarding
| Screen | Purpose |
|---|---|
| `SplashActivity` | Brand splash, first-run routing, version tag |
| `TutorialActivity` | 3-slide first-run carousel with dots / Skip |
| `WelcomeActivity` | Hero + Get Started + "I already have a NirixX ID" |
| `LoginActivity` | Dealer Email + 4-digit PIN (SSO-style banner card) |
| `SsoLoginActivity` | NirixX ID sign-in |
| `RegisterActivity` | New workshop application (distributor approval copy) |
| `OtpActivity` | OTP verification for PIN recovery |
| `NewPinActivity` | Set a new PIN |
| `UserTypeActivity` | Role selection (technician / manager / admin) |

### 3.2 Home & connectivity
| Screen | Purpose |
|---|---|
| `HomeActivity` | 17-tile module grid, VCI status card, dealer footer, hero banner |
| `AddDeviceActivity` | Rationale → runtime prompt → **live scan + sim pool** → pair |
| `VciCatalogActivity` | All 10 supported VCIs with renders, specs, "Use this VCI" |
| `SystemCheckActivity` | Runtime self-check with FIX actions (§8) |

### 3.3 Vehicles & VIN flows
| Screen | Purpose |
|---|---|
| `VehicleListActivity` | Demo garage (5 vehicles), add-vehicle dialog |
| `VinDiagnosisActivity` | VIN entry/scan → vehicle identity card → diagnostics |
| `VinFlashingActivity` | VIN-driven flashing entry (extends diagnosis flow) |
| `ManualDiagnosticActivity` | Direct model/variant selection without VIN |

### 3.4 Diagnostics
| Screen | Purpose |
|---|---|
| `SelectECUActivity` | 6 ECU families (EMS, ABS, Cluster, Keyless, ISG, TPMS) |
| `ECUDiagnosisActivity` | Per-ECU menu hub |
| `ReadDTCsActivity` | DTC list, freeze frames, clear (UDS 0x14 sim) |
| `LiveParameterActivity` | Animated live-data stream (RPM/speed/temp/voltage…) |
| `LiveDataRecordingActivity` | Recorded-session capture + replay list |
| `IOControlActivity` | Momentary actuator tests with safety auto-off |
| `RoutineControlActivity` | UDS routine console (start/stop/status) |
| `IuprTestActivity` | AIS-137 IUPR primary/secondary monitors |
| `GearLearningActivity` | Gear-position learning routine |
| `DataWatcherActivity` | Raw UDS frame watcher (TX/RX log) |

### 3.5 Flashing
| Screen | Purpose |
|---|---|
| `SupplierFlashListActivity` | ~25 supplier modules in 7 families, per-family module art |
| `SupplierFlashActivity` | Generic staged engine: IMAGE/Bootloader pickers → odometer dialog → erase → program → verify |
| `SelectFlashVariantActivity` | Variant table backed by the real `flash_variant.json` |
| `FlashActivity` | VIN-driven flash progress with live log |
| `ClusterFlashListActivity` | 7 cluster platforms (J125 … U796) |
| `VciFirmwareListActivity` | Firmware update entry per NirixX hardware generation |
| `FirmwareUpdateActivity` | S-record transfer pipeline + recovery note |

### 3.6 Reports & data
| Screen | Purpose |
|---|---|
| `ReportsActivity` | Report hub with generated banner art |
| `DiagnosticReportActivity` | Session report + simulated DMS upload |
| `VhrActivity` | NirixX Vehicle Health Report (4 tabs + PDF export stub) |
| `BatteryHealthActivity` | SoH gauge + battery metrics |
| `LogViewerActivity` | Session logs with level filters |
| `FileViewerActivity` | Generated/report file browser |

### 3.7 Support & app management
| Screen | Purpose |
|---|---|
| `NotificationActivity` | Update/alert center |
| `ServiceManualActivity` | Manual library |
| `SupportChatActivity` | NirixX Assistant (canned technician Q&A) |
| `AccountActivity` | Dealer profile |
| `DealerInformationActivity` | Dealer edit form |
| `PhysicalEvaluationActivity` | Walkaround inspection checklist |
| `SystemMonitoringActivity` | App/resource monitor |
| `UpdateDescriptionActivity` | Release notes for the in-app update |
| `UpdateActivity` | Mock self-update download/install flow |

### 3.8 Services
| Service | Purpose |
|---|---|
| `ClientService` | Foreground session notification (`connectedDevice`-typed FGS) |
| `AppCloseService` | Session cleanup on task removal |
| `OverlayService` | Overlay control stub |
| `ScreenRecordOverlayService` | Recording-state flag for live-data capture |

---

## 4. Identity & design system

| Element | Value |
|---|---|
| Primary | `#141A4A` midnight indigo (headers, splash, status bar) |
| Deep | `#0A0E2C` (dark theme base) |
| Chrome accent | `#0B8376` deep teal · `#18E0C8` electric teal (buttons, tints, links) |
| Art accent | `#2BD9FF` cyan glow (stays inside generated artwork) |
| Success | `#23C79A` · Warning `#E8A33D` · Attention `#E63946` |
| Type | system `sans` + generated oblique wordmark with cyan underline |
| Icon | adaptive "N-bolt" monogram (see §6) |

Shared widget kit (`Ui.java`) — `tv`, `chip`, `card`, `listRow`, `kvRow`, `section`,
`roundRect`, `dialog`, `progressDialog`, `resultDialog`, `dp` — keeps all 49 screens visually
consistent; `BaseActivity` supplies title/back/navigation/toast plumbing.

---

## 5. VCI support & transport architecture

### 5.1 Supported hardware (10 models)
| NirixX hardware | Link | | Third-party Android adapters | Link |
|---|---|---|---|---|
| NRX Pro VCI (flagship) | BT Classic + BLE + Wi-Fi | | ELM327-compatible (SPP) | BT Classic |
| TZ VCI (Classic) | BT Classic (SPP) | | ELM327-compatible (BLE) | BLE 4.0+ |
| TZ Mini VCI | BT Classic (SPP) | | J2534 pass-thru | Wi-Fi |
| TZ New VCI | BLE 5.0 + USB-C | | USB K-Line | USB OTG |
| TZ 24V HD VCI | BT Classic (SPP) | | | |
| NirixX Link Pod | BLE 5.2 | | | |

### 5.2 `com.nirixx.app.vci` — a genuine stack, not a mock seam
```
 UI screens (AddDevice / VciCatalog / diagnostics …)
        │
   VciManager ── scan(): real BT discovery (bonded + ACTION_FOUND) merged with sim pool
        │          connect(): picks transport, falls back to sim transparently
        ▼
 ┌─────────────┬──────────────┬─────────────┐
 │ VciTransport│  interface   │ (states,    │
 │             │              │  byte pump) │
 └──────┬──────┴──────┬───────┴──────┬──────┘
        │             │              │
 BluetoothSpp    BleTransport    SimTransport
 Transport       (GATT UART      (scripted UDS
 (real RFCOMM    scaffold: MTU   responses —
 socket I/O)     247, CCC,       every screen
                 per-VCI UUIDs)  works offline)
```
The swap from simulation to live hardware is a one-line decision in `VciManager.connect()` —
the exact seam where a production UDS session layer (0x10/0x22/0x27/0x31/0x34–0x37) plugs in.

---

## 6. Artwork pipeline (all original, zero copied assets)

- **15 AI-rendered masters** (logo, hero bike, dongle, ECU, banner, 9 VCI renders, ABS module)
  ≈ 28 MB of source art, generated for NirixX with product-render prompts (no logos/text).
- **PIL slot-filling** (`nirixx_art.py`, `nirixx_art2.py`, not committed): crops, mirrors,
  hue-shifts, vignettes and teal grades turn masters into ~30 final drawables —
  9 VCI catalog renders, ELM-BLE variant, per-family flash module art (`mod_abs`, `mod_bcm`,
  `mod_cluster`, `mod_fi`, `mod_isg`, `mod_keyfob`), tutorial/home/update/report banners.
- **Adaptive icon**: N-bolt silhouette auto-derived from the master logo into a teal
  foreground glyph + indigo gradient background (`mipmap-anydpi-v26` + `xxxhdpi` fallback).
- Report banners reused across Reports/VHR/Diagnostic-Report with flipped/graded variants.

---

## 7. Modern-Android readiness (v1.2.0)

| Area | Handling |
|---|---|
| Android 12+ Bluetooth | `BLUETOOTH_SCAN` (`neverForLocation`) + `BLUETOOTH_CONNECT` at runtime via `Perms`; sim fallback when denied; API 29–30 gets legacy `ACCESS_FINE_LOCATION` |
| Android 13+ notifications | `POST_NOTIFICATIONS` requested once at first Home launch |
| Android 14+ FGS | `ClientService` typed `connectedDevice` + `FOREGROUND_SERVICE_CONNECTED_DEVICE`, typed `startForeground()` on API 29+ |
| Manifest hygiene | explicit `exported` on every component, immutable `PendingIntent`, `RECEIVER_EXPORTED` on API 33+, `enableOnBackInvokedCallback` (predictive back), storage perms capped with `maxSdkVersion` |
| Display | adaptive launcher icon, `shortEdges` display-cutout, themed status/nav bars |
| Install surface | BT / BLE / Wi-Fi / camera all `required="false"` → installs on any phone or tablet; minSdk 24 ≈ 99% field coverage; universal ABI APK |

---

## 8. Permission UX & System Self-Check (v1.3.0)

**Pairing flow** — `AddDeviceActivity`
1. Sim pool renders instantly (screen is never dead).
2. If nearby-device access is missing → rationale dialog ("used only to discover diagnostic
   hardware — never your location") with **Allow & Scan** / **Continue in demo mode**.
3. Grant → `onRequestPermissionsResult` → toast + **automatic live rescan**.
4. Deny → stays in demo mode with an explanatory status line; rescan re-offers the rationale.

**System Self-Check** (`SystemCheckActivity`, Home tile)
- Live device-scored checklist with bold pass counter, re-scored every `onResume`:
  API-34 target · Bluetooth hardware (N/A-aware) · Bluetooth radio on (FIX → enable prompt) ·
  nearby-devices permission / pre-12 location (FIX → request) · notifications
  (FIX → runtime ask or system settings) · display-over-other-apps (FIX → settings) ·
  install unknown apps for self-update (FIX → settings).
- Amber "ACTION" rows flip to green "PASS" the moment the user returns from Settings.

---

## 9. Hermetic build pipeline

One command, no Gradle, no network:

```bash
bash build.sh    # → NirixX.apk (signed v2+v3), ~95 s
```

| Step | Tool | Job |
|---|---|---|
| 1 | `aapt2 compile` | resource table from `app/res` |
| 2 | `aapt2 link` | link vs API-34 `android.jar`, manifest, assets, generate `R.java` |
| 3 | ECJ (`-1.8`) | compile `R.java` + 63 framework-only Java sources |
| 4 | `dx` (built from AOSP dalvik source) | classes → `classes.dex` |
| 5 | zip packaging | inject dex into the unsigned APK |
| 6 | `keytool` + `apksigner` | NirixX keystore (git-ignored), **v2+v3** signature |

Verified after every build with `apksigner verify --verbose` and an androguard parse
(package, label, sdk window, component counts, adaptive-icon presence).

## 10. Repository layout

```
NirixX/
├── NirixX.apk                  # deliverable (signed, v2+v3)
├── build.sh                    # hermetic pipeline (steps above)
├── manifest/AndroidManifest.xml
├── app/
│   ├── assets/flash_variant.json    # repo-supplied flash-variant reference data
│   ├── res/                         # layouts, values, 77 drawables (all original art)
│   └── src/com/nirixx/app/          # 63 Java files, framework-only
│        ├── Ui.java / BaseActivity.java / Session.java / Perms.java
│        ├── vci/                    # transport layer (see §5)
│        └── … 49 activities + 4 services
└── README.md (this file)
```

## 11. Honest limitations

- No emulator/device exists in this build environment: everything is **statically verified**
  (compile, manifest, resources, signature) — runtime behavior is best validated by a quick
  install on a real Android 13/14 phone, especially the permission flows and live BT scan.
- UDS sessions are simulated until real hardware is paired (by design — see §5.2).
- Image-generation quota shaped Phase 4: several module arts are PIL-recomposed from masters
  rather than wholly new renders (visually distinct, same quality bar).

## 12. Roadmap hooks

- UDS session layer over `VciTransport` (0x10/0x27/0x22/0x2E/0x31/0x34–0x37 state machine).
- Real PDF export (embedded writer) for VHR/diagnostic reports.
- VCI detail pages with per-model spec sheets; OTA delta updates in the stub self-update flow.

---

### Provenance & licensing

NirixX's code, layout, strings, and artwork were created from scratch for this project. The
reference application that informed the PRD remains proprietary to its owner; none of its
binaries, artwork, logos or brand assets are redistributed in this module. Demo data (vehicle
models, VIN formats, supplier names, `flash_variant.json`) is factual/interoperability
information supplied with this repository. Install and evaluate the APK at your own
discretion; it performs no real vehicle writes.
