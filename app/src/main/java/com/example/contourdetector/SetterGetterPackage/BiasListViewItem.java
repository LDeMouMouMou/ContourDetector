package com.example.contourdetector.SetterGetterPackage;

public class BiasListViewItem {

    private float x;
    private float y;
    private float bias;

    public BiasListViewItem(float x, float y, float bias) {
        this.x = x;
        this.y = y;
        this.bias = bias;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getBias() {
        return bias;
    }

    public void setBias(float bias) {
        this.bias = bias;
    }

}
