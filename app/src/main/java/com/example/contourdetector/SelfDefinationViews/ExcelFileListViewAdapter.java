package com.example.contourdetector.SelfDefinationViews;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.example.contourdetector.ImportSession.SelectionPart;
import com.example.contourdetector.R;
import com.example.contourdetector.SetterGetterPackage.FileListViewItem;

import java.util.List;

public class ExcelFileListViewAdapter extends ArrayAdapter<FileListViewItem> {

    private int resourceId;
    private Context context;
    private View.OnClickListener onClickListener;

    public ExcelFileListViewAdapter(@NonNull Context context, int resource, @NonNull List<FileListViewItem> objects,
                                    View.OnClickListener onClickListener) {
        super(context, resource, objects);
        this.context = context;
        this.resourceId = resource;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        FileListViewItem fileListViewItem = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        Button fileName = view.findViewById(R.id.listview_filename);
        Button fileDelete = view.findViewById(R.id.listview_delete);
        Button fileOpen = view.findViewById(R.id.listview_open);
        fileName.setText(fileListViewItem.getFileName());
        if (fileListViewItem.getOpenStatue() == -1) {
            fileOpen.setEnabled(false);
            fileOpen.setText(R.string.import_item_checking);
            fileOpen.setTextColor(getContext().getResources().getColor(R.color.colorLightGray));
        }
        else if (fileListViewItem.getOpenStatue() == 0) {
            fileOpen.setEnabled(false);
            fileOpen.setText(R.string.import_item_invalid);
            fileOpen.setTextColor(getContext().getResources().getColor(R.color.colorLightGray));
        }
        else if (fileListViewItem.getOpenStatue() == 1) {
            fileOpen.setEnabled(true);
            fileOpen.setText(R.string.import_item_open);
            fileOpen.setTextColor(getContext().getResources().getColor(R.color.colorBlack));
        }
        fileDelete.setTag(position+"del");
        fileOpen.setTag(position+"open");
        fileDelete.setOnClickListener(this.onClickListener);
        fileOpen.setOnClickListener(this.onClickListener);
        return view;
    }
}
