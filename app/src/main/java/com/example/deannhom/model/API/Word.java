package com.example.deannhom.model.API;

import java.util.ArrayList;

public class Word {

    String word;
    Phonetic phonetic;
    ArrayList<Meaning> meanings;

    public void setWord(String word) {
        this.word = word;
    }

    public Phonetic getPhonetics() {
        return phonetic;
    }

    public void setPhonetics(Phonetic phonetic) {
        this.phonetic = phonetic;
    }

    public ArrayList<Meaning> getMeanings() {
        return meanings;
    }

    public void setMeanings(ArrayList<Meaning> meanings) {
        this.meanings = meanings;
    }
}
