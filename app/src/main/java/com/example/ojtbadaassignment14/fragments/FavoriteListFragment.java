package com.example.ojtbadaassignment14.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private RecyclerView recyclerView;
    private MovieListAdapter movieListAdapter;
    private ProgressBar progressBar;

    private List<Movie> favoriteList;


    public FavoriteListFragment(CallbackService callbackService) {
        // Required empty public constructor
        this.favoriteList = new ArrayList<>();
        this.callbackService = callbackService;

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.idPBLoading);

        progressBar.setVisibility(View.GONE); // hide progress bar

        // Initialize database helper
        dbHelper = new DatabaseHelper(getContext());
        // Retrieve favorite list from the database
        favoriteList = dbHelper.getAllFavoriteMovies();

        // Set up the recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        movieListAdapter = new MovieListAdapter(favoriteList, callbackService);
        recyclerView.setAdapter(movieListAdapter);

        return view;
    }


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

    public int getFavoriteListSize() {
        if(favoriteList.isEmpty()) {
            return 0;
        } else {
            return favoriteList.size();
        }
    }
}