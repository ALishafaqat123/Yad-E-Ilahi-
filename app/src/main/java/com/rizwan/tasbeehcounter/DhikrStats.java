package com.rizwan.tasbeehcounter;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Read-only summaries of what has actually been counted. Shared by the home screen
 * and the "میں" screen so both show the same numbers.
 */
final class DhikrStats {

    private DhikrStats() {
    }

    /** Everything counted today, across all adhkar. */
    static int todayCount(Context context) {
        int total = 0;
        for (DhikrHistoryStore.Entry entry
                : DhikrHistoryStore.getEntries(context, DhikrHistoryStore.today())) {
            total += entry.count;
        }
        return total;
    }

    /**
     * Unbroken run of days with at least one dhikr. A day that has only just started
     * does not break the run, so the count is taken from yesterday when today is empty.
     */
    static int streakDays(Context context) {
        Set<String> dates = new HashSet<>(DhikrHistoryStore.getDates(context));
        if (dates.isEmpty()) return 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
        Calendar cursor = Calendar.getInstance();
        if (!dates.contains(format.format(cursor.getTime()))) {
            cursor.add(Calendar.DAY_OF_YEAR, -1);
        }
        int streak = 0;
        while (dates.contains(format.format(cursor.getTime()))) {
            streak++;
            cursor.add(Calendar.DAY_OF_YEAR, -1);
        }
        return streak;
    }

    /** Every day on which something was counted. */
    static int totalDays(Context context) {
        return DhikrHistoryStore.getDates(context).size();
    }

    /** Seven flags for the past week, oldest first, ending with today. */
    static boolean[] lastSevenDays(Context context) {
        Set<String> dates = new HashSet<>(DhikrHistoryStore.getDates(context));
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
        Calendar cursor = Calendar.getInstance();
        cursor.add(Calendar.DAY_OF_YEAR, -6);
        boolean[] days = new boolean[7];
        for (int i = 0; i < 7; i++) {
            days[i] = dates.contains(format.format(cursor.getTime()));
            cursor.add(Calendar.DAY_OF_YEAR, 1);
        }
        return days;
    }
}
