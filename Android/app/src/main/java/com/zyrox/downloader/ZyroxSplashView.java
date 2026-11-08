package com.zyrox.downloader;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import com.zyrox.downloader.branding.ZyroxLogoDrawable;

/** Native startup: a suspended download mark, quiet orbital lines and a settling progress rail. */
public final class ZyroxSplashView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ZyroxLogoDrawable logo = new ZyroxLogoDrawable();
    private final ValueAnimator animator;
    private final Typeface wordmark = Typeface.create("sans-serif-black", Typeface.BOLD);
    private final Typeface medium = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    private float reveal;
    private final boolean reducedMotion;
    private RadialGradient halo;
    public ZyroxSplashView(Context context) {
        super(context);
        setContentDescription("ZYROX DOWNLOADER. Save it. Play it. Keep it.");
        setClickable(true);
        reducedMotion = !ValueAnimator.areAnimatorsEnabled();
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(900);
        animator.setInterpolator(new DecelerateInterpolator(1.6f));
        animator.addUpdateListener(value -> { reveal = (float)value.getAnimatedValue(); invalidate(); });
    }
    @Override protected void onAttachedToWindow() { super.onAttachedToWindow(); if (reducedMotion) { reveal=1; invalidate(); } else animator.start(); }
    @Override protected void onDetachedFromWindow() { animator.cancel(); super.onDetachedFromWindow(); }
    private float dp(float n) { return n * getResources().getDisplayMetrics().density; }
    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        halo = new RadialGradient(w/2f, h*.41f, dp(230), new int[]{0x5527458D,0x00080D1B}, null, Shader.TileMode.CLAMP);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas); canvas.drawColor(0xFF080D1B);
        float cx=getWidth()/2f, cy=getHeight()*.41f, progress=Math.max(.01f,reveal);
        paint.setShader(halo); paint.setAlpha(255); canvas.drawCircle(cx,cy,dp(230),paint); paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(dp(1));
        paint.setColor(0xFF213A65); canvas.drawCircle(cx,cy,dp(96),paint);
        paint.setColor(0xFF152745); canvas.drawCircle(cx,cy,dp(123),paint);
        paint.setStyle(Paint.Style.FILL); paint.setColor(0xFF67DFFF);
        canvas.drawCircle(cx+dp(92),cy-dp(27),dp(3),paint);
        paint.setColor(0xFF92B7FF); canvas.drawCircle(cx-dp(73),cy+dp(63),dp(2),paint);
        int save=canvas.save();
        canvas.translate(cx,cy+dp(16)*(1-progress)); canvas.scale(.82f+.18f*progress,.82f+.18f*progress);
        int half=(int)dp(58); logo.setBounds(-half,-half,half,half); logo.setAlpha((int)(255*progress)); logo.draw(canvas);
        canvas.restoreToCount(save);
        paint.setTextAlign(Paint.Align.CENTER); paint.setTypeface(wordmark); paint.setTextSize(dp(36));
        paint.setColor(0xFFF5F8FF); paint.setAlpha((int)(255*progress)); canvas.drawText("Z Y R O X",cx,cy+dp(176),paint);
        paint.setTypeface(medium); paint.setTextSize(dp(11)); paint.setColor(0xFF92B7FF);
        canvas.drawText("D O W N L O A D E R",cx,cy+dp(204),paint);
        paint.setTextSize(dp(13)); paint.setColor(0xFFA4B4D0); canvas.drawText("Save it. Play it. Keep it.",cx,cy+dp(244),paint);
        paint.setColor(0xFF1B2B49); canvas.drawRoundRect(cx-dp(30),cy+dp(276),cx+dp(30),cy+dp(279),dp(2),dp(2),paint);
        paint.setColor(0xFF67DFFF); canvas.drawRoundRect(cx-dp(30),cy+dp(276),cx-dp(30)+dp(60)*progress,cy+dp(279),dp(2),dp(2),paint);
    }
}
