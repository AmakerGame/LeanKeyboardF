package com.EdS.LeanKeyboardF.fragments.settings;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.GuidanceStylist.Guidance;
import com.EdS.LeanKeyboardF.utils.LeanKeyPreferences;
import com.EdS.LeanKeyboardF.R;

public class KbKeyboardFragment extends BaseSettingsFragment {
    private LeanKeyPreferences mPrefs;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mPrefs = LeanKeyPreferences.instance(getActivity());

        addCheckedAction(R.string.physical_keyboard_mode, R.string.physical_keyboard_mode_desc,
                mPrefs::isPhysicalKeyboardMode, mPrefs::setPhysicalKeyboardMode);
        addCheckedAction(R.string.floating_keyboard, R.string.floating_keyboard_desc,
                mPrefs::isFloatingKeyboard, mPrefs::setFloatingKeyboard);

        // Current size is shown as a plain info row (not a checkbox - it
        // isn't a toggle), with Increase/Decrease as separate button-like
        // rows right below it.
        String current = getSizeLabel();
        addInfoAction(
                getString(R.string.keyboard_size) + " (" + current + ")",
                getString(R.string.keyboard_size_desc, current)
        );
        addNextAction(R.string.increase_size, () -> mPrefs.increaseKeyboardSize());
        addNextAction(R.string.decrease_size, () -> mPrefs.decreaseKeyboardSize());
    }

    private String getSizeLabel() {
        int level = mPrefs.getKeyboardSizeLevel();
        switch (level) {
            case 1: return getString(R.string.size_large);
            case 2: return getString(R.string.size_xlarge);
            default: return getString(R.string.size_normal);
        }
    }

    @NonNull
    @Override
    public Guidance onCreateGuidance(Bundle savedInstanceState) {
        String title = getActivity().getResources().getString(R.string.keyboard_settings);
        String desc = getActivity().getResources().getString(R.string.keyboard_settings_desc);
        Drawable icon = ContextCompat.getDrawable(getActivity(), R.drawable.ic_launcher);

        return new Guidance(
                title,
                desc,
                "",
                icon
        );
    }
}
