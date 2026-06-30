# 📱 Rock QR Code

Rock QR Code is an offline-first Android QR generator and scanner built with Kotlin and Jetpack Compose.

[![Android CI](https://github.com/SayanthRock/Rock-QR-Code/actions/workflows/android-autoheal-ci.yml/badge.svg)](https://github.com/SayanthRock/Rock-QR-Code/actions/workflows/android-autoheal-ci.yml)
[![GitHub Pages](https://github.com/SayanthRock/Rock-QR-Code/actions/workflows/pages.yml/badge.svg)](https://github.com/SayanthRock/Rock-QR-Code/actions/workflows/pages.yml)

---

## 🌐 Public Website

GitHub Pages landing page:

```text
https://sayanthrock.github.io/Rock-QR-Code/
```

This repo now includes `index.html`, `404.html`, `.nojekyll`, and a GitHub Pages deployment workflow so the public link has a proper homepage instead of showing GitHub Pages 404.

---

## ✅ Current Project Status

The repository is now focused on the Android app, the Android CI build pipeline, and a simple static GitHub Pages landing page.

---

## 💎 Android Feature Highlights

- **Modern Jetpack Compose UI** with a premium dark interface.
- **Offline QR generation and scanning** without requiring a hosted backend.
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
- **CI/CD:** GitHub Actions and GitHub Pages.

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
