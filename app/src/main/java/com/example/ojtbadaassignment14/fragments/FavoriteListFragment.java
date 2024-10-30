package com.example.ojtbadaassignment14.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.ojtbadaassignment14.R;
import com.example.ojtbadaassignment14.adapters.MovieListAdapter;
import com.example.ojtbadaassignment14.db.DatabaseHelper;
import com.example.ojtbadaassignment14.models.Movie;
import com.example.ojtbadaassignment14.services.CallbackService;

import java.util.ArrayList;
import java.util.List;


public class FavoriteListFragment extends Fragment {

    // interface
    CallbackService callbackService;

    private DatabaseHelper dbHelper;

    RecyclerView recyclerView;
    private MovieListAdapter movieListAdapter;
    ProgressBar progressBar;
    SwipeRefreshLayout swipeRefreshLayout;

    private List<Movie> favoriteList;

    // SharedPreferences and SharedPreferences listener
    SharedPreferences sharedPreferences;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    public FavoriteListFragment() {
        // Required empty public constructor
    }

    public static FavoriteListFragment newInstance() {
        FavoriteListFragment fragment = new FavoriteListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        favoriteList = new ArrayList<>();

        // SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        registerSharedPreferencesListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.idPBLoading);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        progressBar.setVisibility(View.GONE); // hide progress bar
        swipeRefreshLayout.setEnabled(false); // disable swipe refresh

        // Get favorite list from database
        dbHelper = new DatabaseHelper(getContext());
        favoriteList = dbHelper.getAllFavoriteMovies();

        // Set up the recycler view to display the favorite list
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        movieListAdapter = new MovieListAdapter(favoriteList, callbackService);
        recyclerView.setAdapter(movieListAdapter);

        // Filter and sort favorite list based on shared preferences settings
        filterAndSortFavoriteList();

        return view;
    }


    /**
     * Register shared preferences change listener
     */
    private void registerSharedPreferencesListener() {
        listener = (sharedPreferences, key) -> {
            if(key != null) {
                filterAndSortFavoriteList();

                // callback to update badge count in main activity
                callbackService.updateBadgeCount();
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Filter and sort favorite list based on shared preferences settings
     */
    private void filterAndSortFavoriteList() {
        String rating = sharedPreferences.getString("rating", "0");
        String releaseYear = sharedPreferences.getString("releaseYear", "1970");
        String sortBy = sharedPreferences.getString("sortBy", "Release Year");

        List<Movie> filteredList = dbHelper.getAllFavoriteMovies();
        filteredList.removeIf(movie -> movie.getVoteAverage() < Double.parseDouble(rating) ||
                Integer.parseInt(movie.getReleaseDate().substring(0, 4)) < Integer.parseInt(releaseYear));

        if (sortBy.equals("Release Year")) {
            filteredList.sort((movie1, movie2) -> movie2.getReleaseDate().compareTo(movie1.getReleaseDate()));
        } else if (sortBy.equals("Rating")) {
            filteredList.sort((movie1, movie2) -> Double.compare(movie2.getVoteAverage(), movie1.getVoteAverage()));
        }

        favoriteList.clear();
        favoriteList.addAll(filteredList);

        movieListAdapter.notifyDataSetChanged();
    }

    /**
     * Set callback service
     * @param callbackService
     */
    public void setCallbackService(CallbackService callbackService) {
        this.callbackService = callbackService;
    }

    /**
     * Update favorite list
     * @param movie: movie object to be updated
     */
    public void updateFavoriteList(Movie movie) {
        if (movie.getIsFavorite() == 1) {
            favoriteList.add(movie);
            dbHelper.addFavoriteMovie(movie);
            movieListAdapter.notifyItemInserted(favoriteList.size() - 1);
        } else {
            int index = -1;
            for (int i = 0; i < favoriteList.size(); i++) {
                if (favoriteList.get(i).getId() == movie.getId()) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                favoriteList.remove(index);
                dbHelper.removeFavoriteMovie(movie.getId());
                movieListAdapter.notifyItemRemoved(index);
            }
        }
    }

    /**
     * Search favorite movie by title
     * @param search: search name keyword
     */
    public void searchFavoriteMovie(String search) {
        favoriteList.clear();
        favoriteList.addAll(dbHelper.searchFavoriteMovies(search));
        movieListAdapter.notifyDataSetChanged();
    }

    /**
     * Get favorite list size
     * @return favorite list size
     */
    public int getFavoriteListSize() {
        if(favoriteList.isEmpty()) {
            return 0;
        } else {
            return favoriteList.size();
        }
    }
}