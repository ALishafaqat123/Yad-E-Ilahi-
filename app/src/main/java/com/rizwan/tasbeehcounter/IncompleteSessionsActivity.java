package com.rizwan.tasbeehcounter;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/** Full-screen, card-based list of all unfinished adhkar. */
public class IncompleteSessionsActivity extends Activity {
    private LinearLayout sessionsContainer;
    private TextView screenTitle;
    private TextView emptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomplete_sessions);
        UrduFont.applyToActivity(this);

        sessionsContainer = findViewById(R.id.sessionsContainer);
        screenTitle = findViewById(R.id.incompleteScreenTitle);
        emptyMessage = findViewById(R.id.incompleteEmptyMessage);

        findViewById(R.id.incompleteBackButton).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderSessions();
    }

    private void renderSessions() {
        List<LastSessionStore.Record> sessions = LastSessionStore.getIncompleteSessions(this);
        sessionsContainer.removeAllViews();
        screenTitle.setText("ادھورے اذکار — " + sessions.size());

        if (sessions.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
            return;
        }
        emptyMessage.setVisibility(View.GONE);

        for (LastSessionStore.Record record : sessions) {
            sessionsContainer.addView(createSessionCard(record));
        }
        UrduFont.applyToActivity(this);
    }

    private View createSessionCard(LastSessionStore.Record record) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.RIGHT);
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        card.setBackgroundResource(R.drawable.mode_card_background);
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.topMargin = dp(12);
        card.setLayoutParams(cardParams);

        TextView title = new TextView(this);
        title.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        title.setGravity(Gravity.RIGHT);
        title.setText(record.title);
        title.setTextColor(getColor(R.color.green_dark));
        title.setTextSize(23);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        card.addView(title);

        TextView progress = new TextView(this);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        progressParams.topMargin = dp(3);
        progress.setLayoutParams(progressParams);
        progress.setGravity(Gravity.RIGHT);
        progress.setText(LastSessionStore.getProgress(this, record));
        progress.setTextColor(getColor(R.color.gold));
        progress.setTextSize(18);
        progress.setTypeface(progress.getTypeface(), Typeface.BOLD);
        card.addView(progress);

        TextView date = new TextView(this);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        dateParams.topMargin = dp(3);
        date.setLayoutParams(dateParams);
        date.setGravity(Gravity.RIGHT);
        date.setText(DhikrHistoryStore.today().equals(record.sessionDate)
                ? "آج کا ریکارڈ"
                : "تاریخ: " + DhikrHistoryStore.displayDate(record.sessionDate));
        date.setTextColor(getColor(R.color.text_dark));
        date.setTextSize(14);
        card.addView(date);

        TextView action = new TextView(this);
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        actionParams.topMargin = dp(4);
        action.setLayoutParams(actionParams);
        action.setGravity(Gravity.RIGHT);
        action.setText("اسی جگہ سے ذکر جاری رکھنے کے لیے ٹیپ کریں");
        action.setTextColor(getColor(R.color.text_dark));
        action.setTextSize(15);
        card.addView(action);

        card.setOnClickListener(v ->
                DhikrStartHelper.resumeExisting(this, record));
        UrduFont.apply(card, this);
        return card;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
