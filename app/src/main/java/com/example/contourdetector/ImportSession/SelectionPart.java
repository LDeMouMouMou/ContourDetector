package com.example.contourdetector.ImportSession;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.bravin.btoast.BToast;
import com.example.contourdetector.MainActivity;
import com.example.contourdetector.MeaureSession.DataPart;
import com.example.contourdetector.R;
import com.example.contourdetector.SelfDefinationViews.ExcelFileListViewAdapter;
import com.example.contourdetector.ServicesPackage.ParameterServer;
import com.example.contourdetector.SetterGetterPackage.FileListViewItem;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SelectionPart extends AppCompatActivity {

    private ParameterServer parameterServer;
    private List<FileListViewItem> fileListViewItemList;
    private ListView fileListView;
    private ExcelFileListViewAdapter fileListViewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);
        findViewByIds();
        bindService(new Intent(SelectionPart.this, ParameterServer.class),
                parameterServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void findViewByIds() {
        TextView headTitleText = findViewById(R.id.headtitleText);
        headTitleText.setText(R.string.homepageButton3);
        Button backButton = findViewById(R.id.backButton);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SelectionPart.this, MainActivity.class));
                finish();
            }
        });
        fileListView = findViewById(R.id.import_fileListView);
    }

    private ServiceConnection parameterServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ParameterServer.ParameterBinder parameterBinder = (ParameterServer.ParameterBinder) service;
            parameterServer = parameterBinder.getService();
            showNoticeB4Import();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    // 在开始导入前，显示路径、文件类型、格式等的提醒
    private void showNoticeB4Import() {
        final Dialog noticeDialog = new Dialog(SelectionPart.this, R.style.centerDialog);
        noticeDialog.setCancelable(false);
        noticeDialog.setCanceledOnTouchOutside(false);
        Window window = noticeDialog.getWindow();
        View view =View.inflate(SelectionPart.this, R.layout.import_noticedialog, null);
        final Button checkPathButton = view.findViewById(R.id.notice_check);
        checkPathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPathButton.setText(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
                checkPathButton.setTextSize(16);
                checkPathButton.setTextColor(getResources().getColor(R.color.colorAccent));
            }
        });
        view.findViewById(R.id.notice_backhomepage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noticeDialog.dismiss();
                startActivity(new Intent(SelectionPart.this, MainActivity.class));
                finish();
            }
        });
        view.findViewById(R.id.notice_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noticeDialog.dismiss();
                showAllExcelFilesInDownload();
            }
        });
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setContentView(view);
        noticeDialog.show();
    }

    // 显示在Downloads目录下找到的所有Excel文件（仅限.xls）
    private void showAllExcelFilesInDownload() {
        // 这个List都是add进来的，所以在更新的时候还是需要重置
        fileListViewItemList = new ArrayList<>();
        String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        File file = new File(dirPath);
        // 获取指定路径下的所有文件的File数组
        File[] files = file.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                // 排除文件夹
                if (!files[i].isDirectory()) {
                    String fileName = files[i].getName();
                    // 排除文件名不包含.xls的文件，最后显示出来
                    // 不在一起判断是为了提前排除文件夹，避免多余操作
                    if (files[i].getName().contains(".xls")) {
                        // 添加到显示列表的Item中，-1表示尚未校验，0表示正在校验，1表示校验通过可以打开
                        fileListViewItemList.add(new FileListViewItem(fileName, -1));
                    }
                }
            }
        }
        fileListViewAdapter = new ExcelFileListViewAdapter(SelectionPart.this, R.layout.import_fileitem,
                fileListViewItemList, onClickListener);
        fileListView.setAdapter(fileListViewAdapter);
        verifyExcelFiles();
    }

    // 检测每个excel文件的有效性，并在item中标记，更新列表
    private void verifyExcelFiles() {
        for (int i = 0; i < fileListViewItemList.size(); i++) {
            // 获取完整的文件路径名
            String excelFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()
                    + "/" + fileListViewItemList.get(i).getFileName();
            // 在fileListViewItem中标记statue，若校验失败则无法打开，按钮不可用
            if (parameterServer.isExcelFileValid(excelFilePath)) {
                fileListViewItemList.get(i).setOpenStatue(1);
            }
            else {
                fileListViewItemList.get(i).setOpenStatue(0);
            }
        }
        BToast.success(SelectionPart.this).animate(true)
                .text("文件筛选完毕").show();
        // 更新列表数据
        fileListViewAdapter.notifyDataSetChanged();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(@NotNull View v) {
            // 首先确定点击的位置及所属文件的完整地址
            int position = Integer.valueOf(v.getTag().toString().substring(0, 1));
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()
                    + "/" + fileListViewItemList.get(position).getFileName();
            // 表示点击了"删除文件"按钮
            if (v.getTag().toString().contains("del")) {
                if (parameterServer.delteExistedExcelFile(filePath)) {
                    BToast.success(SelectionPart.this).animate(true).text("删除文件成功").show();
                    showAllExcelFilesInDownload();
                }
                else {
                    BToast.error(SelectionPart.this).animate(true).text("删除文件失败").show();
                }
            }
            // 表示点击了"查看详细"按钮
            else if (v.getTag().toString().contains("open")) {
                parameterServer.readExcelFromExistedFile(filePath);
                startActivity(new Intent(SelectionPart.this, DataPart.class));
            }
        }
    };

    @Override
    protected void onDestroy() {
        unbindService(parameterServiceConnection);
        super.onDestroy();
    }
}
