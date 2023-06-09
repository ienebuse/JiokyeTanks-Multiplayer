package com.jiokye.tankbattle_multiplayer.puzzles.numberpuzzle;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;

import androidx.appcompat.app.AppCompatActivity;

import com.jiokye.tankbattle_multiplayer.R;

import java.util.ArrayList;

public class BoardView {
    AppCompatActivity activity;
    protected Board boardmap;
    protected Bitmap image;
    protected Bitmap[] boardView;

    protected ArrayList<ArrayList<Drawable>> cellImage;

    protected final int rowCount;
    protected final int colCount;

    protected final int WIDTH;
    protected final int HEIGHT;

    protected final int width;
    protected final int height;

    private SoundPool sounds;
    private int movesound;
    private int wrong_movesound;
    private int win1sound;
    private int win2sound;


    public BoardView(AppCompatActivity activity, Bitmap image, int rowCount, int colCount)  {
        this.activity = activity;
        this.image = image;
        this.rowCount= rowCount;
        this.colCount= colCount;

        this.WIDTH = image.getWidth();
        this.HEIGHT = image.getHeight();

        this.width = this.WIDTH/ colCount;
        this.height= this.HEIGHT/ rowCount;

        this.boardmap = new Board(rowCount,colCount);
        this.boardView = new Bitmap[boardmap.get_num_cells()];

        extractCellImages();

//        sounds = new SoundPool(5, AudioManager.STREAM_MUSIC,0);
//        movesound = sounds.load(activity, R.raw.movecard, 1);
//        wrong_movesound = sounds.load(activity, R.raw.wrongmove, 1);
//        win1sound = sounds.load(activity, R.raw.win1, 1);
//        win2sound = sounds.load(activity, R.raw.win2, 1);
    }

    void extractCellImages() {
        cellImage = new ArrayList<>();
        for(int row = 0; row < this.rowCount; row++) {
            ArrayList<Drawable> rowImg = new ArrayList<>();
            for(int col = 0; col < this.colCount; col++) {
                Bitmap cellMB = Bitmap.createBitmap(image, col* width, row* height ,width,height);
                Drawable d = new BitmapDrawable(activity.getResources(), cellMB);
                rowImg.add(d);
            }
            cellImage.add(rowImg);
        }
    }

    protected Drawable createCellAt(int row, int col)  {
//        Bitmap cellImage = Bitmap.createBitmap(image, col* width, row* height ,width,height);
//        Drawable d = new BitmapDrawable(activity.getResources(), cellImage);
//        return d;
        return cellImage.get(row).get(col);
    }

    protected int move_cellAt(int row, int col) {
        int res = this.boardmap.move_cell(row,col);
        return res;
    }

    public void shuffle(){
        boardmap.shuffle_board();
    }

    public boolean check_board(boolean sound) {
        boolean complete = boardmap.iscomplete();
        return complete;
    }

    public Drawable updateCellAt(int row,int col) {
        int cell = boardmap.get_cellAt(row,col);
        int cell_row = (int)(cell/boardmap.get_rows());
        int cell_col = (int)(cell%boardmap.get_rows());

        Drawable d = createCellAt(cell_row,cell_col);
        if(cell_row == boardmap.get_rows()-1 && cell_col == boardmap.get_cols()-1) {
            d.setAlpha(0);
        }
        return d;
    }
}
