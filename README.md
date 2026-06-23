# Rock QR Code

![Android CI Debug APK](https://github.com/SayanthRock/Rock-QR-Code/actions/workflows/build.yml/badge.svg)

**Rock QR Code** is a clean, offline-first QR scanner and generator app built with Android Jetpack Compose, CameraX, Room, and ZXing. It also includes a lightweight GitHub Pages web companion for quick QR access from the browser.

## Download APK

Debug APKs are built automatically by GitHub Actions after every push to the `main` branch.

Go to **Actions → Android CI Debug APK → latest successful run → Artifacts → Rock-QR-debug-v1.0.6-7**.

## Production package workflow

Use **Actions → Production Release APK and AAB → Run workflow** to build release package artifacts.

Generated artifacts:

- `Rock_QR.apk`
- `Rock-QR-release-v1.0.6-7.apk`
- `Rock-QR-release-v1.0.6-7.aab`

## Everyone can contribute

Everyone can suggest and make changes by using Pull Requests:

1. Fork this repository.
2. Create a branch in your fork.
3. Make your change.
4. Open a Pull Request into `main`.
5. Wait for the Android CI workflow check.

See [CONTRIBUTING.md](CONTRIBUTING.md) for the full guide.

Direct write access is not open to everyone because that can break the APK or add unsafe code. Pull Requests keep the project open and safe.

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
./gradlew --version
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew assembleRelease bundleRelease
```

Generated output paths:

```text
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/*.apk
app/build/outputs/bundle/release/*.aab
```

## Production stable build flow

### `.github/workflows/build.yml`

Runs on every push and Pull Request to `main`:

1. Checks out the repository code.
2. Sets up Java 17 using Temurin.
3. Validates the Gradle wrapper.
4. Runs `./gradlew --version`.
5. Runs unit tests.
6. Builds the debug APK.
7. Uploads the debug APK as a GitHub Actions artifact.

### `.github/workflows/release.yml`

Runs manually or from `v*` tags:

1. Validates Gradle wrapper runtime.
2. Runs unit tests.
3. Builds release APK and AAB.
4. Uploads release artifacts.

## Current Android package

```text
applicationId: com.aistudio.rockqr.qzlypm
versionName: 1.0.6
versionCode: 7
minSdk: 24
targetSdk: 37
compileSdk: 37
```

## Build toolchain

```text
Android Gradle Plugin: 9.2.0
Gradle Wrapper: 9.4.1
Kotlin: 2.2.10
Java: 17
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
  build.yml       Stable debug APK CI
  release.yml     Manual/tagged release APK and AAB build
.github/dependabot.yml
CONTRIBUTING.md   Contribution guide for Pull Requests
LICENSE           MIT License
index.html        Web QR companion
share/            Web share/import page
sw.js             Service worker cache
```

## Recent fixes

- Upgraded the Android build toolchain to AGP `9.2.0` and Gradle `9.4.1`.
- Updated app build target to API `37`.
- Bumped APK to version `1.0.6` with versionCode `7`.
- Split CI into debug build and release package workflows.
- Removed the duplicate Auto-Heal workflow.
- Enabled strict Gradle wrapper validation in CI.
- Added Dependabot monitoring for Gradle and GitHub Actions updates.
- Restored the Kotlin Android plugin so Kotlin app source files compile correctly.
- Fixed a Compose outlined-button border call that could break APK compilation.
- Added MIT License.

## Roadmap

- Add signed release setup.
- Add better scanner permission UI for first launch.
- Add export/import history backup.
- Add more QR styles and brand presets.
- Add screenshot previews to this README.

## Author

Made by **Sayanth Rock**.

GitHub: [@SayanthRock](https://github.com/SayanthRock)
