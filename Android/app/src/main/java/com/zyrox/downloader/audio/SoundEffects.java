package com.zyrox.downloader.audio;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;

import com.zyrox.downloader.R;
import com.zyrox.downloader.core.Prefs;

/** Original UI sounds. Never raises volume, bypasses DND, or requests media audio focus. */
public final class SoundEffects {
    public static final String COMPLETED_CHANNEL_ID = "zyrox_completed_sound_v110";
    public static final String SILENT_RESULTS_CHANNEL_ID = "zyrox_results_silent_v110";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static SoundPool tapPool;
    private static int tapSample;
    private static boolean tapLoaded;
    private static long lastTapAt, pendingTapAt;
    private static MediaPlayer previewPlayer;

    private SoundEffects() { }

    /** Safe from Java or Kotlin callbacks. SoundPool is created only after a permitted user tap. */
    public static void tap(Context context) {
        if (context == null) return;
        Context app = application(context);
        if (!buttonAllowed(app)) return;
        MAIN.post(() -> {
            if (!buttonAllowed(app)) return;
            long now = SystemClock.elapsedRealtime();
            // Nested click callbacks must not produce two overlapping ticks for one gesture.
            if (lastTapAt != 0 && now - lastTapAt < 55) return;
            lastTapAt = now;
            if (tapLoaded && tapPool != null) {
                tapPool.play(tapSample, .7f, .7f, 1, 0, 1f);
                return;
            }
            pendingTapAt = now;
            if (tapPool != null) return;
            try {
                AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
                tapPool = new SoundPool.Builder().setMaxStreams(2).setAudioAttributes(attributes).build();
                tapPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
                    if (pool != tapPool || sampleId != tapSample) return;
                    tapLoaded = status == 0;
                    long age = SystemClock.elapsedRealtime() - pendingTapAt;
                    if (tapLoaded && pendingTapAt > 0 && age < 250 && buttonAllowed(app))
                        pool.play(tapSample, .7f, .7f, 1, 0, 1f);
                    pendingTapAt = 0;
                    if (!tapLoaded) { pool.release(); tapPool = null; tapSample = 0; }
                });
                tapSample = tapPool.load(app, R.raw.zyrox_tap, 1);
                if (tapSample == 0) { tapPool.release(); tapPool = null; }
            } catch (RuntimeException ignored) {
                if (tapPool != null) tapPool.release();
                tapPool = null;
                tapSample = 0;
                pendingTapAt = 0;
            }
        });
    }

    /** Resource-name URI remains valid when resource IDs change in a future app update. */
    public static Uri completionSoundUri(Context context) {
        return Uri.parse("android.resource://" + context.getPackageName() + "/raw/zyrox_complete");
    }

    public static AudioAttributes completionAudioAttributes() {
        return new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
    }

    /**
     * Explicit Settings action only. Actual background completion sound is owned by Android's
     * notification channel, so completing a download never also calls this preview player.
     */
    public static boolean previewCompletion(Context context) {
        if (context == null) return false;
        Context app = application(context);
        Uri sound;
        try {
            if (!Prefs.completionSoundsEnabled(app) || !systemAllows(app, AudioManager.STREAM_NOTIFICATION, false)) return false;
            NotificationManager notifications = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = notifications == null ? null : notifications.getNotificationChannel(COMPLETED_CHANNEL_ID);
            if (channel != null && (channel.getImportance() < NotificationManager.IMPORTANCE_DEFAULT || channel.getSound() == null)) return false;
            // Preview an explicit user-selected channel sound if Android settings changed it.
            sound = channel == null ? completionSoundUri(app) : channel.getSound();
        } catch (RuntimeException ignored) { return false; }
        final Uri selected = sound;
        MAIN.post(() -> {
            if (!Prefs.completionSoundsEnabled(app) || !systemAllows(app, AudioManager.STREAM_NOTIFICATION, false)) return;
            stopPreviewOnMain();
            final MediaPlayer player = new MediaPlayer();
            previewPlayer = player;
            try {
                player.setAudioAttributes(completionAudioAttributes());
                player.setDataSource(app, selected);
                player.setLooping(false);
                player.setOnPreparedListener(mp -> {
                    if (mp != previewPlayer) return;
                    if (!Prefs.completionSoundsEnabled(app) || !systemAllows(app, AudioManager.STREAM_NOTIFICATION, false)) {
                        stopPreviewOnMain(); return;
                    }
                    try { mp.start(); } catch (IllegalStateException ignored) { stopPreviewOnMain(); }
                });
                player.setOnCompletionListener(mp -> { if (mp == previewPlayer) stopPreviewOnMain(); });
                player.setOnErrorListener((mp, what, extra) -> {
                    if (mp == previewPlayer) stopPreviewOnMain();
                    return true;
                });
                player.prepareAsync();
                // A custom channel sound can be longer than the bundled chime. Preview it once,
                // briefly; never loop or retain a player indefinitely after leaving Settings.
                MAIN.postDelayed(() -> { if (previewPlayer == player) stopPreviewOnMain(); }, 4000);
            } catch (Exception ignored) { stopPreviewOnMain(); }
        });
        return true;
    }

    public static void stopPreview() { MAIN.post(SoundEffects::stopPreviewOnMain); }

    private static void stopPreviewOnMain() {
        MediaPlayer player = previewPlayer;
        previewPlayer = null;
        if (player != null) {
            player.setOnPreparedListener(null);
            player.setOnCompletionListener(null);
            player.setOnErrorListener(null);
            player.release();
        }
    }

    private static boolean buttonAllowed(Context context) {
        try { return Prefs.buttonSoundsEnabled(context) && systemAllows(context, AudioManager.STREAM_SYSTEM, true); }
        catch (RuntimeException ignored) { return false; }
    }

    private static boolean systemAllows(Context context, int stream, boolean touchSound) {
        try {
            AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            NotificationManager notifications = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (audio == null || notifications == null || audio.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) return false;
            if (notifications.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALL) return false;
            if (audio.getStreamVolume(stream) <= 0) return false;
            return !touchSound || Settings.System.getInt(context.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 1) != 0;
        } catch (RuntimeException ignored) { return false; }
    }

    private static Context application(Context context) {
        Context app = context.getApplicationContext();
        return app == null ? context : app;
    }
}
