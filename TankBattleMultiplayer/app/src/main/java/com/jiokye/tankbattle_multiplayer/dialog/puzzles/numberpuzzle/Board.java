package com.jiokye.tankbattle_multiplayer.dialog.puzzles.numberpuzzle;

public class Board {
    private int[][] cells;
    private int num_cells;
    private  int rows;
    private  int cols;
    private int endCell;

    public Board(int row, int col) {
        this.init_board(row,col);
    }

    public void init_board(int rows, int cols) {
        this.cells = new int[rows][cols];
        this.num_cells = rows*cols;
        this.rows = rows;
        this.cols = cols;
        this.endCell = rows*cols - 1;

        int cell = 0;
        for(int i=0; i < rows; i++) {
            for(int j=0; j < cols; j++) {
                cells[i][j] = cell;
                cell++;
            }
        }
    }

    public int move_cell(int row, int col) {
        int current_cell = cells[row][col];
        if (current_cell == endCell) {
            return -1;
        }
        else if(row != 0 && cells[row-1][col] == endCell) {
            cells[row][col] = cells[row-1][col];
            cells[row-1][col] = current_cell;
            return 0;
        }
        else if(row != rows-1 && cells[row+1][col] == endCell) {
            cells[row][col] = cells[row+1][col];
            cells[row+1][col] = current_cell;
            return 0;
        }
        else if(col != 0 && cells[row][col-1] == endCell) {
            cells[row][col] = cells[row][col-1];
            cells[row][col-1] = current_cell;
            return 0;
        }
        else if(col != cols-1 && cells[row][col+1] == endCell) {
            cells[row][col] = cells[row][col+1];
            cells[row][col+1] = current_cell;
            return 0;
        }
        return 1;

    }

    public void shuffle_board() {
        int temp;
        for(int i=0; i < num_cells; i++) {
            int cell = (int) (Math.random()*num_cells);
            temp = cells[(int)(cell/rows)][(int)(cell%rows)];
            cells[(int)(cell/rows)][(int)(cell%rows)] = cells[(int)(i/rows)][(int)(i%rows)];
            cells[(int)(i/rows)][(int)(i%rows)] = temp;
        }
    }

    public boolean iscomplete() {
        int cell = 0;
        for(int row=0; row < rows; row++) {
            for(int col=0; col < cols; col++) {
                if (cells[row][col] != cell) {
                    return false;
                }
                cell++;
            }
        }
        return true;
    }

    public int get_rows() {
        return this.rows;
    }

    public int get_cols() {
        return this.cols;
    }

    public int get_num_cells() {
        return this.num_cells;
    }

    public int get_cellAt(int row,int col) {
        return cells[row][col];
    }
}
