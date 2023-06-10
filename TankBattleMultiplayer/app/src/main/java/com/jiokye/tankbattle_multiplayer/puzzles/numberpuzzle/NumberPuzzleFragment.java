package com.jiokye.tankbattle_multiplayer.puzzles.numberpuzzle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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
import android.widget.Toast;

import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.activity.TankActivity;
import com.jiokye.tankbattle_multiplayer.activity.TankMenuActivity;
import com.jiokye.tankbattle_multiplayer.puzzles.PuzzCompleteDialog;
import com.jiokye.tankbattle_multiplayer.puzzles.PuzzDialog;
import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;
import com.jiokye.tankbattle_multiplayer.utility.CONST;
import com.jiokye.tankbattle_multiplayer.utility.MessageRegister;
import com.jiokye.tankbattle_multiplayer.utility.ServiceListener;
import com.jiokye.tankbattle_multiplayer.utility.SettingsManager;
import com.jiokye.tankbattle_multiplayer.utility.TankToast;
import com.jiokye.tankbattle_multiplayer.utility.Utils;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NumberPuzzleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NumberPuzzleFragment extends Fragment implements View.OnTouchListener {

    SharedPreferences settings;
    View rootView;
    Activity activity;

    LinearLayout gamecountView, goldcountView;

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

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public NumberPuzzleFragment() {
        // Required empty public constructor
    }

    public NumberPuzzleFragment(Activity activity, int level) {
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
    public static NumberPuzzleFragment newInstance(String param1, String param2) {
        NumberPuzzleFragment fragment = new NumberPuzzleFragment();
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

        View decorView = getActivity().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        rootView = inflater.inflate(R.layout.fragment_number_puzzle, container, false);
        settings = activity.getSharedPreferences("TankSettings", 0);

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

//        MessageRegister.getInstance().setServiceListener(this);

        switch (level){
            case 1:
//                rootView = inflater.inflate(R.layout.fragment_number_puzzle_3, container, false);
                resID = R.drawable.puzz3;
//                fragViewID = R.id.fragView3;
                Rows = Cols = 3;
                rwdDrawable = ResourcesCompat.getDrawable(activity.getResources(),R.drawable.lvl1rwd,null);

                break;
            case 2:
//                rootView = inflater.inflate(R.layout.fragment_number_puzzle_4, container, false);
                resID = R.drawable.puzz4;
//                fragViewID = R.id.fragView4;
                Rows = Cols = 4;
                rwdDrawable = ResourcesCompat.getDrawable(activity.getResources(),R.drawable.lvl2rwd,null);
                break;
            case 3:
//                rootView = inflater.inflate(R.layout.fragment_number_puzzle_5, container, false);
                resID = R.drawable.puzz5;
//                fragViewID = R.id.fragView5;
                Rows = Cols = 5;
                rwdDrawable = ResourcesCompat.getDrawable(activity.getResources(),R.drawable.lvl3rwd,null);
                break;
            case 4:
//                rootView = inflater.inflate(R.layout.fragment_number_puzzle_6, container, false);
                resID = R.drawable.puzz6;
//                fragViewID = R.id.fragView6;
                Rows = Cols = 6;
                rwdDrawable = ResourcesCompat.getDrawable(activity.getResources(),R.drawable.game6h,null);
                break;
        }

        rwdImg.setBackground(rwdDrawable);





//        rootView = inflater.inflate(R.layout.fragment_number_puzzle, container, false);
        rootView.findViewById(R.id.fragView3).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });



        boardrows = rootView.findViewById(R.id.rows);

        LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT);
        cellParams.weight = 1;

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0);
        rowParams.weight = 1;
        rowParams.gravity = Gravity.CENTER;

        for(int row = 0; row < Rows; row++) {
            LinearLayout columns = new LinearLayout(getContext());
            columns.setGravity(Gravity.CENTER);
            columns.setOrientation(LinearLayout.HORIZONTAL);
            for(int col = 0; col < Cols; col++) {
                ImageView cell = new ImageView(getContext());
                cell.setLayoutParams(cellParams);
                cell.setTag(String.valueOf(row*Cols + col));
                columns.addView(cell);
            }
            boardrows.addView(columns,rowParams);
        }

        resetBtn = rootView.findViewById(R.id.resetBtn);
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

        Bitmap boardImg = BitmapFactory.decodeResource(activity.getResources(),resID);
        boardView = new BoardView((AppCompatActivity)activity, boardImg, Rows, Cols);


//        SoundManager.getInstance();
//        SoundManager.initSounds(this);
//        int[] sounds = {R.raw.win1, R.raw.win2, R.raw.movecard, R.raw.wrongmove};
//        SoundManager.loadSounds(sounds);

        update_board();

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

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int pos = Integer.parseInt(String.valueOf(view.getTag()));
                int row = (int)(pos/Rows);
                int col = pos % Rows;
                int res = boardView.move_cellAt(row,col);
//                if(res == 0 && sound) {
//                    SoundManager.playSound(Sounds.NUM_PUZZ.MOVE);
//                }
//                else if (res == 1 && sound) {
//                    SoundManager.playSound(Sounds.NUM_PUZZ.WRONGMOVE);
//                }
                update_board();
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void reset() {

        if (started) {

            if(ended) {
//                if(confetti != null) {
//                    confetti.cancel_confetti();
//                }
                updateTimer.cancel();
                boardView.shuffle();
                start_timer();
                startTime = System.currentTimeMillis();
                update_board();
                ended = false;
            }
            else {
                showDialog("Really?").setOnDismissPuzzleDialog(new PuzzDialog.OnDismissPuzzleDialog() {
                    @Override
                    public void finish(boolean ok) {
                        if(ok) {
                            updateTimer.cancel();
                            boardView.shuffle();
                            start_timer();
                            startTime = System.currentTimeMillis();
                            update_board();
                        }
                    }
                });
            }
        }
        else{
//            Rows = boardrows.getChildCount();
            for(int row = 0; row < Rows; row++) {
                LinearLayout current_row =  (LinearLayout)boardrows.getChildAt(row);
//                Cols = current_row.getChildCount();
                for(int col = 0; col < Cols; col++) {
                    ImageView card = (ImageView) current_row.getChildAt(col);
                    card.setOnTouchListener(this);
                    card.setEnabled(true);
//                    if(confetti != null) {
//                        confetti.cancel_confetti();
//                    }
                }
            }
//            ImageView resetBtn = (Button)view;
            resetBtn.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.resetbtn));
//            view.invalidate();

            boardView.shuffle();
            start_timer();
            startTime = System.currentTimeMillis();
            update_board();
            started = true;
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

    protected void update_board() {
        for(int row = 0; row < boardrows.getChildCount(); row++) {
            LinearLayout current_row =  (LinearLayout)boardrows.getChildAt(row);
            for(int col = 0; col < current_row.getChildCount(); col++) {
                ImageView card = (ImageView) current_row.getChildAt(col);
                card.setBackground(boardView.updateCellAt(row,col));
            }
        }
        if(started) {
            boolean won = boardView.check_board(sound);
            if (won) {
                ended = true;
//                if(sound) {
//                    SoundManager.playSound(Sounds.NUM_PUZZ.WIN1);
//                    SoundManager.playSound(Sounds.NUM_PUZZ.WIN2);
//                }
//                String text = "You Won!";
//                int duration = Toast.LENGTH_LONG;
//                TankToast.showTankToast(activity, text, 3000);
                updateTimer.cancel();
//                confetti = new Confetti(this);
//                confetti.generate_confetti(confettiEmitter);

                Rows = boardrows.getChildCount();
                for(int row = 0; row < Rows; row++) {
                    LinearLayout current_row =  (LinearLayout)boardrows.getChildAt(row);
                    Cols = current_row.getChildCount();
                    for(int col = 0; col < Cols; col++) {
                        ImageView card = (ImageView) current_row.getChildAt(col);
                        card.setEnabled(false);
                    }
                }
                started = false;
                doGameWon();
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

//    @Override
//    public void onServiceMessageReceived(int games, long time_left, boolean h6) {
//        if(opened){
//            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss", Locale.ENGLISH);
//            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//            gamecountTxt.setText(String.valueOf(games));
//
//            //game is at maximum, no need to show time and no need to show 6h if this is not a 6h message
//            if(games >= CONST.Tank.MAX_GAME_COUNT && !h6) {
////                Log.d("SERVICE MESSAGE MENU", String.valueOf(games) + " " + time_left + " false");
//                retryTmrTxt.setText("");
//                gamecountImg.setBackground(ResourcesCompat.getDrawable(this.getResources(),R.drawable.game,null));
//            }
//            //this is a 6h message
//            else if(h6) {
////                Log.d("SERVICE MESSAGE MENU", String.valueOf(games) + " " + time_left + " true");
//                sdf = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
//                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//                retryTmrTxt.setText(sdf.format(time_left));
//                gamecountImg.setBackground(ResourcesCompat.getDrawable(this.getResources(),R.drawable.game6h,null));
//            }
//            // this is not a 6h message and game is less than maximum
//            else {
////                Log.d("SERVICE MESSAGE MENU", String.valueOf(games) + " " + time_left + " false");
//                retryTmrTxt.setText(sdf.format(time_left));
//                gamecountImg.setBackground(ResourcesCompat.getDrawable(this.getResources(),R.drawable.game,null));
//            }
//        }
//    }
}