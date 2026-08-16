package com.EdS.LeanKeyboardF.addons.keyboards;

import android.inputmethodservice.Keyboard;
import androidx.annotation.Nullable;

public interface KeyboardBuilder {
    Keyboard createAbcKeyboard();
    Keyboard createSymKeyboard();
    Keyboard createNumKeyboard();
}
