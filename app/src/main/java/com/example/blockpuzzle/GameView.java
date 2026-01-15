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

    private float cellGap; // space between cells
    private float cellPadding;

    private float slotRadius;
    private float blockRadius;
    private float boardRadius;

    private float dragOffsetY;


    private float gridMargin;


    private float slotGap;



    private int emptySlotColor = Color.parseColor("#1E233A");




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

    private static final int EMPTY = -1;


    private Paint gridPaint, blockPaint;

    private Paint boardBorderPaint;

    private Paint boardPaint;
    private Paint slotPaint;


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


        boardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boardPaint.setColor(Color.parseColor("#2B2E4A")); // dark board

        boardBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boardBorderPaint.setStyle(Paint.Style.STROKE);
        boardBorderPaint.setStrokeWidth(getResources().getDisplayMetrics().density * 6);
        boardBorderPaint.setColor(Color.parseColor("#0097A7")); // border color


        slotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        slotPaint.setColor(Color.parseColor("#1F2238")); // empty slot

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = EMPTY;
            }
        }



    }

    @Override
    protected void onDraw(Canvas canvas) {

       /* canvas.save();
        canvas.scale(animationScale, animationScale,
                getWidth() / 2f,
                getHeight() / 2f); */


        super.onDraw(canvas);



        drawBoard(canvas);
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





    private void drawFilledCells(Canvas canvas) {

        float radius = blockRadius;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                if (grid[i][j] != EMPTY) {

                    int baseColor = (highlightRows[i] || highlightCols[j])
                            ? highlightColor
                            : grid[i][j];


                    float left   = gridMargin + j * cellSize + slotGap;
                    float top    = gridMargin + i * cellSize + slotGap;
                    float right  = gridMargin + (j + 1) * cellSize - slotGap;
                    float bottom = gridMargin + (i + 1) * cellSize - slotGap;



                    // Gradient (3D effect)
                    blockPaint.setShader(new android.graphics.LinearGradient(
                            left, top,
                            left, bottom,
                            lighten(baseColor),
                            darken(baseColor),
                            android.graphics.Shader.TileMode.CLAMP
                    ));

                    canvas.drawRoundRect(left, top, right, bottom, radius, radius, blockPaint);

                    blockPaint.setShader(null);
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

        float radius = blockRadius;

        canvas.save();

        // ⭐ scale around block center
        float centerX = block.x + (block.getWidth() * cellSize) / 2f;
        float centerY = block.y + (block.getHeight() * cellSize) / 2f;

        canvas.scale(block.scale, block.scale, centerX, centerY);

        for (int i = 0; i < block.shape.length; i++) {
            for (int j = 0; j < block.shape[0].length; j++) {

                if (block.shape[i][j] == 1) {

                    float left = block.x + j * cellSize + slotGap;
                    float top = block.y + i * cellSize + slotGap;
                    float right = left + cellSize - slotGap * 2;
                    float bottom = top + cellSize - slotGap * 2;

                    blockPaint.setShader(new android.graphics.LinearGradient(
                            left, top,
                            left, bottom,
                            lighten(block.color),
                            darken(block.color),
                            android.graphics.Shader.TileMode.CLAMP
                    ));

                    canvas.drawRoundRect(left, top, right, bottom, radius, radius, blockPaint);
                    blockPaint.setShader(null);
                }
            }
        }

        canvas.restore();
    }








    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isGameOver) return false;


        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                for (Block block : availableBlocks) {
                    if (isTouchInsideBlock(event, block)) {


                        draggingBlock = block;

                        block.startX = block.x;
                        block.startY = block.y;


                        //  enlarge and lift when picked
                        dragOffsetY = cellSize * 4f;

                        block.scale = 1f;
                        block.targetScale = 1f;


                        break;
                    }
                }
                break;


            case MotionEvent.ACTION_MOVE:
                if (draggingBlock != null) {
                    draggingBlock.x = event.getX() - (cellSize / 2f);
                    draggingBlock.y = event.getY() - dragOffsetY;


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
        int col = Math.round((block.x - gridMargin) / cellSize);
        int row = Math.round((block.y - gridMargin) / cellSize);


        if (canPlace(block, row, col)) {
            for (int i = 0; i < block.shape.length; i++) {
                for (int j = 0; j < block.shape[0].length; j++) {
                    if (block.shape[i][j] == 1) {
                        grid[row + i][col + j] = block.color;
                    }
                }
            }

            soundManager.playPlace();
            clearCompletedLines();
            generateBlocks(); // regenerate after placing
        }
        else {
            block.x = block.startX;
            block.y = block.startY;

            //  shrink back to tray size
            block.scale = 0.6f;
            block.targetScale = 0.6f;

            soundManager.playDrop();
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

                    if (grid[r][c] != EMPTY)
                        return false;

                }
            }
        }
        return true;
    }



    private void generateBlocks() {

        float spacing = cellSize * 0.4f; // smaller & natural gap
        float startY = gridMargin + rows * cellSize + (cellSize * 2f);

        //  Create blocks first
        for (int i = 0; i < 3; i++) {
            int shapeIndex = (int) (Math.random() * blockShapes.length);
            int colorIndex = (int) (Math.random() * colors.length);

            availableBlocks[i] = new Block(
                    blockShapes[shapeIndex],
                    colors[colorIndex]
            );
        }

        //  Calculate total width using REAL block sizes
        float totalWidth = 0;
        for (int i = 0; i < 3; i++) {
            totalWidth += availableBlocks[i].getWidth() * cellSize;
            if (i < 2) totalWidth += spacing;
        }

        //  Center horizontally
        float startX = (getWidth() - totalWidth) / 2f;


        // Position blocks (BOTTOM ALIGNED)
        float currentX = startX;

        for (int i = 0; i < 3; i++) {

            Block block = availableBlocks[i];

            block.scale = 0.6f;
            block.targetScale = 0.6f;

            block.x = currentX;

            // ⭐ KEY FIX: align by bottom
            float blockHeight = block.getHeight() * cellSize * block.scale;
            block.y = startY - blockHeight;

            // save original position (for snap-back)
            block.startX = block.x;
            block.startY = block.y;

            currentX += block.getWidth() * cellSize + spacing;
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
                if (grid[i][j] == EMPTY)
                {
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
                if (grid[i][j] == EMPTY)
                {
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
                for (int j = 0; j < cols; j++) grid[i][j] = EMPTY;

                cleared++;
            }
        }

        for (int j = 0; j < cols; j++) {
            if (highlightCols[j]) {
                for (int i = 0; i < rows; i++) grid[i][j] = EMPTY;

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

        gridMargin = w * 0.06f; // 6% margin on left & right

        cellSize = (int) ((w - 2 * gridMargin) / cols);

        slotGap = cellSize * 0.025f;
        slotRadius = cellSize * 0.06f;
        blockRadius = cellSize * 0.08f;
        boardRadius = cellSize * 0.12f;

        floatingTextPaint.setTextSize(cellSize * 0.8f);

        generateBlocks();
    }


    private void drawBoard(Canvas canvas) {

        float boardLeft = gridMargin;
        float boardTop = gridMargin;
        float boardRight = gridMargin + cols * cellSize;
        float boardBottom = gridMargin + rows * cellSize;

        // Board background (container)
        canvas.drawRoundRect(
                boardLeft,
                boardTop,
                boardRight,
                boardBottom,
                boardRadius,
                boardRadius,
                boardPaint
        );

        canvas.drawRoundRect(
                boardLeft,
                boardTop,
                boardRight,
                boardBottom,
                boardRadius,
                boardRadius,
                boardBorderPaint
        );

        // Empty slots
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                float left   = gridMargin + j * cellSize + slotGap;
                float top    = gridMargin + i * cellSize + slotGap;
                float right  = gridMargin + (j + 1) * cellSize - slotGap;
                float bottom = gridMargin + (i + 1) * cellSize - slotGap;


                canvas.drawRoundRect(
                        left,
                        top,
                        right,
                        bottom,
                        slotRadius,
                        slotRadius,
                        slotPaint
                );
            }
        }
    }



    private int lighten(int color) {
        return Color.argb(
                Color.alpha(color),
                Math.min(255, (int)(Color.red(color) * 1.2)),
                Math.min(255, (int)(Color.green(color) * 1.2)),
                Math.min(255, (int)(Color.blue(color) * 1.2))
        );
    }

    private int darken(int color) {
        return Color.argb(
                Color.alpha(color),
                (int)(Color.red(color) * 0.8),
                (int)(Color.green(color) * 0.8),
                (int)(Color.blue(color) * 0.8)
        );
    }


    /*private void drawEmptySlots(Canvas canvas) {

        float padding = cellPadding;              // SAME padding
        float radius = cellSize * 0.18f;           // slightly rounded

        Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emptyPaint.setColor(emptySlotColor);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                float left = j * cellSize + padding;
                float top = i * cellSize + padding;
                float right = left + cellSize - padding * 2;
                float bottom = top + cellSize - padding * 2;

                canvas.drawRoundRect(
                        left,
                        top,
                        right,
                        bottom,
                        radius,
                        radius,
                        emptyPaint
                );
            }
        }
    }*/





}
