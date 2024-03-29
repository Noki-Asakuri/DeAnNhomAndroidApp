package com.example.deannhom.model.API;

import androidx.annotation.NonNull;

public class Phonetic {
    String text;
    String audio;
    String sourceUrl;

    public Phonetic(String text, String audio, String sourceUrl) {
        this.text = text;
        this.audio = audio;
        this.sourceUrl = sourceUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    @NonNull
    @Override
    public String toString() {
        return text + " " + audio + " " + sourceUrl;
    }
}
