# ZYROX sounds

Version 1.1 adds two original synthesized sounds. They contain no sampled media or external recordings. Reproduce the exact PCM files with the standard-library-only script:

```sh
python3 tools/generate_sounds.py
```

Both files use 44,100 Hz, mono, 16-bit PCM WAV. `zyrox_tap.wav` is a 65 ms glassy tick; `zyrox_complete.wav` is a 630 ms ascending three-note chime. Generated peaks are below 0.30 full scale; neither file clips. The resource keep file preserves the completion sound in minified builds, because its Android notification URI uses a stable resource name.

## User controls

Settings contains independent **Button sounds** and **Download complete sound** switches, both initially on. Their values persist in `zyrox_sound_preferences`; existing theme/history DataStore contents are unchanged. Settings also offers an explicit, single-play completion preview and a shortcut to Android's completion notification channel settings.

Button ticks use SoundPool with `USAGE_ASSISTANCE_SONIFICATION`. They require the app switch, normal ringer mode, DND off, nonzero system sound volume, and Android's Touch sounds setting enabled. No code raises phone volume or requests media audio focus.

Automatic completion chimes belong to Android notifications. A chime is requested only after a file was successfully saved and its library record was persisted. Failed saves, failed downloads and cancellations do not play a success chime. Denied notification permission, disabled/muted channels, silent mode, DND and notification volume can silence the automatic alert. There is no second foreground chime that bypasses those settings.

Android channel sounds are immutable, so enabled completion alerts use `zyrox_completed_sound_v110`; disabled completion sounds and failures use `zyrox_results_silent_v110`. The v1.0 results channel's mute/custom-sound choice is preserved on migration, and its old channel is retired. Task-specific notification tags plus `onlyAlertOnce` suppress duplicate result alerts while permitting a retry's successful completion to alert after a previous failure.

The explicit Settings preview respects the completion switch, ringer/DND/notification-volume settings and the completion channel's selected sound. It is an audition initiated by the user, independent of whether automatic notifications have permission. Playback never loops and stops when leaving Settings.

## Validation scope

PCM format, duration and peak levels were checked on the build host. Production compilation, lint and APK packaging checks are recorded with the release. Audible output, OEM notification behavior and audio interaction with other apps still require phone verification.
