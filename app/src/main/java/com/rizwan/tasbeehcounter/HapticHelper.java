package com.rizwan.tasbeehcounter;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;

/** Haptic helper tuned for Android 8.1 / Samsung devices. */
public final class HapticHelper {
    private HapticHelper() { }

    public static boolean hasVibrator(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        return vibrator != null && vibrator.hasVibrator();
    }

    public static boolean tap(Context context, View source) {
        boolean viewHaptic = performViewHaptic(source, HapticFeedbackConstants.VIRTUAL_KEY);
        boolean direct = vibrateOneShot(context, 95L, 255);
        return viewHaptic || direct;
    }

    public static boolean confirmation(Context context, View source) {
        boolean viewHaptic = performViewHaptic(source, HapticFeedbackConstants.LONG_PRESS);
        boolean direct = vibrateOneShot(context, 180L, 255);
        return viewHaptic || direct;
    }

    public static boolean stageComplete(Context context, View source) {
        boolean viewHaptic = performViewHaptic(source, HapticFeedbackConstants.LONG_PRESS);
        boolean direct = vibrateWaveform(context,
                new long[]{0L, 140L, 90L, 190L},
                new int[]{0, 230, 0, 255});
        return viewHaptic || direct;
    }

    public static boolean fullComplete(Context context, View source) {
        boolean viewHaptic = performViewHaptic(source, HapticFeedbackConstants.LONG_PRESS);
        boolean direct = vibrateWaveform(context,
                new long[]{0L, 190L, 100L, 260L, 110L, 360L},
                new int[]{0, 255, 0, 255, 0, 255});
        return viewHaptic || direct;
    }

    public static boolean test(Context context, View source) {
        boolean viewHaptic = performViewHaptic(source, HapticFeedbackConstants.LONG_PRESS);
        boolean direct = vibrateWaveform(context,
                new long[]{0L, 260L, 140L, 420L},
                new int[]{0, 255, 0, 255});
        return viewHaptic || direct;
    }

    private static boolean performViewHaptic(View source, int feedbackConstant) {
        if (source == null) return false;
        source.setHapticFeedbackEnabled(true);
        try {
            return source.performHapticFeedback(feedbackConstant,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                            | HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    private static boolean vibrateOneShot(Context context, long durationMs, int amplitude) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) return false;
        try {
            vibrator.cancel();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude));
            } else {
                vibrator.vibrate(durationMs);
            }
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    private static boolean vibrateWaveform(Context context, long[] timings, int[] amplitudes) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) return false;
        try {
            vibrator.cancel();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
            } else {
                vibrator.vibrate(timings, -1);
            }
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
