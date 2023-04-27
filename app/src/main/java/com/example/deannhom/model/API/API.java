package com.example.deannhom.model.API;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface API {
    String BASE_URL = "https://api.dictionaryapi.dev/api/v2/entries/";

    @GET("en/{word}")
    Call<String> getWord(@Path("word") String word);
}
