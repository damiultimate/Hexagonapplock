package com.hexagon.applock.activity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DBHelper extends SQLiteOpenHelper {
    public Context context;
    public boolean successful=false;
    public DBHelper(Context context) {

        super(context, "lockDetails", null, 1);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LockDetails.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS lockDetails");
        successful=false;
        onCreate(db);
    }
    public void insert(String appName,String password,String locked,String temp){
       try {
           SQLiteDatabase db = this.getWritableDatabase();
           ContentValues values = new ContentValues();
           values.put("appName", appName);
           values.put("password", password);
           values.put("locked", locked);
           values.put("temp", temp);
           long id = db.insert("lockDetails", null, values);
           db.close();
successful=true;
       }catch(Exception e){
successful=false;
       }
    }
    public LockDetails get(String packagename){
     LockDetails lockDetails=null;
     try{
       SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor=db.query("lockDetails",new String[]{"id","appName","password","locked","temp","time"},"appName=?",new String[]{packagename},null,null,null,null);
    if(cursor != null) {
        cursor.moveToFirst();
        lockDetails = new LockDetails(cursor.getInt(cursor.getColumnIndex("id")), cursor.getString(cursor.getColumnIndex("appName")), cursor.getString(cursor.getColumnIndex("password")), cursor.getString(cursor.getColumnIndex("locked")), cursor.getString(cursor.getColumnIndex("temp")), cursor.getString(cursor.getColumnIndex("time")));
        cursor.close();
    }
         db.close();

         successful=true;
    }catch(Exception e){
successful=false;
     }
    return lockDetails;
    }
    public void update(String appName,String password,String locked,String temp) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("appName", appName);
            values.put("password", password);
            values.put("locked", locked);
            values.put("temp", temp);
            db.update("lockDetails", values, "appName=?", new String[]{appName});
       successful=true;
            db.close();

        }catch(Exception e){
            successful=false;
        }

    }
    public void delete(String packagem){
       try {
           SQLiteDatabase db = this.getWritableDatabase();
           db.delete("lockDetails", "appName=?", new String[]{packagem});
           db.close();
           successful=true;
       }catch(Exception e){
           successful=false;
       }
    }
}