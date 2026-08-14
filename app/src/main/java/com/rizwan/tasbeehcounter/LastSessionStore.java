package com.rizwan.tasbeehcounter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Stores one active unfinished session per dhikr without overwriting other adhkar. */
public final class LastSessionStore {
    private static final String IDS_KEY = "resume_session_ids_v3";
    private static final String PREFIX = "resume_session_v3_";
    private static final String SEP = "\u001F";
    private static final String MIGRATED_KEY = "resume_sessions_v3_migrated";

    private static final String OLD_IDS_KEY = "resume_session_ids_v2";
    private static final String OLD_PREFIX = "resume_session_v2_";
    private static final String LEGACY_PREFIX = "last_session_";

    private LastSessionStore() {}

    public static final class Record {
        public final String mode;
        public final String sessionDate;
        public final int target;
        public final int roundTarget;
        public final String title;
        public final String phrase;
        public final String translation;
        public final String source;
        public final boolean longText;
        public final int[] stageTargets;
        public final String[] sequencePhrases;
        public final String[] sequenceTranslations;
        public final long updatedAt;

        private Record(String mode, String sessionDate, int target, int roundTarget,
                       String title, String phrase, String translation, String source,
                       boolean longText, int[] stageTargets, String[] sequencePhrases,
                       String[] sequenceTranslations, long updatedAt) {
            this.mode = mode;
            this.sessionDate = sessionDate;
            this.target = target;
            this.roundTarget = roundTarget;
            this.title = title;
            this.phrase = phrase;
            this.translation = translation;
            this.source = source;
            this.longText = longText;
            this.stageTargets = stageTargets == null ? new int[0] : stageTargets;
            this.sequencePhrases = sequencePhrases == null ? new String[0] : sequencePhrases;
            this.sequenceTranslations = sequenceTranslations == null
                    ? new String[0] : sequenceTranslations;
            this.updatedAt = updatedAt;
        }

        public boolean isMultiStage() {
            return stageTargets.length > 0
                    && stageTargets.length == sequencePhrases.length
                    && stageTargets.length == sequenceTranslations.length;
        }

        public boolean isRoundSequence() {
            return roundTarget > 0 && isMultiStage();
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void save(Context context, String mode, String sessionDate,
                            int target, int roundTarget, int count, int stageIndex,
                            int completedRounds, String title, String phrase,
                            String translation, String source, boolean longText,
                            int[] stageTargets, String[] sequencePhrases,
                            String[] sequenceTranslations) {
        if (mode == null || mode.trim().isEmpty()) return;
        if (sessionDate == null || sessionDate.isEmpty()) sessionDate = DhikrHistoryStore.today();

        if (count <= 0 && stageIndex <= 0 && completedRounds <= 0) {
            clear(context, mode);
            return;
        }

        SharedPreferences p = prefs(context);
        String base = base(mode);
        List<String> ids = getStoredIds(p);
        ids.remove(mode);
        ids.add(0, mode);

        p.edit()
                .putString(IDS_KEY, joinIds(ids))
                .putString(base + "mode", mode)
                .putString(base + "date", sessionDate)
                .putInt(base + "target", target)
                .putInt(base + "round_target", roundTarget)
                .putString(base + "title", safe(title))
                .putString(base + "phrase", safe(phrase))
                .putString(base + "translation", safe(translation))
                .putString(base + "source", safe(source))
                .putBoolean(base + "long", longText)
                .putString(base + "stage_targets", join(stageTargets))
                .putString(base + "sequence_phrases", join(sequencePhrases))
                .putString(base + "sequence_translations", join(sequenceTranslations))
                .putLong(base + "updated_at", System.currentTimeMillis())
                .apply();

        DhikrHistoryStore.update(context, sessionDate, mode, title,
                DhikrHistoryStore.STATUS_INCOMPLETE, count,
                roundTarget > 0 ? 1 : target, stageIndex,
                stageTargets == null ? 0 : stageTargets.length,
                completedRounds, roundTarget);
    }

    public static List<Record> getIncompleteSessions(Context context) {
        migrateIfNeeded(context);
        SharedPreferences p = prefs(context);
        List<String> ids = getStoredIds(p);
        List<Record> result = new ArrayList<>();
        List<String> stale = new ArrayList<>();
        for (String mode : ids) {
            Record record = readRecord(p, mode);
            if (record == null || !isIncomplete(p, record)) {
                stale.add(mode);
            } else {
                result.add(record);
            }
        }
        if (!stale.isEmpty()) {
            ids.removeAll(stale);
            SharedPreferences.Editor editor = p.edit().putString(IDS_KEY, joinIds(ids));
            for (String mode : stale) removeRecord(editor, mode);
            editor.apply();
        }
        return result;
    }

    public static Record findIncomplete(Context context, String mode) {
        if (mode == null) return null;
        for (Record record : getIncompleteSessions(context)) {
            if (mode.equals(record.mode)) return record;
        }
        return null;
    }

    public static boolean has(Context context) {
        return !getIncompleteSessions(context).isEmpty();
    }

    public static String getTitle(Context context) {
        List<Record> records = getIncompleteSessions(context);
        return records.isEmpty() ? "پچھلا ذکر" : records.get(0).title;
    }

    public static String getProgress(Context context) {
        List<Record> records = getIncompleteSessions(context);
        return records.isEmpty() ? "" : getProgress(context, records.get(0));
    }

    public static String getProgress(Context context, Record record) {
        SharedPreferences p = prefs(context);
        int count = p.getInt(countKey(record.mode), 0);
        int stage = p.getInt(stageKey(record.mode), 0);
        int rounds = p.getInt(roundKey(record.mode), 0);
        if (record.isRoundSequence()) {
            int safeStage = Math.max(0, Math.min(record.stageTargets.length - 1, stage));
            return String.format(Locale.US, "لڑی %d/%d — جملہ %d/%d",
                    Math.min(record.roundTarget, rounds + 1), record.roundTarget,
                    safeStage + 1, record.stageTargets.length);
        }
        if (record.isMultiStage()) {
            int safeStage = Math.max(0, Math.min(record.stageTargets.length - 1, stage));
            return String.format(Locale.US, "مرحلہ %d/%d — %d/%d",
                    safeStage + 1, record.stageTargets.length,
                    count, record.stageTargets[safeStage]);
        }
        return record.target > 0
                ? String.format(Locale.US, "%d / %d", count, record.target)
                : String.format(Locale.US, "گنتی: %d", count);
    }

    public static Intent createIntent(Context context) {
        List<Record> records = getIncompleteSessions(context);
        return records.isEmpty()
                ? new Intent(context, MainActivity.class)
                : createIntent(context, records.get(0));
    }

    public static Intent createIntent(Context context, Record record) {
        return createIntentForDate(context, record, record.sessionDate);
    }

    public static Intent createIntentForDate(Context context, Record record, String sessionDate) {
        Intent intent = new Intent(context, TasbeehActivity.class);
        intent.putExtra(MainActivity.EXTRA_MODE, record.mode);
        intent.putExtra(MainActivity.EXTRA_SESSION_DATE,
                sessionDate == null || sessionDate.isEmpty() ? DhikrHistoryStore.today() : sessionDate);
        intent.putExtra(MainActivity.EXTRA_TARGET, record.target);
        intent.putExtra(MainActivity.EXTRA_SEQUENCE_ROUND_TARGET, record.roundTarget);
        intent.putExtra(MainActivity.EXTRA_TITLE, record.title);
        intent.putExtra(MainActivity.EXTRA_PHRASE, record.phrase);
        intent.putExtra(MainActivity.EXTRA_TRANSLATION, record.translation);
        intent.putExtra(MainActivity.EXTRA_SOURCE_NOTE, record.source);
        intent.putExtra(MainActivity.EXTRA_LONG_TEXT, record.longText);
        if (record.stageTargets.length > 0) {
            intent.putExtra(MainActivity.EXTRA_STAGE_TARGETS, record.stageTargets);
            intent.putExtra(MainActivity.EXTRA_SEQUENCE_PHRASES, record.sequencePhrases);
            intent.putExtra(MainActivity.EXTRA_SEQUENCE_TRANSLATIONS,
                    record.sequenceTranslations);
        }
        return intent;
    }

    /**
     * Moves only the active resumable session to today. The previous day's history entry is
     * deliberately left untouched, so it remains a historical incomplete snapshot.
     */
    public static Intent carryForwardToToday(Context context, Record record) {
        if (record == null) return new Intent(context, MainActivity.class);
        String today = DhikrHistoryStore.today();
        SharedPreferences p = prefs(context);
        String base = base(record.mode);
        p.edit()
                .putString(base + "date", today)
                .putLong(base + "updated_at", System.currentTimeMillis())
                .apply();
        return createIntentForDate(context, record, today);
    }

    public static Intent startFreshToday(Context context, Record record) {
        if (record == null) return new Intent(context, MainActivity.class);
        clearModeState(context, record.mode);
        return createIntentForDate(context, record, DhikrHistoryStore.today());
    }

    public static void markCompleted(Context context, String mode) {
        clear(context, mode);
    }

    public static void clear(Context context, String mode) {
        if (mode == null || mode.trim().isEmpty()) return;
        SharedPreferences p = prefs(context);
        List<String> ids = getStoredIds(p);
        ids.remove(mode);
        SharedPreferences.Editor editor = p.edit().putString(IDS_KEY, joinIds(ids));
        removeRecord(editor, mode);
        editor.apply();
    }

    public static void clearModeState(Context context, String mode) {
        if (mode == null) return;
        prefs(context).edit()
                .putInt(countKey(mode), 0)
                .putInt(stageKey(mode), 0)
                .putInt(roundKey(mode), 0)
                .putBoolean(continueKey(mode), false)
                .remove(configKey(mode))
                .apply();
        clear(context, mode);
    }

    public static void clear(Context context) {
        clearAll(context);
    }

    public static void clearAll(Context context) {
        SharedPreferences p = prefs(context);
        List<String> ids = getStoredIds(p);
        SharedPreferences.Editor editor = p.edit().remove(IDS_KEY);
        for (String mode : ids) removeRecord(editor, mode);
        editor.apply();
    }

    private static boolean isIncomplete(SharedPreferences p, Record record) {
        int count = p.getInt(countKey(record.mode), 0);
        int stage = p.getInt(stageKey(record.mode), 0);
        int rounds = p.getInt(roundKey(record.mode), 0);
        if (count <= 0 && stage <= 0 && rounds <= 0) return false;
        if (record.isRoundSequence()) return rounds < record.roundTarget;
        if (record.isMultiStage()) {
            int last = record.stageTargets.length - 1;
            int safeStage = Math.max(0, Math.min(last, stage));
            return !(safeStage == last && count >= record.stageTargets[last]);
        }
        if (record.target > 0) return count < record.target;
        return count > 0;
    }

    private static Record readRecord(SharedPreferences p, String mode) {
        String base = base(mode);
        String storedMode = p.getString(base + "mode", null);
        if (storedMode == null) return null;
        return new Record(storedMode,
                p.getString(base + "date", DhikrHistoryStore.today()),
                p.getInt(base + "target", 0),
                p.getInt(base + "round_target", 0),
                p.getString(base + "title", "پچھلا ذکر"),
                p.getString(base + "phrase", ""),
                p.getString(base + "translation", ""),
                p.getString(base + "source", ""),
                p.getBoolean(base + "long", false),
                parseInts(p.getString(base + "stage_targets", "")),
                parseStrings(p.getString(base + "sequence_phrases", "")),
                parseStrings(p.getString(base + "sequence_translations", "")),
                p.getLong(base + "updated_at", 0L));
    }

    private static void migrateIfNeeded(Context context) {
        SharedPreferences p = prefs(context);
        if (p.getBoolean(MIGRATED_KEY, false)) return;
        List<String> migrated = new ArrayList<>();
        String oldIds = p.getString(OLD_IDS_KEY, "");
        for (String mode : splitIds(oldIds)) {
            String oldBase = OLD_PREFIX + safeId(mode) + "_";
            String title = p.getString(oldBase + "title", "پچھلا ذکر");
            int count = p.getInt(countKey(mode), 0);
            int stage = p.getInt(stageKey(mode), 0);
            if (count > 0 || stage > 0) {
                String base = base(mode);
                SharedPreferences.Editor e = p.edit()
                        .putString(base + "mode", mode)
                        .putString(base + "date", DhikrHistoryStore.today())
                        .putInt(base + "target", p.getInt(oldBase + "target", 0))
                        .putInt(base + "round_target", 0)
                        .putString(base + "title", title)
                        .putString(base + "phrase", p.getString(oldBase + "phrase", ""))
                        .putString(base + "translation", p.getString(oldBase + "translation", ""))
                        .putString(base + "source", p.getString(oldBase + "source", ""))
                        .putBoolean(base + "long", p.getBoolean(oldBase + "long", false))
                        .putString(base + "stage_targets", p.getString(oldBase + "stage_targets", ""))
                        .putString(base + "sequence_phrases", p.getString(oldBase + "sequence_phrases", ""))
                        .putString(base + "sequence_translations", p.getString(oldBase + "sequence_translations", ""))
                        .putLong(base + "updated_at", System.currentTimeMillis());
                e.apply();
                migrated.add(mode);
            }
        }
        if (migrated.isEmpty() && p.getBoolean(LEGACY_PREFIX + "valid", false)) {
            String mode = p.getString(LEGACY_PREFIX + "mode", MainActivity.MODE_SIMPLE);
            int count = p.getInt(countKey(mode), 0);
            if (count > 0) migrated.add(mode);
        }
        p.edit().putString(IDS_KEY, joinIds(migrated)).putBoolean(MIGRATED_KEY, true).apply();
    }

    public static String countKey(String mode) {
        return MainActivity.MODE_SIMPLE.equals(mode) ? MainActivity.KEY_COUNT : "mode_" + mode + "_count";
    }
    public static String stageKey(String mode) { return "mode_" + mode + "_stage"; }
    public static String roundKey(String mode) { return "mode_" + mode + "_round"; }
    public static String configKey(String mode) { return "mode_" + mode + "_config"; }
    public static String continueKey(String mode) { return "mode_" + mode + "_continue"; }

    private static String base(String mode) { return PREFIX + safeId(mode) + "_"; }
    private static String safeId(String mode) { return mode.replaceAll("[^A-Za-z0-9_]", "_"); }

    private static void removeRecord(SharedPreferences.Editor editor, String mode) {
        String base = base(mode);
        editor.remove(base + "mode").remove(base + "date").remove(base + "target")
                .remove(base + "round_target").remove(base + "title")
                .remove(base + "phrase").remove(base + "translation")
                .remove(base + "source").remove(base + "long")
                .remove(base + "stage_targets").remove(base + "sequence_phrases")
                .remove(base + "sequence_translations").remove(base + "updated_at");
    }

    private static List<String> getStoredIds(SharedPreferences p) {
        return splitIds(p.getString(IDS_KEY, ""));
    }

    private static List<String> splitIds(String raw) {
        Set<String> unique = new LinkedHashSet<>();
        if (raw != null && !raw.isEmpty()) {
            for (String value : raw.split(SEP, -1)) if (!value.isEmpty()) unique.add(value);
        }
        return new ArrayList<>(unique);
    }

    private static String joinIds(List<String> ids) {
        StringBuilder b = new StringBuilder();
        for (String id : ids) { if (b.length() > 0) b.append(SEP); b.append(id); }
        return b.toString();
    }

    private static String safe(String value) { return value == null ? "" : value; }

    private static String join(int[] values) {
        if (values == null || values.length == 0) return "";
        StringBuilder b = new StringBuilder();
        for (int value : values) { if (b.length() > 0) b.append(','); b.append(value); }
        return b.toString();
    }

    private static String join(String[] values) {
        if (values == null || values.length == 0) return "";
        StringBuilder b = new StringBuilder();
        for (String value : values) { if (b.length() > 0) b.append(SEP); b.append(safe(value)); }
        return b.toString();
    }

    private static int[] parseInts(String raw) {
        if (raw == null || raw.isEmpty()) return new int[0];
        String[] parts = raw.split(",");
        int[] values = new int[parts.length];
        try { for (int i = 0; i < parts.length; i++) values[i] = Integer.parseInt(parts[i]); }
        catch (NumberFormatException e) { return new int[0]; }
        return values;
    }

    private static String[] parseStrings(String raw) {
        return raw == null || raw.isEmpty() ? new String[0] : raw.split(SEP, -1);
    }
}
