package com.example.cloudclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.TextView;

import com.example.cloudclient.fragments.MainFragment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Init UI
        TextView idView = findViewById(R.id.frag_id);
        TextView nameView = findViewById(R.id.frag_name);
        TextView sizeView = findViewById(R.id.frag_size);
        TextView mimeTypeView = findViewById(R.id.frag_mimeType);
        TextView createdTimeView = findViewById(R.id.frag_createdTime);

        // Werte setzen
        FileDetails fileDetails = (FileDetails) getIntent().getSerializableExtra("details");

        String filename = fileDetails.getName();
        String id = fileDetails.getId();
        String size = String.valueOf(fileDetails.getSize());
        String mimeType = fileDetails.getMimeType();

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
    }
}
