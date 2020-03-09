package com.hexagon.applock.activity;

import android.graphics.drawable.Drawable;

public class Appdetails {
    public String packagename;
    public String Appname;
    public Drawable icon;

    public Appdetails(){

    }
    public Appdetails(String packagename,String Appname, Drawable icon){
this.packagename=packagename;
this.Appname=Appname;
this.icon=icon;
    }
    public String getPackagename(){
        return packagename;
    }

    public String getAppname(){
        return Appname;
    }
    public Drawable getIcon(){
        return icon;
    }
    public void setPackagename(String packagename){
        this.packagename=packagename;
    }
    public void setAppname(String Appname){
        this.Appname=Appname;
    }
    public void setIcon(Drawable icon){
        this.icon=icon;
    }

}
