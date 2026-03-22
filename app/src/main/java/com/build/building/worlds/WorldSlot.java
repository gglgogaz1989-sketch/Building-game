package com.build.building.worlds;

import android.graphics.Bitmap;

public class WorldSlot {
    
    public static final int MAX_SLOTS = 5;
    
    private int slotId;          // 1-5
    private String worldId;
    private String worldName;
    private long lastPlayed;
    private long playTime;
    private int coins;
    private int blockCount;
    private Bitmap thumbnail;
    private boolean isEmpty = true;
    
    public WorldSlot(int slotId) {
        this.slotId = slotId;
        this.worldName = "Пустой слот";
        this.isEmpty = true;
    }
    
    public WorldSlot(int slotId, WorldSaveData data) {
        this.slotId = slotId;
        this.worldId = data.worldId;
        this.worldName = data.worldName;
        this.lastPlayed = data.lastPlayed;
        this.playTime = data.playTime;
        this.coins = data.coins;
        this.blockCount = data.objects.size();
        this.isEmpty = false;
    }
    
    // Геттеры
    public int getSlotId() { return slotId; }
    public String getWorldId() { return worldId; }
    public String getWorldName() { return worldName; }
    public long getLastPlayed() { return lastPlayed; }
    public long getPlayTime() { return playTime; }
    public int getCoins() { return coins; }
    public int getBlockCount() { return blockCount; }
    public boolean isEmpty() { return isEmpty; }
    public Bitmap getThumbnail() { return thumbnail; }
    
    public void setThumbnail(Bitmap thumbnail) { this.thumbnail = thumbnail; }
    public void setWorldName(String name) { this.worldName = name; }
    public void setLastPlayed(long time) { this.lastPlayed = time; }
    public void setPlayTime(long time) { this.playTime = time; }
    public void setCoins(int coins) { this.coins = coins; }
    public void setBlockCount(int count) { this.blockCount = count; }
    public void setEmpty(boolean empty) { this.isEmpty = empty; }
    
    public String getFormattedPlayTime() {
        long hours = playTime / 3600;
        long minutes = (playTime % 3600) / 60;
        long seconds = playTime % 60;
        
        if (hours > 0) {
            return String.format("%dч %02dм", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%dм %02dс", minutes, seconds);
        } else {
            return String.format("%dс", seconds);
        }
    }
    
    public String getFormattedLastPlayed() {
        long diff = System.currentTimeMillis() - lastPlayed;
        if (diff < 60000) {
            return "только что";
        } else if (diff < 3600000) {
            return (diff / 60000) + " мин назад";
        } else if (diff < 86400000) {
            return (diff / 3600000) + " ч назад";
        } else {
            return (diff / 86400000) + " дн назад";
        }
    }
  }
