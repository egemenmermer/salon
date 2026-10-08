# Salon kurulumu

Doğru dosya: **Salon-v1.0.0-universal.apk**. Bu tek APK hem 32 bit hem 64 bit ARM Android TV / Google TV içindir. Android 7.0 veya üzeri gerekir.

1. APK dosyasını USB belleğe kopyalayın, belleği TV'ye takın ve TV'deki dosya yöneticisinden APK'yı açın. Gerekirse yalnızca bu dosya yöneticisine “Bilinmeyen uygulamaları yükle” izni verin. Menü adları TV markasına göre değişebilir.
2. Kurulumdan sonra TV'nin uygulamalar bölümünden **Salon**'u açın. TV ve telefon aynı ev ağına bağlı olsun; TV Ethernet kablosuyla da bağlanabilir.
3. iPhone veya iPad'de Denetim Merkezi → Ekran Yansıtma → **Salon TV** yolunu izleyin. Kod sorulursa TV'de görünen kodu telefona yazın. TV adını değiştirdiyseniz yeni adı seçin.

Mac'te Denetim Merkezi → Ekran Yansıtma yolunu kullanın.

## TV görünmüyorsa

- Telefon ile TV'nin aynı ev ağına bağlı olduğunu kontrol edin.
- Salon'u TV'de açık tutun.
- Telefonda Ekran Yansıtma listesini kapatıp tekrar açın.

Sonra Salon'daki Bağlantı yardımı veya Yeniden dene seçeneğini kullanın. Misafir ağı, VPN ve modemin cihazları birbirinden ayıran ayarları bağlantıyı etkileyebilir.

Video oynarken kumandanın OK veya Geri tuşu küçük kontrol panelini açar. Devam et paneli kapatır; Bağlantıyı kes sizi bekleme ekranına döndürür. Ses için TV kumandasını kullanın. HOME ile başka uygulamaya geçtiğinizde Salon alıcısı durur; Salon'u tekrar açınca başlar.

Bağlantı kodu varsayılan açıktır. Ayarlar'da kapatırsanız aynı ev ağındaki cihazlar kod girmeden bağlanabilir. TV adı ve görüntü ayarındaki değişiklikler etkin bağlantı varsa bir sonraki bağlantıda uygulanır.

Bazı video uygulamaları korumalı içeriklerin yansıtılmasına izin vermeyebilir. Ekran yansıtma, doğrudan video gönderme ve yalnızca ses ayrı özelliklerdir; her uygulamada çalışmaları garanti edilmez.

Güncellemeler aynı `app.salon.receiver` kimliği ve aynı Salon imzasıyla gelmelidir. Upstream/F-Droid AirPlay Server ayrı uygulamadır; Salon onun üzerine güncelleme değildir. Güncellemeden önce Salon'u kaldırmanız ayarları silebilir.

Gerçek cihazda doğrulanmış ve doğrulanmamış davranışlar için teslimdeki TEST_REPORT.md dosyasına bakın.
