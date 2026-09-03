package com.rizwan.tasbeehcounter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

/**
 * The four shortcuts pinned to the bottom of the home and "میں" screens.
 * Text only on purpose: the Nastaleeq font carries no icon glyphs, so symbols
 * would show up as empty boxes on some phones.
 */
final class BottomBar {
    static final int TAB_DHIKR = 0;
    static final int TAB_ROUTINE = 1;
    static final int TAB_CHALLENGE = 2;
    static final int TAB_ME = 3;

    private BottomBar() {
    }

    static void attach(Activity activity, int current) {
        bind(activity, current, TAB_DHIKR, R.id.tabDhikr, R.id.tabDhikrBar,
                R.id.tabDhikrLabel, MainActivity.class);
        bind(activity, current, TAB_ROUTINE, R.id.tabRoutine, R.id.tabRoutineBar,
                R.id.tabRoutineLabel, DailyRoutineActivity.class);
        bind(activity, current, TAB_CHALLENGE, R.id.tabChallenge, R.id.tabChallengeBar,
                R.id.tabChallengeLabel, ChallengeActivity.class);
        bind(activity, current, TAB_ME, R.id.tabMe, R.id.tabMeBar,
                R.id.tabMeLabel, ProfileActivity.class);
    }

    private static void bind(Activity activity, int current, int tab, int rowId, int barId,
                             int labelId, Class<?> target) {
        View row = activity.findViewById(rowId);
        View bar = activity.findViewById(barId);
        TextView label = activity.findViewById(labelId);
        if (row == null || bar == null || label == null) return;

        boolean active = tab == current;
        bar.setBackgroundColor(activity.getColor(
                active ? R.color.brass : android.R.color.transparent));
        label.setTextColor(activity.getColor(active ? R.color.plaster : R.color.muted));

        if (active) {
            row.setOnClickListener(null);
            row.setClickable(false);
            return;
        }
        row.setClickable(true);
        row.setOnClickListener(v -> open(activity, target));
    }

    private static void open(Activity activity, Class<?> target) {
        Intent intent = new Intent(activity, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
        // No finish() here on purpose. "معمول" and "چیلنج" carry no bar of their own yet,
        // so the back button has to lead somewhere. CLEAR_TOP keeps the stack shallow:
        // returning to the home screen drops everything stacked above it.
    }
}
