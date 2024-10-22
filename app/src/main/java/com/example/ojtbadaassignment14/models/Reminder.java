package com.example.ojtbadaassignment14.models;

public class Reminder {
    private long id;
    private long time;
    private long movieId;

    public Reminder() {
    }

    public Reminder(long id, long time, long movieId) {
        this.id = id;
        this.time = time;
        this.movieId = movieId;
    }

    public long getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public long getMovieId() {
        return movieId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setMovieId(long movieId) {
        this.movieId = movieId;
    }
}
