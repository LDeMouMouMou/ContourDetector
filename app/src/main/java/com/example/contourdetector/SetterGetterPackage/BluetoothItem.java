package com.example.contourdetector.SetterGetterPackage;

public class BluetoothItem {

    private String bluetoothName;
    private String bluetoothAddress;
    private boolean bluetoothConnected;
    private boolean bluetoothPaired;

    public BluetoothItem(String bluetoothName, String bluetoothAddress, boolean bluetoothConnected, boolean bluetoothPaired) {
        this.bluetoothName = bluetoothName;
        this.bluetoothAddress = bluetoothAddress;
        this.bluetoothConnected = bluetoothConnected;
        this.bluetoothPaired = bluetoothPaired;
    }

    public String getBluetoothName() {
        return bluetoothName;
    }

    public void setBluetoothName(String bluetoothName) {
        this.bluetoothName = bluetoothName;
    }

    public String getBluetoothAddress() {
        return bluetoothAddress;
    }

    public void setBluetoothAddress(String bluetoothAddress) {
        this.bluetoothAddress = bluetoothAddress;
    }

    public boolean isBluetoothConnected() {
        return bluetoothConnected;
    }

    public void setBluetoothConnected(boolean bluetoothConnected) {
        this.bluetoothConnected = bluetoothConnected;
    }

    public boolean isBluetoothPaired() {
        return bluetoothPaired;
    }

    public void setBluetoothPaired(boolean bluetoothPaired) {
        this.bluetoothPaired = bluetoothPaired;
    }
}
