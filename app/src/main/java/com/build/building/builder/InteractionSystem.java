package com.build.building.builder;

import com.build.building.render.GameObject;
import java.util.ArrayList;
import java.util.List;

public class InteractionSystem {
    
    private GameObject selectedObject = null;
    private boolean isDragging = false;
    private float dragStartX, dragStartY, dragStartZ;
    private float dragOffsetX, dragOffsetY, dragOffsetZ;
    private List<GameObject> moveableObjects = new ArrayList<>();
    
    public interface InteractionListener {
        void onObjectSelected(GameObject obj);
        void onObjectDeselected();
        void onObjectMoved(GameObject obj, float newX, float newY, float newZ);
        void onObjectUsed(GameObject obj);
    }
    
    private InteractionListener listener;
    
    public InteractionSystem() {}
    
    /**
     * Выбор объекта под прицелом
     */
    public void selectObject(GameObject object) {
        if (selectedObject != null && listener != null) {
            listener.onObjectDeselected();
        }
        
        selectedObject = object;
        
        if (selectedObject != null && listener != null) {
            listener.onObjectSelected(selectedObject);
        }
    }
    
    /**
     * Снятие выбора
     */
    public void deselectObject() {
        selectedObject = null;
        if (listener != null) {
            listener.onObjectDeselected();
        }
    }
    
    /**
     * Начать перемещение выбранного объекта
     */
    public void startDrag(float hitX, float hitY, float hitZ) {
        if (selectedObject == null) return;
        
        isDragging = true;
        dragStartX = selectedObject.x;
        dragStartY = selectedObject.y;
        dragStartZ = selectedObject.z;
        
        dragOffsetX = selectedObject.x - hitX;
        dragOffsetY = selectedObject.y - hitY;
        dragOffsetZ = selectedObject.z - hitZ;
    }
    
    /**
     * Перемещение объекта (во время драга)
     */
    public void dragTo(float targetX, float targetY, float targetZ) {
        if (!isDragging || selectedObject == null) return;
        
        float newX = targetX + dragOffsetX;
        float newY = targetY + dragOffsetY;
        float newZ = targetZ + dragOffsetZ;
        
        selectedObject.setPosition(newX, newY, newZ);
        
        if (listener != null) {
            listener.onObjectMoved(selectedObject, newX, newY, newZ);
        }
    }
    
    /**
     * Завершить перемещение
     */
    public void endDrag() {
        isDragging = false;
    }
    
    /**
     * Использовать объект (интерактив)
     */
    public void useObject(GameObject object) {
        if (object == null) return;
        
        if (listener != null) {
            listener.onObjectUsed(object);
        }
        
        // Логика для разных типов объектов
        if ("door".equals(object.type)) {
            // Открыть/закрыть дверь
            object.rotY = (object.rotY == 0) ? 90f : 0f;
            object.updateMatrix();
        }
    }
    
    /**
     * Добавление перемещаемого объекта
     */
    public void addMoveableObject(GameObject obj) {
        moveableObjects.add(obj);
    }
    
    /**
     * Удаление перемещаемого объекта
     */
    public void removeMoveableObject(GameObject obj) {
        moveableObjects.remove(obj);
    }
    
    public GameObject getSelectedObject() {
        return selectedObject;
    }
    
    public boolean isDragging() {
        return isDragging;
    }
    
    public void setListener(InteractionListener listener) {
        this.listener = listener;
    }
}
