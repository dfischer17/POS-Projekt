package com.example.cloudclient.asyncTasks;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.example.cloudclient.FileUtils;
import com.google.api.services.drive.Drive;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static androidx.core.app.ActivityCompat.requestPermissions;

public class DownloadTask extends AsyncTask<Object, Void, Void> {
    private Drive driveService;
    private Activity activity;

    public DownloadTask(Drive driveService, Activity activity) {
        this.driveService = driveService;
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(Object... objects) {
        String fileId = (String) objects[0];
        Uri uri = (Uri) objects[1];

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {

            try {
                FileUtils fileUtils = new FileUtils(activity);
                DocumentFile folder = DocumentFile.fromTreeUri(activity, uri);

                String filename = driveService.files().get(fileId).execute().getName();
                DocumentFile newfile = folder.createFile("text/plain", filename);

                String path = fileUtils.getPath(newfile.getUri());

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
        return null;
    }
}
