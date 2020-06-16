package com.example.cloudclient;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.example.cloudclient.asyncTasks.DeleteTask;
import com.example.cloudclient.asyncTasks.DownloadTask;
import com.example.cloudclient.asyncTasks.RenameTask;
import com.example.cloudclient.asyncTasks.UploadTask;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import static androidx.core.app.ActivityCompat.requestPermissions;

public class DriveExplorer {
    private static final String TAG = "DriveExplorer";
    private static final int REQUEST_CODE_DOWNLOAD_FILE = 2;
    public static final String folderMimeType = "application/vnd.google-apps.folder";

    // Google Drive API
    private Drive driveService;

    private Activity activity;

    public String lastDownloadId = ""; // todo bessere Loesung finden

    public DriveExplorer(Drive driveService, Activity activity) {
        this.driveService = driveService;
        this.activity = activity;
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
        DeleteTask deleteTask = new DeleteTask(driveService);
        deleteTask.execute(fileId);
    }

    public void renameFile(String fileId, String filename) {
        RenameTask renameTask = new RenameTask(driveService);
        renameTask.execute(fileId, filename);
    }

    public void uploadFile(String path) {
        UploadTask uploadTask = new UploadTask(driveService);
        uploadTask.execute(path);

    }

    public void downloadFileRequest(String fileId) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra("id", fileId);
        lastDownloadId = fileId; // todo bessere Loesung finden
        activity.startActivityForResult(intent, REQUEST_CODE_DOWNLOAD_FILE);
    }

    public void downloadFile(String fileId, Uri uri) {
        DownloadTask downloadTask = new DownloadTask(driveService, activity);
        downloadTask.execute(fileId, uri);
    }
}
