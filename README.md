# 📱 Rock QR Code


Rock QR Code is a highly polished, offline-first Material 3 Android application for scanning and generating QR codes. It features visual elements like **Glassmorphism theme settings**, customizable **geode styles**, tactile UI controls with physical scaling animations, and an automated GitHub Actions CI/CD setup.

---

## 🎨 Design & Key Features

- **Crystal Rock & Rounded Pebbles Styles**: Generate QR codes mapped with unique visual styles, including classic structures, rounded pebbles, or sharp metamorphic rock textures.
- **Glassmorphism Interfaces**: Clean semi-transparent card panels, glassy dialogs, and tactile touch behaviors with fine physical scale-down/alpha-shifting feedback.
- **Sleek Settings Panel**: Easily customize light/dark/system theme alignment, toggle Material You dynamic coloring options, and learn about the core QR builder engine.
- **Fully Local & Offline-First**: Built with a reliable SQLite database using KSP Compiled Room Database wrappers to securely store historical scanning entries offline.
- **Continuous Integration (CI)**: Powered by a complete GitHub Actions validation script verifying Gradle parameters, checking compiler settings, and running JVM/Robolectric test suites automatically on every commit.

---

## 🛠️ Tech Stack

- **UI Framework**: Modern Jetpack Compose using Jetpack Navigation, custom graphics overlays, and Material Design 3.
- **Local Persistence**: Google Room Database with Flow observation.
- **Asynchronous Engine**: Kotlin Coroutines and Flows for offline-first reactive states.
- **Unit Testing**: JUnit, Robolectric for local JVM testing, and Roborazzi for automated UI/visual regression testing.
- **Build Automation**: Modern Gradle with Kotlin DSL (`build.gradle.kts`) and central catalog dependency systems (Version Catalog).

---

## ⚙️ Automated CI/CD Workflow

The project is configured with a robust GitHub Actions workflow file (`build.yml`):
1. **Validation Tasks**: Checks `gradle.properties` to ensure modern Kotlin compiler execution strategies have replaced deprecated in-process settings and validates the Gradle Wrapper checksums/permissions.
2. **Unit Tests Run**: Runs JUnit and Robolectric-backed test plans locally without needing an emulator.
3. **Summary Generator**: Extracts raw JUnit results automatically into Markdown formats, displaying status grids directly inside the GitHub Actions live panel summary.
4. **HTML Report Archive**: Uploads detailed HTML reports of test execution results as run artifacts to make tracing failures straightforward.
5. **Debug Compilation**: Generates a compiled APK artifact ready for manual checks, downloads, or deployment.

---

## 📦 How to Compile or Install

### Prerequisites
- Android Studio Koala or newer.
- Gradle JDK level 17+.

### Building Locally
Clone the repository and run:
```bash
./gradlew assembleDebug
```
The compiled APK will be produced at:
`app/build/outputs/apk/debug/app-debug.apk`

### 📲 Downloading the Pre-Built APK
You do not need to compile the application locally to test it on your device:
1. **GitHub Action Artifacts**: On every successful commit to `main` or `master`, the [Rock QR Code CI Workflows](https://github.com/SayanthRock/Rock-QR-Code/actions) run automatically. You can click on any workflow run and download the precompiled **`rock-qr-code-debug-apk`** artifact.
2. **GitHub Releases**: Tagging your repository with a version prefix (e.g., `v1.0.0`, `v1.1.2` etc.) automatically triggers a production-grade packaging pipeline. Under the **Releases** section, a release is automatically created with the high-performance release log notes and the runnable **`app-debug.apk`** binary attached directly to the release.

### Running Unit Tests
To execute unit test cases locally:
```bash
./gradlew testDebugUnitTest
```

---

## 📄 License & Ownership
Created and developed by `@sayanthRock` | Licensed under the MIT License.
