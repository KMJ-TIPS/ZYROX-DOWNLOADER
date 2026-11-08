package com.zyrox.downloader;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import com.zyrox.downloader.audio.SoundEffects;
import java.util.Locale;

/** Local-only media playback. Audio focus and player resources follow the visible Activity. */
public final class PlayerActivity extends Activity {
    private static final int BG = 0xFF080D1B, ACCENT = 0xFF92B7FF, WHITE = 0xFFF5F8FF, DIM = 0xFFA4B4D0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private FocusVideoView video;
    private MediaPlayer audio;
    private SeekBar seek;
    private TextView play, clock, fallback;
    private AudioManager audioManager;
    private AudioFocusRequest focusRequest;
    private MediaController controller;
    private Uri mediaUri;
    private String mime;
    private boolean prepared, seeking, foreground, completed, hasAudioFocus, focusInterrupted;
    private boolean playWhenReady = true;
    private int restoredPosition;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!foreground) return;
            if (audio != null && prepared && !seeking) {
                try {
                    int position = audio.getCurrentPosition();
                    seek.setProgress(position);
                    clock.setText(time(position) + "  /  " + time(audio.getDuration()));
                } catch (IllegalStateException ignored) { }
            }
            handler.postDelayed(this, 500);
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightNavigationBars(false);
        restoredPosition = state == null ? 0 : state.getInt("position", 0);
        playWhenReady = state == null || state.getBoolean("playWhenReady", true);
        completed = state != null && state.getBoolean("completed", false);
        mediaUri = getIntent().getData();
        if (mediaUri == null || !("content".equals(mediaUri.getScheme()) || "file".equals(mediaUri.getScheme()))) {
            finish();
            return;
        }
        mime = getIntent().getType();
        if (mime == null) {
            try { mime = getContentResolver().getType(mediaUri); } catch (RuntimeException ignored) { }
        }
        if (mime == null) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(mediaUri.toString()).toLowerCase(Locale.ROOT);
            mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        if (mime == null) mime = "video/*";
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        AudioAttributes attrs = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
        focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setAudioAttributes(attrs)
            .setWillPauseWhenDucked(true).setOnAudioFocusChangeListener(this::onAudioFocusChanged, handler).build();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(22), dp(16), dp(22), dp(24));
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets safe = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(dp(22) + safe.left, dp(16) + safe.top, dp(22) + safe.right, dp(24) + safe.bottom);
            return insets;
        });
        setContentView(root);
        ViewCompat.requestApplyInsets(root);
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = action("‹", 34, WHITE);
        back.setContentDescription("Back to library");
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> { SoundEffects.tap(this); finish(); });
        bar.addView(back, new LinearLayout.LayoutParams(dp(48), dp(48)));
        TextView brand = text("Z Y R O X", 15, ACCENT);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        bar.addView(brand, new LinearLayout.LayoutParams(0, dp(48), 1));
        brand.setGravity(Gravity.CENTER_VERTICAL);
        TextView share = action("Share", 14, ACCENT);
        share.setContentDescription("Share this media file");
        share.setGravity(Gravity.CENTER);
        share.setOnClickListener(v -> { SoundEffects.tap(this); shareMedia(); });
        bar.addView(share, new LinearLayout.LayoutParams(dp(64), dp(48)));
        root.addView(bar);
        String title = getIntent().getStringExtra("title");
        if (title == null || title.trim().isEmpty()) title = "Your media";
        TextView titleView = text(title, 24, WHITE);
        titleView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titleView.setMaxLines(3);
        titleView.setPadding(0, dp(22), 0, dp(8));
        ViewCompat.setAccessibilityHeading(titleView, true);
        root.addView(titleView);
        boolean isAudio = mime.startsWith("audio/");
        TextView sub = text(isAudio ? "OFFLINE AUDIO  ·  NO ADS" : "OFFLINE VIDEO  ·  NO ADS", 10, DIM);
        sub.setLetterSpacing(.12f);
        root.addView(sub);
        fallback = action("Open in another player", 14, ACCENT);
        fallback.setGravity(Gravity.CENTER);
        fallback.setVisibility(View.GONE);
        fallback.setOnClickListener(v -> { SoundEffects.tap(this); openExternally(); });
        if (isAudio) setupAudio(root, attrs); else setupVideo(root);
        root.addView(fallback, new LinearLayout.LayoutParams(-1, dp(52)));
    }

    /** Also routes VideoView media-key shortcuts through the same focus owner. */
    private final class FocusVideoView extends VideoView {
        FocusVideoView() { super(PlayerActivity.this); }
        @Override public void start() { requestPlayback(); }
        @Override public void pause() { pauseByUser(); }
        void startDirect() { super.start(); }
        void pauseDirect() { super.pause(); }
    }

    private void setupVideo(LinearLayout root) {
        FrameLayout frame = new FrameLayout(this);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, 0, 1);
        fp.topMargin = dp(24);
        root.addView(frame, fp);
        video = new FocusVideoView();
        // One focus owner: the Activity also handles lifecycle and controller actions.
        video.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE);
        video.setContentDescription("Saved video. Tap for playback controls.");
        frame.addView(video, new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER));
        controller = new MediaController(this);
        controller.setAnchorView(video);
        video.setMediaController(controller);
        video.setOnPreparedListener(mp -> {
            prepared = true;
            if (restoredPosition > 0) video.seekTo(restoredPosition);
            controller.setMediaPlayer(new MediaController.MediaPlayerControl() {
                @Override public void start() { requestPlayback(); }
                @Override public void pause() { pauseByUser(); }
                @Override public int getDuration() { return video.getDuration(); }
                @Override public int getCurrentPosition() { return video.getCurrentPosition(); }
                @Override public void seekTo(int position) { completed = false; video.seekTo(position); }
                @Override public boolean isPlaying() { return video.isPlaying(); }
                @Override public int getBufferPercentage() { return video.getBufferPercentage(); }
                @Override public boolean canPause() { return true; }
                @Override public boolean canSeekBackward() { return true; }
                @Override public boolean canSeekForward() { return true; }
                @Override public int getAudioSessionId() { return video.getAudioSessionId(); }
            });
            if (foreground && playWhenReady) startPlayback();
        });
        video.setOnCompletionListener(mp -> {
            onCompleted();
            if (foreground) controller.show(0);
        });
        video.setOnErrorListener((mp, what, extra) -> {
            prepared = false;
            playWhenReady = false;
            clearScreenFlag();
            abandonFocus();
            fallback.setVisibility(View.VISIBLE);
            Toast.makeText(this, "This codec is not supported on your phone. Try another player.", Toast.LENGTH_LONG).show();
            return true;
        });
        try { video.setVideoURI(mediaUri); }
        catch (RuntimeException e) {
            fallback.setVisibility(View.VISIBLE);
            Toast.makeText(this, "This video could not be opened.", Toast.LENGTH_LONG).show();
        }
    }

    private void setupAudio(LinearLayout root, AudioAttributes attrs) {
        FrameLayout art = new FrameLayout(this);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, 0, 1);
        ap.topMargin = dp(24); ap.bottomMargin = dp(24);
        root.addView(art, ap);
        TextView note = text("♫", 100, ACCENT);
        note.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        note.setGravity(Gravity.CENTER);
        GradientDrawable tile = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{0xFF1748A5, 0xFF111C32});
        tile.setCornerRadius(dp(32));
        note.setBackground(tile);
        art.addView(note, new FrameLayout.LayoutParams(dp(236), dp(236), Gravity.CENTER));
        art.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int size = Math.min(dp(236), Math.max(0, Math.min(right - left, bottom - top)));
            if (note.getLayoutParams().width != size) {
                note.setLayoutParams(new FrameLayout.LayoutParams(size, size, Gravity.CENTER));
                note.setTextSize(Math.min(100, size / getResources().getDisplayMetrics().scaledDensity * .45f));
            }
        });
        clock = text("Loading audio…", 13, DIM);
        clock.setGravity(Gravity.CENTER);
        root.addView(clock, new LinearLayout.LayoutParams(-1, dp(32)));
        seek = new SeekBar(this);
        seek.setContentDescription("Audio position");
        seek.setEnabled(false);
        seek.setProgressTintList(android.content.res.ColorStateList.valueOf(ACCENT));
        seek.setThumbTintList(android.content.res.ColorStateList.valueOf(ACCENT));
        root.addView(seek, new LinearLayout.LayoutParams(-1, dp(48)));
        play = action("Loading…", 18, BG);
        play.setGravity(Gravity.CENTER);
        play.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        GradientDrawable button = new GradientDrawable();
        button.setColor(ACCENT); button.setCornerRadius(dp(18));
        play.setBackground(button); play.setEnabled(false);
        root.addView(play, new LinearLayout.LayoutParams(-1, dp(60)));
        play.setOnClickListener(v -> { if (!prepared) return; SoundEffects.tap(this); if (isPlaying()) pauseByUser(); else requestPlayback(); });
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int position, boolean user) {
                if (user && audio != null && prepared) {
                    try { clock.setText(time(position) + "  /  " + time(audio.getDuration())); }
                    catch (IllegalStateException ignored) { }
                }
            }
            @Override public void onStartTrackingTouch(SeekBar s) { seeking = true; }
            @Override public void onStopTrackingTouch(SeekBar s) {
                if (audio != null && prepared) {
                    try { audio.seekTo(s.getProgress()); completed = false; updatePlayLabel(); }
                    catch (IllegalStateException ignored) { }
                }
                seeking = false;
            }
        });
        audio = new MediaPlayer();
        audio.setAudioAttributes(attrs);
        try {
            audio.setDataSource(this, mediaUri);
            audio.setOnPreparedListener(mp -> {
                prepared = true;
                seek.setEnabled(true); seek.setMax(mp.getDuration()); play.setEnabled(true);
                if (restoredPosition > 0) mp.seekTo(restoredPosition);
                updatePlayLabel();
                if (foreground) {
                    handler.removeCallbacks(tick); handler.post(tick);
                    if (playWhenReady) startPlayback();
                }
            });
            audio.setOnCompletionListener(mp -> onCompleted());
            audio.setOnErrorListener((mp, what, extra) -> { failAudio(); return true; });
            audio.prepareAsync();
        } catch (Exception e) { failAudio(); }
    }

    private void onAudioFocusChanged(int change) {
        if (change == AudioManager.AUDIOFOCUS_GAIN) {
            if (!foreground || !playWhenReady) { abandonFocus(); return; }
            hasAudioFocus = true;
            if (focusInterrupted && foreground && prepared && playWhenReady) startPlayback();
            focusInterrupted = false;
        } else if (change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            focusInterrupted = focusInterrupted || isPlaying();
            hasAudioFocus = false;
            pauseInternal();
        } else if (change == AudioManager.AUDIOFOCUS_LOSS) {
            playWhenReady = false;
            focusInterrupted = false;
            hasAudioFocus = false;
            pauseInternal();
            abandonFocus();
        }
    }

    private void requestPlayback() {
        playWhenReady = true;
        focusInterrupted = false;
        startPlayback();
    }

    private void startPlayback() {
        if (!foreground || !prepared) return;
        if (!hasAudioFocus) {
            hasAudioFocus = audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
            if (!hasAudioFocus) {
                playWhenReady = false;
                updatePlayLabel();
                Toast.makeText(this, "Another app is using audio. Try again.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        try {
            if (completed) {
                if (audio != null) audio.seekTo(0); else if (video != null) video.seekTo(0);
                completed = false;
            }
            if (audio != null) audio.start(); else if (video != null) video.startDirect();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            updatePlayLabel();
        } catch (IllegalStateException e) {
            if (audio != null) failAudio();
            else { prepared = false; clearScreenFlag(); abandonFocus(); fallback.setVisibility(View.VISIBLE); }
        }
    }

    private void pauseByUser() {
        playWhenReady = false;
        focusInterrupted = false;
        pauseInternal();
        abandonFocus();
    }

    private void pauseInternal() {
        try {
            if (audio != null && prepared && audio.isPlaying()) audio.pause();
            if (video != null && video.isPlaying()) video.pauseDirect();
        } catch (IllegalStateException ignored) { }
        clearScreenFlag();
        updatePlayLabel();
    }

    private void onCompleted() {
        completed = true;
        playWhenReady = false;
        clearScreenFlag();
        abandonFocus();
        updatePlayLabel();
    }

    private void failAudio() {
        prepared = false;
        playWhenReady = false;
        clearScreenFlag();
        abandonFocus();
        if (clock != null) clock.setText("This audio format could not be played.");
        if (seek != null) seek.setEnabled(false);
        if (play != null) { play.setText("Unavailable"); play.setEnabled(false); }
        fallback.setVisibility(View.VISIBLE);
    }

    private boolean isPlaying() {
        try { return prepared && (audio != null ? audio.isPlaying() : video != null && video.isPlaying()); }
        catch (IllegalStateException ignored) { return false; }
    }

    private void updatePlayLabel() {
        if (play == null || !prepared) return;
        boolean playing = isPlaying();
        play.setText(playing ? "Ⅱ  Pause" : completed ? "↻  Replay" : "▶  Play");
        play.setContentDescription(playing ? "Pause audio" : completed ? "Replay audio" : "Play audio");
    }

    private void clearScreenFlag() { getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); }

    private void abandonFocus() {
        if (audioManager != null && focusRequest != null) audioManager.abandonAudioFocusRequest(focusRequest);
        hasAudioFocus = false;
    }

    private Uri shareableUri() {
        if ("file".equals(mediaUri.getScheme()))
            return FileProvider.getUriForFile(this, getPackageName() + ".provider", new File(mediaUri.getPath()));
        return mediaUri;
    }

    private void shareMedia() {
        try {
            Uri uri = shareableUri();
            Intent send = new Intent(Intent.ACTION_SEND).setType(mime).putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            send.setClipData(ClipData.newRawUri("ZYROX media", uri));
            startActivity(Intent.createChooser(send, "Share media"));
        } catch (RuntimeException e) {
            Toast.makeText(this, "This file could not be shared.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openExternally() {
        try {
            Intent open = new Intent(Intent.ACTION_VIEW).setDataAndType(shareableUri(), mime)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(open, "Open media"));
        } catch (RuntimeException e) {
            Toast.makeText(this, "Install a player that supports this file format.", Toast.LENGTH_LONG).show();
        }
    }

    private String time(int ms) { int seconds = Math.max(ms, 0) / 1000; return String.format(Locale.US, "%d:%02d", seconds / 60, seconds % 60); }
    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value); view.setTextColor(color); view.setTextSize(size); view.setFontFeatureSettings("kern");
        return view;
    }
    private TextView action(String value, int size, int color) {
        TextView view = new TextView(this) {
            @Override public CharSequence getAccessibilityClassName() { return "android.widget.Button"; }
        };
        view.setText(value); view.setTextColor(color); view.setTextSize(size); view.setFocusable(true); view.setClickable(true); view.setSoundEffectsEnabled(false);
        return view;
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override protected void onResume() {
        super.onResume();
        foreground = true;
        if (prepared && playWhenReady) startPlayback();
        if (audio != null && prepared) { handler.removeCallbacks(tick); handler.post(tick); }
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        int position = restoredPosition;
        try { if (audio != null && prepared) position = audio.getCurrentPosition(); else if (video != null) position = video.getCurrentPosition(); }
        catch (IllegalStateException ignored) { }
        state.putInt("position", position);
        state.putBoolean("playWhenReady", playWhenReady);
        state.putBoolean("completed", completed);
        super.onSaveInstanceState(state);
    }
    @Override protected void onPause() {
        foreground = false;
        handler.removeCallbacks(tick);
        pauseInternal();
        focusInterrupted = false;
        abandonFocus();
        super.onPause();
    }
    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        abandonFocus();
        if (controller != null) controller.hide();
        if (audio != null) {
            audio.setOnPreparedListener(null); audio.setOnCompletionListener(null); audio.setOnErrorListener(null);
            audio.release(); audio = null;
        }
        if (video != null) video.stopPlayback();
        prepared = false;
        super.onDestroy();
    }
}
