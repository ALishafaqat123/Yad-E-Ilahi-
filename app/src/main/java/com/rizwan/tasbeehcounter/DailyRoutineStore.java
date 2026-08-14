package com.rizwan.tasbeehcounter;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class DailyRoutineStore {
    private static final String KEY_IDS = "daily_routine_ids";
    private static final String TARGET_PREFIX = "daily_target_";
    private static final String COMPLETED_PREFIX = "daily_completed_";

    private DailyRoutineStore() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static List<String> getIds(Context context) {
        String raw = prefs(context).getString(KEY_IDS, "");
        List<String> result = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return result;
        for (String value : raw.split(",")) {
            String id = value.trim();
            if (!id.isEmpty() && !result.contains(id)) result.add(id);
        }
        return result;
    }

    public static boolean isIncluded(Context context, String id) {
        return getIds(context).contains(id);
    }

    public static void add(Context context, String id, int target) {
        Set<String> ids = new LinkedHashSet<>(getIds(context));
        ids.add(id);
        prefs(context).edit()
                .putString(KEY_IDS, join(ids))
                .putInt(TARGET_PREFIX + id, Math.max(1, target))
                .remove(COMPLETED_PREFIX + id)
                .apply();
    }

    public static void remove(Context context, String id) {
        Set<String> ids = new LinkedHashSet<>(getIds(context));
        ids.remove(id);
        prefs(context).edit()
                .putString(KEY_IDS, join(ids))
                .remove(TARGET_PREFIX + id)
                .remove(COMPLETED_PREFIX + id)
                .apply();
    }

    public static int getTarget(Context context, DhikrItem item) {
        return prefs(context).getInt(TARGET_PREFIX + item.id, Math.max(1, item.defaultTarget));
    }

    public static void markCompleted(Context context, String id) {
        if (isIncluded(context, id)) {
            prefs(context).edit().putString(COMPLETED_PREFIX + id, today()).apply();
        }
    }

    public static boolean isCompletedToday(Context context, String id) {
        return today().equals(prefs(context).getString(COMPLETED_PREFIX + id, ""));
    }

    public static void resetToday(Context context) {
        SharedPreferences.Editor editor = prefs(context).edit();
        for (String id : getIds(context)) {
            editor.remove(COMPLETED_PREFIX + id)
                    .putInt("mode_" + id + "_count", 0)
                    .putInt("mode_" + id + "_stage", 0)
                    .putInt("mode_" + id + "_round", 0)
                    .putBoolean("mode_" + id + "_continue", false);
        }
        editor.apply();
    }

    public static int completedCount(Context context) {
        int count = 0;
        for (String id : getIds(context)) if (isCompletedToday(context, id)) count++;
        return count;
    }

    private static String join(Set<String> ids) {
        StringBuilder builder = new StringBuilder();
        for (String id : ids) {
            if (builder.length() > 0) builder.append(',');
            builder.append(id);
        }
        return builder.toString();
    }

    private static String today() {
        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
    }
}
