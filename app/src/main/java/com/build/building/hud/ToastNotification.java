package com.build.building.hud;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ToastNotification {
    
    private static Toast currentToast;
    private static Handler handler = new Handler(Looper.getMainLooper());
    
    public static void show(Context context, String message) {
        show(context, message, Toast.LENGTH_SHORT);
    }
    
    public static void show(Context context, String message, int duration) {
        handler.post(() -> {
            if (currentToast != null) {
                currentToast.cancel();
            }
            
            currentToast = Toast.makeText(context, message, duration);
            currentToast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, dpToPx(context, 80));
            currentToast.show();
        });
    }
    
    public static void showLong(Context context, String message) {
        show(context, message, Toast.LENGTH_LONG);
    }
    
    public static void showCustom(Context context, String message, int duration, int bgColor, int textColor) {
        handler.post(() -> {
            if (currentToast != null) {
                currentToast.cancel();
            }
            
            Toast toast = new Toast(context);
            View view = new TextView(context);
            TextView textView = (TextView) view;
            textView.setText(message);
            textView.setTextColor(textColor);
            textView.setBackgroundColor(bgColor);
            textView.setPadding(dpToPx(context, 16), dpToPx(context, 8), dpToPx(context, 16), dpToPx(context, 8));
            textView.setTextSize(14);
            
            toast.setView(view);
            toast.setDuration(duration);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, dpToPx(context, 80));
            toast.show();
            
            currentToast = toast;
        });
    }
    
    public static void cancel() {
        handler.post(() -> {
            if (currentToast != null) {
                currentToast.cancel();
                currentToast = null;
            }
        });
    }
    
    private static int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}
