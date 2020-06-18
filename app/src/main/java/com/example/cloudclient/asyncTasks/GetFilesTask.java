package com.example.cloudclient.asyncTasks;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

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
    ProgressBar progressBar;

    public GetFilesTask(Drive driveService, MainActivity activity) {
        this.driveService = driveService;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        // Ladebildschirm anzeigen
        progressBar = activity.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected List<File> doInBackground(String... strings) {
        String folderId = strings[0];

        FileList fileList = null;
        try {
            fileList = driveService.files().list().setQ("'" + folderId + "' in parents and trashed = false").execute();
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

        // Ladebildschirm ausblenden
        progressBar.setVisibility(View.GONE);
        super.onPostExecute(files);
    }
}
