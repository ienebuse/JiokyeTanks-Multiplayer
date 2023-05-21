package com.jiokye.tankbattle_multiplayer.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;

import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.utility.AppManager;
import com.jiokye.tankbattle_multiplayer.utility.SettingsManager;

public class TankIntroActivity extends AppCompatActivity implements AppManager.OnAppManagerSignal {
    CountDownTimer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_tank_intro);

        AppManager.setAppManagerListener(this);

        loadSounds();

        timer = new CountDownTimer(2000, 500) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                openTankTypeActivity();
            }
        };
        timer.start();
    }

    private void openTankTypeActivity() {
        startActivity(new Intent(this, TankTypeActivity.class));
        finish();
    }

    @Override
    public void executeSignal() {
        timer.cancel();
        finishAndRemoveTask();
    }


    protected void onResume() {
        super.onResume();
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        AppManager.checkDebugger();
        AppManager.verifyInstaller(this);
        AppManager.verifySignature(this, AppManager.getAppString());
    }

    public void onWindowFocusChanged (boolean hasFocus) {
//        if(hasFocus) {
//            requestWindowFeature(Window.FEATURE_NO_TITLE);
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
//        }
    }


    private void loadSounds() {
        SoundManager.getInstance();
        SoundManager.initSounds(this);
        int[] sounds = {
                R.raw.tnkbackground,
                R.raw.tnkbonus,
                R.raw.tnkbrick,
                R.raw.tnkexplosion,
                R.raw.tnkfire,
                R.raw.tnkgameover,
                R.raw.tnkgamestart,
                R.raw.tnkscore,
                R.raw.tnksteel,
                R.raw.tnkpowerup,
                R.raw.tnkpause,
                R.raw.tnkearn_gold,
                R.raw.tnkbuy_item,
                R.raw.tnk1up,
                R.raw.tnkslide,
                R.raw.tnkfindgold,
                R.raw.tnkbomb,
                R.raw.tnkdropbomb,
                R.raw.tnkclick,
                R.raw.tnkclick2,
                R.raw.tnkclick3,
                R.raw.tnkconnect,

                R.raw.tnk_hve,
                R.raw.tnk_gamebkgnd,
                R.raw.tnk_fightscene1,
                R.raw.tnk_fightscene2,
                R.raw.tnk_fightscene3,
                R.raw.tnk_fightscene4,
                R.raw.tnk_fightscene5,
                R.raw.tnk_gamesound,
//                R.raw.tnk_fightscene7,

                R.raw.tnkreward,

        };
        SoundManager.loadSounds(sounds);
        SharedPreferences settings = getSharedPreferences("TankSettings", 0);
        boolean sound = settings.getBoolean(SettingsManager.PREF_MUTED,true);
        SoundManager.setSound(sound);
    }

    protected void onDestroy() {
        finishAndRemoveTask();
        super.onDestroy();
    }

}