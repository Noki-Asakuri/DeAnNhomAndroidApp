package com.example.deannhom.utils;

import android.util.Log;

import com.example.deannhom.model.API.Meaning;
import com.example.deannhom.model.API.Phonetic;
import com.example.deannhom.model.API.Word;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class DictionaryApiRequest {
    Gson gson = new Gson();

    private static final String API_ENDPOINT = "https://api.dictionaryapi.dev/api/v2/entries/en/";

    public interface DefinitionCallback {
        void onDefinitionFetched(ArrayList<Word> wordArrayList);
    }

    public void fetchDefinitionFromApi(String word, DefinitionCallback callback) {
        try {
            String encodedWord = URLEncoder.encode(word, "UTF-8");
            URL url = new URL(API_ENDPOINT + encodedWord);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String responseData = stringBuilder.toString();

                // Extract definition from the response data
                // TODO: Get all definition of all words.
                JSONArray jsonArray = new JSONArray(responseData);

                ArrayList<Word> wordArrayList = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject wordJsonObject = jsonArray.getJSONObject(i);

                    Word wordDefinition = new Word();

                    wordDefinition.setWord(wordJsonObject.getString("word"));

                    ArrayList<Phonetic> phonetics = new ArrayList<>();
                    JSONArray phoneticsJSONArray = wordJsonObject.getJSONArray("phonetics");
                    for (int j = 0; j < phoneticsJSONArray.length(); j++) {
                        JSONObject phoneticsJSON = phoneticsJSONArray.getJSONObject(j);

                        String phoneticText = phoneticsJSON.getString("text");
                        String phoneticAudio = phoneticsJSON.getString("audio");
                        String phoneticSourceUrl = phoneticsJSON.getString("sourceUrl");

                        Phonetic phonetic = new Phonetic(phoneticText, phoneticAudio, phoneticSourceUrl);

                        phonetics.add(phonetic);
                    }

                    wordDefinition.setPhonetics(phonetics);

                    ArrayList<Meaning> meanings = new ArrayList<>();
                    JSONArray meaningsJSONArray = wordJsonObject.getJSONArray("meanings");
                    for (int k = 0; k < meaningsJSONArray.length(); k++) {
                        JSONObject meaningsJSON = meaningsJSONArray.getJSONObject(k);

                        String partOfSpeech = meaningsJSON.getString("partOfSpeech");
                        ArrayList<String> definitions = new ArrayList<>();

                        JSONArray definitionJSONArray = meaningsJSON.getJSONArray("definitions");
                        for (int l = 0; l < definitionJSONArray.length(); l++) {
                            JSONObject definitionJSON = definitionJSONArray.getJSONObject(l);

                            String definition = definitionJSON.getString("definition");

                            definitions.add(definition);
                        }

                        Meaning meaning = new Meaning(partOfSpeech, definitions);

                        meanings.add(meaning);
                    }

                    wordDefinition.setMeanings(meanings);

                    wordArrayList.add(wordDefinition);
                }

                callback.onDefinitionFetched(wordArrayList);
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                callback.onDefinitionFetched(null);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}