package com.example.cloudclient.asyncTasks;

import android.os.AsyncTask;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;

public class RenameTask extends AsyncTask<String, Void, Void> {
    private Drive driveService;

    public RenameTask(Drive driveService) {
        this.driveService = driveService;
    }

    @Override
    protected Void doInBackground(String... strings) {
        String fileId = strings[0];
        String filename = strings[1];

        // Google Drive benötigt ein leeres File als Basis zum updaten/umbenennen
        File temp = new File();
        temp.setName(filename);

        try {
            // Altes File mit Informationen aus neuem updaten
            driveService.files().update(fileId, temp).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
