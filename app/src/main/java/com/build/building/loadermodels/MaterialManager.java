package com.build.building.loadermodels;

import android.content.Context;
import android.opengl.GLES30;
import java.util.HashMap;

public class MaterialManager {
    
    private static MaterialManager instance;
    private Context context;
    private HashMap<String, Integer> materialTextures = new HashMap<>();
    private HashMap<String, Integer> materialColors = new HashMap<>();
    
    public static MaterialManager getInstance(Context context) {
        if (instance == null) {
            instance = new MaterialManager(context);
        }
        return instance;
    }
    
    private MaterialManager(Context context) {
        this.context = context;
    }
    
    /**
     * Загрузка всех материалов
     */
    public void loadAllMaterials() {
        loadMaterial("dirt", "materials/dirt_texture.png");
        loadMaterial("stone", "materials/stone_texture.png");
        loadMaterial("glass", "materials/glass_texture.png");
        loadMaterial("brick", "materials/brick_texture.png");
        loadMaterial("wood", "materials/wood_texture.png");
        
        // Цвета для материалов без текстур
        materialColors.put("default", 0xFFCCCCCC);
        materialColors.put("stone", 0xFF888888);
        materialColors.put("dirt", 0xFF8B5A2B);
        materialColors.put("glass", 0x88FFFFFF);
    }
    
    /**
     * Загрузка одного материала
     */
    public void loadMaterial(String name, String texturePath) {
        int textureId = TextureLoader.loadTexture(context, texturePath);
        if (textureId != 0) {
            materialTextures.put(name, textureId);
        }
    }
    
    /**
     * Получение текстуры материала
     */
    public int getMaterialTexture(String name) {
        return materialTextures.getOrDefault(name, 0);
    }
    
    /**
     * Получение цвета материала
     */
    public int getMaterialColor(String name) {
        return materialColors.getOrDefault(name, materialColors.get("default"));
    }
    
    /**
     * Привязка материала для отрисовки
     */
    public void bindMaterial(String name) {
        int textureId = getMaterialTexture(name);
        if (textureId != 0) {
            TextureLoader.bindTexture(textureId);
        } else {
            // Если нет текстуры, используем цвет
            GLES30.glDisable(GLES30.GL_TEXTURE_2D);
        }
    }
    
    /**
     * Проверка, есть ли текстура у материала
     */
    public boolean hasTexture(String name) {
        return materialTextures.containsKey(name);
    }
    
    /**
     * Очистка всех материалов
     */
    public void clear() {
        materialTextures.clear();
        materialColors.clear();
    }
  }
