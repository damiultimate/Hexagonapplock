package com.hexagon.applock.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.hexagon.applock.R;;

public class adapter extends RecyclerView.Adapter<adapter.MyViewHolder> {
    private Activity mContext;
    AlertDialog dialog=null;
    private List<Appdetails> applist;
    PopupMenu popupMenu;
    public class MyViewHolder extends RecyclerView.ViewHolder {
public TextView appname,packagename;
public CardView cardView;
public ImageView appimage;
public ImageView listlocker;

        public MyViewHolder(View view) {
            super(view);
            appname=(TextView)view.findViewById(R.id.applisttruename);
            packagename=(TextView)view.findViewById(R.id.packagename);
            appimage=(ImageView)view.findViewById(R.id.applistimage);
            cardView=(CardView)view.findViewById(R.id.cardview);
            listlocker=(ImageView)view.findViewById(R.id.opennclose);
        }


    }
    public adapter(Activity mContext, List<Appdetails> applist){
        this.mContext = mContext;
        this.applist = applist;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.applist, viewGroup, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int indexx)  {
     //  myViewHolder.setIsRecyclable(false);
Appdetails appdetails = applist.get(indexx);
myViewHolder.setIsRecyclable(false);
myViewHolder.appname.setText(appdetails.getAppname());
        myViewHolder.packagename.setText(appdetails.getPackagename());
myViewHolder.appimage.setImageDrawable(appdetails.getIcon());
        DBHelper checkvalid = new DBHelper(mContext);
        LockDetails checkvalid1=checkvalid.get(appdetails.getPackagename());
        if(!checkvalid.successful){
            LockDetails general = checkvalid.get(mContext.getPackageName());
            checkvalid.insert(appdetails.getPackagename(),"general*0","false","false");

        }
        else if(checkvalid1.getLocked().matches("false")){
            // notifyItemChanged(indexx);
           // checkvalid.update(appdetails.getPackagename(),checkvalid1.getPassword() , "true", "false");
            myViewHolder.listlocker.setImageResource(R.drawable.open);

        }else{
            //  notifyItemChanged(indexx);
           // checkvalid.update(appdetails.getPackagename(),checkvalid1.getPassword() , "false", "false");
            myViewHolder.listlocker.setImageResource(R.drawable.locked);
        }

myViewHolder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
    @Override
    public boolean onLongClick(View v) {
        popupMenu= new PopupMenu(mContext,v);
        MenuInflater inflater = mContext.getMenuInflater();
        inflater.inflate(R.menu.menu_main2, popupMenu.getMenu());
        popupMenu.getMenu().getItem(0).setTitle("Set Custom "+ appdetails.getAppname()+" Password");
       popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
           @Override
           public boolean onMenuItemClick(MenuItem item) {
               switch (item.getItemId()) {
                   case R.id.setcuspassword:
                       DBHelper dbHelper = new DBHelper(mContext);
                       LockDetails lockDetails = dbHelper.get(mContext.getPackageName());
                       if (lockDetails.getPassword().toLowerCase().matches("hexagon")) {
                           Toast.makeText(mContext, "You cannot set a custom password for " + appdetails.getAppname() + " without setting a General Password", Toast.LENGTH_SHORT).show();
                       }else{

                       AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                       builder.setMessage("Select Password Type");
                       builder.setTitle("Set Custom Password For " + appdetails.getAppname());
                       LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                       final View customAlertview = layoutInflater.inflate(R.layout.custom, null);
                       ImageView image = customAlertview.findViewById(R.id.image);
                       PackageManager packageManager = mContext.getPackageManager();
                       try {
                           ApplicationInfo packaged = packageManager.getApplicationInfo(appdetails.getPackagename(), PackageManager.GET_META_DATA);
                           image.setImageDrawable(packaged.loadIcon(packageManager));
                       } catch (PackageManager.NameNotFoundException e) {
                           image.setImageResource(R.drawable.icon);
                       }

                           String[] types = types(mContext);

                           Spinner select_type = customAlertview.findViewById(R.id.spinner);
                           ArrayAdapter arrayAdapter1 = new ArrayAdapter(mContext, R.layout.support_simple_spinner_dropdown_item, types);
                           arrayAdapter1.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                           select_type.setAdapter(arrayAdapter1);
                           EditText new1=customAlertview.findViewById(R.id.new1);
                           TextView little=customAlertview.findViewById(R.id.little);
                           EditText new2=customAlertview.findViewById(R.id.new2);
                           PatternLockView patternLockView=customAlertview.findViewById(R.id.pattern);
                           select_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                               @Override
                               public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                   patternLockView.setEnableHapticFeedback(false);
                                   CheckBox checkBox = customAlertview.findViewById(R.id.checkbox);
if(position==0){
    new1.setVisibility(View.GONE);
    checkBox.setVisibility(View.GONE);
    InputMethodManager in = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    in.hideSoftInputFromWindow(new1.getWindowToken(), 0);
    in.hideSoftInputFromWindow(new2.getWindowToken(), 0);

    patternLockView.setVisibility(View.GONE);
    new2.setVisibility(View.GONE);
    little.setText("The General passsord you have set will be applied to "+appdetails.getAppname());

}
                                   if(position==1){
                                       new1.setVisibility(View.VISIBLE);
                                       new2.setVisibility(View.VISIBLE);
                                       new1.setHint("Enter New Password");
                                       new2.setHint("Confirm New Password");
                                       patternLockView.setVisibility(View.GONE);
checkBox.setVisibility(View.VISIBLE);
                                       little.setText("Note: Passwords are case sensitive.");
                                       new1.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                       new2.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);

                                   }else if(position==2){
                                       little.setText("Enter New PIN");
                                       new1.setHint("Enter New PIN");
                                       new2.setHint("Confirm New PIN");
                                       patternLockView.setVisibility(View.GONE);
                                       checkBox.setVisibility(View.VISIBLE);

                                       new1.setVisibility(View.VISIBLE);
                                       new2.setVisibility(View.VISIBLE);
                                       new1.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                                       new2.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                                   }else if(position==3){
                                       little.setText("Set New Pattern");
                                       new1.setVisibility(View.GONE);
                                       new2.setVisibility(View.GONE);
                                       patternLockView.setVisibility(View.VISIBLE);
                                       checkBox.setVisibility(View.GONE);
                                       InputMethodManager in = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                                       in.hideSoftInputFromWindow(new1.getWindowToken(), 0);
                                       in.hideSoftInputFromWindow(new2.getWindowToken(), 0);


                                   }
                                   else if (position==4){
                                       new1.setVisibility(View.GONE);
                                       checkBox.setVisibility(View.GONE);
                                       InputMethodManager in = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                                       in.hideSoftInputFromWindow(new1.getWindowToken(), 0);
                                       in.hideSoftInputFromWindow(new2.getWindowToken(), 0);

                                       patternLockView.setVisibility(View.GONE);
                                       new2.setVisibility(View.GONE);
                                       little.setText(getBiometric_word(mContext));
                                   }
                               }

                               @Override
                               public void onNothingSelected(AdapterView<?> parent) {

                               }
                           });



                       CheckBox checkBox = customAlertview.findViewById(R.id.checkbox);
                       checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                           @Override
                           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                               if (isChecked) {
                                   if(select_type.getSelectedItemPosition()==0) {
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
                                   }else if(select_type.getSelectedItemPosition()==1){
                                       EditText editText = customAlertview.findViewById(R.id.new1);
                                       EditText editText1 = customAlertview.findViewById(R.id.new2);
                                       String one = editText.getText().toString();
                                       String two = editText1.getText().toString();
                                       editText.setHint("Enter New PIN");
                                       editText1.setHint("Confirm New PIN");
                                       editText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_NORMAL);
                                       editText1.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_NORMAL);
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

                           DBHelper selekt = new DBHelper(mContext);
                           int position = 0;
                           if(!selekt.successful){
                               selekt.insert(appdetails.getPackagename(),"general*0","false","false");

                           }
                           if(selekt.successful){
                               LockDetails selekt1 = selekt.get(appdetails.getPackagename());
                               String[] position1=selekt1.getPassword().split("\\*");
                               position=Integer.parseInt(position1[1])+1;
                           }
if(position==1){
    select_type.setSelection(0);

    new1.setVisibility(View.GONE);
    checkBox.setVisibility(View.GONE);
    InputMethodManager in = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    in.hideSoftInputFromWindow(new1.getWindowToken(), 0);
    in.hideSoftInputFromWindow(new2.getWindowToken(), 0);

    patternLockView.setVisibility(View.GONE);
    new2.setVisibility(View.GONE);
    little.setText("The General passsord you have set will be applied to this app");
}
    if (position==2) {
        select_type.setSelection(1);
        new1.setVisibility(View.VISIBLE);
        new2.setVisibility(View.VISIBLE);
        new1.setHint("Enter New Password");
        new2.setHint("Confirm New Password");
        patternLockView.setVisibility(View.GONE);
        checkBox.setVisibility(View.VISIBLE);
        little.setText("Note: Passwords are case sensitive.");
        new1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

    } else if (position==3) {
        select_type.setSelection(2);

        little.setText("Enter New PIN");
        new1.setHint("Enter New PIN");
        new2.setHint("Confirm New PIN");
        patternLockView.setVisibility(View.GONE);
        checkBox.setVisibility(View.VISIBLE);

        new1.setVisibility(View.VISIBLE);
        new2.setVisibility(View.VISIBLE);
        new1.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        new2.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
    } else if (position==4) {
        select_type.setSelection(3);

        little.setText("Set New Pattern");
        new1.setVisibility(View.GONE);
        new2.setVisibility(View.GONE);
        patternLockView.setVisibility(View.VISIBLE);
        checkBox.setVisibility(View.GONE);
        InputMethodManager in = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(new1.getWindowToken(), 0);
        in.hideSoftInputFromWindow(new2.getWindowToken(), 0);


    } else if (position==5) {
        select_type.setSelection(4);

        new1.setVisibility(View.GONE);
        checkBox.setVisibility(View.GONE);
        InputMethodManager in = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(new1.getWindowToken(), 0);
        in.hideSoftInputFromWindow(new2.getWindowToken(), 0);

        patternLockView.setVisibility(View.GONE);
        new2.setVisibility(View.GONE);
        little.setText(getBiometric_word(mContext));
}


                       Button button = customAlertview.findViewById(R.id.update);
                       button.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                               EditText editText = customAlertview.findViewById(R.id.new1);
                               EditText editText1 = customAlertview.findViewById(R.id.new2);
                               if(select_type.getSelectedItemPosition()==1 || select_type.getSelectedItemPosition()==2) {
                                   if (editText.getText().toString().isEmpty() && editText1.getText().toString().isEmpty()) {
                                         Toast.makeText(mContext, "The Password field cannot be empty", Toast.LENGTH_SHORT).show();

                                   } else if (editText.getText().toString().isEmpty() && !editText1.getText().toString().isEmpty()) {
                                       Toast.makeText(mContext, "Please Enter a Valid Password", Toast.LENGTH_SHORT).show();

                                   } else if (!editText.getText().toString().isEmpty() && editText1.getText().toString().isEmpty()) {
                                       Toast.makeText(mContext, "Please Enter a Valid Password", Toast.LENGTH_SHORT).show();

                                   } else if (editText.getText().toString().contains(" ")) {
                                       Toast.makeText(mContext, "White space Characters are not allowed, Password is not Updated", Toast.LENGTH_SHORT).show();

                                   } else if (editText1.getText().toString().contains(" ")) {
                                       Toast.makeText(mContext, "White space Characters are not allowed, Password is not Updated", Toast.LENGTH_SHORT).show();

                                   } else if (editText.getText().toString().matches(editText1.getText().toString())) {
                                       DBHelper helper = new DBHelper(mContext);
                                       LockDetails lockkz = helper.get(appdetails.getPackagename());
                                       dbHelper.update(appdetails.getPackagename(), editText.getText().toString()+"*"+select_type.getSelectedItemPosition()+"", lockkz.getLocked(), lockkz.getTemp());
                                       Toast.makeText(mContext, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                                       dialog.cancel();
                                   } else {
                                       Toast.makeText(mContext, "New Passwords don't match, Password is not Updated", Toast.LENGTH_SHORT).show();

                                   }
                               }else if(select_type.getSelectedItemPosition()==3){
                                   if(PatternLockUtils.patternToString(patternLockView,patternLockView.getPattern()).length()==0){
                                        Toast.makeText(mContext, "Please match at least 4 dots for Pattern Lock", Toast.LENGTH_SHORT).show();
                                   }
                                   else if(PatternLockUtils.patternToString(patternLockView,patternLockView.getPattern()).length() < 4){
                                       Toast.makeText(mContext, "Please match at least 4 dots for Pattern Lock", Toast.LENGTH_SHORT).show();

                                   }else{
                                       DBHelper helper = new DBHelper(mContext);
                                       LockDetails lockk = helper.get(mContext.getPackageName());
                                       LockDetails lockkz = helper.get(appdetails.getPackagename());
                                       dbHelper.update(appdetails.getPackagename(), PatternLockUtils.patternToString(patternLockView,patternLockView.getPattern())+"*"+select_type.getSelectedItemPosition()+"",lockkz.getLocked(), lockkz.getTemp());
                                       Toast.makeText(mContext, "Password Updated Successfully, Pattern lock set for " + appdetails.getAppname(), Toast.LENGTH_SHORT).show();
                                       dialog.cancel();
                                   }
                               }else if(select_type.getSelectedItemPosition()==0){
                                   DBHelper helper = new DBHelper(mContext);
                                   LockDetails lockk = helper.get(mContext.getPackageName());
                                   LockDetails lockkz = helper.get(appdetails.getPackagename());
                                   dbHelper.update(appdetails.getPackagename(), "general*0", lockkz.getLocked(), lockkz.getTemp());
                                   Toast.makeText(mContext, "Password Updated Successfully, General password set for "+appdetails.getAppname(), Toast.LENGTH_SHORT).show();
                              dialog.cancel();
                               }
                               else{
                                   DBHelper helper = new DBHelper(mContext);
                                   LockDetails lockk = helper.get(mContext.getPackageName());
                                   LockDetails lockkz = helper.get(appdetails.getPackagename());
                                   dbHelper.update(appdetails.getPackagename(), "BIOMETRIC*4",lockkz.getLocked(), lockkz.getTemp());
                                   Toast.makeText(mContext, "Password Updated Successfully, Biometric Password set for " + appdetails.getAppname(), Toast.LENGTH_SHORT).show();
                                   dialog.cancel();
                               }

                           }
                       });
                       builder.setView(customAlertview);
                       dialog = builder.create();
                       dialog.show();
                       break;

               }
           }
               return true;
           }
       });
        popupMenu.show();
        return true;

    }
});


        myViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHelper helper = new DBHelper(mContext);
                LockDetails lockk = helper.get(appdetails.getPackagename());
                String pass=lockk.getPassword();
              LockDetails lockk1 = helper.get(mContext.getPackageName());

                if (lockk1.getPassword().toLowerCase().matches("hexagon")) {
                    Toast.makeText(mContext, "You cannot lock " + appdetails.getAppname() + " without setting a General Password", Toast.LENGTH_SHORT).show();
                }
                else if(lockk.getLocked().matches("false")){
                   // notifyItemChanged(indexx);
                    myViewHolder.listlocker.setImageResource(R.drawable.locked);

                    helper.update(appdetails.getPackagename(),pass , "true", "false");
                    Toast.makeText(mContext, appdetails.getAppname()+" Has Been Locked", Toast.LENGTH_SHORT).show();
                }else{
                  //  notifyItemChanged(indexx);
                    myViewHolder.listlocker.setImageResource(R.drawable.open);

                    helper.update(appdetails.getPackagename(),pass , "false", "false");
                    Toast.makeText(mContext, appdetails.getAppname()+" Has Been Unlocked", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
    public String getBiometric_word(Context context) {

        BiometricManager biometricManager = BiometricManager.from(context);
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
    public String[] types(Context context){
        BiometricManager biometricManager=BiometricManager.from(context);
        int Valid=biometricManager.canAuthenticate();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Valid==BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE){
            String[] types = {"Use General Password","Password", "PIN", "Pattern"};
            return types;
        }else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && Valid==BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE){
            String[] types = {"Use General Password","Password", "PIN", "Pattern"};
            return types;

        }else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && Valid==BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED){
            String[] types = {"Use General Password","Password", "PIN", "Pattern", "Biometric/Fingerprint"};
            return types;

        }
        else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && Valid==BiometricManager.BIOMETRIC_SUCCESS){
            String[] types = {"Use General Password","Password", "PIN", "Pattern", "Biometric/Fingerprint"};
            return types;

        }else{
            String[] types = {"Use General Password","Password", "PIN", "Pattern"};
            return types;

        }

    }

    @Override
    public int getItemCount() {
        return applist.size();
    }

}
