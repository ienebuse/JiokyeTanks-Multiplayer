package com.jiokye.tankbattle_multiplayer.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.activity.TankActivity;
import com.jiokye.tankbattle_multiplayer.activity.TankMenuActivity;
import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;
import com.jiokye.tankbattle_multiplayer.utility.AppManager;
import com.jiokye.tankbattle_multiplayer.utility.CONST;
import com.jiokye.tankbattle_multiplayer.utility.CVTR;
import com.jiokye.tankbattle_multiplayer.utility.SettingsManager;
import com.jiokye.tankbattle_multiplayer.utility.TankToast;
import com.jiokye.tankbattle_multiplayer.utility.Utils;

import java.util.ArrayList;

public class TankDailyRewardDialog extends DialogFragment implements View.OnClickListener {

//    TankView mTankView;

    View rootView;

    public Activity activity;
    public Dialog dialog;
    public ImageView watchBtn;
    public ImageView cancelBtn;
    SharedPreferences settings;
    int goldCount;
    int day;
    private boolean doubleReward = false;

    ArrayList<ImageView> rewardCover = new ArrayList<>();

    ArrayList<RelativeLayout> reward = new ArrayList<>();

    private RewardedAd mRewardedAd;
    private static final String RAD_UNIT_ID = AppManager.getAppString(CONST.Tank.TankDailyReward_RAD); //"ca-app-pub-3940256099942544/5224354917";
    boolean isLoading;
    public static boolean GOT_REWARD = false;


    private static String TOTAL_GOLD = "TOTAL GOLD";

    public TankDailyRewardDialog(Activity a) {
//        super(a);
        this.activity = a;
    }

    public TankDailyRewardDialog(Activity a, int day) {
//        super(a);
        this.activity = a;
//        this.mTankView = null;
//        this.dialog = null;
        this.day = day;
    }

    public static TankDailyRewardDialog newInstance(Activity a) {
        return new TankDailyRewardDialog(a);
    }

    public static TankDailyRewardDialog newInstance(Activity a, int day) {
        return new TankDailyRewardDialog(a, day);
    }

//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Inflate and set the layout for the dialog.
        // Pass null as the parent view because its going in the dialog layout.

//        view.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.dialog_bg,null));
        rootView = requireActivity().getLayoutInflater().inflate(R.layout.dialog_daily_reward, null);
        builder.setView(rootView);
        setCancelable(false);

        settings = activity.getSharedPreferences("TankSettings", 0);
        day = settings.getInt(SettingsManager.CONSECUTIVE_DAYS,0);
        watchBtn = rootView.findViewById(R.id.watchRwdPlay);
        cancelBtn = rootView.findViewById(R.id.closeReward);
        watchBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        rewardCover.add(rootView.findViewById(R.id.day1));
        rewardCover.add(rootView.findViewById(R.id.day2));
        rewardCover.add(rootView.findViewById(R.id.day3));
        rewardCover.add(rootView.findViewById(R.id.day4));
        rewardCover.add(rootView.findViewById(R.id.day5));
        rewardCover.add(rootView.findViewById(R.id.day6));
        rewardCover.add(rootView.findViewById(R.id.day7));
        rewardCover.add(rootView.findViewById(R.id.day8));
        rewardCover.add(rootView.findViewById(R.id.day9));
        rewardCover.add(rootView.findViewById(R.id.day10));
        rewardCover.add(rootView.findViewById(R.id.day11));
        rewardCover.add(rootView.findViewById(R.id.day12));

//        reward.add(findViewById(R.id.day1rwd));
//        reward.add(findViewById(R.id.day2rwd));
//        reward.add(findViewById(R.id.day3rwd));
//        reward.add(findViewById(R.id.day4rwd));
//        reward.add(findViewById(R.id.day5rwd));
//        reward.add(findViewById(R.id.day6rwd));
//        reward.add(findViewById(R.id.day7rwd));

        for (int i = 1; i <= 12; i++) {
            ImageView dayView = rewardCover.get(i - 1);
            if (i == day) {

                Utils.Effects.zoom(dayView, 0.6f, 5).addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        dayView.setAlpha(0.6f);
                        (((ConstraintLayout) dayView.getParent()).getChildAt(1)).setVisibility(View.VISIBLE);
                        SoundManager.playSound(Sounds.TANK.REWARD);
                        animation.removeAllListeners();
                        animation.setDuration(0);
                        ((ValueAnimator) animation).reverse();
                        super.onAnimationEnd(animation);

                    }
                });

                break;
            }
            if (i < day) {
                dayView.setAlpha(0.6f);
                (((ConstraintLayout) dayView.getParent()).getChildAt(1)).setVisibility(View.VISIBLE);

            }
        }

        loadRewardedAd();


        Dialog dialog = builder.create();
        return dialog;
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                              Bundle savedInstanceState) {
//
//        rootView = inflater.inflate(R.layout.dialog_daily_reward, container, false);
//        setCancelable(false);
//
//        settings = activity.getSharedPreferences("TankSettings", 0);
//
//        watch = rootView.findViewById(R.id.watchBtn0);
//        watchBtn = rootView.findViewById(R.id.watchRwdBtn);
//        watchBtn2 = rootView.findViewById(R.id.watchRwdPlay);
//        cancelBtn = rootView.findViewById(R.id.closeReward);
//        watchBtn.setOnClickListener(this);
//        watchBtn2.setOnClickListener(this);
//        cancelBtn.setOnClickListener(this);
//
//        rewardCover.add(rootView.findViewById(R.id.day1));
//        rewardCover.add(rootView.findViewById(R.id.day2));
//        rewardCover.add(rootView.findViewById(R.id.day3));
//        rewardCover.add(rootView.findViewById(R.id.day4));
//        rewardCover.add(rootView.findViewById(R.id.day5));
//        rewardCover.add(rootView.findViewById(R.id.day6));
//        rewardCover.add(rootView.findViewById(R.id.day7));
//
////        reward.add(findViewById(R.id.day1rwd));
////        reward.add(findViewById(R.id.day2rwd));
////        reward.add(findViewById(R.id.day3rwd));
////        reward.add(findViewById(R.id.day4rwd));
////        reward.add(findViewById(R.id.day5rwd));
////        reward.add(findViewById(R.id.day6rwd));
////        reward.add(findViewById(R.id.day7rwd));
//
//        for(int i = 1; i <= 7; i++) {
//            if(i > day) {
//                break;
//            }
//            if(i <= day) {
//                rewardCover.get(i-1).setAlpha(0);
//                if(i == day) {
////                    reward.get(i-1).setVisibility(View.VISIBLE);
//                }
//                else{
////                    reward.get(i-1).setVisibility(View.INVISIBLE);
//                }
//            }
//            else {
//                rewardCover.get(i-1).setAlpha(0.5f);
////                reward.get(i-1).setVisibility(View.INVISIBLE);
//            }
//        }
//
////        setShowsDialog(true);
//
//        return rootView;
//    }

//    @Override
//    public void onResume() {
//        super.onResume();
//
//        View decorView = getDialog().getWindow().getDecorView();
////        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//        decorView.setSystemUiVisibility(uiOptions);
////        view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_ad, null);
//
//        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
//        params.width = ViewGroup.LayoutParams.MATCH_PARENT;//;//ViewGroup.LayoutParams.WRAP_CONTENT;
//        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
////        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
////        getDialog().getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.dialog_bg,null));
//        getDialog().getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),android.R.color.transparent,null));
//
//        //        View v = getDialog().getWindow().getDecorView();
////        v.setBackgroundResource(android.R.color.transparent);
//    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        SharedPreferences.Editor editor = settings.edit();
        int bonus;
        switch (day) {
            case 1:
                bonus = settings.getInt(TankActivity.SHIELD,3);
                bonus  = doubleReward ? bonus+2 : bonus+1;
                bonus = Math.min(bonus,CONST.Tank.MAX_ITEM);
                editor.putInt(TankActivity.SHIELD,bonus);
                break;
            case 2:
                bonus = settings.getInt(TankActivity.CLOCK,3);
                bonus  = doubleReward ? bonus+2 : bonus+1;
                bonus = Math.min(bonus,CONST.Tank.MAX_ITEM);
                editor.putInt(TankActivity.CLOCK,bonus);
                break;
            case 3:
                bonus = settings.getInt(TankActivity.GRENADE,3);
                bonus  = doubleReward ? bonus+2 : bonus+1;
                bonus = Math.min(bonus,CONST.Tank.MAX_ITEM);
                editor.putInt(TankActivity.GRENADE,bonus);

                bonus = settings.getInt(TankActivity.BOAT,3);
                bonus  = doubleReward ? bonus+2 : bonus+1;
                bonus = Math.min(bonus,CONST.Tank.MAX_ITEM);
                editor.putInt(TankActivity.BOAT,bonus);
                break;
            case 4:
                bonus = settings.getInt(TankActivity.STAR,3);
                bonus  = doubleReward ? bonus+2 : bonus+1;
                bonus = Math.min(bonus,CONST.Tank.MAX_ITEM);
                editor.putInt(TankActivity.STAR,bonus);

                bonus = settings.getInt(TankActivity.CLOCK,3);
                bonus  = doubleReward ? bonus+2 : bonus+1;
                bonus = Math.min(bonus,CONST.Tank.MAX_ITEM);
                editor.putInt(TankActivity.CLOCK,bonus);
                break;
            case 5:
                bonus = settings.getInt(TankActivity.GUN,3);
                bonus  = doubleReward ? bonus+2 : bonus+1;
                bonus = Math.min(bonus,CONST.Tank.MAX_ITEM);
                editor.putInt(TankActivity.GUN,bonus);

                bonus = settings.getInt(TankActivity.GOLD,3);
                bonus  = doubleReward ? bonus+4 : bonus+2;
                bonus = Math.min(bonus,CONST.Tank.MAX_GOLD);
                editor.putInt(TankActivity.GOLD,bonus);
                break;
            case 6:
                bonus = settings.getInt(TankActivity.TANK,3);
                bonus  = doubleReward ? bonus+2 : bonus+1;
                bonus = Math.min(bonus,CONST.Tank.MAX_ITEM);
                editor.putInt(TankActivity.TANK,bonus);

                bonus = settings.getInt(TankActivity.SHOVEL,3);
                bonus  = doubleReward ? bonus+2 : bonus+1;
                bonus = Math.min(bonus,CONST.Tank.MAX_ITEM);
                editor.putInt(TankActivity.SHOVEL,bonus);

                bonus = settings.getInt(TankActivity.GOLD,3);
                bonus  = doubleReward ? bonus+10 : bonus+5;
                bonus = Math.min(bonus,CONST.Tank.MAX_GOLD);
                editor.putInt(TankActivity.GOLD,bonus);
                break;
            case 7:
                bonus = settings.getInt(TankActivity.TANK,3);
                bonus  = doubleReward ? bonus+2 : bonus+1;
                bonus = Math.min(bonus,CONST.Tank.MAX_ITEM);
                editor.putInt(TankActivity.TANK,bonus);

                bonus = settings.getInt(TankActivity.GUN,3);
                bonus  = doubleReward ? bonus+2 : bonus+1;
                bonus = Math.min(bonus,CONST.Tank.MAX_ITEM);
                editor.putInt(TankActivity.GUN,bonus);

                bonus = settings.getInt(TankActivity.GOLD,3);
                bonus  = doubleReward ? bonus+20 : bonus+10;
                bonus = Math.min(bonus,CONST.Tank.MAX_GOLD);
                editor.putInt(TankActivity.GOLD,bonus);
                break;

            case 8:
                bonus = settings.getInt(TankActivity.GOLD,3);
                bonus  = doubleReward ? bonus+20 : bonus+10;
                bonus = Math.min(bonus,CONST.Tank.MAX_GOLD);
                editor.putInt(TankActivity.GOLD,bonus);

                bonus = settings.getInt(SettingsManager.RETRY_COUNT,3);
                bonus  = doubleReward ? bonus+2 : bonus+1;
                bonus = Math.min(bonus,CONST.Tank.MAX_GAME_COUNT);
                editor.putInt(SettingsManager.RETRY_COUNT,bonus);
                break;

            case 9:
                bonus = settings.getInt(TankActivity.GOLD,3);
                bonus  = doubleReward ? bonus+20 : bonus+10;
                bonus = Math.min(bonus,CONST.Tank.MAX_GOLD);
                editor.putInt(TankActivity.GOLD,bonus);

                bonus = settings.getInt(TankActivity.BUILDER,3);
                bonus  = doubleReward ? bonus+6 : bonus+3;
                bonus = Math.min(bonus,CONST.Tank.MAX_BUILDER);
                editor.putInt(TankActivity.BUILDER,bonus);
                break;

            case 10:
                bonus = settings.getInt(TankActivity.TANK,3);
                bonus  = doubleReward ? bonus+6 : bonus+3;
                bonus = Math.min(bonus,CONST.Tank.MAX_GOLD);
                editor.putInt(TankActivity.GOLD,bonus);

//                bonus = settings.getInt(SettingsManager.RETRY_COUNT,3);
//                bonus  = doubleReward ? bonus+2 : bonus+1;
//                bonus = Math.min(bonus,CONST.Tank.MAX_GAME_COUNT);
//                editor.putInt(SettingsManager.RETRY_COUNT,bonus);
                break;

            case 11:
                bonus = settings.getInt(TankActivity.MINE,3);
                bonus  = doubleReward ? bonus+6 : bonus+3;
                bonus = Math.min(bonus,CONST.Tank.MAX_ITEM);
                editor.putInt(TankActivity.MINE,bonus);

//                bonus = settings.getInt(SettingsManager.RETRY_COUNT,3);
//                bonus  = doubleReward ? bonus+2 : bonus+1;
//                bonus = Math.min(bonus,CONST.Tank.MAX_GAME_COUNT);
//                editor.putInt(SettingsManager.RETRY_COUNT,bonus);
                break;

            case 12:
                bonus = settings.getInt(TankActivity.MINE,3);
                bonus  = doubleReward ? bonus+6 : bonus+3;
                bonus = Math.min(bonus,CONST.Tank.MAX_ITEM);
                editor.putInt(TankActivity.MINE,bonus);

                bonus = settings.getInt(TankActivity.TANK,3);
                bonus  = doubleReward ? bonus+6 : bonus+3;
                bonus = Math.min(bonus,CONST.Tank.MAX_GOLD);
                editor.putInt(TankActivity.GOLD,bonus);

//                bonus = settings.getInt(SettingsManager.RETRY_COUNT,3);
//                bonus  = doubleReward ? bonus+2 : bonus+1;
//                bonus = Math.min(bonus,CONST.Tank.MAX_GAME_COUNT);
//                editor.putInt(SettingsManager.RETRY_COUNT,bonus);
                break;
        }
        editor.commit();

    }


    public void setDoubleReward() {
        doubleReward = true;
    }



    @Override
    public void onClick(View v) {
        {
            int id = v.getId();
            if (id == R.id.watchRwdPlay) {
                showRewardedVideo();
            }
            else if (id == R.id.closeReward) {
                dismiss();
            }
        }
    }



    public void loadRewardedAd() {
        if (mRewardedAd == null) {
            isLoading = true;
            GOT_REWARD = false;
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(
                    activity,
                    RAD_UNIT_ID,
                    adRequest,
                    new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            mRewardedAd = null;
                            isLoading = false;
//                            Toast.makeText(TankMenuActivity.this, "onAdFailedToLoad", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            mRewardedAd = rewardedAd;
                            isLoading = false;
//                            Toast.makeText(TankMenuActivity.this, "onAdLoaded", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    public boolean showRewardedVideo() {

        if (mRewardedAd == null) {
//            Log.d("TAG", "The rewarded ad wasn't ready yet.");
            loadRewardedAd();
            return false;
        }

        mRewardedAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
//                        Log.d("Rewarded Ads", "onAdShowedFullScreenContent");
//                        Toast.makeText(TankMenuActivity.this, "onAdShowedFullScreenContent", Toast.LENGTH_SHORT).show();
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
//                                TankMenuActivity.this, "onAdFailedToShowFullScreenContent", Toast.LENGTH_SHORT)
//                                .show();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mRewardedAd = null;
                        Log.d("Rewarded Ads", "onAdDismissedFullScreenContent");
//                        Toast.makeText(TankMenuActivity.this, "onAdDismissedFullScreenContent", Toast.LENGTH_SHORT)
//                                .show();
                        // Preload the next rewarded ad.
                        if(GOT_REWARD) {
                            SharedPreferences.Editor editor = settings.edit();
                            watchBtn.setAlpha(0.4f);
                            watchBtn.setOnClickListener(null);
                            doubleReward = true;
                            TankToast.showTankToast(activity,"You got double reward", 500);
                        }
                        loadRewardedAd();
                    }
                });
        Activity activityContext = activity;
        mRewardedAd.show(
                activityContext,
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
        return true;
    }
}
