package com.example.ojtbadaassignment14.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ojtbadaassignment14.R;
import com.example.ojtbadaassignment14.api.MovieApiService;
import com.example.ojtbadaassignment14.api.RetrofitClient;
import com.example.ojtbadaassignment14.db.DatabaseHelper;
import com.example.ojtbadaassignment14.models.Movie;
import com.example.ojtbadaassignment14.models.Reminder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NavReminderAdapter extends RecyclerView.Adapter<NavReminderAdapter.ReminderViewHolder> {

    private List<Reminder> reminderList;

    public NavReminderAdapter(List<Reminder> reminderList) {
        this.reminderList = reminderList;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nav_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        // Get reminder in list
        Reminder reminder = reminderList.get(position);

        // Fetch movie details from API
        MovieApiService apiService = RetrofitClient.getInstance().getMovieApiService();
        Call<Movie> call = apiService.getMovieDetail(reminder.getMovieId());
        call.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(@NonNull Call<Movie> call, @NonNull Response<Movie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Movie movie = response.body();
                    holder.tvMovieInfo.setText(movie.getTitle() + " - " + movie.getReleaseDate().substring(0, 4) + " - " + String.format("%.1f", movie.getVoteAverage()));
                    holder.tvReminderInfo.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(reminder.getTime())));
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Movie> call, @NonNull Throwable t) {
                Toast.makeText(holder.itemView.getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieInfo;
        TextView tvReminderInfo;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovieInfo = itemView.findViewById(R.id.tv_movie_info);
            tvReminderInfo = itemView.findViewById(R.id.tv_reminder_info);
        }
    }
}
