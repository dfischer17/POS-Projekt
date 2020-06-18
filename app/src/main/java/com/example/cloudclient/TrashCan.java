package com.example.cloudclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class TrashCan extends AppCompatActivity {

    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String theme = prefs.getString("theme", "lightTheme");
        if(theme.equals("darkTheme")) {
            setTheme(R.style.DarkTheme);
            setContentView(R.layout.activity_trash_can);
        }
        else if(theme.equals("lightTheme")){
            setTheme(R.style.LightTheme);
            setContentView(R.layout.activity_trash_can);
        }

        //Backbutton
        getSupportActionBar().setTitle("TrashCan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
}
