package com.jiokye.tankbattle_multiplayer.utility;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.activity.TankMenuActivity;

import java.util.Locale;

public class TankToast {
    static int viewID = R.layout.toast_tank;

    static public void showTankToast(Activity activity, String message) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View toastLayout = inflater.inflate(viewID,null);
        ((TextView)toastLayout.findViewById(R.id.toastMessage)).setText(message);

        Toast toast = new Toast(activity.getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastLayout);
        toast.show();
    }

    static public void showTankToast(Activity activity, String message, int delayMillis) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View toastLayout = inflater.inflate(viewID,null);
        ((TextView)toastLayout.findViewById(R.id.toastMessage)).setText(message);

        Toast toast = new Toast(activity.getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastLayout);


        new CountDownTimer(delayMillis,delayMillis) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toast.show();
                    }
                });
            }
        }.start();
    }
}
