# AGENTS.md

## Cursor Cloud specific instructions

### Project overview

SimpleTaskRegister is a native Android app (Kotlin + Jetpack Compose + Material 3) for simple task management. Single-module Gradle project, no backend, no database — all state is in-memory.

### Environment variables

Before running any Gradle command, export:

```
export ANDROID_HOME=/opt/android-sdk
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
```

### Key commands

| Action | Command |
|--------|---------|
| Build debug APK | `./gradlew assembleDebug` |
| Run lint | `./gradlew lint` |
| Run unit tests | `./gradlew test` |
| Install on emulator | `adb install app/build/outputs/apk/debug/app-debug.apk` |

### Android emulator (no KVM)

The Cloud VM does **not** have KVM hardware virtualization. The emulator runs with `-no-accel` and is extremely slow. System ANR dialogs appear frequently — dismiss them with `adb shell input tap` on the "Wait" button. An AVD named `test_device` is pre-created.

Start the emulator:
```
emulator -avd test_device -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect -no-accel -memory 2048
```

Wait for boot to complete (can take several minutes):
```
adb wait-for-device shell 'while [ "$(getprop sys.boot_completed)" != "1" ]; do sleep 5; done'
```

### Gotchas

- JDK 21 is installed and compatible with AGP 8.5.2 (requires JDK 17+).
- The Gradle wrapper (`gradlew`) is committed; no need to install Gradle separately.
- `keystore.properties` is not committed (release signing is optional); debug builds work without it.
- Emulator screenshots must use `/data/local/tmp/` instead of `/sdcard/` due to permissions.
