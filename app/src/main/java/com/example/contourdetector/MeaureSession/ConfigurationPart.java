package com.example.contourdetector.MeaureSession;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.bravin.btoast.BToast;
import com.example.contourdetector.MainActivity;
import com.example.contourdetector.MeaureSession.ConfigurationFragments.DevicesFragment;
import com.example.contourdetector.MeaureSession.ConfigurationFragments.ParameterFragment;
import com.example.contourdetector.MeaureSession.ConfigurationFragments.ScannerFragment;
import com.example.contourdetector.MeaureSession.ConfigurationFragments.TypeFragment;
import com.example.contourdetector.R;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class ConfigurationPart extends AppCompatActivity {

    private BottomNavigationBar bottomNavigationBar;
    private Button backButton;
    private Button goButton;
    private LineChartView lineChartView;
    private List<PointValue> pointValues;
    private TextView headtitleTextView;
    private Fragment[] fragments;
    private int lastFragmentIndex;
    private List<Float> X;
    private List<Float> Y;
    private boolean isProceedable;

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
                startActivity(new Intent(ConfigurationPart.this, DataPart.class));
                finish();
            }
        });
    }

    private void findViewByIds() {
        backButton = findViewById(R.id.backButton);
        goButton = findViewById(R.id.goButton);
        headtitleTextView = findViewById(R.id.headtitleText);
        bottomNavigationBar = findViewById(R.id.configuration_bottombar);
        lineChartView = findViewById(R.id.configuration_scanner);
    }

    private void initFragments() {
        Fragment fragment1 = new DevicesFragment();
        Fragment fragment2 = new TypeFragment();
        Fragment fragment3 = new ParameterFragment();
        Fragment fragment4 = new ScannerFragment();
        fragments = new Fragment[]{fragment1, fragment2, fragment3, fragment4};
        lastFragmentIndex = 0;
        getSupportFragmentManager().beginTransaction().replace(R.id.configuration_fragmentview, fragments[lastFragmentIndex])
                .show(fragments[lastFragmentIndex]).commit();
    }

    private void bottomNavigationBarInit() {
        // 初始化底部导航栏，这里的ActiveColor其实是文字/图标所在的背景色
        // BarBackgroundColor是图标及文字选中时候的颜色，两者在含义上对调了
        bottomNavigationBar.setActiveColor(R.color.colorWhite)
                .setBarBackgroundColor(R.color.colorMainBlue);
        // 添加Tab
        bottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.icon_bluetooth, "设备"))
                .addItem(new BottomNavigationItem(R.drawable.icon_type,"类型"))
                .addItem(new BottomNavigationItem(R.drawable.icon_parameter, "参数"))
                .addItem(new BottomNavigationItem(R.drawable.icon_scanner, "扫描"));
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_SHIFTING);
        bottomNavigationBar.setFirstSelectedPosition(lastFragmentIndex);
        // 单击某个Tab时切换Fragment
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

    // 使用handler来沟通fragment和activity
    // 根据发送过来的信息不同，执行不同的操作
    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null) {
                String messageContent = msg.obj.toString();
                // 预览初始化
                if (messageContent.contains("PREVIEW INIT")) {
                    applyParameterPreviewChange();
                }
                else if (messageContent.contains("PREVIEW UPDATE")) {

                }
                // 扫描初始化，在这里初始化绘图相关的变量，避免因为add造成错误
                else if (messageContent.contains("SCANNER INIT")) {
                    X = new ArrayList<>();
                    Y = new ArrayList<>();
                }
                // 扫描得到的新数据，判断标志位位置，分离出X、Y值，执行绘图命令
                else if (messageContent.contains("SCANNER UPDATE")) {
                    float x = Float.valueOf(messageContent.substring(messageContent.indexOf("/")+1,
                            messageContent.indexOf("#")));
                    float y = Float.valueOf(messageContent.substring(messageContent.indexOf("#")+1));
                    applyScannerLineChartViewChange(x, y);
                }
            }
        }
    };

    // 根据填入的数据画预览，没办法只能用hellochart了，省点力气
    private void applyParameterPreviewChange() {
        pointValues = new ArrayList<>();
        List<Line> lines = new ArrayList<>();
        pointValues.add(new PointValue(-5, 5));
        pointValues.add(new PointValue(-5, 0));
        pointValues.add(new PointValue(5, 0));
        pointValues.add(new PointValue(5, 5));
        pointValues.add(new PointValue(0, 15));
        Line line = new Line(pointValues).setColor(Color.parseColor("#FFCD41"));
        line.setShape(ValueShape.CIRCLE);
        line.setCubic(false);
        line.setPointRadius(2);
        line.setFilled(false);
        line.setHasLabels(false);
        line.setHasLabelsOnlyForSelected(true);
        line.setHasLines(true);
        line.setHasPoints(true);
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        // Coordinate Axis Setting - X
        Axis axisX = new Axis();
        axisX.setHasTiltedLabels(true);
        axisX.setTextColor(Color.BLACK);
        axisX.setName("X(mm)");
        data.setAxisXBottom(axisX);
        axisX.setHasLines(true);
        axisX.setInside(true);
        // Coordinate Axis Setting - Y
        Axis axisY = new Axis();
        axisY.setName("Y(mm)");
        axisY.setTextColor(Color.BLACK);
        axisY.setTextSize(10);
        axisY.setHasLines(true);
        axisY.setInside(true);
        data.setAxisYLeft(axisY);
        // Line Setting
        lineChartView.setInteractive(true);
        lineChartView.setFocusableInTouchMode(true);
        lineChartView.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        lineChartView.setMaxZoom((float)4);
        lineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChartView.setLineChartData(data);
        lineChartView.setVisibility(View.VISIBLE);
        lineChartView.setViewportCalculationEnabled(false);
        Viewport v = new Viewport(lineChartView.getMaximumViewport());
        v.left = -10;
        v.right = 10;
        v.top = 20;
        v.bottom = -5;
        lineChartView.setMaximumViewport(v);
        lineChartView.setCurrentViewport(v);
        lineChartView.startDataAnimation();
    }

    // 更新扫描得到的图线
    private void applyScannerLineChartViewChange(float x, float y) {
        X.add(x);
        Y.add(y);
        // 需要重新绘图，因为点的坐标可能会随着新增加的点而变化，不能仅仅添加最后一个点
        pointValues = new ArrayList<>();
        List<Line> lines = new ArrayList<>();
        for (int i = 0; i < X.size(); i++) {
            pointValues.add(new PointValue(X.get(i), Y.get(i)));
        }
        Line line = new Line(pointValues).setColor(Color.parseColor("#FFCD41"));
        line.setShape(ValueShape.CIRCLE);
        line.setCubic(true);
        line.setPointRadius(2);
        line.setFilled(false);
        line.setHasLabels(false);
        line.setHasLabelsOnlyForSelected(true);
        line.setHasLines(true);
        line.setHasPoints(true);
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        // Coordinate Axis Setting - X
        Axis axisX = new Axis();
        axisX.setHasTiltedLabels(true);
        axisX.setTextColor(Color.BLACK);
        axisX.setName("X(mm)");
        data.setAxisXBottom(axisX);
        axisX.setHasLines(true);
        axisX.setInside(true);
        // Coordinate Axis Setting - Y
        Axis axisY = new Axis();
        axisY.setName("Y(mm)");
        axisY.setTextColor(Color.BLACK);
        axisY.setTextSize(10);
        axisY.setHasLines(true);
        axisY.setInside(true);
        data.setAxisYLeft(axisY);
        // Line Setting
        lineChartView.setInteractive(true);
        lineChartView.setFocusableInTouchMode(true);
        lineChartView.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        lineChartView.setMaxZoom((float)4);
        lineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChartView.setLineChartData(data);
        lineChartView.setVisibility(View.VISIBLE);
        Viewport v = new Viewport(lineChartView.getMaximumViewport());
        lineChartView.setCurrentViewport(v);
        lineChartView.startDataAnimation();
    }

}
