package com.hexagon.applock.activity;

public class LockDetails {
    public int id;
    public String appName;
    public String password;
    public String locked;
    public String temp;
    public String time;
    public static final String CREATE_TABLE="CREATE TABLE lockDetails (id INTEGER PRIMARY KEY AUTOINCREMENT,appName TEXT,password TEXT,locked TEXT,temp TEXT,time DATETIME DEFAULT CURRENT_TIMESTAMP)";

     public LockDetails(){

     }
     public LockDetails(int id,String appName,String password,String locked,String temp,String time){
         this.id=id;
         this.appName=appName;
         this.password=password;
         this.locked=locked;
         this.time=time;
         this.temp=temp;
     }
     public String getTemp(){
         return temp;
     }
     public void setTemp(String temp){
         this.temp=temp;
     }
    public int getId(){
         return id;
    }
    public String getTime(){
         return time;
    }
    public String getAppName() {
        return appName;
    }
    public void setAppName(String appName){
         this.appName=appName;
    }
    public String getPassword(){
         return password;
    }
    public void setPassword(String password){
         this.password=password;
    }
    public String getLocked(){
         return locked;
    }
    public void setLocked(String locked){
         this.locked=locked;
    }

}
