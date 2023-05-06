package com.jiokye.tankbattle_multiplayer.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;
import com.jiokye.tankbattle_multiplayer.utility.AppManager;
import com.jiokye.tankbattle_multiplayer.utility.CONST;
import com.jiokye.tankbattle_multiplayer.utility.SettingsManager;
import com.jiokye.tankbattle_multiplayer.utility.TankToast;
import com.jiokye.tankbattle_multiplayer.utility.TimerBroadcastService;
import com.jiokye.tankbattle_multiplayer.utility.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class TankTypeActivity extends AppCompatActivity implements AppManager.OnAppManagerSignal {

    ImageView classicMode, campaignMode;
    static final String TANK_TYPE = "TANK_TYPE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_tank_type);

        AppManager.setAppManagerListener(this);



        classicMode = findViewById(R.id.classic);
        classicMode.setOnClickListener(modeClickListener);

        campaignMode = findViewById(R.id.campaign);
        campaignMode.setOnClickListener(modeClickListener);

        TimerBroadcastService.settings = getSharedPreferences("TankSettings", 0);

        long game6h = TimerBroadcastService.settings.getLong(SettingsManager.LIFE_TIME_6H,0);
        if(game6h == 0) {
            SharedPreferences.Editor editor = TimerBroadcastService.settings.edit();
            editor.putLong(SettingsManager.LIFE_TIME_6H,game6h);
            editor.commit();
        }


        int games = TimerBroadcastService.settings.getInt(SettingsManager.RETRY_COUNT, CONST.Tank.MAX_GAME_COUNT);
        if(games == CONST.Tank.MAX_GAME_COUNT) {
            SharedPreferences.Editor editor = TimerBroadcastService.settings.edit();
            editor.putInt(SettingsManager.RETRY_COUNT,games);
            editor.commit();
        }


        long life_time = TimerBroadcastService.settings.getLong(SettingsManager.LIFE_TIME,0);
        if(life_time == 0) {
            SharedPreferences.Editor editor = TimerBroadcastService.settings.edit();
            editor.putLong(SettingsManager.LIFE_TIME,life_time);
            editor.commit();
        }
        else {
            long current_time = System.currentTimeMillis();
            long time_passed = current_time - life_time;
            int added_games = (int)(time_passed/(CONST.Tank.LIFE_DURATION_MINS*60000));
            games += added_games;
            if(games > CONST.Tank.MAX_GAME_COUNT) {
                games = CONST.Tank.MAX_GAME_COUNT;
            }
            SharedPreferences.Editor editor = TimerBroadcastService.settings.edit();
            editor.putInt(SettingsManager.RETRY_COUNT,games);
            editor.putLong(SettingsManager.LIFE_TIME, (long) added_games * CONST.Tank.LIFE_DURATION_MINS*60000 + life_time);
            editor.commit();
        }



        startService(new Intent(TankTypeActivity.this, TimerBroadcastService.class));
        campaignMode.setEnabled(false);
    }


    View.OnClickListener modeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SoundManager.playSound(Sounds.TANK.CLICK3);
            int id = view.getId();
            Animation animation = Utils.Effects.blink(view, 10);
            Intent i = new Intent(TankTypeActivity.this, TankMenuActivity.class);
            if(id == R.id.campaign) {
                classicMode.setEnabled(false);
                i.putExtra(TankTypeActivity.TANK_TYPE, "CAMPAIGN");
            }
            else {
                campaignMode.setEnabled(false);
                i.putExtra(TankTypeActivity.TANK_TYPE, "CLASSIC");
            }

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    startActivity(i);
                    finish();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

        }
    };

    @Override
    public void executeSignal() {
        finishAndRemoveTask();
    }

    protected void onPause() {
        super.onPause();
        SoundManager.stopGameSounds();

    }

    protected void onResume() {
        super.onResume();
        SoundManager.stopGameSounds();
        SoundManager.playSound(Sounds.TANK.GAME_SOUND, true);
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
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }


}