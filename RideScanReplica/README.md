# RideScan — Replica APK

A **visual/functional look-alike** of the original RideScan dealer-diagnostics app
(`com.ridescantp.mahle.ridescantp`, analyzed in the docs at the repo root), rebuilt as a
standalone, installable Android app for demonstration and prototyping purposes.

> **This is a mock.** No real Bluetooth/VCI communication, no backend DMS calls, no real
> ECU flashing. All diagnostic/flashing flows are simulated UI with realistic data.

## Deliverable

`RideScan-Replica.apk` (**built artifact, ~3 MB**)
- Package: `com.ridescan.replica` · Label: **RIDE Scan 2.0** · versionName `2.3.6`
- minSdk **24** · targetSdk **29** · signed with **APK Signature Scheme v2 + v3**
- Pure Java + Android framework (no AndroidX dependencies)

## Screens reproduced (25 activities)

- **Onboarding:** Splash (black, RIDE Scan logo) → Welcome (Ronin hero) → Dealer Login
  (email + 4-digit PIN, Forgot PIN → OTP → reset dialog)
- **Home:** MAHLE-blue toolbar, VCI connection status card, 12-feature tile grid
  (VIN Diagnosis / VIN Flashing / Troubleshooting / ECU Flashing / Health Reports /
  Battery Health / VCI Firmware / Vehicle List / Service Manual / Notifications /
  Account / Health Scanner), dealer footer
- **Connectivity:** Add Device — scan → pair TZ VCI / Mini / New / TechPRO (mock)
- **VIN flow:** VIN entry + scanner prompt → vehicle identity card → Select ECU
- **Diagnostics:** ECU feature list matching the original — Read DTCs (freeze frames,
  clear-all confirm), Live Parameters (real-time simulating values + recording marker),
  IO Control (momentary actuator switches), Routine Control (monospace session console),
  IUPR Primary/Secondary monitors (AIS-137 counters)
- **Flashing:** Select Flash Variant — backed by the **real bundled `flash_variant.json`**
  → staged flash pipeline (connect → security access → battery check → erase → program
  → checksum → flash date) with ring+bar progress, abort/retry confirmation, success
  dialog → Diagnostic Report
- **Reports:** Diagnostic Report, Vehicle Health Report (4-tab layout: Dealer /
  Diagnostic / IO Control / Summary), Battery Health (SoH gauge), File Viewer stubs,
  DMS upload simulation
- **Lifecycle:** VCI Firmware Update (TechPRO) with staged S-record transfer,
  Notification center, Service Manual library, Account/Logout

## Design language

Reproduced from the original APK assets:
- Brand colors: MAHLE blue `#001F5A`, button blue `#00347E`, light blue `#C6D6E3`,
  TVS red `#E4002B`
- Original artwork extracted from the reference APK: `ridescanlogo`, `welcome_bike`,
  `login_bike1`, `logo`, `vciimage` (TechPRO), `tvs_logo`, launcher icon + 27 vector
  icons (decompiled from binary AXML and reused as-is)

## Build (hermetic, no Gradle / no network)

```bash
bash build.sh     # → RideScan-Replica.apk
```

Pipeline (tools live outside the repo):
1. **aapt2** (static binary from apktool's prebuilt) — compile + link resources
2. **ECJ 4.6.1** — compiles Java (runs on a PyPI-bundled Temurin JRE, needs no full JDK)
3. **dx** — dexer compiled from `aosp-mirror/platform_dalvik` sources with ECJ
4. **python zipfile** — packages `classes.dex` into the APK
5. **apksigner 32.0.0** jar (Sketchware-Pro) — v2+v3 signing; keystore via JRE `keytool`
6. `android.jar` (API 34) from `Sable/android-platforms`

Regenerate everything from scratch: re-run `build.sh` (idempotent; keystore created once).

## Structure

```
RideScanReplica/
├── build.sh                  # build pipeline
├── manifest/AndroidManifest.xml
├── app/
│   ├── res/                  # layouts, values, shapes, original icons/artwork
│   ├── assets/flash_variant.json   # real variant→ECU→flash-file map from the APK
│   └── src/com/ridescan/replica/   # 28 Java classes (framework-only)
└── RideScan-Replica.apk      # the ready-to-install artifact
```
