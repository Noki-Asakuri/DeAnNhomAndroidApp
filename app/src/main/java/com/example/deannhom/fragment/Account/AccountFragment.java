package com.example.deannhom.fragment.Account;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.deannhom.R;
import com.example.deannhom.databinding.FragmentAccountBinding;
import com.example.deannhom.utils.Tuple;
import com.example.deannhom.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;

public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;
    FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Darkmode
        Tuple<Boolean, SharedPreferences.Editor> returnValue = Utils.isDarkMode(requireContext());

        if (returnValue.isDarkModeOn) {
            binding.btnDarkmode.setIconResource(R.drawable.baseline_dark_mode_24);
        } else {
            binding.btnDarkmode.setIconResource(R.drawable.baseline_light_mode_24);
        }

        binding.btnSignIn.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_navigation_account_to_navigation_login));

        // Event Handlers
        binding.btnLogout.setOnClickListener(v -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this.getContext(), "You currently not signed in!", Toast.LENGTH_LONG).show();
                return;
            }

            firebaseAuth.signOut();

            binding.btnEditUser.setVisibility(View.GONE);
            binding.btnLogout.setVisibility(View.GONE);
            binding.btnSignIn.setVisibility(View.VISIBLE);

            binding.textUsername.setText(R.string.name);
            binding.textUserEmail.setText(R.string.email);

            Toast.makeText(this.getContext(), "Logged out successfully!", Toast.LENGTH_LONG).show();
        });

        binding.btnDarkmode.setOnClickListener(v -> {
            if (returnValue.isDarkModeOn) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                returnValue.editor.putBoolean("isDarkModeOn", false);
                binding.btnDarkmode.setIconResource(R.drawable.baseline_light_mode_24);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                returnValue.editor.putBoolean("isDarkModeOn", true);
                binding.btnDarkmode.setIconResource(R.drawable.baseline_dark_mode_24);
            }
            returnValue.editor.apply();
        });

        binding.btnEditUser.setOnClickListener(v -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this.requireContext(), "You haven't login yet!", Toast.LENGTH_SHORT).show();

                return;
            }

            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_account_to_navigation_edit_info);
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload();

            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            Uri image = currentUser.getPhotoUrl();

            assert email != null;

            if (displayName == null || displayName.isEmpty()) {
                displayName = email.split("@")[0];
            }

            if (image != null) {
                Picasso.get().load(image).into(binding.imgUserAvatar);
            }

            binding.textUsername.setText(MessageFormat.format("Name: {0}", displayName));
            binding.textUserEmail.setText(MessageFormat.format("Email: {0}", email));

            binding.btnEditUser.setVisibility(View.VISIBLE);
            binding.btnLogout.setVisibility(View.VISIBLE);
            binding.btnSignIn.setVisibility(View.GONE);
        } else {
            binding.btnEditUser.setVisibility(View.GONE);
            binding.btnLogout.setVisibility(View.GONE);
            binding.btnSignIn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}