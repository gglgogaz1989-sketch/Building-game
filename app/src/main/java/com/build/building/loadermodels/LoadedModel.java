package com.build.building.loadermodels;

import android.opengl.GLES30;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class LoadedModel {
    
    // Вершинные данные
    public float[] vertices;
    public float[] texCoords;
    public float[] normals;
    public short[] indices;
    
    // OpenGL буферы
    public int vboVertices;
    public int vboTexCoords;
    public int vboNormals;
    public int iboIndices;
    
    public int vertexCount;
    public int indexCount;
    
    // Информация о модели
    public String modelName;
    public boolean hasTexCoords = false;
    public boolean hasNormals = false;
    public boolean hasIndices = false;
    
    public LoadedModel(String modelName) {
        this.modelName = modelName;
    }
    
    /**
     * Создание VBO и IBO буферов
     */
    public void createBuffers() {
        int[] buffers = new int[4];
        GLES30.glGenBuffers(4, buffers, 0);
        
        // Вершинный буфер
        if (vertices != null && vertices.length > 0) {
            vboVertices = buffers[0];
            FloatBuffer vertexBuffer = VertexUtils.createFloatBuffer(vertices);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboVertices);
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertices.length * 4, vertexBuffer, GLES30.GL_STATIC_DRAW);
        }
        
        // Буфер текстурных координат
        if (texCoords != null && texCoords.length > 0) {
            vboTexCoords = buffers[1];
            FloatBuffer texBuffer = VertexUtils.createFloatBuffer(texCoords);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboTexCoords);
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, texCoords.length * 4, texBuffer, GLES30.GL_STATIC_DRAW);
            hasTexCoords = true;
        }
        
        // Буфер нормалей
        if (normals != null && normals.length > 0) {
            vboNormals = buffers[2];
            FloatBuffer normalBuffer = VertexUtils.createFloatBuffer(normals);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboNormals);
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, normals.length * 4, normalBuffer, GLES30.GL_STATIC_DRAW);
            hasNormals = true;
        }
        
        // Индексный буфер
        if (indices != null && indices.length > 0) {
            iboIndices = buffers[3];
            ShortBuffer indexBuffer = VertexUtils.createShortBuffer(indices);
            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, iboIndices);
            GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indices.length * 2, indexBuffer, GLES30.GL_STATIC_DRAW);
            hasIndices = true;
            indexCount = indices.length;
        }
        
        // Очищаем привязку буферов
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
    /**
     * Отрисовка модели
     */
    public void draw(int positionHandle, int texCoordHandle, int normalHandle) {
        // Вершины
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboVertices);
        GLES30.glEnableVertexAttribArray(positionHandle);
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 12, 0);
        
        // Текстурные координаты
        if (hasTexCoords && texCoordHandle >= 0) {
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboTexCoords);
            GLES30.glEnableVertexAttribArray(texCoordHandle);
            GLES30.glVertexAttribPointer(texCoordHandle, 2, GLES30.GL_FLOAT, false, 8, 0);
        }
        
        // Нормали (для освещения)
        if (hasNormals && normalHandle >= 0) {
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboNormals);
            GLES30.glEnableVertexAttribArray(normalHandle);
            GLES30.glVertexAttribPointer(normalHandle, 3, GLES30.GL_FLOAT, false, 12, 0);
        }
        
        // Отрисовка
        if (hasIndices) {
            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, iboIndices);
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, indexCount, GLES30.GL_UNSIGNED_SHORT, 0);
        } else {
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount);
        }
        
        // Отключаем атрибуты
        GLES30.glDisableVertexAttribArray(positionHandle);
        if (hasTexCoords && texCoordHandle >= 0) {
            GLES30.glDisableVertexAttribArray(texCoordHandle);
        }
        if (hasNormals && normalHandle >= 0) {
            GLES30.glDisableVertexAttribArray(normalHandle);
        }
        
        // Отвязываем буферы
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
    /**
     * Удаление буферов
     */
    public void deleteBuffers() {
        int[] buffers = new int[4];
        int count = 0;
        
        if (vboVertices != 0) buffers[count++] = vboVertices;
        if (vboTexCoords != 0) buffers[count++] = vboTexCoords;
        if (vboNormals != 0) buffers[count++] = vboNormals;
        if (iboIndices != 0) buffers[count++] = iboIndices;
        
        if (count > 0) {
            GLES30.glDeleteBuffers(count, buffers, 0);
        }
    }
          }
