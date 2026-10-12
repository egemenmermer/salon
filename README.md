# AirPlay Screen Mirroring Receiver for Android TV

An open-source receiver that displays an iPhone, iPad or Mac screen on Android TV and Google TV over the local network. The TV interface is in English and works with a remote. **Connection PIN authentication is enabled by default.**

[Download APK and installation guide](https://github.com/egemenmermer/salon-tv/releases/latest) · [How it works](docs/ARCHITECTURE.md) · [Build from source](BUILD.md) · [Contribute](CONTRIBUTING.md)

> **Verification:** builds, automated tests and APK checks are recorded in [TEST_REPORT.md](TEST_REPORT.md). Physical TV–Apple connectivity, remote navigation and long sessions have not been tested. 1080p/60 and 4K/30 are requested targets, not guaranteed output. The 4K mode is experimental.

## Install and connect

1. Download and extract [AirPlay Receiver 1.0.2.zip](https://github.com/egemenmermer/salon-tv/releases/download/v1.0.2/AirPlay.Receiver.1.0.2.zip). It contains the APK and an English installation and usage guide.
2. Transfer the APK to your TV by USB or file transfer and install it with a file manager. If prompted, allow that file manager to install unknown apps.
3. Open **AirPlay Receiver** on your TV. On your iPhone/iPad or Mac, open Control Center → **Screen Mirroring**, select the name displayed on the TV and enter the TV’s PIN when prompted.

[APK only](https://github.com/egemenmermer/salon-tv/releases/download/v1.0.2/AirPlay.Receiver.1.0.2.apk) · [Full installation guide](INSTALL.md) · [Troubleshooting](docs/FAQ.md)

Requires **Android 7.0 / API 24 or newer**, with ARM64 or ARMv7. This Android APK does not run on Samsung Tizen or LG webOS. Sender and TV must share a local network; Ethernet on the TV and Wi-Fi on the sender can be used together. The default receiver name is **Android TV**. If discovery renames it after a name collision, use the actual name displayed on the TV.

## Behavior

- PIN authentication is on by default. Random PIN generation failure stops startup; there is no fixed `1234` fallback. Explicitly disabling PIN in settings allows devices on the local network to connect without a code.
- One active source at a time. Extra protocol connections are not counted as extra devices, and a second source cannot take over an active session.
- The receiver runs while the app is visible. Leaving with HOME stops it; returning starts it again.
- Press OK or Back during mirroring to open the controls. Continue closes them; they close after approximately five seconds without interaction.
- Media is not recorded to files. No overlay, boot auto-start or background receiver service is used.
- Settings are stored privately. Connection-related changes apply to the next session.

## Quality options

| Mode | Requested upper target | If unsupported |
| --- | --- | --- |
| Automatic (default) | H.264, 1080p / 30 FPS | Lower H.264 profile |
| Smooth | H.264, 1080p / 60 FPS | 1080p / 30 FPS or lower |
| 4K (experimental) | Hardware HEVC, 3840 × 2160 / 30 FPS | H.264, 1080p / 30 FPS or lower |
| Compatible | H.264, 720p / 30 FPS | Lower H.264 profile |

HEVC is off by default and is advertised only when 4K is selected and hardware decoding supports the profile. Software decoding is limited to 720p/30. The sender, network and TV may deliver less than the selected target. After saving a quality change, end mirroring and reconnect.

## How it works

The implementation builds on [Android AirPlay Server v0.0.31](https://github.com/jqssun/android-airplay-server/tree/v0.0.31) and [UxPlay](https://github.com/FDH2/UxPlay). Android DNS-SD discovery advertises the receiver. The native engine handles pairing and media protocols; MediaCodec decodes video for SurfaceView, while AAC/ALAC audio uses the native engine and Oboe.

Screen mirroring, direct video/HLS and audio-only playback are separate paths. This is not a claim of complete AirPlay 2, DRM or universal sender compatibility. [The architecture guide](docs/ARCHITECTURE.md) explains the lifecycle and PIN flow with links to the code.

## Development and documentation

Current version: **1.0.2**, versionCode **3**. Application ID: `app.salon.receiver`. compileSdk / targetSdk: **36**. Internal identifiers and JNI packages are retained for upgrade and native compatibility; the visible application name is **AirPlay Receiver**. Existing saved TV names are preserved on update.

| Document | Contents |
| --- | --- |
| [INSTALL.md](INSTALL.md) | TV installation, remote controls and updates |
| [BUILD.md](BUILD.md) | Pinned tools, source builds, signing and APK verification |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Contributions, tests and bug reports |
| [UPSTREAM.md](UPSTREAM.md) | Source pins, native dependencies and adaptations |
| [TEST_REPORT.md](TEST_REPORT.md) | Completed checks and untested device scenarios |
| [THIRD_PARTY_NOTICES.txt](THIRD_PARTY_NOTICES.txt) | Third-party notices and licenses |

Native sources are materialized in the repository; no submodule initialization is required. OpenSSL sources and hash checks are included. Android SDK, JDK and Gradle/Maven dependencies are separate requirements. Signing keys, passwords and build caches are excluded. A build signed with your own key cannot update an official installation in place.

## License and corresponding source

The app and its upstream application use [GPL-3.0](LICENSE); other components retain their licenses. Each [release](https://github.com/egemenmermer/salon-tv/releases) provides its corresponding full source archive, notices, manifest and SHA-256 checksums separately. Keep corresponding sources and licenses accessible when redistributing an APK.

This project is not affiliated with Apple Inc. Use of the AirPlay name does not imply Apple certification.
