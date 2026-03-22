package com.build.building.loadermodels;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class VertexUtils {
    
    /**
     * Создание FloatBuffer из массива
     */
    public static FloatBuffer createFloatBuffer(float[] data) {
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = bb.asFloatBuffer();
        buffer.put(data);
        buffer.position(0);
        return buffer;
    }
    
    /**
     * Создание ShortBuffer из массива
     */
    public static ShortBuffer createShortBuffer(short[] data) {
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 2);
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer buffer = bb.asShortBuffer();
        buffer.put(data);
        buffer.position(0);
        return buffer;
    }
    
    /**
     * Создание FloatBuffer для одного значения
     */
    public static FloatBuffer createFloatBuffer(float value) {
        return createFloatBuffer(new float[]{value});
    }
    
    /**
     * Генерация нормалей для модели (если их нет)
     */
    public static float[] generateNormals(float[] vertices, short[] indices) {
        if (vertices == null || vertices.length < 3) return null;
        
        int vertexCount = vertices.length / 3;
        float[] normals = new float[vertices.length];
        
        for (int i = 0; i < indices.length; i += 3) {
            int i1 = indices[i] * 3;
            int i2 = indices[i + 1] * 3;
            int i3 = indices[i + 2] * 3;
            
            float[] v1 = {vertices[i1], vertices[i1 + 1], vertices[i1 + 2]};
            float[] v2 = {vertices[i2], vertices[i2 + 1], vertices[i2 + 2]};
            float[] v3 = {vertices[i3], vertices[i3 + 1], vertices[i3 + 2]};
            
            float[] normal = calculateNormal(v1, v2, v3);
            
            // Добавляем нормаль к каждой вершине
            normals[i1] += normal[0];
            normals[i1 + 1] += normal[1];
            normals[i1 + 2] += normal[2];
            
            normals[i2] += normal[0];
            normals[i2 + 1] += normal[1];
            normals[i2 + 2] += normal[2];
            
            normals[i3] += normal[0];
            normals[i3 + 1] += normal[1];
            normals[i3 + 2] += normal[2];
        }
        
        // Нормализация
        for (int i = 0; i < vertexCount; i++) {
            int idx = i * 3;
            float len = (float) Math.sqrt(
                normals[idx] * normals[idx] +
                normals[idx + 1] * normals[idx + 1] +
                normals[idx + 2] * normals[idx + 2]
            );
            if (len > 0) {
                normals[idx] /= len;
                normals[idx + 1] /= len;
                normals[idx + 2] /= len;
            } else {
                normals[idx] = 0;
                normals[idx + 1] = 1;
                normals[idx + 2] = 0;
            }
        }
        
        return normals;
    }
    
    /**
     * Вычисление нормали по трём вершинам
     */
    private static float[] calculateNormal(float[] v1, float[] v2, float[] v3) {
        float[] u = {v2[0] - v1[0], v2[1] - v1[1], v2[2] - v1[2]};
        float[] v = {v3[0] - v1[0], v3[1] - v1[1], v3[2] - v1[2]};
        
        float[] normal = {
            u[1] * v[2] - u[2] * v[1],
            u[2] * v[0] - u[0] * v[2],
            u[0] * v[1] - u[1] * v[0]
        };
        
        float len = (float) Math.sqrt(normal[0]*normal[0] + normal[1]*normal[1] + normal[2]*normal[2]);
        if (len > 0) {
            normal[0] /= len;
            normal[1] /= len;
            normal[2] /= len;
        }
        
        return normal;
    }
}
