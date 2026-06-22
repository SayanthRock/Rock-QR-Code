# 📱 ROCK Qr

A premium Android QR scanner and generator app by **@sayanthrock**.

Repo: `SayanthRock/Rock-QR-Code`  
GitHub profile: https://github.com/SayanthRock

---

## ✅ App Identity

| Item | Value |
|---|---|
| APK/App name | `ROCK Qr` |
| Android applicationId | `com.rock.qr` |
| Version | `1.0.0` |
| About | `About by @sayanthrock` |

> Internal Kotlin namespace is still kept as `com.example` for build compatibility with the existing source tree. The installed Android package/application ID is `com.rock.qr`.

---

## ✨ Features

- QR code generation from text and links
- Camera QR scanner
- Scan result copy, share, and open-link actions
- Local history support
- Liquid glass / blur inspired premium UI
- Dark theme friendly design
- GitHub Actions APK build

---

## 🛠️ Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- CameraX
- ZXing
- ML Kit Barcode Scanning dependency included
- Room Database
- GitHub Actions

---

## 🚀 Build APK from GitHub

1. Open the repository on GitHub.
2. Go to **Actions**.
3. Select **Build ROCK Qr APK**.
4. Tap **Run workflow**.
5. After it finishes, download the artifact named **ROCK-Qr-debug-apk**.

The APK output names are:

```text
ROCK_Qr.apk
ROCK Qr.apk
```

---

## 💻 Local Build

```bash
chmod +x ./gradlew
./gradlew assembleDebug
```

APK path:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## 👤 About

Designed, improved, and maintained by **@sayanthrock**.
