package com.jiokye.tankbattle_multiplayer.fragments;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.jiokye.tankbattle_multiplayer.activity.TankActivity;
import com.jiokye.tankbattle_multiplayer.activity.TankMenuActivity;
import com.jiokye.tankbattle_multiplayer.connection.TCPClientConnectionThread;
import com.jiokye.tankbattle_multiplayer.connection.TCPServerConnectionThread;
import com.jiokye.tankbattle_multiplayer.tank.TankView;
import com.jiokye.tankbattle_multiplayer.utility.CONST;
import com.jiokye.tankbattle_multiplayer.utility.RemoteMessageListener;
import com.jiokye.tankbattle_multiplayer.utility.SettingsManager;
import com.jiokye.tankbattle_multiplayer.utility.TankTextView;
import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.model.Game;
import com.jiokye.tankbattle_multiplayer.model.TankGameModel;
import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;
import com.jiokye.tankbattle_multiplayer.utility.CVTR;
import com.jiokye.tankbattle_multiplayer.utility.MessageRegister;
import com.jiokye.tankbattle_multiplayer.utility.TankToast;
import com.jiokye.tankbattle_multiplayer.utility.Utils;
import com.jiokye.tankbattle_multiplayer.wifidirect.WifiDialog;
import com.jiokye.tankbattle_multiplayer.wifidirect.WifiDirectManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;


public class TankStageFragment extends Fragment implements View.OnTouchListener, RemoteMessageListener {

    View rootView;

    public AppCompatActivity activity;
    public Dialog d;
    //    public Button yes, no;
    SharedPreferences settings;
    boolean twoPlayers, coop;
    ImageView playBtn, backBtn, playerInfo, constructionBtn;
    TankTextView completedTxt;

    GridLayout stageBtns, objGrid;
//    GridLayout.LayoutParams cardParams, cardParamsSel;
    LinearLayout.LayoutParams cardParamsSel;
    CardView.LayoutParams cardParams;
    ScrollView scrollView;
    int selected = 0;
    ArrayList<boolean[]> objectives;
    private boolean selfDismiss = true;
    public static boolean p2Ready = false, opened = false;

    ProgressBar pb1,pb2,pb3;
    RelativeLayout gft3;
    ConstraintLayout gft1, gft2;
    ImageView itm1, itm2, itm3, reward1, reward2, gft3star;
    ObjectAnimator gftItm1, gftItm2, gftItm3;
    ArrayList<Integer> collectedGifts;
    ArrayList<Integer> gifts;

    ViewGroup.LayoutParams itmParams;
    float progress;


    public TankStageFragment() {

    }

    public TankStageFragment(AppCompatActivity a, boolean twoPlayers, boolean coop) {
        this.activity = a;
        this.twoPlayers = twoPlayers;
        this.coop = coop;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_tank_stage, container, false);
        rootView.findViewById(R.id.fragView).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

//        WifiDirectManager.getInstance().setDisconnectListener(new WifiDirectManager.OnDisconnectListener() {
//            @Override
//            public void handleDisconnection() {
//
//            }
//        });

        MessageRegister.getInstance().setMsgListener(this);

        float SCALE = activity.getResources().getDisplayMetrics().density;
        stageBtns = (GridLayout) rootView.findViewById(R.id.stage_grid);
        objGrid = (GridLayout) rootView.findViewById(R.id.objective_grid);
        scrollView = (ScrollView) rootView.findViewById(R.id.objScroll);
        completedTxt = (TankTextView) rootView.findViewById(R.id.completedTxt) ;
        constructionBtn = rootView.findViewById(R.id.construction_title);

        pb1 = rootView.findViewById(R.id.pb1);
        pb2 = rootView.findViewById(R.id.pb2);
        pb3 = rootView.findViewById(R.id.pb3);

        gft1 = rootView.findViewById(R.id.gift1);
        gft2 = rootView.findViewById(R.id.gift2);
        gft3 = rootView.findViewById(R.id.gift3);

        itm1 = rootView.findViewById(R.id.gift1item);
        itm2 = rootView.findViewById(R.id.gift2item);
        itm3 = rootView.findViewById(R.id.gift3item);

        reward1 = rootView.findViewById(R.id.reward1);
        reward2 = rootView.findViewById(R.id.reward2);
        gft3star = rootView.findViewById(R.id.gift3star);

        constructionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundManager.playSound(Sounds.TANK.CLICK);
                if(twoPlayers && !WifiDirectManager.getInstance().isServer() && TCPClientConnectionThread.serverStarted){
//                    Toast toast = Toast.makeText(activity.getApplicationContext(),
//                            "Wait for player 1 to select stage!",
//                            Toast.LENGTH_SHORT);
//                    toast.show();
                    TankToast.showTankToast(activity,"Wait for player 1 to select a stage!");
                }
                else {
                    switchStage();
                }
            }
        });

        cardParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.MATCH_PARENT);
        cardParamsSel = new LinearLayout.LayoutParams((int) CVTR.toDp(70), (int) CVTR.toDp(70));


        cardParams.setMargins((int)CVTR.toDp(2),(int)CVTR.toDp(2),(int)CVTR.toDp(2),(int)CVTR.toDp(2));
        cardParamsSel.setMargins((int)CVTR.toDp(20),(int)CVTR.toDp(20),(int)CVTR.toDp(0),(int)CVTR.toDp(0));
        cardParamsSel.gravity = Gravity.CENTER;


        CardView.LayoutParams txtParams = new CardView.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        txtParams.setMargins(0,0,0,0);
        txtParams.gravity = Gravity.CENTER;

        settings = activity.getSharedPreferences("TankSettings", 0);
        int unlockLevel = settings.getInt(TankMenuActivity.PREF_LEVEL,1);

        ArrayList<Integer> levelStars = loadStars();


        for(int i = 1 ; i <= 35; i++) {
            CardView selCard = new CardView(this.getContext());
            selCard.setLayoutParams(cardParamsSel);
            selCard.setRadius((int)CVTR.toDp(10));
            selCard.setCardBackgroundColor(Color.TRANSPARENT);

            CardView card = new CardView(this.getContext());
            card.setLayoutParams(cardParams);
            card.setCardBackgroundColor(Color.TRANSPARENT);
            ImageView img = new ImageView(this.getContext());

            //todo remove
//            unlockLevel = 5;
            if(i <= unlockLevel) {
                int star = levelStars.get(i-1);
//                int star = 0;
                switch(star) {
                    case 0:
                        img.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.zerostar,null));
                        break;
                    case 1:
                        img.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.onestar,null));
                        break;
                    case 2:
                        img.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.twostar,null));
                        break;
                    case 3:
                        img.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.threestar,null));
                        break;
                }
//                img.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.unlocked,null));
            }
            else {
                img.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.locked,null));
            }
            card.setRadius((int)CVTR.toDp(10));
            TankTextView stg = new TankTextView(this.getContext());
            stg.setText(String.valueOf(i));
            stg.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            stg.setTextSize(CVTR.toDp(8));
            stg.setBackgroundColor(Color.TRANSPARENT);

            stg.setLayoutParams(txtParams);
            int h = (int)((selCard.getLayoutParams().height - stg.getTextSize() )/2);
            txtParams.setMargins(0,h,0,0);
            stg.setLayoutParams(txtParams);
            card.addView(img);
            card.addView(stg);
            if(i <= unlockLevel) {
                card.setOnTouchListener(this);
            }
            card.setTag(i);
            selCard.addView(card);

            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.height = (int) CVTR.toDp(80);
            param.width = (int) CVTR.toDp(80);
            param.rightMargin = 20;
            param.topMargin = 20;
            param.setGravity(Gravity.CENTER);
            int row  = (i-1)/4;
            int col = (i-1)%4;
            param.columnSpec = GridLayout.spec(col);
            param.rowSpec = GridLayout.spec(row);
            stageBtns.addView(selCard,param);
        }

        ((CardView)stageBtns.getChildAt(selected)).setCardBackgroundColor(Color.WHITE);

        objectives = new ArrayList<>();
        objectives = loadObjectives();

        playBtn = rootView.findViewById(R.id.playGameBtn);
        backBtn = rootView.findViewById(R.id.backGameBtn);
        playerInfo = rootView.findViewById(R.id.playerInfo);

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Utils.Effects.blink(view,2).setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        int games = settings.getInt(SettingsManager.RETRY_COUNT,0);
                        long game6h = settings.getLong(SettingsManager.LIFE_TIME_3H,0);
//                        games = 3;
                        if(games > 0 || game6h > System.currentTimeMillis()){
                            int level = selected + 1;

                            if (twoPlayers && !WifiDirectManager.getInstance().isServer() && TCPClientConnectionThread.serverStarted) {    //This is player 2
                                //TODO Change to dialog
                                SoundManager.playSound(Sounds.TANK.CLICK3);
//                                Toast toast = Toast.makeText(activity.getApplicationContext(),
//                                        "Wait for player 1 to select stage!",
//                                        Toast.LENGTH_SHORT);
//
////                                ViewGroup group = (ViewGroup) toast.getView();
////                                TextView messageTextView = (TextView) group.getChildAt(0);
////                                messageTextView.setTextSize(25);
//
//                                toast.show();
                                TankToast.showTankToast(activity,"Wait for player 1 to select a stage!");
                                TankGameModel model = new TankGameModel();
                                model.playerInfo = true;
                                model.playerReady = true;
                                model.ip = WifiDirectManager.getInstance().getDeviceIP();
                                WifiDirectManager.getInstance().sendTCPMessage(model);
//                                return true;
                                return;
                            }
                            else if (twoPlayers && WifiDirectManager.getInstance().isServer() && TCPServerConnectionThread.serverStarted) {
                                if(p2Ready) {
                                    SoundManager.playSound(Sounds.TANK.CLICK3);
                                    TankGameModel model = new TankGameModel();
                                    model.mlevelInfo = true;
                                    model.mlevel = level;
                                    model.ip = WifiDirectManager.getInstance().getDeviceIP();
                                    TankView.SCENE_SOUND = (int)(Sounds.TANK.FIGHT_SCENE1 + Math.random()*(Sounds.TANK.FIGHT_SCENE5-Sounds.TANK.FIGHT_SCENE1) + 0.5);
                                    model.sceneSound = TankView.SCENE_SOUND;
                                    WifiDirectManager.getInstance().sendTCPMessage(model);
                                }
                                else{
                                    //TODO Change to dialog
                                    SoundManager.playSound(Sounds.TANK.CLICK2);
//                                    Toast toast = Toast.makeText(activity.getApplicationContext(),
//                                            "Player 2 not ready!",
//                                            Toast.LENGTH_SHORT);
//
////                                    ViewGroup group = (ViewGroup) toast.getView();
////                                    TextView messageTextView = (TextView) group.getChildAt(0);
////                                    messageTextView.setTextSize(25);
//
//                                    toast.show();
                                    TankToast.showTankToast(activity,"Player 2 is not ready!");
//                                    return true;
                                    return;
                                }
                            }

                            TankView.level = level;
                            TankView.CONSTRUCTION = false;
                            //TODO
//                        long game6h = settings.getLong(TankActivity.LIFE_TIME_6H,0);
                            if(game6h < System.currentTimeMillis()) {
                                --games;
                                if(games < 0){
                                    games = 0;
                                }
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putInt(SettingsManager.RETRY_COUNT, games);
                                if (games == CONST.Tank.MAX_GAME_COUNT - 1) {
                                    editor.putLong(SettingsManager.LIFE_TIME, System.currentTimeMillis());
                                }
                                editor.apply();
                            }
                            SoundManager.playSound(Sounds.TANK.CLICK);
                            ((TankMenuActivity) activity).startGame(twoPlayers, coop);
//                            startGame(twoPlayers);
                        }
                        else {
                            SoundManager.playSound(Sounds.TANK.CLICK2);
                            TankToast.showTankToast(activity, "You have 0 game left");
                            // todo -- uncomment after rework on ads
//                            openGamePurchse();
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
            }
        });



        displyObjectives(selected);
        int completed = getCompleted(selected+1);
        completedTxt.setText(String.format(Locale.ENGLISH,"CHALLENGES %d/%d", completed, TankView.NUM_OBJECTIVES));
        collectedGifts = loadCollectedGifts();
        gifts = loadGifts();
        int giftCode = gifts.get(selected);

//        for(int i = 0; i < collectedGifts.size(); i++){
//            collectedGifts.set(i,7);
//        }
//        saveCollectedGifts(collectedGifts);
//
//        for(int i = 0; i < gifts.size(); i++){
//            gifts.set(i,0);
//        }
//        saveGifts(gifts);

        gftItm1 = Utils.Effects.rotate(reward1);
        gftItm2 = Utils.Effects.rotate(reward2);
        gftItm3 = Utils.Effects.rotate(gft3star);
        gft3star.setVisibility(View.INVISIBLE);
//        itm1.setVisibility(View.INVISIBLE);
//        itm2.setVisibility(View.INVISIBLE);

        Utils.Effects.pause_rotation(gftItm1);
        Utils.Effects.pause_rotation(gftItm2);

        int collected = collectedGifts.get(selected);
        if((collected & 1) == 0) {
            gft2.setAlpha(0.6f);
            reward2.setVisibility(View.INVISIBLE);

            int[] giftIDs = getGifts(giftCode);
            itm2.setBackground(ResourcesCompat.getDrawable(getResources(),giftIDs[1],null));

        }
        else {
            gft2.setAlpha(1f);
            reward2.setVisibility(View.VISIBLE);
        }

        if((collected & 2) == 0) {
            gft1.setAlpha(0.6f);
            reward1.setVisibility(View.INVISIBLE);
            int[] giftIDs = getGifts(giftCode);
            itm1.setBackground(ResourcesCompat.getDrawable(getResources(),giftIDs[0],null));

        }
        else {
            gft1.setAlpha(1f);
            reward1.setVisibility(View.VISIBLE);
        }

        if((collected & 4) == 0) {
            gft3.setAlpha(0.6f);
            itm3.setAlpha(0.3f);
//            gft3star.setVisibility(View.INVISIBLE);
        }
        else {
            gft1.setAlpha(1f);
            itm3.setAlpha(1f);
//            gft3star.setVisibility(View.VISIBLE);
        }

        progress = (float)completed/TankView.NUM_OBJECTIVES;
        if(progress < CONST.Tank.GIFT_THR1) {
            pb1.setProgress((int)((progress*100)/CONST.Tank.GIFT_THR1));

            gft1.setBackgroundColor(getResources().getColor(R.color.gray));
            pb2.setProgress(0);
            gft2.setBackgroundColor(getResources().getColor(R.color.gray));
            pb3.setProgress(0);
            gft3.setBackgroundColor(getResources().getColor(R.color.gray));
            itm3.setAlpha(0.3f);

            Utils.Effects.pause_rotation(gftItm1);
            reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
            Utils.Effects.pause_rotation(gftItm2);
            reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
        }
        else if(progress < CONST.Tank.GIFT_THR2){
            pb1.setProgress(100);
            gft1.setBackgroundColor(getResources().getColor(R.color.green));
            pb2.setProgress((int)(((progress-CONST.Tank.GIFT_THR1)*100)/CONST.Tank.GIFT_THR1));

            gft2.setBackgroundColor(getResources().getColor(R.color.gray));
            pb3.setProgress(0);
            gft3.setBackgroundColor(getResources().getColor(R.color.gray));
            itm3.setAlpha(0.3f);

            if((collectedGifts.get(selected) & 2) > 0) {
                Utils.Effects.resume_rotation(gftItm1);
                reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
                Utils.Effects.pause_rotation(gftItm2);
                reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
            }
        }
        else if(progress < CONST.Tank.GIFT_THR3){
            pb1.setProgress(100);
            gft1.setBackgroundColor(getResources().getColor(R.color.green));
            pb2.setProgress(100);
            gft2.setBackgroundColor(getResources().getColor(R.color.green));
            pb3.setProgress((int)(((progress-CONST.Tank.GIFT_THR2)*100)/CONST.Tank.GIFT_THR1));

            gft3.setBackgroundColor(getResources().getColor(R.color.gray));
            itm3.setAlpha(0.3f);

            if((collected & 2) != 0) {
                Utils.Effects.resume_rotation(gftItm1);
                reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
            }
            else{
                Utils.Effects.pause_rotation(gftItm1);
                reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
            }

            if((collected & 1) != 0) {
                Utils.Effects.resume_rotation(gftItm2);
                reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
            }else{
                Utils.Effects.pause_rotation(gftItm2);
                reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
            }

//            switch (collected) {
//                case 0:
//                    Utils.Effects.pause_rotation(gftItm1);
//                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
//                    Utils.Effects.pause_rotation(gftItm2);
//                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
//                    break;
//                case 1:
//                    Utils.Effects.pause_rotation(gftItm1);
//                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
//                    Utils.Effects.resume_rotation(gftItm2);
//                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
//                    break;
//                case 2:
//                    Utils.Effects.resume_rotation(gftItm1);
//                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
//                    Utils.Effects.pause_rotation(gftItm2);
//                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
//                    break;
//                case 3:
//                    Utils.Effects.resume_rotation(gftItm1);
//                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
//                    Utils.Effects.resume_rotation(gftItm2);
//                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
//                    break;
//            }
        }
        else {
            pb1.setProgress(100);
            gft1.setBackgroundColor(getResources().getColor(R.color.green));
            pb2.setProgress(100);
            gft2.setBackgroundColor(getResources().getColor(R.color.green));
            pb3.setProgress(100);
            gft3.setBackgroundColor(getResources().getColor(R.color.green));

            if((collected & 2) != 0) {
                Utils.Effects.resume_rotation(gftItm1);
                reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
            }
            else{
                Utils.Effects.pause_rotation(gftItm1);
                reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
            }

            if((collected & 1) != 0) {
                Utils.Effects.resume_rotation(gftItm2);
                reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
            }else{
                Utils.Effects.pause_rotation(gftItm2);
                reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
            }

            if((collected & 4) != 0) {
                itm3.setAlpha(1f);
                gft3star.setVisibility(View.VISIBLE);
                Utils.Effects.resume_rotation(gftItm3);
            }
            else {
                gft3.setAlpha(0.6f);
                itm3.setAlpha(0.3f);
                Utils.Effects.pause_rotation(gftItm3);
            }

//            switch (collected) {
//                case 0:
//                    Utils.Effects.pause_rotation(gftItm1);
//                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
//                    Utils.Effects.pause_rotation(gftItm2);
//                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
//                    break;
//                case 1:
//                    Utils.Effects.pause_rotation(gftItm1);
//                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
//                    Utils.Effects.resume_rotation(gftItm2);
//                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
//                    break;
//                case 2:
//                    Utils.Effects.resume_rotation(gftItm1);
//                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
//                    Utils.Effects.pause_rotation(gftItm2);
//                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
//                    break;
//                case 3:
//                    Utils.Effects.resume_rotation(gftItm1);
//                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
//                    Utils.Effects.resume_rotation(gftItm2);
//                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
//                    break;
//            }
        }


        reward1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int completed = getCompleted(selected+1);
                float prg = (float)completed/TankView.NUM_OBJECTIVES;
                int collected = collectedGifts.get(selected);
                if((collected & 2) > 0 && prg >= CONST.Tank.GIFT_THR1) {                             // check if the first item has not been collected (i.e 0b1x)
                    collectedGifts.set(selected, collected & 0b101);    // set the first bit to 0 to indicate it has been collected
                    Utils.Effects.pause_rotation(gftItm1);

                    int currentGiftCode = gifts.get(selected);
                    int rndGift = getRandomReward();
                    gifts.set(selected,(currentGiftCode & 0x0F) | ((rndGift << 4) & 0xF0));
                    itm1.setBackground(ResourcesCompat.getDrawable(getResources(),getGiftID(rndGift),null));
                    saveGifts(gifts);
                    //todo -- give reward
                    awardGift(rndGift);

                    reward1.setVisibility(View.INVISIBLE);
                    itm1.setVisibility(View.VISIBLE);
                    gft1.setAlpha(0.6f);
                    saveCollectedGifts(collectedGifts);
                    SoundManager.playSound(Sounds.TANK.REWARD);

                }
            }
        });

        reward2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int completed = getCompleted(selected+1);
                float prg = (float)completed/TankView.NUM_OBJECTIVES;
                int collected = collectedGifts.get(selected);
                if((collected & 1) > 0 && prg >= CONST.Tank.GIFT_THR2) {                             // check if the second item has not been collected (i.e 0bx1)
                    collectedGifts.set(selected, collected & 0b110);    // set the second bit to 0 to indicate it has been collected
                    Utils.Effects.pause_rotation(gftItm2);

                    int currentGiftCode = gifts.get(selected);
                    int rndGift = getRandomReward();
                    gifts.set(selected,(currentGiftCode & 0xF0) | (rndGift & 0x0F));
                    itm2.setBackground(ResourcesCompat.getDrawable(getResources(),getGiftID(rndGift),null));
                    saveGifts(gifts);
                    //todo -- give reward
                    awardGift(rndGift);

                    reward2.setVisibility(View.INVISIBLE);
                    itm2.setVisibility(View.VISIBLE);
                    gft2.setAlpha(0.6f);
                    saveCollectedGifts(collectedGifts);
                    SoundManager.playSound(Sounds.TANK.REWARD);

                }
            }
        });

        gft3star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int completed = getCompleted(selected+1);
                float prg = (float)completed/TankView.NUM_OBJECTIVES;
                int collected = collectedGifts.get(selected);
                if((collected & 4) > 0 && prg >= CONST.Tank.GIFT_THR3) {                             // check if the second item has not been collected (i.e 0bx1)
                    collectedGifts.set(selected, collected & 0b011);    // set the second bit to 0 to indicate it has been collected
                    Utils.Effects.pause_rotation(gftItm3);
                    gft3star.setVisibility(View.INVISIBLE);

//                    int currentGiftCode = gifts.get(selected);
//                    int rndGift = getRandomReward();
//                    gifts.set(selected,(currentGiftCode & 0xF0) | (rndGift & 0x0F));
//                    itm3.setBackground(ResourcesCompat.getDrawable(getResources(),getGiftID(rndGift),null));
//                    saveGifts(gifts);
                    //todo -- give reward
                    awardGift(11);

//                    gft3.setVisibility(View.INVISIBLE);
//                    itm3.setVisibility(View.VISIBLE);
                    gft3.setAlpha(0.6f);
                    gft3.setAlpha(0.6f);
                    saveCollectedGifts(collectedGifts);
                    SoundManager.playSound(Sounds.TANK.REWARD);

                }
            }
        });

        selfDismiss = true;
        opened = true;
//        if (twoPlayers && !WifiDirectManager.getInstance().isServer() && ClientConnectionThread.serverStarted) {
//
//            TankGameModel model = new TankGameModel();
//            model.playerInfo = true;
//            model.playerReady = true;
//            WifiDirectManager.getInstance().sendMessage(model);
//        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundManager.playSound(Sounds.TANK.CLICK);
                Utils.Effects.blink(view,2).setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        selfDismiss = true;
                        if (twoPlayers) {
                            if(!WifiDirectManager.getInstance().isServer() && TCPClientConnectionThread.serverStarted) {
                                TankGameModel model = new TankGameModel();
                                model.playerInfo = true;
                                model.playerReady = false;
                                WifiDirectManager.getInstance().sendTCPMessage(model);
                            }
                            WifiDirectManager.getInstance().cancelDisconnect();

                        }
                        dismiss();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
            }
        });

        playerInfo.setBackground(ResourcesCompat.getDrawable(TankStageFragment.this.getResources(),R.drawable.p1,null));

        if(twoPlayers && WifiDirectManager.getInstance().isServer() && TCPServerConnectionThread.serverStarted) {
            if (TankStageFragment.p2Ready) {
                playBtn.setBackground(ResourcesCompat.getDrawable(TankStageFragment.this.getResources(),R.drawable.readybtn,null));
            } else {
                playBtn.setBackground(ResourcesCompat.getDrawable(TankStageFragment.this.getResources(), R.drawable.waitbtn, null));
//                        playBtn.setEnabled(false);
//                        playBtn.setAlpha(0.5f);}
            }
        }

        return rootView;
    }

    private int[] getGifts(int giftCode) {
        int gift1 = giftCode >> 4;
        int gift2 = giftCode & 0x0F;

        return new int[]{getGiftID(gift1), getGiftID(gift2)};
    }

//    private int getRandomReward() {
//       float p = (float)Math.random();
//
//        if(p < 0.18) {
//            return 1;
//        }
//        else if(p < 0.36) {
//            return 2;
//        }
//        else if(p < 0.47) {
//            return 3;
//        }
//        else if(p < 0.65) {
//            return 4;
//        }
//        else if(p < 0.72) {
//            return 5;
//        }
//        else if(p < 0.83) {
//            return 6;
//        }
//        else if(p < 0.875) {
//            return 7;
//        }
//        else if(p < 0.975){
//            return 8;
//        }
//        else if(p <0.1) {
//            return 9;
//        }
//        else {
//            return 10;
//        }
//    }

    private int getRandomReward() {

        int[] prob = new int[10];

        for(int i = 0; i < 2; i++) {
            float p = (float) Math.random();
            if (p < 0.05) {
                prob[0]++;
            } else if (p < 0.15) {
                prob[1]++;
            } else if (p < 0.45) {
                prob[2]++;
            } else if (p < 0.5) {
                prob[3]++;
            } else if (p < 0.65) {
                prob[4]++;
            } else if (p < 0.8) {
                prob[5]++;
            } else if (p < 0.85) {
                prob[6]++;
            } else if (p < 0.9) {
                prob[7]++;
            } else if (p < 0.95) {
                prob[8]++;
            } else {
                prob[9]++;
            }
        }

        int max = 0;
        int max_index = 0;
        for(int i = 0; i < prob.length; i++) {
            if(prob[i] > max) {
                max = prob[i];
                max_index = i;
            }
        }

        return max_index + 1;
    }

    int getGiftID(int id) {
        switch (id) {
            case 1:
                return R.drawable.bonus_mine;
            case 2:
                return R.drawable.bonus_star;
            case 3:
                return R.drawable.bonus_boat;
            case 4:
                return R.drawable.bonus_clock;
            case 5:
                return R.drawable.bonus_shovel;
            case 6:
                return R.drawable.bonus_helmet;
            case 7:
                return R.drawable.bonus_gun;
            case 8:
                return R.drawable.bonus_grenade;
            case 9:
                return R.drawable.bonus_tank;
            case 10:
                return R.drawable.gold;
            default:
                return 0;
        }
    }

    private void awardGift(int id) {
        SharedPreferences.Editor editor = settings.edit();
        int amount;
        switch (id) {
            case 1:
                amount = settings.getInt(TankActivity.MINE,0);
                editor.putInt(TankActivity.MINE,Math.min(CONST.Tank.MAX_ITEM,amount+1));
                break;
            case 2:
                amount = settings.getInt(TankActivity.STAR,0);
                editor.putInt(TankActivity.STAR,Math.min(CONST.Tank.MAX_ITEM,amount+1));
                break;
            case 3:
                amount = settings.getInt(TankActivity.BOAT,0);
                editor.putInt(TankActivity.BOAT,Math.min(CONST.Tank.MAX_ITEM,amount+1));
                break;
            case 4:
                amount = settings.getInt(TankActivity.CLOCK,0);
                editor.putInt(TankActivity.CLOCK,Math.min(CONST.Tank.MAX_ITEM,amount+1));
                break;
            case 5:
                amount = settings.getInt(TankActivity.SHOVEL,0);
                editor.putInt(TankActivity.SHOVEL,Math.min(CONST.Tank.MAX_ITEM,amount+1));
                break;
            case 6:
                amount = settings.getInt(TankActivity.SHIELD,0);
                editor.putInt(TankActivity.SHIELD,Math.min(CONST.Tank.MAX_ITEM,amount+1));
                break;
            case 7:
                amount = settings.getInt(TankActivity.GUN,0);
                editor.putInt(TankActivity.GUN,Math.min(CONST.Tank.MAX_ITEM,amount+1));
                break;
            case 8:
                amount = settings.getInt(TankActivity.GRENADE,0);
                editor.putInt(TankActivity.GRENADE,Math.min(CONST.Tank.MAX_ITEM,amount+1));
                break;
            case 9:
                amount = settings.getInt(TankActivity.TANK,0);
                editor.putInt(TankActivity.TANK,Math.min(CONST.Tank.MAX_ITEM,amount+1));
                break;
            case 10:
                amount = settings.getInt(TankActivity.GOLD,0);
                editor.putInt(TankActivity.GOLD,Math.min(CONST.Tank.MAX_GOLD,amount+1));
                break;
            case 11:
                amount = settings.getInt(SettingsManager.RETRY_COUNT,0);
                editor.putInt(SettingsManager.RETRY_COUNT,Math.min(CONST.Tank.MAX_GAME_COUNT,amount+1));
                break;
        }
        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
//        if(twoPlayers && !(ClientConnectionThread.serverStarted || ServerConnectionThread.serverStarted)) {
        if(twoPlayers){
            openWifiDialog();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent m) {
        if(m.getAction() == MotionEvent.ACTION_DOWN) {
            SoundManager.playSound(Sounds.TANK.CLICK);
            int level = (int) (v.getTag());
            int completed = getCompleted(level);
            completedTxt.setText(String.format(Locale.ENGLISH,"CHALLENGES %d/%d", completed, TankView.NUM_OBJECTIVES));
            ((CardView) stageBtns.getChildAt(selected)).setCardBackgroundColor(Color.TRANSPARENT);
            selected = level - 1;
            ((CardView) stageBtns.getChildAt(selected)).setCardBackgroundColor(Color.WHITE);
            displyObjectives(selected);
            scrollView.fullScroll(ScrollView.FOCUS_UP);

            int giftCode = gifts.get(selected);

            Utils.Effects.pause_rotation(gftItm1);
            Utils.Effects.pause_rotation(gftItm2);
            Utils.Effects.pause_rotation(gftItm3);
            gft3star.setVisibility(View.INVISIBLE);

            float progress = (float)completed/TankView.NUM_OBJECTIVES;

            int collected = collectedGifts.get(selected);
            if((collected & 1) == 0) {
                gft2.setAlpha(0.6f);
                reward2.setVisibility(View.INVISIBLE);
                int[] giftIDs = getGifts(giftCode);
                itm2.setBackground(ResourcesCompat.getDrawable(getResources(),giftIDs[1],null));
            }
            else {
                gft2.setAlpha(1f);
                reward2.setVisibility(View.VISIBLE);
            }

            if((collected & 2) == 0) {
                gft1.setAlpha(0.6f);
                reward1.setVisibility(View.INVISIBLE);
                int[] giftIDs = getGifts(giftCode);
                itm1.setBackground(ResourcesCompat.getDrawable(getResources(),giftIDs[0],null));
            }
            else {
                gft1.setAlpha(1f);
                reward1.setVisibility(View.VISIBLE);
            }

            if((collected & 4) == 0) {
                gft3.setAlpha(0.6f);
                itm3.setAlpha(0.3f);
//            gft3star.setVisibility(View.INVISIBLE);
            }
            else {
                gft1.setAlpha(1f);
                itm3.setAlpha(1f);
//            gft3star.setVisibility(View.VISIBLE);
            }


            if(progress < CONST.Tank.GIFT_THR1) {
                pb1.setProgress((int)((progress*100)/CONST.Tank.GIFT_THR1));

                gft1.setBackgroundColor(getResources().getColor(R.color.gray));
                pb2.setProgress(0);
                gft2.setBackgroundColor(getResources().getColor(R.color.gray));
                pb3.setProgress(0);
                gft3.setBackgroundColor(getResources().getColor(R.color.gray));
                itm3.setAlpha(0.3f);

                Utils.Effects.pause_rotation(gftItm1);
                reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
                Utils.Effects.pause_rotation(gftItm2);
                reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
            }
            else if(progress < CONST.Tank.GIFT_THR2){
                pb1.setProgress(100);
                gft1.setBackgroundColor(getResources().getColor(R.color.green));
                pb2.setProgress((int)(((progress-CONST.Tank.GIFT_THR1)*100)/CONST.Tank.GIFT_THR1));

                gft2.setBackgroundColor(getResources().getColor(R.color.gray));
                pb3.setProgress(0);
                gft3.setBackgroundColor(getResources().getColor(R.color.gray));
                itm3.setAlpha(0.3f);

                if((collectedGifts.get(selected) & 2) > 0) {
                    Utils.Effects.resume_rotation(gftItm1);
                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
                    Utils.Effects.pause_rotation(gftItm2);
                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
                }
            }
            else if(progress < CONST.Tank.GIFT_THR3){
                pb1.setProgress(100);
                gft1.setBackgroundColor(getResources().getColor(R.color.green));
                pb2.setProgress(100);
                gft2.setBackgroundColor(getResources().getColor(R.color.green));
                pb3.setProgress((int)(((progress-CONST.Tank.GIFT_THR2)*100)/CONST.Tank.GIFT_THR1));

                gft3.setBackgroundColor(getResources().getColor(R.color.gray));
                itm3.setAlpha(0.3f);

                if((collected & 2) != 0) {
                    Utils.Effects.resume_rotation(gftItm1);
                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
                }
                else{
                    Utils.Effects.pause_rotation(gftItm1);
                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
                }

                if((collected & 1) != 0) {
                    Utils.Effects.resume_rotation(gftItm2);
                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
                }else{
                    Utils.Effects.pause_rotation(gftItm2);
                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
                }

//            switch (collected) {
//                case 0:
//                    Utils.Effects.pause_rotation(gftItm1);
//                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
//                    Utils.Effects.pause_rotation(gftItm2);
//                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
//                    break;
//                case 1:
//                    Utils.Effects.pause_rotation(gftItm1);
//                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
//                    Utils.Effects.resume_rotation(gftItm2);
//                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
//                    break;
//                case 2:
//                    Utils.Effects.resume_rotation(gftItm1);
//                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
//                    Utils.Effects.pause_rotation(gftItm2);
//                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
//                    break;
//                case 3:
//                    Utils.Effects.resume_rotation(gftItm1);
//                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
//                    Utils.Effects.resume_rotation(gftItm2);
//                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
//                    break;
//            }
            }
            else {
                pb1.setProgress(100);
                gft1.setBackgroundColor(getResources().getColor(R.color.green));
                pb2.setProgress(100);
                gft2.setBackgroundColor(getResources().getColor(R.color.green));
                pb3.setProgress(100);
                gft3.setBackgroundColor(getResources().getColor(R.color.green));

                if((collected & 2) != 0) {
                    Utils.Effects.resume_rotation(gftItm1);
                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
                }
                else{
                    Utils.Effects.pause_rotation(gftItm1);
                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
                }

                if((collected & 1) != 0) {
                    Utils.Effects.resume_rotation(gftItm2);
                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
                }else{
                    Utils.Effects.pause_rotation(gftItm2);
                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
                }

                if((collected & 4) != 0) {
                    itm3.setAlpha(1f);
                    gft3star.setVisibility(View.VISIBLE);
                    Utils.Effects.resume_rotation(gftItm3);
                }
                else {
                    gft3.setAlpha(0.6f);
                    itm3.setAlpha(0.3f);
                    Utils.Effects.pause_rotation(gftItm3);
                }

//            switch (collected) {
//                case 0:
//                    Utils.Effects.pause_rotation(gftItm1);
//                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
//                    Utils.Effects.pause_rotation(gftItm2);
//                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
//                    break;
//                case 1:
//                    Utils.Effects.pause_rotation(gftItm1);
//                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
//                    Utils.Effects.resume_rotation(gftItm2);
//                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
//                    break;
//                case 2:
//                    Utils.Effects.resume_rotation(gftItm1);
//                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
//                    Utils.Effects.pause_rotation(gftItm2);
//                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover1, null));
//                    break;
//                case 3:
//                    Utils.Effects.resume_rotation(gftItm1);
//                    reward1.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
//                    Utils.Effects.resume_rotation(gftItm2);
//                    reward2.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.rewardcover2, null));
//                    break;
//            }
            }
        }
        return true;
    }


    @Override
    public void onMessageReceived(Game message) {
        if(message instanceof TankGameModel) {
            TankGameModel msg = (TankGameModel)message;
            if(twoPlayers && !WifiDirectManager.getInstance().isServer() && TCPClientConnectionThread.serverStarted) {
                if (msg.mlevelInfo) {
//                    String ip = msg.ip;
//                    if(ip != null) {
//                        Log.d("IP ADDRESS: ", ip);
//                        WifiDirectManager.getInstance().startUDPService("0.0.0.0");
//                    }
                    TankView.level = msg.mlevel;
                    Log.d("CONSTRUCTION", String.valueOf(msg.constructionLevelReq));
                    if(msg.constructionLevelReq) {
                        TankView.CONSTRUCTION = true;
                        Log.d("CONSTRUCTION", "Construction level received");
                        if(msg.constLevel != null) {
                            if(!TankView.constructionStageReceived) {
                                Log.d("CONSTRUCTION", "Construction stage received");
                                MessageRegister.getInstance().registerOnConstructionReceived(msg.constLevel);
                            }
                        }
                    }
                    int games = settings.getInt(SettingsManager.RETRY_COUNT, 0);
                    --games;
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt(SettingsManager.RETRY_COUNT, games);
                    if (games == CONST.Tank.MAX_GAME_COUNT - 1) {
                        editor.putLong(SettingsManager.LIFE_TIME, System.currentTimeMillis());
                    }
                    editor.apply();
                    selfDismiss = false;
                    dismiss();
                    TankView.SCENE_SOUND = msg.sceneSound;
                    ((TankMenuActivity) activity).startGame(twoPlayers, coop);
                }
            }
            else if(twoPlayers && WifiDirectManager.getInstance().isServer() && TCPServerConnectionThread.serverStarted)
            {
                if (msg.playerInfo) {
                    p2Ready = msg.playerReady;
//                    String ip = msg.ip;
//                    if(ip != null) {
//                        Log.d("IP ADDRESS: ", ip);
//                        WifiDirectManager.getInstance().startUDPService("0.0.0.0");
//                    }
                    TankStageFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(p2Ready) {
                                playBtn.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.playstage,null));
                            }
                            else {
                                playBtn.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.waitbtn,null));
                            }
                        }
                    });

                }
            }

        }
    }


    private void saveObjectives(ArrayList<boolean[]> objectives) {
        SharedPreferences.Editor editor = settings.edit();
        Gson gson = new Gson();
        String json = gson.toJson(objectives);
        editor.putString(SettingsManager.OBJECTIVES,json);
        editor.apply();
    }

    private ArrayList<boolean[]> loadObjectives() {
        String objectives = settings.getString(SettingsManager.OBJECTIVES,null);
        if(objectives == null) {
            ArrayList<boolean[]> obj = new ArrayList<>();
            for(int i = 0; i < TankView.NUM_LEVELS; i++) {
                boolean[] p = new boolean[TankView.NUM_OBJECTIVES];
                for(int j = 0; j < p.length; j++) {
                    p[j] = false;
                }
                obj.add(p);
            }
            saveObjectives(obj);
            return obj;
        }
        Type type = new TypeToken<ArrayList<boolean[]>>() {}.getType();
        Gson gson = new Gson();

//        ArrayList<boolean[]> objectives = gson.fromJson(json,type);
//        return objectives;
        return gson.fromJson(objectives,type);
    }


    private void saveStars(ArrayList<Integer> stars) {
        SharedPreferences.Editor editor = settings.edit();
        Gson gson = new Gson();
        String json = gson.toJson(stars);
        editor.putString(SettingsManager.LEVEL_STARS,json);
        editor.apply();
    }

    private ArrayList<Integer> loadStars() {
        String stars = settings.getString(SettingsManager.LEVEL_STARS,null);
        if(stars == null) {
            ArrayList<Integer> star = new ArrayList<>();
            for(int i = 0; i < TankView.NUM_LEVELS; i++) {
                star.add(0);
            }
            saveStars(star);
            return star;
        }
        Type type = new TypeToken<ArrayList<Integer>>() {}.getType();
        Gson gson = new Gson();
        return gson.fromJson(stars,type);
    }


    private void saveCollectedGifts(ArrayList<Integer> gifts) {
        SharedPreferences.Editor editor = settings.edit();
        Gson gson = new Gson();
        String json = gson.toJson(gifts);
        editor.putString(SettingsManager.COLLECTED_GIFTS,json);
        editor.apply();
    }

    private ArrayList<Integer> loadCollectedGifts() {
        String gifts = settings.getString(SettingsManager.COLLECTED_GIFTS,null);
        if(gifts == null) {
            ArrayList<Integer> gift = new ArrayList<>();
            for(int i = 0; i < TankView.NUM_LEVELS; i++) {
                gift.add(3);
            }
            saveCollectedGifts(gift);
            return gift;
        }
        Type type = new TypeToken<ArrayList<Integer>>() {}.getType();
        Gson gson = new Gson();
        return gson.fromJson(gifts,type);
    }


    private void saveGifts(ArrayList<Integer> gifts) {
        SharedPreferences.Editor editor = settings.edit();
        Gson gson = new Gson();
        String json = gson.toJson(gifts);
        editor.putString(SettingsManager.GIFTS,json);
        editor.apply();
    }

    private ArrayList<Integer> loadGifts() {
        String gifts = settings.getString(SettingsManager.GIFTS,null);
        if(gifts == null) {
            ArrayList<Integer> gift = new ArrayList<>();
            for(int i = 0; i < TankView.NUM_LEVELS; i++) {
                gift.add(0);
            }
            saveGifts(gift);
            return gift;
        }
        Type type = new TypeToken<ArrayList<Integer>>() {}.getType();
        Gson gson = new Gson();
        return gson.fromJson(gifts,type);
    }



    private void displyObjectives(int level) {
        for(int obj = 0; obj < TankView.NUM_OBJECTIVES; obj++) {
            if(objectives.get(level)[obj]) {
                ((TankTextView)((LinearLayout) ((LinearLayout) ((CardView) objGrid.getChildAt(obj)).getChildAt(0)).getChildAt(0)).getChildAt(1)).setText(R.string.completed);
                ((TankTextView)((LinearLayout) ((LinearLayout) ((CardView) objGrid.getChildAt(obj)).getChildAt(0)).getChildAt(0)).getChildAt(1)).setTextColor(Color.GREEN);
            }
            else {
                ((TankTextView)((LinearLayout) ((LinearLayout) ((CardView) objGrid.getChildAt(obj)).getChildAt(0)).getChildAt(0)).getChildAt(1)).setText(R.string.uncompleted);
                ((TankTextView)((LinearLayout) ((LinearLayout) ((CardView) objGrid.getChildAt(obj)).getChildAt(0)).getChildAt(0)).getChildAt(1)).setTextColor(Color.GRAY);
            }
        }
    }

    private int getCompleted(int level) {
        int completed = 0;
        boolean[] obj = objectives.get(level-1);
        for(boolean i:obj) {
            if(i) {
                ++completed;
            }
        }
        return completed;
    }

    private void dismiss() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            ((TankMenuActivity)activity).enableButtons();
        }
    }

    private void switchStage() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentFrame,new TankStage2Fragment(activity, twoPlayers, coop));
            fragmentTransaction.addToBackStack("cFragment");
            fragmentTransaction.commit();
        }
    }


    public void openWifiDialog() {
        WifiDirectManager.getInstance().initialize(activity);
        WifiDirectManager.getInstance().registerBReceiver();

        WifiDialog wd = new WifiDialog(getActivity());
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.copyFrom(wd.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.dimAmount = 0.8f;
        wd.show();
        wd.getWindow().setAttributes(lp);
        wd.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        wd.setWifiDialogResult(new WifiDialog.OnWifiDialogResult() {
            @Override
            public void finish(boolean p2Selected) {
                if(p2Selected) {
                    if(WifiDirectManager.getInstance().isServer() && TCPServerConnectionThread.serverStarted) {
                        playerInfo.setBackground(ResourcesCompat.getDrawable(TankStageFragment.this.getResources(),R.drawable.p1,null));
                        playBtn.setBackground(ResourcesCompat.getDrawable(TankStageFragment.this.getResources(),R.drawable.waitbtn,null));
//                        playBtn.setEnabled(false);
//                        playBtn.setAlpha(0.5f);
                    }
                    else if(!WifiDirectManager.getInstance().isServer() && TCPClientConnectionThread.serverStarted){
                        playerInfo.setBackground(ResourcesCompat.getDrawable(TankStageFragment.this.getResources(),R.drawable.p2,null));
                        playBtn.setBackground(ResourcesCompat.getDrawable(TankStageFragment.this.getResources(),R.drawable.readybtn,null));
//                        constructionBtn.setEnabled(false);
                    }
                    p2Ready = false;
                }
                else {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    if (fragmentManager.getBackStackEntryCount() > 0) {
                        fragmentManager.popBackStack();
                        ((TankMenuActivity)activity).enableButtons();
                    }
                }
            }
        });
    }
}