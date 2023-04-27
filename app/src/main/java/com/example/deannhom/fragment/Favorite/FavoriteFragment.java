package com.example.deannhom.fragment.Favorite;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.deannhom.databinding.FragmentFavoriteBinding;
import com.example.deannhom.model.favorite.Favorite;
import com.example.deannhom.model.favorite.FavoriteAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class FavoriteFragment extends Fragment implements FavoriteAdapter.UserCallback {
    private FragmentFavoriteBinding binding;

    ArrayList<Favorite> favoriteArrayList;
    FavoriteAdapter favoriteAdapter;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFavoriteBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Get firebase instance
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Load user history data
        loadUserHistoryData();

        favoriteAdapter = new FavoriteAdapter(favoriteArrayList, this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());

        binding.recycleFavoriteView.setAdapter(favoriteAdapter);
        binding.recycleFavoriteView.setLayoutManager(linearLayoutManager);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void loadUserHistoryData() {
        favoriteArrayList = new ArrayList<>();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        firebaseFirestore.collection("favorites").whereEqualTo("userId", currentUser.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Favorite favorite = document.toObject(Favorite.class);
                    favorite.setId(document.getId());

                    favoriteArrayList.add(favorite);
                }

                favoriteAdapter.notifyDataSetChanged();
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });

    }

    @Override
    public void onItemUnfavorite(String id, int position) {
        firebaseFirestore.collection("favorites").document(id).delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this.getContext(), "Removed successfully", Toast.LENGTH_LONG).show();
                favoriteAdapter.notifyDataSetChanged();
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}