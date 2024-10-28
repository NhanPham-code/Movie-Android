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
    private boolean isLoadingMoreData = false; // to check if data is loading

    SharedPreferences sharedPreferences;
    SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;


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
        sharedPreferences = getActivity().getSharedPreferences("MoviePreferences", Context.MODE_PRIVATE);

        // Register preference change listener to update movie list if there is any change in user preferences settings
        registerPreferenceChangeListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("check", "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_movie_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.idPBLoading);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        movieListAdapter = new MovieListAdapter(movieList, callbackService);
        recyclerView.setAdapter(movieListAdapter);


        // Load movie list based on setting save into share preferences
        getMovieListByCategoryFromSharePreferences();


        // Set on scroll listener and load more data when the end of the list is near
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (lastVisibleItem == totalItemCount - 1 && !isLoadingMoreData) {
                        Log.d("checked", "onScrolled: load more data, totalItemCount: " + totalItemCount + ", lastVisibleItem: " + lastVisibleItem);

                        // set loading status to true to prevent loading more data when the end of the list is near
                        isLoadingMoreData = true;

                        movieListAdapter.setLoading(true);

                        currentPage++;

                        // load more data based on the current category
                        getMovieListByCategoryFromSharePreferences();
                    }
                } else if (layoutManager instanceof GridLayoutManager) {
                    GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
                    int totalItemCount = gridLayoutManager.getItemCount();
                    int lastVisibleItem = gridLayoutManager.findLastVisibleItemPosition();
                    if (lastVisibleItem == totalItemCount - 1 && !isLoadingMoreData) {
                        Log.d("checked", "onScrolled: load more data, totalItemCount: " + totalItemCount + ", lastVisibleItem: " + lastVisibleItem);

                        isLoadingMoreData = true;

                        movieListAdapter.setLoading(true);

                        currentPage++;

                        // load more data based on the current category
                        getMovieListByCategoryFromSharePreferences();
                    }
                }

            }
        });


        // Set on refresh listener
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // clear movie list
                movieList.clear();

                // reset current page
                currentPage = 1;

                // set loading status to false
                isLoadingMoreData = false;

                // load more data based on the current category
                getMovieListByCategoryFromSharePreferences();

                // stop refreshing
                swipeRefreshLayout.setRefreshing(false);
            }
        });


        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }


    /**
     * Update movie list if there is any change in user preferences on Settings
     */
    private void registerPreferenceChangeListener() {
        preferenceChangeListener = (sharedPreferences, key) -> {
            // Clear the current movie list and reset the current page
            currentPage = 1;

            // Set loading status to false
            isLoadingMoreData = false;

            // Load movie list based on the updated preferences
            getMovieListByCategoryFromSharePreferences();
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }


    /**
     * Set callback service
     * @param callbackService: callback service
     */
    public void setCallbackService(CallbackService callbackService) {
        this.callbackService = callbackService;
    }


    /**
     * Filter movie list by category, rating, release year, and sort by from user setting
     * and update movie list
     */
    private void getMovieListByCategoryFromSharePreferences() {
        // Retrieve preferences
        String category = sharedPreferences.getString("category", "Popular");

        // get movie list by category
        switch (category) {
            case "Popular":
                //getPopularMovieList(currentPage);
                getMoviesByCategoryFromAPI("popular", currentPage);
                break;
            case "Top Rated":
                //getTopRatedMovieList(currentPage);
                getMoviesByCategoryFromAPI("top_rated", currentPage);
                break;
            case "Upcoming":
                //getUpcomingMovieList(currentPage);
                getMoviesByCategoryFromAPI("upcoming", currentPage);
                break;
            case "Now Playing":
                //getNowPlayingMovieList(currentPage);
                getMoviesByCategoryFromAPI("now_playing", currentPage);
                break;
        }

    }


    /**
     * Get movies by category from API
     * @param category : category of movies
     * @param currentPage : current page
     */
    public void getMoviesByCategoryFromAPI(String category, int currentPage) {

        RetrofitClient retrofitClient = RetrofitClient.getInstance();
        MovieApiService movieApiService = retrofitClient.getMovieApiService();

        Call<Page> call = movieApiService.getMoviesByCategory(category, currentPage);
        call.enqueue(new Callback<Page>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<Page> call, @NonNull Response<Page> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // get page
                    page = response.body();

                    // get movie list
                    if (page != null) {
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

                        // filter and sort movie list
                        filterAndSortMovieList(newMovies);

                        progressBar.setVisibility(View.GONE);

                        isLoadingMoreData = false;

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
     * Filter and sort movie list based on user preferences
     * @param newMovieList: new movie list to filter and sort
     */
    private void filterAndSortMovieList(List<Movie> newMovieList) {
        String rating = sharedPreferences.getString("rating", "0");
        String releaseYear = sharedPreferences.getString("releaseYear", "1970");
        String sortBy = sharedPreferences.getString("sortBy", "Release Year");

        // filter movie list by rating, release year
        List<Movie> filteredMovies = new ArrayList<>();
        for (Movie movie : newMovieList) {
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

        if(isLoadingMoreData) {
            int startPosition = movieList.size();
            Log.d("checked", "filterAndSortMovieList: movieList size: " + movieList.size() + "filteredMovies size: " + filteredMovies.size());
            movieList.addAll(filteredMovies);
            movieListAdapter.setLoading(false);
            movieListAdapter.notifyItemRangeInserted(startPosition, movieList.size());
        } else {
            movieList.clear();
            movieList.addAll(filteredMovies);
            movieListAdapter.setLoading(false);
            movieListAdapter.notifyDataSetChanged();
        }
    }


    /**
     * Load movies by category for option menu
     */
    public void loadMovieListByCategory(String category) {
        currentPage = 1;
        movieList.clear();
        switch (category) {
            case "popular":
                //getPopularMovieList(currentPage);
                getMoviesByCategoryFromAPI("popular", currentPage);
                break;
            case "top_rated":
                //getTopRatedMovieList(currentPage);
                getMoviesByCategoryFromAPI("top_rated", currentPage);
                break;
            case "upcoming":
                //getUpcomingMovieList(currentPage);
                getMoviesByCategoryFromAPI("upcoming", currentPage);
                break;
            case "now_playing":
                //getNowPlayingMovieList(currentPage);
                getMoviesByCategoryFromAPI("now_playing", currentPage);
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