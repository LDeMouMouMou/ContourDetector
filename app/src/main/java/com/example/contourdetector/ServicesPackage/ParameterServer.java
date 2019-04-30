package com.example.contourdetector.ServicesPackage;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.text.format.DateFormat;

import com.example.contourdetector.DataManagerPackage.ExcelUtil;
import com.example.contourdetector.SetterGetterPackage.BiasListViewItem;
import com.example.contourdetector.SetterGetterPackage.DataItem;
import com.example.contourdetector.SetterGetterPackage.ParameterItem;
import com.example.contourdetector.SetterGetterPackage.ResultItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class ParameterServer extends Service {

    public final IBinder binder = new ParameterBinder();
    private ParameterItem parameterItem;
    // 四个测量结果的列表，对应距离、X值、Y值、角度值，之后会在后台执行计算，输出结果
    private List<Float> listD = new ArrayList<>();
    private List<Float> listX = new ArrayList<>();
    private List<Float> listY = new ArrayList<>();
    private List<Float> listA = new ArrayList<>();
    // 内凹、外凸偏差的列表，用以计算最大偏差和最小偏差
    private List<Float> ConcaveBias = new ArrayList<>();
    private List<Float> ConvexBias = new ArrayList<>();
    // 偏差的X、Y列表，用来画图
    private List<Float> listBiasX = new ArrayList<>();
    private List<Float> listBiasY = new ArrayList<>();
    // 存储结果的Item
    private ResultItem resultItem;
    private DataItem dataItem;
    private ExcelUtil excelUtil;

    public class ParameterBinder extends Binder {
        public ParameterServer getService() {
            return ParameterServer.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        // 构建参数初始值：默认标准封头、默认为椭圆形、默认不椭圆检测、默认不测试参数、默认内凹/外凸偏差
        // 其他均为空，这也是判断某个参数是否为空的依据
        // 事实上，可以为空的只有四个参数值（内凹/外凸为固定值，不可随意修改），将其设为-1即可
        parameterItem = new ParameterItem(10, 20, -1, -1,
                -1, -1, false, true, false, false);
        resultItem = new ResultItem();
        resultItem.setMaxDepth(-1);
        dataItem = new DataItem();
        excelUtil = new ExcelUtil();
        super.onCreate();
    }

    public boolean getParamterInspectionResult() {
        return true;
    }

    // 由于四个测量数据均同时产生，因为一步写入即可
    // 这时可以得到最深点的位置（Y值最大），将其作为原点，即将所有的X、Y作平移变换
    // 注意应该用原始数据作平移变换，而不是处理过的数据反复变换，会造成异常，因为是针对原始数据而言改变了原点
    // 这样就可以计算得到偏差数据列表（之后画图需要），最大内凹/外凸偏差及所在的坐标，最大深度所在的坐标等
    public void setAllDataList(List<Float> d, List<Float> x, List<Float> y, List<Float> a) {
        listD = d;
        listX = x;
        listY = y;
        listA = a;
        // 受hellochart限制，Y轴方向只能朝上，所以得到的图像和实际的（向下凹）刚好对称
        // 因此最大的Y值就是深度，先找到它
        float temp = Collections.max(listY);
        // 数据是动态添加进来的，所以只有当最大深度变化时，才需要更改原点坐标，此时全部坐标都需要变换
        // 如果相等，则只需要对新的坐标点按照上一次的原点坐标进行变换
        resultItem.setMaxDepth(temp);
        resultItem.setMaxDepthX(listX.get(listY.indexOf(temp)));
        resultItem.setMaxDepthY(temp);
        // 遍历列表，根据新的原点坐标修改所有数据的坐标
        // (listX, listY) -> (listX', listY'): listX'=listX-maxX listY'=-listY+maxY
        for (int i = 0; i < listX.size(); i++) {
            listX.set(i, listX.get(i)-resultItem.getMaxDepthX());
            listY.set(i, -(listY.get(i))+resultItem.getMaxDepthY());
        }
        applyResultAlgorithm();
    }

    // 封头的形状是半椭圆加上直线的组合，因此在直线段的标准值是insideRadius
    // 直线段的范围由totalHeight和curvedHeight确定，注意此时坐标已经翻转
    // 在椭圆段的标准值比较复杂，需要用几何坐标解出
    // 然后计算得到偏差值，求出最大值，比较得到形状是否合格，之后可以计算椭圆度
    public void applyResultAlgorithm() {
        // 这里的ConvexBias/ConvexBias的元素都是add进去的，所以在计算时要重置
        // 不然就越开越多了
        ConcaveBias = new ArrayList<>();
        ConvexBias = new ArrayList<>();
        // 坐标修改完毕后，遍历整个距离列表D，取得内凹/外凸偏差的列表及最大值
        for (int i = 0; i < listD.size(); i++) {
            float standardInLine = parameterItem.getInsideDiameter();
            float standardInEllipse = (float) (Math.sin(listA.get(i)*Math.PI/180.0)*
                    (parameterItem.getTotalHeight()-parameterItem.getCurvedHeight())+
                    Math.sqrt(parameterItem.getInsideDiameter()*parameterItem.getInsideDiameter()-
                            Math.pow((parameterItem.getTotalHeight()-parameterItem.getCurvedHeight()), 2)*Math.pow(
                                    Math.cos(listA.get(i)*Math.PI/180.0), 2)));
            if (listY.get(i) <= parameterItem.getTotalHeight() && listY.get(i) >= parameterItem.getCurvedHeight()) {
                // 大于0则为外凸偏差，反之则为内凹偏差
                // 虽然每个点只能对应一种偏差，但是另一种偏差还是要加上0，保证数据形式一致性
                if (listD.get(i) - standardInLine > 0) {
                    ConvexBias.add(listD.get(i) - standardInLine);
                    ConcaveBias.add((float) 0);
                }
                else {
                    ConcaveBias.add(standardInLine - listD.get(i));
                    ConvexBias.add((float) 0);
                }
            }
            else {
                if (listD.get(i) - parameterItem.getInsideDiameter() > 0) {
                    ConvexBias.add(listD.get(i) - standardInEllipse);
                    ConcaveBias.add((float) 0);
                }
                else {
                    ConcaveBias.add(standardInEllipse - listD.get(i));
                    ConvexBias.add((float) 0);
                }
            }
        }
        // 这里填入最大外凸/内凹偏差以及对应的坐标
        resultItem.setMaxConcaveBias(Collections.max(ConcaveBias));
        resultItem.setMaxConcaveCorX(listX.get(ConcaveBias.indexOf(resultItem.getMaxConcaveBias())));
        resultItem.setMaxConcaveCorY(listY.get(ConcaveBias.indexOf(resultItem.getMaxConcaveBias())));
        resultItem.setMaxConvexBias(Collections.max(ConvexBias));
        resultItem.setMaxConvexCorX(listX.get(ConvexBias.indexOf(resultItem.getMaxConvexBias())));
        resultItem.setMaxConvexCorY(listY.get(ConvexBias.indexOf(resultItem.getMaxConvexBias())));
        // 判断是否合格，即判断最大偏差是否在设定的范围之内
        if (Collections.max(ConcaveBias) <= parameterItem.getConcaveBias() &&
            Collections.max(ConvexBias) <= parameterItem.getConvexBias()) {
            resultItem.setQualified(true);
        }
        else {
            resultItem.setQualified(false);
        }
        // 计算椭圆度（如果勾选）
        if (parameterItem.isEllipseDetection()) {
            if (parameterItem.getInsideDiameter()/2==parameterItem.getCurvedHeight()) {
                resultItem.setEllipticity(0);
            }
            else {
                resultItem.setEllipticity((parameterItem.getInsideDiameter()/2-parameterItem.getCurvedHeight())/
                        (parameterItem.getInsideDiameter()/2));
            }
        }
    }

    public List<BiasListViewItem> getBiasListViewItemByCondition(float minConcave, float minConvex) {
        List<BiasListViewItem> biasListViewItemList = new ArrayList<>();
        // 抬头，adpter中会将第一个作为标题，所以不能添加任何有效数字
        biasListViewItemList.add(new BiasListViewItem(0, 0, 0));
        for (int i = 0; i < ConcaveBias.size(); i++) {
            if (ConcaveBias.get(i) != 0) {
                if (ConcaveBias.get(i) >= minConcave) {
                    float bias = ConcaveBias.get(i);
                    float x = listX.get(i);
                    float y = listY.get(i);
                    biasListViewItemList.add(new BiasListViewItem(x, y, -bias));
                }
            }
        }
        for (int i = 0; i < ConvexBias.size(); i++) {
            if (ConvexBias.get(i) != 0) {
                if (ConvexBias.get(i) >= minConvex) {
                    float bias = ConvexBias.get(i);
                    float x = listX.get(i);
                    float y = listY.get(i);
                    biasListViewItemList.add(new BiasListViewItem(x, y, bias));
                }
            }
        }
        return biasListViewItemList;
    }

    // 准备数据，创建Excel工作表文件
    public boolean createExcelSavingFile() {
        // 写入所有的D、A、X、Y、内凹、外凸偏差的值
        dataItem.setD(listD);
        dataItem.setA(listA);
        dataItem.setX(listX);
        dataItem.setY(listY);
        dataItem.setConcaveBias(ConcaveBias);
        dataItem.setConvexBias(ConvexBias);
        // 给定文件名，文件名以时间命名，避免重复
        Calendar calendar = Calendar.getInstance();
        dataItem.setFileName(DateFormat.format("yyyy-MM-dd_kk:mm:ss", calendar.getTime()).toString()+".xls");
        // 给定存储路径，存储在Download目录下
        String filePathTemp = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        filePathTemp = filePathTemp + "/" + dataItem.getFileName();
        dataItem.setFilePath(filePathTemp);
        // 给定Excel文件的页名、列名
        dataItem.setSheetName(new String[]{"报告", "参数", "数据"});
        dataItem.setColName(new String[][]{{"类型","椭圆度","直径","深度","最大内凹偏差","最大内凹偏差位置","最大外凸偏差"
                ,"最大外凸偏差位置","形状(是否合格)"},
                {"封头类型","非标准封头","内凹偏差","外凸偏差","封头内径","曲面高度","封头总高","垫块高度","椭圆度检测"},
                {"距离","角度","坐标X","坐标Y","内凹偏差","外凸偏差"}});
        // 给定Excel文件数据，详细数据可以循环提取，但是前两个比较麻烦
        List<String> reportRowList = new ArrayList<>();
        reportRowList.add(parameterItem.isTypeRound()?"椭圆":"蝶形");
        reportRowList.add(String.valueOf(resultItem.getEllipticity()));
        reportRowList.add(String.valueOf(parameterItem.getInsideDiameter()));
        reportRowList.add(String.valueOf(resultItem.getMaxDepth()));
        reportRowList.add(String.valueOf(resultItem.getMaxConcaveBias()));
        reportRowList.add("X="+resultItem.getMaxConcaveCorX()+" Y="+resultItem.getMaxConcaveCorY());
        reportRowList.add(String.valueOf(resultItem.getMaxConvexBias()));
        reportRowList.add("X="+resultItem.getMaxConvexCorX()+" Y="+resultItem.getMaxConvexCorY());
        reportRowList.add(resultItem.isQualified()?"合格":"不合格");
        dataItem.setReportRow(reportRowList);
        List<String> parameterRowList = new ArrayList<>();
        parameterRowList.add(parameterItem.isTypeRound()?"椭圆":"蝶形");
        parameterRowList.add(parameterItem.isNonStandard()?"是":"否");
        parameterRowList.add(String.valueOf(parameterItem.getConcaveBias()));
        parameterRowList.add(String.valueOf(parameterItem.getConvexBias()));
        parameterRowList.add(String.valueOf(parameterItem.getInsideDiameter()));
        parameterRowList.add(String.valueOf(parameterItem.getCurvedHeight()));
        parameterRowList.add(String.valueOf(parameterItem.getTotalHeight()));
        parameterRowList.add(String.valueOf(parameterItem.getPadHeight()));
        parameterRowList.add(parameterItem.isEllipseDetection()?"是":"否");
        dataItem.setParameterRow(parameterRowList);
        // 将数据赋予Excel处理，参数均在单独的Item内，并在server中处理，在util中应用
        excelUtil.setDataItem(dataItem);
        excelUtil.initWriteExcelWorkBook();
        return true;
    }

    // 校验目标Excel是否符合标准，parameterServer作为一个媒介，传递结果
    public boolean isExcelFileValid(String filePath) {
        return excelUtil.isExcelFileValid(filePath);
    }

    // 调用ExcelUtil中的方法读取数据，之后回传dataItem，分离、重新计算得到数据
    public void readExcelFromExistedFile(String filePath) {
        dataItem.setFilePath(filePath);
        excelUtil.setDataItem(dataItem);
        excelUtil.readDataFromExistedExcel();
        dataItem = excelUtil.getDataItem();
        // 从DataItem中分离出parameterItem
        parameterItem.setTypeRound(dataItem.getParameterRow().get(0).equals("椭圆"));
        parameterItem.setNonStandard(dataItem.getParameterRow().get(1).equals("是"));
        parameterItem.setConcaveBias(Float.valueOf(dataItem.getParameterRow().get(2)));
        parameterItem.setConvexBias(Float.valueOf(dataItem.getParameterRow().get(3)));
        parameterItem.setInsideDiameter(Float.valueOf(dataItem.getParameterRow().get(4)));
        parameterItem.setCurvedHeight(Float.valueOf(dataItem.getParameterRow().get(5)));
        parameterItem.setTotalHeight(Float.valueOf(dataItem.getParameterRow().get(6)));
        parameterItem.setPadHeight(Float.valueOf(dataItem.getParameterRow().get(7)));
        parameterItem.setEllipseDetection(dataItem.getParameterRow().get(8).equals("是"));
        // 得到原始数据值（已经经过坐标变换），不能走setAllDataList
        listD = dataItem.getD();
        listA = dataItem.getA();
        listX = dataItem.getX();
        listY = dataItem.getY();
        // 调用结果计算方法得到resultItem
        applyResultAlgorithm();
    }

    // 删除指定路径的文件
    public boolean delteExistedExcelFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        } else {
            return false;
        }
    }

    // 准备数据，创建TXT文件
    // TXT文件的创建比较简单，不需要再开一个class
    // 由于不能从TXT恢复数据，所以也不需要单独的Item来解析
    public boolean createTxtSavingFile() {
        Calendar calendar = Calendar.getInstance();
        String fileName = DateFormat.format("yyyy-MM-dd_kk:mm:ss", calendar.getTime()).toString()+".txt";
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()
                + "/" + fileName;
        dataItem.setFileName(fileName);
        dataItem.setFilePath(filePath);
        String fileContent = "文件创建时间："+fileName.substring(0, fileName.indexOf("."))+"\n"
                +"这是本次测量的简要报告，具体数据表现请参照Excel文档!"+"\n"
                +"测量目标类型："+(parameterItem.isTypeRound()?"椭圆":"蝶形")+"\n"
                +"测量目标直径："+parameterItem.getInsideDiameter()+"\n"
                +"测量目标深度："+resultItem.getMaxDepth()+"\n"
                +"椭圆度："+resultItem.getEllipticity()+"\n"
                +"测得最大内凹偏差："+resultItem.getMaxConcaveBias()
                +" 它所在位置："+"X="+resultItem.getMaxConcaveCorX()+" Y="+resultItem.getMaxConcaveCorY()+"\n"
                +"测得最大外凸偏差"+resultItem.getMaxConvexBias()
                +" 它所在位置："+"X="+resultItem.getMaxConvexCorX()+" Y="+resultItem.getMaxConvexCorY()+"\n"
                +"测量样品形状："+(resultItem.isQualified()?"合格":"不合格")+"\n";
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                boolean b = file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(fileContent.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    // 将参数写入构造体
    public void setParameterItem(ParameterItem parameterItem) {
        this.parameterItem = parameterItem;
    }

    // 将更新后的参数从后台返回到前台
    public ParameterItem getParameterItem() {
        return this.parameterItem;
    }

    public ResultItem getResultItem() {
        return resultItem;
    }

    public DataItem getDataItem() {
        return dataItem;
    }

    // 提取其中某一类型的测量数据，用作进一步的画图、计算等操作
    public List<Float> getDataListOfTypeAfterProcessed(String type) {
        switch (type) {
            case "listD":
                return listD;
            case "listX":
                return listX;
            case "listY":
                return listY;
            case "listA":
                return listA;
            case "listBiasX":
                return listBiasX;
            case "listBiasY":
                return listBiasY;
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
