package com.example.deannhom.model.API;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Word {

    String word;
    ArrayList<Phonetic> phonetics;
    ArrayList<Meaning> meanings;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public ArrayList<Phonetic> getPhonetics() {
        return phonetics;
    }

    public void setPhonetics(ArrayList<Phonetic> phonetics) {
        this.phonetics = phonetics;
    }

    public ArrayList<Meaning> getMeanings() {
        return meanings;
    }

    public void setMeanings(ArrayList<Meaning> meanings) {
        this.meanings = meanings;
    }
}
