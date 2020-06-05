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

    // Logged alle Dateinamen und IDs eines Ordners in der Google Drive
    private class ListFilesThread implements Runnable {
        private String folderId;

        public ListFilesThread(String folderId) {
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
            listOfFiles.forEach(f -> Log.d(TAG, "filename: " + f.getName() + " id " + f.getId()));
        }
    }

    // Loescht eine bestimmte Datei
    private class DeleteFileThread implements Runnable {
        private String fileId;

        public DeleteFileThread(String fileId) {
            this.fileId = fileId;
        }

        @Override
        public void run() {
            try {
                driveService.files().delete(fileId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Aendert den Namen einer bestimmten Datei
    private class RenameFileThread implements Runnable {
        private String fileId;
        private String filename;

        public RenameFileThread(String fileId, String filename) {
            this.fileId = fileId;
            this.filename = filename;
        }

        @Override
        public void run() {
            // Google Drive benötigt ein leeres File zum updaten/umbenennen
            File temp = new File();
            temp.setName(filename);

            try {
                // Altes File mit Informationen aus neuem updaten
                driveService.files().update(fileId, temp).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void printFiles(String folderId) {
        new Thread(new ListFilesThread(folderId)).start();
    }

    public void deleteFile(String fileId) {
        new Thread(new DeleteFileThread(fileId)).start();
    }

    public void renameFile(String fileId, String filename) {
        new Thread(new RenameFileThread(fileId, filename)).start();
    }
}
