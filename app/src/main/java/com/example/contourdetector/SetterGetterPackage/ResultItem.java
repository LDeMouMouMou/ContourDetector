package com.example.contourdetector.SetterGetterPackage;

public class ResultItem {

    private float maxDepth;
    private float maxDepthX;
    private float maxDepthY;
    private float maxConcaveBias;
    private float maxConcaveCorX;
    private float maxConcaveCorY;
    private float maxConvexBias;
    private float maxConvexCorX;
    private float maxConvexCorY;
    private float ellipticity;
    private boolean isQualified;

    public float getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(float maxDepth) {
        this.maxDepth = maxDepth;
    }

    public float getMaxDepthX() {
        return maxDepthX;
    }

    public void setMaxDepthX(float maxDepthX) {
        this.maxDepthX = maxDepthX;
    }

    public float getMaxDepthY() {
        return maxDepthY;
    }

    public void setMaxDepthY(float maxDepthY) {
        this.maxDepthY = maxDepthY;
    }

    public float getMaxConcaveBias() {
        return maxConcaveBias;
    }

    public void setMaxConcaveBias(float maxConcaveBias) {
        this.maxConcaveBias = maxConcaveBias;
    }

    public float getMaxConcaveCorX() {
        return maxConcaveCorX;
    }

    public void setMaxConcaveCorX(float maxConcaveCorX) {
        this.maxConcaveCorX = maxConcaveCorX;
    }

    public float getMaxConcaveCorY() {
        return maxConcaveCorY;
    }

    public void setMaxConcaveCorY(float maxConcaveCorY) {
        this.maxConcaveCorY = maxConcaveCorY;
    }

    public float getMaxConvexBias() {
        return maxConvexBias;
    }

    public void setMaxConvexBias(float maxConvexBias) {
        this.maxConvexBias = maxConvexBias;
    }

    public float getMaxConvexCorX() {
        return maxConvexCorX;
    }

    public void setMaxConvexCorX(float maxConvexCorX) {
        this.maxConvexCorX = maxConvexCorX;
    }

    public float getMaxConvexCorY() {
        return maxConvexCorY;
    }

    public void setMaxConvexCorY(float maxConvexCorY) {
        this.maxConvexCorY = maxConvexCorY;
    }

    public float getEllipticity() {
        return ellipticity;
    }

    public void setEllipticity(float ellipticity) {
        this.ellipticity = ellipticity;
    }

    public boolean isQualified() {
        return isQualified;
    }

    public void setQualified(boolean qualified) {
        isQualified = qualified;
    }
}
