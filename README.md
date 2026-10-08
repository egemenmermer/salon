# Salon

Android TV için yerel ağda çalışan AirPlay alıcısı. Kotlin/Compose arayüzü, UxPlay alıcı motoru ve mevcut JNI sınıf adları korunmuştur.

- Uygulama kimliği: `app.salon.receiver`
- Sürüm: `1.0.0` (`1`)
- Android: API 24 ve üzeri; hedef API 36
- Release: `arm64-v8a` ve `armeabi-v7a`, tek universal APK
- Varsayılan TV adı: Salon TV
- Native bağlantı kodu doğrulaması varsayılan açık; ikinci kaynağın mevcut yayını devralması kapalı
- Uygulama görünürken alıcı çalışır; HOME ile çıkınca kapanır
- Medya kaydı, overlay, otomatik açılış ve arka plan alıcısı yok

## Belgeler

[KURULUM_TR.md](KURULUM_TR.md): televizyona kurulum ve kumanda kullanımı.

[BUILD.md](BUILD.md): araç sürümleri, Git olmadan derleme, imzalama, APK doğrulama ve anahtar yedekleme.

[UPSTREAM.md](UPSTREAM.md): sabitlenmiş kaynak sürümleri, değişiklikler ve bilinen uyumluluk sınırları.

[TEST_REPORT.md](TEST_REPORT.md): yalnızca gerçekten çalıştırılan kontroller ve ayrı işaretlenen cihaz testleri.

[THIRD_PARTY_NOTICES.txt](THIRD_PARTY_NOTICES.txt): açık kaynak katkıları ve lisans metinleri. Uygulamadaki Ayarlar → Hakkında ve yardım → Açık kaynak lisansları bölümünden de okunur.

Kaynak arşivi alt modüllerin gerçek dosyalarını ve OpenSSL kaynak arşivini içerir. Keystore, parolalar, yerel SDK yolları ve derleme önbellekleri arşive alınmaz. Native derleme için kaynak indirilmez; Gradle/Maven bağımlılıkları için ilk derlemede ağ erişimi gerekir.

## Lisans

Salon ve upstream Android AirPlay Server GPL-3.0 kapsamındadır. İlgili native bileşenlerin kendi lisans ve bildirimleri kaynak ağacında korunur. İmzalı APK ile birlikte karşılık gelen tam kaynak arşivini dağıtın.
