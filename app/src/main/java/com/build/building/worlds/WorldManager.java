package com.build.building.worlds;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class WorldManager {
    
    private static WorldManager instance;
    private Context context;
    private Gson gson;
    
    private WorldSlot[] slots = new WorldSlot[WorldSlot.MAX_SLOTS];
    private WorldSaveData currentWorld;
    private int currentSlot = -1;
    
    private WorldListener listener;
    
    public interface WorldListener {
        void onWorldLoaded(WorldSaveData world, int slot);
        void onWorldSaved(int slot);
        void onWorldDeleted(int slot);
        void onWorldsUpdated();
    }
    
    public static WorldManager getInstance(Context context) {
        if (instance == null) {
            instance = new WorldManager(context);
        }
        return instance;
    }
    
    private WorldManager(Context context) {
        this.context = context;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadSlots();
    }
    
    /**
     * Загрузка всех слотов
     */
    private void loadSlots() {
        File worldsDir = getWorldsDir();
        if (!worldsDir.exists()) {
            worldsDir.mkdirs();
        }
        
        for (int i = 0; i < WorldSlot.MAX_SLOTS; i++) {
            File slotFile = getSlotFile(i + 1);
            if (slotFile.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(slotFile);
                    InputStreamReader reader = new InputStreamReader(fis);
                    WorldSaveData data = gson.fromJson(reader, WorldSaveData.class);
                    reader.close();
                    
                    slots[i] = new WorldSlot(i + 1, data);
                    
                    // Загрузка миниатюры
                    File thumbFile = getThumbFile(i + 1);
                    if (thumbFile.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(thumbFile.getAbsolutePath());
                        slots[i].setThumbnail(bitmap);
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    slots[i] = new WorldSlot(i + 1);
                }
            } else {
                slots[i] = new WorldSlot(i + 1);
            }
        }
    }
    
    /**
     * Создание нового мира
     */
    public boolean createWorld(int slot, String worldName) {
        if (slot < 1 || slot > WorldSlot.MAX_SLOTS) return false;
        if (!slots[slot - 1].isEmpty()) return false;
        
        WorldSaveData newWorld = new WorldSaveData(worldName);
        
        // Начальная позиция
        newWorld.playerX = 0;
        newWorld.playerY = 1.6f;
        newWorld.playerZ = 5;
        newWorld.playerYaw = -90;
        newWorld.playerPitch = 0;
        
        // Начальные монеты
        newWorld.coins = 100;
        
        // Начальные разблокировки
        newWorld.unlockedModels.add("brick");
        
        return saveWorld(slot, newWorld);
    }
    
    /**
     * Сохранение мира
     */
    public boolean saveWorld(int slot, WorldSaveData world) {
        if (slot < 1 || slot > WorldSlot.MAX_SLOTS) return false;
        
        try {
            File slotFile = getSlotFile(slot);
            FileOutputStream fos = new FileOutputStream(slotFile);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            gson.toJson(world, writer);
            writer.close();
            
            // Обновляем слот
            slots[slot - 1] = new WorldSlot(slot, world);
            
            if (listener != null) {
                listener.onWorldSaved(slot);
                listener.onWorldsUpdated();
            }
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Загрузка мира
     */
    public WorldSaveData loadWorld(int slot) {
        if (slot < 1 || slot > WorldSlot.MAX_SLOTS) return null;
        if (slots[slot - 1].isEmpty()) return null;
        
        try {
            File slotFile = getSlotFile(slot);
            FileInputStream fis = new FileInputStream(slotFile);
            InputStreamReader reader = new InputStreamReader(fis);
            WorldSaveData data = gson.fromJson(reader, WorldSaveData.class);
            reader.close();
            
            currentWorld = data;
            currentSlot = slot;
            
            // Обновляем время последнего запуска
            data.lastPlayed = System.currentTimeMillis();
            saveWorld(slot, data);
            
            if (listener != null) {
                listener.onWorldLoaded(data, slot);
            }
            
            return data;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Удаление мира
     */
    public boolean deleteWorld(int slot) {
        if (slot < 1 || slot > WorldSlot.MAX_SLOTS) return false;
        if (slots[slot - 1].isEmpty()) return false;
        
        try {
            File slotFile = getSlotFile(slot);
            slotFile.delete();
            
            File thumbFile = getThumbFile(slot);
            thumbFile.delete();
            
            slots[slot - 1] = new WorldSlot(slot);
            
            if (currentSlot == slot) {
                currentWorld = null;
                currentSlot = -1;
            }
            
            if (listener != null) {
                listener.onWorldDeleted(slot);
                listener.onWorldsUpdated();
            }
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Сохранение миниатюры
     */
    public void saveThumbnail(int slot, Bitmap bitmap) {
        if (slot < 1 || slot > WorldSlot.MAX_SLOTS) return;
        
        try {
            File thumbFile = getThumbFile(slot);
            FileOutputStream fos = new FileOutputStream(thumbFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, fos);
            fos.close();
            
            slots[slot - 1].setThumbnail(bitmap);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Автосохранение текущего мира
     */
    public void autoSave() {
        if (currentWorld != null && currentSlot > 0) {
            saveWorld(currentSlot, currentWorld);
        }
    }
    
    /**
     * Получение текущего мира
     */
    public WorldSaveData getCurrentWorld() {
        return currentWorld;
    }
    
    /**
     * Получение текущего слота
     */
    public int getCurrentSlot() {
        return currentSlot;
    }
    
    /**
     * Получение всех слотов
     */
    public WorldSlot[] getAllSlots() {
        return slots;
    }
    
    /**
     * Получение слота
     */
    public WorldSlot getSlot(int slot) {
        if (slot < 1 || slot > WorldSlot.MAX_SLOTS) return null;
        return slots[slot - 1];
    }
    
    /**
     * Проверка, есть ли свободные слоты
     */
    public boolean hasFreeSlot() {
        for (WorldSlot slot : slots) {
            if (slot.isEmpty()) return true;
        }
        return false;
    }
    
    /**
     * Получение следующего свободного слота
     */
    public int getNextFreeSlot() {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i].isEmpty()) {
                return i + 1;
            }
        }
        return -1;
    }
    
    /**
     * Добавление объекта в текущий мир
     */
    public void addObject(String type, float x, float y, float z) {
        if (currentWorld == null) return;
        
        WorldSaveData.WorldObjectData obj = new WorldSaveData.WorldObjectData(type, x, y, z);
        currentWorld.objects.add(obj);
    }
    
    /**
     * Удаление объекта из текущего мира
     */
    public void removeObject(float x, float y, float z) {
        if (currentWorld == null) return;
        
        float eps = 0.1f;
        for (int i = 0; i < currentWorld.objects.size(); i++) {
            WorldSaveData.WorldObjectData obj = currentWorld.objects.get(i);
            if (Math.abs(obj.x - x) < eps && 
                Math.abs(obj.y - y) < eps && 
                Math.abs(obj.z - z) < eps) {
                currentWorld.objects.remove(i);
                break;
            }
        }
    }
    
    /**
     * Обновление позиции игрока
     */
    public void updatePlayerPosition(float x, float y, float z, float yaw, float pitch) {
        if (currentWorld == null) return;
        
        currentWorld.playerX = x;
        currentWorld.playerY = y;
        currentWorld.playerZ = z;
        currentWorld.playerYaw = yaw;
        currentWorld.playerPitch = pitch;
    }
    
    /**
     * Добавление монет
     */
    public void addCoins(int amount) {
        if (currentWorld == null) return;
        
        currentWorld.coins += amount;
        if (currentWorld.coins < 0) currentWorld.coins = 0;
    }
    
    /**
     * Получение монет
     */
    public int getCoins() {
        return currentWorld != null ? currentWorld.coins : 0;
    }
    
    /**
     * Разблокировка модели
     */
    public void unlockModel(String modelId) {
        if (currentWorld != null && !currentWorld.unlockedModels.contains(modelId)) {
            currentWorld.unlockedModels.add(modelId);
        }
    }
    
    /**
     * Проверка, разблокирована ли модель
     */
    public boolean isModelUnlocked(String modelId) {
        return currentWorld != null && currentWorld.unlockedModels.contains(modelId);
    }
    
    public void setListener(WorldListener listener) {
        this.listener = listener;
    }
    
    // === Приватные методы ===
    
    private File getWorldsDir() {
        return new File(context.getFilesDir(), "worlds");
    }
    
    private File getSlotFile(int slot) {
        return new File(getWorldsDir(), "world_" + slot + ".json");
    }
    
    private File getThumbFile(int slot) {
        return new File(getWorldsDir(), "thumb_" + slot + ".png");
    }
                  }
