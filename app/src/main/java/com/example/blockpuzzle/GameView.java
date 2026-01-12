package com.example.blockpuzzle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.blockpuzzlegame.Block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameView extends View {

    private int score = 0;
    private boolean isGameOver = false;
    private int clearAlpha = 255;

    private int gridFillColor = Color.parseColor("#455A64"); // neutral dark
    private int highlightColor = Color.parseColor("#FFD54F"); // gold highlight

    private boolean[] highlightRows;
    private boolean[] highlightCols;


    private SoundManager soundManager;

    private List<FloatingText> floatingTexts = new ArrayList<>();
    private Paint floatingTextPaint;




    private int[][][] blockShapes = {
            {{1}},                     // single
            {{1, 1}},                  // horizontal 2
            {{1}, {1}},                // vertical 2
            {{1, 1}, {1, 1}},           // square
            {{1, 1, 1}},                // horizontal 3
            {{1}, {1}, {1}}             // vertical 3
    };


    private int[] colors = {
            Color.parseColor("#F44336"), // Red
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#00BCD4")  // Cyan
    };



    private com.example.blockpuzzlegame.Block[] availableBlocks = new com.example.blockpuzzlegame.Block[3];
    private com.example.blockpuzzlegame.Block draggingBlock = null;


    private int rows = 8, cols = 8;
    private int cellSize;
    private int[][] grid = new int[rows][cols];

    private Paint gridPaint, blockPaint;

    private float blockX = 100, blockY = 1200;
    private boolean dragging = false;

    public interface ScoreListener {
        void onScoreChanged(int score);
    }

    private ScoreListener scoreListener;

    public void setScoreListener(ScoreListener listener) {
        this.scoreListener = listener;
    }


    // ✅ REQUIRED constructor for XML
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        soundManager = new SoundManager(context);

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

        highlightRows = new boolean[rows];
        highlightCols = new boolean[cols];

        floatingTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        floatingTextPaint.setColor(Color.WHITE);
        cellSize = getWidth() / cols;
        floatingTextPaint.setTextSize(cellSize * 0.8f);
        floatingTextPaint.setTextAlign(Paint.Align.CENTER);
        floatingTextPaint.setFakeBoldText(true);



    }

    @Override
    protected void onDraw(Canvas canvas) {

       /* canvas.save();
        canvas.scale(animationScale, animationScale,
                getWidth() / 2f,
                getHeight() / 2f); */


        super.onDraw(canvas);

        cellSize = getWidth() / cols;


        drawGrid(canvas);
        drawFilledCells(canvas);
        drawBlocks(canvas);

        long now = System.currentTimeMillis();

        Iterator<FloatingText> iterator = floatingTexts.iterator();

        while (iterator.hasNext()) {

            FloatingText ft = iterator.next();
            long elapsed = now - ft.startTime;

            if (elapsed > 800) {
                iterator.remove();
                continue;
            }

            float progress = elapsed / 800f;

            float currentY = ft.y - (progress * cellSize);
            int alpha = (int) (255 * (1 - progress));

            floatingTextPaint.setColor(ft.color);
            floatingTextPaint.setAlpha(alpha);

            canvas.drawText(ft.text, ft.x, currentY, floatingTextPaint);
        }




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

        float radius = cellSize * 0.12f;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                if (grid[i][j] == 1) {

                    if (highlightRows[i] || highlightCols[j]) {
                        blockPaint.setColor(highlightColor); // completed
                    } else {
                        blockPaint.setColor(gridFillColor); // normal
                    }

                    float left = j * cellSize;
                    float top = i * cellSize;

                    canvas.drawRoundRect(
                            left,
                            top,
                            left + cellSize,
                            top + cellSize,
                            radius,
                            radius,
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

    private void drawSingleBlock(Canvas canvas, Block block) {
        blockPaint.setColor(block.color);

        float radius = cellSize * 0.12f; // subtle rounding


        for (int i = 0; i < block.shape.length; i++) {
            for (int j = 0; j < block.shape[0].length; j++) {

                if (block.shape[i][j] == 1) {
                    float left = block.x + j * cellSize;
                    float top = block.y + i * cellSize;
                    float right = left + cellSize;
                    float bottom = top + cellSize;

                    canvas.drawRoundRect(
                            left,
                            top,
                            right,
                            bottom,
                            radius,
                            radius,
                            blockPaint
                    );
                }
            }
        }
    }






    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isGameOver) return false;


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

            soundManager.playPlace();
            clearCompletedLines();
            generateBlocks(); // regenerate after placing
        }
        else {
            soundManager.playDrop();   //  Invalid placement
        }


        if (!canAnyBlockFit()) {
            isGameOver = true;
            soundManager.playGameOver();
            if (gameOverListener != null) {
                gameOverListener.onGameOver(score);
            }
        }

        animatePlacement();

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
            int shapeIndex = (int) (Math.random() * blockShapes.length);
            int colorIndex = (int) (Math.random() * colors.length);

            availableBlocks[i] = new Block(
                    blockShapes[shapeIndex],
                    colors[colorIndex]
            );

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

    private void clearCompletedLines() {


        for (int i = 0; i < rows; i++) highlightRows[i] = false;
        for (int j = 0; j < cols; j++) highlightCols[j] = false;


        // Detect full rows
        for (int i = 0; i < rows; i++) {
            boolean full = true;
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) highlightRows[i] = true;
        }

// Detect full columns
        for (int j = 0; j < cols; j++) {
            boolean full = true;
            for (int i = 0; i < rows; i++) {
                if (grid[i][j] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) highlightCols[j] = true;
        }

        postDelayed(this::clearHighlightedLines, 120);


        int clearedLines = 0;

// Count cleared rows
        for (int i = 0; i < rows; i++) {
            if (highlightRows[i]) {
                clearedLines++;
            }
        }

// Count cleared columns
        for (int j = 0; j < cols; j++) {
            if (highlightCols[j]) {
                clearedLines++;
            }
        }

        // Score logic
        if (clearedLines > 0) {

            int gainedScore = clearedLines * 10;

            soundManager.playClear();

            score += gainedScore;

            showFloatingScore(gainedScore); // ⭐ ADD THIS

            if (scoreListener != null) {
                scoreListener.onScoreChanged(score);
            }
        }


        clearAlpha = 100;
        postDelayed(() -> {
            clearAlpha = 255;
            invalidate();
        }, 120);


        invalidate();
    }

    private boolean canAnyBlockFit() {

        for (com.example.blockpuzzlegame.Block block : availableBlocks) {

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {

                    if (canPlace(block, row, col)) {
                        return true; // At least one move exists
                    }
                }
            }
        }
        return false; // No move possible
    }


    public interface GameOverListener {
        void onGameOver(int finalScore);

    }

    private GameOverListener gameOverListener;

    public void setGameOverListener(GameOverListener listener) {
        this.gameOverListener = listener;

    }


    private void animatePlacement() {
        /* animationScale = 0.8f;

        postDelayed(() -> {
            animationScale = 1f;
            invalidate();
        }, 100); */
    }

    private void clearHighlightedLines() {

        int cleared = 0;

        for (int i = 0; i < rows; i++) {
            if (highlightRows[i]) {
                for (int j = 0; j < cols; j++) grid[i][j] = 0;
                cleared++;
            }
        }

        for (int j = 0; j < cols; j++) {
            if (highlightCols[j]) {
                for (int i = 0; i < rows; i++) grid[i][j] = 0;
                cleared++;
            }
        }

       /* if (cleared > 0) {
            score += cleared * 10;
            if (scoreListener != null)
                scoreListener.onScoreChanged(score);
        } */

        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        soundManager.release();
    }

    private class FloatingText {
        float x, y;
        String text;
        int alpha = 255;
        long startTime;

        int color;
    }

    private void showFloatingScore(int scoreValue) {

        FloatingText ft = new FloatingText();

        ft.text = "+" + scoreValue;
        ft.x = (cols * cellSize) / 2f;
        ft.y = (rows * cellSize) / 2f;
        ft.startTime = System.currentTimeMillis();
        ft.color=Color.parseColor("#FFEB3B");
        floatingTexts.add(ft);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        cellSize = w / cols;

        floatingTextPaint.setTextSize(cellSize * 0.8f);

        generateBlocks();
    }



}
