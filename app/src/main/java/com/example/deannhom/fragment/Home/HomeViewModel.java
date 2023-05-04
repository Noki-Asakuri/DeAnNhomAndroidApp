package com.example.deannhom.fragment.Home;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.deannhom.utils.WordTuple;

public class HomeViewModel extends ViewModel {

    public final MutableLiveData<WordTuple> word;

    public HomeViewModel() {
        word = new MutableLiveData<>();
    }
}
