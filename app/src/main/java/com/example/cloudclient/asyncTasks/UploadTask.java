package com.example.cloudclient.asyncTasks;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import androidx.core.content.ContextCompat;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;

import static androidx.core.app.ActivityCompat.requestPermissions;

public class UploadTask extends AsyncTask<String, Void, Void> {
    Drive driveService;
    Activity activity;

    public UploadTask(Drive driveService, Activity activity) {
        this.driveService = driveService;
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(String... strings) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            String path = strings[0];

            // Pfad der Datei
            java.io.File filePath = new java.io.File(path);

            // Information ueber Datei
            String filename = filePath.getName();
            File fileMetadata = new File();
            fileMetadata.setName(filename);

            // Inhalt der Datei
            FileContent mediaContent = new FileContent(null, filePath);
            try {
                driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        return null;
    }
}
