package com.build.building.mods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mod {
    
    // Основная информация
    private String modId;
    private String name;
    private String version;
    private String author;
    private String description;
    private String iconPath;
    
    // Тип мода
    private boolean isBuiltIn = false;
    private boolean isEnabled = true;
    private boolean isLoaded = false;
    
    // Файлы
    private String manifestPath;
    private String installPath;
    
    // Контент мода
    private List<ModBlock> blocks = new ArrayList<>();
    private List<ModModel> models = new ArrayList<>();
    private List<ModTexture> textures = new ArrayList<>();
    private ModBehavior behavior;
    
    // Для ленивой загрузки
    private boolean detailsLoaded = false;
    
    public static class ModBlock {
        public String id;
        public String name;
        public String modelPath;
        public String texturePath;
        public String material;
        public int price;
        public boolean isPlaceable = true;
        
        public ModBlock(String id, String name, String modelPath, String texturePath, int price) {
            this.id = id;
            this.name = name;
            this.modelPath = modelPath;
            this.texturePath = texturePath;
            this.price = price;
            this.material = id;
        }
    }
    
    public static class ModModel {
        public String id;
        public String path;
        public float scale = 1f;
    }
    
    public static class ModTexture {
        public String id;
        public String path;
    }
    
    public static class ModBehavior {
        public String scriptPath;
        public String className;
        public Map<String, Object> parameters = new HashMap<>();
    }
    
    // Конструкторы
    public Mod(String modId, String name, String version, String author) {
        this.modId = modId;
        this.name = name;
        this.version = version;
        this.author = author;
    }
    
    public Mod(String modId, String name, String version, String author, boolean isBuiltIn) {
        this(modId, name, version, author);
        this.isBuiltIn = isBuiltIn;
    }
    
    // Геттеры
    public String getModId() { return modId; }
    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getAuthor() { return author; }
    public String getDescription() { return description; }
    public String getIconPath() { return iconPath; }
    public boolean isBuiltIn() { return isBuiltIn; }
    public boolean isEnabled() { return isEnabled; }
    public boolean isLoaded() { return isLoaded; }
    public List<ModBlock> getBlocks() { return blocks; }
    public List<ModModel> getModels() { return models; }
    public List<ModTexture> getTextures() { return textures; }
    public ModBehavior getBehavior() { return behavior; }
    
    // Сеттеры
    public void setDescription(String description) { this.description = description; }
    public void setIconPath(String iconPath) { this.iconPath = iconPath; }
    public void setEnabled(boolean enabled) { this.isEnabled = enabled; }
    public void setLoaded(boolean loaded) { this.isLoaded = loaded; }
    public void setInstallPath(String path) { this.installPath = path; }
    public void setManifestPath(String path) { this.manifestPath = path; }
    
    public void addBlock(ModBlock block) { blocks.add(block); }
    public void addModel(ModModel model) { models.add(model); }
    public void addTexture(ModTexture texture) { textures.add(texture); }
    public void setBehavior(ModBehavior behavior) { this.behavior = behavior; }
    
    public boolean hasDetailsLoaded() { return detailsLoaded; }
    public void setDetailsLoaded(boolean loaded) { this.detailsLoaded = loaded; }
    
    public String getInstallPath() { return installPath; }
    public String getManifestPath() { return manifestPath; }
    
    /**
     * Получение модели по ID
     */
    public ModModel getModel(String id) {
        for (ModModel model : models) {
            if (model.id.equals(id)) return model;
        }
        return null;
    }
    
    /**
     * Получение текстуры по ID
     */
    public ModTexture getTexture(String id) {
        for (ModTexture texture : textures) {
            if (texture.id.equals(id)) return texture;
        }
        return null;
    }
    
    /**
     * Получение блока по ID
     */
    public ModBlock getBlock(String id) {
        for (ModBlock block : blocks) {
            if (block.id.equals(id)) return block;
        }
        return null;
    }
  }
