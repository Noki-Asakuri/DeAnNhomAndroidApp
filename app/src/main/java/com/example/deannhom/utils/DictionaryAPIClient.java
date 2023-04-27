package com.example.deannhom.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.deannhom.model.API.API;
import com.example.deannhom.model.API.Meaning;
import com.example.deannhom.model.API.Phonetic;
import com.example.deannhom.model.API.Word;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class DictionaryAPIClient {

    private static DictionaryAPIClient instance = null;
    private final API api;

    public DictionaryAPIClient() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(API.BASE_URL).addConverterFactory(ScalarsConverterFactory.create()).build();

        api = retrofit.create(API.class);
    }

    public static synchronized DictionaryAPIClient getInstance() {
        if (instance == null) {
            instance = new DictionaryAPIClient();
        }

        return instance;
    }

    public API getApi() {
        return api;
    }

    public interface DefinitionCallback {
        void onDefinitionFetched(ArrayList<Word> wordArrayList);
    }

    public void fetchDefinitionFromApi(String word, DefinitionCallback callback) {
        Call<String> callWord = DictionaryAPIClient.getInstance().getApi().getWord(word);

        callWord.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.body() == null) {
                    callback.onDefinitionFetched(null);
                    return;
                }

                try {
                    JSONArray mainJSONArray = new JSONArray(response.body());

                    ArrayList<Word> finalWord = new ArrayList<>();
                    for (int i = 0; i < mainJSONArray.length(); i++) {
                        JSONObject currentWordJSON = mainJSONArray.getJSONObject(i);

                        Word currentWord = new Word();
                        currentWord.setWord(currentWordJSON.getString("word"));

                        JSONObject phonetics = currentWordJSON.getJSONArray("phonetics").getJSONObject(0);
                        currentWord.setPhonetics(new Phonetic(phonetics.getString("text"), phonetics.getString("audio"), ""));

                        JSONArray meanings = currentWordJSON.getJSONArray("meanings");
                        ArrayList<Meaning> meaningArrayList = new ArrayList<>();
                        for (int j = 0; j < meanings.length(); j++) {
                            JSONObject currentMeaning = meanings.getJSONObject(j);

                            String partOfSpeech = currentMeaning.getString("partOfSpeech");
                            ArrayList<String> meaningStrings = new ArrayList<>();
                            for (int k = 0; k < currentMeaning.getJSONArray("definitions").length(); k++) {
                                String meaning = currentMeaning.getJSONArray("definitions").getJSONObject(k).getString("definition");

                                meaningStrings.add(meaning);
                            }

                            meaningArrayList.add(new Meaning(partOfSpeech, meaningStrings));
                        }

                        currentWord.setMeanings(meaningArrayList);
                        finalWord.add(currentWord);
                    }

                    callback.onDefinitionFetched(finalWord);

                } catch (JSONException e) {
                    e.printStackTrace();

                    Log.e("API", e.getMessage());
                    callback.onDefinitionFetched(null);
                }

            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e("API", t.getMessage());

                callback.onDefinitionFetched(null);
            }
        });
    }
}