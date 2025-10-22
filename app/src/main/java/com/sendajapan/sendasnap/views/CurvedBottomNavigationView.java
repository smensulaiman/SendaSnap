package com.sendajapan.sendasnap.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sendajapan.sendasnap.R;

public class CurvedBottomNavigationView extends BottomNavigationView {

    private Paint paint;
    private Path path;
    private int curveHeight = 20;
    private int selectedItem = 0;
    private int itemCount = 3;

    public CurvedBottomNavigationView(Context context) {
        super(context);
        init();
    }

    public CurvedBottomNavigationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CurvedBottomNavigationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getContext().getColor(R.color.primary));

        path = new Path();

        // Set background to transparent so we can draw our custom curve
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCurve(canvas);
    }

    private void drawCurve(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        // Calculate item width
        int itemWidth = width / itemCount;

        // Calculate curve position based on selected item
        int curveCenterX = (selectedItem * itemWidth) + (itemWidth / 2);

        // Create the curved path
        path.reset();

        // Start from left edge
        path.moveTo(0, height);

        // Draw left side curve
        path.quadTo(
                curveCenterX - itemWidth, height - curveHeight,
                curveCenterX - itemWidth / 2, height - curveHeight);

        // Draw center curve (the main curve)
        path.quadTo(
                curveCenterX, height - curveHeight - 10,
                curveCenterX + itemWidth / 2, height - curveHeight);

        // Draw right side curve
        path.quadTo(
                curveCenterX + itemWidth, height - curveHeight,
                width, height);

        // Close the path
        path.lineTo(width, height);
        path.lineTo(0, height);
        path.close();

        // Draw the curve
        canvas.drawPath(path, paint);
    }

    public void setSelectedItem(int position) {
        this.selectedItem = position;
        invalidate(); // Redraw the curve
    }

    public void setItemCount(int count) {
        this.itemCount = count;
        invalidate();
    }

    public void setCurveHeight(int height) {
        this.curveHeight = height;
        invalidate();
    }
}
