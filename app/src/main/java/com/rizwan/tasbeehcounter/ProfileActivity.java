package com.rizwan.tasbeehcounter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

/**
 * The "میں" tab: what has actually been counted, shortcuts into the record screens,
 * and the two feedback switches that used to crowd the bottom of the home screen.
 */
public class ProfileActivity extends Activity {

    private SharedPreferences preferences;
    private TextView todayValue;
    private TextView streakValue;
    private TextView daysValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        UrduFont.applyToActivity(this);
        preferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);

        todayValue = findViewById(R.id.profileToday);
        streakValue = findViewById(R.id.profileStreak);
        daysValue = findViewById(R.id.profileDays);
        UrduFont.useDigitFont(todayValue, streakValue, daysValue);

        setUpSettings();
        BottomBar.attach(this, BottomBar.TAB_ME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomBar.attach(this, BottomBar.TAB_ME);
        todayValue.setText(String.valueOf(DhikrStats.todayCount(this)));
        streakValue.setText(String.valueOf(DhikrStats.streakDays(this)));
        daysValue.setText(String.valueOf(DhikrStats.totalDays(this)));
        buildWeekRow();
        buildRows();
    }

    private void setUpSettings() {
        Switch vibration = findViewById(R.id.vibrationSwitch);
        Switch sound = findViewById(R.id.soundSwitch);
        vibration.setChecked(preferences.getBoolean(MainActivity.KEY_VIBRATION, true));
        sound.setChecked(preferences.getBoolean(MainActivity.KEY_COMPLETION_SOUND, true));
        vibration.setOnCheckedChangeListener((button, checked) ->
                preferences.edit().putBoolean(MainActivity.KEY_VIBRATION, checked).apply());
        sound.setOnCheckedChangeListener((button, checked) ->
                preferences.edit().putBoolean(MainActivity.KEY_COMPLETION_SOUND, checked).apply());

        Button test = findViewById(R.id.vibrationTestButton);
        test.setOnClickListener(v -> {
            if (!HapticHelper.hasVibrator(this)) {
                toast(R.string.no_vibrator);
                return;
            }
            toast(HapticHelper.test(this, v)
                    ? R.string.vibration_test_message
                    : R.string.vibration_test_failed);
        });
    }

    /** Seven beads, oldest on the right, today on the left and a little larger. */
    private void buildWeekRow() {
        LinearLayout weekRow = findViewById(R.id.weekRow);
        weekRow.removeAllViews();
        boolean[] days = DhikrStats.lastSevenDays(this);
        for (int i = 0; i < days.length; i++) {
            boolean isToday = i == days.length - 1;
            View bead = new View(this);
            bead.setBackgroundResource(days[i] ? R.drawable.bead_on : R.drawable.bead_off);
            int size = dpToPx(isToday ? 18 : 13);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.leftMargin = dpToPx(6);
            params.rightMargin = dpToPx(6);
            weekRow.addView(bead, params);
        }
    }

    private void buildRows() {
        LinearLayout holder = findViewById(R.id.rowsHolder);
        holder.removeAllViews();

        int incomplete = LastSessionStore.getIncompleteSessions(this).size();
        if (incomplete > 0) {
            addRow(holder, getString(R.string.row_incomplete),
                    String.format(Locale.US, getString(R.string.incomplete_count), incomplete),
                    IncompleteSessionsActivity.class);
        }
        addRow(holder, getString(R.string.row_history), null, HistoryActivity.class);
        addRow(holder, getString(R.string.row_routine), routineSummary(),
                DailyRoutineActivity.class);
        addRow(holder, getString(R.string.row_challenge), challengeSummary(),
                ChallengeActivity.class);
    }

    private String routineSummary() {
        int total = DailyRoutineStore.getIds(this).size();
        if (total == 0) return null;
        return String.format(Locale.US, getString(R.string.routine_progress),
                DailyRoutineStore.completedCount(this), total);
    }

    private String challengeSummary() {
        int active = 0;
        List<ChallengeStore.Record> all = ChallengeStore.getAll(this);
        for (ChallengeStore.Record record : all) {
            if (!record.isCompleted()) active++;
        }
        if (active == 0) return null;
        return String.format(Locale.US, getString(R.string.challenge_active), active);
    }

    private void addRow(LinearLayout holder, String title, String subtitle, Class<?> target) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setBackgroundResource(R.drawable.row_quiet);
        row.setPadding(dpToPx(18), dpToPx(14), dpToPx(18), dpToPx(14));
        row.setClickable(true);
        row.setFocusable(true);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(20f);
        titleView.setTextColor(getColor(R.color.plaster));
        row.addView(titleView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        if (subtitle != null) {
            TextView subtitleView = new TextView(this);
            subtitleView.setText(subtitle);
            subtitleView.setTextSize(14f);
            subtitleView.setTextColor(getColor(R.color.brass));
            row.addView(subtitleView, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dpToPx(10);
        holder.addView(row, params);
        row.setOnClickListener(v -> startActivity(new Intent(this, target)));
        // These views are built after applyToActivity() ran, so they need the font here.
        UrduFont.apply(row, this);
    }

    private void toast(int messageRes) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
