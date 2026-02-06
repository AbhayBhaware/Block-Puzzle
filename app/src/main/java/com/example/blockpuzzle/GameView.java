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

    private int comboCount = 0;
    private static final int MAX_COMBO = 5;


    private float slotRadius;
    private float blockRadius;
    private float boardRadius;

    private float dragOffsetY;


    private float gridMargin;


    private float slotGap;

    private List<int[]> lastPlacedCells = new ArrayList<>();




    private int emptySlotColor = Color.parseColor("#1E233A");

    ArrayList<Particle> particles = new ArrayList<>();
    Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);





    private int[][][] easyShapes = {

            {{1}},
            {{1, 1}},
            {{1}, {1}},
            {{1, 1}, {1, 1}},
            {{1, 1, 1}},
            {{1}, {1}, {1}},
    };

    private int[][][] mediumShapes = {

            {{1, 0},
                    {1, 0},
                    {1, 1}},
            {{1, 1},
                    {1, 0},
                    {1, 0}},

            {{0, 1},
                    {0, 1},
                    {1, 1}},
            {{1, 1},
                    {0, 1},
                    {0, 1}},

            {{0, 1, 0},
                    {1, 1, 1}},

            {{1, 1, 1},
                    {0, 1, 0}}
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

    public interface CoinListener {
        void onCoinsEarned(int coins);
    }

    private CoinListener coinListener;

    public void setCoinListener(CoinListener listener) {
        this.coinListener = listener;
    }


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

            if (elapsed > ft.duration) {
                iterator.remove();
                continue;
            }

            float progress = (float) elapsed / ft.duration;


            float currentX = ft.startX + (ft.targetX - ft.startX) * progress;
            float currentY = ft.startY + (ft.targetY - ft.startY) * progress;

// fade
            int alpha = (int) (255 * (1 - progress));
            floatingTextPaint.setAlpha(alpha);

// shrink slightly
            float scale = 1f - (0.4f * progress);

            canvas.save();
            canvas.scale(scale, scale, currentX, currentY);

            floatingTextPaint.setColor(ft.color);
            canvas.drawText(ft.text, currentX, currentY, floatingTextPaint);

            canvas.restore();

        }

        if (!floatingTexts.isEmpty()) {
            postInvalidateOnAnimation();
        }


        //  DRAW PARTICLES
        long nowParticles = System.currentTimeMillis();
        Iterator<Particle> particleIterator = particles.iterator();

        while (particleIterator.hasNext()) {
            Particle p = particleIterator.next();

            float elapsed = nowParticles - p.startTime;
            if (elapsed > p.lifetime) {
                particleIterator.remove();
                continue;
            }

            float progress = elapsed / p.lifetime;

            // move
            p.x += p.vx;
            p.y += p.vy;

            // fade out
            int alpha = (int) (255 * (1 - progress));
            particlePaint.setAlpha(alpha);
            particlePaint.setColor(p.color);

            canvas.drawCircle(p.x, p.y, p.radius, particlePaint);
        }

// keep animation alive
        if (!particles.isEmpty()) {
            postInvalidateOnAnimation();
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
        for (Block block : availableBlocks) {
            if (block.isUsed) continue;
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
                    if (block.isUsed) continue;

                    if (isTouchInsideBlock(event, block)) {
                        draggingBlock = block;

                        block.startX = block.x;
                        block.startY = block.y;

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

            lastPlacedCells.clear();
            for (int i = 0; i < block.shape.length; i++) {
                for (int j = 0; j < block.shape[0].length; j++) {
                    if (block.shape[i][j] == 1) {
                        grid[row + i][col + j] = block.color;

                        int r = row + i;
                        int c = col + j;

                        lastPlacedCells.add(new int[]{r, c});

                    }
                }
            }

            block.isUsed = true;

            soundManager.playPlace();
            clearCompletedLines();
           // generateBlocks();

            if (allBlocksUsed()) {
                generateBlocks();
            }
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

    private boolean allBlocksUsed() {
        for (Block block : availableBlocks) {
            if (!block.isUsed) {
                return false;
            }
        }
        return true;
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

            int[][] selectedShape = getRandomShape();

            int colorIndex = (int) (Math.random() * colors.length);

            availableBlocks[i] = new Block(
                    selectedShape,
                    colors[colorIndex]
            );

            availableBlocks[i].isUsed = false;
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

    private int[][] getRandomShape() {

        int emptyCells = countEmptyCells();

        // If board almost full → give small shapes
        if (emptyCells < 15) {
            return easyShapes[(int)(Math.random() * 3)];
        }

        // Difficulty based on score
        if (score < 200) {
            return easyShapes[(int)(Math.random() * easyShapes.length)];
        }

        if (score < 500) {
            int total = easyShapes.length + mediumShapes.length;
            int rand = (int)(Math.random() * total);

            if (rand < easyShapes.length)
                return easyShapes[rand];
            else
                return mediumShapes[rand - easyShapes.length];
        }

        // 500+ score → allow all shapes
        int total = easyShapes.length + mediumShapes.length;
        int rand = (int)(Math.random() * total);

        if (rand < easyShapes.length)
            return easyShapes[rand];
        else
            return mediumShapes[rand - easyShapes.length];
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

        // 🪙 Coin reward logic
        int earnedCoins = 0;

        if (clearedLines == 1) {
            earnedCoins = 1;
        } else if (clearedLines == 2) {
            earnedCoins = 3;
        } else if (clearedLines >= 3) {
            earnedCoins = 5;
        }

        if (earnedCoins > 0 && coinListener != null) {
            coinListener.onCoinsEarned(earnedCoins);
        }



        // Score logic
        if (clearedLines > 0) {

            comboCount++;
            if (comboCount > MAX_COMBO) comboCount = MAX_COMBO;

            int multiplier = comboCount;
            int gainedScore = clearedLines * 10 * multiplier;

            soundManager.playClear();

            score += gainedScore;

            if (comboCount > 1) {
                showComboText(comboCount);

                postDelayed(() -> {
                    showScoreText(gainedScore);
                    invalidate();
                }, 200); // slight delay after combo text
            } else {
                showScoreText(gainedScore);
            }


            if (scoreListener != null) {
                scoreListener.onScoreChanged(score);
            }
        }
        else {
            //  No clear → combo reset
            comboCount = 0;
        }



        clearAlpha = 100;
        postDelayed(() -> {
            clearAlpha = 255;
            invalidate();
        }, 120);


        invalidate();
    }

    private boolean canAnyBlockFit() {

        for (Block block : availableBlocks) {

            //  IMPORTANT: skip already used blocks
            if (block.isUsed) continue;

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {

                    if (canPlace(block, row, col)) {
                        return true; // at least one move exists
                    }
                }
            }
        }

        return false; // no remaining block can fit
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

                for (int[] cell : lastPlacedCells) {
                    if (cell[0] == i) { // same row

                        float cx = gridMargin + cell[1] * cellSize + cellSize / 2f;
                        float cy = gridMargin + cell[0] * cellSize + cellSize / 2f;

                        spawnParticles(cx, cy, 25, Color.CYAN);
                    }
                }

                for (int j = 0; j < cols; j++) grid[i][j] = EMPTY;
            }
        }


        for (int j = 0; j < cols; j++) {
            if (highlightCols[j]) {

                for (int[] cell : lastPlacedCells) {
                    if (cell[1] == j) { // same column

                        float cx = gridMargin + cell[1] * cellSize + cellSize / 2f;
                        float cy = gridMargin + cell[0] * cellSize + cellSize / 2f;

                        spawnParticles(cx, cy, 25, Color.CYAN);
                    }
                }

                for (int i = 0; i < rows; i++) grid[i][j] = EMPTY;
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
        float startX, startY;
        float targetX, targetY;
        float scale = 1f;

        String text;
        long startTime;
        long duration;
        int color;
    }

    private float getScoreX() {
        return getWidth() / 2f;
    }

    private float getScoreY() {
        return gridMargin * 0.7f; // just above board
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

    private void showComboText(int combo) {

        FloatingText ft = new FloatingText();

        ft.text = "Combo x" + combo;

        ft.startX = gridMargin + (cols * cellSize) / 2f;
        ft.startY = gridMargin + (rows * cellSize) / 2f - cellSize * 0.4f;

        ft.targetX = ft.startX;
        ft.targetY = ft.startY - cellSize * 0.5f;

        ft.startTime = System.currentTimeMillis();
        ft.duration = 350;
        ft.color = Color.parseColor("#FF5722");

        floatingTexts.add(ft);
    }


    private void showScoreText(int scoreValue) {

        FloatingText ft = new FloatingText();

        ft.text = "+" + scoreValue;

        ft.startX = gridMargin + (cols * cellSize) / 2f;
        ft.startY = gridMargin + (rows * cellSize) / 2f + cellSize * 0.3f;

        ft.targetX = getScoreX();
        ft.targetY = getScoreY();

        ft.startTime = System.currentTimeMillis();
        ft.duration = 700;
        ft.color = Color.parseColor("#FFEB3B");

        floatingTexts.add(ft);
    }

    class Particle {
        float x, y;
        float vx, vy;
        float radius;
        int color;
        long startTime;
        long lifetime = 600; // ms
    }

    private void spawnParticles(float cx, float cy, int count, int color) {

        for (int i = 0; i < count; i++) {
            Particle p = new Particle();

            p.x = cx;
            p.y = cy;

            float angle = (float) (Math.random() * 2 * Math.PI);
            float speed = 6f + (float) Math.random() * 6f;

            p.vx = (float) Math.cos(angle) * speed;
            p.vy = (float) Math.sin(angle) * speed;

            p.radius = 6f + (float) Math.random() * 4f;
            p.color = color;
            p.startTime = System.currentTimeMillis();

            particles.add(p);
        }
    }

    private int countEmptyCells() {
        int empty = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == EMPTY) empty++;
            }
        }
        return empty;
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
