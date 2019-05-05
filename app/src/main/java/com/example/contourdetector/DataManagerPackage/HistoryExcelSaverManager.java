package com.example.contourdetector.DataManagerPackage;

import android.content.Context;
import android.os.Environment;
import android.text.method.NumberKeyListener;

import com.example.contourdetector.SetterGetterPackage.HistoryItem;
import com.example.contourdetector.SetterGetterPackage.ParameterItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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


// 想来想去还是用Excel来储存记录吧，而且似乎读取的速度也不慢
public class HistoryExcelSaverManager {

    // 创建字体、单元格空变量
    private static WritableCellFormat arial14format = null;
    private static WritableFont arial14font = null;
    private static WritableCellFormat arial12format = null;
    private static WritableFont arial12font = null;
    private final static String UTF8_ENCODING = "UTF-8";
    // 用于写入和读取表格的HistoryItem
    private Context context;

    public HistoryExcelSaverManager(Context context) {
        this.context = context;
    }

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

    // 创建Excel文件，每次有新的测量数据就保存一次新的文件，需要时全部读取即可
    // 保存路径在/data/data/<Package Name>/file/history_time?.xls
    // /data/data/com.example.contourdetector/files/history_2019-05-04_09:57:30.xls
    private String initWriteExcelWorkBook(String time) {
        createFormat();
        WritableWorkbook writableWorkbook = null;
        String fileName = "history_"+time+".xls";
        String filePath = context.getFilesDir().getPath() + "/" + fileName;
        String[] colName = new String[]{"D", "A", "X", "Y"};
        // 创建文件，事实上file一定不存在
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                try {
                    boolean b = file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 由文件名创建工作表文件，得到可写的工作表
            writableWorkbook = Workbook.createWorkbook(file);
            // 创建页，页名就是0吧，就一页
            WritableSheet sheet = writableWorkbook.createSheet("0", 0);
            // 由列名列表在当前页创建列
            for (int j = 0; j < colName.length; j++) {
                // sheet.addcell(new Label(列序号, 行序号, 单元格内容, 字体))
                // 写在第三行是因为前两行要写别的东西
                sheet.addCell(new Label(j, 2, colName[j], arial12format));
            }
            // 行高，不加这个似乎会格式错误
            sheet.setRowView(0, 340);
            // write()方法只能在全部完成后调用一次，不然只能保留第一次的结果
            writableWorkbook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 操作完成，关闭工作表
            if (writableWorkbook != null) {
                try {
                    writableWorkbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // 返回当前文件的完整路径
        return filePath;
    }

    // 在刚刚创建的文件中写入内容
    public void writeHistoryItemToExcelWorkBook(HistoryItem historyItem) {
        createFormat();
        // 得到完整路径
        String filePath = initWriteExcelWorkBook(historyItem.getTime());
        WritableWorkbook writableWorkbook = null;
        InputStream inputStream = null;
        try {
            WorkbookSettings workbookSettings = new WorkbookSettings();
            // 设置编码格式
            workbookSettings.setEncoding(UTF8_ENCODING);
            inputStream = new FileInputStream(new File(filePath));
            Workbook workbook = Workbook.getWorkbook(inputStream);
            // 得到可写的工作表文件
            writableWorkbook = Workbook.createWorkbook(new File(filePath), workbook);
            // 获取DAXY的数据
            List<Float> D = historyItem.getD();
            List<Float> A = historyItem.getA();
            List<Float> X = historyItem.getX();
            List<Float> Y = historyItem.getY();
            // 获取参数
            ParameterItem parameterItem = historyItem.getParameterItem();
            // 一定是第1页
            WritableSheet sheet = writableWorkbook.getSheet(0);
            // 先写入表面信息，按照时间-合格-（标准+形状）-最大内凹-最大外凸的顺序
            sheet.addCell(new Label(0, 0, historyItem.getTime(), arial12format));
            sheet.addCell(new Label(1, 0, historyItem.getQualified(), arial12format));
            sheet.addCell(new Label(2, 0, historyItem.getType(), arial12format));
            sheet.addCell(new Label(3, 0, String.valueOf(historyItem.getMaxConcave()), arial12format));
            sheet.addCell(new Label(4, 0, String.valueOf(historyItem.getMaxConvex()), arial12format));
            sheet.setRowView(0,350);
            // 然后在第二行写入参数信息，按照：形状、非标、内凹、外凸、内径、曲面高、总高、垫块高、椭圆度的顺序
            sheet.addCell(new Label(0, 1, parameterItem.isTypeRound()?"椭圆":"蝶形", arial12format));
            sheet.addCell(new Label(1, 1, parameterItem.isNonStandard()?"是":"否", arial12format));
            sheet.addCell(new Label(2, 1, String.valueOf(parameterItem.getConcaveBias()), arial12format));
            sheet.addCell(new Label(3, 1, String.valueOf(parameterItem.getConvexBias()), arial12format));
            sheet.addCell(new Label(4, 1, String.valueOf(parameterItem.getInsideDiameter()), arial12format));
            sheet.addCell(new Label(5, 1, String.valueOf(parameterItem.getCurvedHeight()), arial12format));
            sheet.addCell(new Label(6, 1, String.valueOf(parameterItem.getTotalHeight()), arial12format));
            sheet.addCell(new Label(7, 1, String.valueOf(parameterItem.getPadHeight()), arial12format));
            sheet.addCell(new Label(8, 1, parameterItem.isEllipseDetection()?"是":"否", arial12format));
            sheet.setRowView(1, 350);
            // 第四列开始再写正常的数据，第三列是列名，后边是原始数据
            for (int i = 0; i < D.size(); i++) {
                // 第一列填D，第二列填A，以此类推，i从第0开始，第三列已经写入了列名，要跳过，从第四(row=3)列开始
                sheet.addCell(new Label(0, i+3, String.valueOf(D.get(i)), arial12format));
                sheet.addCell(new Label(1, i+3, String.valueOf(A.get(i)), arial12format));
                sheet.addCell(new Label(2, i+3, String.valueOf(X.get(i)), arial12format));
                sheet.addCell(new Label(3, i+3, String.valueOf(Y.get(i)), arial12format));
                sheet.setRowView(i+3, 350);
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

    // 读取目标路径下的所有Excel文件，获取historyItemList
    public List<HistoryItem> getSavedHistoryItemList() {
        String dirPath = context.getFilesDir().getPath();
        List<String> filePaths = new ArrayList<>();
        File file = new File(dirPath);
        File[] files = file.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                // 排除文件夹
                if (!files[i].isDirectory()) {
                    // 排除不含.xls的非Excel文件
                    if (files[i].getName().contains(".xls")) {
                        filePaths.add(files[i].getAbsolutePath());
                    }
                }
            }
            // 在文件数量不为零的情况下，分别读取获得historyItem
            if (filePaths.size() != 0) {
                List<HistoryItem> historyItemList = new ArrayList<>();
                for (int i = 0; i < filePaths.size(); i++) {
                    Workbook workbook = null;
                    InputStream inputStream = null;
                    try {
                        inputStream = new FileInputStream(filePaths.get(i));
                        workbook = Workbook.getWorkbook(inputStream);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // 得到第1页，所有数据都在第一页上
                    Sheet sheet = workbook.getSheet(0);
                    // 初始化HistoryItem和parameterItem，准备读取
                    HistoryItem historyItem = new HistoryItem();
                    ParameterItem parameterItem = new ParameterItem(null, 0, 0, 0, 0,
                            0, 0, false, true, true, false);
                    List<Float> D = new ArrayList<>();
                    List<Float> A = new ArrayList<>();
                    List<Float> X = new ArrayList<>();
                    List<Float> Y = new ArrayList<>();
                    // 按照一行一行的顺序遍历
                    for (int j = 0; j < sheet.getRows(); j++) {
                        // 第一行是表面数据
                        if (j == 0) {
                            historyItem.setTime(sheet.getCell(0, j).getContents());
                            historyItem.setQualified(sheet.getCell(1, j).getContents());
                            historyItem.setType(sheet.getCell(2, j).getContents());
                            historyItem.setMaxConcave(Float.valueOf(sheet.getCell(3, j).getContents()));
                            historyItem.setMaxConvex(Float.valueOf(sheet.getCell(4, j).getContents()));
                        }
                        // 第二行是参数
                        else if (j == 1) {
                            parameterItem.setTypeRound(sheet.getCell(0, j).getContents().equals("椭圆"));
                            parameterItem.setNonStandard(sheet.getCell(1, j).getContents().equals("是"));
                            parameterItem.setConcaveBias(Float.valueOf(sheet.getCell(2, j).getContents()));
                            parameterItem.setConvexBias(Float.valueOf(sheet.getCell(3, j).getContents()));
                            parameterItem.setInsideDiameter(Float.valueOf(sheet.getCell(4, j).getContents()));
                            parameterItem.setCurvedHeight(Float.valueOf(sheet.getCell(5, j).getContents()));
                            parameterItem.setTotalHeight(Float.valueOf(sheet.getCell(6, j).getContents()));
                            parameterItem.setPadHeight(Float.valueOf(sheet.getCell(7, j).getContents()));
                            parameterItem.setEllipseDetection(sheet.getCell(8, j).getContents().equals("是"));
                        }
                        // 第四行开始是DAXY
                        else if (j >= 3) {
                            D.add(Float.valueOf(sheet.getCell(0, j).getContents()));
                            A.add(Float.valueOf(sheet.getCell(1, j).getContents()));
                            X.add(Float.valueOf(sheet.getCell(2, j).getContents()));
                            Y.add(Float.valueOf(sheet.getCell(3, j).getContents()));
                        }
                    }
                    // 遍历完成后将数据写入historyItem
                    historyItem.setParameterItem(parameterItem);
                    historyItem.setD(D);
                    historyItem.setA(A);
                    historyItem.setX(X);
                    historyItem.setY(Y);
                    // 加入HistoryItemList
                    historyItemList.add(historyItem);
                    // 当前表格操作完成，关闭工作表
                    if (workbook != null) {
                        try {
                            workbook.close();
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
                return historyItemList;
            } else {
                return null;
            }
        }
        else {
            return null;
        }
    }
}
