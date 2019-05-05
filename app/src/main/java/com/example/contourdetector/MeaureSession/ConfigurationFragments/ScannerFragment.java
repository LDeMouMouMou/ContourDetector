package com.example.contourdetector.MeaureSession.ConfigurationFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.bravin.btoast.BToast;
import com.example.contourdetector.MeaureSession.ConfigurationPart;
import com.example.contourdetector.R;
import com.example.contourdetector.ServicesPackage.BluetoothServer;
import com.example.contourdetector.ServicesPackage.ParameterServer;

import java.util.ArrayList;
import java.util.List;

public class ScannerFragment extends Fragment {

    private BluetoothServer bluetoothServer;
    private ParameterServer parameterServer;
    private Button startButton;
    private Button stopButton;
    private EditText progressText;
    private EditText DText;
    private EditText AText;
    private EditText XYText;
    private Context context;
    private boolean isEnable;
    private ScannerThread scannerThread;
    private Handler handler;
    private List<Float> D;
    private List<Float> A;
    private List<Float> X;
    private List<Float> Y;

    // 使用handler来沟通fragment和activity
    // 在onCreate中获取到目标activity中的handler
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigurationPart configurationPart = (ConfigurationPart) getActivity();
        handler = configurationPart.handler;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scanner, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity().getApplicationContext();
        findViewByIds();
        // 准备异步线程
        scannerThread = new ScannerThread();
        context.bindService(new Intent(context, BluetoothServer.class), bluetoothServiceConnection,
                Context.BIND_AUTO_CREATE);
        context.bindService(new Intent(context, ParameterServer.class), parameterServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    private void findViewByIds() {
        startButton = getActivity().findViewById(R.id.scanner_startbutton);
        stopButton = getActivity().findViewById(R.id.scanner_stopbutton);
        stopButton.setEnabled(false);
        stopButton.setText(R.string.scannerNoStop);
        stopButton.setTextColor(getResources().getColor(R.color.colorLightGray));
        progressText = getActivity().findViewById(R.id.scanner_progressText);
        DText = getActivity().findViewById(R.id.scanner_distanceText);
        AText = getActivity().findViewById(R.id.scanner_angleText);
        XYText = getActivity().findViewById(R.id.scanner_coordinate);
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
            chartControlButtonInit();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void chartControlButtonInit() {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 首先检查参数是否合法，若不合法回到参数设置界面
                if (!parameterServer.getParamterInspectionResult()) {
                    Message message = new Message();
                    message.obj = "SWITCH FRAGMENT/2";
                    handler.sendMessage(message);
                    BToast.error(context).animate(true).text("参数有误").show();
                }
                // 表示进程从未启动过，这是按下开始则启动测量
                // 使能isEnable并将开始按钮的文字变为"暂停测量"
                else if (scannerThread.getStatus() == AsyncTask.Status.PENDING) {
                    isEnable = true;
                    scannerThread.execute();
                    startButton.setText(R.string.scannerPause);
                    // 测量开始后，中止按钮才有效
                    stopButton.setEnabled(true);
                    stopButton.setText(R.string.scannerStop);
                    stopButton.setTextColor(getResources().getColor(R.color.colorBlack));
                }
                // 此时进程正在运行中，改变状态来控制是否接收数据来暂停
                else if (scannerThread.getStatus() == AsyncTask.Status.RUNNING) {
                    // 测量进程正在进行，单击后在暂停/继续之间切换，实质为改变isEnable
                    if (isEnable) {
                        // 暂停时，isEnable置为false，按钮提示"继续"
                        isEnable = false;
                        startButton.setText(R.string.scannerResume);
                    }
                    else {
                        isEnable = true;
                        startButton.setText(R.string.scannerPause);
                    }
                }
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 只有当进程正在运行方可取消
                // 测量取消时开始按钮显示"测量取消"并不可用，中止按钮显示"重新开始"并可用
                // 并重置其他显示
                if (scannerThread.getStatus() == AsyncTask.Status.RUNNING) {
                    isEnable = false;
                    scannerThread.cancel(true);
                    startButton.setEnabled(false);
                    startButton.setText(R.string.scannerCancelledAlready);
                    startButton.setTextColor(getResources().getColor(R.color.colorLightGray));
                    stopButton.setText(R.string.scannerRestart);
                }
                // 被手动取消或者完成时执行重新开始操作，恢复初始状态
                // 开始按钮变为"开始测量"并可用，中止按钮显示"尚未开始"并不可用
                // 重置异步线程，需要用户再次单击（和未开始的情况一样）
                else if (scannerThread.isCancelled() || scannerThread.getStatus() == AsyncTask.Status.FINISHED) {
                    DText.setText(R.string.scannerReady);
                    AText.setText(R.string.scannerReady);
                    XYText.setText(R.string.scannerReady);
                    progressText.setText(R.string.scannerReady);
                    startButton.setEnabled(true);
                    startButton.setText(R.string.scannerStart);
                    startButton.setTextColor(getResources().getColor(R.color.colorBlack));
                    stopButton.setEnabled(false);
                    stopButton.setText(R.string.scannerNoStop);
                    stopButton.setTextColor(getResources().getColor(R.color.colorLightGray));
                    scannerThread = new ScannerThread();
                }
            }
        });
    }

    private class ScannerThread extends AsyncTask<String, Float, String> {

        // 异步线程开始前，发送指令，并将数据列表重置，防止add造成的错误
        @Override
        protected void onPreExecute() {
            D = new ArrayList<>();
            A = new ArrayList<>();
            X = new ArrayList<>();
            Y = new ArrayList<>();
            super.onPreExecute();
            Message message = new Message();
            message.obj = "SCANNER INIT/";
            handler.sendMessage(message);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        // 测量结束后修改两个按钮的文字，并将数据保存到paramterServer
        // 开始按钮变为不可用的"测量结束"，中止按钮变为"重新开始"
        // 将数据发送给parameterServer进行处理
        // 告知activity测量已经结束，可以进入查看结果
        @Override
        protected void onPostExecute(String result) {
            startButton.setText(R.string.scannerFinished);
            startButton.setEnabled(false);
            startButton.setTextColor(getResources().getColor(R.color.colorLightGray));
            stopButton.setText(R.string.scannerRestart);
            parameterServer.setAllDataList(D, X, Y, A);
            Message message = new Message();
            message.obj = "SCANNER FINISHED";
            handler.sendMessage(message);
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
                    publishProgress(receivedDistance, receivedAngle, x, y, progressPercent);
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

        // 更新各个文本框：进度、距离、角度、坐标
        // 并将命令+数据发送给Activity进行图像更新
        @Override
        protected void onProgressUpdate(Float... params) {
            // 将X、Y的值打包发送
            Message message = new Message();
            message.obj = "SCANNER UPDATE/"+params[2]+"#"+params[3];
            handler.sendMessage(message);
            progressText.setText(params[4]+"%");
            DText.setText(params[0]+"毫米");
            AText.setText(params[1]+"度");
            // 保留三位小数
            XYText.setText("X= "+Math.round(params[2]*1000)/1000.0
                    +", Y= "+Math.round(params[3]*1000)/1000.0);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        context.unbindService(bluetoothServiceConnection);
        context.unbindService(parameterServiceConnection);
    }
}
