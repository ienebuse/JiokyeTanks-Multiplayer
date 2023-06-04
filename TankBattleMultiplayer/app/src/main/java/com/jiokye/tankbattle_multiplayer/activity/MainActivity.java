package com.jiokye.tankbattle_multiplayer.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.utility.AppManager;
import com.jiokye.tankbattle_multiplayer.utility.TankToast;
import com.jiokye.tankbattle_multiplayer.utility.Utils;

public class MainActivity extends AppCompatActivity implements AppManager.OnAppManagerSignal {

    ImageView logo;
    CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
//        splashScreen.setKeepOnScreenCondition(() -> false );
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        AppManager.setAppManagerListener(this);

        logo = findViewById(R.id.logo);

        Utils.Effects.zoom(logo,0.7f,1);

        timer = new CountDownTimer(5000, 500) {
            public void onTick(long millisUntilFinished) {}

            public void onFinish() {
                openIntroActivity();
            }
        };
        timer.start();

    }

    private void openIntroActivity() {
        startActivity(new Intent(MainActivity.this, TankIntroActivity.class));
        finish();
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

    @Override
    public void executeSignal() {
        timer.cancel();
        finishAndRemoveTask();
    }


    public void onRemoveFromRecents(View view) {
        // The document is no longer needed; remove its task.
        finishAndRemoveTask();
    }

    protected void onDestroy() {
        finishAndRemoveTask();
        super.onDestroy();
    }


}