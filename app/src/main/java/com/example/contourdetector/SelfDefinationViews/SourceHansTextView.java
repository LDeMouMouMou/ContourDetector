package com.example.contourdetector.SelfDefinationViews;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class SourceHansTextView extends AppCompatTextView {

    public SourceHansTextView(Context context) {
        this(context, null);
    }

    public SourceHansTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SourceHansTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        Typeface typeface = null;
        try {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SourceHanSansCN-Normal.otf");
        } catch (Exception e) {
            typeface = Typeface.DEFAULT;
            e.printStackTrace();
        } finally {
            setTypeface(typeface);
        }
    }

}
