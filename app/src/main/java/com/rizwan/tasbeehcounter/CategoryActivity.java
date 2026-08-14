package com.rizwan.tasbeehcounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class CategoryActivity extends Activity {
    private static final int MAX_TARGET = 1_000_000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        UrduFont.applyToActivity(this);

        String category = getIntent().getStringExtra(MainActivity.EXTRA_CATEGORY);
        if (category == null) category = DhikrCatalog.CATEGORY_ISTIGHFAR;

        TextView title = findViewById(R.id.categoryTitle);
        TextView subtitle = findViewById(R.id.categorySubtitle);
        LinearLayout container = findViewById(R.id.categoryItemsContainer);

        title.setText(DhikrCatalog.getCategoryTitle(category));
        subtitle.setText(DhikrCatalog.getCategorySubtitle(category));
        findViewById(R.id.categoryBackButton).setOnClickListener(v -> finish());

        List<DhikrItem> items = DhikrCatalog.getItems(category);
        for (DhikrItem item : items) container.addView(makeItemCard(item));
    }

    private View makeItemCard(DhikrItem item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        card.setBackgroundResource(R.drawable.mode_card_background);
        card.setClickable(false);
        card.setFocusable(false);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.topMargin = dpToPx(12);
        card.setLayoutParams(cardParams);

        TextView title = new TextView(this);
        title.setText(item.title);
        title.setTextColor(0xFF0C4625);
        title.setTextSize(23f);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setGravity(Gravity.RIGHT);
        card.addView(title);

        TextView arabic = new TextView(this);
        arabic.setText(item.arabic);
        arabic.setTextColor(0xFF2D2922);
        arabic.setTextSize(item.longText ? 18f : 20f);
        arabic.setGravity(Gravity.RIGHT);
        arabic.setLineSpacing(0f, 1.18f);
        LinearLayout.LayoutParams arabicParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        arabicParams.topMargin = dpToPx(3);
        card.addView(arabic, arabicParams);

        TextView translation = new TextView(this);
        translation.setText("ترجمہ: " + item.translation);
        translation.setTextColor(0xFF6B6255);
        translation.setTextSize(15f);
        translation.setGravity(Gravity.RIGHT);
        translation.setLineSpacing(dpToPx(2), 1.0f);
        LinearLayout.LayoutParams translationParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        translationParams.topMargin = dpToPx(5);
        card.addView(translation, translationParams);

        if (!item.explanation.isEmpty()) {
            TextView explanation = new TextView(this);
            explanation.setText("تفہیم:\n" + item.explanation);
            explanation.setTextColor(0xFF3D372F);
            explanation.setTextSize(15.5f);
            explanation.setGravity(Gravity.RIGHT);
            explanation.setLineSpacing(dpToPx(3), 1.08f);
            explanation.setBackgroundResource(R.drawable.settings_panel_background);
            explanation.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
            LinearLayout.LayoutParams explanationParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            explanationParams.topMargin = dpToPx(9);
            card.addView(explanation, explanationParams);
        }

        TextView target = new TextView(this);
        if (item.isSequence()) {
            target.setText("پانچ جملے = ایک مکمل لڑی — لڑیوں کی تعداد مقرر کریں");
        } else {
            target.setText(item.reportedTarget
                    ? "منقول تعداد: " + item.defaultTarget + " — اپنی تعداد بھی مقرر کرسکتے ہیں"
                    : "ابتدائی ہدف: " + item.defaultTarget + " — اپنی تعداد مقرر کرسکتے ہیں");
        }
        target.setTextColor(0xFF8A5E00);
        target.setTextSize(14f);
        target.setTypeface(target.getTypeface(), Typeface.BOLD);
        target.setGravity(Gravity.RIGHT);
        LinearLayout.LayoutParams targetParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        targetParams.topMargin = dpToPx(7);
        card.addView(target, targetParams);

        Button defaultButton = new Button(this);
        defaultButton.setAllCaps(false);
        if (item.isSequence()) {
            defaultButton.setText(item.title + " شروع کریں");
        } else {
            defaultButton.setText(item.reportedTarget
                    ? "منقول تعداد " + item.defaultTarget + " سے شروع کریں"
                    : "ابتدائی ہدف " + item.defaultTarget + " سے شروع کریں");
        }
        defaultButton.setTextColor(0xFFFFFFFF);
        defaultButton.setTextSize(15f);
        defaultButton.setGravity(Gravity.CENTER);
        defaultButton.setBackgroundResource(R.drawable.button_primary);
        LinearLayout.LayoutParams defaultParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        defaultParams.topMargin = dpToPx(12);
        card.addView(defaultButton, defaultParams);

        Button customButton = new Button(this);
        customButton.setAllCaps(false);
        customButton.setText(item.isSequence()
                ? "لڑیوں کی تعداد خود مقرر کریں" : getString(R.string.set_custom_target));
        customButton.setTextColor(0xFF2D2922);
        customButton.setTextSize(15f);
        customButton.setGravity(Gravity.CENTER);
        customButton.setBackgroundResource(R.drawable.button_secondary);
        LinearLayout.LayoutParams customButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        customButtonParams.topMargin = dpToPx(8);
        card.addView(customButton, customButtonParams);

        Button dailyButton = new Button(this);
        dailyButton.setAllCaps(false);
        dailyButton.setTextSize(15f);
        dailyButton.setGravity(Gravity.CENTER);
        dailyButton.setBackgroundResource(R.drawable.button_secondary);
        LinearLayout.LayoutParams dailyParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        dailyParams.topMargin = dpToPx(8);
        card.addView(dailyButton, dailyParams);
        updateDailyButton(item, dailyButton);

        Button challengeButton = new Button(this);
        challengeButton.setAllCaps(false);
        challengeButton.setText("چیلنج میں شامل کریں");
        challengeButton.setTextColor(0xFF8A5E00);
        challengeButton.setTextSize(15f);
        challengeButton.setGravity(Gravity.CENTER);
        challengeButton.setBackgroundResource(R.drawable.button_secondary);
        LinearLayout.LayoutParams challengeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        challengeParams.topMargin = dpToPx(8);
        card.addView(challengeButton, challengeParams);

        LinearLayout customPanel = makeInlineCustomPanel(item, dailyButton);
        customPanel.setVisibility(View.GONE);
        card.addView(customPanel);

        defaultButton.setOnClickListener(v -> startItem(item, item.defaultTarget));
        customButton.setOnClickListener(v -> {
            boolean opening = customPanel.getVisibility() != View.VISIBLE;
            customPanel.setVisibility(opening ? View.VISIBLE : View.GONE);
            customButton.setText(opening ? "اپنی تعداد بند کریں"
                    : (item.isSequence() ? "لڑیوں کی تعداد خود مقرر کریں"
                    : getString(R.string.set_custom_target)));
            if (opening) {
                EditText input = customPanel.findViewWithTag("targetInput");
                if (input != null) {
                    input.requestFocus();
                    input.setSelection(input.getText().length());
                }
            } else hideKeyboard(customPanel);
        });
        dailyButton.setOnClickListener(v -> {
            if (DailyRoutineStore.isIncluded(this, item.id)) {
                DailyRoutineStore.remove(this, item.id);
                Toast.makeText(this, "روزانہ کے معمول سے ہٹا دیا گیا", Toast.LENGTH_SHORT).show();
            } else {
                DailyRoutineStore.add(this, item.id, item.defaultTarget);
                Toast.makeText(this, "روزانہ کے معمول میں شامل ہوگیا", Toast.LENGTH_SHORT).show();
            }
            updateDailyButton(item, dailyButton);
        });
        challengeButton.setOnClickListener(v -> showChallengeDialog(item));

        UrduFont.apply(card, this);
        return card;
    }

    private void updateDailyButton(DhikrItem item, Button button) {
        boolean included = DailyRoutineStore.isIncluded(this, item.id);
        button.setText(included ? "روزانہ کے معمول سے ہٹائیں" : "روزانہ کے معمول میں شامل کریں");
        button.setTextColor(included ? 0xFF8A1C1C : 0xFF0C4625);
    }

    private LinearLayout makeInlineCustomPanel(DhikrItem item, Button dailyButton) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(0, dpToPx(10), 0, 0);

        TextView label = new TextView(this);
        label.setText(item.isSequence()
                ? "کتنی مکمل لڑیاں پڑھنی ہیں؟" : "اپنی مطلوبہ تعداد لکھیں");
        label.setTextColor(0xFF4E473D);
        label.setTextSize(15f);
        label.setGravity(Gravity.RIGHT);
        panel.addView(label);

        EditText input = new EditText(this);
        input.setTag("targetInput");
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(item.defaultTarget));
        input.setSelectAllOnFocus(true);
        input.setGravity(Gravity.CENTER);
        input.setTextSize(22f);
        input.setSingleLine(true);
        input.setFontFeatureSettings("tnum");
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        inputParams.topMargin = dpToPx(4);
        panel.addView(input, inputParams);

        Button startButton = new Button(this);
        startButton.setAllCaps(false);
        startButton.setText(R.string.start);
        startButton.setTextColor(0xFF000000);
        startButton.setTextSize(16f);
        startButton.setBackgroundResource(R.drawable.completion_primary_button);
        LinearLayout.LayoutParams startParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        startParams.topMargin = dpToPx(7);
        panel.addView(startButton, startParams);

        Button addDailyCustomButton = new Button(this);
        addDailyCustomButton.setAllCaps(false);
        addDailyCustomButton.setText("اسی تعداد کے ساتھ روزانہ میں شامل کریں");
        addDailyCustomButton.setTextColor(0xFF0C4625);
        addDailyCustomButton.setTextSize(15f);
        addDailyCustomButton.setBackgroundResource(R.drawable.button_secondary);
        LinearLayout.LayoutParams addDailyParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        addDailyParams.topMargin = dpToPx(7);
        panel.addView(addDailyCustomButton, addDailyParams);

        startButton.setOnClickListener(v -> {
            Integer value = parseTarget(input);
            if (value == null) {
                input.setError(getString(R.string.invalid_target));
                Toast.makeText(this, R.string.invalid_target, Toast.LENGTH_SHORT).show();
                return;
            }
            hideKeyboard(input);
            startItem(item, value);
        });
        addDailyCustomButton.setOnClickListener(v -> {
            Integer value = parseTarget(input);
            if (value == null) {
                input.setError(getString(R.string.invalid_target));
                return;
            }
            DailyRoutineStore.add(this, item.id, value);
            updateDailyButton(item, dailyButton);
            Toast.makeText(this, "یہ تعداد روزانہ کے معمول میں محفوظ ہوگئی", Toast.LENGTH_SHORT).show();
            hideKeyboard(input);
        });
        return panel;
    }

    private void showChallengeDialog(DhikrItem item) {
        LinearLayout holder = new LinearLayout(this);
        holder.setOrientation(LinearLayout.VERTICAL);
        int padding = dpToPx(20);
        holder.setPadding(padding, dpToPx(4), padding, 0);

        TextView note = new TextView(this);
        note.setText(item.isSequence()
                ? "یہاں تعداد سے مراد مکمل لڑیوں کی تعداد ہے۔ یہ ذاتی چیلنج ہدف ہوگا۔"
                : "یہ ذاتی چیلنج ہدف ہوگا؛ اسے مسنون مقرر تعداد نہیں کہا جائے گا۔");
        note.setTextColor(0xFF6F675A);
        note.setTextSize(14.5f);
        note.setGravity(Gravity.RIGHT);
        holder.addView(note);

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(item.defaultTarget));
        input.setSelectAllOnFocus(true);
        input.setGravity(Gravity.CENTER);
        input.setTextSize(22f);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        inputParams.topMargin = dpToPx(8);
        holder.addView(input, inputParams);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("چیلنج میں شامل کریں — " + item.title)
                .setView(holder)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton("چیلنج قبول کریں", null)
                .create();
        dialog.setOnShowListener(ignored -> {
            UrduFont.applyToDialog(dialog);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                Integer target = parseTarget(input);
                if (target == null) {
                    input.setError(getString(R.string.invalid_target));
                    return;
                }
                ChallengeStore.Record existing = ChallengeStore.findActiveBySource(
                        this, "dhikr:" + item.id);
                if (existing != null) {
                    Toast.makeText(this, "یہ ذکر پہلے سے ایک چیلنج کے طور پر جاری ہے",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                ChallengeStore.Record record = ChallengeStore.acceptDhikr(this, item, target);
                if (record != null) {
                    dialog.dismiss();
                    Toast.makeText(this,
                            "چیلنج قبول ہوگیا — مرکزی صفحے کے چیلنج سیکشن میں دیکھیں",
                            Toast.LENGTH_LONG).show();
                }
            });
        });
        dialog.show();
    }

    private Integer parseTarget(EditText input) {
        try {
            int value = Integer.parseInt(input.getText().toString().trim());
            return value >= 1 && value <= MAX_TARGET ? value : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void startItem(DhikrItem item, int target) {
        int roundTarget = item.isSequence() ? target : 0;
        int[] stageTargets = item.isSequence() ? item.sequenceTargets : null;
        DhikrStartHelper.start(this, item.id, item.isSequence() ? 0 : target, roundTarget,
                item.title, item.arabic, item.translation, item.sourceNote, item.longText,
                stageTargets, item.sequencePhrases, item.sequenceTranslations);
    }

    private void hideKeyboard(View view) {
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
