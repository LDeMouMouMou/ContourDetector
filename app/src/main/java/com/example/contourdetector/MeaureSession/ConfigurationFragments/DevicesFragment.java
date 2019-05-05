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
    private ListView bondedListView;
    private ListView foundedListView;
    private List<BluetoothItem> bondedItemList;
    private List<BluetoothItem> foundedItemList;

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
        bondedListView = getActivity().findViewById(R.id.bondedListView);
        foundedListView = getActivity().findViewById(R.id.foundedListView);
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

    // 配对设备列表
    private void initPairedDeviceListView() {
        // 先隐藏另一个，显示当前需要的列表
        bondedListView.setVisibility(View.VISIBLE);
        foundedListView.setVisibility(View.INVISIBLE);
        bondedItemList = bluetoothServer.getBondedDeviceItemList();
        final BluetoothListViewAdapter bondedBluetoothListViewAdapter = new BluetoothListViewAdapter(getActivity().getApplicationContext(),
                R.layout.fragment_devices_listviewitem, bondedItemList);
        bondedListView.setAdapter(bondedBluetoothListViewAdapter);
        bondedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (bluetoothServer.connectSeletectedDevice(bondedItemList.get(position).getBluetoothAddress()) == 1) {
                    BToast.info(getActivity().getApplicationContext())
                            .text("正在尝试连接" + bondedItemList.get(position).getBluetoothName() + "...")
                            .animate(true).show();
                    bondedItemList.get(position).setBluetoothConnectionState(1);
                }
                else {
                    bondedItemList.get(position).setBluetoothConnectionState(0);
                }
                bondedBluetoothListViewAdapter.notifyDataSetChanged();
            }
        });
    }

    // 未配对设备列表（周围发现的未配对设备）
    private void initNewDeviceListView() {
        bondedListView.setVisibility(View.INVISIBLE);
        foundedListView.setVisibility(View.VISIBLE);
        foundedItemList = bluetoothServer.getNewBluetoothDevice();
        final BluetoothListViewAdapter foundedBluetoothListViewAdapter = new BluetoothListViewAdapter(getActivity().getApplicationContext(),
                R.layout.fragment_devices_listviewitem, foundedItemList);
        foundedListView.setAdapter(foundedBluetoothListViewAdapter);
        foundedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothServer.pairSeletecedDevice(foundedItemList.get(position).getBluetoothAddress());
                foundedBluetoothListViewAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getApplicationContext().unbindService(bluetoothServiceConnection);
    }
}
