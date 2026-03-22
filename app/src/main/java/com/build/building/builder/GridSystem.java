package com.build.building.builder;

public class GridSystem {
    
    private float gridSize = 0.5f;  // размер ячейки
    private boolean snapToGrid = true;
    
    public GridSystem() {}
    
    public GridSystem(float gridSize) {
        this.gridSize = gridSize;
    }
    
    /**
     * Округление координаты до сетки
     */
    public float snap(float value) {
        if (!snapToGrid) return value;
        return Math.round(value / gridSize) * gridSize;
    }
    
    /**
     * Округление позиции до сетки
     */
    public float[] snapPosition(float x, float y, float z) {
        return new float[]{
            snap(x),
            snap(y),
            snap(z)
        };
    }
    
    /**
     * Получение индекса ячейки
     */
    public int getCellIndex(float x, float z) {
        int gridX = (int) Math.floor(x / gridSize);
        int gridZ = (int) Math.floor(z / gridSize);
        return gridX * 10000 + gridZ;  // простой хэш
    }
    
    /**
     * Проверка, находится ли позиция в сетке
     */
    public boolean isAligned(float value) {
        float snapped = snap(value);
        return Math.abs(value - snapped) < 0.01f;
    }
    
    /**
     * Получение размера сетки
     */
    public float getGridSize() {
        return gridSize;
    }
    
    /**
     * Установка размера сетки
     */
    public void setGridSize(float size) {
        this.gridSize = size;
    }
    
    /**
     * Включение/выключение привязки
     */
    public void setSnapToGrid(boolean snap) {
        this.snapToGrid = snap;
    }
    
    public boolean isSnapToGrid() {
        return snapToGrid;
    }
}
