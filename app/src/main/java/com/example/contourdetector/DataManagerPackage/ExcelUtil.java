package com.example.contourdetector.DataManagerPackage;

import com.example.contourdetector.SetterGetterPackage.DataItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
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
    // 用于写入和读取表格的DataItem
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
            // Arial字体、12号、加粗，作为正文字体
            arial12font = new WritableFont(WritableFont.ARIAL, 12, WritableFont.BOLD);
            arial12format = new WritableCellFormat(arial12font);
            arial12format.setAlignment(Alignment.CENTRE);
            arial12format.setBorder(Border.ALL, BorderLineStyle.THIN);
            arial12format.setBackground(Colour.GRAY_25);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 创建Excel文件，并设置好分页名称以及列名，均来于dataItem的列表
    public void initWriteExcelWorkBook() {
        createFormat();
        WritableWorkbook workbook = null;
        String filePath = dataItem.getFilePath();
        String[] sheetName = dataItem.getSheetName();
        String[][] colName = dataItem.getColName();
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                try {
                    boolean b = file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 由文件名创建工作表文件
            workbook = Workbook.createWorkbook(file);
            for (int i = 0; i < sheetName.length; i++) {
                // 由页名列表创建页
                WritableSheet sheet = workbook.createSheet(sheetName[i], i);
                // 由列名列表在当前页创建列
                for (int j = 0; j < colName[i].length; j++) {
                    // sheet.addcell(new Label(列序号, 行序号, 单元格内容, 字体))
                    sheet.addCell(new Label(j, 0, colName[i][j], arial12format));
                }
                // 设置行高，开启写入模式
                sheet.setRowView(0, 340);
            }
            workbook.write();
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
        writeDataToExcelWorkBook(filePath);
    }

    // 写入数据
    private void writeDataToExcelWorkBook(String filePath) {
        WritableWorkbook writableWorkbook = null;
        InputStream inputStream = null;
        // 设置编码格式，由文件流创建一个可写入的WritableWorkbook
        try {
            WorkbookSettings workbookSettings = new WorkbookSettings();
            workbookSettings.setEncoding(UTF8_ENCODING);
            inputStream = new FileInputStream(new File(filePath));
            Workbook workbook = Workbook.getWorkbook(inputStream);
            writableWorkbook = Workbook.createWorkbook(new File(filePath), workbook);
            for (int i = 0; i < dataItem.getSheetName().length; i++) {
                WritableSheet sheet = writableWorkbook.getSheet(i);
                // 不同的页要分开处理，第一页是报告，第二页是参数，第三页是实际数据
                if (i == 0) {
                    for (int j = 0; j < dataItem.getReportRow().size(); j++) {
                        sheet.addCell(new Label(j, 1, dataItem.getReportRow().get(j), arial12format));
                    }
                    sheet.setRowView(1, 350);
                } else if (i == 1) {
                    for (int j = 0; j < dataItem.getParameterRow().size(); j++) {
                        sheet.addCell(new Label(j, 1, dataItem.getParameterRow().get(j), arial12format));
                    }
                    sheet.setRowView(1, 350);
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
                    sheet.setRowView(1, 350);
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

    // 读取指定路径的Excel表，检验其是否符合标准格式
    // filePath已经是完整的文件路径，可以直接读取
    public boolean isExcelFileValid(String filePath) {
        Workbook workbook = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            workbook = Workbook.getWorkbook(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 检测工作表是否为空
        if (workbook == null) {
            return false;
        }
        Sheet[] sheets = workbook.getSheets();
        String[] stdSheetName = new String[]{"报告", "参数", "数据"};
        // 检测是否为3页内容
        if (sheets.length != 3) {
            return false;
        }
        // 检查页名是否符合要求
        if (!Arrays.equals(workbook.getSheetNames(), stdSheetName)) {
            return false;
        }
        // 检查每一页中的标题行是否符合标准
        String[][] stdTitles = new String[][]{{"类型","椭圆度","直径","深度","最大内凹偏差","最大内凹偏差位置","最大外凸偏差"
                ,"最大外凸偏差位置","形状(是否合格)"},
                {"封头类型","非标准封头","内凹偏差","外凸偏差","封头内径","曲面高度","封头总高","垫块高度","椭圆度检测"},
                {"距离","角度","坐标X","坐标Y","内凹偏差","外凸偏差"}};
        for (int i = 0; i < sheets.length; i++) {
            List<String> titleRow = new ArrayList<>();
            for (int j = 0; j < sheets[i].getColumns(); j++) {
                Cell cell = sheets[i].getCell(j,0);
                titleRow.add(cell.getContents());
            }
            String[] titleRowString = new String[titleRow.size()];
            titleRow.toArray(titleRowString);
            if (!Arrays.equals(stdTitles[i], titleRowString)) {
                return false;
            }
        }
        // 读取完毕，关闭工作表和文件流
        workbook.close();
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    // 读取指定路径的Excel表，并将结果整合到dataItem中
    // 和写入一样，不实例化dataItem，只能从parameterServer中获取
    public void readDataFromExistedExcel() {
        Workbook workbook = null;
        InputStream inputStream = null;
        String filePath = dataItem.getFilePath();
        try {
            inputStream = new FileInputStream(filePath);
            workbook = Workbook.getWorkbook(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Sheet[] sheets = workbook.getSheets();
        // 获取页名
        dataItem.setSheetName(workbook.getSheetNames());
        // 分别读取每一页的内容，同理，也需要独立操作，每页的格式都不一样
        for (int i = 0; i < sheets.length; i++) {
            if (i == 0) {
                // 读取每一列的内容，存入list，对于前两页来说，只有第2行（row=1）有内容
                List<String> reportRowString = new ArrayList<>();
                for (int j = 0; j < sheets[i].getColumns(); j++) {
                    Cell cell = sheets[i].getCell(j, 1);
                    reportRowString.add(cell.getContents());
                }
                dataItem.setReportRow(reportRowString);
            } else if (i == 1) {
                List<String> parameterRowString = new ArrayList<>();
                for (int j = 0; j < sheets[i].getColumns(); j++) {
                    Cell cell = sheets[i].getCell(j, 1);
                    parameterRowString.add(cell.getContents());
                }
                dataItem.setParameterRow(parameterRowString);
            } else if (i == 2) {
                // 第三页的内容按照纵向（按行）读取到List中，一行为一个List
                for (int j = 0; j < sheets[i].getColumns(); j++) {
                    List<Float> currentColValue = new ArrayList<>();
                    for (int k = 1; k < sheets[i].getRows(); k++) {
                        Cell cell = sheets[i].getCell(j, k);
                        currentColValue.add(Float.valueOf(cell.getContents()));
                    }
                    // 将List写入dataItem，比较麻烦，毕竟顺序是自己定的
                    switch (j) {
                        case 0:
                            dataItem.setD(currentColValue);
                            break;
                        case 1:
                            dataItem.setA(currentColValue);
                            break;
                        case 2:
                            dataItem.setX(currentColValue);
                            break;
                        case 3:
                            dataItem.setY(currentColValue);
                            break;
                        case 4:
                            dataItem.setConcaveBias(currentColValue);
                            break;
                        case 5:
                            dataItem.setConvexBias(currentColValue);
                            break;
                    }
                }
            }
        }
        // 读取完毕，关闭工作表和文件流
        workbook.close();
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public DataItem getDataItem() {
        return dataItem;
    }

    public void setDataItem(DataItem dataItem) {
        this.dataItem = dataItem;
    }
}
