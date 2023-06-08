package com.jiokye.tankbattle_multiplayer.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;

public class InfoDialog extends Dialog{
    CheckBox showCheck;

    public AppCompatActivity activity;
    public Dialog d;
    public Button yes, no;
//    SharedPreferences settings;

//    OnDismissWifiNotice onDismissWifiNotice;

    public InfoDialog(AppCompatActivity a) {
        super(a);
        this.activity = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        setContentView(R.layout.dialog_info);
        setCancelable(true);

        yes = findViewById(R.id.infoOk);
//        no = findViewById(R.id.cancelBtn);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundManager.playSound(Sounds.TANK.CLICK);
                int id = v.getId();
                if (id == R.id.infoOk) {
//            SharedPreferences.Editor editor = settings.edit();
//            editor.putBoolean(SettingsManager.SHOW_WIFI_NOTICE, showCheck.isChecked());
//            editor.apply();
//            onDismissWifiNotice.finish(true);
                    dismiss();
                }
            }
        });
//        no.setOnClickListener(this);
//        showCheck = findViewById(R.id.show);

//        settings = activity.getSharedPreferences("TankSettings", 0);
//
//        boolean showNotice = settings.getBoolean(SettingsManager.SHOW_WIFI_NOTICE,false);
//        showCheck.setChecked(showNotice);
//
//        SharedPreferences.Editor editor = settings.edit();
//        editor.putBoolean(SettingsManager.SHOW_WIFI_NOTICE, showNotice);
//        editor.apply();


    }

    public void onWindowFocusChanged (boolean hasFocus) {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }



//    @Override
//    public void onClick(View v) {
//        SoundManager.playSound(Sounds.TANK.CLICK);
//        int id = v.getId();
//        if (id == R.id.saveBtn) {
////            SharedPreferences.Editor editor = settings.edit();
////            editor.putBoolean(SettingsManager.SHOW_WIFI_NOTICE, showCheck.isChecked());
////            editor.apply();
////            onDismissWifiNotice.finish(true);
//            dismiss();
//        }
////        else if (id == R.id.cancelBtn) {
////            dismiss();
////        }
//    }

//    public void setOnDismissWifiNotice(OnDismissWifiNotice onDismissWifiNotice) {
//        this.onDismissWifiNotice = onDismissWifiNotice;
//    }
//
//    public interface OnDismissWifiNotice {
//        void finish(boolean ok);
//    }
}
