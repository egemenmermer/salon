# Salon doğrulama raporu

Bu rapor derleme ortamı ve gerçekten çalıştırılan kontrolleri ayırır. Derleme başarısı, gerçek Apple cihazının bağlandığı anlamına gelmez.

## Tamamlanan otomatik kontroller

- Kotlin durum modeli: 9 test, 0 başarısızlık ve 0 hata. Hazır olmak için dinleyici + iki servis ilanı; gerçek ilan adı; izin/kayıt hatasının hazır olmaması; durdurmadan sonra eski callback’lerin etkisizliği; yeniden başlatma; oturum sonunda kod temizliği; UTF-8 ad sınırı; kontrollü zaman aşımı; yeni/bitmiş oturuma eski zaman aşımının etkisizliği.
- Native PIN/SRP: host OpenSSL 3.6.1, ASan + UBSan ile rastgele PIN ve SRP private key üretim hatası, ilk adımsız proof, doğrulanmamış sıfır anahtardan türetilmiş geçerli GCM yükü, kısa/yanlış proof, sıfır public key ve doğrulama tamamlanmadan oturum temizliği. Koruma testleri geçti. Bunlar gerçek Apple cihazından doğru/yanlış PIN testinin yerine geçmez.
- Native HTTP/RTSP: host AddressSanitizer + UndefinedBehaviorSanitizer ile normal ve tek byte parçalı RTSP, gövde sınırı, toplam başlık sınırı ve başlık sayısı. Bellek veya UB raporu yok.
- Renk kontrast hesabı: kullanılan ana/ikincil metin, odak, hazır ve hata renkleri için oranlar 9,11:1 ile 16,94:1 arasında; 4,5:1 altı yok. Kumanda odak ve büyütülmüş metin kontrolleri ayrı cihaz testidir.

## Upstream referans denemeleri

Arayüz değişikliğinden önce v0.0.31 kaynaklarıyla üç referans derleme denendi. İlkinde eşzamanlı SDK hazırlığından kaynaklanan dosya çakışması, ikincisinde eksik android.jar, üçüncüsünde native derleme sırasında No space left hatası oluştu. SDK yeniden kuruldu ve sonraki derlemeler daha düşük bellek/paralellikle düzenlendi. Bu denemelerden başarılı referans APK üretilmedi; Salon arayüzü için çalışan referans cihaz testi sayılmaz.

## Paket ve cihaz kontrolleri

TV API 33 ARM64 emülatör görüntüsü kuruldu ve başlatma denendi. Emülatörün boş alan ön kontrolü başlatmayı reddetti; çalışan TV oturumu oluşmadı. Bu yüzden emülatörde kurulum, launcher, kumanda, odak, HOME/Geri, yeniden açılış, büyük metin ve ayar koruması: **Test edilmedi**. Fiziksel cihaz taramasında yetkili Android cihazı yoktu.

- İlk kaynak arşivi ayrı, `.git` ve önceki uygulama/native build çıktıları olmayan dizine açıldı. JDK/SDK ve Maven bağımlılıkları aynı sabit araçlardır. `testDebugUnitTest` + `assembleRelease`: **BUILD SUCCESSFUL, 16 dakika 45 saniye, 84 görev çalıştırıldı**. OpenSSL dahil iki ARM native sürüm kaynaktan derlendi.
- Son PIN koruması ve metin değişikliklerini içeren arşiv aynı ayrı dizine açıldı. Değişen kaynaklar yeniden derlendi ve aynı özel anahtarla imzalandı: **BUILD SUCCESSFUL, 44 saniye**. Kaynak arşivi son teslim belgeleriyle yeniden paketlendi; kod ve build girdilerinin arşiv ile doğrulama dizininde aynı olduğu byte karşılaştırmasıyla kontrol edildi.
- Son imzalı universal APK: `app.salon.receiver`, 1.0.0 / versionCode 1, minSdk 24, targetSdk 36. ARM64 + ARMv7; debuggable kapalı; TV launcher, banner/icon ve dokunmatik gerekmemesi manifestten kontrol edildi. Kaldırılması istenen overlay, kayıt, boot, foreground-service ve yerel ağ runtime izinleri yok.
- APK v2/v3 imzası, ZIP 16 KB sayfa hizalaması, iki mimaride paketlenen tüm ELF dosyalarının LOAD alanları ve ARM64 RELRO alanları geçti. Native DT_NEEDED bağımlılıkları pakette veya izin verilen Android sistem kütüphanelerinde bulundu. Alıcı, OpenSSL, Oboe ve C++ runtime statik bağlanır; AndroidX Graphics Path aynı 1.0.1 kaynaklarından doğru hizalama ile yeniden derlenmiştir.
- Son APK DEX ve ELF kontrolü: R8 sonrasında 22 NativeBridge native metodu, 22 alıcı callback imzası, log callback’i ve AndroidX Graphics Path JNI tanımları korunmuş; her iki ARM kütüphanesinde gerekli JNI girişleri dışa aktarılmıştır. Bu statik doğrulama, cihaz üzerinde JNI çalıştırma testi değildir.

## Gerçek cihaz sınırları

Yetkili fiziksel Android TV bulunmadı ve iPhone/iPad test cihazına erişilemedi. Derleme MacBook Air üzerinde yapıldı; bu Mac’in gerçek AirPlay göndericisi olarak bir TV’ye bağlantısı test edilmedi. Gerçek iPhone/iPad/Mac keşfi, doğru/yanlış kodla gerçek Apple eşleştirmesi, sesli ekran yansıtma, dikey/yatay geçiş, ikinci gerçek kaynak reddi, ardışık bağlantı ve 10–15 dakika yayın: **Test edilmedi**.

Mac bağlantı düşmesi (#54), gerçek TV decoder/ashmem (#50), doğrudan HLS (#49), HEVC (#51), telefon/TV ses seviyesi (#53), fotoğraf paylaşımı (#41): **Test edilmedi**. HEVC varsayılan kapalıdır. HLS ve yalnızca ses, ekran yansıtmaya ait başarı kanıtı olarak değerlendirilmez. Korumalı HLS, doğrulanmış aynı kaynak RAOP oturumu ister; bunu yapmayan doğrudan-video göndericileri kod açıkken reddedilir.

Gerçek Wi-Fi/Ethernet geçişi ve toparlanma, gerçek NSD isim çakışması, etkin oturumda ayar ertelemesi ve yalnızca ses aktarımı: **Test edilmedi**. Ağ/kayıt/PIN olayları için model ve native koruma testlerinin kapsamı yukarıda belirtilmiştir; bunlar uçtan uca cihaz testleri değildir.
