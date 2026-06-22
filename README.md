# Lunabelle 🌸

A cute, fully-offline menstrual cycle tracker for Android — Jetpack Compose + Material 3 Expressive.

## Features
- **Private & offline-first:** no INTERNET permission, encrypted database (Room + SQLCipher), biometric/PIN app lock, JSON export/import.
- **Predictions:** skip-aware Bayesian cycle model with recency weighting + symptothermal refinement (LH tests, BBT, cervical mucus).
- **Reminders:** per-phase custom reminders (each cycle phase, your own title & text).
- **Tracking:** calendar with sexual-activity ❤️ marking, daily symptom/flow logging, statistics & insights.
- **Design:** Material 3 Expressive, Nunito type, wavy indicators, pick-your-own theme colour (full HSV picker), light/dark, RU/EN.
- minSdk 26 (Android 8+), targetSdk/compileSdk 37.

## Build (Docker)
The project builds in a thin JDK container with the Android SDK/Gradle caches bind-mounted:

```sh
docker run --rm --network=host --cpus=6 --memory=4g \
  -e ANDROID_PLATFORM=android-37.0 -e ANDROID_BUILD_TOOLS=37.0.0 \
  -v "$PWD":/workspace -v "<cache>/gradle":/cache/gradle -v "<cache>/sdk":/opt/android-sdk \
  -w /workspace period-tracker-build:latest \
  ./gradlew assembleDebug
```

Release signing reads `keystore.properties` (kept out of version control).
