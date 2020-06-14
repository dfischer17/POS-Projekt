package com.example.cloudclient;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int REQUEST_CODE_DOWNLOAD_FILE = 2;
    private static final int REQUEST_CODE_UPLOAD_FILE = 3;
    private static final int IMAGE_REQUEST = 1;

    // Google Drive API
    Drive driveService;

    // File Management
    private List<File> curDirectory = new ArrayList<>();
    private DriveContentAdapter driveContentAdapter;
    private FloatingActionButton cameraBtn;
    private FloatingActionButton historyBtn;
    private List<TimelineItem> timelineItems;
    private String currentDate;
    private ListView lv;
    ArrayAdapter adapter;

    // Erledigt Drive-Befehle
    DriveExplorer driveExplorer;
    private String currentImagePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Authentication
        requestSignIn();

        // Preferences
        SharedPreferences prefs;
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String theme = prefs.getString("theme", "lightTheme");
        if (theme.equals("darkTheme")) {
            setTheme(R.style.DarkTheme);
            setContentView(R.layout.activity_main);
        } else if (theme.equals("lightTheme")) {
            setTheme(R.style.LightTheme);
            setContentView(R.layout.activity_main);
        }

        // init UI
        ListView curDirectoryLayout = findViewById(R.id.curDirectoryListView);

        // Erlaubt durchlaufen des FileTrees
        curDirectoryLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File clickedFolder = curDirectory.get(position); // todo Aktion auf Ordner beschraenken

                // Ueberpruefen ob Auswahl ein Ordner ist
                if (clickedFolder.getMimeType().equals(DriveExplorer.folderMimeType)) {
                    // Unterordner laden
                    loadCurDirecotry(clickedFolder.getId());
                }
            }
        });

        // Ermoeglicht anzeigen der Context Menues
        registerForContextMenu(curDirectoryLayout);

        // Adapter
        driveContentAdapter = new DriveContentAdapter(curDirectory, R.layout.list_item, this);
        curDirectoryLayout.setAdapter(driveContentAdapter);

        //Camera
        cameraBtn = findViewById(R.id.cameraBtn);
        cameraBtn.setOnClickListener(v -> takePhoto());

        //Timeline
        timelineItems = new ArrayList<>();
        currentDate = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        lv = findViewById(R.id.timelineListView);
        historyBtn = findViewById(R.id.historyBtn);
        historyBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, Timeline.class);
            startActivity(intent);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK && resultData != null) {

            // Login
            if (requestCode == REQUEST_CODE_SIGN_IN) {
                handleSignInResult(resultData);
            }

            // file-management
            else {
                Uri uri = resultData.getData();
                if (requestCode == REQUEST_CODE_DOWNLOAD_FILE) {
                    String fileId = driveExplorer.lastDownloadId; //resultData.getStringExtra("id"); todo bessere Loesung finden
                    driveExplorer.downloadFile(fileId, uri);
                }

                else if (requestCode == REQUEST_CODE_UPLOAD_FILE) {
                    FileUtils fileUtils = new FileUtils(this);
                    String path = fileUtils.getPath(uri);
                    driveExplorer.uploadFile(path);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    private void requestSignIn() {
        Log.d(TAG, "Requesting sign-in");

        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE))
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
                                    this, Collections.singleton(DriveScopes.DRIVE));
                    credential.setSelectedAccount(googleAccount.getAccount());
                    driveService =
                            new Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    new GsonFactory(),
                                    credential)
                                    .setApplicationName("Drive API Migration")
                                    .build();

                    driveExplorer = new DriveExplorer(driveService, this);
                    loadCurDirecotry("root");
                })
                .addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));
    }

    // Fuegt durch getFiles Methode vom Drive Explorer erhaltene Dateien ins UI ein
    public void loadCurDirecotry(String folderId) {
        driveExplorer.getFiles(folderId)
                .addOnSuccessListener(files -> loadCurDirectoryHandler(files));
    }

    public void upload() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, REQUEST_CODE_UPLOAD_FILE);
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settingsBtn:
                Intent temp = new Intent(this, Settings.class);
                startActivity(temp);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Context Menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        int viewId = v.getId();
        if (viewId == R.id.curDirectoryListView) {
            getMenuInflater().inflate(R.menu.context_menu, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        File selectedFile = curDirectory.get(info.position);
        String fileId = selectedFile.getId();
        int selectedAction = item.getItemId();

        switch (selectedAction) {
            case R.id.context_download:
                driveExplorer.downloadFileRequest(fileId);
                timelineItems.add(new TimelineItem(selectedFile.getDescription() + " downloaded", currentDate));
                break;

            case R.id.context_rename:
                final View dialogView = getLayoutInflater().inflate(R.layout.rename_file_dialog, null);
                new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setPositiveButton("Rename", (dialog, which) -> {
                            EditText renameEditText = dialogView.findViewById(R.id.renameEditText);
                            String newFileName = renameEditText.getText().toString();
                            driveExplorer.renameFile(fileId, newFileName);
                            curDirectory.get(info.position).setName(newFileName);
                            updateUI();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                timelineItems.add(new TimelineItem(selectedFile.getDescription() + " renamed", currentDate));
                break;

            case R.id.context_delete:
                new AlertDialog.Builder(this)
                        .setMessage("Do you really want to delete " + selectedFile.getName() + " ?")
                        .setPositiveButton("delete", (dialog, which) -> {
                            driveExplorer.deleteFile(fileId);
                            curDirectory.remove(selectedFile);
                            updateUI();
                        })
                        .setNegativeButton("cancel", null)
                        .show();
                timelineItems.add(new TimelineItem(selectedFile.getDescription() + " deleted", currentDate));
                break;

            default: Log.e(TAG, "Unguelitge Contextmenueauswahl!");
        }
        adapter = new ArrayAdapter(this, R.layout.list_item, timelineItems);

        return super.onContextItemSelected(item);
    }

    // Helper
    private void loadCurDirectoryHandler(List<File> files) {
        curDirectory.clear();
        curDirectory.addAll(files);
        driveContentAdapter.notifyDataSetChanged();
    }

    private void updateUI() {
        driveContentAdapter.notifyDataSetChanged();
    }

    //Camera
    private void takePhoto(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(cameraIntent.resolveActivity(getPackageManager()) != null){
            java.io.File imageFile = null;

            try {
                imageFile = getImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(imageFile != null){
                Uri imageUri = FileProvider.getUriForFile(this, "com.example.cloudclient.fileprovider",imageFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(cameraIntent, IMAGE_REQUEST);
            }
        }
    }

    private java.io.File getImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "jpg_" + timeStamp + "_";
        java.io.File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        java.io.File imageFile = java.io.File.createTempFile(imageName, ".jpg", storageDir);
        currentImagePath = imageFile.getAbsolutePath();
        return imageFile;
    }


}