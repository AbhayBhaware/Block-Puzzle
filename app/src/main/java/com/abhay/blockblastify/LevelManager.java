package com.abhay.blockblastify;

import android.content.Context;
import android.content.SharedPreferences;

public class LevelManager {

    private static final String PREFS_NAME = "BlockPuzzlePrefs";
    private static final String KEY_UNLOCKED_LEVEL = "unlocked_level";
    public static final int TOTAL_LEVELS = 30;

    public static int getUnlockedLevel(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_UNLOCKED_LEVEL, 1);
    }

    public static void setUnlockedLevel(Context context, int level) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_UNLOCKED_LEVEL, 1);
        if (level > current) {
            prefs.edit().putInt(KEY_UNLOCKED_LEVEL, level).apply();
        }
    }

    public static boolean isLevelUnlocked(Context context, int level) {
        return level <= getUnlockedLevel(context);
    }

    public static void completeLevel(Context context, int completedLevel) {
        setUnlockedLevel(context, completedLevel + 1);
    }

    public static int getTargetScore(int level) {
        if (level <= 1) return 300;
        if (level == 2) return 550;
        if (level == 3) return 850;
        if (level == 4) return 1200;
        if (level == 5) return 1650;
        if (level == 6) return 2150;
        if (level == 7) return 2700;
        if (level == 8) return 3300;
        if (level == 9) return 4000;
        if (level == 10) return 4800;
        // Levels 11-30 scale progressively
        return 4800 + (level - 10) * 850;
    }

    public static int getObstacleCount(int level) {
        if (level <= 2) return 0;
        if (level <= 4) return 2;
        if (level <= 6) return 3;
        if (level <= 8) return 4;
        if (level <= 10) return 5;
        if (level <= 15) return 6;
        if (level <= 20) return 7;
        return 8;
    }

    public static int getRewardCoins(int level) {
        return 20 + (level * 5);
    }
}
