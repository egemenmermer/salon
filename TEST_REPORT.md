# Salon 1.0.1 doğrulama raporu

## Görüntü seçenekleri

Otomatik: en fazla 1080p/30; Akıcı: hedef 1080p/60; 4K: hedef 3840×2160/30, deneysel ve donanım HEVC desteği gerektirir; Uyumlu: en fazla 720p/30. Hedefler decoder kapasitesine göre aşağı iner. 60 FPS desteklenmiyorsa 1080p/30 denenir; 4K desteklenmiyorsa HEVC ilan edilmez ve H.264 1080p/30 veya daha düşük hedef seçilir. Yazılım decoder’ı en fazla 720p/30 ile sınırlıdır. Kalite, uygulama özel ayarlarında tutulur; etkin oturumda değişiklik sonraki oturuma ertelenir.

TV arayüzü düşük çözünürlükte olsa da mirroring SurfaceView tamponu gelen video boyutuyla, kenar başına en fazla 3840 piksel olarak ayarlanır. Dikey/yatay oran korunur. Bu değişikliğin gerçek TV’de görsel kontrolü yapılmadı.

Bu değerler Apple göndericisine bildirilen üst hedeflerdir. Gerçek çözünürlük ve kare hızı telefon/Mac ve ağa bağlı olarak daha düşük olabilir. Derleme ve kapasite seçimi testleri, gerçek 4K/30 veya 1080p/60 yayın kanıtı değildir.

## Otomatik kontroller

- Durum modeli: 9 test. Dinleyici ve iki ilan olmadan hazır olmama, gerçek ilan adı, kayıt/izin hatası, eski callback, durdurma/yeniden başlatma, oturum sonu PIN temizliği, UTF-8 ad doğrulaması ve kontrollü zaman aşımı.
- Kalite politikası: 7 test. 1080p/60 seçimi; 60 FPS yoksa 1080p/30; donanım HEVC/4K kapasitesi yoksa H.264 hedefe dönme; 1080p yoksa 720p; hiçbir hedef yoksa başarısızlık; varsayılan ve uyumlu sınırları; ayar round-trip ve eski ayar taşıması; UHD/dikey Surface boyutu ve hatalı boyut reddi. Android MediaCodec cihaz sorguları bu host testlerinin kapsamına girmez.
- Native kaynaklar 1.0.0 ile aynıdır. Önceki host ASan/UBSan HTTP/RTSP ve PIN/SRP testleri geçmiştir: parçalı RTSP ve istek sınırları; RNG hatası; sırası bozuk eşleştirme; doğrulanmamış sıfır anahtarlı GCM yükü; kısa/yanlış proof; sıfır public key; oturum temizliği. Bunlar gerçek Apple eşleştirme testi değildir.
- APK doğrulaması: uygulama kimliği app.salon.receiver, versionCode 2, sürüm 1.0.1, minSdk 24 / targetSdk 36, yalnız ARM64 + ARMv7, debuggable kapalı, TV launcher ve banner/icon. Overlay, kayıt, boot, foreground-service ve ACCESS_LOCAL_NETWORK izinleri yok.
- V2/V3 imzası; ZIP 16 KB hizalaması; iki ABI’nin ELF LOAD alanları ve ARM64 RELRO; native bağımlılık kapanışı; R8 sonrasında 22 native tanım ve 22 callback, log ve Graphics Path JNI korunumu doğrulama betikleriyle geçti. Nihai sonuçlar beraberindeki verification kayıtlarındadır.
- İmza anahtarı önceki Salon release ile aynıdır; önceki uygulamanın üzerine güncelleme için versionCode artırılmıştır.

## Kaynak arşivi

Kaynaklar .git olmayan ayrı doğrulama dizininde derlendi. Native kaynaklar değişmediği için önceki temiz kaynak arşivi derlemesinin native çıktıları kullanılabildi; bu ilk derleme OpenSSL dahil iki ABI’yi kaynaktan derlemiş, 84 görevi çalıştırmış ve 16 dakika 45 saniyede tamamlanmıştı. 1.0.1’in Kotlin/Compose ve kaynak değişiklikleri ayrı dizinde yeniden derlendi. Son arşivin tüm dosyaları çalışma kaynağı ve doğrulama dizini ile byte karşılaştırmasıyla eşleştirildi. Keystore, kimlik bilgileri, Git, önbellek ve build çıktıları kaynak arşivine alınmaz. Maven/JDK/SDK araçları kaynak arşivinden ayrı derleme gereksinimleridir.

## Test edilmedi

Bağlı/yetkili Android cihazı son ADB taramasında görünmedi. Önceki TV API 33 emülatör başlatma denemesi çalışır TV oturumu oluşturmadı. Kumanda, odak, büyük metin, Surface boyutu değişimi, HOME/Geri, yeniden açılış, cihazda ayar koruması ve güncelleme kurulumu: **Test edilmedi**.

Gerçek iPhone/iPad/Mac keşfi, doğru/yanlış PIN, 1080p/60 ve 4K/30 yayının gerçek çözünürlüğü/FPS’i, sesli yansıtma, yön değişimi, ikinci kaynak, ardışık bağlantı, Wi-Fi/Ethernet toparlanması ve 10–15 dakika yayın: **Test edilmedi**. MacBook Air derleme hostudur; gerçek TV’ye AirPlay göndericisi olarak bağlantısı test edilmedi.

Doğrudan HLS ve yalnızca ses ayrı ayrı: **Test edilmedi**. Korumalı HLS doğrulanmış aynı kaynak RAOP oturumu ister. Upstream Mac bağlantı düşmesi (#54), decoder/ashmem (#50), doğrudan video (#49), HEVC (#51), ses seviyesi (#53), fotoğraf paylaşımı (#41) regresyonları: **Test edilmedi**. Bu yüzden 4K seçeneği deneysel işaretlidir.

Upstream referans v0.0.31 için arayüz değişikliklerinden önceki üç derleme denemesi başarısızdı; başarılı upstream referans APK üretilmedi. Ayrıntılar önceki 1.0.0 teslim raporundadır.

## Nihai 1.0.1 sonucu

Son Git olmayan doğrulama dizini derlemesi 2 dakika 14 saniyede tamamlandı. 16 otomatik test geçti (9 durum modeli + 7 kalite politikası). İmzalı universal APK, manifest, ZIP/ELF, native bağımlılık ve JNI/R8 kontrollerini geçti. İmza sertifikası önceki release ile eşleşti. Doğrulama kaydı: verification/quality-surface-release.log.
