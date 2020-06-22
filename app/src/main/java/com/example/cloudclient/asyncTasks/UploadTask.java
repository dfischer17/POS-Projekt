package com.example.cloudclient.asyncTasks;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import android.os.AsyncTask;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.example.cloudclient.MainActivity;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;

import static androidx.core.app.ActivityCompat.requestPermissions;

public class UploadTask extends AsyncTask<String, Void, Void> {
    Drive driveService;
    MainActivity mainActivity;
    String filename;

    public UploadTask(Drive driveService, MainActivity mainActivity) {
        this.driveService = driveService;
        this.mainActivity = mainActivity;
    }

    @Override
    protected Void doInBackground(String... strings) {
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            String path = strings[0];

            // Pfad der Datei
            java.io.File filePath = new java.io.File(path);

            // Information ueber Datei
            filename = filePath.getName();
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
            requestPermissions(mainActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mainActivity.newUploadNotification(filename);
    }
}
