package com.abhay.blockblastify;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LevelsAdapter extends RecyclerView.Adapter<LevelsAdapter.ViewHolder> {

    public interface OnLevelClickListener {
        void onLevelClick(int level);
    }

    private final Context context;
    private final int totalLevels;
    private int unlockedLevel;
    private final OnLevelClickListener listener;

    public LevelsAdapter(Context context, OnLevelClickListener listener) {
        this.context = context;
        this.totalLevels = LevelManager.TOTAL_LEVELS;
        this.unlockedLevel = LevelManager.getUnlockedLevel(context);
        this.listener = listener;
    }

    public void refresh() {
        this.unlockedLevel = LevelManager.getUnlockedLevel(context);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_level, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int level = position + 1;
        holder.levelNumberText.setText(String.valueOf(level));

        if (level < unlockedLevel) {
            // Completed Level
            holder.levelItemRoot.setBackgroundResource(R.drawable.level_cell_bg);
            holder.levelNumberText.setTextColor(Color.WHITE);
            holder.starsContainer.setVisibility(View.VISIBLE);
            holder.currentPlayBadge.setVisibility(View.GONE);
            holder.lockedIcon.setVisibility(View.GONE);
            holder.levelItemRoot.setOnClickListener(v -> {
                if (listener != null) listener.onLevelClick(level);
            });
        } else if (level == unlockedLevel) {
            // Current Unlocked Level
            holder.levelItemRoot.setBackgroundResource(R.drawable.level_cell_current);
            holder.levelNumberText.setTextColor(Color.parseColor("#FF9100"));
            holder.starsContainer.setVisibility(View.GONE);
            holder.currentPlayBadge.setVisibility(View.VISIBLE);
            holder.lockedIcon.setVisibility(View.GONE);
            holder.levelItemRoot.setOnClickListener(v -> {
                if (listener != null) listener.onLevelClick(level);
            });
        } else {
            // Locked Level
            holder.levelItemRoot.setBackgroundResource(R.drawable.level_cell_locked);
            holder.levelNumberText.setTextColor(Color.parseColor("#475569"));
            holder.starsContainer.setVisibility(View.GONE);
            holder.currentPlayBadge.setVisibility(View.GONE);
            holder.lockedIcon.setVisibility(View.VISIBLE);
            holder.levelItemRoot.setOnClickListener(v -> {
                Toast.makeText(context, "Complete Level " + (level - 1) + " to unlock!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return totalLevels;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout levelItemRoot;
        TextView levelNumberText;
        LinearLayout starsContainer;
        TextView currentPlayBadge;
        ImageView lockedIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            levelItemRoot = itemView.findViewById(R.id.levelItemRoot);
            levelNumberText = itemView.findViewById(R.id.levelNumberText);
            starsContainer = itemView.findViewById(R.id.starsContainer);
            currentPlayBadge = itemView.findViewById(R.id.currentPlayBadge);
            lockedIcon = itemView.findViewById(R.id.lockedIcon);
        }
    }
}
