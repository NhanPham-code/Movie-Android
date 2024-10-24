package com.example.ojtbadaassignment14.fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ojtbadaassignment14.R;
import com.example.ojtbadaassignment14.models.Movie;
import com.example.ojtbadaassignment14.services.CallbackService;


public class CommonFragment extends Fragment {

    private CallbackService callbackService;
    private MovieListFragment movieListFragment;
    private MovieDetailFragment movieDetailFragment;

    public CommonFragment() {
        // Required empty public constructor
    }

    public void setMovieListFragment(MovieListFragment movieListFragment) {
        this.movieListFragment = movieListFragment;
    }

    public void setMovieDetailFragment(MovieDetailFragment movieDetailFragment) {
        this.movieDetailFragment = movieDetailFragment;
    }

    public void setCallbackService(CallbackService callbackService) {
        this.callbackService = callbackService;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add back button handler
        getActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (movieDetailFragment.isVisible()) {

                    // callback MainActivity to show movie list
                    callbackService.backToMovieList();

                    // pop back stack
                    //getChildFragmentManager().popBackStack();

                } else {
                    getActivity().finish();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_common, container, false);

        // Show movie list fragment
        getChildFragmentManager().beginTransaction()
                .add(R.id.fragment_container_view_tag, movieListFragment)
                .addToBackStack(MovieDetailFragment.class.getName())
                .commit();



        return view;
    }

    public void showDetailFragment() {
        getChildFragmentManager().beginTransaction()
                .add(R.id.fragment_container_view_tag, movieDetailFragment)
                //.addToBackStack(MovieDetailFragment.class.getName())
                .commit();
    }

    public void showMovieListFragment() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view_tag, movieListFragment)
                //.addToBackStack(MovieListFragment.class.getName())
                .commit();
    }

}