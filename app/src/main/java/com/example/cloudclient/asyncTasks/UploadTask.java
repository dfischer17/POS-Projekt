package com.example.cloudclient.asyncTasks;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.cloudclient.MainActivity;
import com.example.cloudclient.R;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;

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
        String path = strings[0];

        // Pfad der Datei
        java.io.File filePath = new java.io.File(path);

        // Information ueber Datei
        filename = filePath.getName();
        File fileMetadata = new File();
        fileMetadata.setName(filename);

        // Inhalt der Datei
        FileContent mediaContent = new FileContent("text/plain", filePath);
        try {
            driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mainActivity.newUploadNotification(filename);
    }
}
