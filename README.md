# Salon TV

Android TV ve Google TV için açık kaynak AirPlay ekran yansıtma alıcısı. iPhone, iPad veya Mac’in **Ekran Yansıtma** menüsünden aynı ağdaki TV’ye bağlanır. Türkçe arayüzü kumandayla kullanılır; bağlantı PIN’i varsayılan açıktır.

[English](README.en.md) · [APK ve kılavuz indir](https://github.com/egemenmermer/salon-tv/releases/latest) · [Nasıl çalışır?](docs/NASIL_CALISIR.md) · [Katkıda bulun](CONTRIBUTING.md)

> **Doğrulama durumu:** 1.0.1’in derlemesi, 16 otomatik testi, imza ve native hizalama kontrolleri geçti. Gerçek TV–iPhone/iPad/Mac bağlantısı ve uzun yayın oturumu henüz test edilmedi. 1080p/60 ve 4K/30, garanti edilen yayın sonuçları değil, cihaz kapasitesine göre seçilen hedeflerdir. [Ayrıntılı test raporu](TEST_REPORT.md).

## İndir ve kur

1. [Salon 1.0.1 ZIP’ini](https://github.com/egemenmermer/salon-tv/releases/download/v1.0.1/Salon.1.0.1.zip) açın. İçinde APK ve Türkçe TV kurulum/kullanım kılavuzu bulunur.
2. APK’yı USB veya dosya aktarımıyla TV’ye gönderip dosya yöneticisinden yükleyin. Gerekirse o dosya yöneticisi için “Bilinmeyen uygulamaları yükle” iznini açın.
3. TV’de Salon’u açın. iPhone/iPad’de Denetim Merkezi → **Ekran Yansıtma** → TV ekranındaki ad yolunu izleyin; kod sorulursa TV’deki PIN’i girin. Mac’te de Denetim Merkezi → Ekran Yansıtma kullanılır.

[Yalnızca APK](https://github.com/egemenmermer/salon-tv/releases/download/v1.0.1/Salon.1.0.1.apk) · [Tam kurulum kılavuzu](KURULUM_TR.md) · [Sorun giderme](docs/SIK_SORULAN_SORULAR.md)

Android **7.0 / API 24 veya üzeri**, ARM64 ya da ARMv7 Android TV / Google TV gerekir. Bu APK, Samsung Tizen veya LG webOS uygulaması değildir. TV ve gönderici aynı yerel ağda olmalıdır; TV Ethernet, telefon Wi-Fi kullanabilir. Varsayılan ad **Salon TV**’dir; ad çakışırsa TV’de gerçekten ilan edilen ad gösterilir.

## Özellikler

- PIN doğrulaması varsayılan açık. Rastgele PIN üretilemezse başlangıç başarısız olur; sabit `1234` yedeği kullanılmaz. Ayarlardan PIN’i kapatmak mümkündür ve aynı ağdaki cihazların kodsuz bağlanmasına izin verir.
- Aynı anda tek aktif kaynak. Protokolün ek bağlantıları ayrı cihaz olarak sayılmaz; başka bir kaynak mevcut oturumu devralamaz.
- Salon görünürken alıcı çalışır. HOME ile başka uygulamaya geçince durur; Salon’a dönünce yeniden başlar.
- OK veya Geri kontrol panelini açar. “Devam et” kapatır; etkileşim olmazsa yaklaşık beş saniye sonra kapanır.
- Medya dosyaya kaydedilmez. Overlay, açılışta otomatik başlatma ve arka planda alıcı servisi bulunmaz.
- Ayarlar uygulamanın özel depolamasında tutulur. Bağlantıyı etkileyen değişiklikler sonraki oturumda uygulanır.

## Görüntü kalitesi

| Mod | İstenen üst hedef | Destek yoksa |
| --- | --- | --- |
| Otomatik (varsayılan) | H.264, 1080p / 30 FPS | Daha düşük H.264 hedef |
| Akıcı | H.264, 1080p / 60 FPS | 1080p / 30 FPS veya daha düşük |
| 4K (deneysel) | Donanım HEVC ile 3840 × 2160 / 30 FPS | H.264, 1080p / 30 FPS veya daha düşük |
| Uyumlu | H.264, 720p / 30 FPS | Daha düşük H.264 hedef |

HEVC varsayılan kapalıdır; yalnız 4K seçildiğinde ve decoder kapasitesi uygun olduğunda ilan edilir. Yazılım decoder’ı en fazla 720p/30 ile sınırlanır. Gönderici, ağ veya TV daha düşük gerçek çözünürlük ve kare hızı kullanabilir. Kaliteyi değiştirince mevcut bağlantıyı bitirip yeniden bağlanın.

## Nasıl çalışır?

Salon, [Android AirPlay Server v0.0.31](https://github.com/jqssun/android-airplay-server/tree/v0.0.31) ve [UxPlay](https://github.com/FDH2/UxPlay) üzerine kuruludur. Android ağ keşfi alıcıyı ilan eder; native motor eşleştirme ve medya protokolünü işler. Android MediaCodec görüntüyü çözer, SurfaceView gösterir; AAC/ALAC ses native motor ve Oboe yolundan TV’ye gider. Ekran yansıtma, doğrudan video/HLS ve yalnız ses ayrı yollardır; tüm AirPlay 2 özellikleri veya korumalı içerikler için destek iddiası yoktur.

[Mimari ve PIN akışı](docs/NASIL_CALISIR.md) kaynak dosyalarına bağlantılarla açıklanmıştır.

## Geliştirme ve belgeler

Sürüm **1.0.1**, versionCode **2**, applicationId `app.salon.receiver`; compileSdk / targetSdk **36**. JNI uyumluluğu için Kotlin paketleri upstream adını korur.

| Belge | İçerik |
| --- | --- |
| [BUILD.md](BUILD.md) | Sabit araçlar, kaynak derleme, kendi anahtarınızla imzalama ve APK kontrolleri |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Katkı, test ve hata bildirimi |
| [UPSTREAM.md](UPSTREAM.md) | Upstream commit’leri, native bağımlılıklar ve değişiklikler |
| [TEST_REPORT.md](TEST_REPORT.md) | Yapılan kontroller ve test edilmeyen cihaz senaryoları |
| [THIRD_PARTY_NOTICES.txt](THIRD_PARTY_NOTICES.txt) | Üçüncü taraf lisansları ve bildirimler |

Native bağımlılıklar kaynak olarak depodadır; Git alt modülü başlatmak gerekmez. OpenSSL kaynağı ve hash kontrolü dahildir. Android SDK, JDK ve Gradle/Maven bağımlılıkları ayrıca gerekir. Resmî release anahtarı, parolalar ve derleme önbellekleri depoda bulunmaz. Kendi anahtarınızla imzaladığınız APK, resmî Salon’un üzerine güncelleme olarak kurulamaz.

## Lisans ve kaynaklar

Salon ve upstream uygulama [GPL-3.0](LICENSE) kapsamındadır; diğer bileşenlerin kendi lisansları korunur. Her [sürümün](https://github.com/egemenmermer/salon-tv/releases) karşılık gelen tam kaynak arşivi, bildirimler, APK manifesti ve SHA-256 özetleri ayrı indirmeler olarak sunulur. İmzalı APK’yı paylaşırken karşılık gelen kaynakları ve lisanslarını da erişilebilir tutun.

Salon, Apple Inc. ile bağlantılı değildir. AirPlay adının kullanılması Apple sertifikasyonu anlamına gelmez.
