package com.rizwan.tasbeehcounter;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;

public final class CompletionSoundHelper {
    private CompletionSoundHelper() { }

    public static void stageComplete() {
        play(false);
    }

    public static void fullComplete() {
        play(true);
    }

    private static void play(boolean doubleTone) {
        final ToneGenerator tone;
        try {
            tone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 58);
        } catch (RuntimeException ignored) {
            return;
        }

        try {
            tone.startTone(ToneGenerator.TONE_PROP_ACK, 180);
            Handler handler = new Handler(Looper.getMainLooper());
            if (doubleTone) {
                handler.postDelayed(() -> {
                    try {
                        tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 230);
                    } catch (RuntimeException ignored) { }
                }, 260L);
                handler.postDelayed(tone::release, 650L);
            } else {
                handler.postDelayed(tone::release, 350L);
            }
        } catch (RuntimeException ignored) {
            tone.release();
        }
    }
}
