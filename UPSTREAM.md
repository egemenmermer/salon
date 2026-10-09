# Salon upstream kaydı

Kaynak: https://github.com/jqssun/android-airplay-server
Tag: v0.0.31
Commit: c8defdd70d7e6a04f4f1b71d353653682d594106
Lisans: GPL-3.0. Salon applicationId: app.salon.receiver.

## Sabit native kaynaklar

- UxPlay: 462153392f2e30937424922039ff9f0cda5e7b1a
- FFmpeg: 38b88335f99e76ed89ff3c93f877fdefce736c13 (ALAC decoder; ağ kapalı, statik build)
- libplist: f41b1ea67045e0c09339974d83e389972d84f166
- openssl-cmake: 4edd36a8dab5f85a8f92650b5bdf6e0cab13aab8
- OpenSSL: 3.4.4; kaynak tar.gz SHA-256: 7bdf55ac20f2779e99e5eca306f824fad2b37dee5a06cc35ed5a8b85a6060010

## Korunan yollar

UxPlay RAOP ve AirPlay protokolü, SRP/PIN ve pair-verify, Android NsdManager üzerinden tek DNS-SD ilan yolu, MediaCodec video ve EGL/SurfaceView, native AAC/ALAC + Oboe ses, Media3 üzerinden doğrudan video/HLS. Ekran yansıtma, ses ve HLS ayrı yollardır. JNI sınıf adları korunmuştur.

## Salon değişiklikleri

Türkçe Compose TV arayüzü; Activity'ye bağlı görünür alıcı; ağ olayları; iki servis ilanının geri bildirimi; gerçek ilan adı; nesil numarasıyla eski callback iptali; ilk çizilen kare; audio focus; sınırlı kayıtlar; gerçek kod doğrulaması; ikinci kaynak reddi. PIN üretim hatasında sabit kod kaldırıldı. Native pair-verify ve SETUP/RECORD doğrulama kapıları eklendi. SRP proof ve eşleştirme veri kopyası boyutları doğrulanıyor. Unicode TV isimleri standart UTF-8 olarak JNI'den geçiyor. İkinci kaynak HLS ile mevcut aynalamayı kapatamaz.

Upstream'in altı UxPlay yaması korunup kaynaklara uygulanmıştır. Build'de git checkout/apply zorunluluğu kaldırıldı; arşivde native kaynaklar zaten yamalıdır. OpenSSL kaynak tarı yerel ve hash doğrulamalıdır. Native işler ve Gradle daha düşük bellek kullanımı için sınırlandı. AGP 8.10.1 ve kararlı Media3 1.11.1 kullanılıyor. Her paylaşımlı native kütüphanede 16 KB hizalama kontrolü ayrı yapılır.

Otomatik boot, overlay, PiP, ekran/medya kaydetme ve debug görünümü ürün akışından kaldırıldı. Telefon sesi TV'nin sistem sesini değiştirmez; ses kumandayla ayarlanır. Pairing istemci anahtarları yalnızca sunucunun belleğinde tutulur. Sunucu yeniden açılınca yeniden eşleştirme gerekebilir.

## Sınırlar ve bilinen upstream bildirimleri

DRM, çok odalı ses ve tüm AirPlay 2 yolları destek iddiası değildir. 4K UI, 4K alım garantisi değildir. Kod açıkken doğrudan video yolu önce doğrulanmış RAOP kaynağı gerektirir; bu eşleştirmeyi sunmayan doğrudan-video istemcileri reddedilir. HLS yerel playlist proxy'si korunmuştur; dış video kaynakları ayrıca internet gerektirebilir. Destek Apple uygulaması ve TV decoder'ına bağlıdır.

İncelenen açık upstream sorunları: #54 Mac bağlantı düşmesi/BLE probe, #50 ashmem erişimi, #49 localhost HLS, #51 HEVC tespiti, #53 ses düzeyi, #41 Fotoğraflar doğrudan-video. Bunlar Salon'da cihaz testi olmadan çözülmüş olarak sunulmaz. HEVC varsayılan kapalı; nohold/bağlantı devralma kapalı; ses ilanı açıktır.

Native HTTP/RTSP okuyucusunda URL (8 KiB), toplam başlık (64 KiB), başlık sayısı (128 çift) ve gövde (16 MiB) sınırları uygulanır. Protokol adı ve sürümü parser callback’lerinden biriktirilir; parçalı paketlerde URL tamponunun sonrasına erişilmez. Host üzerindeki AddressSanitizer/UndefinedBehaviorSanitizer testi byte düzeyinde parçalanmış RTSP isteğini ve boyut sınırlarını doğrular.

Tek aktif kaynak kontrolü doğrulanmış eşleştirme veya medya kurulumu olan RAOP oturumunu esas alır; salt TCP/protokol bağlantı sayısı cihaz sayısı değildir. Aynı ağ adresi ve IPv6 zone kapsamında aynı kaynağın ek kanalları kabul edilir. Aktif sahibinden farklı bir kaynak için yeni oturum/kurulum devralması 409 ile reddedilir.

OpenSSL aynı 3.4.4 kaynağından yalnızca `libssl.a` ve `libcrypto.a` hedefleriyle derlenir ve alıcının native kütüphanesine statik bağlanır. Kullanılan algoritma seçenekleri korunur. Gereksiz ikinci paylaşımlı derleme ve CLI araçları üretilmez; APK’da ayrıca libssl.so/libcrypto.so gerekmemesi bu nedenle beklenen davranıştır.

Oboe 1.9.3 (`b15f5e39c01a7ada306d959e5129620b145fb8b4`, https://github.com/google/oboe/tree/1.9.3) kaynakları vendored olarak eklenmiştir. Sürüm korunur; hazır AAR yerine aynı NDK 27 ile statik derlenir. C++ çalışma zamanı `c++_static` olarak tek alıcı .so dosyasına bağlanır. Bu değişiklik NDK 27’nin hazır ARM64 libc++_shared.so dosyasındaki 16 KB RELRO sınır uyumsuzluğunu paket dışına çıkarır. Yeni Gradle/Android modülü oluşturulmaz. Nihai ELF LOAD ve RELRO alanları ayrıca doğrulanır.

AndroidX Graphics Path 1.0.1 aynı sürümün `8a05a22af450d589ef911d772a001a49dcb05b71` kaynaklarından NDK 27 ile yeniden bağlanır. Java API ve tüketici kuralları JNI içermeyen vendored AAR içinde korunur; hazır AAR içindeki RELRO 16 KB sınırına uymadığı için native kaynak yeniden derlenir. Kullanılmayan DataStore bağımlılığı kaldırılmıştır; ayarlar SharedPreferences kullanır.

SRP adımları için oturum/kanıt kontrolü eklenmiştir: bozuk veya sırası değiştirilmiş istekler, doğrulanmamış sıfır oturum anahtarından türetilmiş GCM yükü, kısa proof ve sıfır istemci public key reddedilir. Rastgele SRP private key üretimi başarısızsa eşleştirme başarısız olur. Geçersiz SRP public key yolundaki native kaynak sızıntısı giderilmiştir.

1.0.1: 1080p/60 ve donanım HEVC ile deneysel 4K/30 kalite seçenekleri eklendi. Otomatik 1080p/30 varsayılanı korunur. Decoder seçiminde bildirilen boyut/rate ve donanım kapasitesi kontrol edilir; boş performance-points listesinde standart kapasite sorgusuna dönülür. Yazılım decoder’ı en fazla 720p/30 ile sınırlandırılır. Desteklenmeyen hedefler sırayla 1080p/30, 720p/30, 540p/30, 360p/30’a iner; hiçbir hedef desteklenmiyorsa başlangıç başarısız olur. Kalite ayarı oturum sırasında değişirse sonraki oturuma ertelenir. Kaynak protokol açıklaması: https://github.com/FDH2/UxPlay

EGL video SurfaceView buffer boyutu gelen video çözünürlüğüne göre, en fazla 3840 piksel kenar ile ayarlanır; TV arayüzü 1080p olduğunda 4K video tamponunu 1080p’ye indirmez. Dikey görüntü oranı layout tarafından korunur. Android açıklaması: https://developer.android.com/media/media3/ui/surface ve https://developer.android.com/reference/android/view/SurfaceHolder#setFixedSize(int,int)
