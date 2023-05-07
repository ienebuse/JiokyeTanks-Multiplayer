package com.jiokye.tankbattle_multiplayer.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.jiokye.tankbattle_multiplayer.activity.TankActivity;
import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;
import com.jiokye.tankbattle_multiplayer.tank.TankView;
import com.jiokye.tankbattle_multiplayer.utility.AppManager;
import com.jiokye.tankbattle_multiplayer.utility.CONST;
import com.jiokye.tankbattle_multiplayer.utility.TankTextView;
import com.jiokye.tankbattle_multiplayer.R;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.jiokye.tankbattle_multiplayer.utility.TankToast;

import java.util.Locale;

public class TankEndGameDialog extends Dialog implements View.OnTouchListener{


    public AppCompatActivity activity;
    public Dialog d;
    TankView mTankView;

    public Button videoBtn2;
    public ImageView videoBtn, goldBtn, closeBtn;
//    public ImageView  goldBtn2;
    private TextView timerTxt, goldCountTxt;
    SharedPreferences settings;
    int tanks, golds;
    CountDownTimer cdt;
    TextView dialogTmr;

    OnDialogResult mDialogResult;

    private RewardedAd mRewardedAd;
    private static final String RAD_UNIT_ID = AppManager.getAppString(CONST.Tank.TankEndGame_RAD); //"ca-app-pub-3940256099942544/5224354917";
    boolean isLoading;
    public static boolean GOT_REWARD = false;


    public TankEndGameDialog(AppCompatActivity a, TankView mTankView) {
        super(a);
        this.activity = a;
        this.mTankView = mTankView;
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

        setContentView(R.layout.dialog_tank_end_game);
        setCancelable(false);

        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        loadRewardedAd();

        videoBtn = findViewById(R.id.videoBtn);
        goldBtn = findViewById(R.id.goldBtn);
//        videoBtn2 = (Button) findViewById(R.id.videoBtn2);
//        goldBtn2 = (ImageView) findViewById(R.id.goldBtn2);
        videoBtn.setOnTouchListener(this);
        goldBtn.setOnTouchListener(this);
//        videoBtn2.setOnTouchListener(this);
//        goldBtn2.setOnTouchListener(this);

        closeBtn = findViewById(R.id.closeBtn);
        closeBtn.setOnTouchListener(this);

        goldCountTxt = findViewById(R.id.retryGoldCnt);

        dialogTmr = findViewById(R.id.dialogTmr);

//        timerTxt = (TankTextView)findViewById(R.id.timer);

        settings = activity.getSharedPreferences("TankSettings", 0);
        golds = settings.getInt(TankActivity.GOLD,0);
        goldCountTxt.setText(String.format(Locale.ENGLISH,"x%d",golds));
//        if(tanks <= 0) {
//            goldBtn.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.shop,null));
//        }

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if(TankView.CHECKING_RETRY != 2 && TankView.CHECKING_RETRY != 3) {
                    TankView.CHECKING_RETRY = 4;
                }
                mTankView.resumeNoAds();
            }
        });

        cdt = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                dialogTmr.setText(String.valueOf(millisUntilFinished/1000));
            }

            @Override
            public void onFinish() {
                dismiss();
            }

        };
        cdt.start();
    }

    public TankView getTankView() {
        return this.mTankView;
    }

    public void setGoldCount() {
        golds = settings.getInt(TankActivity.GOLD,0);
        goldCountTxt.setText(String.format(Locale.US,"x%d",golds));
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent m) {

        if(m.getAction() == MotionEvent.ACTION_DOWN){
            int id = v.getId();
            if (id == R.id.videoBtn) {
                dialogTmr.setText("");
                cdt.cancel();
                showRewardedVideo();
            }
            else if (id == R.id.goldBtn) {
                int cost = Integer.parseInt(activity.getResources().getString(R.string.playOnGoldCost).replace("x", ""));
                if (golds >= cost) {
                    dialogTmr.setText("");
                    cdt.cancel();
                    TankView.CHECKING_RETRY = 3;
                    String lives = activity.getResources().getString(R.string.retryGoldAmnt).replace("x", "");
                    mTankView.updateP1Lives(Integer.parseInt(lives));
                    golds = settings.getInt(TankActivity.GOLD, 0);
                    golds -= cost;
                    goldCountTxt.setText(String.format(Locale.ENGLISH, "x%d", golds));
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt(TankActivity.GOLD, golds);
                    editor.commit();
                    ((TankActivity) activity).updateBonusStack();
                    TankToast.showTankToast(activity,"You got " + lives + " more lives",500);
                    SoundManager.playSound(Sounds.TANK.BONUS1UP);
                    dismiss();
                } else {
//                    Toast toast = Toast.makeText(TankEndGameDialog.this.getContext()/*activity.getApplicationContext()*/,
//                            "Not enough resource",
//                            Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
                    //todo change toast message
                    TankToast.showTankToast(activity,"Not enough gold");

//                    String text = "Not enough resource";
//                    SpannableStringBuilder toastText = new SpannableStringBuilder(text);
//                    toastText.setSpan(new RelativeSizeSpan(0.5f), 0, text.length(), 0);
//                    Toast.makeText(TankEndGameDialog.this.getContext(), toastText, Toast.LENGTH_LONG).show();

                }
            }
            else if (id == R.id.closeBtn) {
                cdt.cancel();
                dismiss();
            }
        }
        return true;
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

    public void setWifiDialogResult(OnDialogResult dialogResult) {
        mDialogResult = dialogResult;
    }

    public interface OnDialogResult {
        void openStore();
//        void playVideo()
    }

    public void loadRewardedAd() {
        if (mRewardedAd == null) {
            isLoading = true;
            GOT_REWARD = false;
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(
                    TankEndGameDialog.this.getContext(),
                    RAD_UNIT_ID,
                    adRequest,
                    new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            Log.d("Rewarded Ads", loadAdError.getMessage());
                            mRewardedAd = null;
                            TankEndGameDialog.this.isLoading = false;
//                            Toast.makeText(TankActivity.this, "onAdFailedToLoad", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            TankEndGameDialog.this.mRewardedAd = rewardedAd;
                            Log.d("Rewarded Ads", "onAdLoaded");
                            TankEndGameDialog.this.isLoading = false;
//                            Toast.makeText(TankActivity.this, "onAdLoaded", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }



    public void showRewardedVideo() {

        if (mRewardedAd == null) {
            Log.d("TAG", "The rewarded ad wasn't ready yet.");
            return;
        }

        mRewardedAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d("Rewarded Ads", "onAdShowedFullScreenContent");
//                        Toast.makeText(TankActivity.this, "onAdShowedFullScreenContent", Toast.LENGTH_SHORT)
//                                .show();
                        GOT_REWARD = false;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                        Log.d("Rewarded Ads", "onAdFailedToShowFullScreenContent");
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mRewardedAd = null;
                        GOT_REWARD = false;
//                        Toast.makeText(
//                                TankActivity.this, "onAdFailedToShowFullScreenContent", Toast.LENGTH_SHORT)
//                                .show();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mRewardedAd = null;
                        Log.d("Rewarded Ads", "onAdDismissedFullScreenContent");
//                        Toast.makeText(TankActivity.this, "onAdDismissedFullScreenContent", Toast.LENGTH_SHORT)
//                                .show();
                        // Preload the next rewarded ad.
                        if(GOT_REWARD) {
//                            SharedPreferences.Editor editor = settings.edit();
//                            int goldCount = settings.getInt(TankActivity.GOLD, 0);
//                            int amount = Integer.parseInt((TankEndGameDialog.this.getContext().getString(R.string.vidGold).replace("x", "")));
//                            int totalGold = goldCount + amount;
//                            goldcountText.setText(String.format(Locale.ENGLISH,"%d",totalGold));
//                            editor.putInt(TankActivity.GOLD, goldCount + amount);
//                            editor.apply();
                            TankView.CHECKING_RETRY = 2;
//                                mTankView.updateP1Lives(1);
                            String lives = TankEndGameDialog.this.getContext().getString(R.string.retryWatchAmnt).replace("x","");
                            mTankView.updateP1Lives(Integer.parseInt(lives));
                            TankToast.showTankToast(activity,"You got " + lives + " more lives",500);
//
//                            SoundManager.playSound(Sounds.TANK.EARN_GOLD);
                            SoundManager.playSound(Sounds.TANK.BONUS1UP);

                        }
                        TankEndGameDialog.this.loadRewardedAd();
                        dismiss();
                    }
                });

        mRewardedAd.show(
                TankEndGameDialog.this.getOwnerActivity(),
                new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        // Handle the reward.
                        Log.d("Rewarded Ads", "The user earned the reward.");
                        int rewardAmount = rewardItem.getAmount();
                        String rewardType = rewardItem.getType();
                        GOT_REWARD = true;

                    }
                });
    }
}
