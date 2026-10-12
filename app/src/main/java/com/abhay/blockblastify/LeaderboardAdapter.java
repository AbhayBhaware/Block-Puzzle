package com.abhay.blockblastify;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final Context context;
    private List<LeaderboardEntry> entries;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

    public LeaderboardAdapter(Context context, List<LeaderboardEntry> entries) {
        this.context = context;
        this.entries = entries;
    }

    public void updateData(List<LeaderboardEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardEntry entry = entries.get(position);

        // Rank Display
        int rank = entry.getRank();
        if (rank == 1) {
            holder.playerRank.setText("🥇 1");
            holder.playerRank.setTextColor(Color.parseColor("#FFD54F")); // Gold
        } else if (rank == 2) {
            holder.playerRank.setText("🥈 2");
            holder.playerRank.setTextColor(Color.parseColor("#E0E0E0")); // Silver
        } else if (rank == 3) {
            holder.playerRank.setText("🥉 3");
            holder.playerRank.setTextColor(Color.parseColor("#FFAB91")); // Bronze
        } else {
            holder.playerRank.setText("#" + rank);
            holder.playerRank.setTextColor(Color.parseColor("#94A3B8"));
        }

        // Name & Tag
        holder.playerName.setText(entry.getName());
        if (entry.isUser()) {
            holder.userTag.setVisibility(View.VISIBLE);
            holder.playerName.setTextColor(ContextCompat.getColor(context, R.color.accent_neon));
            holder.leaderboardItemRoot.setBackgroundResource(R.drawable.card_surface_user);
        } else {
            holder.userTag.setVisibility(View.GONE);
            holder.playerName.setTextColor(Color.WHITE);
            holder.leaderboardItemRoot.setBackgroundResource(R.drawable.card_surface_bg);
        }

        // Subtext (Location & Time)
        holder.playerSubtext.setText(entry.getLocation() + " • " + entry.getTimeAgo());

        // Score
        holder.playerScore.setText(numberFormat.format(entry.getScore()));

        // Trend badge
        if (entry.isUser()) {
            holder.playerTrend.setText("⭐️ You");
            holder.playerTrend.setTextColor(ContextCompat.getColor(context, R.color.accent_neon));
        } else if (rank <= 3) {
            holder.playerTrend.setText("🏆 Top 3");
            holder.playerTrend.setTextColor(Color.parseColor("#FFD54F"));
        } else if (position % 3 == 0) {
            holder.playerTrend.setText("🔥 Streak");
            holder.playerTrend.setTextColor(Color.parseColor("#FF5722"));
        } else {
            holder.playerTrend.setText("⚡ Active");
            holder.playerTrend.setTextColor(ContextCompat.getColor(context, R.color.accent_green));
        }

        // Avatar circle initial & color
        String initial = "?";
        if (entry.getName() != null && !entry.getName().trim().isEmpty()) {
            initial = String.valueOf(entry.getName().trim().charAt(0)).toUpperCase(Locale.US);
        }
        holder.avatarInitial.setText(initial);

        GradientDrawable circleDrawable = new GradientDrawable();
        circleDrawable.setShape(GradientDrawable.OVAL);
        circleDrawable.setColor(entry.getAvatarColor());
        holder.avatarCircle.setBackground(circleDrawable);
    }

    @Override
    public int getItemCount() {
        return entries != null ? entries.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leaderboardItemRoot;
        TextView playerRank;
        FrameLayout avatarFrame;
        View avatarCircle;
        TextView avatarInitial;
        TextView playerName;
        TextView userTag;
        TextView playerSubtext;
        TextView playerScore;
        TextView playerTrend;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            leaderboardItemRoot = itemView.findViewById(R.id.leaderboardItemRoot);
            playerRank = itemView.findViewById(R.id.playerRank);
            avatarFrame = itemView.findViewById(R.id.avatarFrame);
            avatarCircle = itemView.findViewById(R.id.avatarCircle);
            avatarInitial = itemView.findViewById(R.id.avatarInitial);
            playerName = itemView.findViewById(R.id.playerName);
            userTag = itemView.findViewById(R.id.userTag);
            playerSubtext = itemView.findViewById(R.id.playerSubtext);
            playerScore = itemView.findViewById(R.id.playerScore);
            playerTrend = itemView.findViewById(R.id.playerTrend);
        }
    }
}
