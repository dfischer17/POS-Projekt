package com.example.cloudclient.asyncTasks;

import android.os.AsyncTask;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;

public class UploadTask extends AsyncTask<String, Void, Void> {
    Drive driveService;

    public UploadTask(Drive driveService) {
        this.driveService = driveService;
    }

    @Override
    protected Void doInBackground(String... strings) {
        String path = strings[0];

        // Pfad der Datei
        java.io.File filePath = new java.io.File(path);

        // Information ueber Datei
        String filename = filePath.getName();
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
}
