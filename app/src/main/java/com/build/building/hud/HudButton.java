package com.build.building.hud;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import java.io.InputStream;

public class HudButton extends View {
    
    private Bitmap backgroundBitmap;
    private Bitmap iconBitmap;
    private Paint paint;
    private Paint textPaint;
    private OnHudButtonListener listener;
    private String buttonId;
    private boolean isPressed = false;
    private String overlayText = "";
    private float textSize = 24f;
    private int textColor = Color.WHITE;
    private boolean showText = false;
    private float alpha = 1f;
    private boolean isVisible = true;
    
    public interface OnHudButtonListener {
        void onTap(String id);
        void onLongPress(String id);
        void onRelease(String id);
    }
    
    // Конструктор с фоном из assets
    public HudButton(Context context, String assetPath, String id) {
        super(context);
        this.buttonId = id;
        init(context, assetPath, null);
    }
    
    // Конструктор с фоном + иконкой поверх
    public HudButton(Context context, String backgroundPath, String iconPath, String id) {
        super(context);
        this.buttonId = id;
        init(context, backgroundPath, iconPath);
    }
    
    private void init(Context context, String backgroundPath, String iconPath) {
        paint = new Paint();
        paint.setAntiAlias(true);
        
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        loadBackground(context, backgroundPath);
        
        if (iconPath != null) {
            loadIcon(context, iconPath);
        }
        
        setClickable(true);
        setFocusable(true);
    }
    
    private void loadBackground(Context context, String assetPath) {
        try {
            InputStream is = context.getAssets().open(assetPath);
            backgroundBitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadIcon(Context context, String assetPath) {
        try {
            InputStream is = context.getAssets().open(assetPath);
            iconBitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setOverlayText(String text) {
        this.overlayText = text;
        this.showText = !text.isEmpty();
        invalidate();
    }
    
    public void setTextStyle(float size, int color) {
        this.textSize = size;
        this.textColor = color;
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        invalidate();
    }
    
    public void setBackgroundImage(Context context, String assetPath) {
        loadBackground(context, assetPath);
        invalidate();
    }
    
    public void setIconImage(Context context, String assetPath) {
        loadIcon(context, assetPath);
        invalidate();
    }
    
    public void setAlpha(float alpha) {
        this.alpha = alpha;
        invalidate();
    }
    
    public void setVisible(boolean visible) {
        this.isVisible = visible;
        setVisibility(visible ? VISIBLE : GONE);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (!isVisible) return;
        
        int width = getWidth();
        int height = getHeight();
        
        if (width == 0 || height == 0) return;
        
        paint.setAlpha((int)(255 * alpha));
        
        if (backgroundBitmap != null) {
            canvas.drawBitmap(backgroundBitmap, null, new Rect(0, 0, width, height), paint);
        } else {
            // Фон по умолчанию
            paint.setColor(0xCC000000);
            canvas.drawRoundRect(0, 0, width, height, 16, 16, paint);
            paint.setColor(0xFFFFFFFF);
            paint.setStrokeWidth(2);
            canvas.drawRoundRect(2, 2, width - 2, height - 2, 14, 14, paint);
        }
        
        if (iconBitmap != null) {
            int iconSize = Math.min(width, height) / 2;
            int left = (width - iconSize) / 2;
            int top = (height - iconSize) / 2;
            canvas.drawBitmap(iconBitmap, null, new Rect(left, top, left + iconSize, top + iconSize), paint);
        }
        
        if (showText && !overlayText.isEmpty()) {
            textPaint.setTextSize(textSize);
            textPaint.setColor(textColor);
            textPaint.setAlpha((int)(255 * alpha));
            float x = width / 2f;
            float y = height / 2f + textSize / 3f;
            canvas.drawText(overlayText, x, y, textPaint);
        }
        
        if (isPressed) {
            paint.setColor(0x33FFFFFF);
            canvas.drawRect(0, 0, width, height, paint);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || !isVisible) return false;
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isPressed = true;
                invalidate();
                if (listener != null) {
                    listener.onTap(buttonId);
                }
                postDelayed(longPressRunnable, 500);
                return true;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isPressed = false;
                invalidate();
                removeCallbacks(longPressRunnable);
                if (listener != null) {
                    listener.onRelease(buttonId);
                }
                return true;
        }
        return super.onTouchEvent(event);
    }
    
    private Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPressed && listener != null) {
                listener.onLongPress(buttonId);
            }
        }
    };
    
    public void setListener(OnHudButtonListener listener) {
        this.listener = listener;
    }
    
    public String getButtonId() {
        return buttonId;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (backgroundBitmap != null) {
            setMeasuredDimension(backgroundBitmap.getWidth(), backgroundBitmap.getHeight());
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
                    }
