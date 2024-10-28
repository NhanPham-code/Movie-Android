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

    @GET("movie/{category}?api_key=e7631ffcb8e766993e5ec0c1f4245f93")
    Call<Page> getMoviesByCategory(@Path("category") String category, @Query("page") int page);

    @GET("movie/popular?api_key=e7631ffcb8e766993e5ec0c1f4245f93")
    Call<Page> getPopularMovies(@Query("page") int page);

    @GET("movie/top_rated?api_key=e7631ffcb8e766993e5ec0c1f4245f93")
    Call<Page> getTopRatedMovies(@Query("page") int page);

    @GET("movie/upcoming?api_key=e7631ffcb8e766993e5ec0c1f4245f93")
    Call<Page> getUpcomingMovies(@Query("page") int page);

    @GET("movie/now_playing?api_key=e7631ffcb8e766993e5ec0c1f4245f93")
    Call<Page> getNowPlayingMovies(@Query("page") int page);

    @GET("movie/{movieId}/credits?api_key=e7631ffcb8e766993e5ec0c1f4245f93")
    Call<CastOfMovie> getMovieCast(@Path("movieId") long movieId);

    @GET("movie/{movieId}?api_key=e7631ffcb8e766993e5ec0c1f4245f93")
    Call<Movie> getMovieDetail(@Path("movieId") long movieId);
}
