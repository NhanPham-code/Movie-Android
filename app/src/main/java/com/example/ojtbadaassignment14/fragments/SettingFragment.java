package com.example.ojtbadaassignment14.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.ojtbadaassignment14.R;

public class SettingFragment extends Fragment {

    TextView lbCategory;
    TextView choiceCategory;
    TextView lbRatting;
    TextView choiceRatting;
    TextView lbReleaseYear;
    TextView choiceReleaseYear;
    TextView lbSortBy;
    TextView choiceSortBy;

    SharedPreferences sharedPreferences;

    public SettingFragment() {
        // Required empty public constructor
    }


    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences("MoviePreferences", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_setting, container, false);

        lbCategory = view.findViewById(R.id.lb_category);
        choiceCategory = view.findViewById(R.id.choice_category);
        lbRatting = view.findViewById(R.id.lb_ratting);
        choiceRatting = view.findViewById(R.id.choice_ratting);
        lbReleaseYear = view.findViewById(R.id.lb_release_year);
        choiceReleaseYear = view.findViewById(R.id.choice_release_year);
        lbSortBy = view.findViewById(R.id.lb_sort_by);
        choiceSortBy = view.findViewById(R.id.choice_sort_by);

        // Set on click listener for each text view to show dialog and get user input
        lbCategory.setOnClickListener(v -> showCategoryDialog());
        lbRatting.setOnClickListener(v -> showRatingDialog());
        lbReleaseYear.setOnClickListener(v -> showReleaseYearDialog());
        lbSortBy.setOnClickListener(v -> showSortByDialog());

        // save data from user to SharedPreferences
        loadPreferences();

        return view;
    }

    private void showCategoryDialog() {
        String[] categories = {"Popular", "Top Rated", "Upcoming", "Now Playing"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose Category")
                .setItems(categories, (dialog, which) -> {
                    choiceCategory.setText(categories[which]);
                    savePreference("category", categories[which]);
                })
                .show();
    }

    private void showRatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_seekbar, null);
        SeekBar seekBar = view.findViewById(R.id.seekBar);
        TextView seekBarValue = view.findViewById(R.id.seekBarValue);

        seekBar.setMax(10);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarValue.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        builder.setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                    choiceRatting.setText(seekBarValue.getText());
                    savePreference("rating", seekBarValue.getText().toString());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showReleaseYearDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View customLayout = getLayoutInflater().inflate(R.layout.dialog_input, null);
        builder.setView(customLayout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            TextView input = customLayout.findViewById(R.id.input);
            choiceReleaseYear.setText(input.getText().toString());
            savePreference("releaseYear", input.getText().toString());
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showSortByDialog() {
        String[] sortByOptions = {"Release Year", "Rating"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Sort By")
                .setItems(sortByOptions, (dialog, which) -> {
                    choiceSortBy.setText(sortByOptions[which]);
                    savePreference("sortBy", sortByOptions[which]);
                })
                .show();
    }

    private void savePreference(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void loadPreferences() {
        choiceCategory.setText(sharedPreferences.getString("category", "Popular"));
        choiceRatting.setText(sharedPreferences.getString("rating", "0"));
        choiceReleaseYear.setText(sharedPreferences.getString("releaseYear", "1970"));
        choiceSortBy.setText(sharedPreferences.getString("sortBy", "Release Year"));
    }
}