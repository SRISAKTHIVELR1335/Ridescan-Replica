# NirixX — two-wheeler diagnostics & flashing suite (APK)

**NirixX** is a standalone Android dealer-diagnostics application — a full workflow clone of the
reference tool analyzed in the docs at the repo root (PRD, architecture, wireframes, project
structure, roadmap), shipped under the NirixX brand with its own visual identity.

> **This is a functional mock** for development/demo: no live Bluetooth/UDS traffic, no DMS
> backend and no real ECU writes. All screens, flows and data are simulated, and **all artwork
> is original NirixX-branded material generated for this project** — no assets from the
> reference app are included.

## Deliverable

`NirixX.apk` (~4.7 MB)

- Package `com.nirixx.app` · Label **NirixX** · versionName `1.0.0` (vc 1)
- minSdk 24 · targetSdk 29 · **APK Signature Scheme v2 + v3** (own NirixX keystore)
- **47 activities + 4 services** · pure Java + Android framework (no AndroidX)

## NirixX identity

| Element | Value |
|---|---|
| Primary | `#141A4A` midnight indigo |
| Deep | `#0A0E2C` |
| Accent | `#2BD9FF` electric cyan |
| Steel | `#4B7BFF` |
| Attention | `#E63946` |
| Icon/wordmark | generated N-bolt monogram + DejaVu oblique wordmark with cyan underline |

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
