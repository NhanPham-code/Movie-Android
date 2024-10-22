package com.example.ojtbadaassignment14.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ojtbadaassignment14.R;
import com.example.ojtbadaassignment14.db.DatabaseHelper;
import com.example.ojtbadaassignment14.models.Movie;
import com.example.ojtbadaassignment14.models.Reminder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NavReminderAdapter extends RecyclerView.Adapter<NavReminderAdapter.ReminderViewHolder> {

    private List<Reminder> reminderList;
    private DatabaseHelper databaseHelper;

    public NavReminderAdapter(List<Reminder> reminderList, DatabaseHelper databaseHelper) {
        this.reminderList = reminderList;
        this.databaseHelper = databaseHelper;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nav_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminderList.get(position);
        Movie movie = databaseHelper.getMovieById(reminder.getMovieId());
        holder.tvMovieInfo.setText(movie.getTitle() + " - " + movie.getReleaseDate().substring(0, 4) + " - " + String.format("%.1f", movie.getVoteAverage()));
        holder.tvReminderInfo.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(reminder.getTime())));
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
