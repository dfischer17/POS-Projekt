package com.example.cloudclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.api.services.drive.Drive;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Main extends AppCompatActivity {

    private static final int REQUEST_CODE_CREATE_FILE = 2;
    private static final int REQUEST_CODE_OPEN_FILE = 3;
    private static final int REQUEST_CODE_SAVE_FILE = 4;
    private static final int REQUEST_CODE_UPLOAD_FILE = 5;
    private static final int REQUEST_CODE_EXPLORER_UPLOAD_FILE = 6;

    private Button createFileBtn;
    private Button openFileBtn;
    private Button saveBtn;
    private Button uploadBtn;
    private Button startExplorerBtn;
    private View listView;
    Drive driveService;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        createFileBtn = findViewById(R.id.createBtn);
        openFileBtn = findViewById(R.id.openBtn);
        saveBtn = findViewById(R.id.saveBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        startExplorerBtn = findViewById(R.id.explorerBtn);
        listView = findViewById(R.id.list_item);

        // init listeners
        createFileBtn.setOnClickListener(v -> createFile());
        openFileBtn.setOnClickListener(v -> openFile());
        saveBtn.setOnClickListener(v -> saveFile());
        uploadBtn.setOnClickListener(v -> uploadFile());
        startExplorerBtn.setOnClickListener(v -> startExplorer());


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

    // Zum Testen der Explorer Funktionen
    public void startExplorer() {
        DriveExplorer driveExplorer = new DriveExplorer(driveService, this);
        //driveExplorer.printFiles("root"); // Dateien in Ordner anzeigen
        //driveExplorer.deleteFile("1dzvdc_--ZLq8XQxvgNncIlsDczyx8GPq"); // Datei loeschen
        //driveExplorer.renameFile("1j7XwvBEFc03JixbADRv0z5UrHb8t96CU", "Tschuess"); // Datei umbenennen
        driveExplorer.downloadFile("1AftFNtf_5pOS8QarMuw4QrRgfBqbDDPC", "/storage/self/primary/Download/");
    }

    public void uploadFileExplorer() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, REQUEST_CODE_EXPLORER_UPLOAD_FILE);
    }



}
