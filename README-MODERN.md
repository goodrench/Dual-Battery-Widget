# Dual Battery Widget — Android modernization

This is a **new standalone Android app module**, designed to compile independently of the legacy Eclipse/Ant sources and retired third-party libraries. Original source and resources remain in the repository as historical reference.

## What is implemented

- Android 8+ baseline, Android SDK 35 target; Gradle/AGP build.
- Home-screen widget for phone battery level and charging state.
- Optional paired Bluetooth battery display using the battery percentage that Android exposes via `BluetoothDevice.getBatteryLevel()`.
- Settings to select paired accessory, choose System / Dark / Light / Material You text palettes, and refresh the widget.
- Widget updates for launcher requests, charging changes when delivered, battery-low/okay, reported accessory changes when delivered, boot, and a 30-minute system-managed fallback.
- Tapping widget opens settings.
- GitHub Actions build producing an installable debug APK artifact.

## Limitations

- Android does **not** expose battery percentages for all Bluetooth accessories. This implementation does not actively connect to generic BLE Battery Service devices, does not scan for unpaired devices, and does not guarantee per-earbud levels.
- No live 1%-by-1% update guarantee with a widget-only background architecture: broadcast delivery is OS-controlled, and `updatePeriodMillis` is a best-effort scheduled fallback.
- Widget artwork currently uses a new minimal layout. The old battery graphics remain in the repository but are not yet integrated into this independent app module.
- The original history, dock-battery-specific integration, donation/billing, and settings screens have **not** been ported.
- Material You changes the widget text palette where available; it does not yet dynamically recolor the background or progress tracks.

## Build and install on Ubuntu

Install Android SDK platform 35 and JDK 17. Use Gradle 8.11.1:

```bash
gradle :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or visit the repository **Actions** tab, open a passing 'Android build' run, and download the `dual-battery-debug-apk` artifact.

After installing, open the app, grant Bluetooth permission, select an accessory, save, and add **Dual Battery Widget** to the home screen.

Test charging/discharging, Bluetooth reconnect, theme changes, launcher restart, device reboot, and battery saver. Report results with screenshots and `adb logcat -d -s AppWidgetHostView AndroidRuntime`.

This is a first full-source integration **without device or CI verification yet**. Do not merge to master until a CI build passes and device behavior is validated.
