# ZYROX DOWNLOADER 1.1 validation

Release date: 8 November 2026 (Asia/Calcutta). Version1.1.0/code2; packagecom.zyrox.downloader; minSDK29,targetSDK36,ARM64.

## Checks

- **73 JVM tests passed**, zero failures/errors/skips. Debug and release Kotlin/Java compilation passed, including the wrapping sound controls.
- Android lint passed with **0 errors and 10 warnings**.
- Signed/minified ARM64 release APK built successfully: version1.1.0/code2, minimumAPI29,targetAPI36. APK signature v2 and ZIP alignment passed.
- APK is62,295,679bytes; SHA-256 `6c3e197bfaae167b1ade97fd9ed6646a41a67fbee636072d295960f9a090dbee`. Its signing certificate exactly matches1.0.0, allowing an in-place update.
- Both bundled WAVs match their source bytes after minification, and their stable rawresource names remain present. See `validation-v1.1/build-verification.json` for evidence.

The two original bundled sound assets are valid mono44.1kHz PCM16 WAVs: a65ms tick and630ms ascending completion chime. Both are explicitly preserved by resource-shrinker rules; the APK check confirms their bytes and rawresource names. The completion channel uses a stable named resource URI so future resource-ID changes do not break it.

Code review traced the completion notification to the successful file-save/history-persistence path. Failure/cancel paths use the quiet result channel. Button sounds are gated by the app switch, phone ringer mode, Do Not Disturb, system volume and Android Touch sounds. Automatic completion audio belongs to Android's notification system and respects permission/channel/mute/volume controls. No separate success player bypasses those controls. See [SOUNDS.md](SOUNDS.md).

## UI renders and download page

All **6 Paparazzi production UI render cases passed**: dark/light Home, Library, Music, Settings with sounds and the native splash. Final settled frames were exported at1080×2340 and visually inspected. The new blue palette, raised controls and download logo/splash match the source. See `validation-v1.1/ui-renders.json` and `docs/assets/screenshots/provenance.json` in the repository.

Images are offscreen renders of the actual production AppRoot and native splash; they are not physical-phone captures. Settings uses the supported API29 profile and default sound-preference values in inspection mode because layoutlib does not implement real system storage/audio/preferences services. The opt-in render harness leaves the production APK free of test tooling.

The new download site passed browser checks at widths320,360,768 and1440: no page overflow, broken images, failed asset requests or JavaScript errors. All4 APK buttons target1.1.0; all6 gallery cards and9 screen placements load. Keyboard menu/gallery, FAQ and reduced-motion controls passed. Website taps start off with no automatic audio request; the explicit WAV preview completed. See `validation-v1.1/download-site.json`.

## Remaining runtime verification

There was no connected physical Android phone or usable emulator. Installation/updating on a phone, audible sound playback, OEM notification-channel behavior, audio interaction with other apps and live platform downloads still require device verification. Existing JVM tests, host-side WAV checks and UI renders do not establish those runtime outcomes.
