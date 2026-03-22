package com.build.building.render;

import android.opengl.Matrix;
import com.build.building.loadermodels.LoadedModel;

public class GameObject {
    
    // Модель
    public LoadedModel model;
    public String modelId;
    
    // Позиция
    public float x, y, z;
    
    // Вращение (градусы)
    public float rotX, rotY, rotZ;
    
    // Масштаб
    public float scaleX = 1f;
    public float scaleY = 1f;
    public float scaleZ = 1f;
    
    // Материал
    public String material = "default";
    
    // Тип объекта (для логики)
    public String type; // "brick", "map", "door", "zombie"
    
    // Уникальный ID
    public String uid;
    
    // Матрица модели
    private float[] modelMatrix = new float[16];
    private boolean matrixDirty = true;
    
    // Статический счётчик для ID
    private static int nextId = 0;
    
    public GameObject(LoadedModel model, float x, float y, float z) {
        this.model = model;
        this.x = x;
        this.y = y;
        this.z = z;
        this.uid = generateUid();
        updateMatrix();
    }
    
    public GameObject(LoadedModel model, String modelId, float x, float y, float z) {
        this.model = model;
        this.modelId = modelId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.uid = generateUid();
        updateMatrix();
    }
    
    private String generateUid() {
        return "obj_" + (nextId++) + "_" + System.currentTimeMillis();
    }
    
    /**
     * Обновление матрицы модели
     */
    public void updateMatrix() {
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, x, y, z);
        Matrix.rotateM(modelMatrix, 0, rotX, 1, 0, 0);
        Matrix.rotateM(modelMatrix, 0, rotY, 0, 1, 0);
        Matrix.rotateM(modelMatrix, 0, rotZ, 0, 0, 1);
        Matrix.scaleM(modelMatrix, 0, scaleX, scaleY, scaleZ);
        matrixDirty = false;
    }
    
    /**
     * Получение матрицы модели
     */
    public float[] getModelMatrix() {
        if (matrixDirty) {
            updateMatrix();
        }
        return modelMatrix;
    }
    
    /**
     * Отрисовка объекта
     */
    public void draw(int positionHandle, int texCoordHandle, int normalHandle, 
                     int mvpHandle, float[] viewProjectionMatrix) {
        if (model == null) return;
        
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, viewProjectionMatrix, 0, getModelMatrix(), 0);
        android.opengl.GLES30.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0);
        
        model.draw(positionHandle, texCoordHandle, normalHandle);
    }
    
    /**
     * Установка позиции
     */
    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        matrixDirty = true;
    }
    
    /**
     * Установка вращения
     */
    public void setRotation(float rotX, float rotY, float rotZ) {
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        matrixDirty = true;
    }
    
    /**
     * Установка масштаба
     */
    public void setScale(float scale) {
        this.scaleX = scale;
        this.scaleY = scale;
        this.scaleZ = scale;
        matrixDirty = true;
    }
    
    /**
     * Проверка пересечения с лучом (для Ray Casting)
     */
    public boolean rayIntersect(float[] origin, float[] direction, float[] outPoint) {
        // Упрощённая проверка AABB
        float halfSize = 0.5f;
        float minX = x - halfSize;
        float maxX = x + halfSize;
        float minY = y - halfSize;
        float maxY = y + halfSize;
        float minZ = z - halfSize;
        float maxZ = z + halfSize;
        
        float tMin = Float.NEGATIVE_INFINITY;
        float tMax = Float.POSITIVE_INFINITY;
        
        for (int i = 0; i < 3; i++) {
            float originComp = (i == 0) ? origin[0] : (i == 1) ? origin[1] : origin[2];
            float dirComp = (i == 0) ? direction[0] : (i == 1) ? direction[1] : direction[2];
            float min = (i == 0) ? minX : (i == 1) ? minY : minZ;
            float max = (i == 0) ? maxX : (i == 1) ? maxY : maxZ;
            
            if (Math.abs(dirComp) < 0.0001f) {
                if (originComp < min || originComp > max) return false;
            } else {
                float t1 = (min - originComp) / dirComp;
                float t2 = (max - originComp) / dirComp;
                if (t1 > t2) { float temp = t1; t1 = t2; t2 = temp; }
                if (t1 > tMin) tMin = t1;
                if (t2 < tMax) tMax = t2;
                if (tMin > tMax) return false;
            }
        }
        
        if (outPoint != null && tMin > 0) {
            outPoint[0] = origin[0] + direction[0] * tMin;
            outPoint[1] = origin[1] + direction[1] * tMin;
            outPoint[2] = origin[2] + direction[2] * tMin;
        }
        
        return tMin > 0;
    }
                  }
