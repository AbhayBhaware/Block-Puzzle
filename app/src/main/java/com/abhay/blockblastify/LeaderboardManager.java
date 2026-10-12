package com.abhay.blockblastify;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LeaderboardManager {

    private static final String PREFS_NAME = "BlockPuzzlePrefs";
    private static final String KEY_PLAYER_NAME = "player_name";
    private static final String KEY_LEADERBOARD_JSON = "leaderboard_json";
    private static final String KEY_LAST_UPDATE_TIME = "leaderboard_last_update";
    private static final String KEY_HIGH_SCORE = "high_score";

    private static final String[] INDIAN_NAMES = {
            "Aarav Sharma", "Vihaan Patel", "Priya Nair", "Rohan Verma",
            "Ananya Iyer", "Kabir Mehta", "Sneha Kulkarni", "Arjun Reddy",
            "Diya Deshmukh", "Devendra Yadav", "Pooja Malhotra", "Aryan Chawla",
            "Riya Sen", "Siddharth Rao", "Tanvi Bhat", "Manish Tiwari",
            "Meera Agarwal", "Karan Kapoor", "Shreya Das", "Nikhil Chopra",
            "Akash Bansal", "Neha Joshi", "Aditya Mukherjee", "Ishita Roy"
    };

    private static final String[] INDIAN_CITIES = {
            "Mumbai", "Delhi", "Bengaluru", "Hyderabad", "Ahmedabad",
            "Chennai", "Kolkata", "Pune", "Jaipur", "Lucknow",
            "Chandigarh", "Indore", "Bhopal", "Nagpur", "Kochi",
            "Surat", "Patna", "Vadodara", "Ghaziabad", "Ludhiana"
    };

    private static final int[] AVATAR_COLORS = {
            Color.parseColor("#FF5722"), // Deep Orange
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#E91E63"), // Pink
            Color.parseColor("#00BCD4"), // Cyan
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#3F51B5"), // Indigo
            Color.parseColor("#009688")  // Teal
    };

    public static String getPlayerName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_PLAYER_NAME, "Player");
    }

    public static void setPlayerName(Context context, String name) {
        if (name == null || name.trim().isEmpty()) {
            name = "Player";
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_PLAYER_NAME, name.trim()).apply();
    }

    public static List<LeaderboardEntry> getLeaderboard(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_LEADERBOARD_JSON, null);
        long lastUpdate = prefs.getLong(KEY_LAST_UPDATE_TIME, 0);
        long now = System.currentTimeMillis();

        List<LeaderboardEntry> entries = new ArrayList<>();

        if (jsonStr == null || jsonStr.isEmpty()) {
            // Initialize default realistic Indian leaderboard
            entries = createInitialDataset();
            saveLeaderboard(context, entries);
        } else {
            entries = parseLeaderboardJson(jsonStr);
            // If data is empty for some reason, re-init
            if (entries.isEmpty()) {
                entries = createInitialDataset();
            }
        }

        // Apply realistic dynamic shifts (every time user views, or every few minutes)
        // Make dynamic changes subtle and realistic
        Random random = new Random();
        boolean modified = false;

        // If at least 30 seconds passed or random trigger, simulate real active players
        if (now - lastUpdate > 30000 || random.nextBoolean()) {
            int playersToChange = 2 + random.nextInt(3); // 2 to 4 players update score
            for (int k = 0; k < playersToChange; k++) {
                int index = random.nextInt(entries.size());
                LeaderboardEntry entry = entries.get(index);
                if (!entry.isUser()) {
                    int delta = (random.nextInt(6) + 1) * 35; // +35 to +210 pts
                    entry.setScore(entry.getScore() + delta);
                    int mins = random.nextInt(8) + 1;
                    entry.setTimeAgo(mins + "m ago");
                    modified = true;
                }
            }
            prefs.edit().putLong(KEY_LAST_UPDATE_TIME, now).apply();
        }

        // Now inject or update the user's entry
        String userName = getPlayerName(context);
        int userHighScore = prefs.getInt(KEY_HIGH_SCORE, 0);

        // Remove any existing user entry from list to re-add fresh
        entries.removeIf(LeaderboardEntry::isUser);

        // Add user entry
        LeaderboardEntry userEntry = new LeaderboardEntry(
                0,
                userName + " (You)",
                userHighScore,
                true,
                "Online",
                "Just now",
                Color.parseColor("#00E5FF") // Electric cyan for user
        );
        entries.add(userEntry);

        // Sort descending by score
        Collections.sort(entries, (a, b) -> Integer.compare(b.getScore(), a.getScore()));

        // Assign ranks
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }

        // Save back state (excluding user so user updates live dynamically)
        if (modified) {
            saveLeaderboard(context, entries);
        }

        return entries;
    }

    private static List<LeaderboardEntry> createInitialDataset() {
        List<LeaderboardEntry> list = new ArrayList<>();
        Random random = new Random(101); // seeded for initial consistency

        // Base descending scores starting around 4,800
        int currentScore = 4820;

        for (int i = 0; i < INDIAN_NAMES.length; i++) {
            String name = INDIAN_NAMES[i];
            String city = INDIAN_CITIES[i % INDIAN_CITIES.length];
            int minsAgo = (i + 1) * 3 + random.nextInt(5);
            String timeAgo = (minsAgo < 60) ? (minsAgo + "m ago") : ((minsAgo / 60) + "h ago");
            int color = AVATAR_COLORS[i % AVATAR_COLORS.length];

            list.add(new LeaderboardEntry(0, name, currentScore, false, city, timeAgo, color));

            // Decrement score realistically
            int step = 120 + random.nextInt(210);
            currentScore = Math.max(300, currentScore - step);
        }

        return list;
    }

    private static List<LeaderboardEntry> parseLeaderboardJson(String jsonStr) {
        List<LeaderboardEntry> list = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(jsonStr);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                boolean isUser = obj.optBoolean("isUser", false);
                if (isUser) continue; // Skip saved user, will re-inject fresh

                LeaderboardEntry entry = new LeaderboardEntry(
                        obj.optInt("rank", i + 1),
                        obj.optString("name", "Player"),
                        obj.optInt("score", 500),
                        false,
                        obj.optString("location", "India"),
                        obj.optString("timeAgo", "5m ago"),
                        obj.optInt("avatarColor", Color.GRAY)
                );
                list.add(entry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static void saveLeaderboard(Context context, List<LeaderboardEntry> entries) {
        try {
            JSONArray array = new JSONArray();
            for (LeaderboardEntry entry : entries) {
                if (entry.isUser()) continue; // Do not persist user, inject live
                JSONObject obj = new JSONObject();
                obj.put("rank", entry.getRank());
                obj.put("name", entry.getName());
                obj.put("score", entry.getScore());
                obj.put("isUser", false);
                obj.put("location", entry.getLocation());
                obj.put("timeAgo", entry.getTimeAgo());
                obj.put("avatarColor", entry.getAvatarColor());
                array.put(obj);
            }
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_LEADERBOARD_JSON, array.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
