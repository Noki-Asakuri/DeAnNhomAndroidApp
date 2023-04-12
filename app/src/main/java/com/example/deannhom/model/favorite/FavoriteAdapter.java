package com.example.deannhom.model.favorite;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deannhom.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {
    ArrayList<Favorite> favoriteArrayList;
    Context context;
    UserCallback userCallback;

    public FavoriteAdapter(ArrayList<Favorite> favoriteArrayList, UserCallback userCallback) {
        this.favoriteArrayList = favoriteArrayList;
        this.userCallback = userCallback;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View userView = inflater.inflate(R.layout.layout_favorite_item, parent, false);

        return new FavoriteViewHolder(userView);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Favorite item = favoriteArrayList.get(position);

        holder.textFavoriteWord.setText(item.getWord());
        holder.textFavoriteTranslated.setText(item.getTranslatedWord());

        holder.btnUnfavoriteWord.setOnClickListener(view -> userCallback.onItemUnfavorite(item.getId(), position));
    }

    @Override
    public int getItemCount() {
        return favoriteArrayList.size();
    }

    public interface UserCallback {
        void onItemUnfavorite(String id, int position);
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        TextView textFavoriteWord, textFavoriteTranslated;
        MaterialButton btnUnfavoriteWord;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);

            textFavoriteWord = itemView.findViewById(R.id.textFavoriteWord);
            textFavoriteTranslated = itemView.findViewById(R.id.textFavoriteTranslated);

            btnUnfavoriteWord = itemView.findViewById(R.id.btnUnfavoriteWord);
        }
    }
}