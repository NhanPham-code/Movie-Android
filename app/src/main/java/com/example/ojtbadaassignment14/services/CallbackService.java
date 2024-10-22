package com.example.ojtbadaassignment14.services;

import com.example.ojtbadaassignment14.models.Movie;
import com.example.ojtbadaassignment14.models.Reminder;

public interface CallbackService {
    void onShowMovieDetail(Movie movie);
    void onFavoriteMovie(Movie movie);
    void backToMovieList();
    void updateReminderList();
}
