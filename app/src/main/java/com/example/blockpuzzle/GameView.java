package com.example.blockpuzzle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameView extends View {

    private int score = 0;
    private boolean isGameOver = false;

    private final int highlightColor = Color.parseColor("#FFD54F"); // gold highlight

    private boolean[] highlightRows;
    private boolean[] highlightCols;

    private SoundManager soundManager;

    private final List<FloatingText> floatingTexts = new ArrayList<>();
    private Paint floatingTextPaint;

    private float dragTouchOffsetX;
    private float dragTouchOffsetY;

    private float dragLiftY;

    private int comboCount = 0;

    private boolean useReviveBlocks = false; // Flag to force 1x1 blocks on revive

    private float slotRadius;
    private float blockRadius;
    private float boardRadius;

    private float gridMargin;

    private float slotGap;

    private final List<int[]> lastPlacedCells = new ArrayList<>();

    private Paint fillTextPaint;
    private Paint strokeTextPaint;

    private final ArrayList<Particle> particles = new ArrayList<>();
    private final Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final int[][][] easyShapes = {
            {{1}},
            {{1, 1}},
            {{1}, {1}},
            {{1, 1}, {1, 1}},
            {{1, 1, 1}},
            {{1}, {1}, {1}},
    };

    private final int[][][] mediumShapes = {
            {{1, 0}, {1, 0}, {1, 1}},
            {{1, 1}, {1, 0}, {1, 0}},
            {{0, 1}, {0, 1}, {1, 1}},
            {{1, 1}, {0, 1}, {0, 1}},
            {{1, 0}, {1, 1}, {1, 0}},
            {{0, 1}, {1, 1}, {0, 1}},
            {{0, 1, 0}, {1, 1, 1}},
            {{1, 1, 1}, {0, 1, 0}},
            {{1, 1, 1}, {1, 0, 0}},
            {{1, 1, 1}, {0, 0, 1}},
            {{1, 0, 0}, {1, 1, 1}},
            {{0, 0, 1}, {1, 1, 1}},
            {{1, 1}, {1, 0}},
            {{1, 1}, {0, 1}},
            {{1, 0}, {1, 1}},
            {{0, 1}, {1, 1}},
            {{1, 0}, {0, 1}},
            {{0, 1}, {1, 0}},
            {{1, 1, 0}, {0, 1, 1}},
            {{0, 1, 1}, {1, 1, 0}},
    };

    private final int[] colors = {
            Color.parseColor("#FF5252"), // Bright Red
            Color.parseColor("#448AFF"), // Bright Blue
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#E040FB"), // Pink/Purple
            Color.parseColor("#00BCD4")  // Cyan
    };

    private final Block[] availableBlocks = new Block[3];
    private Block draggingBlock = null;

    private final int rows = 8, cols = 8;
    private int cellSize;
    private final int[][] grid = new int[rows][cols];

    private static final int EMPTY = -1;

    private Paint blockPaint;
    private Paint boardBorderPaint;
    private Paint boardPaint;
    private Paint slotPaint;
    private Paint shadowPaint;
    private Paint highlightPaint;

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

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public GameView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        soundManager = new SoundManager(context);

        blockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        highlightRows = new boolean[rows];
        highlightCols = new boolean[cols];

        floatingTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        floatingTextPaint.setColor(Color.WHITE);
        floatingTextPaint.setTextAlign(Paint.Align.CENTER);
        floatingTextPaint.setFakeBoldText(true);

        boardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boardPaint.setColor(Color.parseColor("#2B2E4A")); // dark board

        boardBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boardBorderPaint.setStyle(Paint.Style.STROKE);
        boardBorderPaint.setStrokeWidth(getResources().getDisplayMetrics().density * 4);
        boardBorderPaint.setColor(Color.parseColor("#3F4466")); // subtle border

        slotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        slotPaint.setColor(Color.parseColor("#1F2238")); // empty slot

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setAlpha(60);

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setStyle(Paint.Style.FILL);
        highlightPaint.setColor(Color.WHITE);
        highlightPaint.setAlpha(50);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = EMPTY;
            }
        }

        fillTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillTextPaint.setTextAlign(Paint.Align.CENTER);
        fillTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

        strokeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokeTextPaint.setStyle(Paint.Style.STROKE);
        strokeTextPaint.setStrokeWidth(10);
        strokeTextPaint.setTextAlign(Paint.Align.CENTER);
        strokeTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        drawBoard(canvas);
        drawFilledCells(canvas);
        drawBlocks(canvas);

        drawFloatingTexts(canvas);
        drawParticles(canvas);
    }

    private void drawFloatingTexts(Canvas canvas) {
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
            
            // Smoother ease-out movement
            float movementProgress = 1 - (1 - progress) * (1 - progress); // Quadratic ease-out
            
            float currentX = ft.startX + (ft.targetX - ft.startX) * movementProgress;
            float currentY = ft.startY + (ft.targetY - ft.startY) * movementProgress;

            int alpha = (int) (255 * Math.sin(Math.PI * (1 - progress))); // Fade in and then out
            if (progress > 0.8f) alpha = (int) (255 * (1 - progress) * 5); // Faster fade at the end
            
            float scale;
            if (ft.type == FloatingText.TYPE_COMBO) {
                // Pop effect for combo
                if (progress < 0.2f) scale = 0.5f + (progress / 0.2f) * 0.8f;
                else scale = 1.3f - ((progress - 0.2f) / 0.8f) * 0.3f;
            } else {
                scale = 0.8f + (0.4f * (float)Math.sin(Math.PI * progress));
            }

            canvas.save();
            canvas.scale(scale, scale, currentX, currentY);

            fillTextPaint.setTextSize(ft.textSize);
            strokeTextPaint.setTextSize(ft.textSize);

            fillTextPaint.setAlpha(alpha);
            strokeTextPaint.setAlpha(alpha);

            strokeTextPaint.setColor(ft.strokeColor);
            canvas.drawText(ft.text, currentX, currentY, strokeTextPaint);

            fillTextPaint.setColor(ft.fillColor);
            canvas.drawText(ft.text, currentX, currentY, fillTextPaint);

            canvas.restore();
        }

        if (!floatingTexts.isEmpty()) {
            postInvalidateOnAnimation();
        }
    }

    private void drawParticles(Canvas canvas) {
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
            p.x += p.vx;
            p.y += p.vy;

            int alpha = (int) (255 * (1 - progress));
            particlePaint.setAlpha(alpha);
            particlePaint.setColor(p.color);

            canvas.drawCircle(p.x, p.y, p.radius * (1 - progress * 0.5f), particlePaint);
        }

        if (!particles.isEmpty()) {
            postInvalidateOnAnimation();
        }
    }

    private void drawFilledCells(Canvas canvas) {
        float radius = blockRadius;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] != EMPTY) {
                    int baseColor = (highlightRows[i] || highlightCols[j]) ? highlightColor : grid[i][j];
                    float left = gridMargin + j * cellSize + slotGap;
                    float top = gridMargin + i * cellSize + slotGap;
                    float right = gridMargin + (j + 1) * cellSize - slotGap;
                    float bottom = gridMargin + (i + 1) * cellSize - slotGap;

                    drawStyledBlock(canvas, left, top, right, bottom, baseColor, radius);
                }
            }
        }
    }

    private void drawStyledBlock(Canvas canvas, float left, float top, float right, float bottom, int color, float radius) {
        // Main block body with gradient
        blockPaint.setShader(new LinearGradient(
                left, top, left, bottom,
                lighten(color), darken(color),
                Shader.TileMode.CLAMP
        ));
        canvas.drawRoundRect(left, top, right, bottom, radius, radius, blockPaint);
        blockPaint.setShader(null);

        // Subtle Top Highlight (Bevel effect)
        float hMargin = (right - left) * 0.15f;
        canvas.drawRoundRect(left + hMargin, top + hMargin, right - hMargin, top + hMargin * 2.2f, radius / 2, radius / 2, highlightPaint);
    }

    private void drawBlocks(Canvas canvas) {
        for (Block block : availableBlocks) {
            if (block == null || block.isUsed) continue;
            drawSingleBlock(canvas, block);
        }
    }

    private void drawSingleBlock(Canvas canvas, Block block) {
        float radius = blockRadius;
        canvas.save();

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

                    drawStyledBlock(canvas, left, top, right, bottom, block.color, radius);
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
                    if (block == null || block.isUsed) continue;

                    if (isTouchInsideBlock(event, block)) {
                        draggingBlock = block;
                        block.startX = block.x;
                        block.startY = block.y;

                        float fullWidth = block.getWidth() * cellSize;
                        float fullHeight = block.getHeight() * cellSize;
                        float scaledWidth = fullWidth * block.scale;
                        float scaledHeight = fullHeight * block.scale;

                        float visibleLeft = block.x + (fullWidth - scaledWidth) / 2f;
                        float visibleTop = block.y + (fullHeight - scaledHeight) / 2f;

                        dragTouchOffsetX = event.getX() - visibleLeft;
                        dragTouchOffsetY = event.getY() - visibleTop;

                        // Lift block upward so finger doesn't hide it
                        dragLiftY = cellSize * 3f;

                        block.scale = 1.0f;
                        block.targetScale = 1.0f;

                        invalidate();
                        return true;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (draggingBlock != null) {
                    draggingBlock.x = event.getX() - dragTouchOffsetX;
                    draggingBlock.y = event.getY() - dragTouchOffsetY - dragLiftY;

                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (draggingBlock != null) {
                    placeBlock(draggingBlock);
                    draggingBlock = null;
                    invalidate();
                    performClick();
                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (draggingBlock != null) {
                    draggingBlock.x = draggingBlock.startX;
                    draggingBlock.y = draggingBlock.startY;
                    draggingBlock.scale = 0.6f;
                    draggingBlock.targetScale = 0.6f;
                    draggingBlock = null;
                    invalidate();
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void placeBlock(Block block) {
        int col = Math.round((block.x - gridMargin) / cellSize);
        int row = Math.round((block.y - gridMargin) / cellSize);

        if (canPlace(block, row, col)) {
            lastPlacedCells.clear();
            for (int i = 0; i < block.shape.length; i++) {
                for (int j = 0; j < block.shape[0].length; j++) {
                    if (block.shape[i][j] == 1) {
                        grid[row + i][col + j] = block.color;
                        lastPlacedCells.add(new int[]{row + i, col + j});
                    }
                }
            }

            //  Placement score based on block size
            int blockCells = countBlockCells(block);
            int placementScore = blockCells * (1 + score / 500);

            score += placementScore;
            if (scoreListener != null) {
                scoreListener.onScoreChanged(score);
            }

            block.isUsed = true;
            soundManager.playPlace();
            clearCompletedLines();

            if (allBlocksUsed()) {
                generateBlocks();
            }
        } else {
            block.x = block.startX;
            block.y = block.startY;

            //  shrink back to tray size
            block.scale = 0.6f;
            block.targetScale = 0.6f;

            soundManager.playDrop();
        }

        postDelayed(() -> {
            if (!canAnyBlockFit()) {
                isGameOver = true;
                soundManager.playGameOver();
                if (gameOverListener != null) {
                    gameOverListener.onGameOver(score);
                }
            }
        }, 180);
    }

    private boolean allBlocksUsed() {
        for (Block block : availableBlocks) {
            if (block != null && !block.isUsed) {
                return false;
            }
        }
        return true;
    }

    private boolean canPlace(Block block, int row, int col) {
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

    public void generateBlocks() {
        float startY = gridMargin + rows * cellSize + (cellSize * 2.5f);

        for (int i = 0; i < 3; i++) {
            int[][] selectedShape;

            if (useReviveBlocks) {
                selectedShape = new int[][]{{1}};
            } else {
                selectedShape = getRandomShape();
            }

            int colorIndex = (int) (Math.random() * colors.length);
            availableBlocks[i] = new Block(
                    selectedShape,
                    colors[colorIndex]
            );
            availableBlocks[i].isUsed = false;
        }

        useReviveBlocks = false;
        layoutAvailableBlocks(startY);
    }

    private void layoutAvailableBlocks(float startY) {
        float trayLeft = gridMargin;
        float trayRight = getWidth() - gridMargin;
        float trayWidth = trayRight - trayLeft;

        float sectionWidth = trayWidth / 3f;

        for (int i = 0; i < 3; i++) {
            Block block = availableBlocks[i];
            if (block == null) continue;

            block.scale = 0.6f;
            block.targetScale = 0.6f;

            float fullBlockWidth = block.getWidth() * cellSize;
            float scaledBlockWidth = fullBlockWidth * block.scale;
            float scaledBlockHeight = block.getHeight() * cellSize * block.scale;

            float sectionLeft = trayLeft + i * sectionWidth;
            float sectionCenterX = sectionLeft + sectionWidth / 2f;

            float visibleLeft = sectionCenterX - scaledBlockWidth / 2f;

            block.x = visibleLeft - (fullBlockWidth - scaledBlockWidth) / 2f;
            block.y = startY - scaledBlockHeight;

            block.startX = block.x;
            block.startY = block.y;
        }
    }

    public void performRevive() {
        isGameOver = false;
        useReviveBlocks = true;
        draggingBlock = null;
        generateBlocks();
        invalidate();
    }

    private int[][] getRandomShape() {
        int emptyCells = countEmptyCells();

        if (emptyCells < 15) {
            return easyShapes[(int)(Math.random() * 3)];
        }

        if (score < 1000) {
            return easyShapes[(int)(Math.random() * easyShapes.length)];
        }

        if (score < 1500) {
            if (Math.random() < 0.7) {
                return easyShapes[(int)(Math.random() * easyShapes.length)];
            } else {
                return mediumShapes[(int)(Math.random() * mediumShapes.length)];
            }
        }

        if (Math.random() < 0.5) {
            return easyShapes[(int)(Math.random() * easyShapes.length)];
        } else {
            return mediumShapes[(int)(Math.random() * mediumShapes.length)];
        }
    }

    private boolean isTouchInsideBlock(MotionEvent e, Block block) {
        float fullWidth = block.getWidth() * cellSize;
        float fullHeight = block.getHeight() * cellSize;

        float scaledWidth = fullWidth * block.scale;
        float scaledHeight = fullHeight * block.scale;

        float left = block.x + (fullWidth - scaledWidth) / 2f;
        float top = block.y + (fullHeight - scaledHeight) / 2f;
        float right = left + scaledWidth;
        float bottom = top + scaledHeight;

        float touchPadding = cellSize * 0.30f;

        return e.getX() >= left - touchPadding &&
                e.getX() <= right + touchPadding &&
                e.getY() >= top - touchPadding &&
                e.getY() <= bottom + touchPadding;
    }

    private float getStreakMultiplier(int streak) {
        if (streak <= 1) return 1.0f;
        if (streak == 2) return 1.2f;
        if (streak == 3) return 1.5f;
        return 2.0f;
    }

    private void clearCompletedLines() {
        for (int i = 0; i < rows; i++) highlightRows[i] = false;
        for (int j = 0; j < cols; j++) highlightCols[j] = false;

        int clearedRows = 0;
        for (int i = 0; i < rows; i++) {
            boolean full = true;
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == EMPTY) {
                    full = false;
                    break;
                }
            }
            if (full) {
                highlightRows[i] = true;
                clearedRows++;
            }
        }

        int clearedCols = 0;
        for (int j = 0; j < cols; j++) {
            boolean full = true;
            for (int i = 0; i < rows; i++) {
                if (grid[i][j] == EMPTY) {
                    full = false;
                    break;
                }
            }
            if (full) {
                highlightCols[j] = true;
                clearedCols++;
            }
        }

        postDelayed(this::clearHighlightedLines, 120);

        int totalCleared = clearedRows + clearedCols;
        int earnedCoins = 0;

        if (totalCleared >= 3) {
            earnedCoins = 5;
        } else if (clearedRows > 0 && clearedCols > 0) {
            earnedCoins = 3;
        }

        if (earnedCoins > 0) {
            if (coinListener != null) {
                coinListener.onCoinsEarned(earnedCoins);
            }
            soundManager.playCoinEarn();
        }

        if (totalCleared > 0) {
            comboCount++;
            float multiplier = getStreakMultiplier(comboCount);
            int baseLineScore = 50;

            int lineScore = totalCleared * baseLineScore;
            if (totalCleared >= 2) {
                lineScore += (totalCleared * 100);
            }

            int difficultyBonus = Math.min(score / 2000, 5);
            int gainedScore = (int) (lineScore * multiplier * (1 + difficultyBonus));

            soundManager.playClear();
            score += gainedScore;

            if (scoreListener != null) {
                scoreListener.onScoreChanged(score);
            }

            showComboFeedback(totalCleared, comboCount, gainedScore);
        } else {
            comboCount = 0;
        }

        invalidate();
    }

    private void showComboFeedback(int lines, int streak, int scoreValue) {
        String msg = "";
        int color = Color.WHITE;

        if (streak >= 6) {
            msg = "LEGENDARY!";
            color = Color.parseColor("#F44336"); // Red
        } else if (streak == 5) {
            msg = "UNSTOPPABLE!";
            color = Color.parseColor("#E040FB"); // Purple
        } else if (streak == 4) {
            msg = "PERFECT!";
            color = Color.parseColor("#00E5FF"); // Cyan
        } else if (lines >= 3) {
            msg = "COMBO x" + lines + " \uD83D\uDD25";
            color = Color.parseColor("#FF5722"); // Deep Orange
        } else if (lines == 2) {
            msg = "GREAT!";
            color = Color.parseColor("#FFC107"); // Amber
        } else if (streak == 2) {
            msg = "NICE!";
            color = Color.parseColor("#4CAF50"); // Green
        }

        if (!msg.isEmpty()) {
            showComboText(msg, color, streak);
            postDelayed(() -> showScoreText(scoreValue), 250);
        } else {
            showScoreText(scoreValue);
        }
    }

    private boolean canAnyBlockFit() {
        for (Block block : availableBlocks) {
            if (block == null || block.isUsed) continue;

            int blockHeight = block.shape.length;
            int blockWidth = block.shape[0].length;

            for (int row = 0; row <= rows - blockHeight; row++) {
                for (int col = 0; col <= cols - blockWidth; col++) {
                    if (canPlace(block, row, col)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public interface GameOverListener {
        void onGameOver(int finalScore);
    }

    private GameOverListener gameOverListener;

    public void setGameOverListener(GameOverListener listener) {
        this.gameOverListener = listener;
    }

    private void clearHighlightedLines() {
        for (int i = 0; i < rows; i++) {
            if (highlightRows[i]) {
                for (int[] cell : lastPlacedCells) {
                    if (cell[0] == i) {
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
                    if (cell[1] == j) {
                        float cx = gridMargin + cell[1] * cellSize + cellSize / 2f;
                        float cy = gridMargin + cell[0] * cellSize + cellSize / 2f;
                        spawnParticles(cx, cy, 25, Color.CYAN);
                    }
                }
                for (int i = 0; i < rows; i++) grid[i][j] = EMPTY;
            }
        }
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (soundManager != null) soundManager.release();
    }

    private static class FloatingText {
        static final int TYPE_SCORE = 0;
        static final int TYPE_COMBO = 1;

        float startX, startY;
        float targetX, targetY;
        String text;
        long startTime;
        long duration;
        int fillColor;
        int strokeColor;
        float textSize;
        int type;
    }

    private float getScoreX() {
        return getWidth() / 2f;
    }

    private float getScoreY() {
        return gridMargin * 0.7f;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        gridMargin = w * 0.06f;
        cellSize = (int) ((w - 2 * gridMargin) / cols);

        slotGap = cellSize * 0.025f;
        slotRadius = cellSize * 0.08f;
        blockRadius = cellSize * 0.12f;
        boardRadius = cellSize * 0.15f;

        floatingTextPaint.setTextSize(cellSize * 0.8f);

        generateBlocks();
    }

    private void drawBoard(Canvas canvas) {
        float boardLeft = gridMargin;
        float boardTop = gridMargin;
        float boardRight = gridMargin + cols * cellSize;
        float boardBottom = gridMargin + rows * cellSize;

        canvas.drawRoundRect(boardLeft + 10, boardTop + 10, boardRight + 10, boardBottom + 10, boardRadius, boardRadius, shadowPaint);

        canvas.drawRoundRect(boardLeft, boardTop, boardRight, boardBottom, boardRadius, boardRadius, boardPaint);
        canvas.drawRoundRect(boardLeft, boardTop, boardRight, boardBottom, boardRadius, boardRadius, boardBorderPaint);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                float left   = gridMargin + j * cellSize + slotGap;
                float top    = gridMargin + i * cellSize + slotGap;
                float right  = gridMargin + (j + 1) * cellSize - slotGap;
                float bottom = gridMargin + (i + 1) * cellSize - slotGap;

                canvas.drawRoundRect(left, top, right, bottom, slotRadius, slotRadius, slotPaint);
            }
        }
    }

    private int lighten(int color) {
        return Color.argb(
                Color.alpha(color),
                Math.min(255, (int)(Color.red(color) * 1.25)),
                Math.min(255, (int)(Color.green(color) * 1.25)),
                Math.min(255, (int)(Color.blue(color) * 1.25))
        );
    }

    private int darken(int color) {
        return Color.argb(
                Color.alpha(color),
                (int)(Color.red(color) * 0.75),
                (int)(Color.green(color) * 0.75),
                (int)(Color.blue(color) * 0.75)
        );
    }

    private void showComboText(String message, int color, int streak) {
        FloatingText ft = new FloatingText();
        ft.type = FloatingText.TYPE_COMBO;
        ft.text = message;

        ft.startX = gridMargin + (cols * cellSize) / 2f;
        ft.startY = gridMargin + (rows * cellSize) / 2f;

        ft.targetX = ft.startX;
        ft.targetY = ft.startY - cellSize * 2.2f;

        ft.startTime = System.currentTimeMillis();
        ft.duration = 1000 + Math.min(streak * 100L, 1000L);

        ft.fillColor = color;
        ft.strokeColor = Color.BLACK;

        ft.textSize = cellSize * (1.1f + Math.min(streak * 0.1f, 0.6f));

        floatingTexts.add(ft);
    }

    private void showScoreText(int scoreValue) {
        FloatingText ft = new FloatingText();
        ft.type = FloatingText.TYPE_SCORE;
        ft.text = "+" + scoreValue;

        float[] center = getLastPlacedBlockCenter();

        ft.startX = center[0];
        ft.startY = center[1];

        ft.targetX = getScoreX();
        ft.targetY = getScoreY();

        ft.startTime = System.currentTimeMillis();
        ft.duration = 900;

        ft.fillColor = Color.parseColor("#00E5FF");   // neon cyan
        ft.strokeColor = Color.parseColor("#006064"); // dark cyan stroke

        ft.textSize = cellSize * 0.9f;

        floatingTexts.add(ft);
    }

    private static class Particle {
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

    private int countBlockCells(Block block) {
        int count = 0;
        for (int i = 0; i < block.shape.length; i++) {
            for (int j = 0; j < block.shape[0].length; j++) {
                if (block.shape[i][j] == 1) {
                    count++;
                }
            }
        }
        return count;
    }

    public void resumeGame() {
        isGameOver = false;
        invalidate();
    }

    private float[] getLastPlacedBlockCenter() {
        if (lastPlacedCells.isEmpty())
            return new float[]{getWidth()/2f, getHeight()/2f};

        float sumX = 0;
        float sumY = 0;

        for (int[] cell : lastPlacedCells) {
            float cx = gridMargin + cell[1] * cellSize + cellSize / 2f;
            float cy = gridMargin + cell[0] * cellSize + cellSize / 2f;
            sumX += cx;
            sumY += cy;
        }

        float centerX = sumX / lastPlacedCells.size();
        float centerY = sumY / lastPlacedCells.size();

        return new float[]{centerX, centerY};
    }

    public void setSoundEnabled(boolean enabled) {
        if (soundManager != null) {
            soundManager.setSoundEnabled(enabled);
        }
    }

}
