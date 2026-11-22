# Dual Battery Widget

Dual Battery Widget is an Android home-screen widget for monitoring a device's main battery and a compatible secondary dock battery. It was originally created for the Asus Eee Pad Transformer, whose dock battery is not represented by Android's standard battery indicator.

The main battery works on supported Android devices; secondary-battery reporting depends on the device and dock exposing dock battery information to Android.

## Features

- Home-screen battery gauges with configurable battery selection, order, labels, text, and margins
- Charging and dock-connection status, including the last recorded dock level when undocked
- Detailed battery information such as health, technology, voltage, and temperature
- Local battery-history charts for the last 3, 7, or 30 days
- Optional dock-battery level in the notification area
- Phone and tablet layouts for widget configuration and application settings

## Compatibility and background behavior

The current source requires Android 5.0 (API 21) or newer and targets API 37. On Android 13 and newer, the app requests notification permission when opened.

Battery monitoring uses a foreground service with a low-importance ongoing notification, as required by current Android versions. The service listens for battery, dock, and screen changes, updates widgets, and records battery history locally. The separate dock-level notification can be disabled in the app settings.

## Install

The original app is listed on [Google Play](https://play.google.com/store/apps/details?id=org.flexlabs.widgets.dualbattery).

To install a locally built debug APK, use `adb install -r build/outputs/apk/debug/Dual-Battery-Widget-debug.apk` after building.

## Build

Requirements:

- JDK 17 or newer
- Android SDK Platform 37
- Android SDK Build-Tools and platform tools

The Gradle wrapper is included, and application dependencies are resolved from Google Maven and Maven Central.

```bash
git clone https://github.com/goodrench/Dual-Battery-Widget.git
cd Dual-Battery-Widget
./gradlew assembleDebug
```

The debug APK is written to `build/outputs/apk/debug/Dual-Battery-Widget-debug.apk`.

## Tests and checks

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
```

Unit tests cover feedback validation and battery-history series generation. Android Lint checks the application source and resources.

## Project layout

- `src/` — Java and Kotlin application code
- `res/` — Android resources, preferences, layouts, and widget definitions
- `test/` — local JVM unit tests

## Changelog and license

See [CHANGELOG.md](CHANGELOG.md) for the release history and current unreleased changes.

Dual Battery Widget is licensed under the [Apache License 2.0](LICENSE).
