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
import java.util.Collections;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
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
                if (isProceedable) {
                    startActivity(new Intent(ConfigurationPart.this, DataPart.class));
                    finish();
                }
                else {
                    BToast.error(ConfigurationPart.this).animate(true).text("无有效测量数据").show();
                }
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
                    // 设定一个初始值
                    applyParameterPreviewChange(10, 5, 8, 0);
                }
                // 更新预览图分离出三个参数
                else if (messageContent.contains("PREVIEW UPDATE")) {
                    // 分离出三个参数
                    float diameter = Float.valueOf(messageContent.substring(messageContent.indexOf("/")+1,
                            messageContent.indexOf("#")));
                    float curvedheight = Float.valueOf(messageContent.substring(messageContent.indexOf("#")+1,
                            messageContent.indexOf("*")));
                    float totalheight = Float.valueOf(messageContent.substring(messageContent.indexOf("*")+1,
                            messageContent.indexOf("$")));
                    float padHeight = Float.valueOf(messageContent.substring(messageContent.indexOf("$")+1));
                    applyParameterPreviewChange(diameter, curvedheight, totalheight, padHeight);
                }
                // 出现错误时切换到参数界面
                else if (messageContent.contains("SWITCH FRAGMENT")) {
                    int toFragmentIndex = Integer.valueOf(messageContent.substring(messageContent.length()-1));
                    switchFragment(lastFragmentIndex, toFragmentIndex);
                    bottomNavigationBar.selectTab(toFragmentIndex);
                }
                // 扫描初始化，在这里初始化绘图相关的变量，避免因为add造成错误
                else if (messageContent.contains("SCANNER INIT")) {
                    X = new ArrayList<>();
                    Y = new ArrayList<>();
                    isProceedable = false;
                }
                // 扫描得到的新数据，判断标志位位置，分离出X、Y值，执行绘图命令
                else if (messageContent.contains("SCANNER UPDATE")) {
                    headtitleTextView.setText("测量中");
                    float x = Float.valueOf(messageContent.substring(messageContent.indexOf("/")+1,
                            messageContent.indexOf("#")));
                    float y = Float.valueOf(messageContent.substring(messageContent.indexOf("#")+1));
                    applyScannerLineChartViewChange(x, y);
                }
                // 表示测量结束，可以进入DataPart，isProceedable置为true
                else if (messageContent.contains("SCANNER FINISHED")) {
                    isProceedable = true;
                }
            }
        }
    };

    // 根据填入的数据画预览，没办法只能用hellochart了，省点力气
    // 画预览图需要知道内径、曲面高、总高、垫块高度四个参数，垫块高度相当于整体上移
    // 将图分为两段，底部点之间的连线是直线，非平滑的，上段的半椭圆连接为平滑的曲线
    // 还有一段虚线部分
    private void applyParameterPreviewChange(float diameter, float curvedheight, float totalheight, float padheight) {
        List<PointValue> linePointValues = new ArrayList<>();
        List<Line> lines = new ArrayList<>();
        Line line;
        // 底端连接四个点：(-diameter/2, totalheight-curvedheight), (-diameter/2, 0), (diameter/2, 0),
        // (diameter/2, totalhegiht-curvedheight)
        linePointValues.add(new PointValue(-diameter/2.0f, totalheight-curvedheight+padheight));
        linePointValues.add(new PointValue(-diameter/2.0f, 0+padheight));
        linePointValues.add(new PointValue(diameter/2.0f, 0+padheight));
        linePointValues.add(new PointValue(diameter/2.0f, totalheight-curvedheight+padheight));
        line = new Line(linePointValues).setColor(Color.parseColor("#FF000000"));
        // setCubic为false表示连线为直线
        line.setCubic(false);
        // 连线且不显示点
        line.setHasLines(true);
        line.setHasPoints(false);
        lines.add(line);
        // 上端为半(椭)圆，单单连接左、上、右三个点看起来可能比较奇怪，所以要计算形成的(椭)圆方程，算出中间点的位置
        List<PointValue> roundPointValue = new ArrayList<>();
        // 圆，这俩应该可以合起来写，但是为了方便理解，就分开来了
        float y0 = totalheight - curvedheight + padheight; // (椭)圆心(0, y0)，圆半径就是curvedheight
        float step = diameter / 100.0f; // 步长，相当于分为101段（包括首尾），太少会导致曲线不平滑，太多了也不好
        if (curvedheight == diameter/2.0f) {
            for (float i = -diameter/2.0f; i <= diameter/2.0f; i += step) {
                roundPointValue.add(new PointValue(i, (float) (Math.sqrt(curvedheight * curvedheight - i * i) + y0)));
            }
        }
        // 椭圆，但长短轴不确定，且圆心不在原点
        // 但可认为在原点，求出点的y值后加上y0
        else {
            // 长轴在X轴，长轴a为内径/2，短轴b为曲面高度，y^2=b*b*(1-x*x/(a*a))
            if (diameter/2 > curvedheight) {
                float a = diameter / 2;
                for (float i = -diameter/2; i <= diameter/2; i += step) {
                    roundPointValue.add(new PointValue(i,
                            (float) (Math.sqrt(curvedheight*curvedheight*(1-i*i/(a*a)))+y0)));
                }
            }
            else {
                float b = diameter / 2;
                for (float i = -diameter/2; i <= diameter/2; i += step) {
                    roundPointValue.add(new PointValue(i,
                            (float) (Math.sqrt(curvedheight*curvedheight*(1-i*i/(b*b)))+y0)));
                }
            }
        }
        line = new Line(roundPointValue).setColor(Color.parseColor("#FFC2C2C2"));
        line.setCubic(true).setHasLines(true).setHasPoints(false);
        lines.add(line);
        // 画连接中部两点的虚线
        // 虚线无法直接生成，只能画点意思一下
        List<PointValue> dashPointValue = new ArrayList<>();
        for (float i = -diameter/2; i <= diameter/2; i += step) {
            dashPointValue.add(new PointValue(i, totalheight-curvedheight+padheight));
        }
        line = new Line(dashPointValue).setColor(Color.parseColor("#FFC2C2C2"));
        line.setHasPoints(true).setPointRadius(1).setHasLines(false);
        lines.add(line);
        // 添加到data中显示
        LineChartData data = new LineChartData();
        data.setLines(lines);

        // 设置X轴
        Axis axisX = new Axis();
        axisX.setHasTiltedLabels(false).setTextColor(Color.BLACK).setHasLines(true).setInside(true);
        data.setAxisXBottom(axisX);
        // 设置Y轴
        Axis axisY = new Axis();
        axisY.setTextColor(Color.BLACK).setHasLines(true).setInside(true);
        data.setAxisYLeft(axisY);
        // 设置交互
        lineChartView.setInteractive(true);
        lineChartView.setFocusableInTouchMode(true);
        lineChartView.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        lineChartView.setMaxZoom((float)4);
        lineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChartView.setLineChartData(data);
        lineChartView.setVisibility(View.VISIBLE);
        // 设置视图，这一步很关键，在默认情况下，图像以图形为边缘，会导致线无法显示
        lineChartView.setViewportCalculationEnabled(false);
        Viewport v = new Viewport(lineChartView.getMaximumViewport());
        v.left = -diameter/2*1.2f;
        v.right = diameter/2*1.2f;
        v.top = padheight+totalheight*1.2f;
        v.bottom = -4;
        lineChartView.setMaximumViewport(v);
        lineChartView.setCurrentViewport(v);
        lineChartView.startDataAnimation();
    }

    // 更新扫描得到的图线
    private void applyScannerLineChartViewChange(float x, float y) {
        X.add(x);
        Y.add(y);
        // 需要重新绘图，因为点的坐标可能会随着新增加的点而变化，不能仅仅添加最后一个点
        List<PointValue> pointValues = new ArrayList<>();
        List<Line> lines = new ArrayList<>();
        for (int i = 0; i < X.size(); i++) {
            pointValues.add(new PointValue(X.get(i), Y.get(i)));
        }
        Line line = new Line(pointValues).setColor(Color.parseColor("#FFCD41"));
        line.setShape(ValueShape.CIRCLE);
        line.setCubic(true);
        line.setPointRadius(1);
        line.setFilled(false);
        line.setHasLabels(false);
        line.setHasLabelsOnlyForSelected(true);
        line.setHasLines(true);
        line.setHasPoints(true);
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        // 设置X轴
        Axis axisX = new Axis();
        axisX.setHasTiltedLabels(true);
        axisX.setTextColor(Color.BLACK);
        axisX.setName("X(mm)");
        data.setAxisXBottom(axisX);
        axisX.setHasLines(true);
        axisX.setInside(true);
        // 设置Y轴
        Axis axisY = new Axis();
        axisY.setName("Y(mm)");
        axisY.setTextColor(Color.BLACK);
        axisY.setTextSize(10);
        axisY.setHasLines(true);
        axisY.setInside(true);
        data.setAxisYLeft(axisY);
        // 设置交互
        lineChartView.setInteractive(true);
        lineChartView.setFocusableInTouchMode(true);
        lineChartView.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        lineChartView.setMaxZoom((float)4);
        lineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChartView.setLineChartData(data);
        lineChartView.setVisibility(View.VISIBLE);
        // 设置视图，放大20%，保证图线边缘留空
        lineChartView.setViewportCalculationEnabled(false);
        Viewport v = new Viewport(lineChartView.getMaximumViewport());
        v.left = Collections.min(X) - (float) (Math.abs(Collections.min(X)) * 0.2);
        v.right = Collections.max(X) + (float) (Math.abs(Collections.max(X)) * 0.2);
        v.top = Collections.max(Y) + (float) (Math.abs(Collections.max(Y) * 0.2));
        v.bottom = Collections.min(Y) - (float) (Math.abs(Collections.min(Y) * 0.2));
        lineChartView.setMaximumViewport(v);
        lineChartView.setCurrentViewport(v);
        lineChartView.startDataAnimation();
    }

}
