package com.jiokye.tankbattle_multiplayer.dialog.puzzles;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;

public class PuzzCompleteDialog extends Dialog implements View.OnClickListener{

    public Activity activity;
    public Dialog d;
    public Button yes;
    ImageView rwdImg;
    Drawable rwdDrawable;
    SharedPreferences settings;

    OnDismissPuzzleCompleteDialog onDismissPuzzleCompleteDialog;

    public PuzzCompleteDialog(Activity a, Drawable rwdDrawable) {
        super(a);
        this.activity = a;
        this.rwdDrawable = rwdDrawable;
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

        setContentView(R.layout.dialog_puzz_complete);
        setCancelable(false);

        rwdImg = findViewById(R.id.rwdImg);
        rwdImg.setBackground(rwdDrawable);

        yes = findViewById(R.id.okBtn);
        yes.setOnClickListener(this);
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
        SoundManager.playSound(Sounds.TANK.CLICK);
        int id = v.getId();
        if (id == R.id.okBtn) {
            onDismissPuzzleCompleteDialog.finish(true);
            dismiss();
        }
    }

    public void setOnDismissPuzzleCompleteDialog(OnDismissPuzzleCompleteDialog onDismissPuzzleCompleteDialog) {
        this.onDismissPuzzleCompleteDialog = onDismissPuzzleCompleteDialog;
    }

    public interface OnDismissPuzzleCompleteDialog {
        void finish(boolean ok);
    }
}
