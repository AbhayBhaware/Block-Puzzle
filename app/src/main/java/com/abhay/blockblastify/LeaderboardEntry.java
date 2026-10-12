package com.abhay.blockblastify;

public class LeaderboardEntry {
    private int rank;
    private String name;
    private int score;
    private boolean isUser;
    private String location;
    private String timeAgo;
    private int avatarColor;

    public LeaderboardEntry(int rank, String name, int score, boolean isUser, String location, String timeAgo, int avatarColor) {
        this.rank = rank;
        this.name = name;
        this.score = score;
        this.isUser = isUser;
        this.location = location;
        this.timeAgo = timeAgo;
        this.avatarColor = avatarColor;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public int getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(int avatarColor) {
        this.avatarColor = avatarColor;
    }
}
