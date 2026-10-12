# Salon TV

An open-source AirPlay screen-mirroring receiver for Android TV and Google TV, based on Android AirPlay Server and UxPlay. The remote-friendly interface is in Turkish. **PIN authentication is enabled by default.**

[Türkçe README](README.md) · [Download APK and installation guide](https://github.com/egemenmermer/salon-tv/releases/latest) · [Build instructions](BUILD.md) · [Contributing](CONTRIBUTING.md)

## Use

1. Download and extract the release ZIP; install its APK on your Android TV using a file manager.
2. Keep Salon open on the TV and connect the sender and TV to the same local network.
3. On an iPhone/iPad or Mac, open Control Center → Screen Mirroring, choose the name displayed on the TV, and enter its PIN when prompted.

Requires Android 7.0 / API 24 or newer and ARM64 or ARMv7. Samsung Tizen and LG webOS are not supported by this Android APK. Leaving Salon for another app stops the receiver. Press OK or Back during playback to open its controls.

## Quality and verification

Automatic requests up to H.264 1080p/30; Smooth requests 1080p/60; experimental 4K requests HEVC 3840×2160/30 when the hardware decoder supports it; Compatible requests up to 720p/30. Unsupported profiles fall back to lower targets. Actual resolution and frame rate depend on the sender, network and decoder.

The signed 1.0.1 APK passed build, 16 automated unit tests, signature, manifest, native dependency, JNI/R8 and alignment checks. **Physical TV–Apple connectivity, real 1080p/60 or 4K/30 output, remote navigation and long sessions have not been tested.** See [the test report](TEST_REPORT.md). Direct video/HLS and audio-only playback are separate paths; this is not a claim of complete AirPlay 2 or DRM support.

## PIN and source

The receiver requires a PIN by default. Failed random PIN generation stops startup; there is no fixed `1234` fallback. PIN can be explicitly disabled in settings, which allows code-free connections from the local network. A second source cannot take over the active session. Media is not recorded to files.

Source dependencies are materialized in the repository, so no submodule initialization is needed. The release signing key is private and excluded. Local builds signed with your own key cannot update the official installed APK in place.

Salon and its upstream app use [GPL-3.0](LICENSE). Third-party notices and licenses are included. Every release provides the exact corresponding source archive separately alongside the APK and checksums. When redistributing an APK, keep its corresponding sources and licenses accessible. Salon is not affiliated with Apple Inc.
