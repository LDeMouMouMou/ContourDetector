package com.example.contourdetector.SetterGetterPackage;

public class FileListViewItem {

    private String fileName;
    private int openStatue;

    public FileListViewItem(String fileName, int openStatue) {
        this.fileName = fileName;
        this.openStatue = openStatue;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getOpenStatue() {
        return openStatue;
    }

    public void setOpenStatue(int openStatue) {
        this.openStatue = openStatue;
    }
}
