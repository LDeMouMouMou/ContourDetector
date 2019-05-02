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
    private Button typeButton;
    private Button nonstdButton;
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
        typeButton = getActivity().findViewById(R.id.type_headtype);
        nonstdButton = getActivity().findViewById(R.id.type_nonstdard);
        typeButton.setOnClickListener(this);
        nonstdButton.setOnClickListener(this);
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
            // 单击parameterItem中对应项，然后应用变化
            // 要改成按钮当前显示的反义，例如当前为椭圆，则单击后变为蝶形，对应item也要变成!equals(椭圆)
            case R.id.type_headtype:
                parameterItem.setTypeRound(!typeButton.getText().toString().equals(getString(R.string.headType_ellipse)));
                applyButtonShowFromParamter();
                break;
            case R.id.type_nonstdard:
                parameterItem.setNonStandard(!nonstdButton.getText().toString().equals(getString(R.string.nonstandardType_true)));
                applyButtonShowFromParamter();
                break;
            case R.id.type_saveConfiguration:
                // 保存当前参数
                saveCurrentParamter();
                break;
            case R.id.type_importConfiguration:
                // 显示保存参数列表
                showSavedParameters();
                break;
        }
    }

    private ServiceConnection parameterServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ParameterServer.ParameterBinder parameterBinder = (ParameterServer.ParameterBinder) service;
            parameterServer = parameterBinder.getService();
            parameterItem = parameterServer.getParameterItem();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    // 根据paramterItem中对应的值刷新按钮的显示
    private void applyButtonShowFromParamter() {
        typeButton.setText(parameterItem.isTypeRound()?
                R.string.headType_ellipse:R.string.headType_butterfly);
        nonstdButton.setText(parameterItem.isNonStandard()?
                R.string.nonstandardType_true:R.string.nonstandardType_false);
    }

    // 保存的任务交给ParameterServer
    private void saveCurrentParamter() {
        if (parameterServer.saveCurrentParameterItem()) {
            BToast.success(getContext()).animate(true).text("保存成功").show();
        }
    }

    // 显示保存的参数及部分信息，包括时间、形状、标准封头、四个量度参数，及删除、应用按钮
    private void showSavedParameters() {
        // 注意Dialog只能应用在Activity层面，后边的View也一样，不然无法创建窗口
        savedParameters = new Dialog(getActivity(), R.style.centerDialog);
        savedParameters.setCancelable(false);
        savedParameters.setCanceledOnTouchOutside(true);
        Window window = savedParameters.getWindow();
        window.setGravity(Gravity.BOTTOM);
        View view = View.inflate(getActivity(), R.layout.type_parametersdialog, null);
        Button dialogTitleText = view.findViewById(R.id.import_type_title);
        Button nothingToShow = view.findViewById(R.id.import_type_nothing1);
        // 获取到参数列表
        parameterItemList = parameterServer.getSavedParameterItems();
        ListView parameterListView = view.findViewById(R.id.import_type_parameterListView);
        // 如果获得了一个空的列表，则显示没有东西的提示，反之则正常显示，并告知有多少个参数
        if (parameterItemList.size() != 0) {
            dialogTitleText.setText(context.getString(R.string.type_import_title)+"("+"共"+parameterItemList.size()+"个"+")：");
            SavedParameterListViewAdapter listViewAdapter = new SavedParameterListViewAdapter(getActivity(),
                    R.layout.type_parameterdialog_item, parameterItemList, onClickListener);
            parameterListView.setAdapter(listViewAdapter);
        }
        else {
            parameterListView.setVisibility(View.INVISIBLE);
            nothingToShow.setVisibility(View.VISIBLE);
        }
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

    // 给参数列表中的按钮设定监听
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(@NotNull View v) {
            // 获取位置position
            int position = Integer.valueOf(v.getTag().toString().substring(0, 1));
            if (v.getTag().toString().contains("del")) {
                // 删除的时候是置位，因此改变了中间项得到的position和实际的_id并不是对应的
                // 前者是导出的未被删除的list中的position，后者则是全部的position，这样会导致删除错误
                // 所以在item中加了一项Id，用来标记它在数据库中的实际位置，删除的时候直接提取即可
                if (parameterServer.deleteSavedParameterItem(parameterItemList.get(position).getId())) {
                    BToast.success(context).animate(true).text("删除成功，刷新列表").show();
                    // 刷新列表
                    savedParameters.dismiss();
                    showSavedParameters();
                }
            }
            else if (v.getTag().toString().contains("apply")) {
                // 单击应用之后，获取到指定位置的paramterItem，替换当前的item并传递到server中
                parameterServer.setParameterItem(parameterItemList.get(position));
                parameterItem = parameterServer.getParameterItem();
                savedParameters.dismiss();
                // 应用变换
                applyButtonShowFromParamter();
                BToast.success(context).animate(true).text("应用成功").show();
            }
        }
    };

    @Override
    public void onDestroy() {
        if (savedParameters != null && savedParameters.isShowing()) {
            savedParameters.dismiss();
            savedParameters = null;
        }
        super.onDestroy();
        getActivity().getApplicationContext().unbindService(parameterServiceConnection);
    }

}
