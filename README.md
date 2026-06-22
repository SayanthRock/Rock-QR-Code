# Rock QR Code

![Rock QR CI](https://github.com/SayanthRock/Rock-QR-Code/actions/workflows/build.yml/badge.svg)

**Rock QR Code** is a clean, offline-first QR scanner and generator app built with Android Jetpack Compose, CameraX, Room, and ZXing. It also includes a lightweight GitHub Pages web companion for quick QR access from the browser.

## Download APK

The latest APK is published automatically from GitHub Actions.

**Latest release:** [Rock QR Latest APK](https://github.com/SayanthRock/Rock-QR-Code/releases/tag/latest)

Recommended APK file names from Releases:

- `Rock QR.apk`
- `Rock_QR.apk`
- `Rock-QR-release-v*.apk`

> Install note: if an older APK was signed with a different temporary key, uninstall the old app once, then install the new APK. After that, future APK updates should install normally.

## Features

### Android app

- Scan QR codes using the device camera.
- Generate QR codes locally on-device.
- Save scan and generated-code history with Room database.
- Favorite important QR records.
- Share generated QR images.
- Modern Material 3 interface with liquid glass styling.
- Dark and light theme controls.
- Offline-first privacy, no server upload needed for QR generation or history.

### Web companion

- Live web version through GitHub Pages.
- Browser QR generation and scanning support.
- PWA-style service worker cache for basic offline use.
- Deep-link support for sharing QR data into the Android app.

**Web app:** [https://sayanthrock.github.io/Rock-QR-Code/](https://sayanthrock.github.io/Rock-QR-Code/)

## Tech stack

| Area | Technology |
|---|---|
| Android UI | Kotlin, Jetpack Compose, Material 3 |
| Camera | CameraX |
| QR engine | ZXing |
| Local storage | Room Database |
| Async state | Kotlin Coroutines, StateFlow |
| Images/UI background | Coil, Compose Canvas |
| Testing | JUnit, Robolectric, Roborazzi |
| CI/CD | GitHub Actions |
| Web | HTML, Tailwind CDN, JavaScript, Service Worker |

## Build locally

```bash
# Run tests
./gradlew testDebugUnitTest

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

Generated APK paths:

```text
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release.apk
```

## GitHub Actions release flow

The workflow in `.github/workflows/build.yml` runs on pushes to `main` or `master`, version tags, pull requests, and manual dispatch.

It now does the following:

1. Sets up JDK 17 and Gradle.
2. Decodes the stable debug keystore for repeat install compatibility.
3. Runs unit tests.
4. Builds both debug and release APKs.
5. Uploads APK artifacts.
6. Updates the `latest` tag on main/master pushes.
7. Publishes or updates the GitHub Release with APK files attached.

## Current Android package

```text
applicationId: com.aistudio.rockqr.qzlypm
versionName: 1.0.2
versionCode: 3
minSdk: 24
targetSdk: 36
```

## Permissions

Rock QR keeps permissions minimal:

```xml
android.permission.CAMERA
android.permission.VIBRATE
```

No wallpaper permissions, location permissions, or unnecessary network permissions are required for the Android APK.

## Project structure

```text
app/
  src/main/java/com/example/
    data/          Room database, DAO, repository, records
    ui/            Compose screens, components, theme
    utils/         QR generation, scanning helpers, share helpers
    viewmodel/     App state and actions
  src/main/res/    Icons, strings, theme resources, XML configs
.github/workflows/
  build.yml        Test, build, artifact upload, release publish
index.html         Web QR companion
share/             Web share/import page
sw.js              Service worker cache
```

## Cleaned up

- Removed old wallpaper permissions from the Android manifest.
- Removed unused Gradle dependency aliases and unused plugin entries.
- Removed old root APK copy task clutter.
- Removed unused Moshi, Retrofit, OkHttp, Firebase, Play Services, DataStore, Navigation, and Secrets plugin references from the Android build setup.
- Kept CameraX, Room, Coil, ZXing, Compose, and testing dependencies that the app actually uses.

## Roadmap

- Add Play Store-ready signed release support using repository secrets.
- Add better scanner permission UI for first launch.
- Add export/import history backup.
- Add more QR styles and brand presets.
- Add screenshot previews to this README.

## Author

Made by **Sayanth Rock**.

GitHub: [@SayanthRock](https://github.com/SayanthRock)
