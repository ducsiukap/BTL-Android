package com.example.ddht.ui.common;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public final class KeyboardUtils {
    private KeyboardUtils() {
    }

    public static void hideKeyboard(Activity activity) {
        if (activity == null) {
            return;
        }
        View focused = activity.getCurrentFocus();
        if (focused != null) {
            hideKeyboard(activity, focused);
            focused.clearFocus();
        }
    }

    public static void hideKeyboard(Context context, View view) {
        if (context == null || view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}