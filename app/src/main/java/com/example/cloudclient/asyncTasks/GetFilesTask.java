package com.example.cloudclient.asyncTasks;

import android.os.AsyncTask;
import android.widget.ListView;

import com.example.cloudclient.MainActivity;
import com.example.cloudclient.R;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.List;

public class GetFilesTask extends AsyncTask<String, Void, List<File>> {
    Drive driveService;
    private MainActivity activity;

    public GetFilesTask(Drive driveService, MainActivity activity) {
        this.driveService = driveService;
        this.activity = activity;
    }

    @Override
    protected List<File> doInBackground(String... strings) {
        String folderId = strings[0];

        FileList fileList = null;
        try {
            fileList = driveService.files().list().setQ("'" + folderId + "' in parents").execute();
            List<File> curDirectoryFiles = fileList.getFiles();
            return curDirectoryFiles;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<File> files) {
        activity.loadCurDirectoryHandler(files);
        super.onPostExecute(files);
    }
}