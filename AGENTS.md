# Repository Guidelines

## Project Structure & Module Organization
This repository is an Android multi-module project:

- `MonetizeKit/`: reusable library module under `src/main/java/io/monetize/kit/sdk/...` with ad, billing, remote-config, and UI components.
- `app/`: sample/demo app under `src/main/java/com/test/compose/adslibrary/...` used to exercise the library in Compose and XML flows.
- `MonetizeKit/src/main/res` and `app/src/main/res`: layouts, drawables, strings, themes, and launcher assets.
- Root Gradle files: `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`.

Keep library logic in `MonetizeKit`; use `app` only for integration examples and manual verification.

## Build, Test, and Development Commands
Run commands from the repository root:

- `.\gradlew :app:assembleDebug`: build the demo app debug APK.
- `.\gradlew :MonetizeKit:assembleRelease`: build the library release artifact.
- `.\gradlew :app:installDebug`: install the sample app on a connected device/emulator.
- `.\gradlew test`: run JVM unit tests for all modules.
- `.\gradlew connectedAndroidTest`: run instrumentation tests on a device/emulator.
- `.\gradlew lint`: run Android lint checks.

Use the Gradle wrapper rather than a system Gradle install.

## Coding Style & Naming Conventions
Use Kotlin with 4-space indentation and standard Kotlin style. Match the existing package layout by feature, for example `ads/interstitial`, `domain/usecase`, and `presentation/viewmodels`.

- Types: `PascalCase`
- functions/properties: `camelCase`
- constants: `UPPER_SNAKE_CASE`
- layout files: `snake_case.xml` such as `small_native_layout.xml`

Prefer small feature-focused files and keep Compose/UI helpers near the feature they support.

## Testing Guidelines
There are no committed `src/test` or `src/androidTest` sources yet. New behavior should add focused coverage where practical:

- JVM tests in `<module>/src/test/...`
- instrumentation/UI tests in `<module>/src/androidTest/...`

Name test classes after the subject, for example `BannerAdControllerTest`.

## Commit & Pull Request Guidelines
Recent history uses short, imperative commit subjects such as `Update README.md` and version-oriented messages like `3.3.5 subscription, oneTime-purchase changed`. Keep subjects concise and specific.

Pull requests should include:

- a short summary of user-visible or API-facing changes
- affected module(s): `app` or `MonetizeKit`
- screenshots/video for UI changes
- test or manual verification notes

## Security & Configuration Tips
Do not commit secrets. `local.properties` is ignored and should stay local. Treat `app/google-services.json` as environment-specific; coordinate before replacing it. Verify ad IDs, billing product IDs, and Firebase settings in the demo app before release builds.
