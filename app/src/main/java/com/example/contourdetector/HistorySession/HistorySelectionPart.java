package com.example.contourdetector.HistorySession;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.bravin.btoast.BToast;
import com.example.contourdetector.MainActivity;
import com.example.contourdetector.MeaureSession.DataPart;
import com.example.contourdetector.R;
import com.example.contourdetector.SelfDefinationViews.HistoryListViewAdapter;
import com.example.contourdetector.ServicesPackage.ParameterServer;
import com.example.contourdetector.SetterGetterPackage.HistoryItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class HistorySelectionPart extends AppCompatActivity {

    private ParameterServer parameterServer;
    private ListView historyListView;
    private List<HistoryItem> historyItemList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        findViewByIds();
        bindService(new Intent(HistorySelectionPart.this, ParameterServer.class),
                parameterServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void findViewByIds() {
        TextView headTitleText = findViewById(R.id.headtitleText);
        headTitleText.setText(R.string.homepageButton2);
        Button backButton = findViewById(R.id.backButton);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HistorySelectionPart.this, MainActivity.class));
                finish();
            }
        });
        historyListView = findViewById(R.id.history_listview);
    }

    private ServiceConnection parameterServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ParameterServer.ParameterBinder parameterBinder = (ParameterServer.ParameterBinder) service;
            parameterServer = parameterBinder.getService();
            initHistoryListView();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void initHistoryListView() {
        historyItemList = parameterServer.getHistoryItemList();
        Button nothingText = findViewById(R.id.history_nothing);
        if (historyItemList != null) {
            historyListView.setVisibility(View.VISIBLE);
            nothingText.setVisibility(View.INVISIBLE);
            HistoryListViewAdapter historyListViewAdapter = new HistoryListViewAdapter(HistorySelectionPart.this,
                    R.layout.history_item, historyItemList, onClickListener);
            historyListView.setAdapter(historyListViewAdapter);
        }
        // 如果记录为空，就显示当前记录为空的提示
        else {
            historyListView.setVisibility(View.INVISIBLE);
            nothingText.setVisibility(View.VISIBLE);
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(@NotNull View v) {
            int position = Integer.valueOf(v.getTag().toString().substring(0, 1));
            if (v.getTag().toString().contains("del")) {
                // 根据时间获取完整路径
                String filePath = getApplicationContext().getFilesDir().getPath();
                filePath += "/history_"+historyItemList.get(position).getTime()+".xls";
                if (parameterServer.deleteHistoryItem(filePath)) {
                    BToast.success(HistorySelectionPart.this).animate(true).text("删除成功").show();
                }
                else {
                    BToast.error(HistorySelectionPart.this).animate(true).text("文件不存在或已被删除").show();
                }
                initHistoryListView();
            }
            else if (v.getTag().toString().contains("open")) {
                parameterServer.setHistoryItemAsCurrent(historyItemList.get(position));
                startActivity(new Intent(HistorySelectionPart.this, DataPart.class));
            }
        }
    };

    @Override
    protected void onDestroy() {
        unbindService(parameterServiceConnection);
        super.onDestroy();
    }
}
