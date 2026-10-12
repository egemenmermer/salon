# Contributing to the Android TV AirPlay receiver

Bug reports, device compatibility results, documentation improvements and focused code changes are welcome. The UI, documentation, issues and pull requests use English.

## Development

Clone the repository normally; native dependency sources are materialized, so `git submodule update` is unnecessary. Follow [BUILD.md](BUILD.md) for the pinned JDK 17, Android SDK 36, NDK 27.0.12077973, CMake 3.22.1 and Gradle 8.11.1 toolchain.

```sh
git clone https://github.com/egemenmermer/salon-tv.git
cd salon-tv
./gradlew :app:testDebugUnitTest --no-daemon --max-workers=1
./gradlew :app:assembleRelease --no-daemon --max-workers=1
```

Set `JAVA_HOME` and `ANDROID_SDK_ROOT` first. Release assembly produces an unsigned APK. To sign a local build, use your own private keystore and the environment variables described in BUILD.md. The official signing key is not distributed. An APK signed with your key cannot replace an official installed release in place.

## Changes and checks

Keep pull requests focused on one behavior. Explain the problem, the resulting behavior, the tests actually run and remaining device limitations. Preserve upstream attribution and third-party license notices.

For receiver state or quality changes, run the unit tests and add a meaningful regression case where needed. For native parser or pairing changes, run `scripts/test-native.sh` and `scripts/test-pairing.sh` with a suitable host C compiler and OpenSSL prefix; BUILD.md documents their requirements. For changes affecting the packaged release, run the signature, manifest, native dependency, JNI/R8 and alignment verification scripts against a signed APK.

Do not treat a successful build or host test as proof of AirPlay interoperability. Identify tests as **not run** when no suitable TV and Apple sender are available. Keep PIN enabled by default and preserve fail-closed behavior, generation checks and the single-active-source rule when touching those areas.

JNI classes intentionally keep the upstream Kotlin package names. Update scripts and tests if release version identifiers change. Avoid resetting vendored sources during build preparation. Upstream GitHub Actions files under `docs/upstream/workflows/` are reference material, not active receiver CI.

## Bug reports and device results

Use [GitHub Issues](https://github.com/egemenmermer/salon-tv/issues) for reproducible bugs and compatibility reports. Include TV model, Android version, application version, sender model/OS, selected quality, whether PIN is enabled, Wi-Fi/Ethernet setup, reproduction steps, expected behavior and observed behavior. For a successful device test, describe which checks passed and how long the session lasted; do not infer frame rate from the selected quality setting.

Do not include actual PINs, passwords, pairing keys, signing credentials, private media or sensitive URLs. If reporting an exploitable security issue, avoid posting credentials or exploitation details publicly; use an available private reporting channel first. This project currently does not advertise a dedicated private vulnerability reporting channel.

## Licensing

This application uses [GPL-3.0](LICENSE), with separate third-party licenses retained in their source trees and [notices](THIRD_PARTY_NOTICES.txt). Submit material you have permission to contribute under the applicable project license. Corresponding release sources are available from [Releases](https://github.com/egemenmermer/salon-tv/releases).
