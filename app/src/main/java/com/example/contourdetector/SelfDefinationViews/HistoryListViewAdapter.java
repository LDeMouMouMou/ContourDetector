package com.example.contourdetector.SelfDefinationViews;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.example.contourdetector.R;
import com.example.contourdetector.SetterGetterPackage.HistoryItem;

import java.util.List;

public class HistoryListViewAdapter extends ArrayAdapter<HistoryItem> {

    private int resouceId;
    private Context context;
    private View.OnClickListener onClickListener;

    public HistoryListViewAdapter(@NonNull Context context, int resource, @NonNull List<HistoryItem> objects,
                                  View.OnClickListener onClickListener) {
        super(context, resource, objects);
        this.resouceId = resource;
        this.context = context;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        HistoryItem historyItem = getItem(position);
        View view = LayoutInflater.from(context).inflate(resouceId, parent, false);
        Button timeText = view.findViewById(R.id.history_time);
        Button qualifiedText = view.findViewById(R.id.history_qualified);
        Button nonstdText = view.findViewById(R.id.history_nonstd);
        EditText maxConcaveText = view.findViewById(R.id.history_maxConcave);
        EditText maxConvexText = view.findViewById(R.id.history_maxConvex);
        Button deleteButton = view.findViewById(R.id.history_delete);
        Button openButton = view.findViewById(R.id.history_open);
        timeText.setText(context.getString(R.string.history_time)+historyItem.getTime());
        qualifiedText.setText(historyItem.getQualified());
        nonstdText.setText(historyItem.getType());
        maxConcaveText.setText(historyItem.getMaxConcave()+"mm");
        maxConvexText.setText(historyItem.getMaxConvex()+"mm");
        deleteButton.setTag(position+"del");
        openButton.setTag(position+"open");
        deleteButton.setOnClickListener(this.onClickListener);
        openButton.setOnClickListener(this.onClickListener);
        return view;
    }
}
