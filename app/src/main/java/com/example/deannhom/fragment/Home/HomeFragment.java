package com.example.deannhom.fragment.Home;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.deannhom.R;
import com.example.deannhom.databinding.FragmentHomeBinding;
import com.example.deannhom.model.API.Meaning;
import com.example.deannhom.model.API.Word;
import com.example.deannhom.utils.DictionaryAPIClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    MediaPlayer mediaPlayer;
    String currentAudioUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);

        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Event handler
        binding.btnFavorite.setOnClickListener(view -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                Toast.makeText(this.requireContext(), "Login to favorite word!", Toast.LENGTH_LONG).show();
                return;
            }

            String userInputWord = Objects.requireNonNull(binding.inputWord.getText()).toString();

            firebaseFirestore.collection("favorites").where(Filter.and(Filter.equalTo("userId", firebaseUser.getUid()), Filter.equalTo("word", userInputWord))).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot documents = task.getResult();

                    if (documents.isEmpty()) {
                        Map<String, Object> favorite = new HashMap<>();

                        favorite.put("userId", firebaseUser.getUid());
                        favorite.put("word", userInputWord);

                        firebaseFirestore.collection("favorites").add(favorite).addOnCompleteListener(innerTask -> {
                            if (innerTask.isSuccessful()) {
                                Toast.makeText(this.requireContext(), "Favorite word successfully!", Toast.LENGTH_LONG).show();

                                binding.btnFavorite.setImageTintList(ColorStateList.valueOf(Color.rgb(226, 111, 113)));
                            } else {
                                Toast.makeText(this.requireContext(), "Favorite word failed!", Toast.LENGTH_LONG).show();
                                Log.e("Favorite", Objects.requireNonNull(innerTask.getException()).getMessage());
                            }
                        });
                    } else {
                        for (QueryDocumentSnapshot document : documents) {
                            firebaseFirestore.collection("favorites").document(document.getId()).delete().addOnCompleteListener(deleteTask -> {
                                if (deleteTask.isSuccessful()) {
                                    Toast.makeText(this.getContext(), "Unfavorite word successfully!", Toast.LENGTH_LONG).show();

                                    binding.btnFavorite.setImageTintList(ColorStateList.valueOf(Color.rgb(0, 0, 0)));
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }
                            });
                        }
                    }
                }
            });
        });

        binding.btnAudio.setOnClickListener(v -> {
            if (currentAudioUrl == null) {
                Toast.makeText(this.requireContext(), "No audio url found.", Toast.LENGTH_LONG).show();
                return;
            }

            Log.i("AUDIO", currentAudioUrl);

            if (mediaPlayer == null) {
                // initializing media player
                mediaPlayer = new MediaPlayer();

                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }

            if (mediaPlayer.isPlaying()) {
                if (mediaPlayer == null) {
                    return;
                }

                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();

                mediaPlayer = null;

                binding.btnAudio.setImageDrawable(ContextCompat.getDrawable(this.requireContext(), R.drawable.baseline_play_arrow_24));
            } else {
                // below line is use to set our
                // url to our media player.
                try {
                    mediaPlayer.stop();
                    mediaPlayer.reset();

                    mediaPlayer.setDataSource(currentAudioUrl);

                    mediaPlayer.prepare();
                    mediaPlayer.start();

                    mediaPlayer.setOnCompletionListener(player -> onDonePlayingPlayer());
                    binding.btnAudio.setImageDrawable(ContextCompat.getDrawable(this.requireContext(), R.drawable.baseline_pause_24));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        binding.btnSearch.setOnClickListener(v -> {
            binding.textTitle.setText("");
            binding.textDefinition.setText("");

            String userInputWord = Objects.requireNonNull(binding.inputWord.getText()).toString();
            if (userInputWord.isEmpty()) {
                binding.inputWord.setError("Word can't not be empty!");
                return;
            }

            Activity activity = this.getActivity();
            assert activity != null;

//            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
//
//            if (inputManager != null) {
//                inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//            }

            DictionaryAPIClient dictionaryAPIClient = new DictionaryAPIClient();
            dictionaryAPIClient.fetchDefinitionFromApi(userInputWord, wordArrayList -> {
                if (wordArrayList != null) {

                    binding.btnAudio.setVisibility(View.VISIBLE);
                    binding.btnFavorite.setVisibility(View.VISIBLE);
                    binding.btnFavorite.setImageTintList(ColorStateList.valueOf(Color.rgb(0, 0, 0)));

                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        addWordIntoHistoryCollection(firebaseUser);

                        firebaseFirestore.collection("favorites").where(Filter.and(Filter.equalTo("userId", firebaseUser.getUid()), Filter.equalTo("word", userInputWord))).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                QuerySnapshot document = task.getResult();

                                if (!document.isEmpty()) {
                                    binding.btnFavorite.setImageTintList(ColorStateList.valueOf(Color.rgb(226, 111, 113)));
                                }
                            }
                        });
                    }

                    // Set main word
                    String titleText = " - Word: " + userInputWord + "\n" +
                            // Set phonetics
                            " - Phonetic: " + wordArrayList.get(0).getPhonetics().getText();

                    binding.textTitle.setText(titleText);

                    StringBuilder definitionText = new StringBuilder();
                    // Set meanings
                    int position = 1;
                    definitionText.append(" - Meaning: ").append("\n");
                    for (Word word : wordArrayList) {
                        if (word.getPhonetics() != null) {
                            currentAudioUrl = word.getPhonetics().getAudio();
                        }

                        for (Meaning meaning : word.getMeanings()) {
                            definitionText.append(MessageFormat.format("    + {0}: ", position)).append(meaning.getDefinitions()).append("\n");

                            position++;
                        }
                    }

                    binding.textDefinition.setText(definitionText.toString());

                } else {
                    binding.btnAudio.setVisibility(View.INVISIBLE);
                    binding.btnFavorite.setVisibility(View.INVISIBLE);

                    currentAudioUrl = null;

                    Toast.makeText(this.getContext(), "Word not found!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return root;
    }

    void addWordIntoHistoryCollection(FirebaseUser firebaseUser) {
        String userInputWord = Objects.requireNonNull(binding.inputWord.getText()).toString();

        firebaseFirestore.collection("histories").where(Filter.and(Filter.equalTo("userId", firebaseUser.getUid()), Filter.equalTo("word", userInputWord))).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot document = task.getResult();

                if (document.isEmpty()) {
                    Map<String, Object> history = new HashMap<>();

                    history.put("userId", firebaseUser.getUid());
                    history.put("word", userInputWord);

                    firebaseFirestore.collection("histories").add(history).addOnCompleteListener(innerTask -> {
                        if (innerTask.isSuccessful()) {
                            Log.i("History", "History added " + userInputWord);
                        } else {
                            Log.e("History", Objects.requireNonNull(innerTask.getException()).getMessage());
                        }
                    });
                }
            }
        });
    }

    void onDonePlayingPlayer() {
        if (mediaPlayer == null) {
            return;
        }

        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();

        mediaPlayer = null;

        binding.btnAudio.setImageDrawable(ContextCompat.getDrawable(this.requireContext(), R.drawable.baseline_play_arrow_24));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}