# Salon derleme ve imzalama

## Sabit araçlar

JDK 17 (bu ortam: 17.0.14), Gradle 8.11.1, AGP 8.10.1, Kotlin/Compose compiler 2.1.0, KSP 2.1.0-1.0.29, Hilt 2.53.1, Compose BOM 2024.12.01, Media3 1.11.1, Oboe 1.9.3. compileSdk/targetSdk 36; minSdk 24. Android SDK Build Tools 35.0.0, NDK 27.0.12077973, CMake 3.22.1. Oboe 1.9.3 ve libc++ aynı native kütüphaneye statik bağlanır; tüm gerekli native kaynaklar arşivdedir. Yerel ağ runtime izni targetSdk 36'da istenmez. Gradle belleği 2048 MB; Kotlin aynı süreçte derlenir, ayrı bellek isteyen Kotlin daemon’ı açılmaz. Gradle tek worker kullanır; native/OpenSSL/FFmpeg paralelliği sınırlıdır. R8 küçültme aşaması 768 MB ile bellek hatası verdiği için bu sınır yükseltildi.

## Kaynak ZIP'inden derleme

ZIP'i açın ve `Salon` dizinine girin. `.git` veya git alt modül komutu gerekmez. Gerekli native kaynaklar ve uygulanmış yamalar dahildir. OpenSSL kaynak tarı hash doğrulamasıyla yerelden açılır. Android SDK, JDK, Python 3, Perl ve make gerekir. Gradle/Maven bağımlılıklarının ilk alınması için internet gerekir.

```sh
export JAVA_HOME=/path/to/jdk17
export ANDROID_SDK_ROOT=/path/to/android-sdk
"$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager"   'platforms;android-36' 'build-tools;35.0.0' 'ndk;27.0.12077973' 'cmake;3.22.1'
./gradlew :app:testDebugUnitTest --no-daemon --max-workers=1
./scripts/test-native.sh
./gradlew :app:assembleRelease --no-daemon --max-workers=1
```

Her derlemeden önce `scripts/prepare-native.py` native kaynakların ve OpenSSL hash’inin hazır olduğunu kontrol eder. Adım tekrar çalıştırılabilir; kaynakları sıfırlamaz ve Git çağırmaz. Kaynak ZIP'i zaten yamalıdır; UxPlay yamalarını yeniden uygulamayın. Referans SHA'lar SOURCE_METADATA.json ve UPSTREAM.md içindedir. Çalışma alanı için isterseniz `local.properties` içinde yalnızca `sdk.dir` belirtebilirsiniz; bu dosya dağıtıma dahil edilmez.

## İmzalama

`SALON_KEYSTORE`, `SALON_STORE_PASSWORD`, `SALON_KEY_PASSWORD` ve isteğe bağlı `SALON_KEY_ALIAS` (varsayılan salon) ortam değişkenlerini güvenli biçimde sağlayın. Parolaları komut satırına veya build loguna yazmayın. `SALON_DIST_DIR` isteğe bağlı çıktı dizinidir.

```sh
./scripts/build-release.sh
./scripts/verify-apk.sh /path/to/Salon-v1.0.1-universal.apk
python3 scripts/package-source.py /path/to/Salon-v1.0.1-source.zip
python3 scripts/release-manifest.py /path/to/Salon-v1.0.1-universal.apk   /path/to/Salon-v1.0.1-source.zip "$ANDROID_SDK_ROOT" /path/to/RELEASE_MANIFEST.json
```

Build script unsigned release APK'yı alır, 16 KB ZIP hizalaması yapar, özel anahtarla imzalar ve doğrular. Debug APK'yı yeniden adlandırmaz. APK imzalandıktan sonra değiştirilmez. Doğrulama scripti gerçek APK manifestini, ARM64/ARMv7 native kütüphanelerini, DT_NEEDED bağımlılıklarını, ARM64 ELF LOAD ve RELRO alanlarının 16 KB hizalamasını, ARMv7 ELF LOAD alanlarının en az 4 KB hizalamasını ve imzayı inceler. x86_64 debug gerekiyorsa ayrı `-PsalonDebugX86=true :app:assembleDebug` kullanılabilir; release ana teslim iki ARM mimarisidir.

## Kendi anahtarınız ve resmî güncellemeler

Resmî Salon release anahtarı ve parolası özel tutulur; bu depoda veya kaynak arşivlerinde yoktur. Katkıda bulunmak için bu anahtara ihtiyaç yoktur. Yerel imzalama için kendi keystore’unuzu oluşturun ve yukarıdaki ortam değişkenlerini güvenli şekilde sağlayın; anahtar ve parolayı depoya eklemeyin. Bağımsız güvenli yedek tutun.

Kendi anahtarınızla imzalanmış bir APK, resmî Salon’un üzerine güncelleme olarak kurulamaz. Resmî sürümler aynı imzayı kullanır ve versionCode artırılır. Gerçek cihazda güncelleme kurulumu henüz test edilmedi.

## Resmî kaynaklar

- https://developer.android.com/build/releases/agp-8-10-0-release-notes
- https://developer.android.com/privacy-and-security/local-network-permission
- https://developer.android.com/guide/practices/page-sizes
- https://developer.android.com/tools/apksigner
- https://developer.android.com/studio/publish/app-signing
- https://developer.android.com/training/tv/get-started/create

Gerçekte çalıştırılan kontroller ve ortam sınırlamaları TEST_REPORT.md'dedir.

AndroidX Graphics Path 1.0.1 aynı sürümün `8a05a22af450d589ef911d772a001a49dcb05b71` kaynaklarından NDK 27 ile yeniden bağlanır. Java API ve tüketici kuralları JNI içermeyen vendored AAR içinde korunur; hazır AAR içindeki RELRO 16 KB sınırına uymadığı için native kaynak yeniden derlenir. Kullanılmayan DataStore bağımlılığı kaldırılmıştır; ayarlar SharedPreferences kullanır.

Native PIN koruma testleri için host OpenSSL geliştirme başlıklarını kurun ve `SALON_HOST_OPENSSL=/path/to/openssl ./scripts/test-pairing.sh` çalıştırın. Bu testler host ASan/UBSan üzerinde çalışır; gerçek Apple protokol uyumluluğunun kanıtı değildir.

1.0.1 release versionCode 2’dir. Önceki Salon release anahtarı kullanılır. HEVC yalnızca kullanıcı 4K seçtiğinde ve TV donanımı gerekli kapasiteyi bildirdiğinde ilan edilir; varsayılan mod H.264 1080p/30’dur.

Upstream GitHub Actions dosyaları `docs/upstream/workflows/` altında referans olarak korunur. Salon için etkin bir GitHub Actions iş akışı kurulmamıştır; doğrulanmış derleme ve imzalama yolu yukarıdaki yerel betiklerdir.
