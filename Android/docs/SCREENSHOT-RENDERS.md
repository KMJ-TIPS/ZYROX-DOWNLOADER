# App UI renders

These are renders of the production `AppRoot` Compose UI and `ZyroxSplashView` Java view using [Paparazzi](https://cashapp.github.io/paparazzi/) and Android layoutlib. They are **app UI renders, not device captures**. They do not demonstrate a download, media playback or device installation.

The renderer restores the actual selected-tab saved state and supplies a test ViewModel owner. It leaves the production UI unchanged, uses an empty first-use library, and does not initialize the native extraction engine or any network activity. The dark/light Home, Library, Music and splash renders use the API 36 profile. Settings uses the supported API 29/Android 10 profile because layoutlib has no real StorageManager/package service for the all-files access query on newer APIs. This exercises the app's actual scoped-storage display branch.

Paparazzi records four frames from attachment through 1500 ms. The export script selects the final lossless PNG frame, after the real entrance and splash animations settle. It does not redraw, composite, crop, resize or replace the app screen.

To reproduce with JDK 17 and the existing SDK/Gradle setup:

```sh
./gradlew -PrenderScreenshots=true :app:recordPaparazziDebug \
  --tests com.zyrox.downloader.ProductionScreenshots
python3 -m pip install Pillow
python3 scripts/export_screenshots.py --output docs/screenshots
```

The opt-in `renderScreenshots` flag applies Paparazzi and adds `src/screenshotTest/java` only to the test source set. Normal unit tests and production APKs do not include Paparazzi or the rendering harness. The previously built release APK remains unchanged.

Raw recordings are in `app/src/test/snapshots/videos`. Exported images include `provenance.json` with the source-recording and final-PNG SHA-256 hashes, API profile and frame selection.

Paparazzi is test tooling under Apache-2.0; source: https://github.com/cashapp/paparazzi/tree/2.0.0-alpha02.
