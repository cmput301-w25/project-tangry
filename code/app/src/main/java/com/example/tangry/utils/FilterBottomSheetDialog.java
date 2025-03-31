package com.example.tangry.utils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tangry.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * A bottom sheet dialog for filtering emotion posts by emotion type and time.
 */
public class FilterBottomSheetDialog extends BottomSheetDialogFragment {

    public interface FilterAppliedListener {
        void onFilterApplied(List<String> selectedEmotions, boolean filterRecent);
    }

    private final List<String> currentSelectedEmotions;
    private final boolean currentFilterRecent;
    private final FilterAppliedListener listener;

    public FilterBottomSheetDialog(List<String> currentSelectedEmotions,
                                   boolean currentFilterRecent,
                                   FilterAppliedListener listener) {
        this.currentSelectedEmotions = new ArrayList<>(currentSelectedEmotions);
        this.currentFilterRecent = currentFilterRecent;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.modal_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize all checkboxes
        CheckBox happyCheckbox = view.findViewById(R.id.Happy_filter);
        CheckBox sadCheckbox = view.findViewById(R.id.Sad_filter);
        CheckBox angryCheckbox = view.findViewById(R.id.Angry_filter);
        CheckBox disgustedCheckbox = view.findViewById(R.id.Disgusted_filter);
        CheckBox fearfulCheckbox = view.findViewById(R.id.Fearful_filter);
        CheckBox surprisedCheckbox = view.findViewById(R.id.Surprised_filter);
        CheckBox confusedCheckbox = view.findViewById(R.id.Confused_filter);
        CheckBox shameCheckbox = view.findViewById(R.id.Shame_filter);
        CheckBox recentWeekCheckbox = view.findViewById(R.id.recent_week_filter);

        // Set initial checkbox states
        happyCheckbox.setChecked(currentSelectedEmotions.contains("Happiness"));
        sadCheckbox.setChecked(currentSelectedEmotions.contains("Sadness"));
        angryCheckbox.setChecked(currentSelectedEmotions.contains("Angry"));
        disgustedCheckbox.setChecked(currentSelectedEmotions.contains("Disgust"));
        fearfulCheckbox.setChecked(currentSelectedEmotions.contains("Fear"));
        surprisedCheckbox.setChecked(currentSelectedEmotions.contains("Surprise"));
        confusedCheckbox.setChecked(currentSelectedEmotions.contains("Confused"));
        shameCheckbox.setChecked(currentSelectedEmotions.contains("Shame"));

        // Set time filter
        recentWeekCheckbox.setChecked(currentFilterRecent);

        // Apply button handler
        Button applyButton = view.findViewById(R.id.apply_filter_button);
        applyButton.setOnClickListener(v -> {
            List<String> selectedEmotions = new ArrayList<>();

            if (happyCheckbox.isChecked()) selectedEmotions.add("Happiness");
            if (sadCheckbox.isChecked()) selectedEmotions.add("Sadness");
            if (angryCheckbox.isChecked()) selectedEmotions.add("Angry");
            if (disgustedCheckbox.isChecked()) selectedEmotions.add("Disgust");
            if (fearfulCheckbox.isChecked()) selectedEmotions.add("Fear");
            if (surprisedCheckbox.isChecked()) selectedEmotions.add("Surprise");
            if (confusedCheckbox.isChecked()) selectedEmotions.add("Confused");
            if (shameCheckbox.isChecked()) selectedEmotions.add("Shame");

            boolean filterRecent = recentWeekCheckbox.isChecked();

            if (listener != null) {
                listener.onFilterApplied(selectedEmotions, filterRecent);
            }

            dismiss();
        });
    }
}