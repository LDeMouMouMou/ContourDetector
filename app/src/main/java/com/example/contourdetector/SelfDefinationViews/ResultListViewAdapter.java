package com.example.contourdetector.SelfDefinationViews;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.contourdetector.R;
import com.example.contourdetector.SetterGetterPackage.BiasListViewItem;

import java.util.List;

public class ResultListViewAdapter extends ArrayAdapter<BiasListViewItem> {

    private int resourceId;
    private Context context;

    public ResultListViewAdapter(@NonNull Context context, int resource, @NonNull List<BiasListViewItem> objects) {
        super(context, resource, objects);
        this.resourceId = resource;
        this.context = context;
    }



    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        BiasListViewItem biasListViewItem = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        TextView XTextView = view.findViewById(R.id.result_coordinateX);
        TextView YTextView = view.findViewById(R.id.result_coordinateY);
        TextView BiasTextView = view.findViewById(R.id.result_bias);
        if (position != 0) {
            XTextView.setText(String.valueOf(biasListViewItem.getX()));
            YTextView.setText(String.valueOf(biasListViewItem.getY()));
            BiasTextView.setText(String.valueOf(biasListViewItem.getBias()));
        }
        return view;
    }
}
