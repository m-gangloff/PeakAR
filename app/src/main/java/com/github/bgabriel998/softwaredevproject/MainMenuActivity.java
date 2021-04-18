package com.github.bgabriel998.softwaredevproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

public class MainMenuActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    /** Changes view to SettingsActivity */
    public void settingsButton(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /** Changes view to ProfileActivity */
    public void profileButton(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    /** Changes view to CameraActivity */
    public void cameraButton(View view) {
        if(hasCameraPermission()){
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        }
        else{
            requestCameraPermission();
        }
    }

    /** Changes view to CollectionActivity */
    public void collectionButton(View view) {
        Intent intent = new Intent(this, CollectionActivity.class);
        startActivity(intent);
    }

    /** Changes view to RankingsActivity */
    public void rankingsButton(View view) {
        Intent intent = new Intent(this, RankingsActivity.class);
        startActivity(intent);
    }

    /** Changes view to GalleryActivity */
    public void galleryButton(View view) {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent);
    }

    /** Changes view to GalleryActivity */
    public void mapButton(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    /**
     * Checks if the camera permission was already granted
     */
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Creates an AlertDialog to explain why the permission is required and requests the permission
     */
    private void requestCameraPermission() {
        //Create AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);

        //Set title and message
        builder.setTitle("Camera permission required!");
        builder.setMessage("Camera permission is required to be able to use the camera-preview.");

        //Set positive button and opens the permission request on button click
        builder.setPositiveButton("Ok", (dialog, which) -> {
            //Request permission after user clicked on Ok
            ActivityCompat.requestPermissions(
                    this,
                    CAMERA_PERMISSION,
                    CAMERA_REQUEST_CODE
            );
        });

        //Create alertDialog
        AlertDialog alertDialog = builder.create();

        //Make alertDialog appear on screen
        alertDialog.show();
    }

    /**
     * Callback for the result from requesting permissions.
     * Used to open activities as soon as the permission is granted
     * @param requestCode Indicates the permission code
     * @param permissions List of permissions
     * @param grantResults Indicates if permission is granted or not
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST_CODE) {
            // When request is cancelled, the results array are empty
            if((grantResults.length >0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                //Permission granted, start the activity
                Intent intent = new Intent(this, CameraActivity.class);
                startActivity(intent);
            }
        }
    }

    /**
     * Override back button to go to home screen when the back button is pressed
     * instead of going to the initActivity
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}