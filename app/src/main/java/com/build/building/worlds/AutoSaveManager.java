package com.build.building.worlds;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class AutoSaveManager {
    
    private static AutoSaveManager instance;
    private WorldManager worldManager;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private Handler mainHandler;
    
    private boolean isRunning = false;
    private int autoSaveInterval = 30000; // 30 секунд
    private Runnable autoSaveRunnable;
    
    public static AutoSaveManager getInstance(WorldManager worldManager) {
        if (instance == null) {
            instance = new AutoSaveManager(worldManager);
        }
        return instance;
    }
    
    private AutoSaveManager(WorldManager worldManager) {
        this.worldManager = worldManager;
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        backgroundThread = new HandlerThread("AutoSaveThread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
        
        autoSaveRunnable = () -> {
            if (isRunning) {
                worldManager.autoSave();
                scheduleNext();
            }
        };
    }
    
    public void start() {
        if (isRunning) return;
        isRunning = true;
        scheduleNext();
    }
    
    public void stop() {
        isRunning = false;
        backgroundHandler.removeCallbacks(autoSaveRunnable);
    }
    
    private void scheduleNext() {
        if (isRunning) {
            backgroundHandler.postDelayed(autoSaveRunnable, autoSaveInterval);
        }
    }
    
    public void setAutoSaveInterval(int milliseconds) {
        this.autoSaveInterval = milliseconds;
    }
    
    public void saveNow() {
        backgroundHandler.post(() -> worldManager.autoSave());
    }
    
    public void onDestroy() {
        stop();
        backgroundThread.quitSafely();
    }
}
