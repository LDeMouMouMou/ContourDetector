package com.example.contourdetector;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bravin.btoast.BToast;
import com.example.contourdetector.MeaureSession.ConfigurationPart;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startButton = findViewById(R.id.homepage_start);
        Button historyButton = findViewById(R.id.homepage_history);
        Button importButton = findViewById(R.id.homepage_import);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ConfigurationPart.class));
                finish();
            }
        });

    }

}
