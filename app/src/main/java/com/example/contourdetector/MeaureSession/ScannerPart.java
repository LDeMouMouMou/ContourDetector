package com.example.contourdetector.MeaureSession;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.contourdetector.R;
import com.example.contourdetector.ServicesPackage.BluetoothServer;
import com.example.contourdetector.ServicesPackage.ParameterServer;

import java.util.ArrayList;
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

public class ScannerPart extends AppCompatActivity {

    private TextView headTitleTextView;
    private Button startButton;
    private Button stopButton;
    private ProgressBar progressBar;
    private TextView progressTextView;
    private LineChartView lineChartView;
    private BluetoothServer bluetoothServer;
    private ParameterServer parameterServer;
    private boolean isEnable = false;
    private ScannerThread scannerThread;
    String progressText;
    private List<Float> D = new ArrayList<>();
    private List<Float> A = new ArrayList<>();
    private List<Float> X = new ArrayList<>();
    private List<Float> Y = new ArrayList<>();
    private List<PointValue> pointValues = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        findViewByIds();
        headTitleTextView.setText(R.string.scannerTitle);
        scannerThread = new ScannerThread();
        bindService(new Intent(ScannerPart.this, BluetoothServer.class),
                bluetoothServiceConnection, Context.BIND_AUTO_CREATE);
        bindService(new Intent(ScannerPart.this, ParameterServer.class),
                parameterServiceConnection, Context.BIND_AUTO_CREATE);
        chartControlButtonInit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_MUTE: return true;
            default: return false;
        }
    }

    private void findViewByIds() {
        headTitleTextView = findViewById(R.id.headtitleText);
        lineChartView = findViewById(R.id.lineChartView);
        progressBar = findViewById(R.id.scanner_progressBar);
        progressTextView = findViewById(R.id.scanner_progressText);
        startButton = findViewById(R.id.scanner_startbutton);
        stopButton = findViewById(R.id.scanner_stopbutton);
        stopButton.setEnabled(false);
        stopButton.setText(R.string.scannerNoStop);
        stopButton.setTextColor(getResources().getColor(R.color.colorLightGray));
    }

    private ServiceConnection bluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothServer.BluetoothBinder bluetoothBinder = (BluetoothServer.BluetoothBinder) service;
            bluetoothServer = bluetoothBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private ServiceConnection parameterServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ParameterServer.ParameterBinder parameterBinder = (ParameterServer.ParameterBinder) service;
            parameterServer = parameterBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void chartControlButtonInit() {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 表示进程从未启动过，这是按下开始则启动测量
                // 使能isEnable并将开始按钮的文字变为"暂停测量"
                if (scannerThread.getStatus() == AsyncTask.Status.PENDING) {
                    isEnable = true;
                    scannerThread.execute();
                    startButton.setText(R.string.scannerPause);
                    // 测量开始后，中止按钮才有效
                    stopButton.setEnabled(true);
                    stopButton.setText(R.string.scannerStop);
                    stopButton.setTextColor(getResources().getColor(R.color.colorBlack));
                }
                // 此时进程正在运行中，改变状态来控制是否接收数据
                else if (scannerThread.getStatus() == AsyncTask.Status.RUNNING) {
                    // 测量进程正在进行，单击后在暂停/继续之间切换，实质为改变isEnable
                    // 同时修改progressBar下边的进度提示
                    if (isEnable) {
                        isEnable = false;
                        startButton.setText(R.string.scannerResume);
                        progressText += "(已暂停)";
                        progressTextView.setText(progressText);
                    }
                    else {
                        isEnable = true;
                        startButton.setText(R.string.scannerPause);
                        progressText = progressText.substring(0, progressText.indexOf("("));
                        progressTextView.setText(progressText);
                    }
                }
                // 此时测量进程结束，单击即可进入结果界面
                else if (scannerThread.getStatus() == AsyncTask.Status.FINISHED) {
                    startActivity(new Intent(ScannerPart.this, DataPart.class));
                    finish();
                }
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 只有当进程正在运行方可取消，取消后，开始按钮不可用，中止按钮变为返回设置
                if (scannerThread.getStatus() == AsyncTask.Status.RUNNING) {
                    isEnable = false;
                    startButton.setEnabled(false);
                    startButton.setText(R.string.scannerCancelledAlready);
                    startButton.setTextColor(getResources().getColor(R.color.colorLightGray));
                    stopButton.setText(R.string.scannerCancelled);
                    scannerThread.cancel(true);
                    progressText = progressText.substring(0, progressText.indexOf("%")+1)+"(已手动中止)";
                    progressTextView.setText(progressText);
                }
                // 被手动取消
                else if (scannerThread.isCancelled() || scannerThread.getStatus() == AsyncTask.Status.FINISHED) {
                    startActivity(new Intent(ScannerPart.this, ConfigurationPart.class));
                    finish();
                }
            }
        });
    }

    private class ScannerThread extends AsyncTask<String, Float, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onCancelled() {
            isEnable = false;
            super.onCancelled();
        }

        // 测量结束后修改两个按钮的文字，并将数据保存到paramterServer
        @Override
        protected void onPostExecute(String result) {
            progressText += "(已完成)";
            startButton.setText(R.string.scannerFinished);
            stopButton.setText(R.string.scannerCancelled);
            parameterServer.setAllDataList(D, X, Y, A);
        }

        @Override
        protected String doInBackground(String... params) {
            // 180度内的测量点数
            final int totalCount = 20;
            int i = 0;
            while (i <= totalCount && !scannerThread.isCancelled()) {
                if (isEnable) {
                    float receivedDistance = (float) 10.0;
                    float receivedAngle = (float) (180.0 / totalCount * i);
                    float progressPercent = (float) (i * 100 / totalCount);
                    // 根据投影变换计算得到坐标
                    float x = (float) (Math.cos(Math.PI*receivedAngle/180.0)*receivedDistance);
                    float y = (float) (Math.sin(Math.PI*receivedAngle/180.0)*receivedDistance);
                    // 将新的数据添加到列表中，并使用后台服务计算得到新的坐标
                    D.add(receivedDistance);
                    A.add(receivedAngle);
                    X.add(x);
                    Y.add(y);
                    publishProgress(receivedDistance, receivedAngle, progressPercent);
                    i ++;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        // 更新进度及文本框，获取处理后的X、Y列表
        @Override
        protected void onProgressUpdate(Float... params) {
            progressBar.setProgress(params[2].intValue());
            progressText = "正在接收数据："+Math.round(params[2]*100)/100+"%";
            progressTextView.setText(progressText);
            applyLineChartViewChange();
        }
    }

    private void applyLineChartViewChange() {
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

    @Override
    protected void onDestroy() {
        scannerThread = null;
        unbindService(bluetoothServiceConnection);
        unbindService(parameterServiceConnection);
        super.onDestroy();
    }

}
