package com.example.cloudclient.asyncTasks;

import android.os.AsyncTask;

import com.example.cloudclient.DriveExplorer;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.List;

public class LoadParentDirectoryTask extends AsyncTask<String, Void, String> {
    private Drive driveService;
    private DriveExplorer driveExplorer;

    public LoadParentDirectoryTask(Drive driveService, DriveExplorer driveExplorer) {
        this.driveService = driveService;
        this.driveExplorer = driveExplorer;
    }

    // id eines grandparent files herausfinden
    @Override
    protected String doInBackground(String... strings) {
        String fileId = strings[0];

        String parentId = "";
        String grandparentId = "";

        try {
            File curDirFile = driveService.files().get(fileId).setFields("parents").execute();
            List<String> parentIds = curDirFile.getParents();

            parentId = parentIds.get(0);

            File parent = driveService.files().get(parentId).setFields("parents").execute();
            List<String> grandparents = parent.getParents();

            grandparentId = grandparents.get(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return grandparentId;
    }

    @Override
    protected void onPostExecute(String s) {
        driveExplorer.loadFilesIntoUI(s);
        super.onPostExecute(s);
    }
}
