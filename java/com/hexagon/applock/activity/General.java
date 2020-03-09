package com.hexagon.applock.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.provider.Settings;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hexagon.applock.Coolworker;
import com.hexagon.applock.R;


import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class General extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(General.this)) {
            startwork(General.this);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startwork(General.this);
        }
        DBHelper fde = new DBHelper(General.this);
        LockDetails lockDetailss = fde.get("watcher");
        if (!fde.successful) {
            fde.insert("watcher", "hexagon", "false", "free");
        }
        DBHelper dbHelper = new DBHelper(General.this);
        LockDetails lockDetails = dbHelper.get(General.this.getPackageName());

        if (!dbHelper.successful) {

            finish();
            startActivity(new Intent(General.this, MainActivity.class));


        } else if (dbHelper.successful && lockDetails.getPassword().matches("hexagon")) {
            finish();
            startActivity(new Intent(General.this, MainActivity.class));

        } else {
            setContentView(R.layout.general);
            RelativeLayout relativeLayout = findViewById(R.id.parent);
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(General.this);
            Drawable drawable = wallpaperManager.getDrawable();
            if (drawable != null) {
                relativeLayout.setBackground(drawable);
            }

            DBHelper dropdown = new DBHelper(General.this);
            LockDetails lockDetails_dropdown = dropdown.get(General.this.getPackageName());
            String[] getit = lockDetails_dropdown.getLocked().split("\\*");
            String get_it = getit[2];

            if (get_it.matches("0")) {
                TextView unlock = findViewById(R.id.unlock);
                unlock.setText("Enter Your password to Continue");
                findViewById(R.id.submit).setVisibility(View.VISIBLE);
            } else if (get_it.matches("1")) {
                TextView unlock = findViewById(R.id.unlock);
                unlock.setText("Enter Your PIN to Continue");
                findViewById(R.id.submit).setVisibility(View.GONE);

            } else if (get_it.matches("2")) {
                TextView unlock = findViewById(R.id.unlock);
                unlock.setText("Enter Your Pattern to Continue");
                findViewById(R.id.submit).setVisibility(View.GONE);
            } else {
                TextView unlock = findViewById(R.id.unlock);
                unlock.setText("Click on the image to Confirm your Biometric/ Fingerprint");
                findViewById(R.id.submit).setVisibility(View.GONE);
            }
            Executor executor= Executors.newSingleThreadExecutor();
            FragmentActivity activity=this;
            final BiometricPrompt biometricPrompt=new BiometricPrompt(activity, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(General.this, "Error "+errString, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Toast.makeText(General.this, "Authentication Successful", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(General.this, "FingerPrint Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            });
            final BiometricPrompt.PromptInfo promptInfo=new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric")
                    .setSubtitle("biometric subtitle")
                    .setNegativeButtonText("cancel")
                    .build();

findViewById(R.id.biometric).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        biometricPrompt.authenticate(promptInfo);
    }
});

            Button forgot = findViewById(R.id.forgot);

            forgot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder forgot = new AlertDialog.Builder(General.this);
                    forgot.setTitle("Forgot Password");
                    String[] data = lockDetails_dropdown.getTemp().split("\\*");
                    forgot.setMessage(data[0]);
                    LayoutInflater layoutInflater = getLayoutInflater();
                    final View customAlertview = layoutInflater.inflate(R.layout.forgot, null);
                    forgot.setView(customAlertview);
                    Button enter = customAlertview.findViewById(R.id.enter);
                    enter.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText text = customAlertview.findViewById(R.id.forgot);
                            if (data[1].toLowerCase().matches(text.getText().toString().toLowerCase())) {
                                finish();
                                startActivity(new Intent(General.this, MainActivity.class));

                            } else {
                                Toast.makeText(General.this, "The Password is Incorrect", Toast.LENGTH_SHORT).show();
                                text.setText("");
                            }
                        }
                    });
                    AlertDialog dialog = forgot.create();
                    dialog.show();
                }
            });
        }


    }


    private void sdcardpermission() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(General.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 5);

        } else {
            finish();
            startActivity(new Intent(General.this, MainActivity.class));

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            finish();
            startActivity(new Intent(General.this, MainActivity.class));
        } else {
            finish();
            Toast.makeText(this, "The permission is required for this app to function properly", Toast.LENGTH_LONG).show();
        }
    }


    protected void startwork(Context context) {

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest saveRequest =
                new PeriodicWorkRequest.Builder(Coolworker.class, 16, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork("Adwork", ExistingPeriodicWorkPolicy.KEEP, saveRequest);
    }



}
