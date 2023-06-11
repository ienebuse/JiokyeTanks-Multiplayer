package com.jiokye.tankbattle_multiplayer.puzzles.sokoban;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.activity.TankActivity;
import com.jiokye.tankbattle_multiplayer.activity.TankMenuActivity;
import com.jiokye.tankbattle_multiplayer.puzzles.PuzzCompleteDialog;
import com.jiokye.tankbattle_multiplayer.puzzles.PuzzDialog;
import com.jiokye.tankbattle_multiplayer.puzzles.sokoban.BoardView;
import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;
import com.jiokye.tankbattle_multiplayer.tank.TankView;
import com.jiokye.tankbattle_multiplayer.utility.CONST;
import com.jiokye.tankbattle_multiplayer.utility.SettingsManager;
import com.jiokye.tankbattle_multiplayer.utility.Utils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SokobanPuzzleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SokobanPuzzleFragment extends Fragment{

    SharedPreferences settings;
    View rootView;
    Activity activity;

    LinearLayout gamecountView, goldcountView;
    ConstraintLayout navView;

    int level;
    BoardView boardView;
    LinearLayout boardrows;
    LinearLayout.LayoutParams cellParams;
    long startTime;
    Timer updateTimer;
    TextView timerView;
    ImageView resetBtn, endBtn;
    int Rows;
    int Cols;
    boolean started = false;
    boolean ended = false;
    boolean sound = true;
    int resID;
    //    Confetti confetti;
    View[] confettiEmitter;
    boolean opened = false;


    ImageView gamecountImg, goldcountImg, rwdImg;
    TextView retryTmrTxt, gamecountTxt, goldcountTxt;
    Drawable rwdDrawable;
    ImageView upBtn, leftBtn, downBtn, rightBtn;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SokobanPuzzleFragment() {
        // Required empty public constructor
    }

    public SokobanPuzzleFragment(Activity activity, int level) {
        this.activity = activity;
        this.level = level;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NumberPuzzleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SokobanPuzzleFragment newInstance(String param1, String param2) {
        SokobanPuzzleFragment fragment = new SokobanPuzzleFragment();
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View decorView = getActivity().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        rootView = inflater.inflate(R.layout.fragment_puzzle, container, false);
        settings = activity.getSharedPreferences("TankSettings", 0);

        navView = rootView.findViewById(R.id.navView);
        navView.setEnabled(true);
        navView.setVisibility(View.VISIBLE);


        gamecountImg = rootView.findViewById(R.id.gamecountImg);
        goldcountImg = rootView.findViewById(R.id.goldcountImg);
        rwdImg = rootView.findViewById(R.id.rwdImg);

        gamecountView = rootView.findViewById(R.id.gamecountView);
        goldcountView = rootView.findViewById(R.id.goldcountView);

        retryTmrTxt = rootView.findViewById(R.id.menuRetryTmrTxt);
        gamecountTxt = rootView.findViewById(R.id.gamecountTxt);
        goldcountTxt = rootView.findViewById(R.id.goldcountTxt);

        goldcountTxt.setText(String.valueOf(settings.getInt(TankActivity.GOLD,3)));
        retryTmrTxt.setText(String.valueOf(settings.getInt(SettingsManager.RETRY_COUNT,5)));

        upBtn = rootView.findViewById(R.id.upBtn);
        leftBtn = rootView.findViewById(R.id.leftBtn);
        rightBtn = rootView.findViewById(R.id.rightBtn);
        downBtn = rootView.findViewById(R.id.downBtn);

        upBtn.setOnTouchListener(buttonPressed);
        leftBtn.setOnTouchListener(buttonPressed);
        rightBtn.setOnTouchListener(buttonPressed);
        downBtn.setOnTouchListener(buttonPressed);

        switch (level){
            case 1:
                resID = R.drawable.puzz3;
                Rows = Cols = 3;
                rwdDrawable = ResourcesCompat.getDrawable(activity.getResources(),R.drawable.lvl1rwd,null);

                break;
            case 2:
                resID = R.drawable.puzz4;
                Rows = Cols = 4;
                rwdDrawable = ResourcesCompat.getDrawable(activity.getResources(),R.drawable.lvl2rwd,null);
                break;
            case 3:
                resID = R.drawable.puzz5;
                Rows = Cols = 5;
                rwdDrawable = ResourcesCompat.getDrawable(activity.getResources(),R.drawable.lvl3rwd,null);
                break;
            case 4:
                resID = R.drawable.puzz6;
                Rows = Cols = 6;
                rwdDrawable = ResourcesCompat.getDrawable(activity.getResources(),R.drawable.game6h,null);
                break;
        }

        rwdImg.setBackground(rwdDrawable);

        rootView.findViewById(R.id.fragView3).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });



        boardrows = rootView.findViewById(R.id.rows);
        boardrows.setBackgroundColor(Color.BLACK);

        LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT);
        cellParams.weight = 1;

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0);
        rowParams.weight = 1;
        rowParams.gravity = Gravity.CENTER;


        for(int row = 0; row < 20; row++) {
            LinearLayout columns = new LinearLayout(getContext());
            columns.setGravity(Gravity.CENTER);
            columns.setOrientation(LinearLayout.HORIZONTAL);
            for(int col = 0; col < 20; col++) {
                ImageView cell = new ImageView(getContext());
                cell.setLayoutParams(cellParams);
                cell.setTag(String.valueOf(row*Cols + col));
//                cell.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.sokwall));
                columns.addView(cell);
            }
            boardrows.addView(columns,rowParams);
        }

        Bitmap boardImg = BitmapFactory.decodeResource(activity.getResources(),resID);
        boardView = new BoardView((AppCompatActivity)activity, boardrows, level);

        resetBtn = rootView.findViewById(R.id.resetBtn);
        resetBtn.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.resetbtn));
        endBtn = rootView.findViewById(R.id.endBtn);

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.Effects.blink(view, 2).setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationRepeat(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        reset();
                    }


                });

            }
        });

        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.Effects.blink(view,2).setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationRepeat(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        go_back();
                    }


                });

            }
        });



//        confettiEmitter = new View[4];
//        confettiEmitter[0] = findViewById(R.id.emiter_top_right1);
//        confettiEmitter[1] = findViewById(R.id.emiter_top_left1);
//        confettiEmitter[2] = findViewById(R.id.emiter_top_left2);
//        confettiEmitter[3] = findViewById(R.id.emiter_top_right2);


//        timerView = this.findViewById(R.id.timerfield);




//        SoundManager.getInstance();
//        SoundManager.initSounds(this);
//        int[] sounds = {R.raw.win1, R.raw.win2, R.raw.movecard, R.raw.wrongmove};
//        SoundManager.loadSounds(sounds);

//        update_board();

        opened = true;
        return rootView;
    }



    void doGameWon() {
        PuzzCompleteDialog wd = new PuzzCompleteDialog(requireActivity(), rwdDrawable);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.copyFrom(wd.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.dimAmount = 0.8f;
        wd.show();
        wd.getWindow().setAttributes(lp);
        wd.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        wd.setOnDismissPuzzleCompleteDialog(new PuzzCompleteDialog.OnDismissPuzzleCompleteDialog() {
            @Override
            public void finish(boolean ok) {
                if(ok) {
                    started = true;
                    int gold = settings.getInt(TankActivity.GOLD,3);
                    SharedPreferences.Editor editor = settings.edit();
                    switch (level){
                        case 1:
                            editor.putInt(TankActivity.GOLD, gold + CONST.PUZZLE.LVL1RWD);
                            break;
                        case 2:
                            editor.putInt(TankActivity.GOLD, gold + CONST.PUZZLE.LVL2RWD);
                            break;
                        case 3:
                            editor.putInt(TankActivity.GOLD, gold + CONST.PUZZLE.LVL3RWD);
                            break;
                        case 4:
                            long time_3h = System.currentTimeMillis() + CONST.Tank.LIFE_DURATION_3HRS;
                            editor.putLong(SettingsManager.LIFE_TIME_3H, time_3h);
                            editor.putInt(SettingsManager.RETRY_COUNT, CONST.Tank.MAX_GAME_COUNT);
                            break;
                    }
                    editor.commit();
                    SoundManager.playSound(Sounds.TANK.EARN_GOLD);
                    goldcountTxt.setText(String.valueOf(settings.getInt(TankActivity.GOLD,3)));
                    retryTmrTxt.setText(String.valueOf(settings.getInt(SettingsManager.RETRY_COUNT,5)));
                    if(level == 1 || level == 2 || level == 3 ) {
                        Utils.Effects.zoom(goldcountView,0.8f,4);
                    }
                    else if(level == 4) {
                        Utils.Effects.zoom(gamecountView,0.8f,4);
                    }
                }
            }
        });
    }

    private void buttonPressed(int dir) {

        switch (dir) {
            case CONST.Direction.UP: {
                upBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.up31_btn,null));

                downBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.down30_btn,null));
                leftBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.left30_btn,null));
                rightBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.right30_btn,null));
                break;
            }
            case CONST.Direction.DOWN: {
                downBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.down31_btn,null));

                upBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.up30_btn,null));
                leftBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.left30_btn,null));
                rightBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.right30_btn,null));
                break;
            }
            case CONST.Direction.LEFT: {
                leftBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.left31_btn,null));

                upBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.up30_btn,null));
                downBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.down30_btn,null));
                rightBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.right30_btn,null));
                break;
            }
            case CONST.Direction.RIGHT: {
                rightBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.right31_btn,null));

                upBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.up30_btn,null));
                leftBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.left30_btn,null));
                downBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.down30_btn,null));
                break;
            }
            default: {
                rightBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.right30_btn,null));
                upBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.up30_btn,null));
                leftBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.left30_btn,null));
                downBtn.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.down30_btn,null));
                break;
            }
        }
    }

    View.OnTouchListener buttonPressed = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent m) {
            if(m.getAction() == MotionEvent.ACTION_DOWN) {
                if(!started) {
                    started = true;
                }
                int id = v.getId();
                if(id == R.id.upBtn) {
                    buttonPressed(CONST.Direction.UP);
                    boardView.move(CONST.Direction.UP);
                }
                else if(id == R.id.leftBtn) {
                    buttonPressed(CONST.Direction.LEFT);
                    boardView.move(CONST.Direction.LEFT);
                }
                else if(id == R.id.downBtn) {
                    buttonPressed(CONST.Direction.DOWN);
                    boardView.move(CONST.Direction.DOWN);
                }
                else if(id == R.id.rightBtn) {
                    buttonPressed(CONST.Direction.RIGHT);
                    boardView.move(CONST.Direction.RIGHT);
                }
                checkGameState();
            }

            else if(m.getAction() == MotionEvent.ACTION_UP) {
                buttonPressed(-1);
            }
            return true;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    public void reset() {
        enableControls();
        if (started) {

            if(ended) {
//                if(confetti != null) {
//                    confetti.cancel_confetti();
//                }
//                updateTimer.cancel();
                boardView.resetBoard();
//                start_timer();
//                startTime = System.currentTimeMillis();
//                update_board();
                ended = false;
            }
            else {
                showDialog("Really?").setOnDismissPuzzleDialog(new PuzzDialog.OnDismissPuzzleDialog() {
                    @Override
                    public void finish(boolean ok) {
                        if(ok) {
//                            updateTimer.cancel();
                            boardView.resetBoard();
//                            start_timer();
//                            startTime = System.currentTimeMillis();
//                            update_board();
                        }
                    }
                });
            }
        }
    }

    protected  void start_timer() {
        updateTimer = new Timer("update");
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                update_gametime();
            }
        }, 0, 1000);
    }

    PuzzDialog showDialog(String info) {
        PuzzDialog wd = new PuzzDialog(requireActivity(), info);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.copyFrom(wd.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.dimAmount = 0.8f;
        wd.show();
        wd.getWindow().setAttributes(lp);
        wd.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        return wd;
    }

    public void disableControls() {
        upBtn.setEnabled(false);
        rightBtn.setEnabled(false);
        leftBtn.setEnabled(false);
        downBtn.setEnabled(false);
    }

    public void enableControls() {
        upBtn.setEnabled(true);
        rightBtn.setEnabled(true);
        leftBtn.setEnabled(true);
        downBtn.setEnabled(true);
    }

    protected void checkGameState() {
        if(started) {
            boolean won = boardView.check_board(sound);
            if (won) {
                ended = true;

                started = false;
                doGameWon();
                disableControls();
            }
        }
    }

    public void toggle_sound(View view) {
//        Button soundBtn = (Button)view;
//        if(sound){
//            sound = false;
//            soundBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.nosound));
//        }
//        else{
//            sound = true;
//            soundBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.sound));
//        }
    }

    protected void update_gametime(){
//        long currentTime = System.currentTimeMillis() - startTime;
//        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
//        timerView.setText(sdf.format(currentTime));
    }

    public void go_back() {
        if (started) {
            showDialog("Really? Your game will be lost!").setOnDismissPuzzleDialog(new PuzzDialog.OnDismissPuzzleDialog() {
                @Override
                public void finish(boolean ok) {
                    if(ok) {
                        closePuzzle();
                    }
                }
            });
        }
        else{
            closePuzzle();
        }
    }

    public void onDestroy()
    {
        super.onDestroy();
    }

    void closePuzzle() {
        opened = false;
        ((TankMenuActivity)activity).updateStore();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        }
    }



}