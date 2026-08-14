package com.rizwan.tasbeehcounter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Persistent personal dhikr challenges. Challenge targets are user goals, not religious rulings. */
public final class ChallengeStore {
    private static final String PREFS = "dhikr_challenges_v1";
    private static final String IDS = "challenge_ids";
    private static final String PREFIX = "challenge_";
    private static final String MODE_PREFIX = "challenge_mode_";
    private static final String SEP = "\u001F";

    private ChallengeStore() {}

    public static final class Record {
        public final String id;
        public final String sourceId;
        public final String title;
        public final String phrase;
        public final String translation;
        public final String sourceNote;
        public final int target;
        public final boolean longText;
        public final String[] sequencePhrases;
        public final String[] sequenceTranslations;
        public final int[] sequenceTargets;
        public final long acceptedAt;
        public final long completedAt;

        Record(String id, String sourceId, String title, String phrase, String translation,
               String sourceNote, int target, boolean longText, String[] sequencePhrases,
               String[] sequenceTranslations, int[] sequenceTargets, long acceptedAt,
               long completedAt) {
            this.id = id;
            this.sourceId = sourceId;
            this.title = title;
            this.phrase = phrase;
            this.translation = translation;
            this.sourceNote = sourceNote;
            this.target = target;
            this.longText = longText;
            this.sequencePhrases = sequencePhrases;
            this.sequenceTranslations = sequenceTranslations;
            this.sequenceTargets = sequenceTargets;
            this.acceptedAt = acceptedAt;
            this.completedAt = completedAt;
        }

        public boolean isCompleted() { return completedAt > 0L; }
        public boolean isSequence() {
            return sequenceTargets != null && sequencePhrases != null
                    && sequenceTranslations != null && sequenceTargets.length > 0
                    && sequenceTargets.length == sequencePhrases.length
                    && sequenceTargets.length == sequenceTranslations.length;
        }
        public String mode() { return MODE_PREFIX + id; }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static Record acceptPreset(Context context, String presetId, String title,
                                      String phrase, String translation, int target) {
        return accept(context, "preset:" + presetId, title, phrase, translation,
                "Personal challenge target; no claim that this target is a reported fixed count.",
                target, false, null, null, null);
    }

    public static Record acceptCustom(Context context, String title, String phrase, int target) {
        return accept(context, "custom", title, phrase, "",
                "User-created personal dhikr challenge.", target, phrase.length() > 90,
                null, null, null);
    }

    public static Record acceptDhikr(Context context, DhikrItem item, int target) {
        if (item == null) return null;
        return accept(context, "dhikr:" + item.id, item.title, item.arabic, item.translation,
                item.sourceNote, target, item.longText,
                item.sequencePhrases, item.sequenceTranslations, item.sequenceTargets);
    }

    private static Record accept(Context context, String sourceId, String title, String phrase,
                                 String translation, String sourceNote, int target,
                                 boolean longText, String[] sequencePhrases,
                                 String[] sequenceTranslations, int[] sequenceTargets) {
        if (context == null || target < 1 || phrase == null || phrase.trim().isEmpty()) return null;
        long now = System.currentTimeMillis();
        String id = "c" + now + "_" + Math.abs((sourceId + phrase + now).hashCode());
        Record record = new Record(id, safe(sourceId), safe(title), safe(phrase),
                safe(translation), safe(sourceNote), target, longText,
                sequencePhrases == null ? new String[0] : sequencePhrases.clone(),
                sequenceTranslations == null ? new String[0] : sequenceTranslations.clone(),
                sequenceTargets == null ? new int[0] : sequenceTargets.clone(), now, 0L);
        save(context, record);
        return record;
    }

    private static void save(Context context, Record record) {
        SharedPreferences p = prefs(context);
        List<String> ids = getIds(p);
        ids.remove(record.id);
        ids.add(0, record.id);
        String base = base(record.id);
        p.edit()
                .putString(IDS, joinIds(ids))
                .putString(base + "source_id", record.sourceId)
                .putString(base + "title", record.title)
                .putString(base + "phrase", record.phrase)
                .putString(base + "translation", record.translation)
                .putString(base + "source_note", record.sourceNote)
                .putInt(base + "target", record.target)
                .putBoolean(base + "long", record.longText)
                .putString(base + "sequence_phrases", join(record.sequencePhrases))
                .putString(base + "sequence_translations", join(record.sequenceTranslations))
                .putString(base + "sequence_targets", join(record.sequenceTargets))
                .putLong(base + "accepted_at", record.acceptedAt)
                .putLong(base + "completed_at", record.completedAt)
                .apply();
    }

    public static List<Record> getAll(Context context) {
        SharedPreferences p = prefs(context);
        List<Record> result = new ArrayList<>();
        for (String id : getIds(p)) {
            Record record = read(p, id);
            if (record != null) result.add(record);
        }
        result.sort((a, b) -> Long.compare(b.acceptedAt, a.acceptedAt));
        return result;
    }

    public static Record findActiveBySource(Context context, String sourceId) {
        if (sourceId == null) return null;
        for (Record record : getAll(context)) {
            if (!record.isCompleted() && sourceId.equals(record.sourceId)) return record;
        }
        return null;
    }

    public static void start(Activity activity, Record record) {
        if (activity == null || record == null || record.isCompleted()) return;
        LastSessionStore.Record existing = LastSessionStore.findIncomplete(activity, record.mode());
        if (existing != null) {
            DhikrStartHelper.resumeExisting(activity, existing);
            return;
        }
        int roundTarget = record.isSequence() ? record.target : 0;
        DhikrStartHelper.start(activity, record.mode(), record.isSequence() ? 0 : record.target,
                roundTarget, record.title, record.phrase, record.translation, record.sourceNote,
                record.longText, record.isSequence() ? record.sequenceTargets : null,
                record.isSequence() ? record.sequencePhrases : null,
                record.isSequence() ? record.sequenceTranslations : null);
    }

    public static void markCompletedIfChallenge(Context context, String mode) {
        if (!isChallengeMode(mode)) return;
        String id = mode.substring(MODE_PREFIX.length());
        SharedPreferences p = prefs(context);
        String base = base(id);
        if (!p.contains(base + "accepted_at")) return;
        p.edit().putLong(base + "completed_at", System.currentTimeMillis()).apply();
    }

    public static boolean isChallengeMode(String mode) {
        return mode != null && mode.startsWith(MODE_PREFIX);
    }

    public static String progressText(Context context, Record record) {
        if (record == null) return "";
        if (record.isCompleted()) {
            return record.isSequence()
                    ? String.format(Locale.US, "%d / %d لڑیاں", record.target, record.target)
                    : String.format(Locale.US, "%d / %d", record.target, record.target);
        }
        SharedPreferences p = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        if (record.isSequence()) {
            int rounds = p.getInt(LastSessionStore.roundKey(record.mode()), 0);
            int stage = p.getInt(LastSessionStore.stageKey(record.mode()), 0);
            int count = p.getInt(LastSessionStore.countKey(record.mode()), 0);
            if (rounds <= 0 && stage <= 0 && count <= 0) {
                return String.format(Locale.US, "0 / %d لڑیاں", record.target);
            }
            return String.format(Locale.US, "لڑی %d/%d — جملہ %d/%d",
                    Math.min(record.target, rounds + 1), record.target,
                    Math.min(record.sequenceTargets.length, stage + 1), record.sequenceTargets.length);
        }
        int count = p.getInt(LastSessionStore.countKey(record.mode()), 0);
        return String.format(Locale.US, "%d / %d", Math.max(0, count), record.target);
    }

    public static String acceptedDate(Record record) {
        if (record == null || record.acceptedAt <= 0) return "";
        return new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(new Date(record.acceptedAt));
    }

    private static Record read(SharedPreferences p, String id) {
        String base = base(id);
        long acceptedAt = p.getLong(base + "accepted_at", 0L);
        if (acceptedAt <= 0L) return null;
        return new Record(id,
                p.getString(base + "source_id", ""),
                p.getString(base + "title", "ذکر چیلنج"),
                p.getString(base + "phrase", ""),
                p.getString(base + "translation", ""),
                p.getString(base + "source_note", ""),
                p.getInt(base + "target", 1),
                p.getBoolean(base + "long", false),
                parseStrings(p.getString(base + "sequence_phrases", "")),
                parseStrings(p.getString(base + "sequence_translations", "")),
                parseInts(p.getString(base + "sequence_targets", "")),
                acceptedAt, p.getLong(base + "completed_at", 0L));
    }

    private static String base(String id) { return PREFIX + id + "_"; }
    private static String safe(String value) { return value == null ? "" : value; }

    private static List<String> getIds(SharedPreferences p) {
        Set<String> unique = new LinkedHashSet<>();
        String raw = p.getString(IDS, "");
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

    private static String join(String[] values) {
        if (values == null || values.length == 0) return "";
        StringBuilder b = new StringBuilder();
        for (String value : values) { if (b.length() > 0) b.append(SEP); b.append(safe(value)); }
        return b.toString();
    }

    private static String join(int[] values) {
        if (values == null || values.length == 0) return "";
        StringBuilder b = new StringBuilder();
        for (int value : values) { if (b.length() > 0) b.append(','); b.append(value); }
        return b.toString();
    }

    private static String[] parseStrings(String raw) {
        return raw == null || raw.isEmpty() ? new String[0] : raw.split(SEP, -1);
    }

    private static int[] parseInts(String raw) {
        if (raw == null || raw.isEmpty()) return new int[0];
        String[] parts = raw.split(",");
        int[] values = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) values[i] = Integer.parseInt(parts[i]);
        } catch (NumberFormatException e) {
            return new int[0];
        }
        return values;
    }
}
