package com.build.building.mods;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ModImporter {
    
    private Context context;
    private ModManager modManager;
    private ImportCallback callback;
    
    public interface ImportCallback {
        void onImportSuccess(Mod mod);
        void onImportFailed(String error);
    }
    
    public ModImporter(Context context) {
        this.context = context;
        this.modManager = ModManager.getInstance(context);
    }
    
    /**
     * Создание лаунчера для выбора ZIP или JSON файла
     */
    public ActivityResultLauncher<String> createFilePickerLauncher(Activity activity) {
        return activity.registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    importFromUri(uri);
                }
            }
        );
    }
    
    /**
     * Импорт из URI
     */
    public void importFromUri(Uri uri) {
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is == null) {
                if (callback != null) callback.onImportFailed("Не удалось открыть файл");
                return;
            }
            
            // Сохраняем во временный файл
            File tempFile = new File(context.getCacheDir(), "temp_mod." + getFileExtension(uri));
            FileOutputStream fos = new FileOutputStream(tempFile);
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            is.close();
            
            // Импортируем
            boolean success = modManager.importMod(tempFile);
            
            if (success) {
                if (callback != null) callback.onImportSuccess(null);
                Toast.makeText(context, "Мод успешно импортирован", Toast.LENGTH_SHORT).show();
            } else {
                if (callback != null) callback.onImportFailed("Неверный формат мода");
                Toast.makeText(context, "Ошибка импорта", Toast.LENGTH_SHORT).show();
            }
            
            tempFile.delete();
            
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) callback.onImportFailed(e.getMessage());
            Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Импорт из JSON строки
     */
    public boolean importFromJson(String jsonString, String modName) {
        return modManager.importModFromJson(jsonString, modName);
    }
    
    private String getFileExtension(Uri uri) {
        String path = uri.toString();
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex > 0) {
            return path.substring(dotIndex);
        }
        return ".zip";
    }
    
    public void setCallback(ImportCallback callback) {
        this.callback = callback;
    }
}
