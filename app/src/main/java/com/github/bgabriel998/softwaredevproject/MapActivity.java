package com.github.bgabriel998.softwaredevproject;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.github.giommok.softwaredevproject.FirebaseAccount;

public class MapActivity extends AppCompatActivity {

    public static OSMMap osmMap = null;
    private ImageButton zoomOnUserLocationButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);



        //Instantiate Map
        osmMap = new OSMMap(this, findViewById(R.id.map));


        //Get user account
        FirebaseAccount account = FirebaseAccount.getAccount();
        //Display markers on the map
        osmMap.setMarkersForDiscoveredPeaks(account,account.isSignedIn());

        osmMap.displayUserLocation();

        zoomOnUserLocationButton = (ImageButton) findViewById(R.id.zoomOnUserLocation);
        zoomOnUserLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                osmMap.zoomOnUserLocation();
            }
        });

    }


}

