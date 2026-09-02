package com.rizwan.tasbeehcounter;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/** Clean full-screen day-wise history. */
public class HistoryActivity extends Activity {
    private LinearLayout container;
    private TextView empty;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        container = findViewById(R.id.historyContainer);
        empty = findViewById(R.id.historyEmptyMessage);
        findViewById(R.id.historyBackButton).setOnClickListener(v -> finish());
        UrduFont.applyToActivity(this);
    }

    @Override protected void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        container.removeAllViews();
        List<String> dates = DhikrHistoryStore.getDates(this);
        empty.setVisibility(dates.isEmpty() ? View.VISIBLE : View.GONE);
        for (String date : dates) {
            List<DhikrHistoryStore.Entry> entries = DhikrHistoryStore.getEntries(this, date);
            if (entries.isEmpty()) continue;
            TextView dateTitle = text(DhikrHistoryStore.today().equals(date)
                    ? "آج — " + DhikrHistoryStore.displayDate(date)
                    : DhikrHistoryStore.displayDate(date), 22, 0xFF0C4625, true);
            LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            dateParams.topMargin = dp(16);
            container.addView(dateTitle, dateParams);
            for (DhikrHistoryStore.Entry entry : entries) {
                String state;
                if (entry.isCompleted()) {
                    state = "مکمل";
                } else {
                    LastSessionStore.Record active = LastSessionStore.findIncomplete(this, entry.mode);
                    state = active != null && date.equals(active.sessionDate) ? "جاری" : "ادھورا";
                }
                container.addView(card(entry, state));
            }
        }
        UrduFont.applyToActivity(this);
    }

    private View card(DhikrHistoryStore.Entry entry, String state) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        card.setGravity(Gravity.RIGHT);
        card.setBackgroundResource(R.drawable.mode_card_background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(8);
        card.setLayoutParams(params);
        card.addView(text(entry.title, 21, 0xFF0C4625, true));
        int stateColor = entry.isCompleted() ? 0xFF0A6B35 : 0xFF8A5E00;
        card.addView(text(state + " — " + entry.progressText(), 16, stateColor, true));
        UrduFont.apply(card, this);
        return card;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.RIGHT);
        if (bold) view.setTypeface(view.getTypeface(), Typeface.BOLD);
        return view;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
