# Verification report

## Scope

Version 1.0.2 changes English UI text, functional display names, documentation and release version identifiers. The native engine and pairing guards are unchanged from 1.0.1. Physical TV–Apple connectivity is **not tested**.

Automatic requests up to 1080p/30; Smooth targets 1080p/60; experimental 4K targets 3840×2160/30 with hardware HEVC; Compatible is capped at 720p/30. Unsupported hardware falls back to lower profiles. Software decoding is capped at 720p/30. These are requested upper targets, not measured output.

## Existing automated evidence

The signed 1.0.1 release passed 16 unit tests: 9 receiver-state tests and 7 quality-policy tests. State tests cover readiness prerequisites, actual names, registration/permission errors, late callbacks, restart, PIN clearing, UTF-8 names and controlled timeout. Quality tests cover target/fallback selection, no viable profile, defaults, persistence/legacy preferences and valid/invalid SurfaceView dimensions. Host tests do not exercise actual device MediaCodec capability queries.

Previous unchanged-native ASan/UBSan HTTP/RTSP and PIN/SRP tests passed: fragmented RTSP, request limits, RNG failure, out-of-order pairing, unverified zero-key GCM, short/wrong proof, zero public key and cleanup. These are not Apple pairing interoperability tests.

1.0.1 APK checks passed: package ID, SDK levels, version, ARM64/ARMv7, non-debuggable, TV launcher/banner/icon, prohibited permissions, v2/v3 signature, ZIP 16 KB alignment, ELF LOAD/ARM64 RELRO, DT_NEEDED closure and preserved 22 native definitions / 22 callbacks plus log and Graphics Path JNI symbols. The certificate matches the earlier official release.

## Source archive validation

The original clean Git-free source build rebuilt both native ABIs, including OpenSSL, in 84 tasks / 16m45s. 1.0.1 rebuilt Kotlin/Compose in the same Git-free validation directory, reusing unchanged native outputs, and completed in 2m14s. Every archived file was byte-compared against work sources and that directory. Keystores, credentials, Git, caches and outputs are excluded; Maven/JDK/SDK tools are separate requirements.

1.0.2 was built in the Git-free validation directory with unchanged native outputs reused. Final source/archive byte comparisons are recorded in the delivery verification files.

## Not tested

- Physical installation/update, launcher, remote focus/navigation, large text, SurfaceView changes, HOME/Back, reopening and on-device preference retention.
- iPhone/iPad/Mac discovery, correct/wrong PIN, cancellation, real 1080p/60 or 4K/30 output, audio mirroring, orientation changes, second-source rejection, consecutive connections, Wi-Fi/Ethernet recovery and 10–15 minute sessions.
- Direct HLS and audio-only playback separately; upstream regression scenarios #54, #50, #49, #51, #53 and #41.

No authorized Android device was present during the earlier release checks. A TV API 33 emulator startup attempt did not produce a working TV session. The build-host Mac was not tested as a sender to a physical TV. No successful unchanged upstream reference APK was produced in the three pre-customization attempts. No new device testing is claimed for the English text update.

## Final 1.0.2 result

The retry build completed in 7m 56s. All 16 unit tests passed. The signed universal APK passed signature, versionCode 3 / version 1.0.2, English application label, manifest, both ARM ABIs, native dependencies, ZIP/ELF alignment and JNI/R8 checks. The signing certificate matches previous official releases. All 55 maintained screen strings are English, and the visible launcher brand lettering was removed. PIN remains enabled by default. Resource inputs were compared with the build directory.

The first attempt failed when the disk filled while writing a debug classes JAR. Redundant generated files were removed. The successful retry used a temporary 1536 MB Gradle heap override; the source configuration retains its usual 2048 MB limit. No native or application logic was changed to bypass the failure. Build record: `verification/english-release-retry.log` in the delivery outputs. Physical TV tests remain not run.
