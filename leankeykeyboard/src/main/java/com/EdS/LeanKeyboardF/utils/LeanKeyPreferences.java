package com.EdS.LeanKeyboardF.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class LeanKeyPreferences {
    private static final String APP_RUN_ONCE = "appRunOnce";
    private static final String BOOTSTRAP_SELECTED_LANGUAGE = "bootstrapSelectedLanguage";
    private static final String APP_KEYBOARD_INDEX = "appKeyboardIndex";
    private static final String FORCE_SHOW_KEYBOARD = "forceShowKeyboard";
    private static final String ENLARGE_KEYBOARD = "enlargeKeyboard";
    private static final String KEYBOARD_THEME = "keyboardTheme";
    public static final String THEME_DEFAULT = "Default";
    public static final String THEME_DARK = "Dark";
    public static final String THEME_DARK2 = "Dark2";
    public static final String THEME_DARK3 = "Dark3";
    public static final String THEME_LIGHT = "Light";
    public static final String THEME_SYSTEM = "System";
    public static final String THEME_DYNAMIC = "Dynamic";
    private static final String SUGGESTIONS_ENABLED = "suggestionsEnabled";
    private static final String CYCLIC_NAVIGATION_ENABLED = "cyclicNavigationEnabled";
    private static final String AUTODETECT_LAYOUT = "autodetectLayout";
    private static final String PHYSICAL_KEYBOARD_MODE = "physicalKeyboardMode";
    private static final String FLOATING_KEYBOARD = "floatingKeyboard";
    private static final String KEYBOARD_SIZE_LEVEL = "keyboardSizeLevel";
    private static final String CLIPBOARD_HISTORY = "clipboardHistory";
    private static LeanKeyPreferences sInstance;
    private final Context mContext;
    private SharedPreferences mPrefs;

    public static LeanKeyPreferences instance(Context ctx) {
        if (sInstance == null)
            sInstance = new LeanKeyPreferences(ctx);
        return sInstance;
    }

    public LeanKeyPreferences(Context context) {
        mContext = context.getApplicationContext();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public boolean isRunOnce() {
        return mPrefs.getBoolean(APP_RUN_ONCE, false);
    }

    public void setRunOnce(boolean runOnce) {
        mPrefs.edit()
                .putBoolean(APP_RUN_ONCE, runOnce)
                .apply();
    }

    public void setPreferredLanguage(String name) {
        mPrefs.edit()
                .putString(BOOTSTRAP_SELECTED_LANGUAGE, name)
                .apply();
    }

    public String getPreferredLanguage() {
        return mPrefs.getString(BOOTSTRAP_SELECTED_LANGUAGE, "");
    }

    public int getKeyboardIndex() {
        return mPrefs.getInt(APP_KEYBOARD_INDEX, 0);
    }

    public void setKeyboardIndex(int idx) {
        mPrefs.edit()
                .putInt(APP_KEYBOARD_INDEX, idx)
                .apply();
    }

    public boolean getForceShowKeyboard() {
        return mPrefs.getBoolean(FORCE_SHOW_KEYBOARD, true);
    }

    public void setForceShowKeyboard(boolean force) {
        mPrefs.edit()
                .putBoolean(FORCE_SHOW_KEYBOARD, force)
                .apply();
    }

    public boolean getEnlargeKeyboard() {
        return mPrefs.getBoolean(ENLARGE_KEYBOARD, false);
    }

    public void setEnlargeKeyboard(boolean enlarge) {
        mPrefs.edit()
                .putBoolean(ENLARGE_KEYBOARD, enlarge)
                .apply();
    }

    public void setCurrentTheme(String theme) {
        mPrefs.edit()
                .putString(KEYBOARD_THEME, theme)
                .apply();
    }

    public String getCurrentTheme() {
        return mPrefs.getString(KEYBOARD_THEME, THEME_DARK3);
    }

    public void setSuggestionsEnabled(boolean enabled) {
        mPrefs.edit()
                .putBoolean(SUGGESTIONS_ENABLED, enabled)
                .apply();
    }

    public boolean getSuggestionsEnabled() {
        return mPrefs.getBoolean(SUGGESTIONS_ENABLED, true);
    }

    public void setCyclicNavigationEnabled(boolean enabled) {
        mPrefs.edit()
                .putBoolean(CYCLIC_NAVIGATION_ENABLED, enabled)
                .apply();
    }

    public boolean isCyclicNavigationEnabled() {
        return mPrefs.getBoolean(CYCLIC_NAVIGATION_ENABLED, false);
    }

    public boolean getAutodetectLayout() {
        return mPrefs.getBoolean(AUTODETECT_LAYOUT, false);
    }

    public boolean isPhysicalKeyboardMode() {
        return mPrefs.getBoolean(PHYSICAL_KEYBOARD_MODE, false);
    }

    public void setPhysicalKeyboardMode(boolean enabled) {
        mPrefs.edit().putBoolean(PHYSICAL_KEYBOARD_MODE, enabled).apply();
    }

    public boolean isFloatingKeyboard() {
        return mPrefs.getBoolean(FLOATING_KEYBOARD, false);
    }

    public void setFloatingKeyboard(boolean enabled) {
        mPrefs.edit().putBoolean(FLOATING_KEYBOARD, enabled).apply();
    }

    /** 0 = normal, 1 = large, 2 = xlarge */
    public int getKeyboardSizeLevel() {
        return mPrefs.getInt(KEYBOARD_SIZE_LEVEL, 0);
    }

    public void setKeyboardSizeLevel(int level) {
        if (level < 0) level = 0;
        if (level > 2) level = 2;
        mPrefs.edit().putInt(KEYBOARD_SIZE_LEVEL, level).apply();
        // keep legacy enlarge flag in sync
        setEnlargeKeyboard(level > 0);
    }

    public void increaseKeyboardSize() {
        setKeyboardSizeLevel(getKeyboardSizeLevel() + 1);
    }

    public void decreaseKeyboardSize() {
        setKeyboardSizeLevel(getKeyboardSizeLevel() - 1);
    }

    // --- Clipboard buffer history (Копіювати/Вирізати feed this; the
    // "Буфер" key on the keyboard itself shows them for picking) -------
    private static final int CLIPBOARD_HISTORY_MAX_ITEMS = 5;

    public void addClipboardHistoryItem(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        java.util.List<String> history = getClipboardHistory();
        history.remove(text); // move to front if already present, no duplicates
        history.add(0, text);

        while (history.size() > CLIPBOARD_HISTORY_MAX_ITEMS) {
            history.remove(history.size() - 1);
        }

        org.json.JSONArray array = new org.json.JSONArray();
        for (String item : history) {
            array.put(item);
        }

        mPrefs.edit().putString(CLIPBOARD_HISTORY, array.toString()).apply();
    }

    public void clearClipboardHistory() {
        mPrefs.edit().remove(CLIPBOARD_HISTORY).apply();
    }

    public java.util.List<String> getClipboardHistory() {
        java.util.List<String> history = new java.util.ArrayList<>();
        String json = mPrefs.getString(CLIPBOARD_HISTORY, null);

        if (json != null) {
            try {
                org.json.JSONArray array = new org.json.JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    history.add(array.getString(i));
                }
            } catch (org.json.JSONException e) {
                // corrupted/old data - just start fresh next time something is copied
            }
        }

        return history;
    }
}
