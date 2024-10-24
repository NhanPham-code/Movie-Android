package com.example.ojtbadaassignment14.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ojtbadaassignment14.R;
import com.example.ojtbadaassignment14.adapters.MovieListAdapter;
import com.example.ojtbadaassignment14.api.MovieApiService;
import com.example.ojtbadaassignment14.api.RetrofitClient;
import com.example.ojtbadaassignment14.db.DatabaseHelper;
import com.example.ojtbadaassignment14.models.Movie;
import com.example.ojtbadaassignment14.models.Page;
import com.example.ojtbadaassignment14.services.CallbackService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MovieListFragment extends Fragment {

    // interface
    CallbackService callbackService;

    private RecyclerView recyclerView;
    private MovieListAdapter movieListAdapter;
    ProgressBar progressBar;
    SwipeRefreshLayout swipeRefreshLayout;

    // data
    private Page page;
    List<Movie> movieList;
    private int currentPage = 1;
    private boolean isLoading = false; // to check if data is loading


    public MovieListFragment() {

    }

    public static MovieListFragment newInstance() {
        MovieListFragment fragment = new MovieListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("check", "onCreate: ");
        movieList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("check", "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_movie_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.idPBLoading);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);


        // Set up the recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        movieListAdapter = new MovieListAdapter(movieList, callbackService);
        recyclerView.setAdapter(movieListAdapter);

        // get popular movie list in default
        //getPopularMovieList(currentPage);

        // Load movie list based on setting save into share preferences
        getMovieListByCategoryFromSharePreferences();

        // set listener for RecyclerView to load more data when near the end of the list
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isLoading && isNearEndOfList()) {
                    Log.d("checked", "onScrolled: load more data");
                    // set loading status to check point is loading more data and avoid loading more data at the same time
                    isLoading = true;

                    // increase current page
                    currentPage++;

                    // load more data
                    //getPopularMovieList(++currentPage);

                    // load more data based on the current category
                    getMovieListByCategoryFromSharePreferences();
                }

            }
        });

        // listener for swipe refresh layout to refresh data
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // clear movie list
                movieList.clear();

                // reset current page
                currentPage = 1;

                // get popular movie list
                //getPopularMovieList(currentPage);

                // load more data based on the current category
                getMovieListByCategoryFromSharePreferences();

                // stop refreshing
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }


    /**
     * Filter movie list by category, rating, release year, and sort by from user setting
     * and update movie list
     */
    private void getMovieListByCategoryFromSharePreferences() {
        // Retrieve preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MoviePreferences", Context.MODE_PRIVATE);
        String category = sharedPreferences.getString("category", "Popular");

        // get movie list by category
        switch (category) {
            case "Popular":
                getPopularMovieList(currentPage);
                break;
            case "Top Rated":
                getTopRatedMovieList(currentPage);
                break;
            case "Upcoming":
                getUpcomingMovieList(currentPage);
                break;
            case "Now Playing":
                getNowPlayingMovieList(currentPage);
                break;
        }

    }

    /**
     * Filter and sort movie list by rating, release year, and sort by from user setting
     */
    private void filterAndSortMovieList() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MoviePreferences", Context.MODE_PRIVATE);
        String rating = sharedPreferences.getString("rating", "0");
        String releaseYear = sharedPreferences.getString("releaseYear", "1970");
        String sortBy = sharedPreferences.getString("sortBy", "Release Year");

        // filter movie list by rating, release year
        List<Movie> filteredMovies = new ArrayList<>();
        for (Movie movie : movieList) {
            if (movie.getVoteAverage() >= Double.parseDouble(rating) &&
                    Integer.parseInt(movie.getReleaseDate().substring(0, 4)) >= Integer.parseInt(releaseYear)) {
                filteredMovies.add(movie);
            }
        }

        // sort movie list
        if (sortBy.equals("Release Year")) {
            filteredMovies.sort((o1, o2) -> o2.getReleaseDate().compareTo(o1.getReleaseDate()));
        } else {
            filteredMovies.sort((o1, o2) -> Double.compare(o2.getVoteAverage(), o1.getVoteAverage()));
        }

        // update movie list
        movieList.clear();
        movieList.addAll(filteredMovies);
        movieListAdapter.notifyDataSetChanged();
    }

    /**
     * Set callback service
     * @param callbackService: callback service
     */
    public void setCallbackService(CallbackService callbackService) {
        this.callbackService = callbackService;
    }


    /**
     * Check if the end of the list is near
     * @return: true if the end of the list is near, false otherwise
     */
    private boolean isNearEndOfList() {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            int totalItemCount = linearLayoutManager.getItemCount();
            int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
            Log.d("checked", "isNearEndOfList: " + totalItemCount + " " + lastVisibleItem);
            return totalItemCount <= (lastVisibleItem + 2);
        } else if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            int totalItemCount = gridLayoutManager.getItemCount();
            int lastVisibleItem = gridLayoutManager.findLastVisibleItemPosition();
            Log.d("checked", "isNearEndOfList: " + totalItemCount + " " + lastVisibleItem);
            return totalItemCount <= (lastVisibleItem + 2);
        }
        return false;
    }

    /**
     * Get popular movies from API
     */
    public void getPopularMovieList(int currentPage) {
        // Hiển thị progress bar trong khi chờ tải dữ liệu
        progressBar.setVisibility(View.VISIBLE);

        // get movie list
        RetrofitClient retrofitClient = RetrofitClient.getInstance();
        MovieApiService movieApiService = retrofitClient.getMovieApiService();

        Call<Page> call = movieApiService.getPopularMovies(RetrofitClient.API_KEY,  currentPage);
        call.enqueue(new Callback<Page>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<Page> call, @NonNull Response<Page> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // get page
                    page = response.body();

                    // get movie list
                    if(page != null) {
                        List<Movie> newMovies = page.getResults();

                        // Get favorite movies from the database
                        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
                        List<Movie> favoriteMovies = dbHelper.getAllFavoriteMovies();

                        // Update favorite status for each movie
                        for (Movie movie : newMovies) {
                            for (Movie favorite : favoriteMovies) {
                                if (movie.getId() == favorite.getId()) {
                                    movie.setIsFavorite(1);  // Set as favorite
                                    break;
                                }
                            }
                        }

                        movieList.addAll(newMovies);
                        //movieListAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);

                        // set loading status to false after loading data for next time loading
                        isLoading = false;

                        // filter and sort movie list after loading data from API
                        filterAndSortMovieList();

                        Log.d("check", "onResponse: " + movieList.size());
                        Log.d("check", "onResponse: " + movieList.get(0).getTitle());

                    } else {
                        showError();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Page> call, @NonNull Throwable t) {
                showError();
            }
        });
    }

    /**
     * Get top-rated movies from API
     */
    public void getTopRatedMovieList(int currentPage) {
        // Hiển thị progress bar trong khi chờ tải dữ liệu
        progressBar.setVisibility(View.VISIBLE);

        // get movie list
        RetrofitClient retrofitClient = RetrofitClient.getInstance();
        MovieApiService movieApiService = retrofitClient.getMovieApiService();

        Call<Page> call = movieApiService.getTopRatedMovies(RetrofitClient.API_KEY,  currentPage);
        call.enqueue(new Callback<Page>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<Page> call, @NonNull Response<Page> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // get page
                    page = response.body();

                    // get movie list
                    if(page != null) {

                        List<Movie> newMovies = page.getResults();

                        // Get favorite movies from the database
                        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
                        List<Movie> favoriteMovies = dbHelper.getAllFavoriteMovies();

                        // Update favorite status for each movie
                        for (Movie movie : newMovies) {
                            for (Movie favorite : favoriteMovies) {
                                if (movie.getId() == favorite.getId()) {
                                    movie.setIsFavorite(1);  // Set as favorite
                                    break;
                                }
                            }
                        }

                        movieList.addAll(newMovies);
                        //movieListAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);

                        // set loading status to false after loading data for next time loading
                        isLoading = false;

                        // filter and sort movie list after loading data from API
                        filterAndSortMovieList();

                        Log.d("check", "onResponse: " + movieList.size());
                        Log.d("check", "onResponse: " + movieList.get(0).getTitle());

                    } else {
                        showError();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Page> call, @NonNull Throwable t) {
                showError();
            }
        });
    }

    /**
     * Get upcoming movies from API
     */
    public void getUpcomingMovieList(int currentPage) {
        // Hiển thị progress bar trong khi chờ tải dữ liệu
        progressBar.setVisibility(View.VISIBLE);

        // get movie list
        RetrofitClient retrofitClient = RetrofitClient.getInstance();
        MovieApiService movieApiService = retrofitClient.getMovieApiService();

        Call<Page> call = movieApiService.getUpcomingMovies(RetrofitClient.API_KEY,  currentPage);
        call.enqueue(new Callback<Page>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<Page> call, @NonNull Response<Page> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // get page
                    page = response.body();

                    // get movie list
                    if(page != null) {

                        List<Movie> newMovies = page.getResults();

                        // Get favorite movies from the database
                        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
                        List<Movie> favoriteMovies = dbHelper.getAllFavoriteMovies();

                        // Update favorite status for each movie
                        for (Movie movie : newMovies) {
                            for (Movie favorite : favoriteMovies) {
                                if (movie.getId() == favorite.getId()) {
                                    movie.setIsFavorite(1);  // Set as favorite
                                    break;
                                }
                            }
                        }

                        movieList.addAll(newMovies);
                        //movieListAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);

                        // set loading status to false after loading data for next time loading
                        isLoading = false;

                        // filter and sort movie list after loading data from API
                        filterAndSortMovieList();

                        Log.d("check", "onResponse: " + movieList.size());
                        Log.d("check", "onResponse: " + movieList.get(0).getTitle());

                    } else {
                        showError();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Page> call, @NonNull Throwable t) {
                showError();
            }
        });
    }

    /**
     * Get now playing movies from API
     */
    public void getNowPlayingMovieList(int currentPage) {
        // Hiển thị progress bar trong khi chờ tải dữ liệu
        progressBar.setVisibility(View.VISIBLE);

        // get movie list
        RetrofitClient retrofitClient = RetrofitClient.getInstance();
        MovieApiService movieApiService = retrofitClient.getMovieApiService();

        Call<Page> call = movieApiService.getNowPlayingMovies(RetrofitClient.API_KEY,  currentPage);
        call.enqueue(new Callback<Page>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<Page> call, @NonNull Response<Page> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // get page
                    page = response.body();

                    // get movie list
                    if(page != null) {

                        List<Movie> newMovies = page.getResults();

                        // Get favorite movies from the database
                        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
                        List<Movie> favoriteMovies = dbHelper.getAllFavoriteMovies();

                        // Update favorite status for each movie
                        for (Movie movie : newMovies) {
                            for (Movie favorite : favoriteMovies) {
                                if (movie.getId() == favorite.getId()) {
                                    movie.setIsFavorite(1);  // Set as favorite
                                    break;
                                }
                            }
                        }

                        movieList.addAll(newMovies);
                        //movieListAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);

                        // set loading status to false after loading data for next time loading
                        isLoading = false;

                        // filter and sort movie list after loading data from API
                        filterAndSortMovieList();

                        Log.d("check", "onResponse: " + movieList.size());
                        Log.d("check", "onResponse: " + movieList.get(0).getTitle());

                    } else {
                        showError();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Page> call, @NonNull Throwable t) {
                showError();
            }
        });
    }


    /**
     * Load movies by category
     */
    public void loadMovieListByCategory(String category) {
        currentPage = 1;
        movieList.clear();
        switch (category) {
            case "popular":
                getPopularMovieList(currentPage);
                break;
            case "top_rated":
                getTopRatedMovieList(currentPage);
                break;
            case "upcoming":
                getUpcomingMovieList(currentPage);
                break;
            case "now_playing":
                getNowPlayingMovieList(currentPage);
                break;
        }
        movieListAdapter.notifyDataSetChanged();
    }

    private void showError() {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
    }

    /**
     * Change layout of movie list
     * @param isGridLayout: true if grid layout, false if linear layout
     */
    public void changeLayout(boolean isGridLayout) {
        if (isGridLayout) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        movieListAdapter.setGridLayout(isGridLayout);
        recyclerView.setAdapter(movieListAdapter);
    }

    /**
     * Update movie list show
     * @param movie: movie to update favorite status
     */
    public void updateMovieListShow(Movie movie) {
        for (int i = 0; i < movieList.size(); i++) {
            if (movieList.get(i).getId() == movie.getId()) {
                movieList.set(i, movie);
                movieListAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

}