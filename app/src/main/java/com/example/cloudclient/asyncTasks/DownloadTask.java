package com.example.cloudclient.asyncTasks;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.documentfile.provider.DocumentFile;

import com.example.cloudclient.activities.MainActivity;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.io.OutputStream;

public class DownloadTask extends AsyncTask<Object, Void, Void> {
    private Drive driveService;
    private MainActivity activity;
    String filename;

    public DownloadTask(Drive driveService, MainActivity activity) {
        this.driveService = driveService;
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(Object... objects) {
        String fileId = (String) objects[0];
        Uri uri = (Uri) objects[1];

        try {
            // Datei mit Namen und MimeType des Drive Files erstellen
            File downloadFile = driveService.files().get(fileId).execute();
            filename = downloadFile.getName();
            String mimeType = downloadFile.getMimeType();

            DocumentFile tree = DocumentFile.fromTreeUri(activity, uri);
            DocumentFile destination = tree.createFile(mimeType, filename);

            // Inhalt des Drive Files uebertragen
            ContentResolver contentResolver = activity.getContentResolver();
            OutputStream stream = contentResolver.openOutputStream(destination.getUri());

            driveService.files().get(fileId)
                    .executeMediaAndDownloadTo(stream);
            stream.flush();
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        activity.newDownloadNotification(filename);
    }
}
