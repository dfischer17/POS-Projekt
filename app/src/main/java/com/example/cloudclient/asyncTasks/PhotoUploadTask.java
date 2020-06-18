package com.example.cloudclient.asyncTasks;

import android.os.AsyncTask;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.List;

public class PhotoUploadTask extends AsyncTask<String, Void, Void> {

    private Drive driveService;

    public PhotoUploadTask(Drive driveService) {
        this.driveService = driveService;

    }

    @Override
    protected Void doInBackground(String... strings) {
        File fileMetadata = new File();
        fileMetadata.setName(strings[1]);
        fileMetadata.setMimeType("image/jpeg");

        java.io.File filePath = new java.io.File(strings[0]);
        FileContent mediaContent = new FileContent("image/jpeg", filePath);
        File file = null;
        try {
            file = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("File ID: " + file.getId());
        return null;
    }
}