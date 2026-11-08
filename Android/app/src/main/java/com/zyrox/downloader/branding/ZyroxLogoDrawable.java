package com.zyrox.downloader.branding;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/** Original ZYROX download mark, shared by Compose and the native launch screen. */
public final class ZyroxLogoDrawable extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF tile = new RectF(1, 1, 63, 63);
    private final Path arrow = new Path();
    private final Path tray = new Path();
    private final LinearGradient gradient = new LinearGradient(5, 3, 58, 64,
        new int[]{0xFF3B7BFF, 0xFF1748A5, 0xFF102758}, null, Shader.TileMode.CLAMP);
    private int opacity = 255;
    public ZyroxLogoDrawable() {
        arrow.moveTo(28, 13); arrow.quadTo(28, 11, 30, 11); arrow.lineTo(34, 11);
        arrow.quadTo(36, 11, 36, 13); arrow.lineTo(36, 29); arrow.lineTo(44, 29);
        arrow.quadTo(47, 29, 45, 31); arrow.lineTo(33.5f, 42.5f);
        arrow.quadTo(32, 44, 30.5f, 42.5f); arrow.lineTo(19, 31);
        arrow.quadTo(17, 29, 20, 29); arrow.lineTo(28, 29); arrow.close();
        tray.moveTo(16, 45); tray.lineTo(16, 48); tray.quadTo(16, 54, 22, 54);
        tray.lineTo(42, 54); tray.quadTo(48, 54, 48, 48); tray.lineTo(48, 45);
    }
    private void colour(int colour, float strength) {
        paint.setColor(colour); paint.setAlpha(Math.round(opacity * strength));
    }
    @Override public void draw(Canvas canvas) {
        int saved = canvas.save();
        canvas.translate(getBounds().left, getBounds().top);
        canvas.scale(getBounds().width()/64f, getBounds().height()/64f);
        paint.setStyle(Paint.Style.FILL); paint.setShader(gradient); paint.setAlpha(opacity);
        canvas.drawRoundRect(tile, 18, 18, paint); paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(1);
        colour(0xFFD9EBFF, .25f); canvas.drawRoundRect(tile, 18, 18, paint);
        colour(0xFFB8D7FF, .12f); canvas.drawCircle(32, 31, 24, paint);
        paint.setStyle(Paint.Style.FILL); colour(0xFFF5F8FF, 1); canvas.drawPath(arrow, paint);
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(4.2f);
        paint.setStrokeCap(Paint.Cap.ROUND); paint.setStrokeJoin(Paint.Join.ROUND);
        colour(0xFF67DFFF, 1); canvas.drawPath(tray, paint);
        paint.setStyle(Paint.Style.FILL); canvas.restoreToCount(saved);
    }
    @Override public void setAlpha(int alpha) { opacity = Math.max(0, Math.min(255, alpha)); invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
