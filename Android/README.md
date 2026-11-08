# ZYROX DOWNLOADER

A rebranded fork of [Saurabh-gzp/VidGrab](https://github.com/Saurabh-gzp/VidGrab), with on-device yt-dlp and FFmpeg. Android 10+ · ARM64 · Kotlin / Jetpack Compose with native Java launch artwork and media player.

## Build

Install JDK 17, Android SDK Platform 36 and Build Tools 36.0.0. Open this folder in Android Studio. Set your SDK path in a local, untracked `local.properties`, then create the local development key using the command in [build notes](docs/BUILD-NOTES-ZYROX.md), and run:

```sh
./gradlew testDebugUnitTest lintDebug assembleRelease
```

The default APK targets ARM64 (`arm64-v8a`). Other supported CPUs can be built with `-PtargetAbis=armeabi-v7a` or `-PtargetAbis=x86_64`.

See [build and signing notes](docs/BUILD-NOTES-ZYROX.md) for signing configuration. Local signing keys, passwords, SDK paths and build caches are excluded from the source archive. Configure your own release identity for distribution. Application ID: `com.zyrox.downloader`; version: `1.1.0`.

## Rebrand

- New download-arrow logo, adaptive/themed launcher icons and editable SVG/PNG brand assets.
- Midnight cobalt and ice-cyan dark theme; white and deep-blue light theme; selectable system/light/dark appearance.
- Redesigned Home, quality selection, Library/history, Music and Settings with animated navigation and press feedback.
- Native Java splash animation, plus Android 12+ branded system launch screen. Shortened startup animation when system animations are disabled.
- Native local video/audio player with seeking, playback controls and audio-focus handling. Saved media playback contains no advertising. Playback pauses when leaving the player.
- Separate app identity and storage folders: `Movies/ZYROX/Video` and `Music/ZYROX/Audio` through MediaStore.
- Existing metadata, quality selection, download queue, cancellation, retry, sharing, history and verified official yt-dlp updates are retained.

## Downloads and limits

Paste a public video link or use another app's Share menu → ZYROX DOWNLOADER, fetch formats, choose video/audio and start the download. Active transfers and history are in Library; saved audio also appears in Music. The native engine runs on the phone; no developer-operated download server, account or API key is required.

Platform support depends on yt-dlp and on the source's current access rules. Private, login-only, geo-restricted or DRM media may fail. This build does not import account cookies or promise access to every platform/account. Only save media you are allowed to download. This app does not modify the official YouTube player's advertisements.

## Provenance and licenses

Based on the repository's browsable Android project, upstream 3.1.0. The original is Kotlin/Compose and remains so; it has not been represented as a complete Java rewrite. Original project credits are in Settings → About and [upstream documentation](docs/upstream/UPSTREAM-README.md). The bundled engine provenance is recorded in `docs/engine-provenance.json`. Third-party runtimes have their own licenses: see [notices](docs/THIRD_PARTY_NOTICES.md).

The validation results for this fork are in `docs/VALIDATION-1.1.md`. Older validation documents describe upstream work, not testing performed on this rebrand.

## UI screenshots

The public gallery contains actual offscreen renders of the production UI. To reproduce all six images without a phone/emulator, follow [screenshot render instructions](docs/SCREENSHOT-RENDERS.md). The renderer is enabled only by the `renderScreenshots` Gradle property and is absent from the released APK.

## Version 1.1 sounds

Settings has independent **Button sounds** and **Download complete sound** switches, both initially on. Soft taps respect Android Touch sounds, silent/vibrate, DND and system volume. The original completion chime is the success notification sound after the file and history record are saved; failed/cancelled downloads stay quiet. Notification permission and channel settings apply. Preview the tone in Settings or open Android's notification controls there. See [sound behavior and source](docs/SOUNDS.md).

Version 1.1.0 uses version code 2 and the same local signing certificate as the delivered 1.0.0 build. Install the new APK over that build to retain the library and preferences.
