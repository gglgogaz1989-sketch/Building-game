package com.build.building.loadermodels;

import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class OBJLoader {
    
    /**
     * Загрузка .obj модели из assets
     */
    public static LoadedModel loadModel(Context context, String assetPath) {
        return loadModel(context, assetPath, null);
    }
    
    /**
     * Загрузка .obj модели с указанием имени
     */
    public static LoadedModel loadModel(Context context, String assetPath, String modelName) {
        if (modelName == null) {
            modelName = assetPath.substring(assetPath.lastIndexOf('/') + 1);
            if (modelName.contains(".")) {
                modelName = modelName.substring(0, modelName.lastIndexOf('.'));
            }
        }
        
        LoadedModel model = new LoadedModel(modelName);
        
        try {
            InputStream inputStream = context.getAssets().open(assetPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            
            ArrayList<float[]> vertices = new ArrayList<>();
            ArrayList<float[]> texCoords = new ArrayList<>();
            ArrayList<float[]> normals = new ArrayList<>();
            ArrayList<int[]> faces = new ArrayList<>();
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                String[] parts = line.split("\\s+");
                if (parts.length == 0) continue;
                
                switch (parts[0]) {
                    case "v":
                        // Вершина
                        float[] v = new float[3];
                        v[0] = Float.parseFloat(parts[1]);
                        v[1] = Float.parseFloat(parts[2]);
                        v[2] = Float.parseFloat(parts[3]);
                        vertices.add(v);
                        break;
                        
                    case "vt":
                        // Текстурная координата
                        float[] vt = new float[2];
                        vt[0] = Float.parseFloat(parts[1]);
                        vt[1] = 1 - Float.parseFloat(parts[2]); // инвертируем Y для OpenGL
                        texCoords.add(vt);
                        break;
                        
                    case "vn":
                        // Нормаль
                        float[] vn = new float[3];
                        vn[0] = Float.parseFloat(parts[1]);
                        vn[1] = Float.parseFloat(parts[2]);
                        vn[2] = Float.parseFloat(parts[3]);
                        normals.add(vn);
                        break;
                        
                    case "f":
                        // Грань (треугольник)
                        int[] face = parseFace(parts);
                        if (face != null) {
                            faces.add(face);
                        }
                        break;
                }
            }
            
            reader.close();
            inputStream.close();
            
            // Сборка данных для OpenGL
            buildModelData(model, vertices, texCoords, normals, faces);
            
            // Создание буферов
            model.createBuffers();
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        return model;
    }
    
    /**
     * Разбор строки грани
     */
    private static int[] parseFace(String[] parts) {
        if (parts.length < 4) return null;
        
        int[] face = new int[9]; // 3 вершины * 3 индекса (v, vt, vn)
        
        for (int i = 0; i < 3; i++) {
            String[] indices = parts[i + 1].split("/");
            
            // Индекс вершины
            int vIdx = Integer.parseInt(indices[0]) - 1;
            face[i * 3] = vIdx;
            
            // Индекс текстурной координаты
            if (indices.length > 1 && !indices[1].isEmpty()) {
                face[i * 3 + 1] = Integer.parseInt(indices[1]) - 1;
            } else {
                face[i * 3 + 1] = -1;
            }
            
            // Индекс нормали
            if (indices.length > 2 && !indices[2].isEmpty()) {
                face[i * 3 + 2] = Integer.parseInt(indices[2]) - 1;
            } else {
                face[i * 3 + 2] = -1;
            }
        }
        
        return face;
    }
    
    /**
     * Сборка данных модели из списков
     */
    private static void buildModelData(LoadedModel model, 
                                        ArrayList<float[]> vertices,
                                        ArrayList<float[]> texCoords,
                                        ArrayList<float[]> normals,
                                        ArrayList<int[]> faces) {
        
        ArrayList<Float> vertexList = new ArrayList<>();
        ArrayList<Float> texCoordList = new ArrayList<>();
        ArrayList<Float> normalList = new ArrayList<>();
        ArrayList<Short> indexList = new ArrayList<>();
        
        HashMap<String, Short> vertexCache = new HashMap<>();
        short currentIndex = 0;
        
        for (int[] face : faces) {
            for (int i = 0; i < 3; i++) {
                int vIdx = face[i * 3];
                int vtIdx = face[i * 3 + 1];
                int vnIdx = face[i * 3 + 2];
                
                String key = vIdx + "|" + vtIdx + "|" + vnIdx;
                
                if (vertexCache.containsKey(key)) {
                    indexList.add(vertexCache.get(key));
                } else {
                    // Добавляем вершину
                    float[] v = vertices.get(vIdx);
                    vertexList.add(v[0]);
                    vertexList.add(v[1]);
                    vertexList.add(v[2]);
                    
                    // Добавляем текстурную координату
                    if (vtIdx >= 0 && vtIdx < texCoords.size()) {
                        float[] vt = texCoords.get(vtIdx);
                        texCoordList.add(vt[0]);
                        texCoordList.add(vt[1]);
                    } else {
                        texCoordList.add(0f);
                        texCoordList.add(0f);
                    }
                    
                    // Добавляем нормаль
                    if (vnIdx >= 0 && vnIdx < normals.size()) {
                        float[] vn = normals.get(vnIdx);
                        normalList.add(vn[0]);
                        normalList.add(vn[1]);
                        normalList.add(vn[2]);
                    } else {
                        normalList.add(0f);
                        normalList.add(1f);
                        normalList.add(0f);
                    }
                    
                    vertexCache.put(key, currentIndex);
                    indexList.add(currentIndex);
                    currentIndex++;
                }
            }
        }
        
        // Конвертируем в массивы
        model.vertices = new float[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) {
            model.vertices[i] = vertexList.get(i);
        }
        model.vertexCount = vertexList.size() / 3;
        
        model.texCoords = new float[texCoordList.size()];
        for (int i = 0; i < texCoordList.size(); i++) {
            model.texCoords[i] = texCoordList.get(i);
        }
        
        model.normals = new float[normalList.size()];
        for (int i = 0; i < normalList.size(); i++) {
            model.normals[i] = normalList.get(i);
        }
        
        model.indices = new short[indexList.size()];
        for (int i = 0; i < indexList.size(); i++) {
            model.indices[i] = indexList.get(i);
        }
        model.indexCount = indexList.size();
        model.hasIndices = true;
    }
            }
