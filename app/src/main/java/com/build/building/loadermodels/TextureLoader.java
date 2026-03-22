package com.build.building.loadermodels;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import java.io.InputStream;
import java.util.HashMap;

public class TextureLoader {
    
    private static HashMap<String, Integer> textureCache = new HashMap<>();
    
    /**
     * Загрузка текстуры из assets
     */
    public static int loadTexture(Context context, String assetPath) {
        // Проверяем кэш
        if (textureCache.containsKey(assetPath)) {
            return textureCache.get(assetPath);
        }
        
        int[] textureId = new int[1];
        GLES30.glGenTextures(1, textureId, 0);
        
        if (textureId[0] == 0) {
            return 0;
        }
        
        try {
            InputStream is = context.getAssets().open(assetPath);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId[0]);
            
            // Настройка фильтрации
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
            
            // Загрузка текстуры
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
            
            // Генерация мип-карт
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
            
            bitmap.recycle();
            is.close();
            
            textureCache.put(assetPath, textureId[0]);
            
        } catch (Exception e) {
            e.printStackTrace();
            GLES30.glDeleteTextures(1, textureId, 0);
            return 0;
        }
        
        return textureId[0];
    }
    
    /**
     * Привязка текстуры
     */
    public static void bindTexture(int textureId) {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
    }
    
    /**
     * Удаление текстуры
     */
    public static void deleteTexture(int textureId) {
        if (textureId != 0) {
            int[] ids = {textureId};
            GLES30.glDeleteTextures(1, ids, 0);
        }
    }
    
    /**
     * Очистка кэша текстур
     */
    public static void clearCache() {
        for (int textureId : textureCache.values()) {
            deleteTexture(textureId);
        }
        textureCache.clear();
    }
}
