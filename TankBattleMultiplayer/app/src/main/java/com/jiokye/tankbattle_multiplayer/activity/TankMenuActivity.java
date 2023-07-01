package com.jiokye.tankbattle_multiplayer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.InstallStatus;
import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.billing.TransactionManager;
import com.jiokye.tankbattle_multiplayer.dialog.AdDialogFragment;
import com.jiokye.tankbattle_multiplayer.dialog.InfoDialog;
import com.jiokye.tankbattle_multiplayer.dialog.PuzzleNoticeDialog;
import com.jiokye.tankbattle_multiplayer.dialog.TankDailyRewardDialog;
import com.jiokye.tankbattle_multiplayer.dialog.TankSettingsDialog;
import com.jiokye.tankbattle_multiplayer.fragments.ConstructionFragment;
import com.jiokye.tankbattle_multiplayer.fragments.StoreFragment;
import com.jiokye.tankbattle_multiplayer.fragments.TankStageFragment;
import com.jiokye.tankbattle_multiplayer.puzzles.numberpuzzle.NumberPuzzleFragment;
import com.jiokye.tankbattle_multiplayer.puzzles.sokoban.SokobanPuzzleFragment;
import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;
import com.jiokye.tankbattle_multiplayer.utility.AppManager;
import com.jiokye.tankbattle_multiplayer.utility.CONST;
import com.jiokye.tankbattle_multiplayer.utility.CheckAdd;
import com.jiokye.tankbattle_multiplayer.utility.MessageRegister;
import com.jiokye.tankbattle_multiplayer.utility.ServiceListener;
import com.jiokye.tankbattle_multiplayer.utility.SettingsManager;
import com.jiokye.tankbattle_multiplayer.utility.TankToast;
import com.jiokye.tankbattle_multiplayer.utility.Utils;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.jiokye.tankbattle_multiplayer.wifidirect.WifiNoticeDialog;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class TankMenuActivity extends AppCompatActivity implements ServiceListener, AppManager.OnAppManagerSignal, InstallStateUpdatedListener {


    static boolean downloaded = false;
    static Intent intent = null;
    boolean coop = false;
    ImageView p1Btn, p2Btn, p1p2Btn, cnstBtn, setBtn, infoBtn;
    Button backBtn, rateBtn;
    TextView goldTxt, retryTxt, retryTmr, adCoinTxt;
    ImageView retryImg, adcoincountImg, puzzleImg;
    private  static String tankType;
    boolean firstTime = true;

    int clickCount;

    SharedPreferences settings;
    private boolean opened = false;


    public static final String TWO_PLAYERS = "two players";
    public static final String COOP = "coop";
    public static final String
            PREF_MUTED = "muted",
            PREF_VIBRATE = "vibrate",
            PREF_LEVEL = "level",

    STAGE_NAMES = "STAGE_NAMES";
    boolean p2, notice;

    private AdView bannerAdView;

    private RewardedAd mRewardedAd;
    private final String RAD_UNIT_ID = AppManager.getAppString(CONST.Tank.TankMenuActivity_RAD); //"ca-app-pub-3940256099942544/5224354917";
    boolean isLoading;
    public static boolean GOT_REWARD = false;

    private RewardedInterstitialAd mRewardedInterstitialAd;
    private final String RIAD_UNIT_ID = AppManager.getAppString(CONST.Tank.TankMenuActivity_RIAD); //"ca-app-pub-3940256099942544/5354046379";
    boolean isLoadingIntAds;
    public static boolean GOT_IREWARD = false;

    private InterstitialAd interstitialAd;
    private final String IAD_UNIT_ID = AppManager.getAppString(CONST.Tank.TankMenuActivity_IAD); //"ca-app-pub-3940256099942544/1033173712";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_tank_menu);
        AppManager.setAppManagerListener(this);
        TransactionManager.getInstnce().billingSetup(this);
        settings = getSharedPreferences("TankSettings", 0);

//        bannerAdView = findViewById(R.id.adView);

//        bannerAdView.setAdSize(AdSize.BANNER);
//        bannerAdView.setAdUnitId(AppManager.getAppString(CONST.Tank.TankMenuActivity_BAD));


        loadRewardedInterstitialAd();
        loadInterstitialAD();

        AppManager.checkDebugger();
        AppManager.verifyInstaller(this);
        AppManager.verifySignature(this, AppManager.getAppString());

        if(intent == null) {
            intent = getIntent();
        }


        if(tankType == null) {
            Bundle bundle = intent.getExtras();
            tankType = bundle.getString(TankTypeActivity.TANK_TYPE, "CLASSIC");
        }
        if(tankType.equals("CAMPAIGN")) {
            findViewById(R.id.menu_header).setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.campaignheader,null));
        }
        else{
            findViewById(R.id.menu_header).setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.classicheader,null));
        }

        backBtn = findViewById(R.id.backbtn);
        rateBtn = findViewById(R.id.ratebtn);
        p1Btn = findViewById(R.id.p1btn);
        p2Btn = findViewById(R.id.p2btn);
        p1p2Btn = findViewById(R.id.p1p2btn);
        cnstBtn = findViewById(R.id.cnstbtn);
        setBtn = findViewById(R.id.setbtn);
        infoBtn = findViewById(R.id.infobtn);

        backBtn.setOnClickListener(buttonListener);
        rateBtn.setOnClickListener(buttonListener);
        p1Btn.setOnClickListener(buttonListener);
        p2Btn.setOnClickListener(buttonListener);
        p1p2Btn.setOnClickListener(buttonListener);
        cnstBtn.setOnClickListener(buttonListener);
        setBtn.setOnClickListener(buttonListener);
        infoBtn.setOnClickListener(buttonListener);

//        findViewById(R.id.buygoldImg).setOnClickListener(storeListener);
//        findViewById(R.id.buygameImg).setOnClickListener(storeListener);
        findViewById(R.id.storeBtn).setOnClickListener(storeListener);

        goldTxt = findViewById(R.id.goldcountTxt);
        retryTxt = findViewById(R.id.gamecountTxt);
//        retryTmr = findViewById(R.id.menuRetryTmrTxt);
        adCoinTxt = findViewById(R.id.adcoincountText);
        retryImg = findViewById(R.id.gamecountImg);
        retryTmr = findViewById(R.id.menuRetryTmrTxt);
        adcoincountImg = findViewById(R.id.adcoincountImg);
        puzzleImg = findViewById(R.id.puzzle);
        Utils.Effects.zoom(adcoincountImg,0.9f,0);
        Utils.Effects.zoom(infoBtn,0.9f,0);
        Utils.Effects.zoom(puzzleImg,0.9f,0);

        adcoincountImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                p2 = false;
                notice = false;
                showRewardedInterstitialAd(false);
            }
        });

        puzzleImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPuzzleNotice();
            }
        });

//        retryImg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ++clickCount;
//                if(clickCount == 20) {
//                    TankToast.showTankToast(TankMenuActivity.this, AppManager.getSignature(TankMenuActivity.this));
//                    clickCount = 0;
//                }
//            }
//        });


        MessageRegister.getInstance().setServiceListener(this);

        updateStore();
        enableButtons();

        if(firstTime && isNewDay()) {
            showDailyReward();
        }

        TankTypeActivity.appUpdateManager.registerListener(this);
    }

    View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            Animation animation;
            if(id == R.id.backbtn || id == R.id.ratebtn|| id == R.id.infobtn || id == R.id.updatebtn) {
                SoundManager.playSound(Sounds.TANK.CLICK);
                animation = Utils.Effects.blink(view, 2);
            }
            else {
                SoundManager.playSound(Sounds.TANK.CLICK3);
                disableButtonsExcept(id);
                animation = Utils.Effects.blink(view, 10);
            }

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    p2 = false;
                    notice = false;
                    if(id == R.id.infobtn) {
                        openInfo();
                    }
                    else if(id == R.id.ratebtn) {
                        rateApp();
                    }
                    else if(id == R.id.backbtn) {

                        Intent i = new Intent(TankMenuActivity.this, TankTypeActivity.class);
                        startActivity(i);
                        finish();
                    }
                    else if(id == R.id.p1btn) {
                        coop = false;
                        openStages(false, false);
                    }
                    else if(id == R.id.p2btn || id == R.id.p1p2btn) {
                        coop = id == R.id.p2btn;
                        if(CheckAdd.getInstance().transition(0.5f) && showInterstitial()) {
                            p2 = true;
                            notice = true;
                        }
                        else {
                            if(!settings.getBoolean(SettingsManager.SHOW_WIFI_NOTICE,false)) {
                                openWifiNotice();
                            }
                            else {
                                openStages(true, coop);
                            }
                        }
                    }
                    else if(id == R.id.cnstbtn) {
                        openConstruction();
                    }
                    else if(id == R.id.setbtn) {
                        openSettings();
                    }
                    enableButtons();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        }
    };

    @Override
    public void executeSignal() {
        finishAndRemoveTask();
    }


    public boolean isNewDay() {

        boolean newDay = false;
        int numDays = 0;
        boolean first_time = settings.getBoolean(SettingsManager.FIRST_TIME,true);
        long lastDay = settings.getLong(SettingsManager.LAST_DAY, 0) / 86400000;
        long currentDay = System.currentTimeMillis() / 86400000;

        if(currentDay > lastDay) {
            numDays = settings.getInt(SettingsManager.CONSECUTIVE_DAYS,0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong(SettingsManager.LAST_DAY,System.currentTimeMillis());
            editor.putBoolean(SettingsManager.FIRST_TIME,false);

            if(!first_time) {
                newDay = true;
                numDays = (numDays % 12) + 1;
            }

            editor.putInt(SettingsManager.CONSECUTIVE_DAYS,numDays);
            editor.commit();
            firstTime = false;
        }

        return newDay;
    }


    public void rateApp() {
        try
        {
            Intent rateIntent = rateIntentForUrl("market://details");
            startActivity(rateIntent);
        }
        catch (ActivityNotFoundException e)
        {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
            startActivity(rateIntent);
        }
    }


    private Intent rateIntentForUrl(String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21)
        {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        }
        else
        {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }

    View.OnClickListener storeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SoundManager.playSound(Sounds.TANK.CLICK);
            StoreFragment sf = Utils.Store.openStore(TankMenuActivity.this,R.id.fragmentFrame);
            sf.setStoreListener(new StoreFragment.OnReturnFromStore() {
                @Override
                public void updateFromStore() {
                    updateStore();
                }
            });
        }
    };


    public void onServiceMessageReceived(int games, long time_left, boolean h6) {
        if(opened){
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            retryTxt.setText(String.valueOf(games));

            //game is at maximum, no need to show time and no need to show 6h if this is not a 6h message
            if(games >= CONST.Tank.MAX_GAME_COUNT && !h6) {
//                Log.d("SERVICE MESSAGE MENU", String.valueOf(games) + " " + time_left + " false");
                retryTmr.setText("");
                retryImg.setBackground(ResourcesCompat.getDrawable(this.getResources(),R.drawable.game,null));
            }
            //this is a 6h message
            else if(h6) {
//                Log.d("SERVICE MESSAGE MENU", String.valueOf(games) + " " + time_left + " true");
                sdf = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                retryTmr.setText(sdf.format(time_left));
                retryImg.setBackground(ResourcesCompat.getDrawable(this.getResources(),R.drawable.game6h,null));
            }
            // this is not a 6h message and game is less than maximum
            else {
//                Log.d("SERVICE MESSAGE MENU", String.valueOf(games) + " " + time_left + " false");
                retryTmr.setText(sdf.format(time_left));
                retryImg.setBackground(ResourcesCompat.getDrawable(this.getResources(),R.drawable.game,null));
            }
        }
    }



    private void disableButtonsExcept(int id) {
        if(id != p1Btn.getId()) {
            p1Btn.setEnabled(false);
        }
        if(id != p2Btn.getId()) {
            p2Btn.setEnabled(false);
        }
        if(id != setBtn.getId()) {
            setBtn.setEnabled(false);
        }
        if(id != cnstBtn.getId()) {
            cnstBtn.setEnabled(false);
        }
//        if(id != infoBtn.getId()) {
//            infoBtn.setEnabled(false);
//        }
    }

    public void enableButtons() {
        p1Btn.setEnabled(true);
        p2Btn.setEnabled(true);
        setBtn.setEnabled(true);
        cnstBtn.setEnabled(true);
//        infoBtn.setEnabled(true);
    }

    private void checkUpdating() {
        TankTypeActivity.appUpdateManager
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
        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.activity_tank_menu),
                        "An update has just been downloaded.",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> {
            TankTypeActivity.appUpdateManager.completeUpdate();
//            TankMenuActivity.this.finish();
//            System.exit(0);
//            restartApp();
        });
        snackbar.setActionTextColor(
                getResources().getColor(android.R.color.white));
        snackbar.show();
    }

    @Override
    public void onStateUpdate(@NonNull InstallState installState) {
        if (installState.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate();
        }
    }

    public void restartApp() {
        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

    protected void onResume() {
        super.onResume();
        clickCount = 0;
        SoundManager.stopGameSounds();
        SoundManager.playSound(Sounds.TANK.GAME_SOUND, true);
//        AdRequest request = new AdRequest.Builder().build();
//        bannerAdView.loadAd(request);
        enableButtons();
        opened = true;
        updateStore();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        intent = getIntent();
        if(tankType.equals("CAMPAIGN")) {
            findViewById(R.id.menu_header).setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.campaignheader,null));
        }
        else{
            findViewById(R.id.menu_header).setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.classicheader,null));
        }

        checkUpdating();
    }

    protected void onPause() {
        super.onPause();
        SoundManager.stopGameSounds();
    }

    @Override
    protected void onDestroy() {
        opened = false;
        TankTypeActivity.appUpdateManager.unregisterListener(this);
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


    public void startGame(boolean twoPlayers, boolean coop) {
        SoundManager.stopSound(Sounds.TANK.GAME_BACKGROUND);
        Intent i = new Intent(this, TankActivity.class);
        i.putExtra(TankMenuActivity.TWO_PLAYERS, twoPlayers);
        i.putExtra(TankMenuActivity.COOP, coop);
        startActivity(i);
        finish();
    }



    private void openStages(boolean twoPlayers, boolean coop) {
        Log.d("Stage Fragment", "Opening fragment");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentFrame,new TankStageFragment(this, twoPlayers, coop));
        fragmentTransaction.addToBackStack("cFragment");
        fragmentTransaction.commit();
    }

    public void openConstruction() {
        Log.d("Construction", "Opening fragment");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentFrame,new ConstructionFragment(TankMenuActivity.this));
        fragmentTransaction.addToBackStack("cFragment");
        fragmentTransaction.commit();
    }

    public void openSettings() {

        TankSettingsDialog wd = new TankSettingsDialog(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.copyFrom(wd.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.dimAmount = 0.8f;
        wd.show();
        wd.getWindow().setAttributes(lp);
        wd.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }


    public void openWifiNotice() {

        WifiNoticeDialog wd = new WifiNoticeDialog(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.copyFrom(wd.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.dimAmount = 0.8f;
        wd.show();
        wd.getWindow().setAttributes(lp);
        wd.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        wd.setOnDismissWifiNotice(new WifiNoticeDialog.OnDismissWifiNotice() {
            @Override
            public void finish(boolean ok) {
                if(ok) {
                    openStages(true, coop);
                }
            }
        });
    }

    void openInfo() {
        InfoDialog wd = new InfoDialog(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.copyFrom(wd.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.dimAmount = 0.8f;
        wd.show();
        wd.getWindow().setAttributes(lp);
        wd.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    public void openPuzzleNotice() {

        PuzzleNoticeDialog wd = new PuzzleNoticeDialog(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.copyFrom(wd.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.dimAmount = 0.8f;
        wd.show();
        wd.getWindow().setAttributes(lp);
        wd.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        wd.setOnDismissPuzzleNotice(new PuzzleNoticeDialog.OnDismissPuzzleNotice() {
            @Override
            public void finish(int selectedPuzzle) {
                openPuzzleLevel(selectedPuzzle);
            }
        });
    }

    void openPuzzleLevel(int selectedPuzzle) {
        openPuzzles(selectedPuzzle, 1);
        return;
//        String puzzInfo = null;
//        Drawable puzzImg = null;
//        switch(selectedPuzzle) {
//            case 0:
//                puzzInfo = "Number Slide";
//                puzzImg = ResourcesCompat.getDrawable(getResources(),R.drawable.numpuzicon,null);
//                break;
//            case 1:
//                puzzInfo = "Water Sort";
//                puzzImg = ResourcesCompat.getDrawable(getResources(),R.drawable.watersorticon,null);
//                break;
//            case 2:
//                puzzInfo = "Box Sort";
//                puzzImg = ResourcesCompat.getDrawable(getResources(),R.drawable.sokobanicon,null);
//                break;
//        }
//
//        if(puzzInfo != null && puzzImg != null) {
//            PuzzLevelDialog wd = new PuzzLevelDialog(this, puzzInfo, puzzImg);
//            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//
//            lp.copyFrom(wd.getWindow().getAttributes());
//            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//            lp.dimAmount = 0.8f;
//            wd.show();
//            wd.getWindow().setAttributes(lp);
//            wd.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//            wd.setOnDismissPuzzleLevel(new PuzzLevelDialog.OnDismissPuzzleLevel() {
//                @Override
//                public void finish(int level) {
//                    openPuzzles(selectedPuzzle, level);
//                }
//            });
//        }
    }

    void openPuzzles(int puzzle, int level) {

        Log.d("Construction", "Opening fragment");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(puzzle == 0) {

            fragmentTransaction.replace(R.id.fragmentFrame,new NumberPuzzleFragment(this,level));

        }
        else if(puzzle == 1) {

        }
        else if(puzzle == 2) {
            fragmentTransaction.replace(R.id.fragmentFrame,new SokobanPuzzleFragment(this,level));
        }

        fragmentTransaction.addToBackStack("cFragment");
        fragmentTransaction.commit();

    }

    void showDailyReward() {
        // Create the fragment and show it as a dialog.
        TankDailyRewardDialog dailyReward = TankDailyRewardDialog.newInstance(this);
        dailyReward.show(getSupportFragmentManager(), "dialog");
    }


    public void updateStore() {
        goldTxt.setText(String.valueOf(settings.getInt(TankActivity.GOLD,3)));
        retryTxt.setText(String.valueOf(settings.getInt(SettingsManager.RETRY_COUNT,5)));
        adCoinTxt.setText(String.valueOf(settings.getInt(SettingsManager.AD_COIN,0)));
    }


    public void loadInterstitialAD() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(
                this,
                IAD_UNIT_ID,
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        TankMenuActivity.this.interstitialAd = interstitialAd;
                        Log.i("Interstitial Ad", "onAdLoaded");
//                        Toast.makeText(TankMenuActivity.this, "onAdLoaded()", Toast.LENGTH_SHORT).show();
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        // Called when fullscreen content is dismissed.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        TankMenuActivity.this.interstitialAd = null;
                                        Log.d("TAG", "The ad was dismissed.");
                                        if(p2) {
                                            if(!settings.getBoolean(SettingsManager.SHOW_WIFI_NOTICE,false)) {
                                                openWifiNotice();
                                            }
                                            else {
                                                openStages(true, coop);
                                            }
                                        }
                                        loadInterstitialAD();
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        // Called when fullscreen content failed to show.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        TankMenuActivity.this.interstitialAd = null;
                                        Log.d("TAG", "The ad failed to show.");
                                        if(p2) {
                                            if(!settings.getBoolean(SettingsManager.SHOW_WIFI_NOTICE,false)) {
                                                openWifiNotice();
                                            }
                                            else {
                                                openStages(true, coop);
                                            }
                                        }
                                        loadInterstitialAD();
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        // Called when fullscreen content is shown.
                                        Log.d("TAG", "The ad was shown.");
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i("Interstitial Ad", loadAdError.getMessage());
                        interstitialAd = null;

                        String error =
                                String.format(
                                        "domain: %s, code: %d, message: %s",
                                        loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
//                        Toast.makeText(TankMenuActivity.this, "onAdFailedToLoad() with error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (interstitialAd != null) {
            interstitialAd.show(this);
            return true;
        } else {
//            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
            return false;
        }
    }



    public void loadRewardedInterstitialAd() {
        if (mRewardedInterstitialAd == null) {
            isLoadingIntAds = true;

            AdRequest adRequest = new AdRequest.Builder().build();
            // Use the test ad unit ID to load an ad.
            RewardedInterstitialAd.load(
                    TankMenuActivity.this,
                    RIAD_UNIT_ID,
                    adRequest,
                    new RewardedInterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(RewardedInterstitialAd ad) {
                            Log.d("Rewarded InterstitialAD", "onAdLoaded");

                            mRewardedInterstitialAd = ad;
                            isLoadingIntAds = false;
//                            Toast.makeText(TankActivity.this, "onAdLoaded", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            Log.d("Rewarded InterstitialAD", "onAdFailedToLoad: " + loadAdError.getMessage());

                            // Handle the error.
                            mRewardedInterstitialAd = null;
                            isLoadingIntAds = false;
//                            Toast.makeText(TankActivity.this, "onAdFailedToLoad", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    //todo -- rework on adfunction
    private void introduceVideoAd(int rewardAmount, String rewardType) {
        AdDialogFragment dialog = AdDialogFragment.newInstance(rewardAmount, rewardType);
        dialog.setAdDialogInteractionListener(
                new AdDialogFragment.AdDialogInteractionListener() {
                    @Override
                    public void onShowAd() {
                        Log.d("Rewarded InterstitialAD", "The rewarded interstitial ad is starting.");
                        showRewardedVideoIAD();
                    }

                    @Override
                    public void onCancelAd() {
                        Log.d("Rewarded InterstitialAD", "The rewarded interstitial ad was skipped before it starts.");
                    }
                });
        dialog.show(getSupportFragmentManager(), "AdDialogFragment");
    }

    // parameter cancel determines if the user will have the option to cancel
    public void showRewardedInterstitialAd(boolean cancelOpt) {
        if (mRewardedInterstitialAd != null) {
//            RewardItem rewardItem = mRewardedInterstitialAd.getRewardItem();
//            int rewardAmount = rewardItem.getAmount();
//            String rewardType = rewardItem.getType();

            if(cancelOpt) {
                introduceVideoAd(Integer.parseInt(getResources().getString(R.string.adCoin_bonus)), "Ad coins");
            }
            else {
                showRewardedVideoIAD();
            }
        }
        else {
//            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();

        }


    }
    //
//
    private void showRewardedVideoIAD() {

        mRewardedInterstitialAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdClicked() {
                        // Called when a click is recorded for an ad.
                        GOT_IREWARD = true;
                    }

                    @Override
                    public void onAdImpression() {
                        // Called when an impression is recorded for an ad.
                        GOT_IREWARD = true;
                    }


                    /** Called when ad showed the full screen content. */
                    @Override
                    public void onAdShowedFullScreenContent() {
                        GOT_IREWARD = false;

//                        Toast.makeText(TankActivity.this, "onAdShowedFullScreenContent", Toast.LENGTH_SHORT)
//                                .show();
                    }

                    /** Called when the ad failed to show full screen content. */
                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        Log.d("Rewarded InterstitialAD", "onAdFailedToShowFullScreenContent: " + adError.getMessage());
                        GOT_IREWARD = false;
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mRewardedInterstitialAd = null;
                        if(p2) {
                            if(!settings.getBoolean(SettingsManager.SHOW_WIFI_NOTICE,false)) {
                                openWifiNotice();
                            }
                            else {
                                openStages(true, coop);
                            }
                        }
                        loadRewardedInterstitialAd();

//                        Toast.makeText(
//                                        TankActivity.this, "onAdFailedToShowFullScreenContent", Toast.LENGTH_SHORT)
//                                .show();
                    }

                    /** Called when full screen content is dismissed. */
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mRewardedInterstitialAd = null;
                        Log.d("Rewarded InterstitialAD", "onAdDismissedFullScreenContent");
//                        Toast.makeText(TankActivity.this, "onAdDismissedFullScreenContent", Toast.LENGTH_SHORT)
//                                .show();
                        // Preload the next rewarded interstitial ad.
                        loadRewardedInterstitialAd();
                        if(GOT_IREWARD) {
                            int adcoin = settings.getInt(SettingsManager.AD_COIN,0);
                            adcoin += Integer.parseInt(String.valueOf(TankMenuActivity.this.getResources().getString(R.string.adCoin_bonus)));
                            adcoin = Math.min(adcoin,CONST.Tank.MAX_ADCOIN);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putInt(SettingsManager.AD_COIN,adcoin);
                            editor.apply();
                            SoundManager.playSound(Sounds.TANK.EARN_GOLD);
                            String msg = String.format(Locale.ENGLISH, "You got %s Ad coins", TankMenuActivity.this.getResources().getString(R.string.adCoin_bonus));
//                            Toast.makeText(TankMenuActivity.this, msg, Toast.LENGTH_SHORT).show();
                            TankToast.showTankToast(TankMenuActivity.this, msg,500);
                        }

                        if(p2) {
                            openStages(true, coop);
                        }
                    }
                });

        Activity activityContext = TankMenuActivity.this;
        mRewardedInterstitialAd.show(
                activityContext,
                new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        // Handle the reward.
                        Log.d("Rewarded InterstitialAD", "The user earned the reward.");
//                        addCoins(rewardItem.getAmount());
                        GOT_IREWARD = true;
                    }
                });
    }



    public void loadRewardedAd() {
        if (mRewardedAd == null) {
            isLoading = true;
            GOT_REWARD = false;
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(
                    this,
                    RAD_UNIT_ID,
                    adRequest,
                    new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            Log.d("Rewarded Ads", loadAdError.getMessage());
                            mRewardedAd = null;
                            TankMenuActivity.this.isLoading = false;
//                            Toast.makeText(TankMenuActivity.this, "onAdFailedToLoad", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            TankMenuActivity.this.mRewardedAd = rewardedAd;
                            Log.d("Rewarded Ads", "onAdLoaded");
                            TankMenuActivity.this.isLoading = false;
//                            Toast.makeText(TankMenuActivity.this, "onAdLoaded", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    public boolean showRewardedVideo() {

        if (mRewardedAd == null) {
            Log.d("TAG", "The rewarded ad wasn't ready yet.");
            return false;
        }

        mRewardedAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d("Rewarded Ads", "onAdShowedFullScreenContent");
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
                        }
                        TankMenuActivity.this.loadRewardedAd();
                    }
                });
        Activity activityContext = TankMenuActivity.this;
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