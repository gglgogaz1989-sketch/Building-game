package com.build.building.builder;

import com.build.building.render.GameObject;
import java.util.ArrayList;
import java.util.List;

public class RayCaster {
    
    private float maxDistance = 10f;
    private float stepSize = 0.05f;
    
    public RayCaster() {}
    
    public RayCaster(float maxDistance, float stepSize) {
        this.maxDistance = maxDistance;
        this.stepSize = stepSize;
    }
    
    /**
     * Результат рейкаста
     */
    public static class HitResult {
        public GameObject hitObject;
        public float hitX, hitY, hitZ;
        public float distance;
        public int face; // 0=X+, 1=X-, 2=Y+, 3=Y-, 4=Z+, 5=Z-
        
        public boolean isHit() {
            return hitObject != null;
        }
    }
    
    /**
     * Поиск объекта под прицелом
     */
    public HitResult raycast(float originX, float originY, float originZ,
                             float dirX, float dirY, float dirZ,
                             List<GameObject> objects) {
        
        HitResult result = new HitResult();
        float closestDistance = maxDistance;
        
        for (GameObject obj : objects) {
            float[] hitPoint = new float[3];
            if (obj.rayIntersect(new float[]{originX, originY, originZ},
                                  new float[]{dirX, dirY, dirZ}, hitPoint)) {
                float dx = hitPoint[0] - originX;
                float dy = hitPoint[1] - originY;
                float dz = hitPoint[2] - originZ;
                float dist = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
                
                if (dist < closestDistance) {
                    closestDistance = dist;
                    result.hitObject = obj;
                    result.hitX = hitPoint[0];
                    result.hitY = hitPoint[1];
                    result.hitZ = hitPoint[2];
                    result.distance = dist;
                    result.face = getHitFace(obj, hitPoint);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Пошаговый рейкаст (для определения места установки блока)
     */
    public HitResult raycastStep(float originX, float originY, float originZ,
                                  float dirX, float dirY, float dirZ,
                                  List<GameObject> objects) {
        
        HitResult result = new HitResult();
        
        float x = originX;
        float y = originY;
        float z = originZ;
        
        for (float dist = 0; dist < maxDistance; dist += stepSize) {
            x += dirX * stepSize;
            y += dirY * stepSize;
            z += dirZ * stepSize;
            
            for (GameObject obj : objects) {
                if (obj.rayIntersect(new float[]{x - stepSize, y - stepSize, z - stepSize},
                                     new float[]{dirX, dirY, dirZ}, null)) {
                    result.hitObject = obj;
                    result.hitX = x;
                    result.hitY = y;
                    result.hitZ = z;
                    result.distance = dist;
                    result.face = getHitFace(obj, new float[]{x, y, z});
                    return result;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Определение стороны, по которой попали в блок
     */
    private int getHitFace(GameObject obj, float[] hitPoint) {
        float dx = hitPoint[0] - obj.x;
        float dy = hitPoint[1] - obj.y;
        float dz = hitPoint[2] - obj.z;
        
        float absDx = Math.abs(dx);
        float absDy = Math.abs(dy);
        float absDz = Math.abs(dz);
        
        if (absDx > absDy && absDx > absDz) {
            return dx > 0 ? 0 : 1;  // X+ или X-
        } else if (absDy > absDx && absDy > absDz) {
            return dy > 0 ? 2 : 3;  // Y+ или Y-
        } else {
            return dz > 0 ? 4 : 5;  // Z+ или Z-
        }
    }
    
    /**
     * Получение позиции для установки нового блока (рядом с попавшим)
     */
    public float[] getPlacePosition(HitResult hit, float blockSize) {
        if (!hit.isHit()) return null;
        
        float x = hit.hitObject.x;
        float y = hit.hitObject.y;
        float z = hit.hitObject.z;
        
        switch (hit.face) {
            case 0: x += blockSize; break;  // X+
            case 1: x -= blockSize; break;  // X-
            case 2: y += blockSize; break;  // Y+
            case 3: y -= blockSize; break;  // Y-
            case 4: z += blockSize; break;  // Z+
            case 5: z -= blockSize; break;  // Z-
        }
        
        return new float[]{x, y, z};
    }
    
    public void setMaxDistance(float distance) {
        this.maxDistance = distance;
    }
    
    public void setStepSize(float step) {
        this.stepSize = step;
    }
          }
