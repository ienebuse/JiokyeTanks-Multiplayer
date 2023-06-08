package com.jiokye.tankbattle_multiplayer.puzzles.numberpuzzle;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;
import com.jiokye.tankbattle_multiplayer.utility.Utils;

public class NumPuzzLevelDialog extends Dialog implements View.OnClickListener{

    public AppCompatActivity activity;
    public Dialog d;
    public Button yes, no;
    SharedPreferences settings;

    ImageView numpuzz, watersort, sokoban;
    ImageView numpuzz_sel, watersort_sel, sokoban_sel;
    TextView puzzInfo, puzzSel;
    int selected = 0;

    OnDismissPuzzleNotice onDismissPuzzleNotice;

    public NumPuzzLevelDialog(AppCompatActivity a) {
        super(a);
        this.activity = a;
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

        setContentView(R.layout.dialog_puzzle_notice);
        setCancelable(false);

        yes = findViewById(R.id.okBtn);
        no = findViewById(R.id.cancelBtn);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

        numpuzz = findViewById(R.id.numpuzz);
        watersort = findViewById(R.id.watersortpuzz);
        sokoban = findViewById(R.id.sokoban);
        puzzInfo = findViewById(R.id.puzzInfo);
        puzzSel = findViewById(R.id.puzz_sel);
        puzzSel.setText(activity.getResources().getString(R.string.numpuzz_sel));
        puzzInfo.setText(activity.getResources().getString(R.string.info_numpuzz));

        numpuzz.setOnTouchListener(puzzleSelectListener);
        watersort.setOnTouchListener(puzzleSelectListener);
        sokoban.setOnTouchListener(puzzleSelectListener);

        numpuzz_sel = findViewById(R.id.numpuzz_sel);
        watersort_sel = findViewById(R.id.watersort_sel);
        sokoban_sel = findViewById(R.id.sokoban_sel);

        Utils.Effects.zoom(numpuzz_sel,0.95f,0);
        Utils.Effects.zoom(watersort_sel,0.95f,0);
        Utils.Effects.zoom(sokoban_sel,0.95f,0);
    }

    View.OnTouchListener puzzleSelectListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int id = view.getId();
            if(id == R.id.numpuzz) {
                numpuzz_sel.setBackgroundColor(Color.GREEN);
                watersort_sel.setBackgroundColor(Color.TRANSPARENT);
                sokoban_sel.setBackgroundColor(Color.TRANSPARENT);

                numpuzz_sel.setVisibility(View.VISIBLE);
                watersort_sel.setVisibility(View.INVISIBLE);
                sokoban_sel.setVisibility(View.INVISIBLE);

                puzzSel.setText(activity.getResources().getString(R.string.numpuzz_sel));
                puzzInfo.setText(activity.getResources().getString(R.string.info_numpuzz));

                selected = 0;
            }
            else if(id == R.id.watersortpuzz) {
                watersort_sel.setBackgroundColor(Color.GREEN);
                numpuzz_sel.setBackgroundColor(Color.TRANSPARENT);
                sokoban_sel.setBackgroundColor(Color.TRANSPARENT);

                watersort_sel.setVisibility(View.VISIBLE);
                numpuzz_sel.setVisibility(View.INVISIBLE);
                sokoban_sel.setVisibility(View.INVISIBLE);

                puzzSel.setText(activity.getResources().getString(R.string.watersort_sel));
                puzzInfo.setText(activity.getResources().getString(R.string.info_watersort));

                selected = 1;
            }
            else if(id == R.id.sokoban) {
                sokoban_sel.setBackgroundColor(Color.GREEN);
                watersort_sel.setBackgroundColor(Color.TRANSPARENT);
                numpuzz_sel.setBackgroundColor(Color.TRANSPARENT);

                sokoban_sel.setVisibility(View.VISIBLE);
                numpuzz_sel.setVisibility(View.INVISIBLE);
                watersort_sel.setVisibility(View.INVISIBLE);

                puzzSel.setText(activity.getResources().getString(R.string.sokoban_sel));
                puzzInfo.setText(activity.getResources().getString(R.string.info_sokoban));

                selected = 2;
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
            onDismissPuzzleNotice.finish(selected);
            dismiss();
        }
        else if (id == R.id.cancelBtn) {
            dismiss();
        }
    }

    public void setOnDismissPuzzleNotice(OnDismissPuzzleNotice onDismissPuzzleNotice) {
        this.onDismissPuzzleNotice = onDismissPuzzleNotice;
    }

    public interface OnDismissPuzzleNotice {
        void finish(int selectedPuzzle);
    }
}
