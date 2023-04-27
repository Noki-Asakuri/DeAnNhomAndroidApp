package com.example.deannhom.fragment.EditInfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.deannhom.R;
import com.example.deannhom.databinding.FragmentEditInfoBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class EditInfoFragment extends Fragment {
    private FragmentEditInfoBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEditInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();

        binding.btnConfirmed.setOnClickListener(view -> {
            AtomicBoolean isUpdatingEmail = new AtomicBoolean(false);

            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                return;
            }

            String firstName = Objects.requireNonNull(binding.inputUserFirstName.getText()).toString();
            String lastName = Objects.requireNonNull(binding.inputUserLastName.getText()).toString();
            String userEmail = Objects.requireNonNull(binding.inputUserEmail.getText()).toString();
            String userPhone = Objects.requireNonNull(binding.inputUserPhone.getText()).toString();

            UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();

            // TODO: Finish this too, case missing first or missing last
            if (!firstName.isEmpty() && !lastName.isEmpty()) {
                builder.setDisplayName(lastName + " " + firstName);
            }

            if (!userEmail.isEmpty()) {
                isUpdatingEmail.set(true);
                firebaseUser.updateEmail(userEmail).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        isUpdatingEmail.set(false);
                    }

                    if (task.getException() != null) {

                        task.getException().getMessage();
                    }
                });
            }

            // TODO: Finish this
//            if (!userPhone.isEmpty()) {
//
//                firebaseUser.updatePhoneNumber();
//            }

            firebaseUser.updateProfile(builder.build()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    while (isUpdatingEmail.get()) {
                    }

                    Toast.makeText(this.requireContext(), "Update profile successfully!", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).navigate(R.id.action_navigation_edit_info_to_navigation_account);
                }
            });
        });

        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.reload();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}