package com.hexagon.applock.activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.hexagon.applock.R;
import com.rvalerio.fgchecker.AppChecker;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Watcher extends Service {
    public NotificationManager notificationManager;
    public NotificationCompat.Builder notify;
    public Notification notification;
Timer timer=new Timer();
String appname="",appname1="";
String action="";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            action = intent.getAction();
            if (action.matches("stop")) {
                DBHelper fde=new DBHelper(Watcher.this);
                LockDetails lockDetailss=fde.get("watcher");
                LockDetails lockk1=fde.get(Watcher.this.getPackageName());

                fde.update("watcher","hexagon","false","none");

                stopSelf();
            }else if(!action.matches("start")){
                String[]received=action.split("\\*");
                DBHelper dbHelper = new DBHelper(Watcher.this);
                LockDetails lockDetails = dbHelper.get(received[1]);
             dbHelper.update(received[1], lockDetails.getPassword(), lockDetails.getLocked(), "true");
           new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
               @Override
               public void run() {
                   dbHelper.update(received[1], lockDetails.getPassword(), lockDetails.getLocked(), "false");
               }
           },Integer.parseInt(received[0]));

            }

        }else{
            prepareAlbums();
        }


        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        DBHelper dbHelper = new DBHelper(Watcher.this);
        LockDetails lockDetails = dbHelper.get("watcher");
        if (!dbHelper.successful) {
            dbHelper.insert("watcher", "hexagon", "false", "false");
        }
appname=startWatch(Watcher.this);
        appname1=startWatch(Watcher.this);
startNotify();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                appname=startWatch(Watcher.this);

                OnChanged();
            }
        },0,190);

        prepareAlbums();
    }

public String startWatch(Context context){
    AppChecker appChecker=new AppChecker();
    String packageName=appChecker.getForegroundApp(context);
    String neww="pumpkin";
if(!packageName.contains("hexagon.applo")) {
    neww = packageName;
}
      return neww;
}
public void OnChanged(){
      appname=startWatch(Watcher.this);
      if(!appname.matches(appname1)){
          DBHelper checkvalid = new DBHelper(Watcher.this);
          LockDetails locker=checkvalid.get(appname);
          if(checkvalid.successful){
             String locked= locker.getLocked();
             String tempp=locker.getTemp();
             if(locked.matches("true") && tempp.matches("false")){
                 Intent intent=new Intent(Watcher.this,AppLock.class);
                 intent.setAction(appname);
                if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O){
                     startForegroundService(intent);
                 }else{
                     startService(intent);
                 }
             }
          }
      }
    appname1=startWatch(Watcher.this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!action.matches("stop")) {
            Intent intent=new Intent(Watcher.this,Watcher.class);
            intent.setAction("start");
            if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O){
                startForegroundService(intent);
            }else{
                startService(intent);
            }
        }

        DBHelper dbHelper = new DBHelper(Watcher.this);
        LockDetails lockDetails = dbHelper.get(Watcher.this.getPackageName());
        dbHelper.update("watcher", "hexagon", "false", "free");

    prepareAlbums();
    }

    public void startNotify() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notify = new NotificationCompat.Builder(getApplicationContext(), "App lock")
                .setSmallIcon(R.drawable.locked)
                .setContentTitle("App Lock is Running")
                .setContentText("Applock is  Preventing Your Apps From Unauthorized Entry")
                .setOngoing(true)
                .setColor(ContextCompat.getColor(Watcher.this,R.color.colorPrimary))
                .setVibrate(null)
                .setSound(null)
                .setPriority(Notification.PRIORITY_MAX);
        notify.build().flags = Notification.FLAG_NO_CLEAR | Notification.PRIORITY_MAX | Notification.FLAG_ONGOING_EVENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelid = "Hexagon App Lock";
            NotificationChannel channel = new NotificationChannel(channelid, "App lock", NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(false);
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
            notify.setChannelId(channelid);

        }
        notification = notify.build();

        startForeground(1000, notification);
    }

    private void prepareAlbums() {
        final PackageManager packageManager = this.getPackageManager();
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo applicationInfo : packages) {
            if (packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null) {
                if (!applicationInfo.packageName.toLowerCase().contains("hexagon.applock")) {
                    DBHelper checkvalid = new DBHelper(Watcher.this);
                   LockDetails lockDetailsi=checkvalid.get(applicationInfo.packageName);
                    if (!checkvalid.successful) {
                        checkvalid.insert(applicationInfo.packageName, "general", "false", "false");
                    } else {
                        LockDetails app = checkvalid.get(applicationInfo.packageName);
                        checkvalid.update(applicationInfo.packageName, app.getPassword(), app.getLocked(), "false");
                    }
                }

            }
        }
    }

}
