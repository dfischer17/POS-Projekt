package com.example.cloudclient;

import android.content.Intent;
import android.net.Uri;

import com.example.cloudclient.activities.MainActivity;
import com.example.cloudclient.asyncTasks.DeleteTask;
import com.example.cloudclient.asyncTasks.DownloadTask;
import com.example.cloudclient.asyncTasks.GetFileDetailsTask;
import com.example.cloudclient.asyncTasks.GetFilesTask;
import com.example.cloudclient.asyncTasks.PhotoUploadTask;
import com.example.cloudclient.asyncTasks.RenameTask;
import com.example.cloudclient.asyncTasks.UploadTask;
import com.google.api.services.drive.Drive;

public class DriveExplorer {
    private static final String TAG = "DriveExplorer";
    private static final int REQUEST_CODE_DOWNLOAD_FILE = 2;
    public static final String folderMimeType = "application/vnd.google-apps.folder";

    // Google Drive API
    private Drive driveService;

    private MainActivity activity;

    public String lastDownloadId = "";

    public DriveExplorer(Drive driveService, MainActivity activity) {
        this.driveService = driveService;
        this.activity = activity;
    }

    public void loadFilesIntoUI(String folderId) {
        GetFilesTask getFilesTask = new GetFilesTask(driveService, activity);
        getFilesTask.execute(folderId);
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
        UploadTask uploadTask = new UploadTask(driveService, activity);
        uploadTask.execute(path);
    }

    public void downloadFileRequest(String fileId) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra("id", fileId);
        lastDownloadId = fileId;
        activity.startActivityForResult(intent, REQUEST_CODE_DOWNLOAD_FILE);
    }

    public void downloadFile(String fileId, Uri uri) {
        DownloadTask downloadTask = new DownloadTask(driveService, activity);
        downloadTask.execute(fileId, uri);
    }

    public void uploadPhoto(String path, String filename){
        PhotoUploadTask photoUploadTask = new PhotoUploadTask(driveService, activity);
        photoUploadTask.execute(path, filename);
    }

    public void getFileDetails(String fileId) {
        GetFileDetailsTask fileDetailsTask = new GetFileDetailsTask(driveService, activity);
        fileDetailsTask.execute(fileId);
    }
}