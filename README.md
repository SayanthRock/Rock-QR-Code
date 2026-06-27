# 📱 Rock QR Code

Rock QR Code is an offline-first Android QR generator and scanner built with Kotlin and Jetpack Compose.

[![Android CI](https://github.com/SayanthRock/Rock-QR-Code/actions/workflows/android-autoheal-ci.yml/badge.svg)](https://github.com/SayanthRock/Rock-QR-Code/actions/workflows/android-autoheal-ci.yml)

---

## ✅ Current Project Status

The repository is now focused on the Android app and the Android CI build pipeline.

---

## 💎 Android Feature Highlights

- **Modern Jetpack Compose UI** with a premium dark interface.
- **Offline QR generation and scanning** without requiring a hosted web page.
- **Camera scanner support** with Android camera permission.
- **Local history storage** using Room database.
- **Custom QR styling** for generated codes.
- **Clean sharing** through QR image sharing or plain text content sharing.
- **GitHub Actions CI** for debug APK build, unit tests, and artifact upload.

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
