package com.hexagon.applock.activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hexagon.applock.Coolworker;
import com.hexagon.applock.R;

import java.util.concurrent.TimeUnit;

public class AppLock extends Service {
    CoordinatorLayout coordinatorLayout;
    private View lockview;
    private WindowManager windowManager;
    public NotificationManager notificationManager;
    public NotificationCompat.Builder notify;
    public Notification notification;
    String appname;
    public AppLock(){

    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
                 appname = intent.getAction();
        }

        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startNotify();
        coordinatorLayout=new CoordinatorLayout(this){
            public void onCloseSystemDialogs(String reason)
            { new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopSelf();
                }
            },700);
            }

            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){
                    RelativeLayout relativeLayout=lockview.findViewById(R.id.lockwithtime);
relativeLayout.setVisibility(View.GONE);
                    return true;
                }
                return super.dispatchKeyEvent(event);
            }

        };
        coordinatorLayout.setFocusable(true);
        lockview = LayoutInflater.from(this).inflate(R.layout.lock_view,coordinatorLayout);
        WindowManager.LayoutParams params;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
        }else {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
        }

        params.gravity= Gravity.TOP|Gravity.LEFT;
        params.x=0;
        params.y=0;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(lockview,params);
        RelativeLayout relativeLayout=lockview.findViewById(R.id.parent);
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(AppLock.this);
        Drawable drawable=wallpaperManager.getDrawable();
        if(drawable != null){
            relativeLayout.setBackground(drawable);
        }else{

        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                TextView information=lockview.findViewById(R.id.information);
                ImageView appimage=lockview.findViewById(R.id.appimage);
                PackageManager packageManager = AppLock.this.getPackageManager();
                try {
                    ApplicationInfo packaged = packageManager.getApplicationInfo(appname, PackageManager.GET_META_DATA);
                    information.setText(packaged.loadLabel(packageManager)+" has been Locked by Hexagon App Lock, Please enter the Password to gain access into "+packaged.loadLabel(packageManager)+".");
               appimage.setImageDrawable(packaged.loadIcon(packageManager));
                } catch (Exception e) {

                }
            }
        });

        startwork(AppLock.this);

        RelativeLayout lockwithtime=lockview.findViewById(R.id.lockwithtime);
        CheckBox checkBox = lockview.findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    EditText editText = lockview.findViewById(R.id.password);
                    String one = editText.getText().toString();
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    editText.setText(one);
                    editText.setSelection(one.length());
                }
                if (!isChecked) {
                    EditText editText = lockview.findViewById(R.id.password);
                    String one = editText.getText().toString();
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    editText.setText(one);
                    editText.setSelection(one.length());
                }
            }
        });
EditText password=lockview.findViewById(R.id.password);
password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if(actionId== EditorInfo.IME_ACTION_DONE) {
            DBHelper checkvalid = new DBHelper(AppLock.this);
            LockDetails checkvalid1=checkvalid.get(appname);
            String password1=checkvalid1.getPassword();
            if(password1.matches("general")){
                LockDetails supper=checkvalid.get(AppLock.this.getPackageName());
password1=supper.getPassword();
            }
            if(password.getText().toString().matches(password1)){
                TextView select= lockview.findViewById(R.id.selecttime);
                String ansTime=select.getText().toString();
                Intent intent=new Intent(AppLock.this,Watcher.class);
                String action="";
                if(ansTime.toLowerCase().contains("session".toLowerCase())){
                    action="0*"+appname;
                }
                else if(ansTime.toLowerCase().contains("1 minute".toLowerCase())){
                    action="60000*"+appname;
                }
               else if(ansTime.toLowerCase().contains("2 minute".toLowerCase())){
                    action="120000*"+appname;
                }
                else if(ansTime.toLowerCase().contains("5 minute".toLowerCase())){
                    action="300000*"+appname;
                }
                else if(ansTime.toLowerCase().contains("10 minute".toLowerCase())){
                    action="600000*"+appname;
                }
                else if(ansTime.toLowerCase().contains("30 minute".toLowerCase())){
                    action="1800000*"+appname;
                }
                else if(ansTime.toLowerCase().contains("1 Hour".toLowerCase())){
                    action="3600000*"+appname;
                }
                else if(ansTime.toLowerCase().contains("2 Hours".toLowerCase())){
                    action="7200000*"+appname;
                }
                else if(ansTime.toLowerCase().contains("4 Hours".toLowerCase())){
                    action="14400000*"+appname;
                }
                else if(ansTime.toLowerCase().contains("6 Hours".toLowerCase())){
                    action="21600000*"+appname;
                }
                intent.setAction(action);
               if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
              startForegroundService(intent);
               }else{
                   startService(intent);
               }
                stopSelf();
            }else{
                password.setText("");
            }

                return true;
        }
        return false;
    }
});
TextView selecttime=lockview.findViewById(R.id.selecttime);
selecttime.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        lockwithtime.setVisibility(View.VISIBLE);
    }
});
do_radio_buttons();
    }

    protected void startwork(Context context){

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest saveRequest =
                new PeriodicWorkRequest.Builder(Coolworker.class, 16, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork("Adwork", ExistingPeriodicWorkPolicy.KEEP,saveRequest);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        if (lockview != null) {
            windowManager.removeView(lockview);
        }
    }
    private void do_radio_buttons(){
      int [] Radio={R.id.r0,R.id.r1,R.id.r2,R.id.r3,R.id.r4,R.id.r5,R.id.r6,R.id.r7,R.id.r8,R.id.r9};
        String [] questions={"Unlock for this Session","Unlock for 1 Minute","Unlock for 2 Minutes","Unlock for 5 Minutes","Unlock for 10 Minutes","Unlock for 30 Minutes","Unlock for 1 Hour","Unlock for 2 Hours","Unlock for 4 Hours","Unlock for 6 Hours"};

for(int g=0; g<Radio.length;g++){
    int a=g;

    RadioButton radioButton=lockview.findViewById(Radio[g]);
    radioButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
           RelativeLayout relativeLayout=lockview.findViewById(R.id.lockwithtime);
           relativeLayout.setVisibility(View.GONE);
           TextView select= lockview.findViewById(R.id.selecttime);
           select.setText(questions[a]);
        }
    });
}
    }
    public void startNotify() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notify = new NotificationCompat.Builder(getApplicationContext(), "App lock")
                .setSmallIcon(R.drawable.transparent)
                .setContentTitle("Hexagon App Lock")
                .setContentText("Password Verification Needed")
                .setOngoing(true)
                .setColor(ContextCompat.getColor(AppLock.this,R.color.colorAccent2))
                .setVibrate(null)
                .setSound(null)
                .setPriority(Notification.PRIORITY_MIN);
        notify.build().flags = Notification.FLAG_FOREGROUND_SERVICE | Notification.PRIORITY_MIN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelid = "Hexagon App Lock Password Verfication";
            NotificationChannel channel = new NotificationChannel(channelid, "App Lock Password Verfication", NotificationManager.IMPORTANCE_MIN);
            channel.enableVibration(false);
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
            notify.setChannelId(channelid);
        }
        notification = notify.build();

        startForeground(1900, notification);
    }
}
