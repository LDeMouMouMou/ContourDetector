package com.example.contourdetector.SetterGetterPackage;

import java.util.List;

public class HistoryItem {

    // 这些是表面的，就是在history界面看到的item
    private String time;
    private String qualified;
    private String type;
    private float maxConcave;
    private float maxConvex;
    // 这些是内部的，根据这些还原数据
    private List<Float> D;
    private List<Float> A;
    private List<Float> X;
    private List<Float> Y;
    private ParameterItem parameterItem;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getQualified() {
        return qualified;
    }

    public void setQualified(String qualified) {
        this.qualified = qualified;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getMaxConcave() {
        return maxConcave;
    }

    public void setMaxConcave(float maxConcave) {
        this.maxConcave = maxConcave;
    }

    public float getMaxConvex() {
        return maxConvex;
    }

    public void setMaxConvex(float maxConvex) {
        this.maxConvex = maxConvex;
    }

    public List<Float> getD() {
        return D;
    }

    public void setD(List<Float> d) {
        D = d;
    }

    public List<Float> getA() {
        return A;
    }

    public void setA(List<Float> a) {
        A = a;
    }

    public List<Float> getX() {
        return X;
    }

    public void setX(List<Float> x) {
        X = x;
    }

    public List<Float> getY() {
        return Y;
    }

    public void setY(List<Float> y) {
        Y = y;
    }

    public ParameterItem getParameterItem() {
        return parameterItem;
    }

    public void setParameterItem(ParameterItem parameterItem) {
        this.parameterItem = parameterItem;
    }
}
