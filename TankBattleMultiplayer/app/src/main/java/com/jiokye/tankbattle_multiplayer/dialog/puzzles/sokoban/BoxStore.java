package com.jiokye.tankbattle_multiplayer.dialog.puzzles.sokoban;

import android.graphics.Point;

public class BoxStore extends Point {
    boolean filled = false;
    BoxStore(int x, int y) {
        super(x,y);
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    public boolean isFilled() {
        return filled;
    }
}
