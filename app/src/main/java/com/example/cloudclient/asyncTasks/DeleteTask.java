package com.example.cloudclient.asyncTasks;

import android.os.AsyncTask;
import com.google.api.services.drive.Drive;
import java.io.IOException;

public class DeleteTask extends AsyncTask<String, Void, Void> {
    private Drive driveService;

    public DeleteTask(Drive driveService) {
        this.driveService = driveService;
    }

    @Override
    protected Void doInBackground(String... strings) {
        String fileId = strings[0];
        try {
            driveService.files().delete(fileId).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
