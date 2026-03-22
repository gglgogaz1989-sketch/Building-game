package com.build.building.hud;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import java.io.InputStream;

public class CoinDisplay extends View {
    
    private Bitmap coinIcon;
    private Paint textPaint;
    private Paint bgPaint;
    private int coinCount = 0;
    private boolean showBackground = true;
    private float alpha = 1f;
    
    public CoinDisplay(Context context) {
        super(context);
        init(context);
    }
    
    private void init(Context context) {
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(0xFFFFD700);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setShadowLayer(4, 2, 2, 0x88000000);
        
        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(0xAA000000);
        
        try {
            InputStream is = context.getAssets().open("hud/coin_icon.png");
            coinIcon = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setCoinCount(int count) {
        this.coinCount = count;
        invalidate();
    }
    
    public void addCoins(int amount) {
        this.coinCount += amount;
        invalidate();
    }
    
    public void removeCoins(int amount) {
        this.coinCount -= amount;
        if (coinCount < 0) coinCount = 0;
        invalidate();
    }
    
    public int getCoinCount() {
        return coinCount;
    }
    
    public void setShowBackground(boolean show) {
        this.showBackground = show;
        invalidate();
    }
    
    public void setAlpha(float alpha) {
        this.alpha = alpha;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        if (width == 0 || height == 0) {
            // Устанавливаем размер
            setMeasuredDimension(dpToPx(120), dpToPx(48));
            return;
        }
        
        textPaint.setAlpha((int)(255 * alpha));
        bgPaint.setAlpha((int)(200 * alpha));
        
        if (showBackground) {
            canvas.drawRoundRect(0, 0, width, height, 24, 24, bgPaint);
        }
        
        int iconSize = height - dpToPx(8);
        int iconLeft = dpToPx(8);
        int iconTop = dpToPx(4);
        
        if (coinIcon != null) {
            canvas.drawBitmap(coinIcon, null, 
                new Rect(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize), 
                bgPaint);
        }
        
        String text = String.valueOf(coinCount);
        float textX = iconLeft + iconSize + dpToPx(8);
        float textY = height / 2f + textPaint.getTextSize() / 3f;
        canvas.drawText(text, textX, textY, textPaint);
    }
    
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(dpToPx(120), dpToPx(48));
    }
    }
