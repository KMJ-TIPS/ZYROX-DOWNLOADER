package com.zyrox.downloader;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/** Lightweight native launch animation; no network, bitmap or runtime dependency. */
public final class ZyroxSplashView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ValueAnimator animator;
    private float reveal;
    private final boolean reducedMotion;

    public ZyroxSplashView(Context context) {
        super(context);
        setContentDescription("ZYROX DOWNLOADER. Save the moment.");
        setClickable(true);
        reducedMotion = !ValueAnimator.areAnimatorsEnabled();
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(900);
        animator.setInterpolator(new DecelerateInterpolator(1.6f));
        animator.addUpdateListener(value -> {
            reveal = (float) value.getAnimatedValue();
            invalidate();
        });
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (reducedMotion) { reveal = 1f; invalidate(); } else animator.start();
    }
    @Override protected void onDetachedFromWindow() { animator.cancel(); super.onDetachedFromWindow(); }
    private float dp(float n) { return n * getResources().getDisplayMetrics().density; }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.rgb(10,16,20));
        float cx = getWidth()/2f, cy = getHeight()*.43f;
        paint.setShader(new RadialGradient(cx, cy, dp(220), new int[]{0x23103F2A, 0x000A1014}, null, Shader.TileMode.CLAMP));
        canvas.drawCircle(cx, cy, dp(220), paint);
        paint.setShader(null);
        float progress = Math.max(.01f, reveal);
        int save = canvas.save();
        canvas.translate(cx, cy + dp(12)*(1-progress));
        canvas.scale(.84f+.16f*progress, .84f+.16f*progress);
        paint.setColor(0xFF294637); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(dp(1));
        canvas.drawCircle(0, 0, dp(81), paint);
        paint.setColor(0xFF183C2C); canvas.drawCircle(0, 0, dp(104), paint);
        paint.setStyle(Paint.Style.FILL); paint.setColor(0xFF17382C); paint.setAlpha((int)(255*progress));
        float tile=dp(100);
        canvas.drawRoundRect(-tile/2,-tile/2,tile/2,tile/2,dp(27),dp(27),paint);
        canvas.save(); canvas.translate(-tile/2,-tile/2); canvas.scale(tile/64f,tile/64f);
        Path z = new Path(); z.moveTo(16,17); z.lineTo(49,17); z.lineTo(49,25); z.lineTo(29,42); z.lineTo(48,42); z.lineTo(48,50); z.lineTo(14,50); z.lineTo(14,42); z.lineTo(35,25); z.lineTo(16,25); z.close();
        paint.setColor(0xFFB6F5C5); paint.setAlpha((int)(255*progress)); canvas.drawPath(z,paint);
        Path accent = new Path(); accent.moveTo(40,17); accent.lineTo(49,17); accent.lineTo(49,25); accent.lineTo(32,25); accent.close();
        paint.setColor(0xFFF4F4EB); paint.setAlpha((int)(255*progress)); canvas.drawPath(accent,paint);
        canvas.restore(); canvas.restoreToCount(save);
        paint.setTextAlign(Paint.Align.CENTER); paint.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));
        paint.setTextSize(dp(36)); paint.setColor(0xFFF4F4EB); paint.setAlpha((int)(255*progress));
        canvas.drawText("Z Y R O X",cx,cy+dp(158),paint);
        paint.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL)); paint.setTextSize(dp(11)); paint.setColor(0xFFB6F5C5);
        canvas.drawText("D O W N L O A D E R",cx,cy+dp(184),paint);
        paint.setTextSize(dp(13)); paint.setColor(0xFFA2AFB2);
        canvas.drawText("Save the moment.",cx,cy+dp(222),paint);
        paint.setColor(0xFF213B2F);
        canvas.drawRoundRect(cx-dp(24),cy+dp(251),cx+dp(24),cy+dp(254),dp(2),dp(2),paint);
        paint.setColor(0xFFB6F5C5);
        canvas.drawRoundRect(cx-dp(24),cy+dp(251),cx-dp(24)+dp(48)*progress,cy+dp(254),dp(2),dp(2),paint);
    }
}
