# 📱 Rock QR Code Ecosystem

A high-fidelity, dual-platform QR Code suite delivering a premium user experience across both an **offline-first Android client** (Jetpack Compose) and a **luxury companion web app** (GitHub Pages / PWA). 

[![Rock QR Code CI](https://github.com/SayanthRock/Rock-QR-Code/actions/workflows/build.yml/badge.svg)](https://github.com/SayanthRock/Rock-QR-Code/actions/workflows/build.yml)

---

## 🌐 Live Companion Web App

Experience the premium web suite instantly:
👉 **[Try the Rock QR Web Console](https://sayanthrock.github.io/Rock-QR-Code/)**

*Fully installable Progressive Web App (PWA) with complete offline support, real-time dynamic web links, custom metadata tags, and integrated camera scanner lens.*

---

## 💎 Ecosystem Feature Highlights

### 📱 Android Native Application
- **Modern Jetpack Compose UI**: Designed around Material 3 fluid glassmorphic palettes, customizable geode styles, and tactile feedback.
- **Offline Persistence**: Google Room Database integrated with modern flow architecture to manage historic scans securely on-device.
- **Flexible Options**: Generate codes mapping unique structures, rounded pebbles, or metamorphic textures.
- **Continuous Integration**: Configured with automated Robolectric and Roborazzi UI/Visual Regression testing suites.

### 🌐 Progressive Web App (Companion)
- **Fluid glassmorphic client interface**: Designed with Tailwind CSS, Plus Jakarta Sans display typography, and beautiful frosted-glass backdrops.
- **Web-to-App Synchronization Links**: Share generated QR matrices via universal deep-links that seamlessly import into the companion hub.
- **Interactive Lens Scanner**: Embedded high-performance webcam scanning using local JS decoding routines.
- **Dual-Mode Portability**: Fully compatible for home screen installations, functioning entirely offline without server dependencies.

---

## 🛠️ Combined Architecture & Tech Stack

### Native Client Core (Android)
- **Framework**: Jetpack Compose, Jetpack Navigation, custom graphics canvas overlays, vector-drawn QR modules.
- **Persistence**: SQLite Room Database with Kotlin Symbol Processing (KSP).
- **Asynchronous Flow**: Kotlin Coroutines and StateFlow lifecycle elements.

### Web Console Core (PWA)
- **Design & Layout**: Tailwind CSS custom configuration, Lucide premium symbols.
- **Matrix Generation**: Client-side QR generation engine (QRCode.js).
- **Capture Engine**: HTML5-QR webcam reader routines.
- **Caching**: Service worker `sw.js` script with structured service-level resource interception.

---

## ⚙️ Automated CI/CD Pipelines

Our workflows run automatically inside `.github/workflows/`:
1. **Build Checklist (`build.yml`)**: Restores dependencies, runs local Robolectric tests, and packages debug binaries.
2. **Release pipeline (`release.yml`)**: Triggered automatically on tags (e.g. `v1.2.0`), auto-generating rich changelogs from commits and attaching production APK bundles to releases.
3. **Dependency submission (`dependency-submission.yml`)**: Continuous tracking of dependency trees to ensure security compliance.

---

## 📦 Run & Build Locally

### 📱 Running the Android App
Clone the repository and compile using Gradle:
```bash
# Compile and generate a local debug APK
./gradlew assembleDebug

# Execution of test suites
./gradlew testDebugUnitTest
```
The compiled APK is preserved at `app/build/outputs/apk/debug/app-debug.apk`.

### 🌐 Running the Web Console
Double-click `index.html` or host a quick local server:
```bash
python3 -m http.server 8000
```
Open `http://localhost:8000` to test local service workers and webcam support.

---

## ⚙️ Setting Up GitHub Pages
To publish the live web version on your own repository branch:
1. Navigate to your repository **Settings** tab.
2. Select **Pages** from the left navigation.
3. Under **Build and deployment**, select **Deploy from a branch** as the source.
4. Set the branch to `main` (or your active default branch) and directory to `/ (root)`.
5. Tap **Save**. Your site will be published at your custom GitHub Pages URL!

---

## 📄 License & Integrity
Designed, engineered, and developed by `@sayanthRock` | Licensed under the MIT License.
