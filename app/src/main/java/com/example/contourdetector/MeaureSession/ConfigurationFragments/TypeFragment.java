package com.example.contourdetector.MeaureSession.ConfigurationFragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

import com.bravin.btoast.BToast;
import com.example.contourdetector.R;
import com.example.contourdetector.ServicesPackage.ParameterServer;
import com.example.contourdetector.SetterGetterPackage.ParameterItem;

import org.angmarch.views.NiceSpinner;

import java.util.ArrayList;
import java.util.List;

public class TypeFragment extends Fragment {

    private ParameterServer parameterServer;
    private ParameterItem parameterItem;
    private NiceSpinner typeSpinner;
    private NiceSpinner nonStdSpinner;
    private Button concaveButton;
    private Button convexButton;
    private Button saveButton;
    private Button importButton;
    private EditText concaveEditText;
    private EditText convexEditText;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_type, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViewByIds();
        getActivity().getApplicationContext().bindService(new Intent(getActivity().getApplicationContext(), ParameterServer.class),
                parameterServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void findViewByIds() {
        typeSpinner = getActivity().findViewById(R.id.typeSpinner);
        nonStdSpinner = getActivity().findViewById(R.id.nonstd_spinner);
        concaveButton = getActivity().findViewById(R.id.type_concaveButton);
        convexButton = getActivity().findViewById(R.id.type_convexButton);
        concaveEditText = getActivity().findViewById(R.id.type_concaveInput);
        convexEditText = getActivity().findViewById(R.id.type_convexInput);
        saveButton = getActivity().findViewById(R.id.type_saveConfiguration);
        importButton = getActivity().findViewById(R.id.type_importConfiguration);
    }

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

    private ServiceConnection parameterServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ParameterServer.ParameterBinder parameterBinder = (ParameterServer.ParameterBinder) service;
            parameterServer = parameterBinder.getService();
            typeSpinnerInit();
            nonStdSpinnerInit();
            parameterItem = parameterServer.getParameterItem();
            BToast.success(getActivity().getApplicationContext()).animate(true)
                    .text("类型实时监测服务已开启").show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getApplicationContext().unbindService(parameterServiceConnection);
    }
}
