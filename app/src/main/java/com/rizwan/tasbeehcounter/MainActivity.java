package com.rizwan.tasbeehcounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {
    public static final String PREFS_NAME = "tasbeeh_prefs";
    public static final String KEY_COUNT = "count";
    public static final String KEY_VIBRATION = "vibration";
    public static final String KEY_DIM_MODE = "dim_mode";
    public static final String KEY_COMPLETION_SOUND = "completion_sound";

    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_TARGET = "target";
    public static final String EXTRA_STAGE_TARGETS = "stage_targets";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_PHRASE = "phrase";
    public static final String EXTRA_TRANSLATION = "translation";
    public static final String EXTRA_SOURCE_NOTE = "source_note";
    public static final String EXTRA_LONG_TEXT = "long_text";
    public static final String EXTRA_CATEGORY = "category";
    public static final String EXTRA_SEQUENCE_PHRASES = "sequence_phrases";
    public static final String EXTRA_SEQUENCE_TRANSLATIONS = "sequence_translations";
    public static final String EXTRA_SEQUENCE_ROUND_TARGET = "sequence_round_target";
    public static final String EXTRA_SESSION_DATE = "session_date";

    public static final String MODE_SIMPLE = "simple";
    public static final String MODE_POST_PRAYER = "post_prayer";
    public static final String MODE_SUBHAN_BIHAMDIHI = "subhan_bihamdihi";
    public static final String MODE_ISTIGHFAR = "istighfar";
    public static final String MODE_TAWHID = "tawhid";

    private static final int MAX_TARGET = 1_000_000;

    private SharedPreferences preferences;
    private Switch vibrationSwitch;
    private Switch soundSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UrduFont.applyToActivity(this);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        vibrationSwitch = findViewById(R.id.vibrationSwitch);
        soundSwitch = findViewById(R.id.soundSwitch);
        Button vibrationTestButton = findViewById(R.id.vibrationTestButton);

        vibrationSwitch.setChecked(preferences.getBoolean(KEY_VIBRATION, true));
        soundSwitch.setChecked(preferences.getBoolean(KEY_COMPLETION_SOUND, true));

        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferences.edit().putBoolean(KEY_VIBRATION, isChecked).apply());
        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferences.edit().putBoolean(KEY_COMPLETION_SOUND, isChecked).apply());

        vibrationTestButton.setOnClickListener(v -> {
            if (!HapticHelper.hasVibrator(this)) {
                Toast.makeText(this, R.string.no_vibrator, Toast.LENGTH_LONG).show();
                return;
            }
            preferences.edit().putBoolean(KEY_VIBRATION, true).apply();
            vibrationSwitch.setChecked(true);
            boolean sent = HapticHelper.test(this, vibrationTestButton);
            Toast.makeText(this,
                    sent ? R.string.vibration_test_message : R.string.vibration_test_failed,
                    Toast.LENGTH_LONG).show();
        });

        findViewById(R.id.dailyRoutineCard).setOnClickListener(v ->
                startActivity(new Intent(this, DailyRoutineActivity.class)));
        findViewById(R.id.challengeCard).setOnClickListener(v ->
                startActivity(new Intent(this, ChallengeActivity.class)));
        findViewById(R.id.historyCard).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));
        findViewById(R.id.dailyDuasCategoryCard).setOnClickListener(v ->
                openCategory(DhikrCatalog.CATEGORY_DAILY_DUAS));
        findViewById(R.id.postPrayerCard).setOnClickListener(v -> showPostPrayerChoice());
        findViewById(R.id.istighfarCategoryCard).setOnClickListener(v ->
                openCategory(DhikrCatalog.CATEGORY_ISTIGHFAR));
        findViewById(R.id.tasbeehCategoryCard).setOnClickListener(v ->
                openCategory(DhikrCatalog.CATEGORY_TASBEEH));
        findViewById(R.id.tawhidCategoryCard).setOnClickListener(v ->
                openCategory(DhikrCatalog.CATEGORY_TAWHID));
        findViewById(R.id.simpleCard).setOnClickListener(v -> startSimpleMode());
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.view.View resumeCard = findViewById(R.id.resumeCard);
        TextView resumeTitle = findViewById(R.id.resumeTitle);
        TextView resumeProgress = findViewById(R.id.resumeProgress);
        List<LastSessionStore.Record> sessions = LastSessionStore.getIncompleteSessions(this);
        boolean hasSessions = !sessions.isEmpty();
        resumeCard.setVisibility(hasSessions ? android.view.View.VISIBLE : android.view.View.GONE);
        if (hasSessions) {
            resumeTitle.setText("ادھورے اذکار جاری رکھیں — " + sessions.size());
            resumeProgress.setText(makeResumeSummary(sessions));
            resumeCard.setOnClickListener(v ->
                    startActivity(new Intent(this, IncompleteSessionsActivity.class)));
        } else {
            resumeCard.setOnClickListener(null);
        }
    }

    private String makeResumeSummary(List<LastSessionStore.Record> sessions) {
        StringBuilder text = new StringBuilder();
        int shown = Math.min(2, sessions.size());
        for (int i = 0; i < shown; i++) {
            LastSessionStore.Record record = sessions.get(i);
            if (text.length() > 0) text.append("  •  ");
            text.append(record.title)
                    .append(" ")
                    .append(LastSessionStore.getProgress(this, record));
        }
        if (sessions.size() > shown) {
            text.append("  •  اور ").append(sessions.size() - shown).append(" مزید");
        }
        return text.toString();
    }

    private void openCategory(String category) {
        Intent intent = new Intent(this, CategoryActivity.class);
        intent.putExtra(EXTRA_CATEGORY, category);
        startActivity(intent);
    }

    private void startSimpleMode() {
        Intent intent = new Intent(this, TasbeehActivity.class);
        intent.putExtra(EXTRA_MODE, MODE_SIMPLE);
        intent.putExtra(EXTRA_SESSION_DATE, DhikrHistoryStore.today());
        intent.putExtra(EXTRA_TARGET, 0);
        intent.putExtra(EXTRA_TITLE, getString(R.string.simple_counter_title));
        intent.putExtra(EXTRA_PHRASE, getString(R.string.simple_counter_phrase));
        intent.putExtra(EXTRA_TRANSLATION, getString(R.string.simple_counter_translation));
        intent.putExtra(EXTRA_SOURCE_NOTE, "Personal free counter; no source attached.");
        intent.putExtra(EXTRA_LONG_TEXT, false);
        startActivity(intent);
    }

    private void showPostPrayerChoice() {
        String[] options = new String[]{
                getString(R.string.post_prayer_default_option),
                getString(R.string.post_prayer_custom_option)
        };
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.post_prayer_title)
                .setItems(options, (choiceDialog, which) -> {
                    if (which == 0) {
                        startPostPrayer(new int[]{33, 33, 34});
                    } else {
                        showCustomSequenceDialog();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        dialog.setOnShowListener(ignored -> UrduFont.applyToDialog(dialog));
        dialog.show();
    }

    private void showCustomSequenceDialog() {
        LinearLayout holder = new LinearLayout(this);
        holder.setOrientation(LinearLayout.VERTICAL);
        int padding = dpToPx(22);
        holder.setPadding(padding, dpToPx(4), padding, 0);

        EditText subhan = addLabeledInput(holder, R.string.subhanallah_arabic, 33);
        EditText alhamdulillah = addLabeledInput(holder, R.string.alhamdulillah_arabic, 33);
        EditText allahuAkbar = addLabeledInput(holder, R.string.allahu_akbar_arabic, 34);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.custom_sequence_title)
                .setMessage(R.string.custom_sequence_message)
                .setView(holder)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.start, null)
                .create();

        dialog.setOnShowListener(ignored -> {
            UrduFont.applyToDialog(dialog);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    Integer first = parseTarget(subhan);
                    Integer second = parseTarget(alhamdulillah);
                    Integer third = parseTarget(allahuAkbar);
                    if (first == null) {
                        subhan.setError(getString(R.string.invalid_target));
                        return;
                    }
                    if (second == null) {
                        alhamdulillah.setError(getString(R.string.invalid_target));
                        return;
                    }
                    if (third == null) {
                        allahuAkbar.setError(getString(R.string.invalid_target));
                        return;
                    }
                    dialog.dismiss();
                    startPostPrayer(new int[]{first, second, third});
                });
        });
        dialog.show();
    }

    private void startPostPrayer(int[] stageTargets) {
        DhikrStartHelper.start(this, MODE_POST_PRAYER, 0, 0,
                getString(R.string.post_prayer_title),
                getString(R.string.subhanallah_arabic),
                getString(R.string.subhanallah_translation),
                "Riyad as-Salihin 1420 / related hadith. Verify exact source before public release.",
                false, stageTargets,
                new String[]{getString(R.string.subhanallah_arabic),
                        getString(R.string.alhamdulillah_arabic),
                        getString(R.string.allahu_akbar_arabic)},
                new String[]{getString(R.string.subhanallah_translation),
                        "تمام تعریف اللہ ہی کے لیے ہے۔",
                        "اللہ سب سے بڑا ہے۔"});
    }

    private EditText addLabeledInput(LinearLayout holder, int labelRes, int value) {
        TextView label = new TextView(this);
        label.setText(labelRes);
        label.setTextSize(20f);
        label.setTextColor(0xFF1C1C1C);
        label.setGravity(Gravity.RIGHT);
        UrduFont.apply(label, this);
        holder.addView(label, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        EditText input = makeNumberInput(value);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dpToPx(10);
        holder.addView(input, params);
        return input;
    }

    private EditText makeNumberInput(int value) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(value));
        input.setSelectAllOnFocus(true);
        input.setGravity(Gravity.CENTER);
        input.setTextSize(22f);
        input.setSingleLine(true);
        input.setFontFeatureSettings("tnum");
        UrduFont.apply(input, this);
        return input;
    }

    private Integer parseTarget(EditText input) {
        try {
            int value = Integer.parseInt(input.getText().toString().trim());
            return value >= 1 && value <= MAX_TARGET ? value : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
