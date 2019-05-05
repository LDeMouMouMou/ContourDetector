package com.example.contourdetector.ServicesPackage;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

import com.example.contourdetector.SetterGetterPackage.BluetoothItem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothServer extends Service {

    public final IBinder binder = new BluetoothBinder();
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String connectedDeviceAddress = null;
    private BroadcastReceiver blutoothReceiver;
    private List<BluetoothItem> bondedBluetoothDevice = new ArrayList<>();
    private List<BluetoothItem> newBluetoothDevice = new ArrayList<>();

    public class BluetoothBinder extends Binder {
        public BluetoothServer getService() {
            return BluetoothServer.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return;
        }
        if (!bluetoothAdapter.isEnabled())
        {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBluetoothIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(enableBluetoothIntent);
        }
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            Intent startDiscoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startDiscoverable.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startDiscoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(startDiscoverable);
        }
        startDiscovery();
        bluetoothStartReceive();
        super.onCreate();
    }

    // 获取已配对的蓝牙列表，0表示未连接，1表示正在连接，2表示连接成功
    public List<BluetoothItem> getBondedDeviceItemList() {
        bondedBluetoothDevice = new ArrayList<>();
        Set<BluetoothDevice> bondedDevices  = bluetoothAdapter.getBondedDevices();
        if (bondedDevices.size() > 0) {
            for (BluetoothDevice device : bondedDevices) {
                bondedBluetoothDevice.add(new BluetoothItem(device.getName(), device.getAddress(),
                        0, 0));
            }
        }
        return bondedBluetoothDevice;
    }

    // 连接到指定地址的蓝牙设备
    public int connectSeletectedDevice(String address) {
        if (address.equals(connectedDeviceAddress)) {
            return -1;
        }
        else {
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            connectedDeviceAddress = address;
            if (bluetoothDevice != null) {
                stopDiscovery();
                ConnectThread connectThread = new ConnectThread();
                connectThread.start();
            }
        }
        return 1;
    }

    // 新开线程，连接过程是一个耗时操作，会阻塞UI
    class ConnectThread extends Thread {
        @Override
        public void run() {
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                bluetoothSocket = null;
            }
            if (bluetoothSocket != null) {
                try {
                    bluetoothSocket.connect();
                } catch (IOException e) {
                    try {
                        bluetoothSocket.close();
                    } catch (IOException e2) {
                        bluetoothSocket = null;
                    }
                }
                if (bluetoothSocket != null) {
                    try {
                        inputStream = bluetoothSocket.getInputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                        inputStream = null;
                    }
                    try {
                        outputStream = bluetoothSocket.getOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                        outputStream = null;
                    }
                }
            }
        }
    }

    // 使用BroadcasrReceiver接收周围的蓝牙设备信息，并监测连接状态
    private void bluetoothStartReceive() {
        startDiscovery();
        IntentFilter intentFilter = new IntentFilter();
        // 发现新的蓝牙设备
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        // 蓝牙设备的配对请求
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        // 蓝牙设备的配对状态的改变，已配对等
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        // 连接成功
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        // 断开连接
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        // 构建广播
        blutoothReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // 发现新的蓝牙设备，将其添加到列表中，0表示未配对，1表示正在配对，2表示配对成功
                // 去掉名称或者地址无法显示的设备
                if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getName() != null && device.getAddress() != null) {
                        BluetoothItem bluetoothItem = new BluetoothItem(device.getName(), device.getAddress(),
                                0, 0);
                        newBluetoothDevice.add(bluetoothItem);
                    }
                }
                else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    for (int i = 0; i < newBluetoothDevice.size(); i++) {
                        if (newBluetoothDevice.get(i).getBluetoothAddress().equals(device.getAddress())) {
                            // 根据配对的状态设置相应item的标志位
                            newBluetoothDevice.get(i).setBluetoothBondState(device.getBondState()==BluetoothDevice.BOND_NONE?0:(
                                    device.getBondState()==BluetoothDevice.BOND_BONDING?1:(
                                            device.getBondState()==BluetoothDevice.BOND_BONDED?2:0)
                                    ));
                        }
                    }
                }
                else if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED) || action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    for (int i = 0; i < bondedBluetoothDevice.size(); i++) {
                        if (bondedBluetoothDevice.get(i).getBluetoothAddress().equals(device.getAddress())) {
                            // 根据连接的状态设置相应item的标志位
                            bondedBluetoothDevice.get(i).setBluetoothConnectionState(
                                    action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)?2:0
                            );
                        }
                    }
                }
            }
        };
        registerReceiver(blutoothReceiver, intentFilter);
    }

    // 配对到指定设备
    public boolean pairSeletecedDevice(String address) {
        stopDiscovery();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        try {
            Method createBond = BluetoothDevice.class.getMethod("createBond");
            return (Boolean) createBond.invoke(device);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 获取待配对的设备列表，同时去除重复
    // 去掉已经配对的设备
    public List<BluetoothItem> getNewBluetoothDevice() {
        List<String> addressList = new ArrayList<>();
        List<BluetoothItem> bluetoothItemList = new ArrayList<>();
        // 先取出所有的不重复的地址
        for (int i = 0; i < newBluetoothDevice.size(); i++) {
            if (newBluetoothDevice.get(i).getBluetoothAddress() != null) {
                if (!addressList.contains(newBluetoothDevice.get(i).getBluetoothAddress())) {
                    addressList.add(newBluetoothDevice.get(i).getBluetoothAddress());
                }
            }
        }
        // 再将对应地址的item添加进来，因为地址不会重复，所以item也不会重复
        for (int i = 0; i < addressList.size(); i++) {
            for (int j = 0; j < newBluetoothDevice.size(); j++) {
                if (newBluetoothDevice.get(j).getBluetoothAddress().equals(addressList.get(i))) {
                    bluetoothItemList.add(newBluetoothDevice.get(j));
                    break;
                }
            }
        }
        return bluetoothItemList;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    private void startDiscovery() {
        if (!bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.startDiscovery();
        }
    }

    private void stopDiscovery() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(blutoothReceiver);
        super.onDestroy();
    }

}
