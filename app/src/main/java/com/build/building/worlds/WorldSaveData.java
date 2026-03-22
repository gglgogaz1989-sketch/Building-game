package com.build.building.worlds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldSaveData {
    
    public String worldName;
    public String worldId;
    public long createdAt;
    public long lastPlayed;
    public int playTime; // секунды
    
    // Игрок
    public float playerX;
    public float playerY;
    public float playerZ;
    public float playerYaw;
    public float playerPitch;
    
    // Валюта
    public int coins;
    
    // Разблокированные предметы
    public List<String> unlockedModels = new ArrayList<>();
    public List<String> unlockedMods = new ArrayList<>();
    
    // Объекты в мире
    public List<WorldObjectData> objects = new ArrayList<>();
    
    // Моды (включённые)
    public List<String> enabledMods = new ArrayList<>();
    
    // Версия сохранения (для миграции)
    public int version = 1;
    
    public WorldSaveData() {
        this.worldName = "Новый мир";
        this.worldId = generateWorldId();
        this.createdAt = System.currentTimeMillis();
        this.lastPlayed = System.currentTimeMillis();
    }
    
    public WorldSaveData(String worldName) {
        this();
        this.worldName = worldName;
    }
    
    private String generateWorldId() {
        return "world_" + System.currentTimeMillis();
    }
    
    public static class WorldObjectData {
        public String type;      // "brick", "stone", и т.д.
        public String modelId;
        public float x, y, z;
        public float rotX, rotY, rotZ;
        public float scaleX, scaleY, scaleZ;
        public String material;
        
        public WorldObjectData() {}
        
        public WorldObjectData(String type, float x, float y, float z) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
            this.rotY = 0;
            this.scaleX = this.scaleY = this.scaleZ = 0.5f;
            this.material = type;
        }
    }
}
