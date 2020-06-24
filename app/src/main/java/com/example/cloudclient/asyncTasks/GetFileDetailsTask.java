package com.example.cloudclient.asyncTasks;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.cloudclient.activities.DetailActivity;
import com.example.cloudclient.FileDetails;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;

public class GetFileDetailsTask extends AsyncTask <String, Void, FileDetails> {
    private Drive driveService;
    private Activity activity;

    public GetFileDetailsTask(Drive driveService, Activity activity) {
        this.driveService = driveService;
        this.activity = activity;
    }

    @Override
    protected FileDetails doInBackground(String... strings) {
        String fileId = strings[0];

        try {
            File file = driveService.files().get(fileId).setFields("id, name, mimeType, createdTime, size").execute();

            String id = file.getId();
            String name = file.getName();
            Long size = file.getSize();
            String mimeType = file.getMimeType();
            DateTime createdDate = file.getCreatedTime();

            return new FileDetails(id, name, size, mimeType, createdDate);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(FileDetails fileDetails) {
        Intent intent = new Intent(activity, DetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("details", fileDetails);
        intent.putExtras(bundle);
        activity.startActivity(intent);
        super.onPostExecute(fileDetails);
    }
}
