package com.example.cloudclient;

import android.util.Log;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.IOException;
import java.util.List;

public class DriveExplorer {
    private static final String TAG = "DriveExplorer";

    // Google Drive API
    private Drive driveService;

    public DriveExplorer(Drive driveService) {
        this.driveService = driveService;
    }

    // Logged alle Dateinamen in der Google Drive
    private class Worker implements Runnable {
        private  String folderId;

        public Worker(String folderId) {
            this.folderId = folderId;
        }

        @Override
        public void run() {
            FileList fileList = null;
            try {
                fileList = driveService.files().list().setQ("'" + folderId + "' in parents").execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<File> listOfFiles = fileList.getFiles();
            listOfFiles.forEach(f -> Log.d(TAG, "filename: " + f.getName()));
        }
    }

    public void printFiles(String folderId) {
        new Thread(new Worker(folderId)).start();
    }
}
