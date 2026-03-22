package com.build.building.builder;

import com.build.building.render.GameObject;
import java.util.ArrayList;
import java.util.List;

public class BlockBreaker {
    
    private float breakDistance = 5f;
    private int breakStrength = 1;  // количество ударов для разрушения
    private int currentHitCount = 0;
    private GameObject currentTarget = null;
    
    public interface BreakListener {
        void onBlockBroken(GameObject block);
        void onBlockHit(GameObject block, int hitCount, int required);
    }
    
    private BreakListener listener;
    
    public BlockBreaker() {}
    
    /**
     * Попытка разрушить блок под прицелом
     */
    public boolean tryBreak(GameObject target) {
        if (target == null) return false;
        
        if (target != currentTarget) {
            currentTarget = target;
            currentHitCount = 0;
        }
        
        currentHitCount++;
        
        if (listener != null) {
            listener.onBlockHit(target, currentHitCount, breakStrength);
        }
        
        if (currentHitCount >= breakStrength) {
            if (listener != null) {
                listener.onBlockBroken(target);
            }
            currentTarget = null;
            currentHitCount = 0;
            return true;
        }
        
        return false;
    }
    
    /**
     * Мгновенное разрушение (для режима удаления)
     */
    public boolean breakInstant(GameObject target) {
        if (target == null) return false;
        
        if (listener != null) {
            listener.onBlockBroken(target);
        }
        return true;
    }
    
    /**
     * Разрушение в радиусе
     */
    public int breakInRadius(GameObject center, float radius, List<GameObject> allBlocks) {
        int brokenCount = 0;
        ArrayList<GameObject> toRemove = new ArrayList<>();
        
        for (GameObject block : allBlocks) {
            float dx = block.x - center.x;
            float dy = block.y - center.y;
            float dz = block.z - center.z;
            float dist = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
            
            if (dist <= radius) {
                toRemove.add(block);
                brokenCount++;
            }
        }
        
        for (GameObject block : toRemove) {
            if (listener != null) {
                listener.onBlockBroken(block);
            }
        }
        
        return brokenCount;
    }
    
    public void setBreakDistance(float distance) {
        this.breakDistance = distance;
    }
    
    public void setBreakStrength(int strength) {
        this.breakStrength = strength;
    }
    
    public void setListener(BreakListener listener) {
        this.listener = listener;
    }
    
    public void resetHitCount() {
        currentTarget = null;
        currentHitCount = 0;
    }
        }
