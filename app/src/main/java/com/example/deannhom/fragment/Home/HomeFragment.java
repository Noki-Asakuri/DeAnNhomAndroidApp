package com.example.deannhom.fragment.Home;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.deannhom.databinding.FragmentHomeBinding;
import com.example.deannhom.model.API.Meaning;
import com.example.deannhom.model.API.Phonetic;
import com.example.deannhom.model.API.Word;
import com.example.deannhom.utils.DictionaryApiRequest;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.text.MessageFormat;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

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

        // Event handler
        binding.btnSearch.setOnClickListener(v -> {
            String userInputWord = Objects.requireNonNull(binding.inputWord.getText()).toString();
            if (userInputWord.isEmpty()) {
                binding.inputWord.setError("Word can't not be empty!");
                return;
            }

            Activity activity = this.getActivity();
            assert activity != null;

            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

            DictionaryApiRequest dictionaryApiRequest = new DictionaryApiRequest();
            dictionaryApiRequest.fetchDefinitionFromApi(userInputWord, wordArrayList -> {
                if (wordArrayList != null) {
                    StringBuilder text = new StringBuilder();

                    text.append(userInputWord);

                    for (int i = 0; i < wordArrayList.size(); i++) {
                        Word word = wordArrayList.get(i);

                        String insideText = "";
                        StringBuilder phonetics = new StringBuilder("\nPhonetics: ");
                        for (int j = 0; j < word.getPhonetics().size(); j++) {
                            Phonetic phonetic = word.getPhonetics().get(j);

                            phonetics.append(MessageFormat.format("\n - {0}", phonetic.getText()));
                        }

                        StringBuilder meanings = new StringBuilder("\nMeanings: ");
                        for (int k = 0; k < word.getMeanings().size(); k++) {
                            Meaning meaning = word.getMeanings().get(k);

                            meanings.append("\n - ").append(meaning.getPartOfSpeech().toUpperCase()).append(": ");

                            StringBuilder definitions = new StringBuilder();
                            for (int l = 0; l < meaning.getDefinitions().size(); l++) {
                                definitions.append(meaning.getDefinitions().get(l));
                            }

                            meanings.append("\n     - ").append(definitions);
                        }

                        insideText += phonetics + meanings.toString();

                        if (i > 0) {
                            text.append("\n\n ");
                        }

                        text.append(insideText);
                    }

                    binding.textDefinition.setText(text.toString());

                } else {
                    Toast.makeText(this.getContext(), "Word not found!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}