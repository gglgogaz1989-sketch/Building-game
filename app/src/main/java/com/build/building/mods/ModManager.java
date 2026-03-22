package com.build.building.mods;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModManager {
    
    private static ModManager instance;
    private Context context;
    private ModLoader modLoader;
    
    private Map<String, Mod> loadedMods = new HashMap<>();
    private List<Mod> builtInMods = new ArrayList<>();
    private List<Mod> userMods = new ArrayList<>();
    
    private ModChangeListener listener;
    
    public interface ModChangeListener {
        void onModEnabled(Mod mod);
        void onModDisabled(Mod mod);
        void onModImported(Mod mod);
        void onModRemoved(Mod mod);
    }
    
    public static ModManager getInstance(Context context) {
        if (instance == null) {
            instance = new ModManager(context);
        }
        return instance;
    }
    
    private ModManager(Context context) {
        this.context = context;
        this.modLoader = new ModLoader(context);
        loadAllMods();
    }
    
    /**
     * Загрузка всех модов
     */
    private void loadAllMods() {
        loadedMods.clear();
        builtInMods.clear();
        userMods.clear();
        
        // Загрузка встроенных
        builtInMods = modLoader.loadAllBuiltInMods();
        for (Mod mod : builtInMods) {
            loadedMods.put(mod.getModId(), mod);
        }
        
        // Загрузка пользовательских
        userMods = modLoader.loadAllUserMods();
        for (Mod mod : userMods) {
            if (!loadedMods.containsKey(mod.getModId())) {
                loadedMods.put(mod.getModId(), mod);
            }
        }
    }
    
    /**
     * Импорт мода из ZIP или папки
     */
    public boolean importMod(File sourceFile) {
        try {
            File modsDir = new File(context.getFilesDir(), "mods");
            if (!modsDir.exists()) {
                modsDir.mkdirs();
            }
            
            String modFolderName = sourceFile.getName();
            if (modFolderName.endsWith(".zip")) {
                modFolderName = modFolderName.substring(0, modFolderName.lastIndexOf('.'));
            }
            
            File targetDir = new File(modsDir, modFolderName);
            
            if (sourceFile.isDirectory()) {
                copyDirectory(sourceFile, targetDir);
            } else if (sourceFile.getName().endsWith(".zip")) {
                unzipMod(sourceFile, targetDir);
            } else {
                return false;
            }
            
            // Загружаем импортированный мод
            Mod mod = modLoader.loadUserMod(targetDir);
            if (mod != null) {
                userMods.add(mod);
                loadedMods.put(mod.getModId(), mod);
                if (listener != null) {
                    listener.onModImported(mod);
                }
                return true;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Импорт мода из JSON строки
     */
    public boolean importModFromJson(String jsonString, String modName) {
        try {
            File modsDir = new File(context.getFilesDir(), "mods");
            if (!modsDir.exists()) {
                modsDir.mkdirs();
            }
            
            File modFolder = new File(modsDir, modName);
            modFolder.mkdirs();
            
            File manifestFile = new File(modFolder, "mod.json");
            FileOutputStream fos = new FileOutputStream(manifestFile);
            fos.write(jsonString.getBytes());
            fos.close();
            
            return importMod(modFolder);
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Удаление мода
     */
    public boolean removeMod(String modId) {
        Mod mod = loadedMods.get(modId);
        if (mod == null || mod.isBuiltIn()) return false;
        
        File modFolder = new File(mod.getModFolderPath());
        deleteRecursive(modFolder);
        
        loadedMods.remove(modId);
        userMods.remove(mod);
        
        if (listener != null) {
            listener.onModRemoved(mod);
        }
        
        return true;
    }
    
    /**
     * Включение/выключение мода
     */
    public void setModEnabled(String modId, boolean enabled) {
        Mod mod = loadedMods.get(modId);
        if (mod != null) {
            mod.setEnabled(enabled);
            if (enabled && listener != null) {
                listener.onModEnabled(mod);
            } else if (!enabled && listener != null) {
                listener.onModDisabled(mod);
            }
        }
    }
    
    /**
     * Получение мода
     */
    public Mod getMod(String modId) {
        return loadedMods.get(modId);
    }
    
    /**
     * Все моды
     */
    public List<Mod> getAllMods() {
        List<Mod> all = new ArrayList<>();
        all.addAll(builtInMods);
        all.addAll(userMods);
        return all;
    }
    
    /**
     * Включённые моды
     */
    public List<Mod> getEnabledMods() {
        List<Mod> enabled = new ArrayList<>();
        for (Mod mod : loadedMods.values()) {
            if (mod.isEnabled()) {
                enabled.add(mod);
            }
        }
        return enabled;
    }
    
    /**
     * Блоки из всех включённых модов
     */
    public List<Mod.ModBlock> getAllModBlocks() {
        List<Mod.ModBlock> allBlocks = new ArrayList<>();
        for (Mod mod : getEnabledMods()) {
            allBlocks.addAll(mod.getBlocks());
        }
        return allBlocks;
    }
    
    public void setListener(ModChangeListener listener) {
        this.listener = listener;
    }
    
    public void refresh() {
        loadAllMods();
    }
    
    // === Вспомогательные методы ===
    
    private void copyDirectory(File source, File target) throws Exception {
        if (!target.exists()) target.mkdirs();
        
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                File dest = new File(target, file.getName());
                if (file.isDirectory()) {
                    copyDirectory(file, dest);
                } else {
                    java.nio.file.Files.copy(file.toPath(), dest.toPath());
                }
            }
        }
    }
    
    private void unzipMod(File zipFile, File targetDir) throws Exception {
        // Упрощённая распаковка
        java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new FileInputStream(zipFile));
        java.util.zip.ZipEntry entry;
        byte[] buffer = new byte[1024];
        
        while ((entry = zis.getNextEntry()) != null) {
            File outFile = new File(targetDir, entry.getName());
            if (entry.isDirectory()) {
                outFile.mkdirs();
            } else {
                outFile.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(outFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zis.closeEntry();
        }
        zis.close();
    }
    
    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        file.delete();
    }
              }
