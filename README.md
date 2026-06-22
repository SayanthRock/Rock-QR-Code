

**Rock QR Code** is a clean, offline-first QR scanner and generator app built with Android Jetpack Compose, CameraX, Room, and ZXing. It also includes a lightweight GitHub Pages web companion for quick QR access from the browser.

## Download APK

The APK is built automatically by GitHub Actions after every push to the `main` branch.

Go to **Actions → Build Android APK → latest successful run → Artifacts → Rock-QR-debug-apk**.

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

## GitHub Actions APK build flow

The workflow in `.github/workflows/build.yml` runs on pushes to `main`, pull requests to `main`, and manual workflow dispatch.

It does the following:

1. Checks out the repository code.
2. Sets up Java 17 using Temurin.
3. Makes `gradlew` executable.
4. Prepares a debug keystore.
5. Runs Gradle unit tests.
6. Builds the debug APK with `./gradlew assembleDebug --stacktrace`.
7. Uploads the generated APK as a GitHub Actions artifact.

## Current Android package

```text
applicationId: com.aistudio.rockqr.qzlypm
versionName: 1.0.3
versionCode: 4
minSdk: 24
targetSdk: 36
```

## Permissions

Rock QR keeps permissions minimal:

```xml
android.permission.CAMERA
android.permission.VIBRATE
android.permission.WRITE_EXTERNAL_STORAGE, maxSdkVersion 28
```

No wallpaper, location, or unnecessary network permissions are required for the Android APK.

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
  build.yml        Test, build, and upload APK artifact
index.html         Web QR companion
share/             Web share/import page
sw.js              Service worker cache
```

## Recent fixes

- Added a clean GitHub Actions APK build workflow.
- Added the missing Kotlin Android Gradle plugin.
- Stabilized scanner camera callbacks by moving UI state updates onto the main thread.
- Improved QR frame decoding for camera row stride and pixel stride handling.
- Simplified Gradle configuration for CI stability.
- Added older Android gallery-save support with scoped legacy storage permission.
- Removed old wallpaper permissions and unused dependency clutter.

## Roadmap

- Add Play Store-ready signed release support using repository secrets.
- Add better scanner permission UI for first launch.
- Add export/import history backup.
- Add more QR styles and brand presets.
- Add screenshot previews to this README.

## Author

Made by **Sayanth Rock**.

GitHub: [@SayanthRock](https://github.com/SayanthRock)
