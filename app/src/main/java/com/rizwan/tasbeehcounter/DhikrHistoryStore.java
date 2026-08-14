package com.rizwan.tasbeehcounter;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Small V1 history store. One durable record per date and dhikr. */
public final class DhikrHistoryStore {
    public static final String STATUS_INCOMPLETE = "incomplete";
    public static final String STATUS_COMPLETED = "completed";

    private static final String DATES = "history_dates_v1";
    private static final String MODES = "history_modes_v1_";
    private static final String PREFIX = "history_v1_";

    private DhikrHistoryStore() {}

    public static final class Entry {
        public final String date;
        public final String mode;
        public final String title;
        public final String status;
        public final int count;
        public final int target;
        public final int stageIndex;
        public final int stageCount;
        public final int completedRounds;
        public final int roundTarget;
        public final long updatedAt;

        Entry(String date, String mode, String title, String status, int count, int target,
              int stageIndex, int stageCount, int completedRounds, int roundTarget,
              long updatedAt) {
            this.date = date;
            this.mode = mode;
            this.title = title;
            this.status = status;
            this.count = count;
            this.target = target;
            this.stageIndex = stageIndex;
            this.stageCount = stageCount;
            this.completedRounds = completedRounds;
            this.roundTarget = roundTarget;
            this.updatedAt = updatedAt;
        }

        public boolean isCompleted() {
            return STATUS_COMPLETED.equals(status);
        }

        public String progressText() {
            if (roundTarget > 0 && stageCount > 0) {
                int strand = isCompleted() ? roundTarget : Math.min(roundTarget, completedRounds + 1);
                int sentence = isCompleted() ? stageCount : Math.min(stageCount, stageIndex + 1);
                return String.format(Locale.US, "لڑی %d/%d — جملہ %d/%d",
                        strand, roundTarget, sentence, stageCount);
            }
            if (stageCount > 0) {
                return String.format(Locale.US, "مرحلہ %d/%d — %d/%d",
                        Math.min(stageCount, stageIndex + 1), stageCount, count, target);
            }
            return target > 0
                    ? String.format(Locale.US, "%d / %d", count, target)
                    : String.format(Locale.US, "گنتی: %d", count);
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static String today() {
        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
    }

    public static String displayDate(String date) {
        if (date == null || date.length() != 8) return date == null ? "" : date;
        return date.substring(6, 8) + "-" + date.substring(4, 6) + "-" + date.substring(0, 4);
    }

    public static void update(Context context, String date, String mode, String title,
                              String status, int count, int target, int stageIndex,
                              int stageCount, int completedRounds, int roundTarget) {
        if (date == null || date.isEmpty() || mode == null || mode.isEmpty()) return;
        if (!STATUS_COMPLETED.equals(status) && count <= 0 && stageIndex <= 0
                && completedRounds <= 0) return;

        SharedPreferences p = prefs(context);
        Set<String> dates = new HashSet<>(p.getStringSet(DATES, Collections.emptySet()));
        Set<String> modes = new HashSet<>(p.getStringSet(MODES + date, Collections.emptySet()));
        dates.add(date);
        modes.add(mode);
        String base = base(date, mode);
        p.edit()
                .putStringSet(DATES, dates)
                .putStringSet(MODES + date, modes)
                .putString(base + "title", title == null ? mode : title)
                .putString(base + "status", status)
                .putInt(base + "count", Math.max(0, count))
                .putInt(base + "target", Math.max(0, target))
                .putInt(base + "stage", Math.max(0, stageIndex))
                .putInt(base + "stage_count", Math.max(0, stageCount))
                .putInt(base + "rounds", Math.max(0, completedRounds))
                .putInt(base + "round_target", Math.max(0, roundTarget))
                .putLong(base + "updated", System.currentTimeMillis())
                .apply();
    }

    public static void markCompleted(Context context, String date, String mode, String title,
                                     int count, int target, int stageIndex, int stageCount,
                                     int completedRounds, int roundTarget) {
        update(context, date, mode, title, STATUS_COMPLETED, count, target, stageIndex,
                stageCount, completedRounds, roundTarget);
    }

    public static List<String> getDates(Context context) {
        List<String> result = new ArrayList<>(prefs(context)
                .getStringSet(DATES, Collections.emptySet()));
        result.sort(Collections.reverseOrder());
        return result;
    }

    public static List<Entry> getEntries(Context context, String date) {
        SharedPreferences p = prefs(context);
        List<Entry> result = new ArrayList<>();
        for (String mode : p.getStringSet(MODES + date, Collections.emptySet())) {
            String base = base(date, mode);
            result.add(new Entry(date, mode,
                    p.getString(base + "title", mode),
                    p.getString(base + "status", STATUS_INCOMPLETE),
                    p.getInt(base + "count", 0),
                    p.getInt(base + "target", 0),
                    p.getInt(base + "stage", 0),
                    p.getInt(base + "stage_count", 0),
                    p.getInt(base + "rounds", 0),
                    p.getInt(base + "round_target", 0),
                    p.getLong(base + "updated", 0L)));
        }
        result.sort((a, b) -> Long.compare(b.updatedAt, a.updatedAt));
        return result;
    }

    private static String base(String date, String mode) {
        return PREFIX + date + "_" + mode.replaceAll("[^A-Za-z0-9_]", "_") + "_";
    }
}
