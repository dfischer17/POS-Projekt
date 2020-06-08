package com.example.cloudclient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int REQUEST_CODE_DOWNLOAD_FILE = 2;
    private static final int REQUEST_CODE_UPLOAD_FILE = 3;
    
    // Google Drive API
    Drive driveService;
    private List<File> curDirectory = new ArrayList<>();
    private ListView curDirectoryLayout;
    private DriveContentAdapter driveContentAdapter;

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

        // load root directory
        curDirectoryLayout = findViewById(R.id.curDirectoryListView);
        driveContentAdapter = new DriveContentAdapter(curDirectory, R.layout.list_item, this);
        curDirectoryLayout.setAdapter(driveContentAdapter);
        fillListView(curDirectory); // todo loeschen
    }

    // todo loeschen
    private void fillListView(List<File> list) {
        list.add(new File().setName("Test"));
        list.add(new File().setName("Test2"));
        list.add(new File().setName("Test2"));
        driveContentAdapter.notifyDataSetChanged();
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
                    // TODO herausfinden ob Stack Overflow Loesung moeglich ist
                    FileUtils fileUtils = new FileUtils(this);
                    DriveExplorer driveExplorer = new DriveExplorer(driveService, this);

                    DocumentFile folder = DocumentFile.fromTreeUri(this, uri);
                    DocumentFile newfile = folder.createFile("text/plain", "abc.txt");

                    String path = fileUtils.getPath(newfile.getUri());
                    driveExplorer.downloadFile("1YLXh9a20_S03Gote311QLAdH1WhIDGXt", path);
                }

                else if (requestCode == REQUEST_CODE_UPLOAD_FILE) {
                    DriveExplorer driveExplorer = new DriveExplorer(driveService, this);
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
                })
                .addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));
    }

    // Zum Testen der Explorer Funktionen
    public void startExplorer() {
        DriveExplorer driveExplorer = new DriveExplorer(driveService, this);
        //driveExplorer.printFiles("root"); // Dateien in Ordner anzeigen
        //driveExplorer.deleteFile("1dzvdc_--ZLq8XQxvgNncIlsDczyx8GPq"); // Datei loeschen
        //driveExplorer.renameFile("1j7XwvBEFc03JixbADRv0z5UrHb8t96CU", "Tschuess"); // Datei umbenennen
        //upload(); // Datei uploaden
        //download(); // Datei downloaden
    }

    public void download() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_CODE_DOWNLOAD_FILE);
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
}