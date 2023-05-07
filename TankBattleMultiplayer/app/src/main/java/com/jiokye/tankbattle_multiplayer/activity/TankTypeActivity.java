package com.jiokye.tankbattle_multiplayer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
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

public class TankTypeActivity extends AppCompatActivity implements AppManager.OnAppManagerSignal, InstallStateUpdatedListener {

    ImageView classicMode, campaignMode, updateBtn;
    TextView verName;
    static final String TANK_TYPE = "TANK_TYPE";
    public static AppUpdateManager appUpdateManager;
    public static int UPDATE_REQUEST_CODE = 107;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_tank_type);


        appUpdateManager = AppUpdateManagerFactory.create(this);
        AppManager.setAppManagerListener(this);



        classicMode = findViewById(R.id.classic);
        classicMode.setOnClickListener(modeClickListener);

        campaignMode = findViewById(R.id.campaign);
        campaignMode.setOnClickListener(modeClickListener);

        updateBtn = findViewById(R.id.updatebtn);
        updateBtn.setVisibility(View.INVISIBLE);
        updateBtn.setEnabled(false);
        Utils.Effects.zoom(updateBtn,0.9f,0);

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.updatebtn) {
                    appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
                        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                                // This example applies an immediate update. To apply a flexible update
                                // instead, pass in AppUpdateType.FLEXIBLE
                                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                            // Request the update.
                            startUpdate(appUpdateInfo, AppUpdateType.FLEXIBLE);
                        }
                    });

                }
            }
        });

        verName = findViewById(R.id.verName);
        verName.setText(String.format("v%s", getVersionName()));

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
        checkUpdate();
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
        checkUpdating();
    }

    protected void onDestroy() {
        appUpdateManager.unregisterListener(this);
        super.onDestroy();
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

    private String getVersionName() {
        String versionName;
        try{
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return versionName;
    }

    private void checkUpdate() {
        // Returns an intent object that you use to check for an update.
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // This example applies an immediate update. To apply a flexible update
                    // instead, pass in AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                // Request the update.
                updateBtn.setVisibility(View.VISIBLE);
                updateBtn.setEnabled(true);
            }
        });

        appUpdateManager.registerListener(this);

    }

    private void checkUpdating() {
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(appUpdateInfo -> {

                    // If the update is downloaded but not installed,
                    // notify the user to complete the update.
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        popupSnackbarForCompleteUpdate();
                    }
                });
    }

    private void popupSnackbarForCompleteUpdate() {
        updateBtn.setEnabled(false);
        updateBtn.setVisibility(View.INVISIBLE);
        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.activity_tank_type),
                        "An update has just been downloaded.",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> {
            appUpdateManager.completeUpdate();
//            TankMenuActivity.this.finish();
//            System.exit(0);
//            restartApp();
        });
        snackbar.setActionTextColor(
                getResources().getColor(android.R.color.white));
        snackbar.show();
    }

    private void startUpdate(AppUpdateInfo info, int AppUpdateType) {

        try {
            appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    info,
                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    AppUpdateType,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    UPDATE_REQUEST_CODE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
//            Log.d("UPATE", "Update failed");
        }
    }

    @Override
    public void onStateUpdate(@NonNull InstallState installState) {
//        downloaded = true;

        if (installState.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
//                Log.d("UPDATE","Update flow failed! Result code: " + resultCode);
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
    }


}