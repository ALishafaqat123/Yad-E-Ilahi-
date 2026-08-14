package com.rizwan.tasbeehcounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;

/** Centralizes safe resume/new-session choices so progress is never silently overwritten. */
public final class DhikrStartHelper {
    private DhikrStartHelper() {}

    public static void start(Activity activity, String mode, int target, int roundTarget,
                             String title, String phrase, String translation, String source,
                             boolean longText, int[] stageTargets, String[] sequencePhrases,
                             String[] sequenceTranslations) {
        LastSessionStore.Record existing = LastSessionStore.findIncomplete(activity, mode);
        if (existing == null) {
            launchNew(activity, mode, target, roundTarget, title, phrase, translation, source,
                    longText, stageTargets, sequencePhrases, sequenceTranslations);
            return;
        }

        boolean previousDay = !DhikrHistoryStore.today().equals(existing.sessionDate);
        String message = previousDay
                ? "گزشتہ دن کا “" + existing.title + "” "
                    + LastSessionStore.getProgress(activity, existing) + " پر ادھورا ہے۔"
                : "“" + existing.title + "” "
                    + LastSessionStore.getProgress(activity, existing) + " پر ادھورا ہے۔";

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("ادھورا ذکر موجود ہے")
                .setMessage(message)
                .setNegativeButton("وہیں سے جاری رکھیں", (d, which) ->
                        activity.startActivity(previousDay
                                ? LastSessionStore.carryForwardToToday(activity, existing)
                                : LastSessionStore.createIntent(activity, existing)))
                .setPositiveButton(previousDay ? "آج نیا ہدف شروع کریں" : "نیا ہدف شروع کریں",
                        (d, which) -> {
                            LastSessionStore.clearModeState(activity, mode);
                            launchNew(activity, mode, target, roundTarget, title, phrase,
                                    translation, source, longText, stageTargets,
                                    sequencePhrases, sequenceTranslations);
                        })
                .setNeutralButton(R.string.cancel, null)
                .create();
        dialog.setOnShowListener(ignored -> UrduFont.applyToDialog(dialog));
        dialog.show();
    }

    /**
     * Used by the full-screen unfinished-sessions page. Same-day items resume immediately.
     * Previous-day items always ask whether to carry the saved count into today or start a
     * fresh target. Carrying forward keeps yesterday's history snapshot unchanged.
     */
    public static void resumeExisting(Activity activity, LastSessionStore.Record existing) {
        if (existing == null) return;
        boolean previousDay = !DhikrHistoryStore.today().equals(existing.sessionDate);
        if (!previousDay) {
            activity.startActivity(LastSessionStore.createIntent(activity, existing));
            return;
        }

        String message = "گزشتہ دن کا “" + existing.title + "” "
                + LastSessionStore.getProgress(activity, existing) + " پر ادھورا ہے۔";

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("گزشتہ دن کا ادھورا ذکر")
                .setMessage(message)
                .setNegativeButton("وہیں سے جاری رکھیں", (d, which) ->
                        activity.startActivity(LastSessionStore.carryForwardToToday(activity, existing)))
                .setPositiveButton("آج نیا ہدف شروع کریں", (d, which) ->
                        activity.startActivity(LastSessionStore.startFreshToday(activity, existing)))
                .setNeutralButton(R.string.cancel, null)
                .create();
        dialog.setOnShowListener(ignored -> UrduFont.applyToDialog(dialog));
        dialog.show();
    }

    private static void launchNew(Activity activity, String mode, int target, int roundTarget,
                                  String title, String phrase, String translation, String source,
                                  boolean longText, int[] stageTargets,
                                  String[] sequencePhrases, String[] sequenceTranslations) {
        Intent intent = new Intent(activity, TasbeehActivity.class);
        intent.putExtra(MainActivity.EXTRA_MODE, mode);
        intent.putExtra(MainActivity.EXTRA_SESSION_DATE, DhikrHistoryStore.today());
        intent.putExtra(MainActivity.EXTRA_TARGET, target);
        intent.putExtra(MainActivity.EXTRA_SEQUENCE_ROUND_TARGET, roundTarget);
        intent.putExtra(MainActivity.EXTRA_TITLE, title);
        intent.putExtra(MainActivity.EXTRA_PHRASE, phrase);
        intent.putExtra(MainActivity.EXTRA_TRANSLATION, translation);
        intent.putExtra(MainActivity.EXTRA_SOURCE_NOTE, source);
        intent.putExtra(MainActivity.EXTRA_LONG_TEXT, longText);
        if (stageTargets != null && stageTargets.length > 0) {
            intent.putExtra(MainActivity.EXTRA_STAGE_TARGETS, stageTargets);
            intent.putExtra(MainActivity.EXTRA_SEQUENCE_PHRASES, sequencePhrases);
            intent.putExtra(MainActivity.EXTRA_SEQUENCE_TRANSLATIONS, sequenceTranslations);
        }
        activity.startActivity(intent);
    }
}
