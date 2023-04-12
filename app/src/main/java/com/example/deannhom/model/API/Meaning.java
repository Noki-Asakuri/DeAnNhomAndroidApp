package com.example.deannhom.model.API;

import java.util.ArrayList;

public class Meaning {
    String partOfSpeech;
    ArrayList<String> definitions;

    public Meaning(String partOfSpeech, ArrayList<String> definitions) {
        this.partOfSpeech = partOfSpeech;
        this.definitions = definitions;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public ArrayList<String> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(ArrayList<String> definitions) {
        this.definitions = definitions;
    }
}
