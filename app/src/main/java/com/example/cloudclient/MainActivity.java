package com.example.cloudclient;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int REQUEST_CODE_CREATE_FILE = 2;
    private static final int REQUEST_CODE_OPEN_FILE = 3;
    private static final int REQUEST_CODE_SAVE_FILE = 4;
    private static final int REQUEST_CODE_UPLOAD_FILE = 5;

    // Google Drive API
    Drive googleDriveService;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    private TextView contentEditText;
    private Button createFileBtn;
    private Button openFileBtn;
    private Button saveBtn;
    private Button uploadBtn;

    private String lastUploadId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Authentication
        requestSignIn();

        // init UI
        contentEditText = findViewById(R.id.contentEditText);
        createFileBtn = findViewById(R.id.createBtn);
        openFileBtn = findViewById(R.id.openBtn);
        saveBtn = findViewById(R.id.saveBtn);
        uploadBtn = findViewById(R.id.uploadBtn);

        // init listeners
        findViewById(R.id.createBtn).setOnClickListener(v -> createFile());
        findViewById(R.id.openBtn).setOnClickListener(v -> openFile());
        findViewById(R.id.saveBtn).setOnClickListener(v -> saveFile());
        findViewById(R.id.uploadBtn).setOnClickListener(v -> uploadFile());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // Ist Activity Result OK und gibt es ein Ergebnis
        if (resultCode == Activity.RESULT_OK && resultData != null) {

            // Login
            if (requestCode == REQUEST_CODE_SIGN_IN) {
                handleSignInResult(resultData);
            }

            // file-management
            else {
                Uri uri = resultData.getData();
                String content = "";

                // create File
                if (requestCode == REQUEST_CODE_CREATE_FILE) {
                    contentEditText.setText("");
                    Log.d(TAG, "Datei erstellt");
                }

                // open File
                else if (requestCode == REQUEST_CODE_OPEN_FILE) {
                    try {
                        content = readTextFromUri(uri);
                        Log.d(TAG, "Datei geoeffnet");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Fehler bei URI umwandeln");
                    }

                }

                // save File
                else if (requestCode == REQUEST_CODE_SAVE_FILE) {
                    String filecontent = contentEditText.getText().toString();

                    writeTextToFile(uri, filecontent);
                    Log.d(TAG, "Datei gespeichert");
                }

                // upload file
                else if (requestCode == REQUEST_CODE_UPLOAD_FILE) {
                    // Datei zum Upload einlesen
                    String filename = getFileName(uri);
                    String filecontent = "";
                    try {
                        filecontent = readTextFromUri(uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Fehler bei URI umwandeln");
                    }

                    // Datei in Google Drive hochladen
                    String finalFilecontent = filecontent;
                    createFileTask(filename)
                            // TODO Kann man Google File erstellen und Inhalt angeben in einen Schritt machen?
                            .addOnSuccessListener(new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(String s) {
                                    // Wenn Datei erfolgreich hochgeladen Text einfuegen
                                    updateFileTask(lastUploadId, filename, finalFilecontent);
                                }
                            })
                            .addOnFailureListener(exception ->
                                    Log.e(TAG, "Datei " + filename + " konnte nicht hochgeladen werden!"));

                    Log.d(TAG, "Datei " + filename + " hochgeladen");
                }

                contentEditText.setText(content);
            }
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    private void requestSignIn() {
        Log.d(TAG, "Requesting sign-in");

        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    Log.d(TAG, "Signed in as " + googleAccount.getEmail());

                    // Use the authenticated account to sign in to the Drive service.
                    GoogleAccountCredential credential =
                            GoogleAccountCredential.usingOAuth2(
                                    this, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleAccount.getAccount());
                    googleDriveService =
                            new Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    new GsonFactory(),
                                    credential)
                                    .setApplicationName("Drive API Migration")
                                    .build();
                })
                .addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));
    }

    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "untitled.txt");
        startActivityForResult(intent, REQUEST_CODE_CREATE_FILE);
    }

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, REQUEST_CODE_OPEN_FILE);
    }

    public void saveFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, REQUEST_CODE_SAVE_FILE);
    }

    public void uploadFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, REQUEST_CODE_UPLOAD_FILE);
    }

    // Tasks
    private Task<String> createFileTask(String name) {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType("text/plain")
                    .setName(name);

            File googleFile = googleDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            lastUploadId = googleFile.getId();
            return googleFile.getId();
        });
    }

    public Task<Void> updateFileTask(String fileId, String name, String content) {
        return Tasks.call(mExecutor, () -> {
            // Create a File containing any metadata changes.
            File metadata = new File().setName(name);

            // Convert content to an AbstractInputStreamContent instance.
            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

            // Update the metadata and contents.
            googleDriveService.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }

    // Utility
    private String readTextFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream =
                     getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }
    private void writeTextToFile(Uri uri, String text) {
        try {
            ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(text.getBytes());

            fileOutputStream.close();
            pfd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}