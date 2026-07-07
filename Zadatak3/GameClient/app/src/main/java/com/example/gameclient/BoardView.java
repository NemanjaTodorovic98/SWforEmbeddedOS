package com.example.gameclient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class BoardView extends View {

    private static final int ROWS = 6;
    private static final int COLS = 7;

    private final int[][] cells = new int[ROWS][COLS];
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private OnColumnClickListener columnClickListener;

    public interface OnColumnClickListener {
        void onColumnClick(int col);
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnColumnClickListener(OnColumnClickListener l) {
        this.columnClickListener = l;
    }

    public void setCell(int row, int col, int player) {
        cells[row][col] = player;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float cellSize = getWidth() / (float) COLS;

        // Pozadina table
        paint.setColor(Color.BLUE);
        canvas.drawRect(0, 0, getWidth(), cellSize * ROWS, paint);

        // Celije
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                switch (cells[r][c]) {
                    case 1: paint.setColor(Color.RED); break;
                    case 2: paint.setColor(Color.YELLOW); break;
                    default: paint.setColor(Color.WHITE); break;
                }
                float cx = c * cellSize + cellSize / 2;
                float cy = r * cellSize + cellSize / 2;
                canvas.drawCircle(cx, cy, cellSize * 0.4f, paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && columnClickListener != null) {
            float cellSize = getWidth() / (float) COLS;
            int col = (int) (event.getX() / cellSize);
            if (col >= 0 && col < COLS) columnClickListener.onColumnClick(col);
        }
        return true;
    }

    public void resetBoard() {
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                cells[r][c] = 0;
        invalidate();
    }
}