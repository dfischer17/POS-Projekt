package com.example.cloudclient;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import static androidx.core.app.ActivityCompat.requestPermissions;

public class DriveExplorer {
    private static final String TAG = "DriveExplorer";

    // Google Drive API
    private Drive driveService;

    private Activity activity;

    public DriveExplorer(Drive driveService, Activity activity) {
        this.driveService = driveService;
        this.activity = activity;
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

    // Uploaded eine Datei
    private class UploadFileThread implements Runnable {
        private String path;

        public UploadFileThread(String path) {
            this.path = path;
        }

        @Override
        public void run() {
            // Pfad der Datei
            java.io.File filePath = new java.io.File(path);

            // Information ueber Datei
            String filename = filePath.getName();
            File fileMetadata = new File();
            fileMetadata.setName(filename);

            // Inhalt der Datei
            FileContent mediaContent = new FileContent("text/plain", filePath);
            try {
                driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Downloaded eine Datei
    private class DownloadFileThread implements Runnable {
        private String fileId;
        private String path;

        public DownloadFileThread(String fileId, String path) {
            this.fileId = fileId;
            this.path = path;
        }

        @Override
        public void run() {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {

                try {
                    String filename = driveService.files().get(fileId).execute().getName(); // TODO notwendig?

                    // content
                    OutputStream outputStream = new ByteArrayOutputStream();
                    driveService.files().get(fileId)
                            .executeMediaAndDownloadTo(outputStream);
                    String filecontent = outputStream.toString();

                    // create file
                    FileOutputStream fos = new FileOutputStream(path);
                    byte[] buffer = filecontent.getBytes();
                    fos.write(buffer, 0, buffer.length);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else {
                requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    // Gibt einen Task zurueck, welcher Dateien aus Verzeichnis laedt
    public Task<List<File>> getFiles(String folderId) {
        final Executor mExecutor = Executors.newSingleThreadExecutor();
        return Tasks.call(mExecutor, () -> {
            FileList fileList = null;
            try {
                fileList = driveService.files().list().setQ("'" + folderId + "' in parents").execute();
                List<File> curDirectoryFiles = fileList.getFiles();
                return curDirectoryFiles;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        });
    }

    public void deleteFile(String fileId) {
        new Thread(new DeleteFileThread(fileId)).start();
    }

    public void renameFile(String fileId, String filename) {
        new Thread(new RenameFileThread(fileId, filename)).start();
    }

    public void uploadFile(String path) {
        new Thread(new UploadFileThread(path)).start();
    }

    public void downloadFile(String fileId, String path) {
        new Thread(new DownloadFileThread(fileId, path)).start();
    }
}
