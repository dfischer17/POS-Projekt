package com.example.cloudclient.asyncTasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.example.cloudclient.MainActivity;
import com.example.cloudclient.R;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.List;

public class GetDeletedFilesTask extends AsyncTask<Void, Void, List<File>> {
    private Drive driveService;
    private Activity activity;
    private ProgressBar progressBar;

    public GetDeletedFilesTask(Drive driveService, Activity activity) {
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
    protected List<File> doInBackground(Void... voids) {
        FileList deletedFiles = null;
        try {
            deletedFiles = driveService.files().list().setQ("trashed = true").execute();
            List<File> curDirectoryFiles = deletedFiles.getFiles();
            return curDirectoryFiles;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<File> files) {
        //activity.loadCurDirectoryHandler(files); // TODO neuen Handler fuer Trash Activity schreiben

        // Ladebildschirm ausblenden
        progressBar.setVisibility(View.GONE);
        super.onPostExecute(files);
    }
}
