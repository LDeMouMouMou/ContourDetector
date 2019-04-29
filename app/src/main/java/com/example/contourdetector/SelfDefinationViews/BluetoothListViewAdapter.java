package com.example.contourdetector.SelfDefinationViews;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.contourdetector.SetterGetterPackage.BluetoothItem;
import com.example.contourdetector.R;

import java.util.List;

public class BluetoothListViewAdapter extends ArrayAdapter<BluetoothItem> {

    private int resourceId;
    private Context context;

    public BluetoothListViewAdapter(@NonNull Context context, int resource, @NonNull List<BluetoothItem> Object) {
        super(context, resource, Object);
        this.resourceId = resource;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        BluetoothItem bluetoothItem = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        TextView nameText = view.findViewById(R.id.bluetoothListView_name);
        TextView addressText = view.findViewById(R.id.bluetoothListView_address);
        nameText.setText(bluetoothItem.getBluetoothName());
        addressText.setText(bluetoothItem.getBluetoothAddress());
        if (bluetoothItem.isBluetoothConnected() || bluetoothItem.isBluetoothPaired()) {
            nameText.setTypeface(Typeface.DEFAULT_BOLD);
            addressText.setTypeface(Typeface.DEFAULT_BOLD);
        }
        return view;
    }
}
