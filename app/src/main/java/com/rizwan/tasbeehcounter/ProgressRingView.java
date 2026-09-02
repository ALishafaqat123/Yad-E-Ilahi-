package com.rizwan.tasbeehcounter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * تسبیح کے دانوں کا حلقہ۔
 *
 * پہلے یہ ایک مسلسل arc کھینچتا تھا۔ اب یہ الگ الگ دانے کھینچتا ہے، بالکل اصل تسبیح کی طرح،
 * تاکہ صارف عدد پڑھے بغیر جان لے کہ کتنا رہ گیا ہے۔
 *
 * دانوں کی تعداد کا اصول:
 *   ہدف 100 یا کم  → ہدف کے برابر دانے، ایک ٹیپ = ایک دانہ
 *   ہدف 100 سے زیادہ → 100 دانے، ہر دانہ (ہدف ÷ 100) ٹیپ پر بھرتا ہے
 *   آزاد گنتی        → 33 دانے، جو بار بار بھرتے اور خالی ہوتے رہتے ہیں
 *
 * کوئی بھی بیرونی لائبریری استعمال نہیں ہوتی — صرف android.graphics.Canvas۔
 */
public class ProgressRingView extends View {

    private static final int FREE_BEADS = 33;
    private static final int MAX_BEADS = 100;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint beadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint currentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int beadCount = FREE_BEADS;
    private int filledBeads = 0;
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

        trackPaint.setStyle(Paint.Style.FILL);
        trackPaint.setColor(0xFF252525);

        beadPaint.setStyle(Paint.Style.FILL);
        beadPaint.setColor(0xFFFFD166);

        currentPaint.setStyle(Paint.Style.FILL);
        currentPaint.setColor(0xFFFFF1CF);
    }

    /**
     * گنتی اور ہدف دونوں دے دیں، حلقہ خود طے کر لے گا کہ کتنے دانے بنانے ہیں اور کتنے بھرنے ہیں۔
     * آزاد گنتی کے لیے target میں 0 بھیجیں۔
     */
    public void setCounter(int count, int target) {
        if (count < 0) count = 0;

        if (target <= 0) {
            beadCount = FREE_BEADS;
            filledBeads = count % FREE_BEADS;
            // پورا چکر مکمل ہو تو خالی حلقے کے بجائے بھرا ہوا حلقہ دکھائیں
            if (count > 0 && filledBeads == 0) filledBeads = FREE_BEADS;
        } else if (target <= MAX_BEADS) {
            beadCount = target;
            filledBeads = Math.min(target, count);
        } else {
            beadCount = MAX_BEADS;
            double perBead = target / (double) MAX_BEADS;
            filledBeads = (int) Math.min(MAX_BEADS, Math.floor(count / perBead));
        }
        invalidate();
    }

    /** پرانی جگہوں کے لیے برقرار — 0 سے 1 کے درمیان حصہ۔ */
    public void setProgress(float value) {
        float fraction = Math.max(0f, Math.min(1f, value));
        filledBeads = Math.round(fraction * beadCount);
        invalidate();
    }

    public void setDimmed(boolean dimmed) {
        trackPaint.setColor(dimmed ? 0xFF171717 : 0xFF252525);
        beadPaint.setColor(dimmed ? 0xFF6B5528 : 0xFFFFD166);
        currentPaint.setColor(dimmed ? 0xFF8A6E33 : 0xFFFFF1CF);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int total = Math.max(1, beadCount);
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float margin = strokeWidth / 2f + dp(3);
        float radius = Math.min(centerX, centerY) - margin;
        if (radius <= 0f) return;

        // دانے کا حجم فاصلے سے نکالیں: 33 دانے موٹے لگیں، 100 دانے بھی الگ الگ نظر آئیں
        float spacing = (float) (2 * Math.PI * radius / total);
        float beadRadius = Math.max(dp(1.4f), Math.min(dp(5.5f), spacing * 0.34f));

        for (int i = 0; i < total; i++) {
            double angle = -Math.PI / 2 + (2 * Math.PI * i) / total;
            float x = centerX + (float) (radius * Math.cos(angle));
            float y = centerY + (float) (radius * Math.sin(angle));

            if (i == filledBeads - 1) {
                // موجودہ دانہ — ذرا بڑا اور روشن، نظر فوراً یہیں جاتی ہے
                canvas.drawCircle(x, y, beadRadius * 1.5f, currentPaint);
            } else {
                canvas.drawCircle(x, y, beadRadius, i < filledBeads ? beadPaint : trackPaint);
            }
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
