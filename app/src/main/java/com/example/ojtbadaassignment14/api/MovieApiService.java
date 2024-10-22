package com.example.ojtbadaassignment14.api;

import com.example.ojtbadaassignment14.models.CastOfMovie;
import com.example.ojtbadaassignment14.models.Movie;
import com.example.ojtbadaassignment14.models.Page;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MovieApiService {
    // https://api.themoviedb.org/3/movie/popular?api_key=e7631ffcb8e766993e5ec0c1f4245f93&page=1

    // detail: api.themoviedb.org/3/movie/{movieId}?api_key=e7631ffcb8e766993e5ec0c1f4245f93

    // cast: api.themoviedb.org/3/movie/{movieId}/credits?api_key=e7631ffcb8e766993e5ec0c1f4245f93

    @GET("movie/popular")
    Call<Page> getPopularMovies(@Query("api_key") String apiKey, @Query("page") int page);

    @GET("movie/top_rated")
    Call<Page> getTopRatedMovies(@Query("api_key") String apiKey, @Query("page") int page);

    @GET("movie/upcoming")
    Call<Page> getUpcomingMovies(@Query("api_key") String apiKey, @Query("page") int page);

    @GET("movie/now_playing")
    Call<Page> getNowPlayingMovies(@Query("api_key" ) String apiKey, @Query("page") int page);

    @GET("movie/{movieId}/credits")
    Call<CastOfMovie> getMovieCast(@Path("movieId") int movieId, @Query("api_key") String apiKey);

    @GET("movie/{movieId}")
    Call<Movie> getMovieDetail(@Path("movieId") long movieId, @Query("api_key") String apiKey);
}
