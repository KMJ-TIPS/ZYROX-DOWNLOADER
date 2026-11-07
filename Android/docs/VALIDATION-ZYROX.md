# ZYROX validation

Fork base: Saurabh-gzp/VidGrab commit `ffbb72a7044b37a3008bf61de8261411cacee951` (3.1.0).

## Completed checks

- Debug and release Kotlin/Java compilation succeeded for the migrated `com.zyrox.downloader` package, including the custom Java splash and local player.
- Android `lintDebug` completed successfully. It reported warnings, with no errors; detailed counts are in `validation-zyrox/build-verification.json`.
- Existing JVM regression suite: **73 tests, 0 failures, 0 errors, 0 skipped**. This covers format selection, metadata snapshots, URL handling, progress, engine access coordination, artifact files and official engine update/checksum behavior.
- Bundled yt-dlp is version `2026.10.26`. Its SHA-256 matches `docs/engine-provenance.json`: `e51e69e89489f199530f13d32a4342da3ddb2cf34842f364603a066db05afbf3`.
- The bundled zipapp successfully downloaded the public W3Schools Big Buck Bunny MP4 sample on the build host. This checks the bundled engine, HTTPS extraction and a real transfer; it is not an Android-runtime or all-platform test.
- Minified release APK built successfully. APK signature v2 and ZIP alignment checks passed with the fork's fresh development certificate.
- APK metadata: label `ZYROX DOWNLOADER`, app ID `com.zyrox.downloader`, version `1.0.0` / code `1`, minimum SDK 29, target SDK 36, ARM64 only. Native Python, QuickJS, FFmpeg, FFprobe and license assets are present.
- All Android resource XML and the manifest parse successfully.
- Branding audit found original product names only in deliberate upstream attribution and internal compatibility identifiers, not current product labels/icons.

## UI gallery and website

Six production UI render cases passed using Paparazzi/layoutlib: Home dark/light, Library, Music, Settings and the Java splash. Screens are published as explicitly labelled offscreen app UI renders, not device captures. The rendering setup is opt-in and leaves the release APK unchanged. See `SCREENSHOT-RENDERS.md` and `validation-zyrox/ui-renders.json`. The download site passed browser checks at widths 320, 360, 768 and 1440 with working links/gallery/FAQ and no broken images, JavaScript errors or page overflow.

## Runtime limits

No connected phone or usable Android emulator was available. The final splash animation, device-specific narrow/large-font layouts, local media player, notification/queue lifecycle and platform-specific downloads still need phone verification. Historical upstream validation documents describe upstream tests and must not be treated as new validation of this rebrand.
