package com.ys.PressureTest.log;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ys.PressureTest.utils.FileUtils;
import com.ys.PressureTest.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;


public class SQLiteDao {
    private static final String TAG = "OrdersDao";
    private static SQLiteDao instance;

    // 列定义
    private final String[] ORDER_COLUMNS = new String[] {SQliteDBHelper.COLUMN_ID, SQliteDBHelper.COLUMN_NAME,SQliteDBHelper.COLUMN_CONTENT};

    private Context context;
    private SQliteDBHelper ordersDBHelper;

    public static SQLiteDao getInstance(Context context) {
        if (instance == null) {
            synchronized (SQLiteDao.class) {
                if (instance == null) {
                    instance = new SQLiteDao(context);
                }
            }
        }
        return instance;
    }

    public SQLiteDao(Context context) {
        this.context = context;
        ordersDBHelper = new SQliteDBHelper(context);
    }

    public void initTable(){
        SQLiteDatabase db = null;

        try {
            db = ordersDBHelper.getWritableDatabase();
            db.beginTransaction();
//            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (0, 'Zero','板卡的基本信息')");
//            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (1, 'One','下一次的开关机时间，暂时未开始测试')");
//            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (2, 'Two','重启日志，暂时未开始测试') ");
//            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (3, 'Three','已经测试过的定时开关机记录，暂时未开始测试') ");
//            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (4, 'Four','定时开关机测试次数，暂时未开始测试') ");
//            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (5, 'Five','同步网络时间记录，暂时未开始测试') ");
//            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (6, 'Six','同步网络时间的测试次数，暂时未开始测试') ");
//            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (7, 'Seven','高清视频和图片测试记录，暂时未开始测试') ");
//            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (8, 'Eight','以太网测试记录，重启模式，暂时未开始测试') ");
//            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (9, 'Nine','以太网测试记录，不重启模式，暂时未开始测试') ");
//            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (10, 'Ten','Wifi测试记录，暂时未开始测试') ");
//            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (11, 'Eleven','4G测试记录，暂时未开始测试') ");
            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (0, 'Zero','')");
            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (1, 'One','')");
            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (2, 'Two','') ");
            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (3, 'Three','') ");
            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (4, 'Four','') ");
            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (5, 'Five','') ");
            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (6, 'Six','') ");
            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (7, 'Seven','') ");
            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (8, 'Eight','') ");
            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (9, 'Nine','') ");
            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (10, 'Ten','') ");
            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (11, 'Eleven','') ");
            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (12, 'Twelve','') ");
            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (13, 'Thirteen','') ");
            db.execSQL("insert into " + SQliteDBHelper.TABLE_NAME + " (Id, ColumnName,ColumnContent) values (14, 'Fourteen','') ");
            db.setTransactionSuccessful();
        }catch (Exception e){
            Log.e(TAG, "", e);
        }finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }

    /**
     * 判断表中是否有数据
     */
    public boolean isDataExist(){
        int count = 0;

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = ordersDBHelper.getReadableDatabase();
            // select count(Id) from Orders
            cursor = db.query(SQliteDBHelper.TABLE_NAME, new String[]{"COUNT(Id)"}, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            if (count > 0) return true;
        }
        catch (Exception e) {
            Log.e(TAG, "", e);
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return false;
    }

    /**
     * 查询数据库中所有数据
     */
    public List<LogContent> getAllDate(){
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = ordersDBHelper.getReadableDatabase();
            // select * from Orders
            cursor = db.query(SQliteDBHelper.TABLE_NAME, ORDER_COLUMNS, null, null, null, null, null);

            if (cursor.getCount() > 0) {
                List<LogContent> orderList = new ArrayList<LogContent>(cursor.getCount());
                while (cursor.moveToNext()) {
                    orderList.add(parseOrder(cursor));
                }
                return orderList;
            }
        }
        catch (Exception e) {
            Log.e(TAG, "", e);
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return null;
    }

    public boolean isAssignDataExist(String columnName){
        int count = 0;

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = ordersDBHelper.getReadableDatabase();
            // select count(Id) from Orders
//            cursor = db.query(SQliteDBHelper.TABLE_NAME, new String[]{"COUNT(Id)"}, null, null, null, null, null);

            cursor = db.query(SQliteDBHelper.TABLE_NAME,
                    ORDER_COLUMNS,
                    "ColumnName = ?",
                    new String[] {columnName},
                    null, null, null);
            if (cursor.getCount() > 0) {
               return true;
            }

        }
        catch (Exception e) {
            Log.e(TAG, "", e);
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return false;
    }

    /**
     * 新增一条数据
     */
    public boolean insertDate(String content){
        SQLiteDatabase db = null;

        try {
            db = ordersDBHelper.getWritableDatabase();
            db.beginTransaction();

            // insert into Orders(Id, CustomName, OrderPrice, Country) values (7, "Jne", 700, "China");
            ContentValues contentValues = new ContentValues();
//            contentValues.put(SQliteDBHelper.COLUMN_ID, id);
            contentValues.put(SQliteDBHelper.COLUMN_CONTENT, content);
            db.insert(SQliteDBHelper.TABLE_NAME, null, contentValues);

            db.setTransactionSuccessful();
            return true;
        }catch (SQLiteConstraintException e){
            ToastUtils.showShortToast(context,"主键重复");
        }catch (Exception e){
            Log.e(TAG, "", e);
        }finally {
            if (db != null) {
                db.endTransaction();
                db.close();
                FileUtils.saveDaoToSD(context);
            }
        }
        return false;
    }

    public boolean insertDate(int id,String content){
        SQLiteDatabase db = null;

        try {
            db = ordersDBHelper.getWritableDatabase();
            db.beginTransaction();

            // insert into Orders(Id, CustomName, OrderPrice, Country) values (7, "Jne", 700, "China");
            ContentValues contentValues = new ContentValues();
            contentValues.put(SQliteDBHelper.COLUMN_ID, id);
            contentValues.put(SQliteDBHelper.COLUMN_NAME,getColumnName(id));
            contentValues.put(SQliteDBHelper.COLUMN_CONTENT, content);
            db.insert(SQliteDBHelper.TABLE_NAME, null, contentValues);

            db.setTransactionSuccessful();
            return true;
        }catch (SQLiteConstraintException e){
            ToastUtils.showShortToast(context,"主键重复");
        }catch (Exception e){
            Log.e(TAG, "", e);
        }finally {
            if (db != null) {
                db.endTransaction();
                db.close();
                FileUtils.saveDaoToSD(context);
            }
        }
        return false;
    }

    public boolean deleteOrder(int id) {
        SQLiteDatabase db = null;

        try {
            db = ordersDBHelper.getWritableDatabase();
            db.beginTransaction();

            // delete from Orders where Id = 7
            db.delete(SQliteDBHelper.TABLE_NAME, "Id = ?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "", e);
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
                FileUtils.saveDaoToSD(context);
            }
        }
        return false;
    }

    public boolean deleteAllOrder() {
        SQLiteDatabase db = null;

        try {
            db = ordersDBHelper.getWritableDatabase();
            db.beginTransaction();

            db.delete(SQliteDBHelper.TABLE_NAME, null, null);
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "", e);
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
                FileUtils.saveDaoToSD(context);
            }
        }
        return false;
    }

    /**
     * 修改一条数据
     */
    public boolean updateOrder(int id,String content) {
        if (!isAssignDataExist(getColumnName(id)))
            insertDate(id, content);
        else {
            SQLiteDatabase db = null;
        try {
            db = ordersDBHelper.getWritableDatabase();
            db.beginTransaction();

            // update Orders set OrderPrice = 800 where Id = 6
            ContentValues cv = new ContentValues();
            cv.put(SQliteDBHelper.COLUMN_CONTENT, content);
            db.update(SQliteDBHelper.TABLE_NAME,
                    cv,
                    "Id = ?",
                    new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();

            return true;
        } catch (Exception e) {
            Log.e(TAG, "", e);
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
                FileUtils.saveDaoToSD(context);
            }
        }
    }
        return false;
    }


    /**
     * 将查找到的数据转换成Order类
     */
    private LogContent parseOrder(Cursor cursor){
        int id = (cursor.getInt(cursor.getColumnIndex(SQliteDBHelper.COLUMN_ID)));
        String name = (cursor.getString(cursor.getColumnIndex(SQliteDBHelper.COLUMN_NAME)));
        String content = (cursor.getString(cursor.getColumnIndex(SQliteDBHelper.COLUMN_CONTENT)));
        return new LogContent(id,name,content);
    }

    private String getColumnName(int id) {
        String name = "";
        switch (id) {
            case 0:
                name = "Zero";
                break;
            case 1:
                name = "One";
                break;
            case 2:
                name = "Two";
                break;
            case 3:
                name = "Three";
                break;
            case 4:
                name = "Four";
                break;
            case 5:
                name = "Five";
                break;
            case 6:
                name = "Six";
                break;
            case 7:
                name = "Seven";
                break;
            case 8:
                name = "Eight";
                break;
            case 9 :
                name = "Nine";
                 break;
            case 10:
                name = "Ten";
                break;
            case 11:
                name = "Eleven";
                break;
            case 12:
                name = "Twelve";
                break;
                default:
                    break;
        }
        return name;
    }
}
