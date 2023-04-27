package com.example.deannhom.fragment.Register;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.deannhom.R;
import com.example.deannhom.databinding.FragmentRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class RegisterFragment extends Fragment {
    FirebaseAuth firebaseAuth;

    private FragmentRegisterBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //
        firebaseAuth = FirebaseAuth.getInstance();

        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.btnSignUp.setOnClickListener(Register());
        binding.btnSignIn.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_register_to_navigation_login);
        });

        return root;
    }

    private View.OnClickListener Register() {
        return view -> {
            // Get user input;
            String email = Objects.requireNonNull(binding.inputEmail.getText()).toString();
            String password = Objects.requireNonNull(binding.inputPassword.getText()).toString();
            String confirm = Objects.requireNonNull(binding.inputConfirmPassword.getText()).toString();

            if (email.isEmpty()) {
                binding.inputEmail.setError("Email can not be blank!");
                return;
            }

            if (password.isEmpty()) {
                binding.inputPassword.setError("Password can not be blank!");
                return;
            }

            if (confirm.isEmpty()) {
                binding.inputConfirmPassword.setError("Confirm password can not be blank!");
                return;
            }

            if (!(confirm.equals(password))) {
                binding.inputConfirmPassword.setError("Confirm password is not equal to password!");
                return;
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success");
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    updateUI(user);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    Toast.makeText(this.getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
            });
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload();
        }

        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser == null) {
            return;
        }

        NavHostFragment.findNavController(this).navigate(R.id.action_navigation_register_to_navigation_account);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}