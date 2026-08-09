# RideScan — Replica APK (full build)

A **complete visual/functional look-alike** of the original RideScan dealer-diagnostics app
(`com.ridescantp.mahle.ridescantp`, analyzed in the docs at the repo root — PRD, architecture,
wireframes, project structure, roadmap), rebuilt as a standalone Android app.

> **This is a functional mock** of the real product: no live Bluetooth/UDS traffic, no DMS
> backend and no real ECU writes. Flows, screens, artwork, strings and data are reproduced
> from the reference APK for development/demo purposes.

## Deliverable

`RideScan-Replica.apk` (~4.7 MB)
- Package `com.ridescan.replica` · Label **RIDE Scan 2.0** · versionName `2.3.6` (vc 2)
- minSdk 24 · targetSdk 29 · **APK Signature Scheme v2 + v3**
- **47 activities + 4 services** · pure Java + Android framework (no AndroidX)

## Module coverage (mapped to `RideScan_PRD.md` §12)

| Area | Screens |
|---|---|
| Auth | Splash → Tutorial → Welcome → Login (PIN) → SSO → Register → Forgot PIN → OTP → New PIN → UserType |
| Connectivity | Add Device (TZ VCI / Mini / New / TechPRO scan+pair), VCI status card, foreground `ClientService` notification |
| Vehicles | Vehicle list (+ add dialog), VIN diagnosis/flashing with identity card |
| Diagnostics | Select ECU (6 ECU families) → Read DTCs (freeze frames, clear UDS 0x14), Live Parameters (animated stream + recording), IO Control (momentary actuators), Routine Control (UDS console), IUPR Primary/Secondary (AIS-137), Gear Learning, Manual Diagnostic, Data Watcher |
| ECU flashing | **~25 supplier modules** (Conti/Conti2, ABS Conti/BABS/BABS2/KABS, Pricol ×8, Sedemac ×3, KEMS/Mikuni CAN/KEMS Recovery, Keihin Sport, INEL, ISG ×3, Keyless, Ronin) with IMAGE/Bootloader pickers + odometer dialog + staged flash engine — plus VIN-driven variant flow backed by the real `flash_variant.json` |
| Cluster flashing | J125, N597, U577 Basic/Premium, U732 RLCD/TFT, U796 |
| VCI firmware | TechPRO, TZ 24V, TZ Mini, TZ New, TZ VCI (S-record transfer pipeline, recovery note) |
| Reports | Diagnostic Report (+ DMS upload), **MOTOSHIELD VHR** (4 tabs + PDF export), Battery Health (SoH gauge), report list with real template artwork |
| Support | Log Viewer (level filters), File Viewer, Notification center, Service Manual library, RIDE Scan Chatbot, Physical Evaluation, System Monitoring |
| Lifecycle | App self-update flow (release notes → "Downloading File..." → install stub, Skip(5)), `AppCloseService`, `ScreenRecordOverlayService`, `OverlayService` |

## Design authenticity

- Brand palette pulled from the APK's own `resources.arsc`: MAHLE blue `#001F5A`,
  button blue `#00347E`, light blue `#C6D6E3`, TVS red `#E4002B`
- Original artwork extracted from the reference APK: RIDE Scan logo, Ronin hero images,
  TechPRO VCI photo, TVS logos, launcher icon, **MOTOSHIELD VHR PDF templates**, and
  27 vector icons decompiled from binary AXML and reused unchanged
- Real strings mirrored (`flash_warning_msg`, `skip_5`, `welcome_CB`, OTP copy,
  `Select IMAGE type`/`Select Booloader type`, erase-request text, VCI-update caption…)
- Real data: the bundled `flash_variant.json` from the original APK drives the
  flash-variant picker (26 supplier flash files, hardware-revision notes included)

## Build (hermetic, no Gradle / no network)

```bash
bash build.sh     # → RideScan-Replica.apk
```

| Stage | Tool | Source |
|---|---|---|
| Resource compile+link | aapt2 2.19 (static ELF) | apktool 2.4.1 prebuilts (npm `apktool-jar`) |
| Bootclasspath | android.jar API 34 | `Sable/android-platforms` (GitHub blob API) |
| Java compile | ECJ 4.6.1 | `SciAps/nexus` git-hosted maven mirror |
| DEX | dx (built from source) | `aosp-mirror/platform_dalvik` via ECJ |
| Signing | apksigner 32.0.0 (+JRE keytool) | `Sketchware-Pro/Sketchware-Pro` app/libs |
| JVM | Temurin JRE | PyPI `jdk4py` |

## Structure

```
RideScanReplica/
├── build.sh                       # idempotent build pipeline
├── manifest/AndroidManifest.xml   # 47 activities, 4 services, BT/camera/location perms
├── app/
│   ├── res/                       # layouts, theme values, decompiled original icons/artwork
│   ├── assets/flash_variant.json  # real variant→ECU→flash-file map (from the APK)
│   └── src/com/ridescan/replica/  # 51 Java classes, framework-only
└── RideScan-Replica.apk           # installable artifact
```
