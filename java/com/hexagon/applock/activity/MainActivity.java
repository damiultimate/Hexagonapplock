package com.hexagon.applock.activity;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricManager;
import androidx.biometric.FingerprintHelperFragment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.hexagon.applock.Coolworker;
import com.hexagon.applock.R;
import com.hexagon.applock.Webviewone;

public class MainActivity extends AppCompatActivity{
Activity Head;
Handler handler=new Handler(Looper.getMainLooper());
    AlertDialog dialog=null;
    private Toolbar toolbar;
    private Menu menu;
    public int checker;
    public String Appname,Packagename;
    public Drawable Icon;
    String quest,ans;
    public RecyclerView recyclerView;
    private List<Appdetails> applist;
    private adapter adapter;
    DBHelper dbHelper;
    Timer timer=new Timer();
    ArrayList<String> total_apps=new ArrayList<String>();
    LockDetails lockDetails;
    final int DRAW_OVER_OTHER_APPS=1;
    final int USAGE_ACCESS=2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Head = this;
        Head.setTitle("Hexagon App Lock");

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                dbHelper = new DBHelper(MainActivity.this);
                lockDetails = dbHelper.get(MainActivity.this.getPackageName());
                if (!dbHelper.successful) {
                    dbHelper.insert(MainActivity.this.getPackageName(), "hexagon", "0*false*0*false", "none*none");
                    getifisShowhelp();
                }
                DBHelper fde = new DBHelper(MainActivity.this);
                LockDetails lockDetailss = fde.get("watcher");
                if (!fde.successful) {
                    fde.insert("watcher", "hexagon", "false", "free");
                }
            }
        });
        check_permission_draw_on_apps();

        Button start = findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check_permission_usage_stts();
            }
        });
        Button changePassword = findViewById(R.id.change);
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && checkSelfPermission(Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.USE_BIOMETRIC}, 18);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.USE_FINGERPRINT}, 18);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Set General Password");
                    builder.setMessage("Select Password Type.");
                    LayoutInflater layoutInflater = getLayoutInflater();
                    final View customAlertview = layoutInflater.inflate(R.layout.selectpassword, null);
                    DBHelper dropdown = new DBHelper(MainActivity.this);
                    LockDetails lockDetails_dropdown = dropdown.get(MainActivity.this.getPackageName());
                    final String[] arr1 = lockDetails_dropdown.getLocked().split("\\*");
                    String position = arr1[0];
                    String activate = arr1[1];
                    String pass_type = arr1[2];
                    String vibrateor = arr1[3];


                    final String[] value = lockDetails_dropdown.getTemp().split("\\*");
                    quest = value[0];
                    if (quest.matches("none")) {
                        quest = "";
                    }
                    ans = value[1];
                    if (ans.matches("none")) {
                        ans = "";
                    }
                    if (activate.matches("true")) {
                        CheckBox checkBox = customAlertview.findViewById(R.id.reboot);
                        checkBox.setChecked(true);
                    } else {
                        CheckBox checkBox = customAlertview.findViewById(R.id.reboot);
                        checkBox.setChecked(false);
                    }
                    if (position.matches("5")) {
                        EditText editText = customAlertview.findViewById(R.id.customquestion);
                        editText.setVisibility(View.VISIBLE);
                        editText.setText(quest);
                    } else {
                        EditText editText = customAlertview.findViewById(R.id.customquestion);
                        editText.setVisibility(View.GONE);
                        editText.setText(quest);
                    }

                    Spinner select_type = customAlertview.findViewById(R.id.select_type);
                    ArrayAdapter arrayAdapter1 = new ArrayAdapter(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, types());
                    arrayAdapter1.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    select_type.setAdapter(arrayAdapter1);

                    EditText new1 = customAlertview.findViewById(R.id.new1);
                    TextView little = customAlertview.findViewById(R.id.little);
                    EditText new2 = customAlertview.findViewById(R.id.new2);
                    CheckBox checkbox = customAlertview.findViewById(R.id.checkbox);
                    PatternLockView patternLockView = customAlertview.findViewById(R.id.pattern);
                    EditText custom = customAlertview.findViewById(R.id.customquestion);
                    EditText answerr = customAlertview.findViewById(R.id.answer);
                    CheckBox vibrate = customAlertview.findViewById(R.id.vibrate);
                    if (vibrateor.contains("true")) {
                        vibrate.setChecked(true);
                    } else {
                        vibrate.setChecked(false);
                    }


                    if (pass_type.contains("0")) {
                        new1.setVisibility(View.VISIBLE);
                        new2.setVisibility(View.VISIBLE);
                        checkbox.setVisibility(View.VISIBLE);
                        new1.setHint("Enter New Password");
                        new2.setHint("Confirm New Password");
                        patternLockView.setVisibility(View.GONE);

                        checkbox.setText("Show Password");
                        little.setText("Note: Passwords are case sensitive.");
                        new1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        new2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    } else if (pass_type.contains("1")) {
                        little.setText("Enter New PIN");
                        new1.setHint("Enter New PIN");
                        new2.setHint("Confirm New PIN");
                        checkbox.setVisibility(View.VISIBLE);
                        checkbox.setText("Show Pin");
                        patternLockView.setVisibility(View.GONE);

                        new1.setVisibility(View.VISIBLE);
                        new2.setVisibility(View.VISIBLE);
                        new1.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                        new2.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                    } else if (pass_type.contains("2")) {
                        little.setText("Set New Pattern");
                        new1.setVisibility(View.GONE);
                        new2.setVisibility(View.GONE);
                        checkbox.setVisibility(View.GONE);
                        patternLockView.setVisibility(View.VISIBLE);

                    } else if (pass_type.contains("3")) {
                        new1.setVisibility(View.GONE);
                        checkbox.setVisibility(View.GONE);
                        patternLockView.setVisibility(View.GONE);
                        new2.setVisibility(View.GONE);
                        little.setText("Note: A Biometric or Fingerprint Security option has to be enabled within your device in \"SETTINGS\" for optimal functionality within this App.");
                    }
                    select_type.setSelection(Integer.parseInt(pass_type));


                    select_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            patternLockView.setEnableHapticFeedback(false);

                            if (position == 0) {
                                new1.setVisibility(View.VISIBLE);
                                new2.setVisibility(View.VISIBLE);
                                checkbox.setVisibility(View.VISIBLE);
                                new1.setHint("Enter New Password");
                                new2.setHint("Confirm New Password");
                                patternLockView.setVisibility(View.GONE);

                                checkbox.setText("Show Password");
                                little.setText("Note: Passwords are case sensitive.");
                                new1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                new2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                            } else if (position == 1) {
                                little.setText("Enter New PIN");
                                new1.setHint("Enter New PIN");
                                new2.setHint("Confirm New PIN");
                                checkbox.setVisibility(View.VISIBLE);
                                checkbox.setText("Show Pin");
                                patternLockView.setVisibility(View.GONE);

                                new1.setVisibility(View.VISIBLE);
                                new2.setVisibility(View.VISIBLE);
                                new1.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                                new2.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                            } else if (position == 2) {
                                little.setText("Set New Pattern");
                                new1.setVisibility(View.GONE);
                                new2.setVisibility(View.GONE);
                                checkbox.setVisibility(View.GONE);
                                patternLockView.setVisibility(View.VISIBLE);
                                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                in.hideSoftInputFromWindow(new1.getWindowToken(), 0);
                                in.hideSoftInputFromWindow(new2.getWindowToken(), 0);

                            } else if (position == 3) {
                                new1.setVisibility(View.GONE);
                                checkbox.setVisibility(View.GONE);
                                patternLockView.setVisibility(View.GONE);
                                new2.setVisibility(View.GONE);

                                little.setText(getBiometric_word());
                                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                in.hideSoftInputFromWindow(new1.getWindowToken(), 0);
                                in.hideSoftInputFromWindow(new2.getWindowToken(), 0);

                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    String[] questions = {"Select a Question", "What is your favourite Game ?", " What is the name of your best friend/partner ?", "What is your best Movie", "What is your best Song ?", "Set a Custom Question..."};
                    Spinner spinner = customAlertview.findViewById(R.id.spinner);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 5) {
                                EditText editText = customAlertview.findViewById(R.id.customquestion);
                                editText.setText("");
                                editText.setVisibility(View.VISIBLE);
                            } else {
                                EditText editText = customAlertview.findViewById(R.id.customquestion);
                                editText.setText(questions[position]);
                                editText.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, questions);
                    arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    spinner.setAdapter(arrayAdapter);
                    spinner.setSelection(Integer.parseInt(position));
                    EditText editText = customAlertview.findViewById(R.id.answer);
                    editText.setText(ans);
                    CheckBox checkBox = customAlertview.findViewById(R.id.checkbox);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                if (select_type.getSelectedItemPosition() == 0) {
                                    EditText editText = customAlertview.findViewById(R.id.new1);
                                    EditText editText1 = customAlertview.findViewById(R.id.new2);
                                    String one = editText.getText().toString();
                                    String two = editText1.getText().toString();
                                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                                    editText1.setInputType(InputType.TYPE_CLASS_TEXT);
                                    editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                                    editText1.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                                    editText.setText(one);
                                    editText1.setText(two);
                                    editText.setSelection(one.length());
                                    editText1.setSelection(two.length());
                                } else if (select_type.getSelectedItemPosition() == 1) {
                                    EditText editText = customAlertview.findViewById(R.id.new1);
                                    EditText editText1 = customAlertview.findViewById(R.id.new2);
                                    String one = editText.getText().toString();
                                    String two = editText1.getText().toString();
                                    editText.setHint("Enter New PIN");
                                    editText1.setHint("Confirm New PIN");
                                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                                    editText1.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                                    editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                                    editText1.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                                    editText.setText(one);
                                    editText1.setText(two);
                                    editText.setSelection(one.length());
                                    editText1.setSelection(two.length());
                                }
                            }
                            if (!isChecked) {
                                if (select_type.getSelectedItemPosition() == 0) {
                                    EditText editText = customAlertview.findViewById(R.id.new1);
                                    EditText editText1 = customAlertview.findViewById(R.id.new2);
                                    String one = editText.getText().toString();
                                    String two = editText1.getText().toString();

                                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                    editText1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                    editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                    editText1.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                    editText.setText(one);
                                    editText1.setText(two);
                                    editText.setSelection(one.length());
                                    editText1.setSelection(two.length());
                                } else if (select_type.getSelectedItemPosition() == 1) {
                                    EditText editText = customAlertview.findViewById(R.id.new1);
                                    EditText editText1 = customAlertview.findViewById(R.id.new2);
                                    String one = editText.getText().toString();
                                    String two = editText1.getText().toString();
                                    editText.setHint("Enter New PIN");
                                    editText1.setHint("Confirm New PIN");

                                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                                    editText1.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                                    editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                    editText1.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                    editText.setText(one);
                                    editText1.setText(two);
                                    editText.setSelection(one.length());
                                    editText1.setSelection(two.length());
                                }
                            }
                        }
                    });
                    Button dissmiss = customAlertview.findViewById(R.id.dissmiss);
                    dissmiss.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    Button button = customAlertview.findViewById(R.id.update);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText editText = customAlertview.findViewById(R.id.new1);
                            EditText editText1 = customAlertview.findViewById(R.id.new2);
                            if (select_type.getSelectedItemPosition() == 0 || select_type.getSelectedItemPosition() == 1) {
                                if (editText.getText().toString().isEmpty()) {
                                    Toast.makeText(Head, "Please Enter a Valid Password", Toast.LENGTH_SHORT).show();

                                } else if (editText1.getText().toString().isEmpty()) {
                                    Toast.makeText(Head, "Please Enter a Valid Password", Toast.LENGTH_SHORT).show();

                                } else if (editText.getText().toString().contains(" ")) {
                                    Toast.makeText(Head, "White space Characters are not allowed, Password is not Updated", Toast.LENGTH_SHORT).show();

                                } else if (editText1.getText().toString().contains(" ")) {
                                    Toast.makeText(Head, "White space Characters are not allowed, Password is not Updated", Toast.LENGTH_SHORT).show();

                                } else if (editText.getText().toString().matches(editText1.getText().toString())) {
                                    Spinner spinner = customAlertview.findViewById(R.id.spinner);
                                    CheckBox checkBox = customAlertview.findViewById(R.id.reboot);
                                    if (custom.getText().toString().isEmpty()) {
                                        Toast.makeText(MainActivity.this, "Custom question Cannot be empty", Toast.LENGTH_SHORT).show();
                                    } else if (answerr.getText().toString().isEmpty()) {
                                        Toast.makeText(MainActivity.this, "The Answer Field cannot be empty", Toast.LENGTH_SHORT).show();
                                    } else if (answerr.getText().toString().contains(" ")) {
                                        Toast.makeText(MainActivity.this, "The Answer Fiend Cannot contain Whitespace Characters", Toast.LENGTH_SHORT).show();
                                    } else if (spinner.getSelectedItemPosition() == 0) {
                                        Toast.makeText(MainActivity.this, "Please Select Any Question from the Dropdown List", Toast.LENGTH_SHORT).show();
                                    } else {
                                        dbHelper.update(MainActivity.this.getPackageName(), editText.getText().toString(), spinner.getSelectedItemPosition() + "*" + checkBox.isChecked() + "*" + select_type.getSelectedItemPosition() + "*" + vibrate.isChecked(), custom.getText().toString() + "*" + answerr.getText().toString().toLowerCase());
                                        dialog.cancel();
                                        Toast.makeText(Head, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(Head, "New Passwords don't match, Password is not Updated", Toast.LENGTH_SHORT).show();

                                }
                            } else if (select_type.getSelectedItemPosition() == 2) {
                                if (PatternLockUtils.patternToString(patternLockView, patternLockView.getPattern()).length() < 4) {
                                    Toast.makeText(Head, "Please Draw a Pattern that matches at least 4 Dots...", Toast.LENGTH_SHORT).show();

                                } else if (custom.getText().toString().isEmpty()) {
                                    Toast.makeText(MainActivity.this, "Custom question Cannot be empty", Toast.LENGTH_SHORT).show();
                                } else if (answerr.getText().toString().isEmpty()) {
                                    Toast.makeText(MainActivity.this, "The Answer Field cannot be empty", Toast.LENGTH_SHORT).show();
                                } else if (answerr.getText().toString().contains(" ")) {
                                    Toast.makeText(MainActivity.this, "The Answer Fiend Cannot contain Whitespace Characters", Toast.LENGTH_SHORT).show();
                                } else if (spinner.getSelectedItemPosition() == 0) {
                                    Toast.makeText(MainActivity.this, "Please Select Any Question from the Dropdown List", Toast.LENGTH_SHORT).show();
                                } else {
                                    dbHelper.update(MainActivity.this.getPackageName(), PatternLockUtils.patternToString(patternLockView, patternLockView.getPattern()) + "", spinner.getSelectedItemPosition() + "*" + checkBox.isChecked() + "*" + select_type.getSelectedItemPosition() + "*" + vibrate.isChecked(), custom.getText().toString() + "*" + answerr.getText().toString().toLowerCase());
                                    dialog.cancel();
                                    Toast.makeText(Head, "Password Updated Successfully", Toast.LENGTH_SHORT).show();

                                }
                            } else {
                                if (custom.getText().toString().isEmpty()) {
                                    Toast.makeText(MainActivity.this, "Custom question Cannot be empty", Toast.LENGTH_SHORT).show();
                                } else if (answerr.getText().toString().isEmpty()) {
                                    Toast.makeText(MainActivity.this, "The Answer Field cannot be empty", Toast.LENGTH_SHORT).show();
                                } else if (answerr.getText().toString().contains(" ")) {
                                    Toast.makeText(MainActivity.this, "The Answer Fiend Cannot contain Whitespace Characters", Toast.LENGTH_SHORT).show();
                                } else if (spinner.getSelectedItemPosition() == 0) {
                                    Toast.makeText(MainActivity.this, "Please Select Any Question from the Dropdown List", Toast.LENGTH_SHORT).show();
                                } else {
                                    dbHelper.update(MainActivity.this.getPackageName(), "BIOMETRIC", spinner.getSelectedItemPosition() + "*" + checkBox.isChecked() + "*" + select_type.getSelectedItemPosition() + "*" + vibrate.isChecked(), custom.getText().toString() + "*" + answerr.getText().toString().toLowerCase());
                                    dialog.cancel();
                                    Toast.makeText(Head, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                    Button next = customAlertview.findViewById(R.id.next);
                    next.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Animation bring = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up);
                            LinearLayout one = customAlertview.findViewById(R.id.custom_two);
                            LinearLayout two = customAlertview.findViewById(R.id.custom_one);
                            one.startAnimation(bring);
                            bring.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    Animation bring = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up1);
                                    two.setVisibility(View.VISIBLE);
                                    two.startAnimation(bring);
                                    one.setVisibility(View.GONE);

                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                        }

                    });
                    Button back = customAlertview.findViewById(R.id.back);
                    back.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Animation bring = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_down);
                            LinearLayout one = customAlertview.findViewById(R.id.custom_two);
                            LinearLayout two = customAlertview.findViewById(R.id.custom_one);
                            two.startAnimation(bring);
                            bring.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    Animation bring = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_down1);
                                    one.setVisibility(View.VISIBLE);
                                    one.startAnimation(bring);
                                    two.setVisibility(View.GONE);

                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                        }

                    });


                    builder.setView(customAlertview);
                    builder.setCancelable(false);
                    dialog = builder.create();
                    dialog.show();
                }
            }
        });

        Button change = findViewById(R.id.change);
        change.postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
                applist = new ArrayList<>();
                adapter = new adapter(MainActivity.this, applist);
                RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(MainActivity.this, calculateNumberOfColumns(2));
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(adapter);
                prepareAlbums();
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView progressBar = findViewById(R.id.progressBar_cyclic);
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 300);

    }
    public String getBiometric_word() {

        BiometricManager biometricManager = BiometricManager.from(MainActivity.this);
        int Valid = biometricManager.canAuthenticate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Valid == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
            return "You have not Enabed any Biometric/FingerPrint Security Option on this device, You would not be able to unlock your device Unless a Biometric/FingerPrint Seurity option is Enabled. You can enable the option within \"SETTINGS\" ";

        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Valid == BiometricManager.BIOMETRIC_SUCCESS) {
            return "You have Enabled a Biometric/FingerPrint option within your device, You are good to go";

        }
        else{
            return "";
        }
    }
    public String[] types(){
        BiometricManager biometricManager=BiometricManager.from(MainActivity.this);
        int Valid=biometricManager.canAuthenticate();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Valid==BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE){
            String[] types = {"Password", "PIN", "Pattern"};
            return types;
        }else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && Valid==BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE){
            String[] types = {"Password", "PIN", "Pattern"};
            return types;

        }else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && Valid==BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED){
            String[] types = {"Password", "PIN", "Pattern", "Biometric/Fingerprint"};
            return types;

        }
        else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && Valid==BiometricManager.BIOMETRIC_SUCCESS){
            String[] types = {"Password", "PIN", "Pattern", "Biometric/Fingerprint"};
            return types;

        }else{
            String[] types = {"Password", "PIN", "Pattern"};
            return types;

        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(Head)) {
            startwork(MainActivity.this);
        }
        if(Build.VERSION.SDK_INT <Build.VERSION_CODES.M){
            startwork(MainActivity.this);
        }
        DBHelper dbHelper=new DBHelper(MainActivity.this);
        LockDetails lockDetails=dbHelper.get("watcher");
        if(!dbHelper.successful){
            dbHelper.insert("watcher","hexagon","false","free");
        }
        DBHelper fde=new DBHelper(MainActivity.this);
        LockDetails lockDetailss=fde.get("watcher");
        LockDetails lockk1=fde.get(MainActivity.this.getPackageName());
         if(lockDetailss.getLocked().matches("true")){
           // fde.update("watcher","hexagon","true","none");
             Intent intent=new Intent(Head,Watcher.class);
             intent.setAction("start");
             startService(intent);
            Button button=findViewById(R.id.start);
            button.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.edittext3));
            button.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.colorPrimary));
            button.setText("DEACTIVATE");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        finish();
        startActivity(new Intent(MainActivity.this,General.class));
        super.onRestart();
    }

    private void prepareAlbums(){
final PackageManager packageManager=this.getPackageManager();
List<ApplicationInfo> packages=packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
for(ApplicationInfo applicationInfo:packages){
    if(packageManager.getLaunchIntentForPackage(applicationInfo.packageName)!=null){
   if(!applicationInfo.packageName.toLowerCase().contains("hexagon.applock")){
       Appdetails appdetails = new Appdetails(applicationInfo.packageName,applicationInfo.loadLabel(packageManager).toString(),applicationInfo.loadIcon(packageManager));
       applist.add(appdetails);
       DBHelper checkvalid = new DBHelper(MainActivity.this);
       LockDetails checkvalid1=checkvalid.get(applicationInfo.packageName);
       if(!checkvalid.successful){
           LockDetails general = checkvalid.get(MainActivity.this.getPackageName());
           checkvalid.insert(applicationInfo.packageName,"general*0","false","false");
       }else{
           LockDetails general = checkvalid.get(MainActivity.this.getPackageName());
           LockDetails app = checkvalid.get(applicationInfo.packageName);
           checkvalid.update(applicationInfo.packageName,app.getPassword(),app.getLocked(),"false");
       }
   }

    }
}

}
private void check_permission_draw_on_apps(){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

        //If the draw over permission is not available open the settings screen
        //to grant the permission.
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivityForResult(intent, DRAW_OVER_OTHER_APPS);
        timer.scheduleAtFixedRate(new TimerTask() {
            TimerTask timerr=this;
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            AppOpsManager appOpsManager = (AppOpsManager) MainActivity.this.getSystemService(Context.APP_OPS_SERVICE);
                            final int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, android.os.Process.myUid(), MainActivity.this.getPackageName());
                            boolean granted = mode == AppOpsManager.MODE_DEFAULT ? (MainActivity.this.checkCallingOrSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED) : (mode == AppOpsManager.MODE_ALLOWED);
                            if (granted) {
                                finish();
                                timerr.cancel();
                                startActivity(new Intent(MainActivity.this, MainActivity.class));

                            }
                        }

                    }
                });
            }
        },0,120);
    }else{

            }

}


    private void check_permission_usage_stts(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !hasUsageStats(this)) {
            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PackageManager packageManager = MainActivity.this.getPackageManager();
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, USAGE_ACCESS);
                timer.scheduleAtFixedRate(new TimerTask() {
                    TimerTask timerr=this;
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(hasUsageStats(MainActivity.this)){
                                    finish();
                                    timerr.cancel();
                                    startActivity(new Intent(MainActivity.this,MainActivity.class));
                                    DBHelper fde=new DBHelper(MainActivity.this);
                                    LockDetails lockDetailss=fde.get("watcher");
                                    LockDetails lockk1=fde.get(MainActivity.this.getPackageName());
                                    if (lockk1.getPassword().toLowerCase().matches("hexagon")) {
                                        Toast.makeText(MainActivity.this, "You cannot Activate the App Lock without setting a General Password", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            }
                        });
                    }
                },0,120);
                Toast.makeText(Head, "You have to enable Usage Access Permission for Hexagon App Lock", Toast.LENGTH_LONG).show();

            } else {
AlertDialog.Builder builder=new AlertDialog.Builder(Head);
builder.setTitle("Error ");
builder.setMessage("You have to enable Usage Access Permission for \"Hexagon App Lock\" within \"Settings\" in order for the app to function properly. Your device does not allow apps to open \"Settings\" so this has to be done by you. We're very sorry for any Inconveniences .");
builder.setPositiveButton("Alright", new DialogInterface.OnClickListener() {
    @Override
    public void onClick(DialogInterface dialog, int which) {
       dialog.cancel();
    }
});
builder.show();

            }

              }
              else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},5);
            Toast.makeText(Head, "You have to enable the SD card permission for this app to function properly", Toast.LENGTH_SHORT).show();
        }
              else{

            DBHelper fde=new DBHelper(MainActivity.this);
            LockDetails lockDetailss=fde.get("watcher");
            LockDetails lockk1=fde.get(MainActivity.this.getPackageName());

            if (lockk1.getPassword().toLowerCase().matches("hexagon")) {
                Toast.makeText(MainActivity.this, "You cannot Activate the App Lock without setting a General Password", Toast.LENGTH_SHORT).show();
            }
            else if(lockDetailss.getLocked().matches("false")){
                fde.update("watcher","hexagon","true","none");
                Intent intent=new Intent(Head,Watcher.class);
                intent.setAction("start");
                startService(intent);
                Button button=findViewById(R.id.start);
                button.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.edittext3));
                button.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.colorPrimary));
                button.setText("DEACTIVATE");
//startService(new Intent(MainActivity.this,Webviewone.class));
            }else{
                fde.update("watcher","hexagon","false","none");
                Intent intent=new Intent(Head,Watcher.class);
                intent.setAction("stop");
                startService(intent);
                Button button=findViewById(R.id.start);
                button.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.edittext2));
                button.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.textColorPrimary));
                button.setText("ACTIVATE");
            }

        }
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



@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public boolean hasUsageStats(Context context){
        AppOpsManager appOpsManager=(AppOpsManager)context.getSystemService(Context.APP_OPS_SERVICE);
        int mode=appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(),context.getPackageName());
    boolean granted = mode == AppOpsManager.MODE_DEFAULT ? (MainActivity.this.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED) : (mode == AppOpsManager.MODE_ALLOWED);
return granted;
}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==5){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                findViewById(R.id.start).performClick();
            }
        }
        if(requestCode==18){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                findViewById(R.id.change).performClick();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
       if(requestCode==DRAW_OVER_OTHER_APPS){
           if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
               AppOpsManager appOpsManager=(AppOpsManager)MainActivity.this.getSystemService(Context.APP_OPS_SERVICE);
               final int mode=appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,android.os.Process.myUid(),MainActivity.this.getPackageName());
               boolean granted=mode==AppOpsManager.MODE_DEFAULT ? (MainActivity.this.checkCallingOrSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)==PackageManager.PERMISSION_GRANTED):(mode==AppOpsManager.MODE_ALLOWED);
               if(!granted) {
                   Toast.makeText(MainActivity.this, "The Overlay permission is required  for this app to function properly", Toast.LENGTH_LONG).show();
                   finish();
               }else{

               }
           }
       }
       else if(requestCode==USAGE_ACCESS){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                AppOpsManager appOpsManager=(AppOpsManager)MainActivity.this.getSystemService(Context.APP_OPS_SERVICE);
                final int mode=appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(),MainActivity.this.getPackageName());
                boolean granted=mode==AppOpsManager.MODE_DEFAULT ? (MainActivity.this.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS)==PackageManager.PERMISSION_GRANTED):(mode==AppOpsManager.MODE_ALLOWED);
                if(!granted) {
                    Toast.makeText(MainActivity.this, "The Usage Access permission is required  for this app to function properly", Toast.LENGTH_LONG).show();
                }else{

                }
            }
        }
       else{
           super.onActivityResult(requestCode, resultCode, data);
       }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            getifisShowhelp();
        }
        if(id==R.id.contact){
            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:hexagontranslator@gmail.com"));
            startActivity(intent);
        }
        if(id==R.id.terms){
            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("http://hexagonapplock.blogspot.com"));
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    public void getifisShowhelp(){

        AlertDialog.Builder builder=new AlertDialog.Builder(Head);
        builder.setTitle("Welcome To The Hexagon Applock");
        builder.setMessage("Welcome to the Hexagon App Lock.\n" +
                "\n" +
                "IMPORTANT NOTICE!!!!!\n" +
                "This app might not have complete functionality on certain devices because of memory management/battery optimization apps. These apps stop applications from running in the background with full capacity. App Lock runs in the background making it difficult to accomplish important tasks on these devices. Even when the “Activate Applock on Device Reboot” is activated, when the device is rebooted, the applock might not start because of the Memory management /Battery optimization app. Devices like Samsung, Huwei, Oppo, Tecno, Infinix, etc…(and many others) fall under this category. On some of these devices, the app lock might stop functioning when it is swiped away from the “recent apps list” or even “when closed for a long time”. For this reason, it is advisable to give the App Lock full functionality by enabling/White listing the App lock on your Memory management /Battery optimization app ( if you’re experiencing the applock closing or not functioning for any reason aside you explicitly stopping it ). Different manufacturers have different apps that optimizes battery usage/memory management, Contact your device manufacturer for more information.\n" +
                "\n" +
                "How do I Lock an app?: Firstly, you can only lock an application if a general password is set. Once you’ve set your general password, you’d be able to lock any application of choice.\n" +
                "\n" +
                "How do I start the App Lock?: You click on the “Activate” Button on the top right corner of the application. You’d have to enable the usage access permission if your Android Version is from 5.0 upwards before you’d be able to Activate the App Lock.\n" +
                "Note: You can only unlock an app by inputting your password and pressing the “Enter key” on your keyboard.\n" +
                "\n" +
                "I Want to Set different Passwords for Different Apps: We have provided the means to do this. All you have to do is Perform a Long click the on the app you want to set the password and you should see a popup, click on the popup and you’d be able to set a custom password for each app.\n" +
                "\n" +
                "I Want the App Lock to Start Anytime I Reboot my device: Click on the “Set general password” button at the top left corner of the application, an option to do so is available.\n" +
                "\n" +
                "I Have Forgotten My Application’s/General Password: For this reason, we have put a password recovery question that you’d be able to answer within the general password setting popup. Once you’ve forgotten your password, you can only reset it within the App lock.\n" +
                "Note: The App Lock becomes Locked after you’ve set your passwords to prevent unauthorized entry. You can unlock it with either your general password or By answering the question you’ve set.\n" +
               "\n");
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setCancelable(false);

            builder.show();

    }
    protected int calculateNumberOfColumns(int base) {


        int columns = base;
        String screenSize = getScreenSizeCategory();
        if (screenSize.equals("small")) {
            if (base != 1) {
                columns = columns - 1;
            }
        } else if (screenSize.equals("normal")) {
            columns = columns - 1;
        } else if (screenSize.equals("large")) {

        } else if (screenSize.equals("xlarge")) {

        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            columns = columns + 1;
        }

        return columns;
    }

    protected String getScreenOrientation() {
        String orientation = "undefined";

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            orientation = "landscape";
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            orientation = "portrait";
        }

        return orientation;
    }

    // Custom method to get screen size category
    protected String getScreenSizeCategory() {
        int screenLayout = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenLayout) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                // small screens are at least 426dp x 320dp
                return "small";
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                // normal screens are at least 470dp x 320dp
                return "normal";
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                // large screens are at least 640dp x 480dp
                return "large";
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                // xlarge screens are at least 960dp x 720dp
                return "xlarge";
            default:
                return "undefined";
        }
    }
}
