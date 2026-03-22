package com.build.building.hud;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import java.io.InputStream;

public class DynamicJoystick extends View {
    
    private Bitmap baseBitmap;
    private Bitmap thumbBitmap;
    private Paint paint;
    
    private float centerX, centerY;
    private float thumbX, thumbY;
    private float thumbRadius = 60f;
    private float baseRadius = 100f;
    
    private float joyX = 0f;
    private float joyY = 0f;
    
    private OnJoystickMoveListener listener;
    private boolean isTouching = false;
    private float deadZone = 0.15f;
    private float alpha = 0.8f;
    
    public interface OnJoystickMoveListener {
        void onMove(float x, float y);
        void onStop();
    }
    
    public DynamicJoystick(Context context, String basePath, String thumbPath) {
        super(context);
        init(context, basePath, thumbPath);
    }
    
    private void init(Context context, String basePath, String thumbPath) {
        paint = new Paint();
        paint.setAntiAlias(true);
        
        try {
            InputStream baseIs = context.getAssets().open(basePath);
            baseBitmap = BitmapFactory.decodeStream(baseIs);
            baseIs.close();
            
            InputStream thumbIs = context.getAssets().open(thumbPath);
            thumbBitmap = BitmapFactory.decodeStream(thumbIs);
            thumbIs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        setFocusable(true);
        setAlpha(alpha);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
        thumbX = centerX;
        thumbY = centerY;
        
        baseRadius = Math.min(w, h) / 2.5f;
        thumbRadius = baseRadius / 2.2f;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        paint.setAlpha((int)(255 * alpha));
        
        if (baseBitmap != null) {
            Rect baseRect = new Rect(
                (int)(centerX - baseRadius),
                (int)(centerY - baseRadius),
                (int)(centerX + baseRadius),
                (int)(centerY + baseRadius)
            );
            canvas.drawBitmap(baseBitmap, null, baseRect, paint);
        } else {
            paint.setColor(0xCC444444);
            canvas.drawCircle(centerX, centerY, baseRadius, paint);
            paint.setColor(0xCC888888);
            canvas.drawCircle(centerX, centerY, baseRadius - 4, paint);
        }
        
        if (thumbBitmap != null) {
            Rect thumbRect = new Rect(
                (int)(thumbX - thumbRadius),
                (int)(thumbY - thumbRadius),
                (int)(thumbX + thumbRadius),
                (int)(thumbY + thumbRadius)
            );
            canvas.drawBitmap(thumbBitmap, null, thumbRect, paint);
        } else {
            paint.setColor(0xCCFFFFFF);
            canvas.drawCircle(thumbX, thumbY, thumbRadius, paint);
            paint.setColor(0xCCDDDDDD);
            canvas.drawCircle(thumbX, thumbY, thumbRadius - 4, paint);
        }
        
        if (isTouching) {
            paint.setColor(0x33FFFFFF);
            canvas.drawCircle(centerX, centerY, baseRadius + 10, paint);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isTouching = true;
                updateThumbPosition(x, y);
                return true;
                
            case MotionEvent.ACTION_MOVE:
                if (isTouching) {
                    updateThumbPosition(x, y);
                }
                return true;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isTouching = false;
                resetJoystick();
                return true;
        }
        return super.onTouchEvent(event);
    }
    
    private void updateThumbPosition(float touchX, float touchY) {
        float dx = touchX - centerX;
        float dy = touchY - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance > baseRadius) {
            dx = dx * baseRadius / distance;
            dy = dy * baseRadius / distance;
            distance = baseRadius;
        }
        
        thumbX = centerX + dx;
        thumbY = centerY + dy;
        
        joyX = dx / baseRadius;
        joyY = dy / baseRadius;
        
        if (Math.abs(joyX) < deadZone) joyX = 0f;
        if (Math.abs(joyY) < deadZone) joyY = 0f;
        
        if (listener != null && isTouching) {
            listener.onMove(joyX, joyY);
        }
        
        invalidate();
    }
    
    private void resetJoystick() {
        thumbX = centerX;
        thumbY = centerY;
        joyX = 0f;
        joyY = 0f;
        
        if (listener != null) {
            listener.onStop();
        }
        
        invalidate();
    }
    
    public void setListener(OnJoystickMoveListener listener) {
        this.listener = listener;
    }
    
    public void setDeadZone(float zone) {
        this.deadZone = zone;
    }
    
    public void setAlpha(float alpha) {
        this.alpha = alpha;
        invalidate();
    }
    
    public float getJoyX() {
        return joyX;
    }
    
    public float getJoyY() {
        return joyY;
    }
    
    public boolean isTouching() {
        return isTouching;
    }
              }
