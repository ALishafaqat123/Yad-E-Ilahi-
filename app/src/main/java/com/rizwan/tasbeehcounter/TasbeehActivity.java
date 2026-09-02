package com.rizwan.tasbeehcounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Locale;

public class TasbeehActivity extends Activity {
    private static final long LONG_PRESS_MS = 850L;
    private static final long CONTROLS_HIDE_MS = 4200L;
    private static final int BOTTOM_GESTURE_EXCLUSION_DP = 80;

    private static final String[] POST_PRAYER_PHRASES = new String[]{
            "سُبْحَانَ اللّٰهِ",
            "اَلْحَمْدُ لِلّٰهِ",
            "اَللّٰهُ أَكْبَرُ"
    };
    private static final String[] POST_PRAYER_TRANSLATIONS = new String[]{
            "اللہ ہر عیب سے پاک ہے۔",
            "تمام تعریفیں اللہ ہی کے لیے ہیں۔",
            "اللہ سب سے بڑا ہے۔"
    };

    private SharedPreferences preferences;
    private FrameLayout counterTapArea;
    private FrameLayout ringContainer;
    private LinearLayout topBar;
    private LinearLayout controlPanel;
    private TextView countText;
    private TextView statusText;
    private TextView hintText;
    private TextView modeTitleText;
    private TextView phraseText;
    private TextView translationText;
    private TextView progressText;
    private ProgressRingView progressRing;
    private Button dimButton;
    private Button vibrationButton;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private String mode;
    private String sessionDate;
    private String displayTitle;
    private String displayPhrase;
    private String displayTranslation;
    @SuppressWarnings("unused")
    private String hiddenSourceNote;
    private boolean longText;
    private int target;
    private int sequenceRoundTarget;
    private int completedRounds;
    private int[] stageTargets;
    private String[] sequencePhrases;
    private String[] sequenceTranslations;
    private int stageIndex;
    private int count;
    private boolean dimMode;
    private boolean vibrationEnabled;
    private boolean completionSoundEnabled;
    private boolean controlsVisible = true;
    private boolean longPressTriggered;
    private boolean gestureMoved;
    private boolean activityResumed;
    private boolean windowFocused;
    private boolean stageTransition;
    private boolean completionDialogShowing;
    private boolean allowBeyondTarget;
    private float downX;
    private float downY;
    private int activePointerId = MotionEvent.INVALID_POINTER_ID;
    private int touchSlopPx;
    private int bottomGestureExclusionPx;

    private final Runnable longPressRunnable = () -> {
        if (!canCount() || gestureMoved) return;
        longPressTriggered = true;
        showControls();
        if (vibrationEnabled) HapticHelper.confirmation(this, counterTapArea);
    };
    private final Runnable hideControlsRunnable = this::hideControls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        configureWindow();
        setContentView(R.layout.activity_tasbeeh);
        UrduFont.applyToActivity(this);

        preferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        mode = getIntent().getStringExtra(MainActivity.EXTRA_MODE);
        if (mode == null) mode = MainActivity.MODE_SIMPLE;
        sessionDate = safeExtra(MainActivity.EXTRA_SESSION_DATE, DhikrHistoryStore.today());
        target = getIntent().getIntExtra(MainActivity.EXTRA_TARGET, 0);
        sequenceRoundTarget = getIntent().getIntExtra(MainActivity.EXTRA_SEQUENCE_ROUND_TARGET, 0);
        stageTargets = getIntent().getIntArrayExtra(MainActivity.EXTRA_STAGE_TARGETS);
        sequencePhrases = getIntent().getStringArrayExtra(MainActivity.EXTRA_SEQUENCE_PHRASES);
        sequenceTranslations = getIntent().getStringArrayExtra(MainActivity.EXTRA_SEQUENCE_TRANSLATIONS);
        displayTitle = safeExtra(MainActivity.EXTRA_TITLE, getString(R.string.simple_counter_title));
        displayPhrase = safeExtra(MainActivity.EXTRA_PHRASE, getString(R.string.simple_counter_phrase));
        displayTranslation = safeExtra(MainActivity.EXTRA_TRANSLATION, "");
        hiddenSourceNote = safeExtra(MainActivity.EXTRA_SOURCE_NOTE, "");
        longText = getIntent().getBooleanExtra(MainActivity.EXTRA_LONG_TEXT, false);

        if (MainActivity.MODE_POST_PRAYER.equals(mode)) {
            if (stageTargets == null || stageTargets.length != 3) {
                stageTargets = new int[]{33, 33, 34};
            }
            sequencePhrases = POST_PRAYER_PHRASES;
            sequenceTranslations = POST_PRAYER_TRANSLATIONS;
        } else if (stageTargets != null) {
            boolean validSequence = sequencePhrases != null && sequenceTranslations != null
                    && stageTargets.length > 0
                    && stageTargets.length == sequencePhrases.length
                    && stageTargets.length == sequenceTranslations.length;
            if (!validSequence) {
                stageTargets = null;
                sequencePhrases = null;
                sequenceTranslations = null;
            }
        }

        dimMode = preferences.getBoolean(MainActivity.KEY_DIM_MODE, false);
        vibrationEnabled = preferences.getBoolean(MainActivity.KEY_VIBRATION, true);
        completionSoundEnabled = preferences.getBoolean(MainActivity.KEY_COMPLETION_SOUND, true);
        touchSlopPx = ViewConfiguration.get(this).getScaledTouchSlop();
        bottomGestureExclusionPx = dpToPx(BOTTOM_GESTURE_EXCLUSION_DP);

        counterTapArea = findViewById(R.id.counterTapArea);
        ringContainer = findViewById(R.id.ringContainer);
        topBar = findViewById(R.id.topBar);
        controlPanel = findViewById(R.id.controlPanel);
        countText = findViewById(R.id.countText);
        statusText = findViewById(R.id.statusText);
        hintText = findViewById(R.id.hintText);
        modeTitleText = findViewById(R.id.modeTitleText);
        phraseText = findViewById(R.id.phraseText);
        translationText = findViewById(R.id.translationText);
        progressText = findViewById(R.id.progressText);
        progressRing = findViewById(R.id.progressRing);
        dimButton = findViewById(R.id.dimButton);
        vibrationButton = findViewById(R.id.vibrationButton);
        Button undoButton = findViewById(R.id.undoButton);
        Button resetButton = findViewById(R.id.resetButton);
        Button homeButton = findViewById(R.id.homeButton);

        applyContentSizing();
        loadModeState();
        updateInterface();
        counterTapArea.setOnTouchListener(this::handleCounterTouch);

        undoButton.setOnClickListener(v -> {
            if (!canOperateControls()) return;
            undoOne();
            showControls();
        });
        resetButton.setOnClickListener(v -> {
            if (!canOperateControls()) return;
            showResetConfirmation();
        });
        dimButton.setOnClickListener(v -> {
            if (!canOperateControls()) return;
            dimMode = !dimMode;
            saveState();
            applyDimMode();
            if (vibrationEnabled) HapticHelper.tap(this, dimButton);
            showControls();
        });
        vibrationButton.setOnClickListener(v -> {
            if (!canOperateControls()) return;
            vibrationEnabled = !vibrationEnabled;
            preferences.edit().putBoolean(MainActivity.KEY_VIBRATION, vibrationEnabled).apply();
            updateVibrationButton();
            if (vibrationEnabled) HapticHelper.test(this, vibrationButton);
            showControls();
        });
        homeButton.setOnClickListener(v -> finish());
        showControls();
    }

    private String safeExtra(String key, String fallback) {
        String value = getIntent().getStringExtra(key);
        return value == null ? fallback : value;
    }

    @Override protected void onResume() {
        super.onResume();
        activityResumed = true;
        completionSoundEnabled = preferences.getBoolean(MainActivity.KEY_COMPLETION_SOUND, true);
        hideSystemBars();
        showControls();
    }

    @Override protected void onPause() {
        activityResumed = false;
        cancelPendingTouches();
        handler.removeCallbacks(hideControlsRunnable);
        saveState();
        super.onPause();
    }

    @Override protected void onStop() {
        activityResumed = false;
        cancelPendingTouches();
        saveState();
        super.onStop();
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        windowFocused = hasFocus;
        if (hasFocus) hideSystemBars(); else cancelPendingTouches();
    }

    @Override public void onBackPressed() {
        if (!controlsVisible) showControls(); else finish();
    }

    private boolean handleCounterTouch(View view, MotionEvent event) {
        if (!canCount()) return false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getY() >= view.getHeight() - bottomGestureExclusionPx) {
                    cancelPendingTouches();
                    return false;
                }
                activePointerId = event.getPointerId(0);
                downX = event.getX();
                downY = event.getY();
                gestureMoved = false;
                longPressTriggered = false;
                handler.postDelayed(longPressRunnable, LONG_PRESS_MS);
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                gestureMoved = true;
                handler.removeCallbacks(longPressRunnable);
                return true;
            case MotionEvent.ACTION_MOVE:
                int pointerIndex = event.findPointerIndex(activePointerId);
                if (pointerIndex < 0) {
                    gestureMoved = true;
                    handler.removeCallbacks(longPressRunnable);
                    return true;
                }
                float dx = event.getX(pointerIndex) - downX;
                float dy = event.getY(pointerIndex) - downY;
                if (dx * dx + dy * dy > touchSlopPx * touchSlopPx) {
                    gestureMoved = true;
                    handler.removeCallbacks(longPressRunnable);
                }
                return true;
            case MotionEvent.ACTION_UP:
                handler.removeCallbacks(longPressRunnable);
                boolean isRealTap = !longPressTriggered && !gestureMoved
                        && event.getPointerCount() == 1 && canCount();
                activePointerId = MotionEvent.INVALID_POINTER_ID;
                if (isRealTap) incrementCount();
                return true;
            case MotionEvent.ACTION_CANCEL:
                cancelPendingTouches();
                return true;
            default:
                return false;
        }
    }

    private boolean canCount() {
        return activityResumed && windowFocused && !isFinishing()
                && !stageTransition && !completionDialogShowing;
    }

    private boolean canOperateControls() {
        return activityResumed && !isFinishing() && !stageTransition && !completionDialogShowing;
    }

    private void incrementCount() {
        if (!canCount() || count == Integer.MAX_VALUE) return;
        int currentTarget = getCurrentTarget();
        if (currentTarget > 0 && count >= currentTarget && !allowBeyondTarget) {
            handleTargetReached();
            return;
        }
        count++;
        saveState();
        updateCounterUi();
        animateCount(true);
        if (vibrationEnabled) HapticHelper.tap(this, counterTapArea);
        scheduleControlsHide();
        if (currentTarget > 0 && count == currentTarget && !allowBeyondTarget) {
            handleTargetReached();
        }
    }

    private void handleTargetReached() {
        if (isMultiStage() && stageIndex < stageTargets.length - 1) {
            moveToNextStage();
            return;
        }
        if (isRoundSequence()) {
            completedRounds++;
            count = 0;
            if (completedRounds >= sequenceRoundTarget) {
                stageIndex = stageTargets.length - 1;
                saveState();
                showCompletionDialog();
            } else {
                stageIndex = 0;
                saveState();
                updateInterface();
                if (vibrationEnabled) HapticHelper.stageComplete(this, counterTapArea);
                if (completionSoundEnabled) CompletionSoundHelper.stageComplete();
                Toast.makeText(this, "ایک لڑی مکمل — اگلی لڑی شروع کریں", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        showCompletionDialog();
    }

    private void moveToNextStage() {
        stageTransition = true;
        if (vibrationEnabled) HapticHelper.stageComplete(this, counterTapArea);
        if (completionSoundEnabled) CompletionSoundHelper.stageComplete();
        String finishedPhrase = sequencePhrases[stageIndex];
        String nextPhrase = sequencePhrases[stageIndex + 1];
        Toast.makeText(this,
                getString(R.string.stage_completed_toast, finishedPhrase, nextPhrase),
                Toast.LENGTH_LONG).show();
        handler.postDelayed(() -> {
            stageIndex++;
            count = 0;
            stageTransition = false;
            saveState();
            updateInterface();
            animateCount(false);
        }, isRoundSequence() ? 300L : 900L);
    }

    private void showCompletionDialog() {
        if (completionDialogShowing) return;
        completionDialogShowing = true;
        if (vibrationEnabled) HapticHelper.fullComplete(this, counterTapArea);
        if (completionSoundEnabled) CompletionSoundHelper.fullComplete();
        DailyRoutineStore.markCompleted(this, mode);
        ChallengeStore.markCompletedIfChallenge(this, mode);
        DhikrHistoryStore.markCompleted(this, sessionDate, mode, displayTitle,
                count, getCurrentTarget(), stageIndex,
                isMultiStage() ? stageTargets.length : 0,
                completedRounds, sequenceRoundTarget);
        LastSessionStore.markCompleted(this, mode);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.mashallah)
                .setMessage(String.format(Locale.US, getString(R.string.completion_message),
                        getCompletionName(), getCompletionCount()))
                .setCancelable(false)
                .setPositiveButton(R.string.finish_and_reset, (d, which) -> {
                    resetProgress();
                    completionDialogShowing = false;
                    finish();
                })
                .setNegativeButton(R.string.continue_counting, (d, which) -> {
                    if (isRoundSequence()) {
                        sequenceRoundTarget = Math.max(sequenceRoundTarget + 1, completedRounds + 1);
                        stageIndex = 0;
                        count = 0;
                        allowBeyondTarget = false;
                    } else {
                        allowBeyondTarget = true;
                    }
                    completionDialogShowing = false;
                    saveState();
                    updateInterface();
                    showControls();
                })
                .create();
        dialog.setOnShowListener(d -> styleCompletionDialog(dialog));
        dialog.setOnDismissListener(d -> completionDialogShowing = false);
        dialog.show();
    }

    private void styleCompletionDialog(AlertDialog dialog) {
        UrduFont.applyToDialog(dialog);
        Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (positive != null) {
            positive.setTextColor(Color.BLACK);
            positive.setBackgroundTintList(ColorStateList.valueOf(0xFFFFD166));
            positive.setAllCaps(false);
            positive.setPadding(dpToPx(14), 0, dpToPx(14), 0);
        }
        if (negative != null) {
            negative.setTextColor(Color.WHITE);
            negative.setBackgroundTintList(ColorStateList.valueOf(0xFF3B3B3B));
            negative.setAllCaps(false);
            negative.setPadding(dpToPx(14), 0, dpToPx(14), 0);
        }
    }

    private void undoOne() {
        if (count > 0) count--;
        else if (isMultiStage() && stageIndex > 0) {
            stageIndex--;
            count = Math.max(0, stageTargets[stageIndex] - 1);
        } else if (isRoundSequence() && completedRounds > 0) {
            completedRounds--;
            stageIndex = stageTargets.length - 1;
            count = Math.max(0, stageTargets[stageIndex] - 1);
        } else return;
        allowBeyondTarget = false;
        saveState();
        updateInterface();
        animateCount(false);
        if (vibrationEnabled) HapticHelper.tap(this, counterTapArea);
    }

    private void showResetConfirmation() {
        completionDialogShowing = true;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.reset_title)
                .setMessage(R.string.reset_current_mode_message)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, (d, which) -> {
                    resetProgress();
                    updateInterface();
                })
                .create();
        dialog.setOnShowListener(d -> styleCompletionDialog(dialog));
        dialog.setOnDismissListener(d -> {
            completionDialogShowing = false;
            showControls();
        });
        dialog.show();
    }

    private void resetProgress() {
        count = 0;
        stageIndex = 0;
        completedRounds = 0;
        allowBeyondTarget = false;
        saveState();
        LastSessionStore.clear(this, mode);
    }

    private void updateInterface() {
        updateModeText();
        updateCounterUi();
        applyDimMode();
        updateVibrationButton();
    }

    private void updateModeText() {
        if (isMultiStage()) {
            modeTitleText.setText(displayTitle);
            phraseText.setText(sequencePhrases[stageIndex]);
            translationText.setText("ترجمہ: " + sequenceTranslations[stageIndex]);
            if (isRoundSequence()) {
                statusText.setText(String.format(Locale.US, "لڑی %d / %d — جملہ %d / %d",
                        Math.min(sequenceRoundTarget, completedRounds + 1), sequenceRoundTarget,
                        stageIndex + 1, stageTargets.length));
            } else {
                statusText.setText(String.format(Locale.US, getString(R.string.stage_status),
                        stageIndex + 1, stageTargets.length));
            }
        } else {
            modeTitleText.setText(displayTitle);
            phraseText.setText(displayPhrase);
            translationText.setText(displayTranslation.isEmpty() ? "" : "ترجمہ: " + displayTranslation);
            statusText.setText(getCurrentTarget() > 0 ? R.string.target_mode_active : R.string.free_count_active);
        }
    }

    private void updateCounterUi() {
        countText.setText(String.format(Locale.US, "%d", count));
        int currentTarget = getCurrentTarget();
        if (currentTarget > 0) {
            progressText.setText(String.format(Locale.US, getString(R.string.progress_format),
                    count, currentTarget));
            progressRing.setCounter(count, currentTarget);
        } else {
            progressText.setText(R.string.free_count_label);
            progressRing.setCounter(count, 0);
        }
    }

    private boolean isMultiStage() {
        return stageTargets != null && sequencePhrases != null && sequenceTranslations != null
                && stageTargets.length > 0
                && stageTargets.length == sequencePhrases.length
                && stageTargets.length == sequenceTranslations.length;
    }

    private boolean isRoundSequence() {
        return sequenceRoundTarget > 0 && isMultiStage();
    }

    private int getCurrentTarget() {
        if (isMultiStage()) return stageTargets[stageIndex];
        return target;
    }

    private String getCompletionName() {
        return displayTitle;
    }

    private int getCompletionCount() {
        if (isRoundSequence()) return sequenceRoundTarget;
        if (isMultiStage()) {
            int total = 0;
            for (int value : stageTargets) total += value;
            return total;
        }
        return count;
    }

    private void applyContentSizing() {
        android.view.ViewGroup.LayoutParams params = ringContainer.getLayoutParams();
        if (longText) {
            params.width = dpToPx(210);
            params.height = dpToPx(210);
            phraseText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
            translationText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
            countText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 70f);
            hintText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        } else {
            params.width = dpToPx(270);
            params.height = dpToPx(270);
            phraseText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f);
            translationText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            countText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 88f);
        }
        ringContainer.setLayoutParams(params);
    }

    private void applyDimMode() {
        if (dimMode) {
            countText.setTextColor(0xFF777777);
            phraseText.setTextColor(0xFF777777);
            translationText.setTextColor(0xFF595959);
            modeTitleText.setTextColor(0xFF626262);
            progressText.setTextColor(0xFF626262);
            progressRing.setDimmed(true);
            dimButton.setText(R.string.bright_button);
            setBrightness(0.025f);
        } else {
            countText.setTextColor(0xFFF4F4F4);
            phraseText.setTextColor(0xFFF5F5F5);
            translationText.setTextColor(0xFFC8C0B5);
            modeTitleText.setTextColor(0xFFC4C4C4);
            progressText.setTextColor(0xFFC5C5C5);
            progressRing.setDimmed(false);
            dimButton.setText(R.string.dim_button);
            setBrightness(0.10f);
        }
    }

    private void updateVibrationButton() {
        vibrationButton.setAlpha(vibrationEnabled ? 1.0f : 0.48f);
        vibrationButton.setText(vibrationEnabled
                ? R.string.vibration_button_on : R.string.vibration_button_off);
    }

    private void animateCount(boolean increment) {
        countText.animate().cancel();
        countText.setScaleX(1f);
        countText.setScaleY(1f);
        if (increment && !dimMode) countText.setTextColor(0xFFFFD166);
        countText.animate().scaleX(1.09f).scaleY(1.09f).setDuration(70L)
                .withEndAction(() -> countText.animate()
                        .scaleX(1f).scaleY(1f).setDuration(100L)
                        .withEndAction(this::applyDimMode).start())
                .start();
    }

    private void showControls() {
        controlsVisible = true;
        topBar.setVisibility(View.VISIBLE);
        controlPanel.setVisibility(View.VISIBLE);
        hintText.setVisibility(View.VISIBLE);
        topBar.animate().alpha(1f).setDuration(180L).start();
        controlPanel.animate().alpha(1f).translationY(0f).setDuration(180L).start();
        hintText.animate().alpha(1f).setDuration(180L).start();
        scheduleControlsHide();
    }

    private void hideControls() {
        if (!canCount()) return;
        controlsVisible = false;
        topBar.animate().alpha(0f).setDuration(180L)
                .withEndAction(() -> { if (!controlsVisible) topBar.setVisibility(View.INVISIBLE); })
                .start();
        controlPanel.animate().alpha(0f).translationY(18f).setDuration(180L)
                .withEndAction(() -> { if (!controlsVisible) controlPanel.setVisibility(View.INVISIBLE); })
                .start();
        hintText.animate().alpha(0f).setDuration(180L)
                .withEndAction(() -> { if (!controlsVisible) hintText.setVisibility(View.INVISIBLE); })
                .start();
    }

    private void scheduleControlsHide() {
        handler.removeCallbacks(hideControlsRunnable);
        handler.postDelayed(hideControlsRunnable, CONTROLS_HIDE_MS);
    }

    private void cancelPendingTouches() {
        handler.removeCallbacks(longPressRunnable);
        longPressTriggered = false;
        gestureMoved = true;
        activePointerId = MotionEvent.INVALID_POINTER_ID;
    }

    private void configureWindow() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (android.os.Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
            setTurnScreenOn(false);
        }
        hideSystemBars();
    }

    private void setBrightness(float brightness) {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = brightness;
        getWindow().setAttributes(params);
    }

    private void hideSystemBars() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private void loadModeState() {
        String config = currentConfig();
        String savedConfig = preferences.getString(configKey(), null);
        if (!config.equals(savedConfig)) {
            int initialCount = MainActivity.MODE_SIMPLE.equals(mode)
                    ? preferences.getInt(MainActivity.KEY_COUNT, 0) : 0;
            count = initialCount;
            stageIndex = 0;
            completedRounds = 0;
            allowBeyondTarget = false;
            preferences.edit()
                    .putString(configKey(), config)
                    .putInt(countKey(), initialCount)
                    .putInt(stageKey(), 0)
                    .putInt(roundKey(), 0)
                    .putBoolean(continueKey(), false)
                    .apply();
            return;
        }
        count = preferences.getInt(countKey(), 0);
        stageIndex = preferences.getInt(stageKey(), 0);
        completedRounds = preferences.getInt(roundKey(), 0);
        allowBeyondTarget = preferences.getBoolean(continueKey(), false);
        if (isMultiStage()) {
            stageIndex = Math.max(0, Math.min(stageTargets.length - 1, stageIndex));
        } else stageIndex = 0;
    }

    private void saveState() {
        preferences.edit()
                .putInt(countKey(), count)
                .putInt(stageKey(), stageIndex)
                .putInt(roundKey(), completedRounds)
                .putString(configKey(), currentConfig())
                .putBoolean(continueKey(), allowBeyondTarget)
                .putBoolean(MainActivity.KEY_DIM_MODE, dimMode)
                .putBoolean(MainActivity.KEY_VIBRATION, vibrationEnabled)
                .apply();
        LastSessionStore.save(this, mode, sessionDate, target, sequenceRoundTarget,
                count, stageIndex, completedRounds, displayTitle, displayPhrase,
                displayTranslation, hiddenSourceNote, longText, stageTargets,
                sequencePhrases, sequenceTranslations);
    }

    private String currentConfig() {
        if (isRoundSequence()) return "rounds:" + sequenceRoundTarget + ":" + Arrays.toString(stageTargets);
        if (isMultiStage()) return Arrays.toString(stageTargets);
        if (MainActivity.MODE_SIMPLE.equals(mode)) return "free";
        return String.valueOf(target);
    }

    private String countKey() { return LastSessionStore.countKey(mode); }
    private String stageKey() { return LastSessionStore.stageKey(mode); }
    private String roundKey() { return LastSessionStore.roundKey(mode); }
    private String configKey() { return LastSessionStore.configKey(mode); }
    private String continueKey() { return LastSessionStore.continueKey(mode); }
    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
