package com.abhay.blockblastify;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

public class StrokeTextView extends androidx.appcompat.widget.AppCompatTextView {

    private int strokeColor = Color.BLACK;
    private float strokeWidth = 6f;
    private int fillColor = Color.CYAN;

    public StrokeTextView(Context context) {
        super(context);
        init();
    }

    public StrokeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        fillColor = getCurrentTextColor();
    }

    public void setStrokeColor(int color) {
        this.strokeColor = color;
        invalidate();
    }

    public void setStrokeWidth(float width) {
        this.strokeWidth = width;
        invalidate();
    }

    public void setFillColor(int color) {
        this.fillColor = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = getPaint();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        setTextColor(strokeColor);
        super.onDraw(canvas);

        paint.setStyle(Paint.Style.FILL);
        setTextColor(fillColor);
        super.onDraw(canvas);
    }
}