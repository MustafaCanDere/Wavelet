# Beleşify

Spotify tarzı, ücretsiz bir müzik çalar. Windows ve Android'de uygulama olarak çalışır.

- **YouTube:** Video ya da oynatma listesi bağlantısını yapıştır, YouTube'un resmi oynatıcısıyla çalsın. Video küçük bir pencerede görünür kalır (YouTube kuralları). Arama için kendi ücretsiz YouTube Data API anahtarını uygulamaya girebilirsin.
- **Kendi dosyaların:** MP3, M4A, OGG, WAV, FLAC. Dosyalar sadece cihazında kalır.
- **Beleşify Stüdyo:** Uygulamanın içinde, kodla bestelenen 12 özgün parça (lo-fi, synthwave, house, ambient, chiptune, Hicaz makamı).
- Çalma listeleri, beğenilenler, sıra, karıştırma, tekrar, arama.

## İndir

[Releases](../../releases) sayfasından:

| Dosya | Ne işe yarar |
| --- | --- |
| `Belesify-Kurulum-x.y.z.exe` | Windows'a kurar, masaüstüne kısayol koyar |
| `Belesify-Tasinabilir-x.y.z.exe` | Kurmadan açılır |
| `Belesify.apk` | Android (5.0 ve üstü) |

Dosyalar imzasız. Windows "bilgisayarınızı korudu" derse **Ek bilgi → Yine de çalıştır**. Android'de APK'yı açarken "bilinmeyen kaynaklardan yükleme" iznini vermen gerekir.

## Klasörler

```
web/       Uygulamanın kendisi (tek bir index.html + simgeler). Masaüstü ve Android ikisi de bunu kullanır.
desktop/   Windows uygulaması (Electron). Uygulamayı 127.0.0.1 üzerinden sunar ki YouTube oynatıcısı çalışsın.
android/   Android uygulaması (WebView). Uygulamayı https://appassets.androidplatform.net üzerinden sunar.
```

## Derleme

GitHub Actions her `main` push'unda `.exe` ve `.apk` derler (Actions sekmesinde "Artifacts"). Her başarılı derleme dosyaları otomatik olarak yeni bir Release olarak yayınlar (`v1.0.<derleme no>`). İstersen `v` ile başlayan bir etiket de gönderebilirsin.

Kendi bilgisayarında:

```sh
# Windows
cd desktop && npm install && npm start          # dene
npm run build:win                                # desktop/dist içine .exe

# Android (JDK 17 + Android SDK gerekir)
gradle -p android assembleRelease                # android/app/build/outputs/apk/release
```

### Android imzası

Anahtar ayarlanmazsa APK, derleme makinesinin debug anahtarıyla imzalanır; bu durumda yeni sürümü kurmadan önce eskisini kaldırman gerekebilir. Sabit bir anahtar için deponun **Settings → Secrets and variables → Actions** kısmına şunları ekle: `ANDROID_KEYSTORE_BASE64` (keystore dosyasının base64 hali), `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`.
