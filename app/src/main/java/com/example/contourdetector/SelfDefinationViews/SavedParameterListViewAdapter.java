package com.example.contourdetector.SelfDefinationViews;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.example.contourdetector.R;
import com.example.contourdetector.SetterGetterPackage.ParameterItem;

import java.util.List;

public class SavedParameterListViewAdapter extends ArrayAdapter<ParameterItem> {

    private int resourceId;
    private Context context;
    private View.OnClickListener onClickListener;

    public SavedParameterListViewAdapter(@NonNull Context context, int resource, @NonNull List<ParameterItem> objects,
                                         View.OnClickListener onClickListener) {
        super(context, resource, objects);
        this.resourceId = resource;
        this.context = context;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ParameterItem parameterItem = getItem(position);
        View view = LayoutInflater.from(context).inflate(resourceId, parent, false);
        Button timeText = view.findViewById(R.id.paramter_savetime);
        Button typeText = view.findViewById(R.id.type_import_headtype);
        Button nonstdText = view.findViewById(R.id.type_import_nonstd);
        Button insideDiameterText = view.findViewById(R.id.type_import_insidediameter);
        Button curvedHeightText = view.findViewById(R.id.type_import_curvedHeight);
        Button totalHeightText = view.findViewById(R.id.type_import_totalHeight);
        Button padHeightText = view.findViewById(R.id.type_import_padHeight);
        Button parameterDelete = view.findViewById(R.id.type_import_delete);
        Button paramterApply = view.findViewById(R.id.type_import_apply);
        String time = parameterItem.getTime();
        time = context.getString(R.string.type_import_time) + time;
        timeText.setText(time);
        typeText.setText(context.getString(R.string.headTypeTitle)+(parameterItem.isTypeRound()?"椭圆":"蝶形"));
        nonstdText.setText(context.getString(R.string.nonstandardType)+(parameterItem.isNonStandard()?"是":"否"));
        insideDiameterText.setText(context.getString(R.string.insideRadiusText)+parameterItem.getInsideDiameter());
        curvedHeightText.setText(context.getString(R.string.curvedHeightText)+parameterItem.getCurvedHeight());
        totalHeightText.setText(context.getString(R.string.totalHeightText)+parameterItem.getTotalHeight());
        padHeightText.setText(context.getString(R.string.padHeightText)+parameterItem.getPadHeight());
        parameterDelete.setTag(position+"del");
        paramterApply.setTag(position+"apply");
        parameterDelete.setOnClickListener(this.onClickListener);
        paramterApply.setOnClickListener(this.onClickListener);
        return view;
    }
}
