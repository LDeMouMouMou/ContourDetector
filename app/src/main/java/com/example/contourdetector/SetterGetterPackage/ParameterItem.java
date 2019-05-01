package com.example.contourdetector.SetterGetterPackage;

public class ParameterItem {

    private String time;
    private float concaveBias;
    private float convexBias;
    private float insideDiameter;
    private float curvedHeight;
    private float totalHeight;
    private float padHeight;
    private boolean nonStandard;
    private boolean typeRound;
    private boolean ellipseDetection;
    private boolean deleted;

    public ParameterItem(String time, float concaveBias, float convexBias, float insideDiameter, float curvedHeight,
                         float totalHeight, float padHeight, boolean nonStandard, boolean typeRound,
                         boolean ellipseDetection, boolean deleted) {
        this.time = time;
        this.concaveBias = concaveBias;
        this.convexBias = convexBias;
        this.insideDiameter = insideDiameter;
        this.curvedHeight = curvedHeight;
        this.totalHeight = totalHeight;
        this.padHeight = padHeight;
        this.nonStandard = nonStandard;
        this.typeRound = typeRound;
        this.ellipseDetection = ellipseDetection;
        this.deleted = deleted;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public float getConcaveBias() {
        return concaveBias;
    }

    public void setConcaveBias(float concaveBias) {
        this.concaveBias = concaveBias;
    }

    public float getConvexBias() {
        return convexBias;
    }

    public void setConvexBias(float convexBias) {
        this.convexBias = convexBias;
    }

    public float getInsideDiameter() {
        return insideDiameter;
    }

    public void setInsideDiameter(float insideDiameter) {
        this.insideDiameter = insideDiameter;
    }

    public float getCurvedHeight() {
        return curvedHeight;
    }

    public void setCurvedHeight(float curvedHeight) {
        this.curvedHeight = curvedHeight;
    }

    public float getTotalHeight() {
        return totalHeight;
    }

    public void setTotalHeight(float totalHeight) {
        this.totalHeight = totalHeight;
    }

    public float getPadHeight() {
        return padHeight;
    }

    public void setPadHeight(float padHeight) {
        this.padHeight = padHeight;
    }

    public boolean isNonStandard() {
        return nonStandard;
    }

    public void setNonStandard(boolean nonStandard) {
        this.nonStandard = nonStandard;
    }

    public boolean isTypeRound() {
        return typeRound;
    }

    public void setTypeRound(boolean typeRound) {
        this.typeRound = typeRound;
    }

    public boolean isEllipseDetection() {
        return ellipseDetection;
    }

    public void setEllipseDetection(boolean ellipseDetection) {
        this.ellipseDetection = ellipseDetection;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
