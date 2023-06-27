package com.jiokye.tankbattle_multiplayer.dialog.puzzles.sokoban;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.SoundPool;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.utility.CONST;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class BoardView {
    AppCompatActivity activity;
    protected Bitmap image;


    protected ArrayList<ArrayList<Drawable>> cellImage;

    protected final int rowCount = 20;
    protected final int colCount = 20;


    private int px,py;
    private ArrayList<BoxStore> storeLoc;
    private int[][] boardLayout;
    private LinearLayout boardView;
    private Drawable wall, box, strLoc;
    private Drawable[] sok;

    private SoundPool sounds;
    private int movesound;
    private int wrong_movesound;
    private int win1sound;
    private int win2sound;


    int level,randLevel;


    public BoardView(AppCompatActivity activity, LinearLayout boardView, int level)  {
        this.activity = activity;
        this.image = image;

        this.boardView = boardView;

//        sounds = new SoundPool(5, AudioManager.STREAM_MUSIC,0);
//        movesound = sounds.load(activity, R.raw.movecard, 1);
//        wrong_movesound = sounds.load(activity, R.raw.wrongmove, 1);
//        win1sound = sounds.load(activity, R.raw.win1, 1);
//        win2sound = sounds.load(activity, R.raw.win2, 1);

        wall = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.sokwall,null);
        box = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.sokbox,null);
        strLoc = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.sokloc,null);
        sok = new Drawable[4];
        sok[CONST.Direction.UP] = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.sokup,null);
        sok[CONST.Direction.LEFT] = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.sokleft,null);
        sok[CONST.Direction.DOWN] = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.sokdown,null);
        sok[CONST.Direction.RIGHT] = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.sokright,null);

        this.level = level;

        randLevel = (int)(1+(Math.random()*50));
        randLevel = Math.min(randLevel,50);
        randLevel = Math.max(randLevel,1);

        loadLevel(level);

    }

    public void resetBoard() {
        loadLevel(level);
    }



    private void loadLevel(int level) {
        boardLayout = new int[20][20];
        storeLoc = new ArrayList<>();
        BufferedReader reader;
//        randLevel = 1;
        int row_count = 0;
        try {
            InputStream inputStream = activity.getAssets().open("sokstages/" + level + "/" + randLevel);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();

            while(line != null){
                for (int col_count = 0; col_count < line.length(); col_count++){
                    char c = line.charAt(col_count);
                    switch (c){
                        case '#' :
                            //wall
                            boardLayout[row_count][col_count] = 1;
                            ((LinearLayout)boardView.getChildAt(row_count)).getChildAt(col_count).setBackground(wall);
                            ((LinearLayout)boardView.getChildAt(row_count)).getChildAt(col_count).setAlpha(1f);
                            break;
                        case '@' :
                            // store point
                            storeLoc.add(new BoxStore(col_count, row_count));
                            boardLayout[row_count][col_count] = 0;
                            ((LinearLayout)boardView.getChildAt(row_count)).getChildAt(col_count).setBackground(strLoc);
                            ((LinearLayout)boardView.getChildAt(row_count)).getChildAt(col_count).setAlpha(1f);
                            break;
                        case '.' :
                            //empty
                            boardLayout[row_count][col_count] = 0;
                            ((LinearLayout)boardView.getChildAt(row_count)).getChildAt(col_count).setBackgroundColor(Color.BLACK);
                            ((LinearLayout)boardView.getChildAt(row_count)).getChildAt(col_count).setAlpha(1f);
                            break;
                        case '&' :
                            // box
                            boardLayout[row_count][col_count] = 2;
                            ((LinearLayout)boardView.getChildAt(row_count)).getChildAt(col_count).setBackground(box);
                            ((LinearLayout)boardView.getChildAt(row_count)).getChildAt(col_count).setAlpha(1f);
                            break;
                        case '$' :
                            px = col_count;
                            py = row_count;
                            boardLayout[row_count][col_count] = 0;
                            ((LinearLayout)boardView.getChildAt(row_count)).getChildAt(col_count).setBackground(sok[CONST.Direction.UP]);
                            ((LinearLayout)boardView.getChildAt(row_count)).getChildAt(col_count).setAlpha(1f);
                            break;
                        case '~' :
                            // store point and box
                            boardLayout[row_count][col_count] = 2;
                            BoxStore boxStore = new BoxStore(col_count, row_count);
                            boxStore.setFilled(true);
                            storeLoc.add(boxStore);
                            ((LinearLayout)boardView.getChildAt(row_count)).getChildAt(col_count).setBackground(box);
                            ((LinearLayout)boardView.getChildAt(row_count)).getChildAt(col_count).setAlpha(0.7f);
                            break;
                    }
                }

                ++row_count;
                line = reader.readLine();
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void move(int dir) {
        int row = py, col = px;
        switch (dir) {
            case CONST.Direction.UP:
                if(row-1 >= 0 && boardLayout[row-1][col] == 0){ // up is empty
                    move_sokAt(row,col,dir);
                }
                else if(row-2 >= 0 && boardLayout[row-2][col] == 0 && boardLayout[row-1][col] == 2) { //up is box and up of box is empty
                    move_cellAt(row-1,col,dir);
                    move_sokAt(row,col,dir);
                }
                break;
            case CONST.Direction.LEFT:
                if(col-1 >= 0 && boardLayout[row][col-1] == 0){
                    move_sokAt(row,col,dir);
                }
                else if(col-2 >= 0 && boardLayout[row][col-2] == 0 && boardLayout[row][col-1] == 2) {
                    move_cellAt(row,col-1,dir);
                    move_sokAt(row,col,dir);
                }
                break;
            case CONST.Direction.DOWN:
                if(row+1 < rowCount && boardLayout[row+1][col] == 0){
                    move_sokAt(row,col,dir);
                }
                else if(row+2 < rowCount && boardLayout[row+2][col] == 0 && boardLayout[row+1][col] == 2) {
                    move_cellAt(row+1,col,dir);
                    move_sokAt(row,col,dir);
                }
                break;
            case CONST.Direction.RIGHT:
                if(col+1 < colCount && boardLayout[row][col+1] == 0){
                    move_sokAt(row,col,dir);
                }
                else if(col+2 < colCount && boardLayout[row][col+2] == 0 && boardLayout[row][col+1] == 2) {
                    move_cellAt(row,col+1,dir);
                    move_sokAt(row,col,dir);
                }
                break;
        }

    }

    protected void move_cellAt(int row, int col, int dir) {
        int toRow = row, toCol = col;
        switch (dir) {
            case CONST.Direction.UP:
                toRow--;
                break;
            case CONST.Direction.LEFT:
                toCol--;
                break;
            case CONST.Direction.DOWN:
                toRow++;
                break;
            case CONST.Direction.RIGHT:
                toCol++;
                break;
        }

        ((ImageView)((LinearLayout)boardView.getChildAt(toRow)).getChildAt(toCol)).setBackground(box);
        if(isStoreLoc(toRow,toCol)) {
//            toImg.setAlpha(0.6f);
            ((ImageView)((LinearLayout)boardView.getChildAt(toRow)).getChildAt(toCol)).setAlpha(0.6f);
        }
        else {
            ((ImageView)((LinearLayout)boardView.getChildAt(toRow)).getChildAt(toCol)).setAlpha(1f);
        }

        if(isStoreLoc(row,col)) {
            ((LinearLayout)boardView.getChildAt(row)).getChildAt(col).setBackground(strLoc);
        }
        else {
            ((LinearLayout) boardView.getChildAt(row)).getChildAt(col).setBackgroundColor(Color.BLACK);
        }
        ((ImageView)((LinearLayout)boardView.getChildAt(row)).getChildAt(col)).setAlpha(1f);

        boardLayout[row][col] = 0;
        boardLayout[toRow][toCol] = 2;

    }

    protected void move_sokAt(int row, int col, int dir) {
        int toRow = row, toCol = col;
        switch (dir) {
            case CONST.Direction.UP:
                toRow--;
                break;
            case CONST.Direction.LEFT:
                toCol--;
                break;
            case CONST.Direction.DOWN:
                toRow++;
                break;
            case CONST.Direction.RIGHT:
                toCol++;
                break;
        }

        px = toCol;
        py = toRow;

        ImageView toImg = (ImageView)((LinearLayout)boardView.getChildAt(toRow)).getChildAt(toCol);
        toImg.setBackground(sok[dir]);
        if(isStoreLoc(row,col)) {
            ((LinearLayout)boardView.getChildAt(row)).getChildAt(col).setBackground(strLoc);
        }
        else {
            ((LinearLayout) boardView.getChildAt(row)).getChildAt(col).setBackgroundColor(Color.BLACK);
        }
    }

    boolean isStoreLoc(int row, int col) {
        for(BoxStore b : storeLoc) {
            if(b.x == col && b.y == row) {
                return true;
            }
        }
        return false;
    }

    public boolean check_board(boolean sound) {
        for(BoxStore b : storeLoc) {
            if(boardLayout[b.y][b.x] != 2) {
                return false;
            }
        }
        return true;
    }
}
