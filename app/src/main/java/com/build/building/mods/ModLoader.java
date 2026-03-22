package com.build.building.mods;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ModLoader {
    
    private Context context;
    private Gson gson;
    
    public ModLoader(Context context) {
        this.context = context;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    /**
     * Загрузка всех встроенных модов из assets/mods/
     */
    public List<Mod> loadAllBuiltInMods() {
        List<Mod> mods = new ArrayList<>();
        
        try {
            String[] modFolders = context.getAssets().list("mods");
            if (modFolders != null) {
                for (String folder : modFolders) {
                    Mod mod = loadBuiltInMod(folder);
                    if (mod != null) {
                        mods.add(mod);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Встроенный мод "Улучшение ботов" (если нет папки)
        if (mods.isEmpty()) {
            Mod enhancedMod = new Mod("enhanced_bots", "Улучшение ботов", "1.0", "Built-in");
            enhancedMod.setDescription("Зомби становятся умнее: ломают блоки, открывают двери");
            enhancedMod.setBuiltIn(true);
            enhancedMod.setLoaded(true);
            enhancedMod.setDetailsLoaded(true);
            mods.add(enhancedMod);
        }
        
        return mods;
    }
    
    /**
     * Загрузка одного встроенного мода из assets/mods/название/
     */
    public Mod loadBuiltInMod(String modFolderName) {
        try {
            String manifestPath = "mods/" + modFolderName + "/mod.json";
            InputStream is = context.getAssets().open(manifestPath);
            return loadModFromStream(is, modFolderName, true);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Загрузка пользовательского мода из папки files/mods/название/
     */
    public Mod loadUserMod(File modFolder) {
        try {
            File manifestFile = new File(modFolder, "mod.json");
            if (!manifestFile.exists()) return null;
            
            FileInputStream fis = new FileInputStream(manifestFile);
            return loadModFromStream(fis, modFolder.getAbsolutePath(), false);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Загрузка всех пользовательских модов из files/mods/
     */
    public List<Mod> loadAllUserMods() {
        List<Mod> mods = new ArrayList<>();
        File modsDir = new File(context.getFilesDir(), "mods");
        
        if (modsDir.exists()) {
            File[] folders = modsDir.listFiles(File::isDirectory);
            if (folders != null) {
                for (File folder : folders) {
                    Mod mod = loadUserMod(folder);
                    if (mod != null) {
                        mods.add(mod);
                    }
                }
            }
        }
        
        return mods;
    }
    
    /**
     * Основная загрузка из InputStream
     */
    private Mod loadModFromStream(InputStream is, String sourcePath, boolean isBuiltIn) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            
            JsonObject json = JsonParser.parseString(sb.toString()).getAsJsonObject();
            
            String modId = json.get("modId").getAsString();
            String name = json.get("name").getAsString();
            String version = json.get("version").getAsString();
            String author = json.get("author").getAsString();
            
            Mod mod = new Mod(modId, name, version, author);
            mod.setBuiltIn(isBuiltIn);
            
            if (isBuiltIn) {
                mod.setModFolderPath("mods/" + sourcePath);
            } else {
                mod.setModFolderPath(sourcePath);
            }
            
            if (json.has("description")) {
                mod.setDescription(json.get("description").getAsString());
            }
            if (json.has("icon")) {
                mod.setIconPath(json.get("icon").getAsString());
            }
            
            // Загрузка блоков
            if (json.has("blocks")) {
                json.get("blocks").getAsJsonArray().forEach(blockElement -> {
                    JsonObject blockJson = blockElement.getAsJsonObject();
                    String id = blockJson.get("id").getAsString();
                    String blockName = blockJson.get("name").getAsString();
                    String modelPath = blockJson.get("model").getAsString();
                    String texturePath = blockJson.has("texture") ? blockJson.get("texture").getAsString() : null;
                    int price = blockJson.has("price") ? blockJson.get("price").getAsInt() : 0;
                    
                    Mod.ModBlock block = new Mod.ModBlock(id, blockName, modelPath, texturePath, price);
                    if (blockJson.has("material")) {
                        block.material = blockJson.get("material").getAsString();
                    }
                    mod.addBlock(block);
                });
            }
            
            // Загрузка моделей
            if (json.has("models")) {
                json.get("models").getAsJsonArray().forEach(modelElement -> {
                    JsonObject modelJson = modelElement.getAsJsonObject();
                    Mod.ModModel model = new Mod.ModModel();
                    model.id = modelJson.get("id").getAsString();
                    model.path = modelJson.get("path").getAsString();
                    if (modelJson.has("scale")) {
                        model.scale = modelJson.get("scale").getAsFloat();
                    }
                    mod.addModel(model);
                });
            }
            
            // Загрузка текстур
            if (json.has("textures")) {
                json.get("textures").getAsJsonArray().forEach(textureElement -> {
                    JsonObject textureJson = textureElement.getAsJsonObject();
                    Mod.ModTexture texture = new Mod.ModTexture();
                    texture.id = textureJson.get("id").getAsString();
                    texture.path = textureJson.get("path").getAsString();
                    mod.addTexture(texture);
                });
            }
            
            mod.setDetailsLoaded(true);
            mod.setLoaded(true);
            
            return mod;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
                        }
