package com.example.blockpuzzle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class GameView extends View {

    private int[][][] blockShapes = {
            {{1}},                     // single
            {{1, 1}},                  // horizontal 2
            {{1}, {1}},                // vertical 2
            {{1, 1}, {1, 1}},           // square
            {{1, 1, 1}},                // horizontal 3
            {{1}, {1}, {1}}             // vertical 3
    };


    private com.example.blockpuzzlegame.Block[] availableBlocks = new com.example.blockpuzzlegame.Block[3];
    private com.example.blockpuzzlegame.Block draggingBlock = null;


    private int rows = 8, cols = 8;
    private int cellSize;
    private int[][] grid = new int[rows][cols];

    private Paint gridPaint, blockPaint;

    private float blockX = 100, blockY = 1200;
    private boolean dragging = false;

    // ✅ REQUIRED constructor for XML
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // ✅ Optional but recommended
    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // Optional for Java usage
    public GameView(Context context) {
        super(context);
        init();
    }

    private void init() {
        gridPaint = new Paint();
        gridPaint.setColor(Color.GRAY);
        gridPaint.setStrokeWidth(3);

        blockPaint = new Paint();
        blockPaint.setColor(Color.BLUE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        cellSize = getWidth() / cols;

        drawGrid(canvas);
        drawFilledCells(canvas);
        drawBlocks(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        generateBlocks();
    }


    private void drawGrid(Canvas canvas) {
        for (int i = 0; i <= rows; i++) {
            canvas.drawLine(0, i * cellSize,
                    cols * cellSize, i * cellSize, gridPaint);
        }

        for (int j = 0; j <= cols; j++) {
            canvas.drawLine(j * cellSize, 0,
                    j * cellSize, rows * cellSize, gridPaint);
        }
    }

    private void drawFilledCells(Canvas canvas) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == 1) {
                    canvas.drawRect(
                            j * cellSize,
                            i * cellSize,
                            (j + 1) * cellSize,
                            (i + 1) * cellSize,
                            blockPaint
                    );
                }
            }
        }
    }

    private void drawBlocks(Canvas canvas) {
        for (com.example.blockpuzzlegame.Block block : availableBlocks) {
            drawSingleBlock(canvas, block);
        }
    }

    private void drawSingleBlock(Canvas canvas, com.example.blockpuzzlegame.Block block) {
        blockPaint.setColor(block.color);

        for (int i = 0; i < block.shape.length; i++) {
            for (int j = 0; j < block.shape[0].length; j++) {
                if (block.shape[i][j] == 1) {
                    canvas.drawRect(
                            block.x + j * cellSize,
                            block.y + i * cellSize,
                            block.x + (j + 1) * cellSize,
                            block.y + (i + 1) * cellSize,
                            blockPaint
                    );
                }
            }
        }
    }





    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                for (com.example.blockpuzzlegame.Block block : availableBlocks) {
                    if (isTouchInsideBlock(event, block)) {
                        draggingBlock = block;
                        break;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (draggingBlock != null) {
                    draggingBlock.x = event.getX() - cellSize;
                    draggingBlock.y = event.getY() - cellSize;
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (draggingBlock != null) {
                    placeBlock(draggingBlock);
                    draggingBlock = null;
                    invalidate();
                }
                break;
        }
        return true;
    }


    private void placeBlock(com.example.blockpuzzlegame.Block block) {
        int col = Math.round(block.x / cellSize);
        int row = Math.round(block.y / cellSize);

        if (canPlace(block, row, col)) {
            for (int i = 0; i < block.shape.length; i++) {
                for (int j = 0; j < block.shape[0].length; j++) {
                    if (block.shape[i][j] == 1) {
                        grid[row + i][col + j] = 1;
                    }
                }
            }
            generateBlocks(); // regenerate after placing
        }
    }

    private boolean canPlace(com.example.blockpuzzlegame.Block block, int row, int col) {
        for (int i = 0; i < block.shape.length; i++) {
            for (int j = 0; j < block.shape[0].length; j++) {

                if (block.shape[i][j] == 1) {
                    int r = row + i;
                    int c = col + j;

                    if (r < 0 || r >= rows || c < 0 || c >= cols)
                        return false;

                    if (grid[r][c] == 1)
                        return false;
                }
            }
        }
        return true;
    }



    private void generateBlocks() {
        for (int i = 0; i < 3; i++) {
            int index = (int) (Math.random() * blockShapes.length);
            availableBlocks[i] = new com.example.blockpuzzlegame.Block(blockShapes[index], Color.BLUE);
            availableBlocks[i].x = 100 + i * 300;
            availableBlocks[i].y = getHeight() - 300;
        }
    }

    private boolean isTouchInsideBlock(MotionEvent e, com.example.blockpuzzlegame.Block block) {
        float width = block.getWidth() * cellSize;
        float height = block.getHeight() * cellSize;

        return e.getX() >= block.x && e.getX() <= block.x + width &&
                e.getY() >= block.y && e.getY() <= block.y + height;
    }

}
