package com.jiokye.tankbattle_multiplayer.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.jiokye.tankbattle_multiplayer.activity.TankMenuActivity;
import com.jiokye.tankbattle_multiplayer.connection.TCPClientConnectionThread;
import com.jiokye.tankbattle_multiplayer.connection.TCPServerConnectionThread;
import com.jiokye.tankbattle_multiplayer.tank.TankView;
import com.jiokye.tankbattle_multiplayer.utility.RemoteMessageListener;
import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.model.Game;
import com.jiokye.tankbattle_multiplayer.model.TankGameModel;
import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;
import com.jiokye.tankbattle_multiplayer.utility.CONST;
import com.jiokye.tankbattle_multiplayer.utility.MessageRegister;
import com.jiokye.tankbattle_multiplayer.utility.SettingsManager;
import com.jiokye.tankbattle_multiplayer.utility.TankTextView;
import com.jiokye.tankbattle_multiplayer.utility.TankToast;
import com.jiokye.tankbattle_multiplayer.utility.Utils;
import com.jiokye.tankbattle_multiplayer.wifidirect.WifiDirectManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;


public class TankStage2Fragment extends Fragment implements View.OnTouchListener, RemoteMessageListener {

    View rootView;

    public AppCompatActivity activity;
    public Dialog d;
    //    public Button yes, no;
    SharedPreferences settings;
    boolean twoPlayers;
    ImageView playBtn, backBtn, playerInfo;
    TankTextView completedTxt;

    GridLayout stageBtns, objGrid;
    LinearLayout.LayoutParams cardParams, cardParamsSel;
    LinearLayout scrollBound;
    ScrollView scrollView;
    LinearLayout stageView;
    int selected = 0;
    ArrayList<boolean[]> objectives;
    private boolean selfDismiss = true;
    public static boolean p2Ready = false, opened = false;


    ArrayList<String> savedNames;
    char[][] stage;
    private char[][] stageObjects;
    ListView stageListView;
    ArrayAdapter adapter;
    String stageName;

    Drawable brick, stone, water, bush, ice, eagle;

    public TankStage2Fragment() {

    }

    public TankStage2Fragment(AppCompatActivity a, boolean twoPlayers) {
        this.activity = a;
        this.twoPlayers = twoPlayers;
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
        rootView = inflater.inflate(R.layout.fragment_tank_stage3, container, false);
        rootView.findViewById(R.id.frag2View).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        settings = activity.getSharedPreferences("TankSettings", 0);

        MessageRegister.getInstance().setMsgListener(this);
        savedNames = loadStageNames();

////        float SCALE = activity.getResources().getDisplayMetrics().density;
////        stageBtns = (GridLayout) rootView.findViewById(R.id.stage_grid);

//        objGrid = rootView.findViewById(R.id.objective_grid);
//        scrollView = rootView.findViewById(R.id.objScroll);

        stageView = rootView.findViewById(R.id.stageView);
        stageView.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams vLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        vLayout.gravity = Gravity.CENTER;
        vLayout.weight = 1;
        vLayout.height = 0;


        LinearLayout.LayoutParams hLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        hLayout.gravity = Gravity.CENTER;
        hLayout.weight = 1;
        hLayout.width = 0;

        for(int row = 0; row < 26; row++) {
            LinearLayout rowObj = new LinearLayout(rootView.getContext());
            rowObj.setOrientation(LinearLayout.HORIZONTAL);
            rowObj.setLayoutParams(vLayout);
            for(int col = 0; col < 26; col++) {
                ImageView obj = new ImageView(rootView.getContext());
                obj.setLayoutParams(hLayout);
                if(row == 0 || row == 1) {
                    if(col == 0 || col == 1 || col == 12 || col == 13 || col == 24 || col == 25) {
                        obj.setBackgroundColor(Color.DKGRAY);
                    }
                }
                else if(row >= 23) {
                    if(col >= 11 && col <= 14) {
                        obj.setBackgroundColor(Color.DKGRAY);
                    }
                    if(row >= 24) {
                        if(col == 8 || col == 9 || col == 16 || col == 17) {
                            obj.setBackgroundColor(Color.DKGRAY);
                        }
                    }
                }
                rowObj.addView(obj);
            }
            stageView.addView(rowObj);
        }

        brick = ResourcesCompat.getDrawable(getResources(),R.drawable.brick,null);
        stone = ResourcesCompat.getDrawable(getResources(),R.drawable.stone,null);
        bush = ResourcesCompat.getDrawable(getResources(),R.drawable.bush,null);
        water = ResourcesCompat.getDrawable(getResources(),R.drawable.water,null);
        ice = ResourcesCompat.getDrawable(getResources(),R.drawable.ice,null);

        rootView.findViewById(R.id.stage_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundManager.playSound(Sounds.TANK.CLICK);
                switchStage();
            }
        });

        stageListView = rootView.findViewById(R.id.stageList);

        adapter = new ArrayAdapter<String>(stageListView.getContext(),
                R.layout.save_list_view, savedNames);
        stageListView.setAdapter(adapter);

        stageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SoundManager.playSound(Sounds.TANK.CLICK);
                stageName = (String) ((TextView) view).getText();
                int count = adapterView.getCount();

                for(int c = 0; c < count; c++) {
//                    (adapterView.getChildAt(c)).setBackground(null);
                    (adapterView.getChildAt(c)).setBackground(ResourcesCompat.getDrawable(TankStage2Fragment.this.getResources(),R.drawable.boarderwhite,null));
                }
                view.setBackground(ResourcesCompat.getDrawable(TankStage2Fragment.this.getResources(),R.drawable.boarder,null));
                selected = adapterView.getPositionForView(view);
                Log.d("Adapter", "Clicked on view " + selected);

                if(stageName != null) {
                    stage = loadStage(stageName);
                    updateStage(stage);
                }
            }
        });

        stageObjects = new char[26][26];

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
                    public void onAnimationRepeat(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if((!twoPlayers || WifiDirectManager.getInstance().isServer()) &&  (stageName == null || stageName.isEmpty())) {
                            return;
                        }
                        int games = settings.getInt(SettingsManager.RETRY_COUNT,0);
                        long game6h = settings.getLong(SettingsManager.LIFE_TIME_6H,0);

                        if(games > 0 || game6h > System.currentTimeMillis()){

                            int level = selected + 1;

                            if (twoPlayers && !WifiDirectManager.getInstance().isServer() && TCPClientConnectionThread.serverStarted) {
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
                                WifiDirectManager.getInstance().sendTCPMessage(model);
//                                return true;
                                return;
                            }
                            else if (twoPlayers && WifiDirectManager.getInstance().isServer() && TCPServerConnectionThread.serverStarted) {
                                if(TankStageFragment.p2Ready) {
                                    SoundManager.playSound(Sounds.TANK.CLICK3);
                                    TankGameModel model = new TankGameModel();
                                    model.mlevelInfo = true;
                                    model.mlevel = level;
                                    model.constructionLevelReq = true;
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
                            TankView.CONSTRUCTION = true;
                            //TODO
//                        long game6h = settings.getLong(TankActivity.LIFE_TIME_6H,0);
                            if(game6h < System.currentTimeMillis()) {
                                --games;
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putInt(SettingsManager.RETRY_COUNT, games);

                                if (games == CONST.Tank.MAX_GAME_COUNT - 1) {
                                    editor.putLong(SettingsManager.LIFE_TIME, System.currentTimeMillis());
                                }
                                editor.apply();
                            }
                            SoundManager.playSound(Sounds.TANK.CLICK);
                            ((TankMenuActivity) activity).startGame(twoPlayers);
                        }
                        else {
                            SoundManager.playSound(Sounds.TANK.CLICK2);
//                            openGamePurchse();
                        }
                    }
                });

            }
        });



//        displyObjectives(selected);
//        int completed = getCompleted(selected+1);
//        completedTxt.setText(String.format(Locale.ENGLISH,"CHALLENGES %d/%d", completed, TankView.NUM_OBJECTIVES));

        selfDismiss = true;
        opened = true;
        if (twoPlayers && !WifiDirectManager.getInstance().isServer() && TCPClientConnectionThread.serverStarted) {

            TankGameModel model = new TankGameModel();
            model.playerInfo = true;
            model.playerReady = true;
            WifiDirectManager.getInstance().sendTCPMessage(model);
        }

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

        playerInfo.setBackground(ResourcesCompat.getDrawable(TankStage2Fragment.this.getResources(),R.drawable.p1,null));

        if(twoPlayers && WifiDirectManager.getInstance().isServer() && TCPServerConnectionThread.serverStarted) {
            if (TankStageFragment.p2Ready) {
                playBtn.setBackground(ResourcesCompat.getDrawable(TankStage2Fragment.this.getResources(),R.drawable.readybtn,null));
            } else {
                playBtn.setBackground(ResourcesCompat.getDrawable(TankStage2Fragment.this.getResources(), R.drawable.waitbtn, null));
//                        playBtn.setEnabled(false);
//                        playBtn.setAlpha(0.5f);}
            }
        }

        return rootView;
    }

    @Override
    public boolean onTouch(View v, MotionEvent m) {
        if(m.getAction() == MotionEvent.ACTION_DOWN) {
            SoundManager.playSound(Sounds.TANK.CLICK);
            int level = (int) (v.getTag());
            int completed = getCompleted(level);
            completedTxt.setText(String.format(Locale.ENGLISH,"CHALLENGES %d/%d", completed, CONST.Tank.NUM_OBJECTIVES));
            ((CardView) stageBtns.getChildAt(selected)).setCardBackgroundColor(Color.TRANSPARENT);
            selected = level - 1;
            ((CardView) stageBtns.getChildAt(selected)).setCardBackgroundColor(Color.WHITE);
            displyObjectives(selected);
            scrollView.fullScroll(ScrollView.FOCUS_UP);
        }

        return true;
    }


    @Override
    public void onMessageReceived(Game message) {
        if(message instanceof TankGameModel) {
            TankGameModel msg = (TankGameModel)message;
            if(twoPlayers && !WifiDirectManager.getInstance().isServer() && TCPClientConnectionThread.serverStarted) {
                if (msg.mlevelInfo) {
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
                    ((TankMenuActivity) activity).startGame(twoPlayers);
                }
            }
            else if(twoPlayers && WifiDirectManager.getInstance().isServer() && TCPServerConnectionThread.serverStarted)
            {
                if (msg.playerInfo) {
                    TankStageFragment.p2Ready = msg.playerReady;
                    TankStage2Fragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(TankStageFragment.p2Ready) {
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


    public void openGamePurchse() {
//        TankPurchaseGameDialog wd = new TankPurchaseGameDialog(activity);
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(wd.getWindow().getAttributes());
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//        wd.show();
//        wd.getWindow().setAttributes(lp);
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
            for(int i = 0; i < CONST.Tank.NUM_LEVELS; i++) {
                boolean[] p = new boolean[CONST.Tank.NUM_OBJECTIVES];
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
            for(int i = 0; i < CONST.Tank.NUM_LEVELS; i++) {
                star.add(0);
            }
            saveStars(star);
            return star;
        }
        Type type = new TypeToken<ArrayList<Integer>>() {}.getType();
        Gson gson = new Gson();
        return gson.fromJson(stars,type);
    }

    private void displyObjectives(int level) {
        for(int obj = 0; obj < CONST.Tank.NUM_OBJECTIVES; obj++) {
            if(objectives.get(level)[obj]) {
                ((LinearLayout) ((LinearLayout) ((CardView) objGrid.getChildAt(obj)).getChildAt(0)).getChildAt(0)).getChildAt(1).setVisibility(View.INVISIBLE);
                ((LinearLayout) ((CardView) objGrid.getChildAt(obj)).getChildAt(0)).getChildAt(1).setVisibility(View.VISIBLE);
            }
            else {
                ((LinearLayout) ((LinearLayout) ((CardView) objGrid.getChildAt(obj)).getChildAt(0)).getChildAt(0)).getChildAt(1).setVisibility(View.VISIBLE);
                ((LinearLayout) ((CardView) objGrid.getChildAt(obj)).getChildAt(0)).getChildAt(1).setVisibility(View.INVISIBLE);
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
        }
    }

    private void switchStage() {
        Log.d("Fragment Switch", "Switching to fragment1");

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentFrame,new TankStageFragment(activity, twoPlayers));
            fragmentTransaction.addToBackStack("cFragment");
            fragmentTransaction.commit();
        }
    }

    private char[][] loadStage(String name) {
        String stage = settings.getString(name,null);
        if(stage == null) {
            return new char[26][26];
        }
        Type type = new TypeToken<char[][]>() {}.getType();
        Gson gson = new Gson();
        return gson.fromJson(stage,type);
    }

    private ArrayList<String> loadStageNames() {
        String stageNames = settings.getString(SettingsManager.STAGE_NAMES,null);
        if(stageNames == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        Gson gson = new Gson();
        return gson.fromJson(stageNames,type);
    }

    public void updateStage(char[][] stageIDs) {
        for(int row = 0; row < 26; row++) {
            for(int col = 0; col < 26; col++) {
                if(row == 0 || row == 1) {
                    if(col == 0 || col == 1 || col == 12 || col == 13 || col == 24 || col == 25) {
                        continue;
                    }
                }
                else if(row >= 23) {
                    if(col >= 11 && col <= 14) {
                        continue;
                    }
                    if(row >= 24) {
                        if(col == 8 || col == 9 || col == 16 || col == 17) {
                            continue;
                        }
                    }
                }

                char id = stageIDs[row][col];
                ImageView pos = (ImageView) ((LinearLayout)((LinearLayout)stageView).getChildAt(row)).getChildAt(col);
                if(id==0) {
                    pos.setBackground(null);
                }
                else if(id=='@') {
                    pos.setBackground(stone);
                }
                else if(id=='#') {
                    pos.setBackground(brick);
                }
                else if(id=='%') {
                    pos.setBackground(bush);
                }
                else if(id=='~') {
                    pos.setBackground(water);
                }
                else if(id=='-') {
                    pos.setBackground(ice);
                }

                stageObjects[row][col] = id;

            }
        }
    }
}