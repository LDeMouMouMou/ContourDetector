package com.example.contourdetector.ServicesPackage;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.example.contourdetector.SetterGetterPackage.BiasListViewItem;
import com.example.contourdetector.SetterGetterPackage.ParameterItem;
import com.example.contourdetector.SetterGetterPackage.ResultItem;

import java.util.ArrayList;
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
