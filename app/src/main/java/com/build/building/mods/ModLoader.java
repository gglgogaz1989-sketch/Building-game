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
     * Загрузка мода из JSON (из assets)
     */
    public Mod loadModFromAssets(String assetPath) {
        try {
            InputStream is = context.getAssets().open(assetPath);
            return loadModFromStream(is, assetPath);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Загрузка мода из файла (импортированный)
     */
    public Mod loadModFromFile(File jsonFile) {
        try {
            FileInputStream fis = new FileInputStream(jsonFile);
            return loadModFromStream(fis, jsonFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Загрузка мода из InputStream
     */
    private Mod loadModFromStream(InputStream is, String sourcePath) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            
            JsonObject json = JsonParser.parseString(sb.toString()).getAsJsonObject();
            
            // Базовая информация
            String modId = json.get("modId").getAsString();
            String name = json.get("name").getAsString();
            String version = json.get("version").getAsString();
            String author = json.get("author").getAsString();
            
            Mod mod = new Mod(modId, name, version, author);
            mod.setManifestPath(sourcePath);
            
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
            
            // Загрузка поведения (скриптов)
            if (json.has("behavior")) {
                JsonObject behaviorJson = json.get("behavior").getAsJsonObject();
                Mod.ModBehavior behavior = new Mod.ModBehavior();
                if (behaviorJson.has("script")) {
                    behavior.scriptPath = behaviorJson.get("script").getAsString();
                }
                if (behaviorJson.has("class")) {
                    behavior.className = behaviorJson.get("class").getAsString();
                }
                if (behaviorJson.has("parameters")) {
                    behavior.parameters = gson.fromJson(behaviorJson.get("parameters"), 
                                                         new com.google.gson.reflect.TypeToken<java.util.HashMap<String, Object>>(){}.getType());
                }
                mod.setBehavior(behavior);
            }
            
            return mod;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Получение списка всех встроенных модов
     */
    public List<Mod> loadBuiltInMods() {
        List<Mod> mods = new ArrayList<>();
        
        // Встроенный мод "Улучшение ботов"
        Mod enhancedBotsMod = new Mod("enhanced_bots", "Улучшение ботов", "1.0", "Built-in", true);
        enhancedBotsMod.setDescription("Зомби становятся умнее: ломают блоки, открывают двери, меняют структуры");
        enhancedBotsMod.setEnabled(true);
        enhancedBotsMod.setLoaded(true);
        
        mods.add(enhancedBotsMod);
        
        return mods;
    }
        }
