package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class RankingsActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Rankings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rankings);
        ToolbarHandler.SetupToolbar(this, TOOLBAR_TITLE);
    }
}