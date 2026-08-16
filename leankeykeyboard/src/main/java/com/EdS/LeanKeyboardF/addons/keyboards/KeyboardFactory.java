package com.EdS.LeanKeyboardF.addons.keyboards;

import android.content.Context;

import java.util.List;

public interface KeyboardFactory {
    List<? extends KeyboardBuilder> getAllAvailableKeyboards(Context context);
    boolean needUpdate();
}
