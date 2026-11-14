# Android modernization — first pass

Branch: `android-modernization` (preserves original `master`).

## Implemented in this branch

- Widget provider renders directly from AppWidget callbacks instead of starting a legacy background service.
- Read the current sticky battery broadcast when refreshing, avoiding stale process-wide battery data.
- Change the widget update interval from 24 hours to 30 minutes (Android may defer periodic updates).
- Handle power connection/disconnection and battery-low/okay system broadcasts when Android delivers them. **Do not rely on these broadcasts alone on Android 8+**, where implicit-broadcast restrictions can apply.
- Fix the fourth widget variant being excluded from the array length when refreshing all widget sizes.
- Add immutable activity PendingIntent flag for Android 12+ compatibility.
- Declare explicit component export states and the boot permission.
- Refresh the current widget on per-widget preference changes while settings are open.

## Not yet done

This repository is an Eclipse/Ant project targeting Android API 16, using ActionBarSherlock, legacy support libraries, billing, and three Git submodules. **This branch is not yet buildable as a modern Android app**, and no APK or device verification has been performed. The existing old build setup also cannot compile the newer PendingIntent flag without updating compileSdk.

1. Migrate to a current maintained Gradle/Android Gradle Plugin build and replace obsolete libraries; decide what to do with legacy billing/history modules.
2. Re-test manifest-registered power broadcasts on a target-SDK-35+ build. If they are not delivered, implement a supported battery monitoring strategy (such as a foreground monitoring mode with clear opt-in, or accept periodic refresh limitations). `ACTION_BATTERY_CHANGED` cannot be received by a manifest-declared receiver.
3. Test refresh after charging state changes, 1% changes, screen off, boot, launcher restart, and process death. A 30-minute scheduled fallback is **not** real-time.
4. Theme support currently means existing normal/rotated layout and white/black text. Material You/day-night palettes are **not implemented**.
5. Inspect per-widget preference persistence and rendering on Pixel 9 (especially theme or text color changes and resizing).

## Suggested Ubuntu verification

```bash
git fetch origin
git switch android-modernization
git log --oneline -8
# After Gradle migration:
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell dumpsys appwidget
adb shell dumpsys battery
```

Do not merge to master until the project builds and the widget is tested on an Android 15–17 device.
