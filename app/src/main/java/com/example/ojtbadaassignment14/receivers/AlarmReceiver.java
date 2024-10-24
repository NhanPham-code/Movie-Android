package com.example.ojtbadaassignment14.receivers;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.example.ojtbadaassignment14.MainActivity;
import com.example.ojtbadaassignment14.R;
import com.example.ojtbadaassignment14.api.MovieApiService;
import com.example.ojtbadaassignment14.api.RetrofitClient;
import com.example.ojtbadaassignment14.db.DatabaseHelper;
import com.example.ojtbadaassignment14.models.Movie;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "1001";
    private static final String REMINDER_ID = "reminderId";
    private static final String REMINDER_MOVIE_ID = "movieId";

    long reminderId;
    long movieId;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("check", "onReceive: Alarm received");

        // get reminder ID to delete reminder
        reminderId = intent.getLongExtra(REMINDER_ID, -1);
        // get movie id to show on notification
        movieId = intent.getLongExtra(REMINDER_MOVIE_ID, -1);

        Log.d("check", "onReceive: reminderId = " + reminderId);
        Log.d("check", "onReceive: movieId = " + movieId);

        // Fetch movie details and push notification
        fetchMovieDetailsAndPushNotification(context, movieId);


        // Xóa reminder trong SQLite
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        Log.d("check", "onReceive: reminderId = " + reminderId);
        if (reminderId != -1) {
            databaseHelper.removeReminder(reminderId);
            Log.d("check", "onReceive: Reminder deleted");
        } else {
            Log.d("check", "Reminder ID is invalid");
        }
    }

    private void fetchMovieDetailsAndPushNotification(Context context, long movieId) {
        MovieApiService apiService = RetrofitClient.getInstance().getMovieApiService();
        Call<Movie> call = apiService.getMovieDetail(movieId, RetrofitClient.API_KEY);
        call.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(@NonNull Call<Movie> call, @NonNull Response<Movie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("check", "onResponse: Movie fetched successfully");

                    // get movie details from API
                    Movie movie = response.body();

                    // push notification
                    pushNotification(context, movie);

                    // send for receiver in MainActivity to update the reminder list in navigation view after notification is pushed
                    Intent updateIntent = new Intent("com.example.ojtbadaassignment14.UPDATE_REMINDER_LIST_MAIN");
                    context.sendBroadcast(updateIntent);

                    // send for receiver in AllReminderFragment to update the reminder list in recycler view after notification is pushed
                    Intent updateIntent1 = new Intent("com.example.ojtbadaassignment14.UPDATE_REMINDER_LIST_FRAGMENT");
                    context.sendBroadcast(updateIntent1);

                } else {
                    Log.d("check", "Failed to fetch movie details");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Movie> call, @NonNull Throwable t) {
                Log.d("check", "Error: " + t.getMessage());
            }
        });
    }

    private void pushNotification(Context context, Movie movie) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID, "Reminder Channel", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(movie.getTitle())
                .setContentText("Year: " + movie.getReleaseDate() + ", Rate: " + String.format("%.1f/10", movie.getVoteAverage()))
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.app_img))
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify((int) movieId, notification);
    }

}