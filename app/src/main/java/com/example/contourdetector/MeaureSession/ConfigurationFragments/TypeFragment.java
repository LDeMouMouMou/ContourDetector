package com.example.contourdetector.MeaureSession.ConfigurationFragments;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.bravin.btoast.BToast;
import com.example.contourdetector.R;
import com.example.contourdetector.SelfDefinationViews.SavedParameterListViewAdapter;
import com.example.contourdetector.ServicesPackage.ParameterServer;
import com.example.contourdetector.SetterGetterPackage.ParameterItem;

import org.angmarch.views.NiceSpinner;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TypeFragment extends Fragment implements Button.OnClickListener {

    private ParameterServer parameterServer;
    private ParameterItem parameterItem;
    private List<ParameterItem> parameterItemList;
    private NiceSpinner typeSpinner;
    private NiceSpinner nonStdSpinner;
    private Button concaveButton;
    private Button convexButton;
    private EditText concaveEditText;
    private EditText convexEditText;
    private Context context;
    private Dialog savedParameters;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_type, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViewByIds();
        context = getActivity().getApplicationContext();
        context.bindService(new Intent(getActivity().getApplicationContext(), ParameterServer.class),
                parameterServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void findViewByIds() {
        typeSpinner = getActivity().findViewById(R.id.typeSpinner);
        nonStdSpinner = getActivity().findViewById(R.id.nonstd_spinner);
        concaveButton = getActivity().findViewById(R.id.type_concaveButton);
        convexButton = getActivity().findViewById(R.id.type_convexButton);
        concaveEditText = getActivity().findViewById(R.id.type_concaveInput);
        convexEditText = getActivity().findViewById(R.id.type_convexInput);
        // 这俩按钮只是调用相应的方法而已，就直接引用就好了
        getActivity().findViewById(R.id.type_saveConfiguration).setOnClickListener(this);
        getActivity().findViewById(R.id.type_importConfiguration).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.type_saveConfiguration:
                saveCurrentParamter();
                break;
            case R.id.type_importConfiguration:
                showSavedParameters();
                break;
        }
    }

    private ServiceConnection parameterServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ParameterServer.ParameterBinder parameterBinder = (ParameterServer.ParameterBinder) service;
            parameterServer = parameterBinder.getService();
            typeSpinnerInit();
            nonStdSpinnerInit();
            parameterItem = parameterServer.getParameterItem();
//            BToast.success(getActivity().getApplicationContext()).animate(true)
//                    .text("类型实时监测服务已开启").show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    // 初始化封头类型的spinner，可实时保存到parameterItem
    private void typeSpinnerInit() {
        final List<String> typeList = new ArrayList<>();
        typeList.add("椭圆");
        typeList.add("蝶形");
        typeSpinner.attachDataSource(typeList);
        typeSpinner.setSelectedIndex(0);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    parameterItem.setTypeRound(true);
                }
                else {
                    parameterItem.setTypeRound(false);
                }
                parameterServer.setParameterItem(parameterItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    // 初始化非标封头的spinner，可实时保存到parameterItem
    public void nonStdSpinnerInit() {
        final List<String> choiceList = new ArrayList<>();
        choiceList.add(getString(R.string.falseText));
        choiceList.add(getString(R.string.trueText));
        nonStdSpinner.attachDataSource(choiceList);
        nonStdSpinner.setSelectedIndex(0);
        nonStdSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    parameterItem.setNonStandard(false);
                }
                else {
                    parameterItem.setNonStandard(true);
                }
                parameterServer.setParameterItem(parameterItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    // 保存的任务交给ParameterServer
    private void saveCurrentParamter() {
        if (parameterServer.saveCurrentParameterItem()) {
            BToast.success(getContext()).animate(true).text("保存成功").show();
        }
    }

    // 显示保存的参数及部分信息，包括时间、四个量度参数，及删除、应用按钮
    private void showSavedParameters() {
        // 注意Dialog只能应用在Activity层面，后边的View也一样，不然无法创建窗口
        savedParameters = new Dialog(getActivity(), R.style.centerDialog);
        savedParameters.setCancelable(false);
        savedParameters.setCanceledOnTouchOutside(false);
        Window window = savedParameters.getWindow();
        window.setGravity(Gravity.BOTTOM);
        View view = View.inflate(getActivity(), R.layout.type_parametersdialog, null);
        parameterItemList = parameterServer.getSavedParameterItems();
        ListView parameterListView = view.findViewById(R.id.import_type_parameterListView);
        SavedParameterListViewAdapter listViewAdapter = new SavedParameterListViewAdapter(getActivity(),
                R.layout.type_parameterdialog_item, parameterItemList, onClickListener);
        parameterListView.setAdapter(listViewAdapter);
        view.findViewById(R.id.type_import_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savedParameters.dismiss();
            }
        });
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setContentView(view);
        savedParameters.show();
    }

    // 应用因为选择了新的参数而导致的显示变化
    // 但是这个好像没用
    private void applyParameterChange() {
        typeSpinner.setSelectedIndex(parameterItem.isTypeRound()?0:1);
        nonStdSpinner.setSelectedIndex(parameterItem.isNonStandard()?1:0);
    }

    // 给参数列表中的按钮设定监听
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(@NotNull View v) {
            // 获取位置position
            int position = Integer.valueOf(v.getTag().toString().substring(0, 1));
            if (v.getTag().toString().contains("del")) {
                // _id是从1开始计数的
                if (parameterServer.deleteSavedParameterItem(position+1)) {
                    BToast.success(context).animate(true).text("删除成功").show();
                    // 刷新列表
                    savedParameters.dismiss();
                    showSavedParameters();
                }
            }
            else if (v.getTag().toString().contains("apply")) {
                parameterServer.setParameterItem(parameterItemList.get(position));
                savedParameters.dismiss();
                applyParameterChange();
                BToast.success(context).animate(true).text("应用成功").show();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getApplicationContext().unbindService(parameterServiceConnection);
    }

}
