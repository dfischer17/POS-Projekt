package com.example.cloudclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Timeline extends AppCompatActivity {
    private static final String TAG = "timeline";
    private static String filename = "timeline.txt";

    private Button backBtn;

    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener;


    //Timeline Items
    private List<TimelineItem> timelineItems;
    private GridView listViewTimeline;
    ListView lv;
    TimelineAdapter timelineAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String theme = prefs.getString("theme", "lightTheme");
        if(theme.equals("darkTheme")) {
            setTheme(R.style.DarkTheme);
            setContentView(R.layout.activity_timeline);
        }
        else if(theme.equals("lightTheme")){
            setTheme(R.style.LightTheme);
            setContentView(R.layout.activity_timeline);
        }

        //Backbutton
        getSupportActionBar().setTitle("Timeline");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        timelineItems = new ArrayList<>();
        //init UI
        listViewTimeline = findViewById(R.id.timelineListView);
        timelineAdapter = new TimelineAdapter(timelineItems, R.layout.timeline_item, this);
        listViewTimeline.setAdapter(timelineAdapter);
        //load History
        loadHistory();
    }

    private void loadHistory() {
        timelineItems.addAll(readFile());
        timelineAdapter.notifyDataSetChanged();
    }

    public List<TimelineItem> readFile() {
        List<TimelineItem> list = new ArrayList<>();
        try {
            FileInputStream fis = openFileInput(filename);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));
            String line = "";
            while ((line = in.readLine()) != null) {
                String[] temp = line.split(";");
                String desc = temp[0];
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                LocalDateTime dateTime = LocalDateTime.parse(temp[1], formatter);
                DriveAction action = DriveAction.valueOf(temp[2]);
                list.add(new TimelineItem(desc, dateTime, action));
            }
            in.close();
        } catch (IOException exp) {
            Log.d(TAG, exp.getStackTrace().toString());
        }
        Collections.reverse(list);
        return list;
    }

    private void preferenceChanged(SharedPreferences sharedPrefs, String key){
        Intent mIntent = new Intent(this, Timeline.class);
        startActivity(mIntent);
    }

    private void changeToMain(){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}
