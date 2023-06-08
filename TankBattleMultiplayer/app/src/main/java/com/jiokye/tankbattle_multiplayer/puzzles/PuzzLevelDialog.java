package com.jiokye.tankbattle_multiplayer.puzzles;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;
import com.jiokye.tankbattle_multiplayer.utility.Utils;

public class PuzzLevelDialog extends Dialog implements View.OnClickListener{

    public AppCompatActivity activity;
    public Dialog d;
    public Button yes, no;
    SharedPreferences settings;

    ImageView lvl1, lvl2, lvl3, lvl4;
    ImageView lvl1_sel, lvl2_sel, lvl3_sel, lvl4_sel, puzzSel;
    TextView puzzInfo;
    int selected = 1;
    String puzz;
    Drawable puzzImg;
    Drawable selView;

    OnDismissPuzzleLevel onDismissPuzzleLevel;

    public PuzzLevelDialog(AppCompatActivity a, String puzz, Drawable puzzImg) {
        super(a);
        this.activity = a;
        this.puzz = puzz;
        this.puzzImg = puzzImg;
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

        setContentView(R.layout.dialog_puzz_level);
        setCancelable(false);

        yes = findViewById(R.id.okBtn);
        no = findViewById(R.id.cancelBtn);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

        lvl1 = findViewById(R.id.puzzlvl1);
        lvl2 = findViewById(R.id.puzzlvl2);
        lvl3 = findViewById(R.id.puzzlvl3);
        lvl4 = findViewById(R.id.puzzlvl4);

        lvl1_sel = findViewById(R.id.lvl1_sel);
        lvl2_sel = findViewById(R.id.lvl2_sel);
        lvl3_sel = findViewById(R.id.lvl3_sel);
        lvl4_sel = findViewById(R.id.lvl4_sel);



        puzzInfo = findViewById(R.id.puzz_sel);
        puzzSel = findViewById(R.id.sel_puzz_img);
        puzzInfo.setText(puzz);
        puzzSel.setBackground(puzzImg);

        lvl1.setOnTouchListener(lvlSelectListener);
        lvl2.setOnTouchListener(lvlSelectListener);
        lvl3.setOnTouchListener(lvlSelectListener);
        lvl4.setOnTouchListener(lvlSelectListener);

        selView = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.selbg, null);


        Utils.Effects.zoom(lvl1_sel,0.95f,0);
        Utils.Effects.zoom(lvl2_sel,0.95f,0);
        Utils.Effects.zoom(lvl3_sel,0.95f,0);
        Utils.Effects.zoom(lvl4_sel,0.95f,0);
    }

    View.OnTouchListener lvlSelectListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int id = view.getId();
            if(id == R.id.puzzlvl1) {
                lvl1_sel.setBackground(selView);
                lvl2_sel.setBackground(null);
                lvl3_sel.setBackground(null);
                lvl4_sel.setBackground(null);

                selected = 1;
            }
            else if(id == R.id.puzzlvl2) {
                lvl1_sel.setBackground(null);
                lvl2_sel.setBackground(selView);
                lvl3_sel.setBackground(null);
                lvl4_sel.setBackground(null);

                selected = 2;
            }
            else if(id == R.id.puzzlvl3) {
                lvl1_sel.setBackground(null);
                lvl2_sel.setBackground(null);
                lvl3_sel.setBackground(selView);
                lvl4_sel.setBackground(null);

                selected = 3;
            }
            else if(id == R.id.puzzlvl4){
                lvl1_sel.setBackground(null);
                lvl2_sel.setBackground(null);
                lvl3_sel.setBackground(null);
                lvl4_sel.setBackground(selView);
                selected = 4;
            }
            return true;
        }
    };

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
        SoundManager.playSound(Sounds.TANK.CLICK);
        int id = v.getId();
        if (id == R.id.okBtn) {
            onDismissPuzzleLevel.finish(selected);
            dismiss();
        }
        else if (id == R.id.cancelBtn) {
            dismiss();
        }
    }

    public void setOnDismissPuzzleLevel(OnDismissPuzzleLevel onDismissPuzzleLevel) {
        this.onDismissPuzzleLevel = onDismissPuzzleLevel;
    }

    public interface OnDismissPuzzleLevel {
        void finish(int selectedlevel);
    }
}
