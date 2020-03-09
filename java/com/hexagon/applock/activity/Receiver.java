package com.hexagon.applock.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DBHelper dbHelper=new DBHelper(context);
LockDetails lockDetails=dbHelper.get(context.getPackageName());
if(dbHelper.successful){
    String [] data=lockDetails.getLocked().split("\\*");
    String reload=data[1];
    if(reload.matches("true")){
        dbHelper.update("watcher","hexagon","true","none");
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O){
    Intent intent1=new Intent(context,Watcher.class);
    intent1.setAction("start");
    context.startForegroundService(intent1);
}else{
    Intent intent1=new Intent(context,Watcher.class);
    intent1.setAction("start");
    context.startService(intent1);
}
    }
}
    }
}
