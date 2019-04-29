package com.example.contourdetector.MeaureSession;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.example.contourdetector.MainActivity;
import com.example.contourdetector.MeaureSession.ConfigurationFragments.DevicesFragment;
import com.example.contourdetector.MeaureSession.ConfigurationFragments.ParameterFragment;
import com.example.contourdetector.MeaureSession.ConfigurationFragments.TypeFragment;
import com.example.contourdetector.R;

public class ConfigurationPart extends AppCompatActivity {

    BottomNavigationBar bottomNavigationBar;
    Button backButton;
    Button goButton;
    TextView headtitleTextView;
    private Fragment[] fragments;
    private int lastFragmentIndex;
    private boolean isParameterValid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        findViewByIds();
        headtitleTextView.setText(R.string.configurationTitle);
        backButton.setVisibility(View.VISIBLE);
        goButton.setVisibility(View.VISIBLE);
        initFragments();
        bottomNavigationBarInit();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ConfigurationPart.this, MainActivity.class));
                finish();
            }
        });
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ConfigurationPart.this, ScannerPart.class));
                finish();
            }
        });
    }

    private void findViewByIds() {
        backButton = findViewById(R.id.backButton);
        goButton = findViewById(R.id.goButton);
        headtitleTextView = findViewById(R.id.headtitleText);
        bottomNavigationBar = findViewById(R.id.configuration_bottombar);
    }

    private void initFragments() {
        Fragment fragment1 = new DevicesFragment();
        Fragment fragment2 = new TypeFragment();
        Fragment fragment3 = new ParameterFragment();
        fragments = new Fragment[]{fragment1, fragment2, fragment3};
        lastFragmentIndex = 1;
        getSupportFragmentManager().beginTransaction().replace(R.id.configuration_fragmentview, fragments[lastFragmentIndex])
                .show(fragments[lastFragmentIndex]).commit();
    }

    private void bottomNavigationBarInit() {
        bottomNavigationBar.setActiveColor(R.color.colorLightGray)
                .setBarBackgroundColor(R.color.colorPrimary);
        bottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.icon_bluetooth, "设备"))
                .addItem(new BottomNavigationItem(R.drawable.icon_type,"类型"))
                .addItem(new BottomNavigationItem(R.drawable.icon_parameter, "参数"));
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_SHIFTING);
        bottomNavigationBar.setFirstSelectedPosition(lastFragmentIndex);
        bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                if (position != lastFragmentIndex) {
                    switchFragment(lastFragmentIndex, position);
                    lastFragmentIndex = position;
                }
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {

            }
        });
        bottomNavigationBar.initialise();
    }

    private void switchFragment(int lastfragment, int index) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.hide(fragments[lastfragment]);
        if (!fragments[index].isAdded()) {
            fragmentTransaction.add(R.id.configuration_fragmentview, fragments[index]);
        }
        fragmentTransaction.show(fragments[index]).commitAllowingStateLoss();
    }

}
