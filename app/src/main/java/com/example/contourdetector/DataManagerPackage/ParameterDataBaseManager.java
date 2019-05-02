package com.example.contourdetector.DataManagerPackage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.contourdetector.SetterGetterPackage.ParameterItem;

import java.util.ArrayList;
import java.util.List;

public class ParameterDataBaseManager {

    private SQLiteDatabase db;

    // 初始化数据库
    public void initDataBase(Context context) {
        // 获取到数据库的路径，并打开/创建它（如果不存在）
        String dataBasePath = context.getDatabasePath("parameter.db").getPath();
        db = SQLiteDatabase.openOrCreateDatabase(dataBasePath, null);
        // 如果没有对应的表格就创建它
        db.execSQL("CREATE TABLE IF NOT EXISTS parameter" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, time TEXT, type TEXT, " +
                "nonstd TEXT, concave TEXT, convex TEXT, diameter TEXT, curvedHeight TEXT," +
                "totalHeight TEXT, padHeight TEXT, ellipse TEXT, deleted TEXT)");
    }

    // 在这里添加parameterItem的各项数据到数据库的表格中，形式和输出的Excel一致
    // 为了保证一致性，所有的表格内容都是TEXT
    public void addOneParameterRecordLine(ParameterItem parameterItem) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("time", parameterItem.getTime());
        contentValues.put("type", parameterItem.isTypeRound()?"椭圆":"蝶形");
        contentValues.put("nonstd", parameterItem.isNonStandard()?"是":"否");
        contentValues.put("concave", String.valueOf(parameterItem.getConcaveBias()));
        contentValues.put("convex", String.valueOf(parameterItem.getConvexBias()));
        contentValues.put("diameter", String.valueOf(parameterItem.getInsideDiameter()));
        contentValues.put("curvedHeight", String.valueOf(parameterItem.getCurvedHeight()));
        contentValues.put("totalHeight", String.valueOf(parameterItem.getTotalHeight()));
        contentValues.put("padHeight", String.valueOf(parameterItem.getPadHeight()));
        contentValues.put("ellipse", parameterItem.isEllipseDetection()?"是":"否");
        contentValues.put("deleted", parameterItem.isDeleted()?"是":"否");
        db.insert("parameter", null, contentValues);
    }

    // 从现有的数据库表格中提取出所有的参数文件，并保存为列表返回给parameteServer
    public List<ParameterItem> getParameterItemList() {
        List<ParameterItem> parameterItemList = new ArrayList<>();
        // 游标
        Cursor c = db.query("parameter", null, null, null, null, null, null);
        for (c.moveToLast(); !c.isBeforeFirst(); c.moveToPrevious()) {
            if (!c.getString(11).equals("是")) {
                String time = c.getString(1);
                boolean type = c.getString(2).equals("椭圆");
                boolean nonstd = c.getString(3).equals("是");
                float concave = Float.valueOf(c.getString(4));
                float convex = Float.valueOf(c.getString(5));
                float diameter = Float.valueOf(c.getString(6));
                float curvedHeight = Float.valueOf(c.getString(7));
                float totalHeight = Float.valueOf(c.getString(8));
                float padHeight = Float.valueOf(c.getString(9));
                boolean ellipse = c.getString(10).equals("是");
                ParameterItem parameterItem = new ParameterItem(time, concave, convex, diameter, curvedHeight, totalHeight,
                        padHeight, nonstd, type, ellipse, false);
                // 添加用来标记实际位置的Id
                parameterItem.setId(Integer.valueOf(c.getString(0)));
                parameterItemList.add(parameterItem);
            }
        }
        c.close();
        return parameterItemList;
    }

    // 删除某一行的内容
    // 但是，由于删除之后自增长的id并不会自己更新，相当于留下了一个空行，这样会导致有的时候并不会真正地删除
    // 因此我们并不需要真正地删除它，而是将deleted标志位置为true，这样读取的时候就不会读进来
    public void deleteParameterItem(int position) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("deleted", "是");
        db.update("parameter", contentValues, "_id=?", new String[]{String.valueOf(position)});
    }

    public void closeDataBase() {
        db.close();
    }

}
