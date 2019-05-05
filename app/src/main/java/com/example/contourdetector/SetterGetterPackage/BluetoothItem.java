package com.example.contourdetector.SetterGetterPackage;

public class BluetoothItem {

    private String bluetoothName;
    private String bluetoothAddress;
    private int bluetoothConnectionState;
    private int bluetoothBondState;

    public BluetoothItem(String bluetoothName, String bluetoothAddress, int bluetoothConnectionState, int bluetoothBondState) {
        this.bluetoothName = bluetoothName;
        this.bluetoothAddress = bluetoothAddress;
        this.bluetoothConnectionState = bluetoothConnectionState;
        this.bluetoothBondState = bluetoothBondState;
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

    public int getBluetoothConnectionState() {
        return bluetoothConnectionState;
    }

    public void setBluetoothConnectionState(int bluetoothConnectionState) {
        this.bluetoothConnectionState = bluetoothConnectionState;
    }

    public int getBluetoothBondState() {
        return bluetoothBondState;
    }

    public void setBluetoothBondState(int bluetoothBondState) {
        this.bluetoothBondState = bluetoothBondState;
    }
}
