package com.example.deannhom.fragment.Home;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
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
import androidx.lifecycle.ViewModelProvider;

import com.example.deannhom.R;
import com.example.deannhom.databinding.FragmentHomeBinding;
import com.example.deannhom.model.API.Meaning;
import com.example.deannhom.model.API.Word;
import com.example.deannhom.utils.DictionaryAPIClient;
import com.example.deannhom.utils.Utils;
import com.example.deannhom.utils.WordTuple;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
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

        HomeViewModel homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        homeViewModel.word.observe(getViewLifecycleOwner(), word -> {
            if (word == null) {
                binding.textTitle.setText("");
                binding.textDefinition.setText("");

                binding.btnFavorite.setVisibility(View.INVISIBLE);
                binding.btnAudio.setVisibility(View.INVISIBLE);

                return;
            }

            if (word.word != null && word.audioUrl == null && word.definition == null && word.title == null) {
                binding.inputWord.setText(word.word);
                binding.btnSearch.callOnClick();

                return;
            }

            binding.inputWord.setText(word.word);
            binding.textTitle.setText(word.title);
            binding.textDefinition.setText(word.definition);

            binding.btnFavorite.setVisibility(View.VISIBLE);

            if (word.audioUrl != null && !word.audioUrl.isEmpty()) {
                currentAudioUrl = word.audioUrl;
                binding.btnAudio.setVisibility(View.VISIBLE);
            } else {
                currentAudioUrl = null;
            }
        });

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
                        favorite.put("timestamp", FieldValue.serverTimestamp());

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

                                    binding.btnFavorite.setImageTintList(getColorBaseOnMode());
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

            if (mediaPlayer == null) {
                // initializing media player
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }

            if (mediaPlayer.isPlaying()) {
                onDonePlayingPlayer();
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
            homeViewModel.word.setValue(null);

            String userInputWord = Objects.requireNonNull(binding.inputWord.getText()).toString();
            if (userInputWord.isEmpty()) {
                binding.inputWord.setError("Word can't not be empty!");
                return;
            }

            boolean hasInternet = isNetworkConnected();
            if (!hasInternet) {
                Toast.makeText(this.getContext(), "You must have internet connection to search word", Toast.LENGTH_LONG).show();
                return;
            }

            Activity activity = this.getActivity();
            assert activity != null;

            DictionaryAPIClient dictionaryAPIClient = new DictionaryAPIClient();
            dictionaryAPIClient.fetchDefinitionFromApi(userInputWord, wordArrayList -> {
                if (wordArrayList != null) {

                    binding.btnAudio.setVisibility(View.INVISIBLE);
                    binding.btnFavorite.setVisibility(View.VISIBLE);
                    binding.btnFavorite.setImageTintList(getColorBaseOnMode());

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
                    String titleText = " - Word: " + userInputWord + "\n";

                    // Set phonetics
                    if (wordArrayList.get(0).getPhonetics() != null) {
                        titleText += " - Phonetic: " + wordArrayList.get(0).getPhonetics().getText();
                    }

                    StringBuilder definitionText = new StringBuilder();
                    // Set meanings
                    int position = 1;
                    String audioUrl = "";
                    definitionText.append(" - Meaning: ").append("\n");
                    for (Word word : wordArrayList) {
                        if (word.getPhonetics() != null) {
                            audioUrl = word.getPhonetics().getAudio();
                            binding.btnAudio.setVisibility(View.VISIBLE);
                        }

                        for (Meaning meaning : word.getMeanings()) {
                            definitionText.append(MessageFormat.format("    + {0}: ", position)).append(meaning.getDefinitions()).append("\n");

                            position++;
                        }
                    }

                    WordTuple wordTuple = new WordTuple(userInputWord, titleText, definitionText.toString(), audioUrl);
                    homeViewModel.word.setValue(wordTuple);

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

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    ColorStateList getColorBaseOnMode() {
        int color;

        if (Utils.isDarkMode(this.requireContext(), true)) {
            color = Color.rgb(255, 255, 255);
        } else {
            color = Color.rgb(0, 0, 0);
        }

        return ColorStateList.valueOf(color);
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
                    history.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("histories").add(history).addOnCompleteListener(innerTask -> {
                        if (innerTask.isSuccessful()) {
                            Log.i("History", "History added " + userInputWord);
                        } else {
                            Log.e("History", Objects.requireNonNull(innerTask.getException()).getMessage());
                        }
                    });
                } else {
                    Map<String, Object> history = new HashMap<>();
                    history.put("timestamp", FieldValue.serverTimestamp());

                    for (QueryDocumentSnapshot documentSnapshot : document) {
                        firebaseFirestore.collection("histories").document(documentSnapshot.getId()).update(history).addOnCompleteListener(innerTask -> {
                            if (innerTask.isSuccessful()) {
                                Log.i("History", "History updated " + userInputWord);
                            } else {
                                Log.e("History", Objects.requireNonNull(innerTask.getException()).getMessage());
                            }
                        });
                    }
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