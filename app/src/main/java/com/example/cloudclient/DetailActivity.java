package com.example.cloudclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cloudclient.fragments.MainFragment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DetailActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String theme = prefs.getString("theme", "lightTheme");

        if (theme.equals("darkTheme")) {
            setTheme(R.style.DarkTheme);
            setContentView(R.layout.activity_detail);
        } else if (theme.equals("lightTheme")) {
            setTheme(R.style.LightTheme);
            setContentView(R.layout.activity_detail);
        }

        // Init UI
        TextView idView = findViewById(R.id.frag_id);
        TextView nameView = findViewById(R.id.frag_name);
        TextView sizeView = findViewById(R.id.frag_size);
        TextView mimeTypeView = findViewById(R.id.frag_mimeType);
        TextView createdTimeView = findViewById(R.id.frag_createdTime);
        ImageView imageView = findViewById(R.id.imageView);

        // Werte setzen
        FileDetails fileDetails = (FileDetails) getIntent().getSerializableExtra("details");

        String filename = fileDetails.getName();
        String id = fileDetails.getId();
        String size = String.valueOf(fileDetails.getSize());
        String mimeType = fileDetails.getMimeType();

        //Backbutton
        getSupportActionBar().setTitle("Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Created Date parsen
            DateTimeFormatter rfc3339Parser = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            LocalDateTime createdTime = LocalDateTime.parse(fileDetails.getCreatedDate().toStringRfc3339(), rfc3339Parser);

            // In richtiges format ausgeben
            DateTimeFormatter userDatePrinter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        String createdDateString = createdTime.format(userDatePrinter);


        nameView.setText(filename);
        idView.setText(fileDetails.getId());
        sizeView.setText(size + " bytes");
        mimeTypeView.setText(mimeType);
        createdTimeView.setText(createdDateString);

        if (mimeType.equals(DriveExplorer.folderMimeType)){
            imageView.setBackgroundResource(R.drawable.ic_folder);
        }
        else if(mimeType.equals("image/jpeg") || mimeType.equals("image/png") || mimeType.equals("image/gif")){
            imageView.setBackgroundResource(R.drawable.ic_image);
        }
        else if(mimeType.equals("application/pdf")){
            imageView.setBackgroundResource(R.drawable.ic_pdf);
        }
        else if(mimeType.equals("video/mpeg") || mimeType.equals("video/mp4") || mimeType.equals("video/ogg")){
            imageView.setBackgroundResource(R.drawable.ic_video);
        }
        else if(mimeType.equals("audio/mp3") || mimeType.equals("audio/mpeg") || mimeType.equals("audio/mp4") || mimeType.equals("audio/ogg")){
            imageView.setBackgroundResource(R.drawable.ic_music_note);
        }
        else {
            imageView.setBackgroundResource(R.drawable.ic_file);
        }
    }
}
