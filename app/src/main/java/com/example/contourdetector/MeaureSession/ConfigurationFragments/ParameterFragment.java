package com.example.contourdetector.MeaureSession.ConfigurationFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.bravin.btoast.BToast;
import com.example.contourdetector.R;
import com.example.contourdetector.ServicesPackage.ParameterServer;
import com.example.contourdetector.SetterGetterPackage.ParameterItem;

import org.jetbrains.annotations.Contract;


public class ParameterFragment extends Fragment {

    private ParameterServer parameterServer;
    private ParameterItem parameterItem;
    private Button insideRadiusButton;
    private Button curvedHeightButton;
    private Button totalHeightButton;
    private Button padHeightButton;
    private EditText insideRadiusInput;
    private EditText curvedHeightInput;
    private EditText totalHeightInput;
    private EditText padHeightInput;
    private CheckBox ellipseCheckBox;
    private CheckBox testDataCheckBox;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parameter, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViewByIds();
        getActivity().getApplicationContext()
                .bindService(new Intent(getActivity().getApplicationContext(), ParameterServer.class),
                        parameterServiceConnection, Context.BIND_AUTO_CREATE);
    }

    // 当该fragment重新获得焦点时，重新获取参数，刷新曲面半径的可输入状态
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            parameterItem = parameterServer.getParameterItem();
            setCurvedHeightInputState();
        }
    }

    private void findViewByIds() {
        insideRadiusButton = getActivity().findViewById(R.id.parameter_insideRadiusButton);
        insideRadiusInput = getActivity().findViewById(R.id.parameter_insideRadiusInput);
        curvedHeightButton = getActivity().findViewById(R.id.parameter_curvedHeightButton);
        curvedHeightInput = getActivity().findViewById(R.id.parameter_curvedHeightInput);
        totalHeightButton = getActivity().findViewById(R.id.parameter_totalHeightButton);
        totalHeightInput = getActivity().findViewById(R.id.parameter_totalHeightInput);
        padHeightButton = getActivity().findViewById(R.id.parameter_padHeightButton);
        padHeightInput = getActivity().findViewById(R.id.parameter_padHeightInput);
        ellipseCheckBox = getActivity().findViewById(R.id.parameter_ellipseDetectionCheckBox);
        testDataCheckBox = getActivity().findViewById(R.id.parameter_testdataCheckBox);
    }

    private void setCurvedHeightInputState() {
        // 重新切换回改fragment后检查是否为非标准封头，如果不是，将曲面高度显示为封头内径的，并使其不可用
        if (!parameterItem.isNonStandard()) {
            curvedHeightInput.setError(null, null);
            curvedHeightInput.setText(insideRadiusInput.getText().toString());
            curvedHeightInput.setTextColor(getResources().getColor(R.color.colorLightGray));
            curvedHeightInput.setEnabled(false);
            curvedHeightInput.setFocusable(false);
            curvedHeightInput.setFocusableInTouchMode(false);
        }
        // 其他情况下可以改动编辑
        else {
            curvedHeightInput.setEnabled(true);
            curvedHeightInput.setError(null, null);
            curvedHeightInput.setTextColor(getResources().getColor(R.color.colorBlack));
            curvedHeightInput.setEnabled(true);
            curvedHeightInput.setFocusable(true);
            curvedHeightInput.setFocusableInTouchMode(true);
        }
    }

    private ServiceConnection parameterServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ParameterServer.ParameterBinder parameterBinder = (ParameterServer.ParameterBinder) service;
            parameterServer = parameterBinder.getService();
            initInputWatcher();
            parameterItem = parameterServer.getParameterItem();
            setCurvedHeightInputState();
            BToast.success(getActivity().getApplicationContext()).animate(true)
                    .text("参数实时监测服务已开启").show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void initInputWatcher() {
        insideRadiusInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // 当封头为标准封头时，曲面高度=封头内径，前者要随着后者的输入而更改
                String insideRadiusInputText = insideRadiusInput.getText().toString();
                if (!isNullEmptyBlank(insideRadiusInputText)) {
                    insideRadiusInput.setBackgroundResource(R.color.colorWhite);
                    parameterItem.setInsideDiameter(Float.parseFloat(insideRadiusInputText));
                    if (!parameterItem.isNonStandard()) {
                        curvedHeightInput.setText(insideRadiusInputText);
                        parameterItem.setCurvedHeight(Float.parseFloat(insideRadiusInputText));
                    }
                }
                else {
                    insideRadiusInput.setError("参数不可为空", null);
                    insideRadiusInput.setBackgroundResource(R.drawable.errortextbackground);
                    if (!parameterItem.isNonStandard()) {
                        curvedHeightInput.setText("");
                    }
                }
                parameterServer.setParameterItem(parameterItem);
            }
        });
        curvedHeightInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // 只有为非标准封头时，曲面高度才能输入，才需要实时监测
                // 不加此判断条件会出现参数不可为空的错误
                String curvedHeightInputText = curvedHeightInput.getText().toString();
                if (parameterItem.isNonStandard()) {
                    if (!isNullEmptyBlank(curvedHeightInputText)) {
                        curvedHeightInput.setBackgroundResource(R.color.colorWhite);
                        parameterItem.setCurvedHeight(Float.parseFloat(curvedHeightInputText));
                    } else {
                        curvedHeightInput.setError("参数不可为空", null);
                        curvedHeightInput.setBackgroundResource(R.drawable.errortextbackground);
                    }
                    parameterServer.setParameterItem(parameterItem);
                }
            }
        });
        totalHeightInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String totalHeightInputText = totalHeightInput.getText().toString();
                if (!isNullEmptyBlank(totalHeightInputText)) {
                    totalHeightInput.setBackgroundResource(R.color.colorWhite);
                    parameterItem.setTotalHeight(Float.parseFloat(totalHeightInputText));
                }
                else {
                    totalHeightInput.setError("参数不可为空", null);
                    totalHeightInput.setBackgroundResource(R.drawable.errortextbackground);
                }
                parameterServer.setParameterItem(parameterItem);
            }
        });
        padHeightInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String padHeightInputText = padHeightInput.getText().toString();
                if (!isNullEmptyBlank(padHeightInputText)) {
                    padHeightInput.setBackgroundResource(R.color.colorWhite);
                    parameterItem.setPadHeight(Float.parseFloat(padHeightInputText));
                }
                else {
                    padHeightInput.setError("参数不可为空", null);
                    padHeightInput.setBackgroundResource(R.drawable.errortextbackground);
                }
                parameterServer.setParameterItem(parameterItem);
            }
        });
        ellipseCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                parameterItem.setEllipseDetection(isChecked);
                parameterServer.setParameterItem(parameterItem);
            }
        });
        testDataCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                parameterItem.setTestData(isChecked);
                parameterServer.setParameterItem(parameterItem);
            }
        });
    }

    @Contract("null -> true")
    private boolean isNullEmptyBlank(String str){
        if (str == null || "".equals(str) || "".equals(str.trim())){
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getApplicationContext().unbindService(parameterServiceConnection);
    }
}
