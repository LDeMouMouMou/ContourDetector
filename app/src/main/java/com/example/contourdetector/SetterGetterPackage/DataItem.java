package com.example.contourdetector.SetterGetterPackage;

import java.util.List;

public class DataItem {

    private String fileName;
    private String filePath;
    private String[] sheetName;
    private String[][] colName;
    private List<String> reportRow;
    private List<String> parameterRow;
    private List<Float> D;
    private List<Float> A;
    private List<Float> X;
    private List<Float> Y;
    private List<Float> ConcaveBias;
    private List<Float> ConvexBias;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String[] getSheetName() {
        return sheetName;
    }

    public void setSheetName(String[] sheetName) {
        this.sheetName = sheetName;
    }

    public String[][] getColName() {
        return colName;
    }

    public void setColName(String[][] colName) {
        this.colName = colName;
    }

    public List<String> getReportRow() {
        return reportRow;
    }

    public void setReportRow(List<String> reportRow) {
        this.reportRow = reportRow;
    }

    public List<String> getParameterRow() {
        return parameterRow;
    }

    public void setParameterRow(List<String> parameterRow) {
        this.parameterRow = parameterRow;
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

    public List<Float> getConcaveBias() {
        return ConcaveBias;
    }

    public void setConcaveBias(List<Float> concaveBias) {
        ConcaveBias = concaveBias;
    }

    public List<Float> getConvexBias() {
        return ConvexBias;
    }

    public void setConvexBias(List<Float> convexBias) {
        ConvexBias = convexBias;
    }
}
