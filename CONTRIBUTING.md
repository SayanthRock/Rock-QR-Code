# Contributing to Rock QR Code

Thanks for helping improve Rock QR Code. Everyone is welcome to suggest fixes, improve the UI, report bugs, or add useful features.

## How to make a change

1. Fork this repository.
2. Create a new branch in your fork.
3. Make your code or documentation changes.
4. Test the Android build locally if possible:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

5. Open a Pull Request into the `main` branch.
6. Wait for GitHub Actions to finish.

## Good first changes

- Fix UI spacing, text, or icons.
- Improve README documentation.
- Add screenshots or app previews.
- Fix scanner bugs.
- Improve QR generation styles.
- Improve app performance.

## Pull Request rules

- Keep changes focused and easy to review.
- Do not commit APK files directly into the repository.
- Do not add private keys, tokens, passwords, or secrets.
- Do not add unnecessary permissions.
- Explain what changed and why.

## Android build check

Every Pull Request should pass the Android APK build workflow. The workflow builds the APK and uploads it as an artifact for testing.

## Safe contribution model

This repository accepts changes through Pull Requests. Direct write access is not given to everyone because that can break builds or add unsafe code. Pull Requests let everyone contribute while keeping the APK safe and reviewable.

## Maintainer

Made and maintained by **Sayanth Rock**.
