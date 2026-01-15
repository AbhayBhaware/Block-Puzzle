package com.example.blockpuzzlegame;

public class Block {

    public int[][] shape;
    public int color;

    public float startX;
    public float startY;

    public float x, y;

    public float scale = 0.6f;
    public float targetScale = 0.6f;

    public Block(int[][] shape, int color) {
        this.shape = shape;
        this.color = color;
    }

    public int getWidth() {
        return shape[0].length;
    }

    public int getHeight() {
        return shape.length;
    }
}
