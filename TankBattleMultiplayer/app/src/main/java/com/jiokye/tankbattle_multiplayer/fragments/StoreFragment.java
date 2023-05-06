package com.jiokye.tankbattle_multiplayer.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.app.Fragment;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
//import androidx.fragment.app.Fragment;
//import android.support.v4.app.Fragment;
//import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.activity.TankActivity;
import com.jiokye.tankbattle_multiplayer.billing.TransactionManager;
import com.jiokye.tankbattle_multiplayer.dialog.ExchangeDialog;
import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;
import com.jiokye.tankbattle_multiplayer.utility.AppManager;
import com.jiokye.tankbattle_multiplayer.utility.CONST;
import com.jiokye.tankbattle_multiplayer.utility.MessageRegister;
import com.jiokye.tankbattle_multiplayer.utility.SettingsManager;
import com.jiokye.tankbattle_multiplayer.utility.TankToast;
import com.jiokye.tankbattle_multiplayer.utility.TransanctionListener;
import com.jiokye.tankbattle_multiplayer.utility.Utils;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StoreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StoreFragment extends Fragment implements TransanctionListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    OnReturnFromStore onReturnFromStore;
    SharedPreferences settings;

    TextView minecountTxt,
            starcountText,
            boatcountTxt,
            clockcountText,
            shovelcountTxt,
            helmetcountText,
            guncountTxt,
            grenadecountText,
            tankcountTxt,
            gamecountTxt,
            goldcountText,
            adcoincountText;


    ImageView mineImg,
            starImg,
            boatImg,
            clockImg,
            shovelImg,
            helmetImg,
            gunImg,
            grenadeImg,
            tankImg,
            gamecountImg,
            goldImg,
            adcoinImg;


    View rootView;
    Activity activity;

    private RewardedAd mRewardedAd;
    private AdView mAdView;
    private static final String RAD_UNIT_ID = AppManager.getAppString(CONST.Tank.TankStore_RAD); //"ca-app-pub-3940256099942544/5224354917";
    boolean isLoading;
    public static boolean GOT_REWARD = false;

    public StoreFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public StoreFragment(Activity activity) {
        this.activity = activity;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StoreFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StoreFragment newInstance(String param1, String param2) {
        StoreFragment fragment = new StoreFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View decorView = getActivity().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        MessageRegister.getInstance().setTransListener(this);
        rootView = inflater.inflate(R.layout.fragment_store, container, false);
        rootView.findViewById(R.id.fragView).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        rootView.findViewById(R.id.backbtn).setOnClickListener(storeListener);
        rootView.findViewById(R.id.storefreegold).setOnClickListener(storeListener);
        rootView.findViewById(R.id.storestarboat).setOnClickListener(storeListener);
        rootView.findViewById(R.id.storeclockshovel).setOnClickListener(storeListener);
        rootView.findViewById(R.id.storehelmetgun).setOnClickListener(storeListener);
        rootView.findViewById(R.id.storegrenadetank).setOnClickListener(storeListener);
        rootView.findViewById(R.id.storemine).setOnClickListener(storeListener);
        rootView.findViewById(R.id.storegame).setOnClickListener(storeListener);
        rootView.findViewById(R.id.storegame6h).setOnClickListener(storeListener);
        rootView.findViewById(R.id.storeadgold).setOnClickListener(storeListener);
        rootView.findViewById(R.id.storebuygold50).setOnClickListener(storeListener);
        rootView.findViewById(R.id.storebuygold100).setOnClickListener(storeListener);
        rootView.findViewById(R.id.storebuygold200).setOnClickListener(storeListener);

        minecountTxt = rootView.findViewById(R.id.minecountTxt);
        starcountText = rootView.findViewById(R.id.starcountText);
        boatcountTxt = rootView.findViewById(R.id.boatcountTxt);
        clockcountText = rootView.findViewById(R.id.clockcountText);
        shovelcountTxt = rootView.findViewById(R.id.shovelcountTxt);
        helmetcountText = rootView.findViewById(R.id.helmetcountText);
        guncountTxt = rootView.findViewById(R.id.guncountTxt);
        grenadecountText = rootView.findViewById(R.id.grenadecountText);
        tankcountTxt = rootView.findViewById(R.id.tankcountTxt);
        gamecountTxt = rootView.findViewById(R.id.gamecountTxt);
        goldcountText = rootView.findViewById(R.id.goldcountText);
        adcoincountText = rootView.findViewById(R.id.adcoincountText);


        mineImg = rootView.findViewById(R.id.mineImg);
        starImg = rootView.findViewById(R.id.starImg);
        boatImg = rootView.findViewById(R.id.boatImg);
        clockImg = rootView.findViewById(R.id.clockImg);
        shovelImg = rootView.findViewById(R.id.shovelImg);
        helmetImg = rootView.findViewById(R.id.helmetImg);
        gunImg = rootView.findViewById(R.id.gunImg);
        grenadeImg = rootView.findViewById(R.id.grenadeImg);
        tankImg = rootView.findViewById(R.id.tankImg);
        gamecountImg = rootView.findViewById(R.id.gamecountImg);
        goldImg = rootView.findViewById(R.id.goldImg);
        adcoinImg = rootView.findViewById(R.id.adcoinImg);

        settings = activity.getSharedPreferences("TankSettings", 0);

        updateBonus();

        MobileAds.initialize(rootView.getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });



        loadRewardedAd();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        View decorView = getActivity().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void onWindowFocusChanged (boolean hasFocus) {
        View decorView = getActivity().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    View.OnClickListener storeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            view.setEnabled(false);
            Animation animation = Utils.Effects.blink(view, 2);

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {

                    int cost;
                    int goldCount = settings.getInt(TankActivity.GOLD,3);

                    if(id == R.id.backbtn) {
                        onReturnFromStore.updateFromStore();
                        Utils.Store.closeStore((FragmentActivity)getActivity());
                    }
                    else if(id == R.id.storefreegold) {
                        openExchangeDialog(1,0);
                    }
                    else if(id == R.id.storestarboat) {
                        int star = settings.getInt(TankActivity.STAR, 0);
                        int boat = settings.getInt(TankActivity.BOAT, 0);
                        if(star >= CONST.Tank.MAX_ITEM && boat >= CONST.Tank.MAX_ITEM) {
                            showFullToast();
                        }
                        else {
                            cost = Integer.parseInt((activity.getResources().getString(R.string.starboat_gold).replace("x", "")));
                            if (cost <= goldCount) {
                                openExchangeDialog(2, cost);
                            } else {
                                SoundManager.playSound(Sounds.TANK.CLICK2);
                                showToast();
                            }
                        }
                    }
                    else if(id == R.id.storeclockshovel) {
                        int clock = settings.getInt(TankActivity.CLOCK, 0);
                        int shovel = settings.getInt(TankActivity.SHOVEL, 0);
                        if(clock >= CONST.Tank.MAX_ITEM && shovel >= CONST.Tank.MAX_ITEM) {
                            showFullToast();
                        }
                        else {
                            cost = Integer.parseInt((activity.getResources().getString(R.string.clockshovel_gold).replace("x", "")));
                            if (cost <= goldCount) {
                                openExchangeDialog(3, cost);
                            } else {
                                SoundManager.playSound(Sounds.TANK.CLICK2);
                                showToast();
                            }
                        }
                    }
                    else if(id == R.id.storehelmetgun) {
                        int helmet = settings.getInt(TankActivity.SHIELD, 0);
                        int gun = settings.getInt(TankActivity.GUN, 0);
                        if(helmet >= CONST.Tank.MAX_ITEM && gun >= CONST.Tank.MAX_ITEM) {
                            showFullToast();
                        }
                        else {
                            cost = Integer.parseInt((activity.getResources().getString(R.string.gunhelmet_gold).replace("x", "")));
                            if (cost <= goldCount) {
                                openExchangeDialog(4, cost);
                            } else {
                                SoundManager.playSound(Sounds.TANK.CLICK2);
                                showToast();
                            }
                        }
                    }
                    else if(id == R.id.storegrenadetank) {
                        int grenade = settings.getInt(TankActivity.GRENADE, 0);
                        int tank = settings.getInt(TankActivity.TANK, 0);
                        if(grenade >= CONST.Tank.MAX_ITEM && tank >= CONST.Tank.MAX_ITEM) {
                            showFullToast();
                        }
                        else {
                            cost = Integer.parseInt((activity.getResources().getString(R.string.tankgrenade_gold).replace("x", "")));
                            if (cost <= goldCount) {
                                openExchangeDialog(5, cost);
                            } else {
                                SoundManager.playSound(Sounds.TANK.CLICK2);
                                showToast();
                            }
                        }
                    }
                    else if(id == R.id.storemine) {
                        int mine = settings.getInt(TankActivity.MINE, 0);
                        if(mine >= CONST.Tank.MAX_ITEM) {
                            showFullToast();
                        }
                        else {
                            cost = Integer.parseInt((activity.getResources().getString(R.string.mine_gold).replace("x", "")));
                            if (cost <= goldCount) {
                                openExchangeDialog(6, cost);
                            } else {
                                SoundManager.playSound(Sounds.TANK.CLICK2);
                                showToast();
                            }
                        }
                    }
                    else if(id == R.id.storegame) {
                        int game_count = settings.getInt(SettingsManager.RETRY_COUNT, 0);
                        if(game_count >= CONST.Tank.MAX_GAME_COUNT) {
                            showFullToast();
//                            Toast.makeText(activity, "Games full", Toast.LENGTH_SHORT).show();
//                            SoundManager.playSound(Sounds.TANK.CLICK2);
                        }
                        else {
                            cost = Integer.parseInt((activity.getResources().getString(R.string.game_gold).replace("x", "")));
                            if (cost <= goldCount) {
                                openExchangeDialog(7, cost);
                            } else {
                                SoundManager.playSound(Sounds.TANK.CLICK2);
                                showToast();
                            }
                        }
                    }
                    else if(id == R.id.storegame6h) {
                        cost = Integer.parseInt(activity.getResources().getString(R.string.game6h_gold).replace("x", ""));
                        if (cost <= goldCount) {
                            openExchangeDialog(8,cost);
                        } else {
                            SoundManager.playSound(Sounds.TANK.CLICK2);
                            showToast();
                        }
                    }
                    else if(id == R.id.storeadgold) {
                        cost = Integer.parseInt(activity.getResources().getString(R.string.adCoin).replace("x", ""));
                        int coinCount = settings.getInt(SettingsManager.AD_COIN,0);
                        if (cost <= coinCount) {
                            openExchangeDialog(9, cost);
                        }else {
                            SoundManager.playSound(Sounds.TANK.CLICK2);
                            showToastMessage("Not enough Adcoin");
                        }
                    }
                    else if(id == R.id.storebuygold50) {
                        TransactionManager.getInstnce().makePurchase(activity,0);
                    }
                    else if(id == R.id.storebuygold100) {
                        TransactionManager.getInstnce().makePurchase(activity,1);
                    }
                    else if(id == R.id.storebuygold200) {
                        TransactionManager.getInstnce().makePurchase(activity,2);
                    }
                    view.setEnabled(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        }
    };

    public void openExchangeDialog(int item, int cost) {

        ExchangeDialog wd = new ExchangeDialog(activity, item, cost);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.copyFrom(wd.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.dimAmount = 0.8f;
        wd.show();
        wd.getWindow().setAttributes(lp);
        wd.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        wd.setConfirmExchange(new ExchangeDialog.ConfirmExchange() {
            @Override
            public void confirmExchange(int item, int cost) {
                int goldCount = settings.getInt(TankActivity.GOLD,3);
                SharedPreferences.Editor editor = settings.edit();
                int amount;
                switch (item) {
                    case 0:

                        break;
                    case 1:
                        showRewardedVideo();
                        break;
                    case 2:
                        int star = settings.getInt(TankActivity.STAR, 3);
                        int boat = settings.getInt(TankActivity.BOAT, 3);

                        amount = Integer.parseInt((activity.getResources().getString(R.string.starboat_count).replace("x", "")));
                        star += amount;
                        boat += amount;
                        star = Math.min(star,CONST.Tank.MAX_ITEM);
                        boat = Math.min(boat,CONST.Tank.MAX_ITEM);
                        goldCount -= cost;
                        goldCount = Math.max(goldCount,0);
                        goldcountText.setText(String.format("%s", goldCount));
                        starcountText.setText(String.format("%s", star));
                        boatcountTxt.setText(String.format("%s", boat));
                        editor.putInt(TankActivity.STAR, star);
                        editor.putInt(TankActivity.BOAT, boat);
                        editor.putInt(TankActivity.GOLD, goldCount);
                        editor.commit();
                        SoundManager.playSound(Sounds.TANK.BUY_ITEM);
                        Utils.Effects.zoom(starImg,0.7f,4);
                        Utils.Effects.zoom(boatImg,0.7f,4);
                        break;
                    case 3:
                        int clock = settings.getInt(TankActivity.CLOCK, 3);
                        int shovel = settings.getInt(TankActivity.SHOVEL, 3);

                        amount = Integer.parseInt((activity.getResources().getString(R.string.clockshovel_count).replace("x", "")));
                        clock += amount;
                        shovel += amount;
                        clock = Math.min(clock,CONST.Tank.MAX_ITEM);
                        shovel = Math.min(shovel,CONST.Tank.MAX_ITEM);

                        goldCount -= cost;
                        goldCount = Math.max(goldCount,0);
                        goldcountText.setText(String.format("%s", goldCount));
                        clockcountText.setText(String.format("%s", clock));
                        shovelcountTxt.setText(String.format("%s", shovel));
                        editor.putInt(TankActivity.CLOCK, clock);
                        editor.putInt(TankActivity.SHOVEL, shovel);
                        editor.putInt(TankActivity.GOLD, goldCount);
                        editor.commit();
                        SoundManager.playSound(Sounds.TANK.BUY_ITEM);
                        Utils.Effects.zoom(clockImg,0.7f,4);
                        Utils.Effects.zoom(shovelImg,0.7f,4);
                        break;
                    case 4:
                        int gun = settings.getInt(TankActivity.GUN, 3);
                        int helmet = settings.getInt(TankActivity.SHIELD, 3);

                        amount = Integer.parseInt((activity.getResources().getString(R.string.gunhelmet_count).replace("x", "")));
                        gun += amount;
                        helmet += amount;
                        gun = Math.min(gun,CONST.Tank.MAX_ITEM);
                        helmet = Math.min(helmet,CONST.Tank.MAX_ITEM);

                        goldCount -= cost;
                        goldCount = Math.max(goldCount,0);
                        goldcountText.setText(String.format("%s", goldCount));
                        guncountTxt.setText(String.format("%s", gun));
                        helmetcountText.setText(String.format("%s", helmet));
                        editor.putInt(TankActivity.GUN, gun);
                        editor.putInt(TankActivity.SHIELD, helmet);
                        editor.putInt(TankActivity.GOLD, goldCount);
                        editor.commit();
                        SoundManager.playSound(Sounds.TANK.BUY_ITEM);
                        Utils.Effects.zoom(gunImg,0.7f,4);
                        Utils.Effects.zoom(helmetImg,0.7f,4);
                        break;
                    case 5:
                        int tank = settings.getInt(TankActivity.TANK, 3);
                        int grenade = settings.getInt(TankActivity.GRENADE, 3);

                        amount = Integer.parseInt((activity.getResources().getString(R.string.tankgrenade_count).replace("x", "")));
                        tank += amount;
                        grenade += amount;
                        tank = Math.min(tank,CONST.Tank.MAX_ITEM);
                        grenade = Math.min(grenade,CONST.Tank.MAX_ITEM);

                        goldCount -= cost;
                        goldCount = Math.max(goldCount,0);
                        goldcountText.setText(String.format("%s", goldCount));
                        tankcountTxt.setText(String.format("%s", tank));
                        grenadecountText.setText(String.format("%s", grenade));
                        editor.putInt(TankActivity.TANK, tank);
                        editor.putInt(TankActivity.GRENADE, grenade);
                        editor.putInt(TankActivity.GOLD, goldCount);
                        editor.commit();
                        SoundManager.playSound(Sounds.TANK.BUY_ITEM);
                        Utils.Effects.zoom(tankImg,0.7f,4);
                        Utils.Effects.zoom(grenadeImg,0.7f,4);
                        break;
                    case 6:
                        int mine = settings.getInt(TankActivity.MINE, 3);
                        amount = Integer.parseInt((activity.getResources().getString(R.string.mine_count).replace("x", "")));
                        mine += amount;
                        mine = Math.min(mine,CONST.Tank.MAX_ITEM);

                        goldCount -= cost;
                        goldCount = Math.max(goldCount,0);
                        goldcountText.setText(String.format("%s", goldCount));
                        minecountTxt.setText(String.format("%s", mine));
                        editor.putInt(TankActivity.MINE, mine);
                        editor.putInt(TankActivity.GOLD, goldCount);
                        editor.commit();
                        SoundManager.playSound(Sounds.TANK.BUY_ITEM);
                        Utils.Effects.zoom(mineImg,0.7f,4);
                        break;
                    case 7:
                        int game_count = settings.getInt(SettingsManager.RETRY_COUNT, 3);
                        amount = Integer.parseInt((activity.getResources().getString(R.string.game_count).replace("x", "")));
                        game_count += amount;
                        game_count = Math.min(game_count,CONST.Tank.MAX_GAME_COUNT);
                        goldCount -= cost;
                        goldCount = Math.max(goldCount,0);
                        goldcountText.setText(String.format("%s", goldCount));
                        gamecountTxt.setText(String.format("%s", game_count));
                        editor.putInt(SettingsManager.RETRY_COUNT, game_count);
                        editor.putInt(TankActivity.GOLD, goldCount);
                        editor.commit();
                        SoundManager.playSound(Sounds.TANK.BUY_ITEM);
                        Utils.Effects.zoom(gamecountImg,0.7f,4);
                        break;
                    case 8:
                        goldCount -= cost;
                        goldCount = Math.max(goldCount,0);
                        goldcountText.setText(String.format("%s", goldCount));
                        ((ImageView)rootView.findViewById(R.id.gamecountImg)).setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.game6h,null));
                        long time_6h = System.currentTimeMillis() + CONST.Tank.LIFE_DURATION_6HRS;
                        editor.putInt(TankActivity.GOLD, goldCount);
                        editor.putLong(SettingsManager.LIFE_TIME_6H, time_6h);
                        editor.putInt(SettingsManager.RETRY_COUNT, CONST.Tank.MAX_GAME_COUNT);
                        editor.commit();
                        SoundManager.playSound(Sounds.TANK.BUY_ITEM);
                        Utils.Effects.zoom(goldImg,0.7f,4);
                        break;
                    case 9:
                        int coinCount = settings.getInt(SettingsManager.AD_COIN,0);
                        amount = Integer.parseInt((activity.getResources().getString(R.string.adGold_count).replace("x", "")));
                        coinCount -= cost;
                        goldCount += amount;
                        goldCount = Math.min(goldCount,CONST.Tank.MAX_GOLD);
                        goldcountText.setText(String.format("%s", goldCount));
                        adcoincountText.setText(String.format("%s", coinCount));
                        editor.putInt(TankActivity.GOLD, goldCount);
                        editor.putInt(SettingsManager.AD_COIN, coinCount);
                        editor.commit();
                        SoundManager.playSound(Sounds.TANK.BUY_ITEM);
                        Utils.Effects.zoom(goldImg,0.7f,4);
                        break;
                }
                updateBonus();
            }
        });
    }

    public void setStoreListener(OnReturnFromStore onReturnFromStore) {
        this.onReturnFromStore = onReturnFromStore;
    }

    public void showToast() {
        TankToast.showTankToast(getActivity(),"Not enough gold");
    }

    public void showFullToast() {
        TankToast.showTankToast(getActivity(),"Requested item is full");
        SoundManager.playSound(Sounds.TANK.CLICK2);
    }

    public void showToastMessage(String message) {
        TankToast.showTankToast(getActivity(),message);
    }

    @Override
    public void onPurchaseSuccessful(int purchaseID) {
//        Log.d("PURCHASE", "Purchase successful listener");
//        Toast.makeText(this.activity, "Purchase successful", Toast.LENGTH_SHORT).show();
        TankToast.showTankToast(activity, "Purchase successful");
        int goldCount = settings.getInt(TankActivity.GOLD,3);
        if(purchaseID == 0){
            goldCount += 100;
        }
        else if(purchaseID == 1) {
            goldCount += 200;
        }
        else if(purchaseID == 2){
            goldCount += 50;
        }

        goldcountText.setText(String.format("%s", goldCount));
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(TankActivity.GOLD, goldCount);
        editor.commit();
        SoundManager.playSound(Sounds.TANK.BUY_ITEM);
//        ((TankMenuActivity) activity).consumePurchase();
        TransactionManager.getInstnce().consumePurchase();
        Utils.Effects.zoom(goldImg,0.7f,2);
    }

    public interface OnReturnFromStore {
        void updateFromStore();
    }

    public void updateBonus() {
        long game6h = settings.getLong(SettingsManager.LIFE_TIME_6H,0);
        if(game6h > System.currentTimeMillis()) {
            (rootView.findViewById(R.id.gamecountImg)).setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.game6h,null));
        }
        grenadecountText.setText(String.valueOf(settings.getInt(TankActivity.GRENADE,3)));
        helmetcountText.setText(String.valueOf(settings.getInt(TankActivity.SHIELD,3)));
        clockcountText.setText(String.valueOf(settings.getInt(TankActivity.CLOCK,3)));
        shovelcountTxt.setText(String.valueOf(settings.getInt(TankActivity.SHOVEL,3)));
        tankcountTxt.setText(String.valueOf(settings.getInt(TankActivity.TANK,3)));
        starcountText.setText(String.valueOf(settings.getInt(TankActivity.STAR,3)));
        guncountTxt.setText(String.valueOf(settings.getInt(TankActivity.GUN,3)));
        boatcountTxt.setText(String.valueOf(settings.getInt(TankActivity.BOAT,3)));
        minecountTxt.setText(String.valueOf(settings.getInt(TankActivity.MINE,3)));
        goldcountText.setText(String.valueOf(settings.getInt(TankActivity.GOLD,3)));
        gamecountTxt.setText(String.valueOf(settings.getInt(SettingsManager.RETRY_COUNT,5)));
        adcoincountText.setText(String.valueOf(settings.getInt(SettingsManager.AD_COIN,0)));
    }

    public void loadRewardedAd() {
        if (mRewardedAd == null) {
            isLoading = true;
            GOT_REWARD = false;
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(
                    rootView.getContext(),
                    RAD_UNIT_ID,
                    adRequest,
                    new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
//                            Log.d("Rewarded Ads", loadAdError.getMessage());
                            mRewardedAd = null;
                            StoreFragment.this.isLoading = false;
//                            Toast.makeText(TankActivity.this, "onAdFailedToLoad", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            StoreFragment.this.mRewardedAd = rewardedAd;
//                            Log.d("Rewarded Ads", "onAdLoaded");
                            StoreFragment.this.isLoading = false;
//                            Toast.makeText(TankActivity.this, "onAdLoaded", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }



    public void showRewardedVideo() {

        if (mRewardedAd == null) {
//            Log.d("TAG", "The rewarded ad wasn't ready yet.");
            return;
        }

        mRewardedAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
//                        Log.d("Rewarded Ads", "onAdShowedFullScreenContent");
//                        Toast.makeText(TankActivity.this, "onAdShowedFullScreenContent", Toast.LENGTH_SHORT)
//                                .show();
                        GOT_REWARD = false;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
//                        Log.d("Rewarded Ads", "onAdFailedToShowFullScreenContent");
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
//                        Log.d("Rewarded Ads", "onAdDismissedFullScreenContent");
//                        Toast.makeText(TankActivity.this, "onAdDismissedFullScreenContent", Toast.LENGTH_SHORT)
//                                .show();
                        // Preload the next rewarded ad.
                        if(GOT_REWARD) {
                            SharedPreferences.Editor editor = settings.edit();
                            int goldCount = settings.getInt(TankActivity.GOLD, 3);
                            int amount = Integer.parseInt((getResources().getString(R.string.vidGold).replace("x", "")));
                            int totalGold = goldCount + amount;
                            totalGold = Math.min(totalGold,CONST.Tank.MAX_GOLD);
                            goldcountText.setText(String.format(Locale.ENGLISH,"%d",totalGold));
                            editor.putInt(TankActivity.GOLD, totalGold);
                            editor.apply();
                            SoundManager.playSound(Sounds.TANK.EARN_GOLD);
                            Utils.Effects.zoom(goldImg,0.7f,4);
                        }
                        StoreFragment.this.loadRewardedAd();
                    }
                });

        mRewardedAd.show(
                StoreFragment.this.getActivity(),
                new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        // Handle the reward.
//                        Log.d("Rewarded Ads", "The user earned the reward.");
                        int rewardAmount = rewardItem.getAmount();
                        String rewardType = rewardItem.getType();
                        GOT_REWARD = true;

                    }
                });
    }
}