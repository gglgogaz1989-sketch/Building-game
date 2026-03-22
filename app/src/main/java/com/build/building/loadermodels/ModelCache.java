package com.build.building.loadermodels;

import android.content.Context;
import java.util.HashMap;

public class ModelCache {
    
    private static ModelCache instance;
    private HashMap<String, LoadedModel> modelCache = new HashMap<>();
    private Context context;
    
    public static ModelCache getInstance(Context context) {
        if (instance == null) {
            instance = new ModelCache(context);
        }
        return instance;
    }
    
    private ModelCache(Context context) {
        this.context = context;
    }
    
    /**
     * Получение модели из кэша или загрузка
     */
    public LoadedModel getModel(String assetPath) {
        return getModel(assetPath, null);
    }
    
    /**
     * Получение модели из кэша или загрузка с указанием имени
     */
    public LoadedModel getModel(String assetPath, String modelName) {
        if (modelCache.containsKey(assetPath)) {
            return modelCache.get(assetPath);
        }
        
        LoadedModel model = OBJLoader.loadModel(context, assetPath, modelName);
        if (model != null) {
            modelCache.put(assetPath, model);
        }
        return model;
    }
    
    /**
     * Проверка, загружена ли модель
     */
    public boolean isLoaded(String assetPath) {
        return modelCache.containsKey(assetPath);
    }
    
    /**
     * Удаление модели из кэша
     */
    public void removeModel(String assetPath) {
        LoadedModel model = modelCache.remove(assetPath);
        if (model != null) {
            model.deleteBuffers();
        }
    }
    
    /**
     * Очистка кэша
     */
    public void clear() {
        for (LoadedModel model : modelCache.values()) {
            model.deleteBuffers();
        }
        modelCache.clear();
    }
}
