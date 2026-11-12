<div align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="96" height="96" alt="JUAR4M Icon">
</div>

# JUAR4M

<img src="screenshots/maincontent-preview.png" width="512" height="auto" alt="Screenshot of the app">

An Android RC Car controller for the "Gigo Maker Control Box" via Bluetooth.

This app was developed for the GreenMech Robot for Mission (R4M) Classic competition. It prioritizes low latency, connection reliability, and precise motor control.

The logo (designed by Jo) features a sabertooth, symbolizing precision and decisive action.

## Firmware (Arduino Code)

> [!IMPORTANT]
> This app requires the companion JUAR4M firmware, which handles the Bluetooth protocol, pin mapping, and safety logic.
>
> **JUAR4M Arduino Repository**: [1rens1/juar4m-arduino](https://github.com/1rens1/juar4m-arduino)

## Requirements

- Android 10 ("Q") (API level 29) or greater
- Bluetooth connectivity and permissions

## Download and Install

1. Open the [latest GitHub Release](https://github.com/1rens1/juar4m/releases/latest).
2. Under **Assets**, download the `.apk` file.
3. Open the downloaded APK on your Android device and follow the installation prompts. Android may ask you to allow your browser or file manager to install unknown apps.

The release page provides the APK directly; there is no archive to extract.

## CI and Releases

Pull requests targeting `master` and pushes to `master` run Android lint, local unit tests, and a debug APK build.

Signed release APKs are built and attached directly to GitHub Releases when a version tag is pushed:

```sh
git tag v1.6.4
git push origin v1.6.4
```

The release workflow reads these secrets from the GitHub Actions `release` environment:

- `KEYSTORE_BASE64`: base64-encoded release keystore
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

## Features

### Connectivity & Safety

- **Auto-Connect:** Automatically connects to the last used device on launch and reconnects if the link is dropped.
- **Watchdog Protocol:**
  - Sends continuous movement commands (Forward, Backward, Left, or Right).
  - If the Arduino does not receive a command within the defined timeout, it automatically stops all motors to prevent runaway robots. (Timeout is defined in the Arduino repository.)

### Controls

- **D-Pad:** Touch-and-drag interface for primary movement.
- **Nudge:** Dedicated buttons for short, precise movement bursts.
- **Servos:**
  - **Servo A & B:** Toggle buttons with configurable angle ranges via sliders.
  - **Servo C:** Dedicated controls for continuous rotation (Up/Down/Stop).

### Utilities

- **Competition Stopwatch:**
  - Auto-starts on first input detection.
  - Tracks Personal Best (PB).
  - Live display of time difference (+/-) against PB.
- **Feedback:** Haptic feedback (vibrations) on button presses.
- **Status Indicators:** Phone battery percentage, Do Not Disturb (DND) status, and connection state.
- **Visuals:** Animated marquee background (can be toggled for battery saving).

## Technologies and Assets Used

- Android Studio
- Kotlin
- Jetpack Compose (Material3)
- Datastore
- [Geist](https://fonts.google.com/specimen/Geist) and [Geist Mono](https://fonts.google.com/specimen/Geist+Mono) by Vercel from Google Fonts (SIL Open Font License, Version 1.1)

## Generative AI

Some Generative AI was used in the development of this project, with all code subject to manual review and testing.
