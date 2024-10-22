package com.example.ojtbadaassignment14.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ojtbadaassignment14.R;
import com.example.ojtbadaassignment14.api.RetrofitClient;
import com.example.ojtbadaassignment14.models.Cast;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CastAdapter  extends RecyclerView.Adapter<CastAdapter.CastViewHolder> {

    private final List<Cast> castList;

    Picasso picasso;

    public CastAdapter(List<Cast> castList) {
        this.castList = castList;
    }

    @NonNull
    @Override
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cast, parent, false);
        return new CastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CastViewHolder holder, int position) {
        Cast cast = castList.get(position);
        holder.nameTextView.setText(cast.getName());

        // load image
        picasso = Picasso.get();
        picasso.load(RetrofitClient.IMAGE_BASE_URL + cast.getProfilePath())
                .placeholder(R.drawable.baseline_image_24)
                .error(R.drawable.baseline_image_not_supported_24)
                .into(holder.profileImageView);
    }

    @Override
    public int getItemCount() {
        return castList.size();
    }

    public static class CastViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView nameTextView;

        public CastViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.iv_cast);
            nameTextView = itemView.findViewById(R.id.tv_name);
        }
    }
}
