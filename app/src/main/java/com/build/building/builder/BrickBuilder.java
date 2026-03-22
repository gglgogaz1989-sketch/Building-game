package com.build.building.builder;

import com.build.building.render.GameObject;
import com.build.building.loadermodels.LoadedModel;
import java.util.ArrayList;
import java.util.List;

public class BrickBuilder {
    
    private LoadedModel brickModel;
    private List<GameObject> bricks = new ArrayList<>();
    private GridSystem gridSystem;
    private int maxBricks = 10000;  // лимит блоков в мире
    
    public interface BuildListener {
        void onBlockPlaced(GameObject block);
        void onBlockRemoved(GameObject block);
        void onMaxBlocksReached();
    }
    
    private BuildListener listener;
    
    public BrickBuilder(LoadedModel brickModel) {
        this.brickModel = brickModel;
        this.gridSystem = new GridSystem(0.5f);
    }
    
    public BrickBuilder(LoadedModel brickModel, float gridSize) {
        this.brickModel = brickModel;
        this.gridSystem = new GridSystem(gridSize);
    }
    
    /**
     * Установка кирпича
     */
    public boolean placeBrick(float x, float y, float z) {
        return placeBrick(x, y, z, "default");
    }
    
    /**
     * Установка кирпича с материалом
     */
    public boolean placeBrick(float x, float y, float z, String material) {
        if (bricks.size() >= maxBricks) {
            if (listener != null) {
                listener.onMaxBlocksReached();
            }
            return false;
        }
        
        // Привязка к сетке
        float gridX = gridSystem.snap(x);
        float gridZ = gridSystem.snap(z);
        float gridY = gridSystem.snap(y);
        
        // Проверка, не занято ли место
        if (isBlockAt(gridX, gridY, gridZ)) {
            return false;
        }
        
        // Создание нового кирпича
        GameObject brick = new GameObject(brickModel, "brick", gridX, gridY, gridZ);
        brick.type = "brick";
        brick.material = material;
        brick.setScale(0.5f);  // размер кирпича
        
        bricks.add(brick);
        
        if (listener != null) {
            listener.onBlockPlaced(brick);
        }
        
        return true;
    }
    
    /**
     * Удаление кирпича
     */
    public boolean removeBrick(float x, float y, float z) {
        float gridX = gridSystem.snap(x);
        float gridY = gridSystem.snap(y);
        float gridZ = gridSystem.snap(z);
        
        GameObject brick = getBlockAt(gridX, gridY, gridZ);
        if (brick != null) {
            bricks.remove(brick);
            if (listener != null) {
                listener.onBlockRemoved(brick);
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Удаление кирпича по объекту
     */
    public boolean removeBrick(GameObject brick) {
        if (bricks.remove(brick)) {
            if (listener != null) {
                listener.onBlockRemoved(brick);
            }
            return true;
        }
        return false;
    }
    
    /**
     * Проверка наличия блока на позиции
     */
    public boolean isBlockAt(float x, float y, float z) {
        return getBlockAt(x, y, z) != null;
    }
    
    /**
     * Получение блока на позиции
     */
    public GameObject getBlockAt(float x, float y, float z) {
        float eps = 0.1f;
        for (GameObject brick : bricks) {
            if (Math.abs(brick.x - x) < eps &&
                Math.abs(brick.y - y) < eps &&
                Math.abs(brick.z - z) < eps) {
                return brick;
            }
        }
        return null;
    }
    
    /**
     * Получение всех кирпичей
     */
    public List<GameObject> getAllBricks() {
        return bricks;
    }
    
    /**
     * Очистка всех кирпичей
     */
    public void clearAllBricks() {
        bricks.clear();
    }
    
    /**
     * Подсчёт кирпичей
     */
    public int getBrickCount() {
        return bricks.size();
    }
    
    /**
     * Установка лимита блоков
     */
    public void setMaxBricks(int max) {
        this.maxBricks = max;
    }
    
    /**
     * Получение системы сетки
     */
    public GridSystem getGridSystem() {
        return gridSystem;
    }
    
    /**
     * Установка слушателя
     */
    public void setListener(BuildListener listener) {
        this.listener = listener;
    }
}
