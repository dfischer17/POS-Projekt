package com.example.cloudclient.asyncTasks;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import com.example.cloudclient.MainActivity;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PhotoUploadTask extends AsyncTask<String, Void, Void> {

    private Drive driveService;
    private MainActivity mainActivity;
    String fileName;

    public PhotoUploadTask(Drive driveService, MainActivity mainActivity) {
        this.driveService = driveService;
        this.mainActivity = mainActivity;
    }




    @Override
    protected Void doInBackground(String... strings) {
        File fileMetadata = new File();
        fileName = strings[1];
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

    @Override
    protected void onPostExecute(Void aVoid) {
        mainActivity.newPhotoUploadNotification(fileName);
    }

    
}