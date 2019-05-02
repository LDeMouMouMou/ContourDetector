package com.example.contourdetector.HistorySession;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.contourdetector.MainActivity;
import com.example.contourdetector.R;


public class HistorySelectionPart extends AppCompatActivity implements Button.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        findViewByIds();
    }

    private void findViewByIds() {
        TextView headTitleText = findViewById(R.id.headtitleText);
        headTitleText.setText(R.string.homepageButton2);
        Button backButton = findViewById(R.id.backButton);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backButton:
                startActivity(new Intent(HistorySelectionPart.this, MainActivity.class));
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
