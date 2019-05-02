package com.example.contourdetector.MeaureSession.ConfigurationFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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

import com.example.contourdetector.MeaureSession.ConfigurationPart;
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
    private EditText insideDiameterInput;
    private EditText curvedHeightInput;
    private EditText totalHeightInput;
    private EditText padHeightInput;
    private Button ellipseCheck;
    private Handler handler;

    // 使用handler来沟通fragment和activity
    // 在onCreate中获取到目标activity中的handler
    // 因为type中没有涉及到具体数值的改变，所以只需要在ParameterFragment中获取即可
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigurationPart configurationPart = (ConfigurationPart) getActivity();
        handler = configurationPart.handler;
        // 唤起显示预览
        Message message = new Message();
        message.obj = "PREVIEW INIT";
        handler.sendMessage(message);
    }

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
    // 也可以根据新获得的paramterItem刷新参数
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            parameterItem = parameterServer.getParameterItem();
            setParameterFromSelection();
        }
    }

    private void findViewByIds() {
        insideRadiusButton = getActivity().findViewById(R.id.parameter_insideRadiusButton);
        insideDiameterInput = getActivity().findViewById(R.id.parameter_insideRadiusInput);
        curvedHeightButton = getActivity().findViewById(R.id.parameter_curvedHeightButton);
        curvedHeightInput = getActivity().findViewById(R.id.parameter_curvedHeightInput);
        totalHeightButton = getActivity().findViewById(R.id.parameter_totalHeightButton);
        totalHeightInput = getActivity().findViewById(R.id.parameter_totalHeightInput);
        padHeightButton = getActivity().findViewById(R.id.parameter_padHeightButton);
        padHeightInput = getActivity().findViewById(R.id.parameter_padHeightInput);
        ellipseCheck = getActivity().findViewById(R.id.parameter_ellipseDetection);
    }

    // 根据新获得的parameterItem刷新参数内容
    private void setParameterFromSelection() {
        // 展开填入新的参数，曲面高度也会被一起保存，所以不需要和封头内径对等
        // 注意当参数值为-1时，留空，这是初始化之后的状态，强行填入会导致输入字符不符要求错误
        insideDiameterInput.setText(parameterItem.getInsideDiameter()==-1?null:String.valueOf(parameterItem.getInsideDiameter()));
        curvedHeightInput.setText(parameterItem.getCurvedHeight()==-1?null:String.valueOf(parameterItem.getCurvedHeight()));
        totalHeightInput.setText(parameterItem.getTotalHeight()==-1?null:String.valueOf(parameterItem.getTotalHeight()));
        padHeightInput.setText(parameterItem.getPadHeight()==-1?null:String.valueOf(parameterItem.getPadHeight()));
        // 重新切换回改fragment后检查是否为非标准封头，如果不是，将曲面高度显示为封头内径的，并使其不可用
        if (!parameterItem.isNonStandard()) {
            curvedHeightInput.setError(null, null);
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
        // 椭圆度的按钮文字也要更改
        ellipseCheck.setText(parameterItem.isEllipseDetection()?
                R.string.nonstandardType_true:R.string.nonstandardType_false);
    }

    private ServiceConnection parameterServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ParameterServer.ParameterBinder parameterBinder = (ParameterServer.ParameterBinder) service;
            parameterServer = parameterBinder.getService();
            initInputWatcher();
            parameterItem = parameterServer.getParameterItem();
            setParameterFromSelection();
//            BToast.success(getActivity().getApplicationContext()).animate(true)
//                    .text("参数实时监测服务已开启").show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    // 给四个EditText添加监听，可以做到空白内容实时监测，同时对于封头内径和曲面高度也可以联动（标准封头）
    // 最重要的是，保存到parameterItem后，可以实现实时画预览图
    private void initInputWatcher() {
        insideDiameterInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // 当封头为标准封头时，曲面高度=封头内径，前者要随着后者的输入而更改
                String insideRadiusInputText = insideDiameterInput.getText().toString();
                if (!isNullEmptyBlank(insideRadiusInputText)) {
                    insideDiameterInput.setBackgroundResource(R.color.colorWhite);
                    parameterItem.setInsideDiameter(Float.parseFloat(insideRadiusInputText));
                    if (!parameterItem.isNonStandard()) {
                        curvedHeightInput.setText(insideRadiusInputText);
                        parameterItem.setCurvedHeight(Float.parseFloat(insideRadiusInputText));
                    }
                }
                else {
                    insideDiameterInput.setError("参数不可为空", null);
                    insideDiameterInput.setBackgroundResource(R.drawable.errortextbackground);
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
        // 若显示为是，则单击后置为否，反之一样
        ellipseCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ellipseCheck.getText().toString().equals(getString(R.string.nonstandardType_true))) {
                    ellipseCheck.setText(R.string.nonstandardType_false);
                    parameterItem.setEllipseDetection(false);
                }
                else {
                    ellipseCheck.setText(R.string.nonstandardType_true);
                    parameterItem.setEllipseDetection(true);
                }
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
