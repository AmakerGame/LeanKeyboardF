package com.EdS.LeanKeyboardF.fragments.settings;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.GuidanceStylist.Guidance;
import com.EdS.LeanKeyboardF.helpers.MessageHelpers;
import com.EdS.LeanKeyboardF.utils.LeanKeyPreferences;
import com.EdS.LeanKeyboardF.utils.LearningDictionary;
import com.EdS.LeanKeyboardF.R;

public class KbSuggestionsFragment extends BaseSettingsFragment {
    private LeanKeyPreferences mPrefs;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mPrefs = LeanKeyPreferences.instance(getActivity());

        addCheckedAction(R.string.enable_suggestions, R.string.enable_suggestions_desc,
                mPrefs::getSuggestionsEnabled, mPrefs::setSuggestionsEnabled);

        addCheckedAction(R.string.learn_keyboard, R.string.learn_keyboard_desc,
                mPrefs::isLearnKeyboardEnabled, mPrefs::setLearnKeyboardEnabled);

        addNextAction(R.string.clear_learned_words, this::onClearClicked);
    }

    private void onClearClicked() {
        LearningDictionary.instance(getActivity()).clear();

        MessageHelpers.showMessage(getActivity(), R.string.clear_learned_words_message);

        popBackStackToGuidedStepSupportFragment(KbSettingsFragment.class, 0);
    }

    @NonNull
    @Override
    public Guidance onCreateGuidance(Bundle savedInstanceState) {
        String title = getActivity().getResources().getString(R.string.suggestions_settings);
        String desc = getActivity().getResources().getString(R.string.suggestions_settings_desc);
        Drawable icon = ContextCompat.getDrawable(getActivity(), R.drawable.ic_launcher);

        return new Guidance(
                title,
                desc,
                "",
                icon
        );
    }
}
