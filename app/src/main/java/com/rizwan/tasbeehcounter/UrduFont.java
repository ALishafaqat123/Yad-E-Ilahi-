package com.rizwan.tasbeehcounter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/** Applies Jameel Noori Nastaleeq to XML and dynamically-created text controls. */
public final class UrduFont {
    private static Typeface cached;

    private UrduFont() {}

    public static Typeface get(Context context) {
        if (cached == null && context != null) {
            cached = context.getResources().getFont(R.font.jameel_noori_nastaleeq_regular);
        }
        return cached;
    }

    public static void applyToActivity(Activity activity) {
        if (activity == null) return;
        apply(activity.findViewById(android.R.id.content), get(activity));
    }

    public static void applyToDialog(Dialog dialog) {
        if (dialog == null || dialog.getWindow() == null) return;
        apply(dialog.getWindow().getDecorView(), get(dialog.getContext()));
    }

    public static void apply(View view, Context context) {
        apply(view, get(context));
    }

    public static void apply(View view, Typeface typeface) {
        if (view == null || typeface == null) return;
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            int style = textView.getTypeface() == null
                    ? Typeface.NORMAL : textView.getTypeface().getStyle();
            textView.setTypeface(typeface, style);
            textView.setIncludeFontPadding(true);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                apply(group.getChildAt(i), typeface);
            }
        }
    }

    /**
     * Nastaleeq renders standalone numbers poorly, so views that show nothing but a
     * number opt out of it. Call this after {@link #applyToActivity(Activity)},
     * which would otherwise have already overwritten the typeface.
     */
    public static void useDigitFont(TextView... views) {
        if (views == null) return;
        for (TextView view : views) {
            if (view == null) continue;
            view.setTypeface(Typeface.SANS_SERIF);
            view.setFontFeatureSettings("tnum");
        }
    }
}
