# Upstream sources and adaptations

Base: [Android AirPlay Server](https://github.com/jqssun/android-airplay-server), tag `v0.0.31`, commit `c8defdd70d7e6a04f4f1b71d353653682d594106`, GPL-3.0. Application ID remains `app.salon.receiver` for update compatibility.

## Pinned native inputs

| Component | Version / commit |
| --- | --- |
| UxPlay | `462153392f2e30937424922039ff9f0cda5e7b1a` |
| FFmpeg | `38b88335f99e76ed89ff3c93f877fdefce736c13` (static ALAC decoder; network disabled) |
| libplist | `f41b1ea67045e0c09339974d83e389972d84f166` |
| openssl-cmake | `4edd36a8dab5f85a8f92650b5bdf6e0cab13aab8` |
| OpenSSL | 3.4.4; local archive SHA-256 `7bdf55ac20f2779e99e5eca306f824fad2b37dee5a06cc35ed5a8b85a6060010` |
| Oboe | 1.9.3; `b15f5e39c01a7ada306d959e5129620b145fb8b4` |
| AndroidX Graphics Path | 1.0.1; `8a05a22af450d589ef911d772a001a49dcb05b71` |

## Preserved implementation

UxPlay RAOP/AirPlay, SRP/PIN and pair-verify; a single Android NsdManager DNS-SD advertisement path; MediaCodec and EGL/SurfaceView video; native AAC/ALAC plus Oboe audio; Media3 direct video/HLS. JNI class names are retained. Mirroring, HLS and audio-only playback are distinct paths.

## Receiver adaptations

Remote-friendly Compose TV interface; visible-Activity-bound receiver; network events; actual advertisement names and registration results; generation-based callback invalidation; first-rendered-frame events; audio focus; bounded diagnostics; native PIN guards and second-source rejection. Fixed PIN fallback was removed; native pair-verify and SETUP/RECORD authentication gates were added. SRP proof and payload lengths are validated. Unicode names use standard UTF-8 across JNI. Another source cannot stop mirroring through HLS.

The six upstream UxPlay patches remain applied. Build preparation no longer requires checkout/apply or resets: sources are materialized and patched. The local OpenSSL archive is hash-checked. AGP 8.10.1 and stable Media3 1.11.1 are used. Build concurrency is constrained and each shared native library is independently checked for alignment.

Boot auto-start, overlay, PiP, media recording and debug UI were removed from the product flow. Sender volume does not change the TV system volume. Pairing client keys remain in server memory; server restart may require pairing again.

The native HTTP/RTSP parser limits URL length to 8 KiB, headers to 64 KiB / 128 pairs and body size to 16 MiB. Protocol/version fragments are accumulated safely. Host ASan/UBSan tests cover byte-fragmented RTSP and size limits.

The active-source guard uses an authenticated or media-established RAOP owner, not connection count. Additional channels from the same network address and IPv6 zone may be accepted; a different source’s takeover/setup is rejected with 409.

SRP guards reject out-of-order requests, unverified zero-key-derived GCM data, short proofs and zero client public keys. Random private-key generation failure fails pairing. A native allocation leak on the invalid public-key path was fixed.

## Native packaging and 16 KB alignment

OpenSSL builds only `libssl.a` and `libcrypto.a` and links statically into the receiver; separate libssl.so/libcrypto.so and CLI tools are unnecessary. Algorithm options are retained.

Oboe 1.9.3 is built from the same pinned source using NDK 27 instead of a prebuilt AAR. `c++_static` links into the single receiver library, avoiding the NDK 27 prebuilt ARM64 libc++_shared RELRO boundary mismatch. No new Gradle/Android module was introduced.

AndroidX Graphics Path 1.0.1 is rebuilt with NDK 27 because its prebuilt RELRO did not meet the 16 KB boundary check. Its original Java API/consumer rules are retained in the vendored AAR without prebuilt JNI binaries. Unused DataStore was removed; SharedPreferences stores settings. Final ELF LOAD and ARM64 RELRO checks remain enabled.

## Version changes

1.0.1 added 1080p/60 and experimental hardware HEVC 4K/30. Default H.264 1080p/30 remains. Decoder capacity includes size/rate and hardware checks, with standard capacity queries when performance-points data is empty. Software is capped at 720p/30. Unsupported profiles fall back through 1080p/30, 720p/30, 540p/30 and 360p/30; no viable profile fails startup. Changed settings defer until the next session.

The SurfaceView buffer follows received dimensions, up to 3840 pixels per edge, instead of assuming the TV UI resolution; layout preserves portrait aspect ratio. References: [UxPlay](https://github.com/FDH2/UxPlay), [Media3 SurfaceView](https://developer.android.com/media/media3/ui/surface), [SurfaceHolder.setFixedSize](https://developer.android.com/reference/android/view/SurfaceHolder#setFixedSize(int,int)).

1.0.2 changes all application text and maintained project documentation to English, uses **AirPlay Receiver** as the visible app name and **Android TV** as the default new-install receiver name. Existing preferences, package ID, native implementation and signing identity remain. Upstream code, license texts and historical release artifacts retain their original attribution and content.

## Compatibility limits

No complete AirPlay 2, DRM or multiroom-audio support is claimed. 4K UI is not evidence of 4K reception. With PIN on, direct video requires the same authenticated RAOP source; incompatible direct-video clients are rejected. The HLS playlist proxy remains, and external sources may require internet.

Previously reviewed upstream reports: Mac drops/BLE probe (#54), ashmem (#50), localhost HLS (#49), HEVC detection (#51), volume (#53) and Photos direct video (#41). These are not represented as fixed without device tests. Their current upstream status has not been rechecked for this documentation update. HEVC remains off by default; takeover/nohold is disabled; audio advertisement is enabled.
