package com.build.building.mods;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Mod {
    
    // Основная информация из mod.json
    private String modId;
    private String name;
    private String version;
    private String author;
    private String description;
    private String iconPath;
    
    // Пути к папкам мода
    private String modFolderPath;
    private String spriteFolderPath;
    private String contentFolderPath;
    
    // Тип мода
    private boolean isBuiltIn = false;
    private boolean isEnabled = true;
    private boolean isLoaded = false;
    
    // Контент
    private List<ModBlock> blocks = new ArrayList<>();
    private List<ModModel> models = new ArrayList<>();
    private List<ModTexture> textures = new ArrayList<>();
    private ModBehavior behavior;
    
    private boolean detailsLoaded = false;
    
    // === Вложенные классы ===
    
    public static class ModBlock {
        public String id;
        public String name;
        public String modelPath;
        public String texturePath;
        public String material;
        public int price;
        
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
    }
    
    // === Конструктор ===
    
    public Mod(String modId, String name, String version, String author) {
        this.modId = modId;
        this.name = name;
        this.version = version;
        this.author = author;
    }
    
    // === Геттеры ===
    
    public String getModId() { return modId; }
    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getAuthor() { return author; }
    public String getDescription() { return description; }
    public String getIconPath() { return iconPath; }
    public String getModFolderPath() { return modFolderPath; }
    public String getSpriteFolderPath() { return spriteFolderPath; }
    public String getContentFolderPath() { return contentFolderPath; }
    public boolean isBuiltIn() { return isBuiltIn; }
    public boolean isEnabled() { return isEnabled; }
    public boolean isLoaded() { return isLoaded; }
    public List<ModBlock> getBlocks() { return blocks; }
    public List<ModModel> getModels() { return models; }
    public List<ModTexture> getTextures() { return textures; }
    public ModBehavior getBehavior() { return behavior; }
    public boolean hasDetailsLoaded() { return detailsLoaded; }
    
    // === Сеттеры ===
    
    public void setDescription(String description) { this.description = description; }
    public void setIconPath(String iconPath) { this.iconPath = iconPath; }
    public void setEnabled(boolean enabled) { this.isEnabled = enabled; }
    public void setLoaded(boolean loaded) { this.isLoaded = loaded; }
    public void setBuiltIn(boolean builtIn) { this.isBuiltIn = builtIn; }
    public void setDetailsLoaded(boolean loaded) { this.detailsLoaded = loaded; }
    
    public void setModFolderPath(String path) { 
        this.modFolderPath = path;
        this.spriteFolderPath = path + File.separator + "sprite";
        this.contentFolderPath = path + File.separator + "content";
    }
    
    public void addBlock(ModBlock block) { blocks.add(block); }
    public void addModel(ModModel model) { models.add(model); }
    public void addTexture(ModTexture texture) { textures.add(texture); }
    public void setBehavior(ModBehavior behavior) { this.behavior = behavior; }
    
    // === Вспомогательные методы ===
    
    public String getFullModelPath(String relativePath) {
        return contentFolderPath + File.separator + relativePath;
    }
    
    public String getFullTexturePath(String relativePath) {
        return spriteFolderPath + File.separator + relativePath;
    }
    
    public ModBlock getBlock(String id) {
        for (ModBlock block : blocks) {
            if (block.id.equals(id)) return block;
        }
        return null;
    }
    }
