package com.example.blockpuzzlegame;

public class Block {

    public int[][] shape;
    public int color;

    public float x, y;

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
