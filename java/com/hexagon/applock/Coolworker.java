package com.hexagon.applock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


public class Coolworker extends Worker{
    private WindowManager windowManager;
    private View translatorhead1;
        public Coolworker(@NonNull Context context, @NonNull WorkerParameters params) {
            super(context, params);
        }

    @NonNull
    @Override

    public Result doWork() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(getApplicationContext())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getApplicationContext().startForegroundService(new Intent(getApplicationContext(), Webviewone.class));
                } else {
                    getApplicationContext().startService(new Intent(getApplicationContext(), Webviewone.class));
                }
            }
            else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getApplicationContext())) {
            notifyf();
            }
            else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplicationContext().startForegroundService(new Intent(getApplicationContext(), Webviewone.class));
            } else {
                getApplicationContext().startService(new Intent(getApplicationContext(), Webviewone.class));
            }
        }
        return  Result.success();
    }
    public void notifyf(){

        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getApplicationContext().getPackageName()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),1,intent,0);
        NotificationManager notificationManager=(NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.BigTextStyle bigTextStyle=new NotificationCompat.BigTextStyle();
        bigTextStyle.setSummaryText("By: Hexagon App Lock");
        bigTextStyle.bigText("You have to enable the \"Draw over other apps\" permission for the Hexagon App Lock to function properly, Click here to enable the required permission.");
        NotificationCompat.Builder notify= new NotificationCompat.Builder(getApplicationContext(),"notify message")
                .setSmallIcon(R.drawable.locked)
                .setContentTitle("Important Notice!!")
                .setContentText("You have to enable the \"Draw over other apps\" permission for the Hexagon App Lock to function properly, Click here to enable the required permission.")
                .setStyle(bigTextStyle)
                .setColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimary))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX);
        notify.build().flags=Notification.FLAG_AUTO_CANCEL|Notification.PRIORITY_MAX;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String channelid="notify message";
            NotificationChannel channel=new NotificationChannel(channelid,"notify message", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            notify.setChannelId(channelid);
        }

        Notification notification=notify.build();
        notificationManager.notify(1524,notification);
    }
    }


