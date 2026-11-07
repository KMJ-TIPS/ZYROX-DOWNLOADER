# Building ZYROX Downloader

Requirements: JDK 17, Android SDK Platform 36 and Build-Tools 36.0.0. The Gradle wrapper downloads Gradle 8.14.3 and Maven dependencies on its first run.

1. Open `Android/` in Android Studio, or add `sdk.dir=/absolute/path/to/Android/Sdk` to `local.properties`.
2. Run `./gradlew testDebugUnitTest assembleRelease` on macOS/Linux, or `gradlew.bat testDebugUnitTest assembleRelease` on Windows.
3. The ARM64 APK is written to `app/build/outputs/apk/release/app-release.apk`.

The default ARM64 build supports Android 10+ on ARM64 phones. To select another supported ABI, pass `-PtargetAbis=armeabi-v7a` or `-PtargetAbis=x86_64`. A comma-separated list packages multiple ABIs into one larger APK.

This fork has package name `com.zyrox.downloader`, version name `1.0.0`, version code `1`. It installs separately from VidGrab.

Without a local release key, builds use the fork's development signing configuration, stored in `.local-signing/zyrox-debug.keystore`; do not use VidGrab's signing identity. A missing debug keystore can be generated with JDK `keytool`:

```sh
mkdir -p .local-signing
keytool -genkeypair -keystore .local-signing/zyrox-debug.keystore \
  -storepass android -keypass android -alias androiddebugkey \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname 'CN=ZYROX Downloader Development,O=ZYROX,C=IN'
```

For distribution, create your own private release keystore and copy `keystore.properties.example` to `keystore.properties`, replacing each placeholder locally. Release signing files, local SDK paths, Gradle caches, and build outputs are excluded from version control. Keep the same private key for subsequent updates.

The source remains the upstream Kotlin/Jetpack Compose Android implementation with a ZYROX rebrand, and bundles yt-dlp/Python/FFmpeg for on-device operation. It has no hosted extraction backend. See `THIRD_PARTY_NOTICES.md` for upstream component sources and licenses.
