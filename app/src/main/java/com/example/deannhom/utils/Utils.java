package com.example.deannhom.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class Utils {
    public static ActionBar getActionBar(AppCompatActivity ctx) {
        ActionBar actionBar = ctx.getSupportActionBar();

        assert actionBar != null;

        return actionBar;
    }

    public static void showError(EditText input, String message) {
        input.setError(message);
    }

    public static Tuple<Boolean, SharedPreferences.Editor> isDarkMode(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);

        return new Tuple<>(isDarkModeOn, editor);
    }

    public static boolean isDarkMode(Context context, boolean placeholder) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);

        return sharedPreferences.getBoolean("isDarkModeOn", false);
    }
}
