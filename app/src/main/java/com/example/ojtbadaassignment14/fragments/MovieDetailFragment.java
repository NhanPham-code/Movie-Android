package com.example.ojtbadaassignment14.fragments;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ojtbadaassignment14.R;
import com.example.ojtbadaassignment14.adapters.CastAdapter;
import com.example.ojtbadaassignment14.api.MovieApiService;
import com.example.ojtbadaassignment14.api.RetrofitClient;
import com.example.ojtbadaassignment14.db.DatabaseHelper;
import com.example.ojtbadaassignment14.models.Cast;
import com.example.ojtbadaassignment14.models.CastOfMovie;
import com.example.ojtbadaassignment14.models.Movie;
import com.example.ojtbadaassignment14.models.Page;
import com.example.ojtbadaassignment14.models.Reminder;
import com.example.ojtbadaassignment14.receivers.AlarmReceiver;
import com.example.ojtbadaassignment14.services.CallbackService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MovieDetailFragment extends Fragment {

    private static final int POST_NOTIFICATIONS_PERMISSION_CODE = 100;
    private static final String REMINDER_ID = "reminderId";
    private static  final  String REMINDER_MOVIE_ID = "movieId";

    CallbackService callbackService;

    private Movie movie;
    private List<Cast> castList;

    ImageView ivFavorite;
    TextView tvReleaseDate;
    TextView tvRating;
    ImageView ivMoviePoster;
    TextView tvOverview;
    Button btnReminder;

    private RecyclerView recyclerView;
    private CastAdapter castAdapter;

    DatabaseHelper databaseHelper;

    public MovieDetailFragment() {
    }


    public static MovieDetailFragment newInstance(Movie movie) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("movie", movie);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.movie = getArguments().getParcelable("movie");
        }
        castList = new ArrayList<>();
        databaseHelper = new DatabaseHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("check", "onCreateView: detail");
        View view = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        // init views
        ivFavorite = view.findViewById(R.id.iv_favorite);
        tvReleaseDate = view.findViewById(R.id.tv_release_date);
        tvRating = view.findViewById(R.id.tv_rating);
        ivMoviePoster = view.findViewById(R.id.iv_movie_poster);
        tvOverview = view.findViewById(R.id.tv_overview);
        recyclerView = view.findViewById(R.id.recycler_view);
        btnReminder = view.findViewById(R.id.btn_reminder);

        // fill data
        tvReleaseDate.setText(movie.getReleaseDate());
        tvRating.setText(String.format("%.1f/10", movie.getVoteAverage()));
        Picasso picasso = Picasso.get();
        picasso.load(RetrofitClient.IMAGE_BASE_URL + movie.getPosterPath()).into(ivMoviePoster);
        tvOverview.setText(movie.getOverview());
        if(movie.getIsFavorite() == 0) {
            ivFavorite.setImageResource(R.drawable.ic_star);
        } else {
            ivFavorite.setImageResource(R.drawable.ic_star_favorite);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        fetchMovieCast(); // fetch movie cast

        // set listener for favorite button
        ivFavorite.setOnClickListener(v -> {
            if(movie.getIsFavorite() == 0) {
                ivFavorite.setImageResource(R.drawable.ic_star_favorite);
                // callback Main activity to update favorite list
                callbackService.onFavoriteMovie(movie);
                Toast.makeText(getActivity(), "Added to favorite", Toast.LENGTH_SHORT).show();
            } else {
                ivFavorite.setImageResource(R.drawable.ic_star);
                // callback Main activity to update favorite list
                callbackService.onFavoriteMovie(movie);
                Toast.makeText(getActivity(), "Removed from favorite", Toast.LENGTH_SHORT).show();
            }
        });

        // set listener for reminder button
        btnReminder.setOnClickListener(v -> {
            // check Movie is already in reminder list
            Reminder reminder = databaseHelper.getReminderByMovieId(movie.getId());
            if(reminder != null) {
                Toast.makeText(getActivity(), "Reminder already set", Toast.LENGTH_SHORT).show();
                return;
            }
            // Show date time picker
            showDateTimePicker();
        });

        return view;
    }

    /**
     * Set callback service
     * @param callbackService
     */
    public void setCallbackService(CallbackService callbackService) {
        this.callbackService = callbackService;
    }

    /**
     * Update movie detail
     *
     * @param movie: movie object with updated details
     */
    public void updateMovieDetail(Movie movie) {
        // Update the movie object
        this.movie = movie;

        // Update the views
        if(movie.getIsFavorite() == 0) {
            ivFavorite.setImageResource(R.drawable.ic_star);
        } else {
            ivFavorite.setImageResource(R.drawable.ic_star_favorite);
        }
    }

    /**
     * Show date time picker
     */
    private void showDateTimePicker() {

        // check permission to show notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission(Manifest.permission.POST_NOTIFICATIONS, POST_NOTIFICATIONS_PERMISSION_CODE);
        }

        // Get current date and time
        Calendar calendar = Calendar.getInstance();

        // Date Picker
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_DialogWhenLarge_NoActionBar,
                (view, year, month, dayOfMonth) -> {
                    calendar.setTimeZone(TimeZone.getDefault());
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Time Picker
                    TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_DialogWhenLarge_NoActionBar,
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);
                                calendar.set(Calendar.MILLISECOND, 0);

                                // get selected time
                                long selectedTimeInMillis = calendar.getTimeInMillis();

                                // check if selected time is in the future
                                if (selectedTimeInMillis <= System.currentTimeMillis()) {
                                    Toast.makeText(getActivity(), "Please choose a future time", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Save to SQLite and get reminder id
                                Reminder newReminder = new Reminder();
                                newReminder.setTime(selectedTimeInMillis);
                                newReminder.setMovieId(movie.getId());

                                long idOfReminder = databaseHelper.addReminder(newReminder);
                                newReminder.setId(idOfReminder);
                                Log.d("check", "showDateTimePicker: reminder id: " + newReminder.getId());

                                // Set alarm with id of reminder to delete reminder later
                                scheduleAlarm(newReminder);

                                // Callback Main activity update reminder list after add reminder
                                callbackService.updateReminderList();

                                Toast.makeText(getActivity(), "Reminder set successfully", Toast.LENGTH_SHORT).show();

                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                    timePickerDialog.show();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }


    private void scheduleAlarm(Reminder reminder) {

        Log.d("check", "scheduleAlarm: time: " + reminder.getTime() + ", id: " + reminder.getId() + ", movieId: " + reminder.getMovieId());

        // Get the AlarmManager service
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        // Create the intent for the alarm receiver
        Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        intent.putExtra(REMINDER_ID, reminder.getId());
        intent.putExtra(REMINDER_MOVIE_ID, reminder.getMovieId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), (int) reminder.getId(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);

        // Set the alarm
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.getTime(), pendingIntent);
    }

    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
        }
    }


    /**
     * Fetch movie cast
     */
    private void fetchMovieCast() {
        MovieApiService apiService = RetrofitClient.getInstance().getMovieApiService();
        Call<CastOfMovie> call = apiService.getMovieCast(movie.getId(), RetrofitClient.API_KEY);
        call.enqueue(new Callback<CastOfMovie>() {
            @Override
            public void onResponse(@NonNull Call<CastOfMovie> call, @NonNull Response<CastOfMovie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    castList.addAll(response.body().getCastList());
                    castAdapter = new CastAdapter(castList);
                    recyclerView.setAdapter(castAdapter);
                } else {
                    Toast.makeText(getContext(), "Failed to fetch cast", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CastOfMovie> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }
}