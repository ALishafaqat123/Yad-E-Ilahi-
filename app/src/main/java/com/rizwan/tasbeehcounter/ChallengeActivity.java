package com.rizwan.tasbeehcounter;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

/** Full-screen challenge hub: suggested challenges, custom challenge, and accepted challenges. */
public class ChallengeActivity extends Activity {
    private static final int MAX_TARGET = 1_000_000;

    private static final class Preset {
        final String id;
        final String title;
        final String phrase;
        final String translation;
        final int target;
        Preset(String id, String title, String phrase, String translation, int target) {
            this.id = id;
            this.title = title;
            this.phrase = phrase;
            this.translation = translation;
            this.target = target;
        }
    }

    private static final List<Preset> PRESETS = Arrays.asList(
            new Preset("astaghfirullah_10000", "استغفر اللہ — دس ہزار",
                    "أَسْتَغْفِرُ اللّٰهَ", "میں اللہ سے بخشش مانگتا ہوں۔", 10000),
            new Preset("salawat_1000", "درود شریف — ایک ہزار",
                    "اللّٰهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَآلِ مُحَمَّدٍ", "", 1000),
            new Preset("tahlil_1000", "لا الٰہ الا اللہ — ایک ہزار",
                    "لَا إِلٰهَ إِلَّا اللّٰهُ", "اللہ کے سوا کوئی معبود نہیں۔", 1000),
            new Preset("hawqalah_1000", "حوقلہ — ایک ہزار",
                    "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللّٰهِ",
                    "گناہ سے بچنے اور نیکی کرنے کی طاقت اللہ ہی کی مدد سے ہے۔", 1000),
            new Preset("subhan_bihamdihi_1000", "سبحان اللہ وبحمدہ — ایک ہزار",
                    "سُبْحَانَ اللّٰهِ وَبِحَمْدِهِ",
                    "اللہ ہر عیب سے پاک ہے اور تمام تعریف اسی کے لیے ہے۔", 1000)
    );

    private LinearLayout suggestionsContainer;
    private LinearLayout acceptedContainer;
    private TextView acceptedEmpty;
    private EditText customTitle;
    private EditText customPhrase;
    private EditText customTarget;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);
        UrduFont.applyToActivity(this);

        suggestionsContainer = findViewById(R.id.challengeSuggestionsContainer);
        acceptedContainer = findViewById(R.id.acceptedChallengesContainer);
        acceptedEmpty = findViewById(R.id.acceptedChallengesEmpty);
        customTitle = findViewById(R.id.customChallengeTitle);
        customPhrase = findViewById(R.id.customChallengePhrase);
        customTarget = findViewById(R.id.customChallengeTarget);

        findViewById(R.id.challengeBackButton).setOnClickListener(v -> finish());
        findViewById(R.id.addCustomChallengeButton).setOnClickListener(v -> addCustomChallenge());
        renderSuggestions();
    }

    @Override protected void onResume() {
        super.onResume();
        renderAccepted();
        renderSuggestions();
    }

    private void renderSuggestions() {
        suggestionsContainer.removeAllViews();
        for (Preset preset : PRESETS) suggestionsContainer.addView(makePresetCard(preset));
        UrduFont.apply(suggestionsContainer, this);
    }

    private View makePresetCard(Preset preset) {
        LinearLayout card = baseCard();
        card.addView(text(preset.title, 21f, 0xFF0C4625, true));
        card.addView(text(preset.phrase, 19f, 0xFF2D2922, false));
        if (!preset.translation.isEmpty()) {
            card.addView(text("ترجمہ: " + preset.translation, 14.5f, 0xFF6B6255, false));
        }
        card.addView(text("تجویز کردہ ذاتی ہدف: " + formatNumber(preset.target),
                15.5f, 0xFF8A5E00, true));
        card.addView(text("نوٹ: یہ چیلنج کی ذاتی تعداد ہے؛ اسے مسنون مقرر تعداد نہیں کہا جارہا۔",
                13.5f, 0xFF6F675A, false));

        ChallengeStore.Record active = ChallengeStore.findActiveBySource(this, "preset:" + preset.id);
        Button accept = button(active == null
                ? "یہ چیلنج قبول کریں — " + formatNumber(preset.target)
                : "یہ چیلنج پہلے سے جاری ہے", true);
        accept.setEnabled(active == null);
        card.addView(accept, top(10));
        accept.setOnClickListener(v -> {
            ChallengeStore.Record record = ChallengeStore.acceptPreset(this, preset.id,
                    preset.title, preset.phrase, preset.translation, preset.target);
            if (record != null) {
                Toast.makeText(this, "چیلنج قبول ہوگیا", Toast.LENGTH_SHORT).show();
                renderAccepted();
                renderSuggestions();
            }
        });

        Button custom = button("تعداد خود مقرر کریں", false);
        custom.setEnabled(active == null);
        card.addView(custom, top(7));

        LinearLayout customPanel = new LinearLayout(this);
        customPanel.setOrientation(LinearLayout.VERTICAL);
        customPanel.setVisibility(View.GONE);
        EditText input = numberInput(preset.target);
        customPanel.addView(input, top(6));
        Button save = button("اس تعداد کے ساتھ چیلنج قبول کریں", true);
        customPanel.addView(save, top(6));
        card.addView(customPanel);

        custom.setOnClickListener(v -> {
            boolean open = customPanel.getVisibility() != View.VISIBLE;
            customPanel.setVisibility(open ? View.VISIBLE : View.GONE);
            custom.setText(open ? "تعداد بند کریں" : "تعداد خود مقرر کریں");
            if (open) {
                input.requestFocus();
                input.selectAll();
            } else hideKeyboard(input);
        });
        save.setOnClickListener(v -> {
            Integer target = parseTarget(input);
            if (target == null) {
                input.setError("1 سے 10 لاکھ تک درست تعداد لکھیں");
                return;
            }
            ChallengeStore.Record record = ChallengeStore.acceptPreset(this, preset.id,
                    preset.title, preset.phrase, preset.translation, target);
            if (record != null) {
                hideKeyboard(input);
                Toast.makeText(this, "چیلنج قبول ہوگیا", Toast.LENGTH_SHORT).show();
                renderAccepted();
                renderSuggestions();
            }
        });
        UrduFont.apply(card, this);
        return card;
    }

    private void addCustomChallenge() {
        String phrase = customPhrase.getText().toString().trim();
        String title = customTitle.getText().toString().trim();
        Integer target = parseTarget(customTarget);
        if (phrase.isEmpty()) {
            customPhrase.setError("ذکر کے الفاظ لکھیں");
            return;
        }
        if (target == null) {
            customTarget.setError("1 سے 10 لاکھ تک درست تعداد لکھیں");
            return;
        }
        if (title.isEmpty()) title = phrase.length() > 28 ? phrase.substring(0, 28) + "…" : phrase;
        ChallengeStore.Record record = ChallengeStore.acceptCustom(this, title, phrase, target);
        if (record != null) {
            customTitle.setText("");
            customPhrase.setText("");
            customTarget.setText("");
            hideKeyboard(customTarget);
            Toast.makeText(this, "آپ کا ذاتی چیلنج شامل ہوگیا", Toast.LENGTH_SHORT).show();
            renderAccepted();
        }
    }

    private void renderAccepted() {
        acceptedContainer.removeAllViews();
        List<ChallengeStore.Record> records = ChallengeStore.getAll(this);
        acceptedEmpty.setVisibility(records.isEmpty() ? View.VISIBLE : View.GONE);
        for (ChallengeStore.Record record : records) acceptedContainer.addView(makeAcceptedCard(record));
        UrduFont.apply(acceptedContainer, this);
    }

    private View makeAcceptedCard(ChallengeStore.Record record) {
        LinearLayout card = baseCard();
        card.addView(text(record.title, 21f, 0xFF0C4625, true));
        card.addView(text(record.phrase, record.longText ? 16.5f : 18.5f, 0xFF2D2922, false));
        String status = record.isCompleted() ? "مکمل ✓" : "جاری";
        int statusColor = record.isCompleted() ? 0xFF0A6B35 : 0xFF8A5E00;
        card.addView(text(status + " — " + ChallengeStore.progressText(this, record),
                16f, statusColor, true));
        card.addView(text("قبول کیا: " + ChallengeStore.acceptedDate(record),
                13.5f, 0xFF6F675A, false));
        if (!record.isCompleted()) {
            Button resume = button("چیلنج جاری رکھیں", true);
            card.addView(resume, top(9));
            resume.setOnClickListener(v -> ChallengeStore.start(this, record));
        }
        UrduFont.apply(card, this);
        return card;
    }

    private LinearLayout baseCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        card.setGravity(Gravity.RIGHT);
        card.setBackgroundResource(R.drawable.mode_card_background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(10);
        card.setLayoutParams(params);
        return card;
    }

    private TextView text(String value, float size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.RIGHT);
        view.setLineSpacing(dp(2), 1.03f);
        if (bold) view.setTypeface(view.getTypeface(), Typeface.BOLD);
        return view;
    }

    private Button button(String label, boolean primary) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(label);
        button.setTextSize(15f);
        button.setGravity(Gravity.CENTER);
        button.setTextColor(primary ? 0xFFFFFFFF : 0xFF2D2922);
        button.setBackgroundResource(primary ? R.drawable.button_primary : R.drawable.button_secondary);
        return button;
    }

    private EditText numberInput(int value) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(value));
        input.setGravity(Gravity.CENTER);
        input.setTextSize(21f);
        input.setSelectAllOnFocus(true);
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

    private LinearLayout.LayoutParams top(int marginDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.topMargin = dp(marginDp);
        return p;
    }

    private void hideKeyboard(View view) {
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();
    }

    private String formatNumber(int value) {
        return String.format(java.util.Locale.US, "%,d", value);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
