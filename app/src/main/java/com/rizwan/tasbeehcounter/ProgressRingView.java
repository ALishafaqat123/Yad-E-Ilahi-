package com.rizwan.tasbeehcounter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ProgressRingView extends View {
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();
    private float progress = 0f;
    private float strokeWidth;

    public ProgressRingView(Context context) {
        super(context);
        init();
    }

    public ProgressRingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressRingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        strokeWidth = dp(10);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidth);
        trackPaint.setColor(0xFF252525);

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(0xFFFFD166);
    }

    public void setProgress(float value) {
        progress = Math.max(0f, Math.min(1f, value));
        invalidate();
    }

    public void setDimmed(boolean dimmed) {
        trackPaint.setColor(dimmed ? 0xFF171717 : 0xFF252525);
        progressPaint.setColor(dimmed ? 0xFF6B5528 : 0xFFFFD166);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float inset = strokeWidth / 2f + dp(3);
        arcBounds.set(inset, inset, getWidth() - inset, getHeight() - inset);
        canvas.drawOval(arcBounds, trackPaint);
        if (progress > 0f) {
            canvas.drawArc(arcBounds, -90f, progress * 360f, false, progressPaint);
        }
    }

    private float dp(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
