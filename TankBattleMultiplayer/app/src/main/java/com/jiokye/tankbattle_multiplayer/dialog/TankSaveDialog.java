package com.jiokye.tankbattle_multiplayer.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;
import com.jiokye.tankbattle_multiplayer.utility.SettingsManager;
import com.google.gson.Gson;

import java.util.ArrayList;

public class TankSaveDialog extends Dialog implements View.OnClickListener{

    public AppCompatActivity activity;
    public Dialog d;
    public Button saveButton, no;
    SharedPreferences settings;
    ArrayList<String> savedNames;
    char[][] stage;
    EditText saveTextField;
    TextView saveNotify;
    private boolean overwrite = false;

    public TankSaveDialog(AppCompatActivity a, ArrayList<String> savedNames, char[][] stage) {
        super(a);
        this.activity = a;
        this.savedNames = savedNames;
        this.stage = stage;
    }

    @SuppressLint("ClickableViewAccessibility")
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

        setContentView(R.layout.dialog_tank_save);
        setCancelable(false);

        saveButton = (Button) findViewById(R.id.saveBtn);
        no = (Button) findViewById(R.id.cancelBtn);
        saveButton.setOnClickListener(this);
        no.setOnClickListener(this);

        saveTextField = findViewById(R.id.saveName);

        saveTextField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                    saveButton.setText("SAVE");
                    saveButton.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.savestage2,null));
                    overwrite = false;
                }
                return false;
            }
        });

        saveNotify = findViewById(R.id.saveNotify);
        settings = activity.getSharedPreferences("TankSettings", 0);
        overwrite = false;
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.saveBtn) {

            String saveName = saveTextField.getText().toString();
            if(saveName.equals("")) {
                SoundManager.playSound(Sounds.TANK.CLICK2);
                saveNotify.setText("Name cannot be empty!");
                return;
            }

            if(savedNames.contains(saveName) && overwrite == true) {
//                saveStageNames(savedNames);
                SoundManager.playSound(Sounds.TANK.CLICK);
                Log.d("SAVING", "Agreed to overwrite");
                saveStage(saveName,stage);
                dismiss();
            }

            else if(savedNames.contains(saveName)) {
                SoundManager.playSound(Sounds.TANK.CLICK);
                saveNotify.setText("Name already exists");
//                saveButton.setText("OVERWRITE");
                saveButton.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.overwritestage,null));
                overwrite = true;
            }

            else if(!savedNames.contains(saveName)) {
                SoundManager.playSound(Sounds.TANK.CLICK);
                savedNames.add(saveName);
                saveStageNames(savedNames);
                saveStage(saveName,stage);
                dismiss();
            }



        }
        else if (id == R.id.cancelBtn) {
            SoundManager.playSound(Sounds.TANK.CLICK);
            saveButton.setText("SAVE");
            dismiss();
        }
    }

    private void saveStage(String name, char[][] stage) {
        SharedPreferences.Editor editor = settings.edit();
        Gson gson = new Gson();
        String json = gson.toJson(stage);
        editor.putString(name,json);
        editor.apply();
    }

    private void saveStageNames(ArrayList<String> stageNames) {
        SharedPreferences.Editor editor = settings.edit();
        Gson gson = new Gson();
        String json = gson.toJson(stageNames);
        editor.putString(SettingsManager.STAGE_NAMES,json);
        editor.apply();
    }
}
