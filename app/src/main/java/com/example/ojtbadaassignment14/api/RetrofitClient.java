package com.example.ojtbadaassignment14.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // https://api.themoviedb.org/3/movie/popular?api_key=e7631ffcb8e766993e5ec0c1f4245f93
    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    public static final String API_KEY = "e7631ffcb8e766993e5ec0c1f4245f93";
    public static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/original";;
    private static RetrofitClient instance;
    private final Retrofit retrofit;

    private RetrofitClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public MovieApiService getMovieApiService() {
        return retrofit.create(MovieApiService.class);
    }
}
