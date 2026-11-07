# Third-party notices

ZYROX Downloader is a rebrand of [Saurabh-gzp/VidGrab](https://github.com/Saurabh-gzp/VidGrab). The upstream history and authorship remain attributable to that project. Its README describes the app as MIT; this checkout did not contain a standalone app LICENSE file.

The native extraction and media-processing code remains subject to its own licenses:

| Component | Version used | Source and licensing |
| --- | --- | --- |
| youtubedl-android library, common and FFmpeg modules | 0.18.1 | [Source release](https://github.com/yausername/youtubedl-android/tree/0.18.1), GPL-3.0; included license text in `licenses/youtubedl-android-GPL-3.0.txt`. Published artifacts: `io.github.junkfood02.youtubedl-android` on Maven Central. |
| yt-dlp bundled zipapp | 2026.10.26 | [Source release](https://github.com/yt-dlp/yt-dlp/tree/2026.10.26), [license](https://github.com/yt-dlp/yt-dlp/blob/2026.10.26/LICENSE); included upstream license in `licenses/yt-dlp-UNLICENSE.txt`. |
| FFmpeg | Native build supplied by youtubedl-android 0.18.1 | [FFmpeg source](https://ffmpeg.org/download.html), [licensing and build configuration](https://ffmpeg.org/legal.html), [Android build instructions](https://github.com/yausername/youtubedl-android/tree/0.18.1). FFmpeg builds can be LGPL or GPL depending on configuration; retain the notices from the shipped native package. |
| CPython | Runtime supplied by youtubedl-android 0.18.1 | [Python source and PSF license](https://www.python.org/downloads/source/), [Android runtime packaging](https://github.com/yausername/youtubedl-android/tree/0.18.1). |
| QuickJS | Runtime supplied by youtubedl-android 0.18.1 | [QuickJS source and MIT license](https://bellard.org/quickjs/), [Android integration](https://github.com/yausername/youtubedl-android/tree/0.18.1). |
| AndroidX and Jetpack Compose | Versions pinned in `app/build.gradle.kts` and Compose BOM 2025.06.00 | [AndroidX source](https://android.googlesource.com/platform/frameworks/support/), Apache-2.0. |
| Kotlin and kotlinx | Versions pinned in Gradle files | [Kotlin source](https://github.com/JetBrains/kotlin), [kotlinx serialization source](https://github.com/Kotlin/kotlinx.serialization), Apache-2.0. |
| Coil | 2.7.0 | [Source release](https://github.com/coil-kt/coil/tree/2.7.0), Apache-2.0. |

This notice does not replace component license terms. When redistributing native binaries, retain upstream notices and provide the corresponding source required by applicable licenses. The build uses upstream published native runtime binaries without modification; source and upstream build instructions are linked above.
