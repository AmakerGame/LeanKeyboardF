package com.EdS.LeanKeyboardF.fragments.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist.Guidance;
import com.EdS.LeanKeyboardF.utils.LeanKeyPreferences;
import com.EdS.LeanKeyboardF.R;

public class KbInterfaceFragment extends BaseSettingsFragment {
    private LeanKeyPreferences mPrefs;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mPrefs = LeanKeyPreferences.instance(getActivity());

        addCheckedAction(R.string.floating_keyboard, R.string.floating_keyboard_desc,
                mPrefs::isFloatingKeyboard, this::onFloatingKeyboardToggled);

        addNextAction(R.string.change_theme, () -> startGuidedFragment(new KbThemeFragment()));

        // Current size is shown as a plain info row (not a checkbox - it
        // isn't a toggle), with Increase/Decrease as separate button-like
        // rows right below it.
        String currentSize = getSizeLabel();
        addInfoAction(
                getString(R.string.keyboard_size) + " (" + currentSize + ")",
                getString(R.string.keyboard_size_desc, currentSize)
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

    // Turning the floating keyboard OFF applies immediately, no prompt.
    // Turning it ON is gated behind a confirmation, since it's still an
    // experimental feature - Cancel reverts the checkbox back to
    // unchecked without changing the preference.
    private void onFloatingKeyboardToggled(boolean checked) {
        if (!checked) {
            mPrefs.setFloatingKeyboard(false);
            return;
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.floating_keyboard_warning_title)
                .setMessage(R.string.floating_keyboard_warning_message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    mPrefs.setFloatingKeyboard(true);
                    refreshCheckedActions();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    mPrefs.setFloatingKeyboard(false);
                    refreshCheckedActions();
                })
                .show();
    }

    @NonNull
    @Override
    public Guidance onCreateGuidance(Bundle savedInstanceState) {
        String title = getActivity().getResources().getString(R.string.interface_settings);
        String desc = getActivity().getResources().getString(R.string.interface_settings_desc);
        Drawable icon = ContextCompat.getDrawable(getActivity(), R.drawable.ic_launcher);

        return new Guidance(
                title,
                desc,
                "",
                icon
        );
    }

    private void startGuidedFragment(GuidedStepSupportFragment fragment) {
        if (getFragmentManager() != null) {
            GuidedStepSupportFragment.add(getFragmentManager(), fragment);
        }
    }
}
