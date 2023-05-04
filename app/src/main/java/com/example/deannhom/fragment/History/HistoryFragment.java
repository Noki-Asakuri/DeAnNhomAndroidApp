package com.example.deannhom.fragment.History;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.deannhom.R;
import com.example.deannhom.databinding.FragmentHistoryBinding;
import com.example.deannhom.fragment.Home.HomeViewModel;
import com.example.deannhom.model.history.History;
import com.example.deannhom.model.history.HistoryAdapter;
import com.example.deannhom.utils.WordTuple;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class HistoryFragment extends Fragment implements HistoryAdapter.UserCallback {
    private FragmentHistoryBinding binding;
    ArrayList<History> historyArrayList;
    HistoryAdapter historyAdapter;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    HomeViewModel homeViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        // Inflate the layout for this fragment
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Get firebase instance
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Load user history data
        loadUserHistoryData();

        historyAdapter = new HistoryAdapter(historyArrayList, this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());

        binding.recycleHistoryView.setAdapter(historyAdapter);
        binding.recycleHistoryView.setLayoutManager(linearLayoutManager);

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
        historyArrayList = new ArrayList<>();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        firebaseFirestore.collection("histories").whereEqualTo("userId", currentUser.getUid()).orderBy("timestamp", Query.Direction.DESCENDING).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    History history = document.toObject(History.class);
                    history.setId(document.getId());

                    historyArrayList.add(history);
                }

                historyAdapter.notifyDataSetChanged();
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });

    }

    @Override
    public void onItemDelete(String id, int position) {
        firebaseFirestore.collection("histories").document(id).delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this.getContext(), "Removed successfully", Toast.LENGTH_LONG).show();

                historyArrayList.remove(position);
                historyAdapter.notifyItemRemoved(position);
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    @Override
    public void onItemClicked(String id, int position) {
        History wordHistory = historyArrayList.get(position);

        homeViewModel.word.setValue(new WordTuple(wordHistory.getWord(), null, null, null));
        NavHostFragment.findNavController(this).navigate(R.id.action_navigation_history_to_navigation_home);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}