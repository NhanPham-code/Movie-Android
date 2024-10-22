package com.example.ojtbadaassignment14.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.ojtbadaassignment14.models.Movie;
import com.example.ojtbadaassignment14.models.Reminder;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 3;


    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create movies table
        db.execSQL(MovieContract.MovieEntry.CREATE_MOVIES_TABLE);

        // Create reminders table
        db.execSQL(MovieContract.ReminderEntry.CREATE_REMINDERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(MovieContract.MovieEntry.DROP_MOVIES_TABLE);
        db.execSQL(MovieContract.ReminderEntry.DROP_REMINDERS_TABLE);
        onCreate(db);
    }

    public void addFavoriteMovie(Movie movie) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_ID, movie.getId());
        values.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
        values.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
        values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
        values.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
        values.put(MovieContract.MovieEntry.COLUMN_IS_ADULT, movie.isAdult() ? 1 : 0);
        values.put(MovieContract.MovieEntry.COLUMN_IS_FAVORITE, movie.getIsFavorite());

        db.insert(MovieContract.MovieEntry.TABLE_MOVIES, null, values);
        db.close();
    }

    public void removeFavoriteMovie(long movieId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MovieContract.MovieEntry.TABLE_MOVIES, MovieContract.MovieEntry.COLUMN_ID + " = ?", new String[]{String.valueOf(movieId)});
        db.close();
    }

    public List<Movie> getAllFavoriteMovies() {
        List<Movie> movieList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + MovieContract.MovieEntry.TABLE_MOVIES, null);

        if (cursor.moveToFirst()) {
            do {
                Movie movie = new Movie();
                movie.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_ID)));
                movie.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_TITLE)));
                movie.setVoteAverage(cursor.getFloat(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE)));
                movie.setReleaseDate(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_RELEASE_DATE)));
                movie.setOverview(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_OVERVIEW)));
                movie.setPosterPath(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_POSTER_PATH)));
                movie.setAdult(cursor.getInt(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_IS_ADULT)) == 1);
                movie.setIsFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_IS_FAVORITE)));

                movieList.add(movie);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return movieList;
    }

    public List<Movie> searchFavoriteMovies(String search) {
        List<Movie> movieList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String queryDefault = "SELECT * FROM " + MovieContract.MovieEntry.TABLE_MOVIES + " WHERE " + MovieContract.MovieEntry.COLUMN_TITLE + " LIKE ?";
        String querySearchKey = "SELECT * FROM " + MovieContract.MovieEntry.TABLE_MOVIES + " WHERE " + MovieContract.MovieEntry.COLUMN_TITLE + " LIKE ?";

        Cursor cursor;
        if(search.isEmpty()){
            cursor = db.rawQuery(queryDefault, new String[]{"%" + search + "%"});
        } else {
            cursor = db.rawQuery(querySearchKey, new String[]{"%" + search + "%"});
        }


        if (cursor.moveToFirst()) {
            do {
                Movie movie = new Movie();
                movie.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_ID)));
                movie.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_TITLE)));
                movie.setVoteAverage(cursor.getFloat(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE)));
                movie.setReleaseDate(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_RELEASE_DATE)));
                movie.setOverview(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_OVERVIEW)));
                movie.setPosterPath(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_POSTER_PATH)));
                movie.setAdult(cursor.getInt(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_IS_ADULT)) == 1);
                movie.setIsFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_IS_FAVORITE)));

                movieList.add(movie);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return movieList;
    }

    public Movie getMovieById(long movieId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + MovieContract.MovieEntry.TABLE_MOVIES + " WHERE " + MovieContract.MovieEntry.COLUMN_ID + " = ?", new String[]{String.valueOf(movieId)});

        Movie movie = new Movie();
        if (cursor.moveToFirst()) {
            movie.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_ID)));
            movie.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_TITLE)));
            movie.setVoteAverage(cursor.getFloat(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE)));
            movie.setReleaseDate(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_RELEASE_DATE)));
            movie.setOverview(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_OVERVIEW)));
            movie.setPosterPath(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_POSTER_PATH)));
            movie.setAdult(cursor.getInt(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_IS_ADULT)) == 1);
            movie.setIsFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_IS_FAVORITE)));
        }

        cursor.close();
        db.close();
        return movie;
    }



    public long addReminder(Reminder reminder) {
        long idOfInsertedRow = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MovieContract.ReminderEntry.COLUMN_TIME, reminder.getTime());
        values.put(MovieContract.ReminderEntry.COLUMN_MOVIE_ID, reminder.getMovieId());

        idOfInsertedRow = db.insert(MovieContract.ReminderEntry.TABLE_REMINDERS, null, values);
        db.close();

        return idOfInsertedRow;
    }

    public void removeReminder(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MovieContract.ReminderEntry.TABLE_REMINDERS, MovieContract.ReminderEntry.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<Reminder> getAllReminders() {
        List<Reminder> reminderList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + MovieContract.ReminderEntry.TABLE_REMINDERS, null);

        if (cursor.moveToFirst()) {
            do {
                Reminder reminder = new Reminder();
                reminder.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MovieContract.ReminderEntry.COLUMN_ID)));
                reminder.setTime(cursor.getLong(cursor.getColumnIndexOrThrow(MovieContract.ReminderEntry.COLUMN_TIME)));
                reminder.setMovieId(cursor.getInt(cursor.getColumnIndexOrThrow(MovieContract.ReminderEntry.COLUMN_MOVIE_ID)));

                reminderList.add(reminder);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return reminderList;
    }

}
