package com.example.ojtbadaassignment14.db;

public class MovieContract {

    private  MovieContract() {
    }

    public static class MovieEntry {
        public static final String TABLE_MOVIES = "movies";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_IS_ADULT = "is_adult";
        public static final String COLUMN_IS_FAVORITE = "is_favorite";

        public static final String CREATE_MOVIES_TABLE = "CREATE TABLE " + TABLE_MOVIES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_VOTE_AVERAGE + " REAL,"
                + COLUMN_RELEASE_DATE + " TEXT,"
                + COLUMN_OVERVIEW + " TEXT,"
                + COLUMN_POSTER_PATH + " TEXT,"
                + COLUMN_IS_ADULT + " INTEGER,"
                + COLUMN_IS_FAVORITE + " INTEGER"
                + ")";

        public static final String DROP_MOVIES_TABLE = "DROP TABLE IF EXISTS " + TABLE_MOVIES;
    }

    public static class ReminderEntry {
        public static final String TABLE_REMINDERS = "reminders";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static final String CREATE_REMINDERS_TABLE = "CREATE TABLE " + TABLE_REMINDERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TIME + " INTEGER, "
                + COLUMN_MOVIE_ID + " INTEGER"
                + ")";

        public static final String DROP_REMINDERS_TABLE = "DROP TABLE IF EXISTS " + TABLE_REMINDERS;
    }
}
