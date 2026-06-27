# 📱 Rock QR Code

Rock QR Code is an offline-first Android QR generator and scanner built with Jetpack Compose.

[![Rock QR Code CI](https://github.com/SayanthRock/Rock-QR-Code/actions/workflows/build.yml/badge.svg)](https://github.com/SayanthRock/Rock-QR-Code/actions/workflows/build.yml)

---

## ✅ Current Project Status

The GitHub Pages web console has been removed from the repository. The project now focuses on the Android app and CI build pipeline only.

---

## 💎 Android Feature Highlights

- **Modern Jetpack Compose UI** with a premium dark interface.
- **Offline QR generation and scanning** without requiring a hosted web page.
- **Local history storage** using Room database.
- **Camera scanner support** for QR code capture.
- **Custom QR styling** for generated codes.
- **GitHub Actions CI** for Android build validation and APK artifact upload.

---

## 🛠️ Architecture & Tech Stack

- **Framework:** Android, Kotlin, Jetpack Compose.
- **Persistence:** Room database.
- **Async:** Kotlin Coroutines and StateFlow.
- **QR Engine:** ZXing.
- **CI/CD:** GitHub Actions.

---

## 📦 Run & Build Locally

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

The compiled debug APK is generated at:

```bash
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License & Integrity

Designed, engineered, and developed by `@sayanthRock`.
