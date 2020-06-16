package com.example.cloudclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import com.google.api.services.drive.Drive;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.ContentValues.TAG;

public class Timeline extends AppCompatActivity {
    private static final String TAG = "timeline";
    private static String filename = "timeline.txt";

    private Button backBtn;


    //Timeline Items
    private List<TimelineItem> timelineItems;
    private ListView listViewTimeline;
    TimelineAdapter timelineAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);

            startActivity(intent);
        });

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
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
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
}
