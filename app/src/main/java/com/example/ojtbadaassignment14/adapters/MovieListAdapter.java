package com.example.ojtbadaassignment14.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ojtbadaassignment14.R;
import com.example.ojtbadaassignment14.api.RetrofitClient;
import com.example.ojtbadaassignment14.models.Movie;
import com.example.ojtbadaassignment14.services.CallbackService;
import com.squareup.picasso.Picasso;


import java.util.List;

public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieViewHolder> {

    // interface
    CallbackService callbackService;

    private List<Movie> movieList;
    private boolean isGridLayout = false; // default is list layout

    Picasso picasso;

    public MovieListAdapter(List<Movie> movieList, CallbackService callbackService) {
        this.movieList = movieList;
        this.callbackService = callbackService;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (isGridLayout) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_grid_type, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_list_type, parent, false);
        }
        return new MovieViewHolder(view, isGridLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        // get movie at position
        Movie movie = movieList.get(position);

        // bind movie data to view holder
        holder.bind(movie);

        if(!isGridLayout) {
            // set click listener for favorite icon in list layout
            holder.ivFavorite.setOnClickListener(v -> {
                // callback MainActivity to add/remove favorite movie
                callbackService.onFavoriteMovie(movie);
            });
        }

        // set click listener for item move to detail
        holder.itemView.setOnClickListener(v -> {
            // callback MainActivity to show movie detail
            callbackService.onShowMovieDetail(movie);
        });

    }

    @Override
    public int getItemCount() {
        Log.d("check", "getItemCount: " + movieList.size());
        return movieList.size();
    }

    /**
     * Set grid layout
     * @param isGridLayout
     */
    public void setGridLayout(boolean isGridLayout) {
        this.isGridLayout = isGridLayout;
        notifyDataSetChanged();
    }


    public class MovieViewHolder extends RecyclerView.ViewHolder {

        ImageView ivMovie;
        TextView tvTitle;
        TextView tvRating;
        TextView tvReleaseDate;
        TextView tvOverview;
        ImageView ivAdultTag;
        ImageView ivFavorite;
        TextView tvDateLabel;
        TextView tvRatingLabel;
        TextView tvOverviewLabel;

        boolean isGridLayout;

        public MovieViewHolder(@NonNull View itemView, boolean isGridLayout) {
            super(itemView);
            this.isGridLayout = isGridLayout;

            ivMovie = itemView.findViewById(R.id.img_movie_poster);
            tvTitle = itemView.findViewById(R.id.tv_movie_title);

            // Find views for movie list if not in grid layout
            if (!isGridLayout) {
                tvRating = itemView.findViewById(R.id.tv_rating);
                tvReleaseDate = itemView.findViewById(R.id.tv_date);
                tvOverview = itemView.findViewById(R.id.tv_overview);
                ivAdultTag = itemView.findViewById(R.id.img_adult_tag);
                ivFavorite = itemView.findViewById(R.id.img_favorite);
                tvDateLabel = itemView.findViewById(R.id.label_date);
                tvRatingLabel = itemView.findViewById(R.id.label_rating);
                tvOverviewLabel = itemView.findViewById(R.id.label_overview);
            }
        }

        public void bind(Movie movie) {

            tvTitle.setText(movie.getTitle());

            // Initialize picasso if not initialized already
            if (picasso == null) {
                picasso = Picasso.get();
            }

            // Load image using Picasso
            picasso.load(RetrofitClient.IMAGE_BASE_URL + movie.getPosterPath())
                    .placeholder(R.drawable.baseline_image_24)
                    .error(R.drawable.baseline_image_not_supported_24)
                    .into(ivMovie);

            // Set text for movie details if not in grid layout
            if (!isGridLayout) {
                tvRating.setText(String.format("%.1f/10", movie.getVoteAverage()));
                tvReleaseDate.setText(movie.getReleaseDate());
                tvOverview.setText(movie.getOverview());

                // tag adult movie
                if (movie.isAdult()) {
                    ivAdultTag.setVisibility(View.VISIBLE);
                } else {
                    ivAdultTag.setVisibility(View.GONE);
                }

                // tag favorite movie
                if (movie.getIsFavorite() == 0) {
                    ivFavorite.setImageResource(R.drawable.ic_star);
                } else {
                    ivFavorite.setImageResource(R.drawable.ic_star_favorite);
                }
            }
        }

    }
}
