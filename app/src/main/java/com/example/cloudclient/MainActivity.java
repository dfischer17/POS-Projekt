package com.example.cloudclient;

import android.app.Activity;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.cloudclient.asyncTasks.LoadParentDirectoryTask;
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int REQUEST_CODE_DOWNLOAD_FILE = 2;
    private static final int REQUEST_CODE_UPLOAD_FILE = 3;

    // Google Drive API
    Drive driveService;

    // File Management
    private List<File> curDirectory = new ArrayList<>();
    private DriveContentAdapter driveContentAdapter;

    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener;

    //History and Camera Menue Items
    private FloatingActionButton menueBtn, cameraBtn, historyBtn;
    private Animation fab_open, fab_close, fab_clock, fab_anticlock;
    Boolean isOpen = false;

    // Erledigt Drive-Befehle
    DriveExplorer driveExplorer;


    //Camera
    private String currentImagePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Authentication
        requestSignIn();

        // Preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        preferencesChangeListener = (sharedPrefs, key) -> preferenceChanged(sharedPrefs, key);
        prefs.registerOnSharedPreferenceChangeListener(preferencesChangeListener);
        String theme = prefs.getString("theme", "lightTheme");

        if (theme.equals("darkTheme")) {
            setTheme(R.style.DarkTheme);
            setContentView(R.layout.activity_main);
        } else if (theme.equals("lightTheme")) {
            setTheme(R.style.LightTheme);
            setContentView(R.layout.activity_main);
        }

        // init UI
        GridView curDirectoryLayout = findViewById(R.id.curDirectoryListView);

        // Erlaubt durchlaufen des FileTrees
        curDirectoryLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File clickedFolder = curDirectory.get(position); // todo Aktion auf Ordner beschraenken

                // Ueberpruefen ob Auswahl ein Ordner ist
                if (clickedFolder.getMimeType().equals(DriveExplorer.folderMimeType)) {

                    // Ueberordner laden
                    if (clickedFolder.getName().equals("Back")) {
                        LoadParentDirectoryTask loadParentDirectory = new LoadParentDirectoryTask(driveService, driveExplorer);
                        loadParentDirectory.execute(clickedFolder.getId());
                    }

                    // Unterordner laden
                    driveExplorer.loadFilesIntoUI(clickedFolder.getId());
                }
            }
        });

        // Ermoeglicht anzeigen der Context Menues
        registerForContextMenu(curDirectoryLayout);

        // Adapter
        driveContentAdapter = new DriveContentAdapter(curDirectory, R.layout.list_item, this);
        curDirectoryLayout.setAdapter(driveContentAdapter);

        //Menue Camera and History Button
        menueBtn = findViewById(R.id.menueFabBtn);
        cameraBtn = findViewById(R.id.cameraBtn);
        historyBtn = findViewById(R.id.historyBtn);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_clock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_clock);
        fab_anticlock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_anticlock);

        menueBtn.setOnClickListener(v -> {
            if (isOpen) {
                cameraBtn.startAnimation(fab_close);
                historyBtn.startAnimation(fab_close);
                menueBtn.startAnimation(fab_anticlock);
                cameraBtn.setClickable(false);
                historyBtn.setClickable(false);
                isOpen = false;
            } else {
                cameraBtn.startAnimation(fab_open);
                historyBtn.startAnimation(fab_open);
                menueBtn.startAnimation(fab_clock);
                cameraBtn.setClickable(true);
                historyBtn.setClickable(true);
                isOpen = true;
            }
        });

        cameraBtn.setOnClickListener(v -> takePhoto());
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
                } else if (requestCode == REQUEST_CODE_UPLOAD_FILE) {
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
                    driveExplorer.loadFilesIntoUI("root");
                })
                .addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));
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
        LocalDateTime ldt = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        switch (selectedAction) {
            case R.id.context_download:
                driveExplorer.downloadFileRequest(fileId);
                writeToFile(new TimelineItem(selectedFile.getName(), ldt, DriveAction.valueOf("DOWNLOAD")));
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
                            writeToFile(new TimelineItem(selectedFile.getName(), ldt, DriveAction.valueOf("RENAME")));
                        })
                        .setNegativeButton("Cancel", null)
                        .show();

                break;

            case R.id.context_delete:
                new AlertDialog.Builder(this)
                        .setMessage("Do you really want to delete " + selectedFile.getName() + " ?")
                        .setPositiveButton("delete", (dialog, which) -> {
                            driveExplorer.deleteFile(fileId);
                            curDirectory.remove(selectedFile);
                            updateUI();
                            writeToFile(new TimelineItem(selectedFile.getName(), ldt, DriveAction.valueOf("DELETE")));
                        })
                        .setNegativeButton("cancel", null)
                        .show();

                break;

            default:
                Log.e(TAG, "Unguelitge Contextmenueauswahl!");
        }

        return super.onContextItemSelected(item);
    }

    // Helper
    public void loadCurDirectoryHandler(List<File> files) {
        curDirectory.clear();

        // Irgendeine fileId des aktuellen Verzeichnisses herausfinden
        if (files != null && files.size() >= 1) {
            String randomFileId = files.get(0).getId();

            // Zurueck Button
            curDirectory.add(new File().setName("Back").setId(randomFileId).setMimeType(DriveExplorer.folderMimeType));

            // Sonstige Dateien
            curDirectory.addAll(files);
            driveContentAdapter.notifyDataSetChanged();
        }


    }

    private void updateUI() {
        driveContentAdapter.notifyDataSetChanged();
    }

    //Camera
    private void takePhoto() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            java.io.File imageFile = null;

            try {
                imageFile = getImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (imageFile != null) {
                Uri imageUri = FileProvider.getUriForFile(this, "com.example.cloudclient.fileprovider", imageFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivity(cameraIntent);
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

    public void writeToFile(TimelineItem input) {
        try {
            FileOutputStream fos = openFileOutput("timeline.txt", MODE_PRIVATE | MODE_APPEND);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(fos));
            out.println(input.toCSVString());
            out.flush();
            out.close();
        } catch (FileNotFoundException exp) {
            Log.d(TAG, exp.getStackTrace().toString());
        }
    }

    private void preferenceChanged(SharedPreferences sharedPrefs, String key) {
        Intent mIntent = new Intent(this, MainActivity.class);
        startActivity(mIntent);
    }
}