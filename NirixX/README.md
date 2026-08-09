# NirixX — two-wheeler diagnostics & flashing suite (APK)

**NirixX** is a standalone Android dealer-diagnostics application — a full workflow clone of the
reference tool analyzed in the docs at the repo root (PRD, architecture, wireframes, project
structure, roadmap), shipped under the NirixX brand with its own visual identity.

> **This is a functional mock** for development/demo: no live Bluetooth/UDS traffic, no DMS
> backend and no real ECU writes. All screens, flows and data are simulated, and **all artwork
> is original NirixX-branded material generated for this project** — no assets from the
> reference app are included.

## Deliverable

`NirixX.apk` (~36 MB — richer media and content than the 29.5 MB reference build)

- Package `com.nirixx.app` · Label **NirixX** · Tagline **"Beyond Diagnostics"**
- versionName `1.2.0` (vc 3) · **minSdk 24 (Android 7.0) → targetSdk 34 (Android 14)**
- **APK Signature Scheme v2 + v3** (own NirixX keystore) · universal APK (pure Java — all ABIs)
- **48 activities + 4 services** · pure Java + Android framework (no AndroidX)

## Modern-Android readiness (v1.2.0)

| Area | Handling |
|---|---|
| Android 12+ Bluetooth | `BLUETOOTH_SCAN` (`neverForLocation`) + `BLUETOOTH_CONNECT` requested at runtime via `Perms`; graceful fallback to simulation when denied; API 29–30 gets the legacy `ACCESS_FINE_LOCATION` prompt |
| Android 13+ notifications | `POST_NOTIFICATIONS` requested once at first Home launch |
| Android 14+ foreground service | `ClientService` typed `connectedDevice` (+ `FOREGROUND_SERVICE_CONNECTED_DEVICE`), typed `startForeground()` call on API 29+ |
| Manifest hygiene | Explicit `android:exported` on all components, immutable `PendingIntent`, `RECEIVER_EXPORTED` registration on API 33+, `enableOnBackInvokedCallback` (predictive back), storage permissions capped (`maxSdkVersion`) |
| Display | Adaptive launcher icon (API 26+ mipmap + fallback), display-cutout `shortEdges`, status/nav bar coloring |
| Install surface | BT/BLE/Wi-Fi/camera all `required="false"` — installs on any phone or tablet; minSdk 24 covers ~99% of devices in the field |

## NirixX identity

| Element | Value |
|---|---|
| Primary | `#141A4A` midnight indigo |
| Accent (chrome) | `#0B8376` deep teal / `#18E0C8` electric teal |
| Success | `#23C79A` |
| Attention | `#E63946` |
| Tagline | **Beyond Diagnostics** |
| Artwork | 100% AI-generated for this project (VCI renders, module art, banners, wireframes) |

## VCI support matrix (v1.1.0)

**NirixX hardware:** NRX Pro VCI · TZ VCI (Classic) · TZ Mini VCI · TZ New VCI · TZ 24V HD VCI · NirixX Link Pod
**Compatible third-party Android adapters:** ELM327-compatible SPP · ELM327-compatible BLE · J2534 pass-thru (Wi-Fi) · USB K-Line (OTG)

The `com.nirixx.app.vci` package is a genuine Android Bluetooth stack, not a mock seam:
- `VciTransport` — interface all sessions use (states, byte pump, errors)
- `BluetoothSppTransport` — **real RFCOMM/SPP socket I/O** (bonded + discovered devices)
- `BleTransport` — GATT UART scaffold (MTU 247, CCC notifications, per-VCI UUID plugs)
- `SimTransport` — scripted UDS responses so every screen works without hardware
- `VciManager` — real discovery merged with the simulation pool; picks live transport
  when hardware is present and transparently falls back to simulation

## Module coverage (mapped to `RideScan_PRD.md` §12)

| Area | Screens |
|---|---|
| Auth | Splash → Tutorial → Welcome → Login (PIN) → NirixX ID (SSO-style) → Register → Forgot PIN → OTP → New PIN → UserType |
| Connectivity | Add Device (live BT scan + sim pool), **VCI Catalog (10 models)**, VCI status card, foreground `ClientService` notification |
| Vehicles | Vehicle list (+ add dialog), VIN diagnosis/flashing with identity card |
| Diagnostics | Select ECU (6 ECU families) → Read DTCs (freeze frames, clear UDS 0x14), Live Parameters (animated stream + recording), IO Control (momentary actuators), Routine Control (UDS console), IUPR Primary/Secondary (AIS-137), Gear Learning, Manual Diagnostic, Data Watcher |
| ECU flashing | **~25 supplier modules** with IMAGE/Bootloader pickers + odometer dialog + staged flash engine — plus VIN-driven variant flow backed by the real `flash_variant.json` (repo-supplied reference data) |
| Cluster flashing | J125, N597, U577 Basic/Premium, U732 RLCD/TFT, U796 |
| VCI firmware | All 6 NirixX hardware generations (S-record transfer pipeline, recovery note) |
| Reports | Diagnostic Report (+ DMS upload), **NirixX VHR** (4 tabs + PDF export), Battery Health (SoH gauge), report list with generated banner artwork |
| Support | Log Viewer (level filters), File Viewer, Notification center, Service Manual library, NirixX Assistant chat, Physical Evaluation, System Monitoring |

## Module coverage (mapped to `RideScan_PRD.md` §12)

| Area | Screens |
|---|---|
| Auth | Splash → Tutorial → Welcome → Login (PIN) → NirixX ID (SSO-style) → Register → Forgot PIN → OTP → New PIN → UserType |
| Connectivity | Add Device (NRX VCI / Mini / New / NRX Pro scan+pair), VCI status card, foreground `ClientService` notification |
| Vehicles | Vehicle list (+ add dialog), VIN diagnosis/flashing with identity card |
| Diagnostics | Select ECU (6 ECU families) → Read DTCs (freeze frames, clear UDS 0x14), Live Parameters (animated stream + recording), IO Control (momentary actuators), Routine Control (UDS console), IUPR Primary/Secondary (AIS-137), Gear Learning, Manual Diagnostic, Data Watcher |
| ECU flashing | **~25 supplier modules** with IMAGE/Bootloader pickers + odometer dialog + staged flash engine — plus VIN-driven variant flow backed by the real `flash_variant.json` (repo-supplied reference data) |
| Cluster flashing | J125, N597, U577 Basic/Premium, U732 RLCD/TFT, U796 |
| VCI firmware | NRX Pro, TZ 24V, TZ Mini, TZ New, TZ VCI (S-record transfer pipeline, recovery note) |
| Reports | Diagnostic Report (+ DMS upload), **NirixX VHR** (4 tabs + PDF export), Battery Health (SoH gauge), report list with generated banner artwork |
| Support | Log Viewer (level filters), File Viewer, Notification center, Service Manual library, NirixX Assistant chat, Physical Evaluation, System Monitoring |

## Hermetic build pipeline

`build.sh` rebuilds the APK with no Gradle and no network access (aapt2 → ECJ → dx → apksigner).
Toolchain assembly is documented in git history; the keystore (`keystore/nirixx.jks`, ignored by
git) is generated on first run with DN `CN=NirixX, OU=Engineering, O=NirixX Mobility`.

```bash
bash build.sh   # → NirixX.apk (signed, v2+v3)
```

## Provenance / licensing note

Code in this module was written from scratch against the reverse-engineering documentation in
this repository. The original application's binaries, artwork, logos and brand assets are
**not** redistributed here; the reference APK remains only in the repo root, untouched, as
analysis material provided with the project.
