package com.build.building.render;

import android.opengl.GLES30;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import com.build.building.loadermodels.VertexUtils;

public class RenderBatch {
    
    private HashMap<String, BatchData> batches = new HashMap<>();
    private int maxVerticesPerBatch = 10000;
    private int maxIndicesPerBatch = 30000;
    
    private static class BatchData {
        ArrayList<Float> vertices = new ArrayList<>();
        ArrayList<Float> texCoords = new ArrayList<>();
        ArrayList<Short> indices = new ArrayList<>();
        int vertexCount = 0;
        int indexCount = 0;
        int vboVertices = 0;
        int vboTexCoords = 0;
        int iboIndices = 0;
        boolean dirty = true;
        String material;
        
        void addVertex(float x, float y, float z, float u, float v, short index) {
            vertices.add(x);
            vertices.add(y);
            vertices.add(z);
            texCoords.add(u);
            texCoords.add(v);
            indices.add((short)(vertexCount + index));
            vertexCount++;
            indexCount++;
            dirty = true;
        }
        
        void upload() {
            if (!dirty) return;
            
            // Создаём или пересоздаём буферы
            if (vboVertices == 0) {
                int[] buffers = new int[3];
                GLES30.glGenBuffers(3, buffers, 0);
                vboVertices = buffers[0];
                vboTexCoords = buffers[1];
                iboIndices = buffers[2];
            }
            
            float[] vertexArray = new float[vertices.size()];
            for (int i = 0; i < vertices.size(); i++) vertexArray[i] = vertices.get(i);
            FloatBuffer vertexBuffer = VertexUtils.createFloatBuffer(vertexArray);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboVertices);
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexArray.length * 4, vertexBuffer, GLES30.GL_STATIC_DRAW);
            
            float[] texArray = new float[texCoords.size()];
            for (int i = 0; i < texCoords.size(); i++) texArray[i] = texCoords.get(i);
            FloatBuffer texBuffer = VertexUtils.createFloatBuffer(texArray);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboTexCoords);
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, texArray.length * 4, texBuffer, GLES30.GL_STATIC_DRAW);
            
            short[] indexArray = new short[indices.size()];
            for (int i = 0; i < indices.size(); i++) indexArray[i] = indices.get(i);
            ShortBuffer indexBuffer = VertexUtils.createShortBuffer(indexArray);
            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, iboIndices);
            GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexArray.length * 2, indexBuffer, GLES30.GL_STATIC_DRAW);
            
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
            
            dirty = false;
        }
        
        void draw(int positionHandle, int texCoordHandle) {
            upload();
            
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboVertices);
            GLES30.glEnableVertexAttribArray(positionHandle);
            GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 12, 0);
            
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboTexCoords);
            GLES30.glEnableVertexAttribArray(texCoordHandle);
            GLES30.glVertexAttribPointer(texCoordHandle, 2, GLES30.GL_FLOAT, false, 8, 0);
            
            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, iboIndices);
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, indexCount, GLES30.GL_UNSIGNED_SHORT, 0);
            
            GLES30.glDisableVertexAttribArray(positionHandle);
            GLES30.glDisableVertexAttribArray(texCoordHandle);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }
    
    public void addToBatch(String material, float[] vertices, float[] texCoords, short[] indices) {
        BatchData batch = batches.get(material);
        if (batch == null) {
            batch = new BatchData();
            batch.material = material;
            batches.put(material, batch);
        }
        
        for (int i = 0; i < indices.length; i++) {
            int vi = indices[i] * 3;
            int ti = indices[i] * 2;
            batch.addVertex(vertices[vi], vertices[vi+1], vertices[vi+2],
                           texCoords[ti], texCoords[ti+1], (short)i);
        }
    }
    
    public void drawBatches(int positionHandle, int texCoordHandle) {
        for (BatchData batch : batches.values()) {
            batch.draw(positionHandle, texCoordHandle);
        }
    }
    
    public void clear() {
        for (BatchData batch : batches.values()) {
            if (batch.vboVertices != 0) {
                int[] buffers = {batch.vboVertices, batch.vboTexCoords, batch.iboIndices};
                GLES30.glDeleteBuffers(3, buffers, 0);
            }
        }
        batches.clear();
    }
             }
