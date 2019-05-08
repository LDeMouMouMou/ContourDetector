package com.example.contourdetector.MeaureSession;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bravin.btoast.BToast;
import com.example.contourdetector.MainActivity;
import com.example.contourdetector.R;
import com.example.contourdetector.SelfDefinationViews.ResultListViewAdapter;
import com.example.contourdetector.ServicesPackage.ParameterServer;
import com.example.contourdetector.SetterGetterPackage.BiasListViewItem;
import com.example.contourdetector.SetterGetterPackage.ParameterItem;
import com.example.contourdetector.SetterGetterPackage.ResultItem;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gdut.bsx.share2.FileUtil;
import gdut.bsx.share2.Share2;
import gdut.bsx.share2.ShareContentType;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class DataPart extends AppCompatActivity implements Button.OnClickListener {

    private LineChartView lineChartView;
    private List<PointValue> pointValueList = new ArrayList<>();
    private Button showOriginalChartButton;
    private Button showBiasChartButton;
    private ListView resultListView;
    private ParameterServer parameterServer;
    private ParameterItem parameterItem;
    private ResultItem resultItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        findViewByIds();
        bindService(new Intent(DataPart.this, ParameterServer.class),
                parameterServiceConnection, Context.BIND_AUTO_CREATE);

    }

    private void findViewByIds() {
        // 能用局部变量还是尽量不要用全局变量比较好，一些按钮都是定义后添加监听就好，其他时候并不会用
        TextView headTitleTextView = findViewById(R.id.headtitleText);
        Button backButton = findViewById(R.id.backButton);
        Button reportButton = findViewById(R.id.goButton);
        lineChartView = findViewById(R.id.data_lineChartView);
        showOriginalChartButton = findViewById(R.id.data_showOriginalChart);
        showOriginalChartButton.setOnClickListener(this);
        showBiasChartButton = findViewById(R.id.data_showBiasChart);
        showBiasChartButton.setOnClickListener(this);
        Button setConditionButton = findViewById(R.id.data_setSearchCondition);
        setConditionButton.setOnClickListener(this);
        resultListView = findViewById(R.id.data_resultListView);
        headTitleTextView.setText(R.string.dataTitle);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(this);
        // reportButton要换成info的图标
        reportButton.setBackgroundResource(R.drawable.button_info);
        reportButton.setVisibility(View.VISIBLE);
        reportButton.setOnClickListener(this);
    }

    private ServiceConnection parameterServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ParameterServer.ParameterBinder parameterBinder = (ParameterServer.ParameterBinder) service;
            parameterServer = parameterBinder.getService();
            // 服务建立后，获取参数Item、结果Item，首先显示报告dialog，并初始化图线为原始数据，偏差表格按照0计算显示
            parameterItem = parameterServer.getParameterItem();
            resultItem = parameterServer.getResultItem();
            showReportDialog();
            applyLineChartView(true);
            initResultListView(0 ,0);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backButton:
                // 先保存数据
                showExitSaveDialog();
                break;
            case R.id.goButton:
                // 这个其实是显示report的按钮，右上角
                showReportDialog();
                break;
            case R.id.data_showOriginalChart:
                applyLineChartView(true);
                break;
            case R.id.data_showBiasChart:
                applyLineChartView(false);
                break;
            case R.id.data_setSearchCondition:
                showSetConditionDialog();
                break;
        }
    }

    // 单击右上角以及刚进入界面时显示的报告 -> 相当于"结果"
    private void showReportDialog() {
        final Dialog reportDialog = new Dialog(DataPart.this, R.style.centerDialog);
        reportDialog.setCancelable(false);
        reportDialog.setCanceledOnTouchOutside(false);
        Window window = reportDialog.getWindow();
        View view = View.inflate(DataPart.this, R.layout.data_reportdialog, null);
        EditText typeText = view.findViewById(R.id.report_type);
        typeText.setHint(parameterItem.isTypeRound()?"椭圆":"蝶形");
        EditText ellipticityText = view.findViewById(R.id.report_ellipticity);
        ellipticityText.setHint(parameterItem.isEllipseDetection()?String.valueOf(resultItem.getEllipticity()):"null");
        EditText diameterText = view.findViewById(R.id.report_diameter);
        diameterText.setHint(String.valueOf(parameterItem.getInsideDiameter()));
        EditText depthText = view.findViewById(R.id.report_depth);
        depthText.setHint(String.valueOf(resultItem.getMaxDepth()));
        EditText maxConcaveBiasText = view.findViewById(R.id.report_maxConcaveBias);
        maxConcaveBiasText.setHint(String.valueOf(resultItem.getMaxConcaveBias()));
        EditText maxConcaveBiasPositionText = view.findViewById(R.id.report_maxConcaveBiasPosition);
        maxConcaveBiasPositionText.setHint("X="+resultItem.getMaxConcaveCorX()+" Y="+resultItem.getMaxConcaveCorY());
        EditText maxConvexBiasText = view.findViewById(R.id.report_maxConvexBias);
        maxConvexBiasText.setHint(String.valueOf(resultItem.getMaxConvexBias()));
        EditText maxConvexBiasPositionText = view.findViewById(R.id.report_maxConvexBiasPosition);
        maxConvexBiasPositionText.setHint("X="+resultItem.getMaxConvexCorX()+" Y="+resultItem.getMaxConvexCorY());
        TextView isQualifiedText = view.findViewById(R.id.report_shape);
        isQualifiedText.setText(resultItem.isQualified()?"合格":"不合格");
        view.findViewById(R.id.report_hide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportDialog.dismiss();
                BToast.info(DataPart.this).text("单击右上角按钮可再次显示").animate(true).show();
            }
        });
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setContentView(view);
        reportDialog.show();
    }

    // 显示原始数据/偏差图线 -> 相当于"偏差"+测量数据的原点变换后
    private void applyLineChartView(boolean isOriginal) {
        // isOriginal=true 显示原始数据的图线，此时原始图线按钮不可用，偏差数图线按钮可用
        // 反之则偏差数据按钮不可用，原始图线按钮可用
        showOriginalChartButton.setEnabled(!isOriginal);
        showOriginalChartButton.setText(isOriginal? (getResources().getString(R.string.dataOriginalChart)+"(当前)"):
                getResources().getString(R.string.dataOriginalChart));
        showOriginalChartButton.setTextColor(isOriginal? getResources().getColor(R.color.colorLightGray):
                getResources().getColor(R.color.colorBlack));
        showBiasChartButton.setEnabled(isOriginal);
        showBiasChartButton.setText(isOriginal? getResources().getString(R.string.dataBiasChart):
                (getResources().getString(R.string.dataBiasChart)+"(当前)"));
        showBiasChartButton.setTextColor(isOriginal? getResources().getColor(R.color.colorBlack):
                getResources().getColor(R.color.colorLightGray));
        // 接下来根据isOriginal刷新图线显示的数据
        List<Float> tempX;
        List<Float> tempY;
        List<Line> lines = new ArrayList<>();
        if (isOriginal) {
            tempX = parameterServer.getDataListOfTypeAfterProcessed("listX");
            tempY = parameterServer.getDataListOfTypeAfterProcessed("listY");
        }
        else {
            tempX = parameterServer.getDataListOfTypeAfterProcessed("listBiasX");
            tempY = parameterServer.getDataListOfTypeAfterProcessed("listBiasY");
        }
        for (int i = 0; i < tempX.size(); i++) {
            pointValueList.add(new PointValue(tempX.get(i), tempY.get(i)));
        }
        Line line = new Line(pointValueList).setColor(Color.parseColor("#FFCD41"));
        line.setShape(ValueShape.CIRCLE);
        line.setCubic(true);
        line.setPointRadius(2);
        line.setFilled(false);
        line.setHasLabels(false);
        line.setHasLabelsOnlyForSelected(true);
        line.setHasLines(isOriginal);
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

    // 设定查询范围的Dialog -> 相当于"数据"左侧
    private void showSetConditionDialog() {
        final Dialog conditionDialog = new Dialog(DataPart.this, R.style.centerDialog);
        conditionDialog.setCancelable(false);
        conditionDialog.setCanceledOnTouchOutside(true);
        Window window = conditionDialog.getWindow();
        window.setGravity(Gravity.CENTER);
        View view = View.inflate(DataPart.this, R.layout.data_setconditiondialog, null);
        final EditText concaveInput = view.findViewById(R.id.condition_concaveInput);
        final EditText convexInput = view.findViewById(R.id.condition_convexInput);
        EditText concaveMax = view.findViewById(R.id.condition_concaveMax);
        EditText convexMax = view.findViewById(R.id.condition_convexMax);
        concaveMax.setHint(String.valueOf(resultItem.getMaxConcaveBias()));
        convexMax.setHint(String.valueOf(resultItem.getMaxConvexBias()));
        view.findViewById(R.id.condition_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float minConcave = isNullEmptyBlank(concaveInput.getText().toString())?0:
                        Float.valueOf(concaveInput.getText().toString());
                float minConvex = isNullEmptyBlank(convexInput.getText().toString())?0:
                        Float.valueOf(convexInput.getText().toString());
                initResultListView(minConcave, minConvex);
                conditionDialog.dismiss();
            }
        });
        view.findViewById(R.id.condition_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conditionDialog.dismiss();
            }
        });
        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        conditionDialog.show();
    }

    // 根据设定的查询范围更新底部的ListView -> 相当于"数据"右侧列表
    private void initResultListView(float concaveBias, float convexBias) {
        List<BiasListViewItem> biasListViewItemList = parameterServer.getBiasListViewItemByCondition(concaveBias, convexBias);
        ResultListViewAdapter resultListViewAdapter = new ResultListViewAdapter(DataPart.this, R.layout.data_resultlistviewitem,
                biasListViewItemList);
        resultListView.setAdapter(resultListViewAdapter);
        resultListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    // 退出时提示是否保存数据 -> 相当于"其他"中的输出
    // 可以选择保存到历史记录
    // 也可以保存为包含报告、参数、具体数据的Excel文件或者仅含有前两者的简单txt文件
    // Excel有固定的格式，事实上在恢复的时候只需要参数(ParameterItem)和具体数据(DataItem)即可
    // 恢复时，只需要解析出Excel中的ParameterItem和D、X、Y、A(来自DataItem)就可以重新计算结果
    private void showExitSaveDialog() {
        final Dialog exitDialog = new Dialog(DataPart.this, R.style.centerDialog);
        exitDialog.setCancelable(false);
        exitDialog.setCanceledOnTouchOutside(true);
        Window window = exitDialog.getWindow();
        View view = View.inflate(DataPart.this, R.layout.data_exitdialog, null);
        view.findViewById(R.id.exit_savehistory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (parameterServer.saveCurrentRecordAsHistory()) {
                    BToast.success(DataPart.this).text("保存成功").animate(true).show();
                }
            }
        });
        view.findViewById(R.id.exit_excel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (parameterServer.createExcelSavingFile()) {
                    exitDialog.dismiss();
                    showFileSavingSuccessDialog();
                }
            }
        });
        view.findViewById(R.id.exit_txt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (parameterServer.createTxtSavingFile()) {
                    exitDialog.dismiss();
                    showFileSavingSuccessDialog();
                }
            }
        });
        view.findViewById(R.id.exit_nosave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitDialog.dismiss();
                startActivity(new Intent(DataPart.this, MainActivity.class));
                finish();
            }
        });
        view.findViewById(R.id.exit_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitDialog.dismiss();
            }
        });
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setContentView(view);
        exitDialog.show();
    }

    // 显示导出成功，可以执行进一步的操作，包括打开、分享、返回等
    private void showFileSavingSuccessDialog() {
        final Dialog successDialog = new Dialog(DataPart.this, R.style.centerDialog);
        successDialog.setCancelable(false);
        successDialog.setCanceledOnTouchOutside(true);
        Window window = successDialog.getWindow();
        View view = View.inflate(DataPart.this, R.layout.data_successdialog, null);
        final String fileName = parameterServer.getDataItem().getFileName();
        final String filePath = parameterServer.getDataItem().getFilePath();
        TextView fileNameText = view.findViewById(R.id.success_filename);
        TextView filePathText = view.findViewById(R.id.success_filepath);
        fileNameText.setText(fileName);
        filePathText.setText(filePath);
        // 使用手机默认的App打开文件，根据文件类型设置不同的DataAndType
        view.findViewById(R.id.success_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(DataPart.this,
                        "com.example.contourdetector.fileprovider", new File(filePath));
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (fileName.contains(".xls")) {
                    intent.setDataAndType(uri, "application/vnd.ms-excel");
                }
                else {
                    intent.setDataAndType(uri, "text/plain");
                }
                startActivityForResult(intent, 1);
            }
        });
        // 使用Share2插件实现调用自带的文件分享功能
        view.findViewById(R.id.success_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Share2.Builder(DataPart.this).setContentType(ShareContentType.FILE)
                        .setShareFileUri(FileUtil.getFileUri(DataPart.this,
                                ShareContentType.FILE, new File(filePath)))
                        .build().shareBySystem();
            }
        });
        // 回到主页面
        view.findViewById(R.id.success_backhome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                successDialog.dismiss();
                startActivity(new Intent(DataPart.this, MainActivity.class));
                finish();
            }
        });
        // 回到上一级界面，就是退出时显示的界面
        view.findViewById(R.id.success_backlast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                successDialog.dismiss();
                showExitSaveDialog();
            }
        });
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setContentView(view);
        successDialog.show();
    }

    @Contract("null -> true")
    private boolean isNullEmptyBlank(String str){
        if (str == null || "".equals(str) || "".equals(str.trim())){
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        unbindService(parameterServiceConnection);
        super.onDestroy();
    }
}
