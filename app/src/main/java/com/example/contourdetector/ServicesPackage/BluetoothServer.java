package com.example.contourdetector.ServicesPackage;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.example.contourdetector.SetterGetterPackage.BluetoothItem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        super.onCreate();
    }

    public List<BluetoothItem> getBondedDeviceItemList() {
        List<BluetoothItem> bluetoothItemList = new ArrayList<>();
        Set<BluetoothDevice> bondedDevices  = bluetoothAdapter.getBondedDevices();
        if (bondedDevices.size() > 0) {
            for (BluetoothDevice device : bondedDevices) {
                bluetoothItemList.add(new BluetoothItem(device.getName(), device.getAddress(),
                        false, false));
            }
        }
        return bluetoothItemList;
    }

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

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startDiscovery() {
        if (!bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.startDiscovery();
        }
    }

    public void stopDiscovery() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }
}
