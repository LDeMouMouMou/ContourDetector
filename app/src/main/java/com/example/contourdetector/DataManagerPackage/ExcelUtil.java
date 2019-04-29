package com.example.contourdetector.DataManagerPackage;

import com.example.contourdetector.SetterGetterPackage.DataItem;
import com.example.contourdetector.SetterGetterPackage.ParameterItem;
import com.example.contourdetector.SetterGetterPackage.ResultItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class ExcelUtil {

    // 创建字体、单元格空变量
    private static WritableCellFormat arial14format = null;
    private static WritableFont arial14font = null;
    private static WritableCellFormat arial12format = null;
    private static WritableFont arial12font = null;
    private final static String UTF8_ENCODING = "UTF-8";
    // 用于写入和读取表格的ResultItem、ParameterItem、DataItem
    private ResultItem resultItem;
    private ParameterItem parameterItem;
    private DataItem dataItem;


    // 在这里赋予字体、单元格格式变量以特定的值
    private void createFormat() {
        try {
            // Arial字体、14号大小、加粗，作为标题字体
            arial14font = new WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD);
            arial14font.setColour(Colour.LIGHT_BLUE);
            arial14format = new WritableCellFormat(arial14font);
            // 单元格格式居中、四周均有边界线、边界线样式为细、背景颜色为浅黄色
            arial14format.setAlignment(Alignment.CENTRE);
            arial14format.setBorder(Border.ALL, BorderLineStyle.THIN);
            arial14format.setBackground(Colour.VERY_LIGHT_YELLOW);
            // Arial字体、12号、不加粗，作为正文字体
            arial12font = new WritableFont(WritableFont.ARIAL, 12, WritableFont.NO_BOLD);
            arial12format = new WritableCellFormat(arial12font);
            arial12format.setAlignment(Alignment.CENTRE);
            arial12format.setBorder(Border.ALL, BorderLineStyle.THIN);
            arial12format.setBackground(Colour.GRAY_25);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 创建Excel文件，并设置好分页名称以及列名，均来于dataItem的列表
    private void initWriteExcelWorkBook(String fileName) {
        createFormat();
        WritableWorkbook workbook = null;
        List<String> sheetName = dataItem.getSheetName();
        List<List<String>> colName = dataItem.getColName();
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                try {
                    boolean b = file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 由文件名创建工作表文件
            workbook = Workbook.createWorkbook(file);
            for (int i = 0; i < sheetName.size(); i++) {
                // 由页名列表创建页
                WritableSheet sheet = workbook.createSheet(sheetName.get(i), i);
                // 由列名列表在当前页创建列
                for (int j = 0; j < colName.get(i).size(); j++) {
                    // sheet.addcell(new Label(列序号, 行序号, 单元格内容, 字体))
                    sheet.addCell(new Label(j, 0, colName.get(i).get(j), arial12format));
                }
                // 设置行高，开启写入模式
                sheet.setRowView(0, 340);
                workbook.write();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 操作完成，关闭工作表
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        writeDataToExcelWorkBook(fileName);
    }

    // 写入数据
    private void writeDataToExcelWorkBook(String fileName) {
        WritableWorkbook writableWorkbook = null;
        InputStream inputStream = null;
        // 设置编码格式，由文件流创建一个可写入的WritableWorkbook
        try {
            WorkbookSettings workbookSettings = new WorkbookSettings();
            workbookSettings.setEncoding(UTF8_ENCODING);
            inputStream = new FileInputStream(new File(fileName));
            Workbook workbook = Workbook.getWorkbook(inputStream);
            writableWorkbook = Workbook.createWorkbook(new File(fileName), workbook);
            for (int i = 0; i < dataItem.getSheetName().size(); i++) {
                WritableSheet sheet = writableWorkbook.getSheet(i);
                // 不同的页要分开处理，第一页是报告，第二页是参数，第三页是实际数据
                if (i == 0) {
                    for (int j = 0; j < dataItem.getReportRow().size(); j++) {
                        sheet.addCell(new Label(j, 1, dataItem.getReportRow().get(j), arial12format));
                    }
                } else if (i == 1) {
                    for (int j = 0; j < dataItem.getPamaterRow().size(); j++) {
                        sheet.addCell(new Label(j, 1, dataItem.getPamaterRow().get(j), arial12format));
                    }
                } else if (i == 2) {
                    for (int j = 0; j < dataItem.getD().size(); j++) {
                        sheet.addCell(new Label(0, j+1, String.valueOf(dataItem.getD().get(j)), arial12format));
                        sheet.addCell(new Label(1, j+1, String.valueOf(dataItem.getA().get(j)), arial12format));
                        sheet.addCell(new Label(2, j+1, String.valueOf(dataItem.getX().get(j)), arial12format));
                        sheet.addCell(new Label(3, j+1, String.valueOf(dataItem.getY().get(j)), arial12format));
                        sheet.addCell(new Label(4, j+1, String.valueOf(dataItem.getConcaveBias().get(j)), arial12format));
                        sheet.addCell(new Label(5, j+1, String.valueOf(dataItem.getConvexBias().get(j)), arial12format));
                        sheet.setRowView(j+1, 350);
                    }
                }
            }
            writableWorkbook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 操作完成，关闭工作表和输入流
            if (writableWorkbook != null) {
                try {
                    writableWorkbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ResultItem getResultItem() {
        return resultItem;
    }

    public void setResultItem(ResultItem resultItem) {
        this.resultItem = resultItem;
    }

    public ParameterItem getParameterItem() {
        return parameterItem;
    }

    public void setParameterItem(ParameterItem parameterItem) {
        this.parameterItem = parameterItem;
    }

    public DataItem getDataItem() {
        return dataItem;
    }

    public void setDataItem(DataItem dataItem) {
        this.dataItem = dataItem;
    }
}
