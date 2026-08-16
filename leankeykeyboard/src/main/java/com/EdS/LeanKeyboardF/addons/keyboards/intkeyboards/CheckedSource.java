package com.EdS.LeanKeyboardF.addons.keyboards.intkeyboards;

import java.util.List;

public interface CheckedSource {
    List<CheckedItem> getItems();

    interface CheckedItem {
        long getId();
        String getTitle();
        void onClick(boolean checked);
        boolean getChecked();
    }
}
