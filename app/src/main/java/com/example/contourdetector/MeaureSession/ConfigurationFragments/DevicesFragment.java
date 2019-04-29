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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.bravin.btoast.BToast;
import com.example.contourdetector.R;
import com.example.contourdetector.SelfDefinationViews.BluetoothListViewAdapter;
import com.example.contourdetector.ServicesPackage.BluetoothServer;
import com.example.contourdetector.SetterGetterPackage.BluetoothItem;

import java.util.List;

public class DevicesFragment extends Fragment {

    private Button pairedDeviceList;
    private Button newDeviceList;
    private BluetoothServer bluetoothServer;
    private ListView bluetoothListView;
    private BluetoothListViewAdapter bluetoothListViewAdapter;
    private List<BluetoothItem> bluetoothItemList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_devices, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViewByIds();
        getActivity().getApplicationContext().
                bindService(new Intent(getActivity().getApplicationContext(), BluetoothServer.class),
                bluetoothServiceConnection, Context.BIND_AUTO_CREATE);
        pairedDeviceList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPairedDeviceListView();
            }
        });
        newDeviceList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initNewDeviceListView();
            }
        });
    }

    private void findViewByIds() {
        pairedDeviceList = getActivity().findViewById(R.id.detection_pairedDeivceList);
        newDeviceList = getActivity().findViewById(R.id.detection_addNewDevice);
        bluetoothListView = getActivity().findViewById(R.id.bluetoothListView);
    }

    private ServiceConnection bluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothServer.BluetoothBinder bluetoothBinder = (BluetoothServer.BluetoothBinder) service;
            bluetoothServer = bluetoothBinder.getService();
            BToast.success(getActivity().getApplicationContext())
                    .text("蓝牙服务已开启").animate(true).show();
            initPairedDeviceListView();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void initPairedDeviceListView() {
        bluetoothItemList = bluetoothServer.getBondedDeviceItemList();
        bluetoothListViewAdapter = new BluetoothListViewAdapter(getActivity().getApplicationContext(),
                R.layout.fragment_devices_listviewitem, bluetoothItemList);
        bluetoothListView.setAdapter(bluetoothListViewAdapter);
        bluetoothListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (bluetoothServer.connectSeletectedDevice(bluetoothItemList.get(position).getBluetoothAddress()) == 1) {
                    BToast.info(getActivity().getApplicationContext())
                            .text("正在尝试连接" + bluetoothItemList.get(position).getBluetoothName() + "...")
                            .animate(true).show();
                    bluetoothItemList.get(position).setBluetoothConnected(true);
                }
                else {
                    bluetoothItemList.get(position).setBluetoothConnected(false);
                }
                bluetoothListViewAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initNewDeviceListView() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getApplicationContext().unbindService(bluetoothServiceConnection);
    }
}
