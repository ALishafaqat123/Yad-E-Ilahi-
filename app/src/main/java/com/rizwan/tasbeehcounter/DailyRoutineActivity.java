package com.rizwan.tasbeehcounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class DailyRoutineActivity extends Activity {
    private LinearLayout container;
    private TextView summary;
    private TextView emptyText;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_routine);
        UrduFont.applyToActivity(this);
        preferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        container = findViewById(R.id.dailyItemsContainer);
        summary = findViewById(R.id.dailySummary);
        emptyText = findViewById(R.id.dailyEmptyText);
        findViewById(R.id.dailyBackButton).setOnClickListener(v -> finish());
        findViewById(R.id.resetDailyButton).setOnClickListener(v -> showResetDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        container.removeAllViews();
        List<String> ids = DailyRoutineStore.getIds(this);
        int completed = DailyRoutineStore.completedCount(this);
        summary.setText(String.format(Locale.US, "آج کے %d میں سے %d اذکار مکمل", ids.size(), completed));
        emptyText.setVisibility(ids.isEmpty() ? View.VISIBLE : View.GONE);
        findViewById(R.id.resetDailyButton).setVisibility(ids.isEmpty() ? View.GONE : View.VISIBLE);
        for (String id : ids) {
            DhikrItem item = DhikrCatalog.findById(id);
            if (item != null) container.addView(makeCard(item));
        }
    }

    private View makeCard(DhikrItem item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        card.setBackgroundResource(R.drawable.mode_card_background);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.topMargin = dpToPx(12);
        card.setLayoutParams(cardParams);

        TextView title = new TextView(this);
        title.setText(item.title);
        title.setTextColor(0xFF0C4625);
        title.setTextSize(22f);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setGravity(Gravity.RIGHT);
        card.addView(title);

        TextView phrase = new TextView(this);
        phrase.setText(item.isSequence() ? item.sequencePhrases[0] : item.arabic);
        phrase.setTextColor(0xFF3A342C);
        phrase.setTextSize(item.longText ? 16f : 18f);
        phrase.setGravity(Gravity.RIGHT);
        phrase.setMaxLines(3);
        LinearLayout.LayoutParams phraseParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        phraseParams.topMargin = dpToPx(3);
        card.addView(phrase, phraseParams);

        boolean complete = DailyRoutineStore.isCompletedToday(this, item.id);
        TextView status = new TextView(this);
        status.setText(complete ? "✓ آج مکمل ہوگیا" : progressText(item));
        status.setTextColor(complete ? 0xFF0A6B35 : 0xFF8A5E00);
        status.setTextSize(15f);
        status.setTypeface(status.getTypeface(), Typeface.BOLD);
        status.setGravity(Gravity.RIGHT);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        statusParams.topMargin = dpToPx(5);
        card.addView(status, statusParams);

        Button start = new Button(this);
        start.setAllCaps(false);
        start.setText(complete ? "دوبارہ پڑھیں" : "شروع کریں / جاری رکھیں");
        start.setTextColor(0xFFFFFFFF);
        start.setTextSize(15f);
        start.setBackgroundResource(R.drawable.button_primary);
        LinearLayout.LayoutParams startParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        startParams.topMargin = dpToPx(9);
        card.addView(start, startParams);

        Button remove = new Button(this);
        remove.setAllCaps(false);
        remove.setText("روزانہ کے معمول سے ہٹائیں");
        remove.setTextColor(0xFF8A1C1C);
        remove.setTextSize(14f);
        remove.setBackgroundResource(R.drawable.button_secondary);
        LinearLayout.LayoutParams removeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        removeParams.topMargin = dpToPx(7);
        card.addView(remove, removeParams);

        start.setOnClickListener(v -> startItem(item));
        remove.setOnClickListener(v -> {
            DailyRoutineStore.remove(this, item.id);
            Toast.makeText(this, "روزانہ کے معمول سے ہٹا دیا گیا", Toast.LENGTH_SHORT).show();
            refresh();
        });
        UrduFont.apply(card, this);
        return card;
    }

    private String progressText(DhikrItem item) {
        int target = DailyRoutineStore.getTarget(this, item);
        int count = preferences.getInt("mode_" + item.id + "_count", 0);
        if (item.isSequence()) {
            int stage = preferences.getInt(LastSessionStore.stageKey(item.id), 0);
            int rounds = preferences.getInt(LastSessionStore.roundKey(item.id), 0);
            stage = Math.max(0, Math.min(item.sequencePhrases.length - 1, stage));
            return String.format(Locale.US, "لڑی %d/%d — جملہ %d/%d",
                    Math.min(target, rounds + 1), target, stage + 1,
                    item.sequencePhrases.length);
        }
        return String.format(Locale.US, "%d / %d", count, target);
    }

    private void startItem(DhikrItem item) {
        int target = DailyRoutineStore.getTarget(this, item);
        DhikrStartHelper.start(this, item.id, item.isSequence() ? 0 : target,
                item.isSequence() ? target : 0, item.title, item.arabic,
                item.translation, item.sourceNote, item.longText,
                item.isSequence() ? item.sequenceTargets : null,
                item.sequencePhrases, item.sequenceTranslations);
    }

    private void showResetDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("آج کا معمول دوبارہ شروع کریں؟")
                .setMessage("آج کی تکمیل اور موجودہ گنتیاں صفر ہوجائیں گی۔ محفوظ ہسٹری برقرار رہے گی۔")
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, (choiceDialog, which) -> {
                    for (String id : DailyRoutineStore.getIds(this)) {
                        LastSessionStore.clearModeState(this, id);
                    }
                    DailyRoutineStore.resetToday(this);
                    refresh();
                })
                .create();
        dialog.setOnShowListener(ignored -> UrduFont.applyToDialog(dialog));
        dialog.show();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
