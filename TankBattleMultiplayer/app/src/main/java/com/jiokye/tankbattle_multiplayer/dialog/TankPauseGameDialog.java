package com.jiokye.tankbattle_multiplayer.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.res.ResourcesCompat;

import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;
import com.jiokye.tankbattle_multiplayer.utility.SettingsManager;
import com.jiokye.tankbattle_multiplayer.utility.TankTextView;
import com.jiokye.tankbattle_multiplayer.utility.Utils;

public class TankPauseGameDialog extends Dialog implements View.OnClickListener{


    public Activity activity;
    public Dialog d;
//    TankView mTankView;
    ImageView soundBtn, vibrateBtn;
    boolean sound, vibrate;
    LinearLayout view;

    private int state = 0;

    public ImageView continueBtn, newGameBtn, endGameBtn;
    private TankTextView closeBtn;
    SharedPreferences settings;
    OnDialogResult onDialogResult;
    int games;


    public TankPauseGameDialog(Activity a) {
        super(a);
        this.activity = a;
//        this.mTankView = mTankView;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        setContentView(R.layout.dialog_tank_pause_game);
        setCancelable(false);

        view = findViewById(R.id.settings_dialog);

        continueBtn = findViewById(R.id.continueBtn);
        newGameBtn = findViewById(R.id.newGameBtn);
        endGameBtn = findViewById(R.id.endGameBtn);

        continueBtn.setOnClickListener(this);
        newGameBtn.setOnClickListener(this);
        endGameBtn.setOnClickListener(this);

        soundBtn = findViewById(R.id.pSoundBtn);
        vibrateBtn = findViewById(R.id.pVibrateBtn);
        soundBtn.setOnClickListener(this);
        vibrateBtn.setOnClickListener(this);

//        closeBtn = (TankTextView) findViewById(R.id.pauseCloseBtn);
//        closeBtn.setOnTouchListener(this);

        settings = activity.getSharedPreferences("TankSettings", 0);
        games = settings.getInt(SettingsManager.RETRY_COUNT,0);

        sound = settings.getBoolean(SettingsManager.PREF_MUTED,true);
        vibrate = settings.getBoolean(SettingsManager.PREF_VIBRATE,true);

        if(sound) {
            soundBtn.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.psound,null));
        }
        else {
            soundBtn.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.nsound,null));
        }

        if(vibrate) {
            vibrateBtn.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.pvibrate,null));
        }
        else {
            vibrateBtn.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.nvibrate,null));
        }

        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                ViewGroup.LayoutParams viewParam = view.getLayoutParams();
//                viewParam.height*;
//                int w = viewParam.width;
//                view.setLayoutParams(viewParam);
            }
        });
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onClick(View v) {
        SoundManager.playSound(Sounds.TANK.CLICK);
        int id = v.getId();

        Utils.Effects.blink(v,2).setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
//            if (id == R.id.continueBtn || id == R.id.pauseCloseBtn) {
                if (id == R.id.continueBtn) {
                    onDialogResult.dialogFinish(0);
                    dismiss();
                } else if (id == R.id.newGameBtn) {
                    if (games > 0) {
                        onDialogResult.dialogFinish(1);
                        dismiss();
                    }
                } else if (id == R.id.endGameBtn) {
                    onDialogResult.dialogFinish(2);
                    dismiss();
                } else if (id == R.id.pSoundBtn) {
                    if (sound) {
                        sound = false;
                        soundBtn.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.nsound, null));
                    } else {
                        sound = true;
                        soundBtn.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.psound, null));
                    }
                    onDialogResult.dialogFinish(3 + (sound?1:0));
                } else if (id == R.id.pVibrateBtn) {
                    if (vibrate) {
                        vibrate = false;
                        vibrateBtn.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.nvibrate, null));
                    } else {
                        vibrate = true;
                        vibrateBtn.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.pvibrate, null));
                    }
                    onDialogResult.dialogFinish(5 + (vibrate?1:0));
                }
            }
        });
    }

    public void setOnDialogResult(OnDialogResult dlg) {
        this.onDialogResult = dlg;
    }

    public interface OnDialogResult {
        void dialogFinish(int btnCliked);
    }
}
