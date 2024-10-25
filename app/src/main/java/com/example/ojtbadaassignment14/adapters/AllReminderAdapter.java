package com.example.ojtbadaassignment14.adapters;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ojtbadaassignment14.MainActivity;
import com.example.ojtbadaassignment14.R;
import com.example.ojtbadaassignment14.api.MovieApiService;
import com.example.ojtbadaassignment14.api.RetrofitClient;
import com.example.ojtbadaassignment14.db.DatabaseHelper;
import com.example.ojtbadaassignment14.models.Movie;
import com.example.ojtbadaassignment14.models.Reminder;
import com.example.ojtbadaassignment14.receivers.AlarmReceiver;
import com.example.ojtbadaassignment14.services.CallbackService;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllReminderAdapter extends RecyclerView.Adapter<AllReminderAdapter.AllReminderViewHolder> {

    List<Reminder> reminderList;
    DatabaseHelper databaseHelper;

    public AllReminderAdapter(List<Reminder> reminderList, DatabaseHelper databaseHelper) {
        this.reminderList = reminderList;
        this.databaseHelper = databaseHelper;
    }

    @NonNull
    @Override
    public AllReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_all_reminder, parent, false);
        return new AllReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllReminderViewHolder holder, int position) {

        Reminder reminder = reminderList.get(position);

        // Call API to get movie details
        MovieApiService apiService = RetrofitClient.getInstance().getMovieApiService();
        Call<Movie> call = apiService.getMovieDetail(reminder.getMovieId());
        call.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(@NonNull Call<Movie> call, @NonNull Response<Movie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Movie movie = response.body();
                    // SET UI
                    holder.tvMovieInfo.setText(movie.getTitle() + " - " + movie.getReleaseDate().substring(0, 4) + " - " + String.format("%.1f", movie.getVoteAverage()));
                    holder.tvReminderInfo.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(reminder.getTime())));

                    Picasso picasso = Picasso.get();
                    picasso.load(RetrofitClient.IMAGE_BASE_URL + movie.getPosterPath())
                            .placeholder(R.drawable.baseline_image_24)
                            .error(R.drawable.baseline_image_not_supported_24)
                            .into(holder.ivMoviePoster);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Movie> call, @NonNull Throwable t) {
                Toast.makeText(holder.itemView.getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        // Set delete reminder button
        holder.btnDeleteReminder.setOnClickListener(v -> {
            // Show popup menu
            PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), holder.btnDeleteReminder);
            popupMenu.getMenuInflater().inflate(R.menu.menu_delete_reminder, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.delete_reminder) {
                    // Show confirmation dialog
                    new AlertDialog.Builder(holder.itemView.getContext())
                            .setTitle("Delete Reminder")
                            .setMessage("Are you sure you want to delete this reminder?")
                            .setPositiveButton("Delete", (dialog, which) -> {

                                // Delete reminder from database
                                databaseHelper.removeReminder(reminder.getId());

                                // Remove reminder from list and notify adapter
                                reminderList.remove(position);
                                notifyItemRemoved(position);

                                // cancel alarm
                                AlarmManager alarmManager = (AlarmManager) holder.itemView.getContext().getSystemService(Context.ALARM_SERVICE);
                                Intent intent = new Intent(holder.itemView.getContext(), AlarmReceiver.class);
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(holder.itemView.getContext(), (int) reminder.getId(),
                                        intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                                if (alarmManager != null) {
                                    alarmManager.cancel(pendingIntent);
                                }

                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });


        // Set on click listener for reminder item
        holder.itemView.setOnClickListener(view -> {
            // Call API to get movie details
            MovieApiService apiService1 = RetrofitClient.getInstance().getMovieApiService();
            Call<Movie> call1 = apiService1.getMovieDetail(reminder.getMovieId());
            call1.enqueue(new Callback<Movie>() {
                @Override
                public void onResponse(@NonNull Call<Movie> call, @NonNull Response<Movie> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Movie movie = response.body();
                        // Start MainActivity with the movie object
                        Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                        intent.putExtra("movie", movie);
                        holder.itemView.getContext().startActivity(intent);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Movie> call, @NonNull Throwable t) {
                    Toast.makeText(holder.itemView.getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public static class AllReminderViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivMoviePoster;
        private final TextView tvMovieInfo;
        private final TextView tvReminderInfo;
        private final ImageButton btnDeleteReminder;


        public AllReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMoviePoster = itemView.findViewById(R.id.movie_poster);
            tvMovieInfo = itemView.findViewById(R.id.movie_info);
            tvReminderInfo = itemView.findViewById(R.id.reminder_info);
            btnDeleteReminder = itemView.findViewById(R.id.btn_remove_reminder);
        }

    }
}
