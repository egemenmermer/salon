# Build, sign and verify the Android TV receiver

## Pinned toolchain

JDK 17; Gradle 8.11.1; Android Gradle Plugin 8.10.1; Kotlin/Compose compiler 2.1.0; KSP 2.1.0-1.0.29; Hilt 2.53.1; Compose BOM 2024.12.01; Media3 1.11.1; Oboe 1.9.3. Android compileSdk/targetSdk 36, minSdk 24; Build Tools 35.0.0; NDK 27.0.12077973; CMake 3.22.1.

Native sources are included, with already-applied patches. OpenSSL, Oboe and libc++ link statically into the receiver library. Native builds do not download their sources. Gradle uses a 2048 MB heap and one worker; Kotlin compiles in-process. Native/OpenSSL/FFmpeg parallelism is limited. R8 required more than the earlier 768 MB heap limit.

## Build from a checkout or source ZIP

Clone the repository or extract the release source ZIP and enter its `AirPlayReceiver` directory. Older archives used `Salon` as the directory name. Git metadata and submodule commands are not needed to build the source archive.

Android SDK, JDK 17, Python 3, Perl, make and a supported host toolchain are required. Internet is needed on the first run for Gradle/Maven dependencies and SDK installation.

```sh
export JAVA_HOME=/path/to/jdk17
export ANDROID_SDK_ROOT=/path/to/android-sdk
"$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager"   'platforms;android-36' 'build-tools;35.0.0'   'ndk;27.0.12077973' 'cmake;3.22.1'
./gradlew :app:testDebugUnitTest --no-daemon --max-workers=1
./gradlew :app:assembleRelease --no-daemon --max-workers=1
```

Release assembly produces an unsigned APK. `scripts/prepare-native.py` verifies native inputs and the local OpenSSL archive hash before the build. It is idempotent, does not reset sources and does not call Git. Do not reapply the UxPlay patches. Pins are recorded in SOURCE_METADATA.json and UPSTREAM.md. A local `local.properties` file can specify `sdk.dir`; do not distribute that file.

For an optional x86_64 debug build, use `-PsalonDebugX86=true :app:assembleDebug`. The universal release includes only ARM64 and ARMv7.

## Sign with your own key

Provide `SALON_KEYSTORE`, `SALON_STORE_PASSWORD`, `SALON_KEY_PASSWORD` and optionally `SALON_KEY_ALIAS` (default: `salon`) securely through the environment. `SALON_DIST_DIR` optionally selects the output directory. These internal variable names are retained for compatibility. Never write passwords into command arguments or build logs.

```sh
./scripts/build-release.sh
./scripts/verify-apk.sh '/path/to/AirPlay Receiver 1.0.2.apk'
python3 scripts/package-source.py '/path/to/AirPlay Receiver 1.0.2 Source.zip'
python3 scripts/release-manifest.py   '/path/to/AirPlay Receiver 1.0.2.apk'   '/path/to/AirPlay Receiver 1.0.2 Source.zip'   "$ANDROID_SDK_ROOT" /path/to/RELEASE_MANIFEST.json
```

The release script runs unit tests and assembles release, aligns the unsigned APK with `zipalign -P 16`, signs it and verifies the result. It does not rename a debug APK. Do not modify an APK after signing.

Verification checks the actual package ID, version, English application label, SDK levels, launcher, prohibited permissions, non-debuggable flag, both ARM ABIs, ELF LOAD alignment, ARM64 RELRO, native DT_NEEDED closure, JNI/R8 symbols, ZIP alignment and signature. ARM64 LOAD/RELRO requirements are checked at 16 KB; ARMv7 LOAD alignment must be at least 4 KB.

The official signing key is private and excluded from this repository and all archives. Contributors can use their own keys. A build signed with a different key cannot update the official app in place. Keep independent secure backups of your signing key and password. Official releases retain the same key and increment versionCode; actual on-device update installation is not yet tested.

## Native host checks

```sh
./scripts/test-native.sh
SALON_HOST_OPENSSL=/path/to/openssl ./scripts/test-pairing.sh
```

These require a host C compiler with AddressSanitizer/UndefinedBehaviorSanitizer and, for pairing, OpenSSL development headers/libraries. They test parser and pairing guards on the host, not actual Apple interoperability.

AndroidX Graphics Path 1.0.1 is rebuilt from commit `8a05a22af450d589ef911d772a001a49dcb05b71` using NDK 27 because the original packaged native RELRO did not meet the 16 KB boundary check. Its Java API and consumer rules remain in the vendored AAR, with its original JNI binaries removed. The unused DataStore dependency was removed; settings use SharedPreferences. See UPSTREAM.md for Oboe/libc++ details.

Upstream GitHub Actions files under `docs/upstream/workflows/` are reference material. No active project GitHub Actions workflow is configured. The verified build/signing path uses the local scripts above.

## Official references

- [AGP 8.10 compatibility](https://developer.android.com/build/releases/agp-8-10-0-release-notes)
- [Local network permissions](https://developer.android.com/privacy-and-security/local-network-permission)
- [16 KB page sizes](https://developer.android.com/guide/practices/page-sizes)
- [apksigner](https://developer.android.com/tools/apksigner)
- [App signing](https://developer.android.com/studio/publish/app-signing)
- [Android TV application setup](https://developer.android.com/training/tv/get-started/create)

Actual verification results and device limitations are in TEST_REPORT.md.
