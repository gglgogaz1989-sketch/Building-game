package com.build.building.render;

import android.opengl.Matrix;
import android.view.MotionEvent;

public class Camera {
    
    // Позиция камеры
    private float eyeX = 0f;
    private float eyeY = 1.6f;   // высота глаз игрока
    private float eyeZ = 5f;
    
    // Точка взгляда
    private float lookX = 0f;
    private float lookY = 0f;
    private float lookZ = 0f;
    
    // Углы поворота
    private float yaw = -90f;      // поворот по горизонтали (градусы)
    private float pitch = 0f;      // поворот по вертикали (градусы)
    
    // Плавное движение
    private float targetYaw = -90f;
    private float targetPitch = 0f;
    private float currentYaw = -90f;
    private float currentPitch = 0f;
    
    // Настройки чувствительности
    private float sensitivity = 0.15f;
    private float smoothFactor = 0.85f;  // плавность (0-1, чем выше тем плавнее)
    
    // Векторы для матриц
    private float[] viewMatrix = new float[16];
    private float[] tempMatrix = new float[16];
    
    // Векторы направления
    private float[] forward = new float[3];
    private float[] right = new float[3];
    private float[] up = new float[3];
    
    // Предыдущие значения для сглаживания касаний
    private float lastDeltaX = 0f;
    private float lastDeltaY = 0f;
    private float filteredDeltaX = 0f;
    private float filteredDeltaY = 0f;
    
    // Время для плавности
    private long lastTime = System.currentTimeMillis();
    
    // Движение
    private float moveSpeed = 5f;
    private float moveForward = 0f;
    private float moveStrafe = 0f;
    
    public Camera() {
        updateViewMatrix();
        updateVectors();
    }
    
    /**
     * Обновление камеры (вызывается каждый кадр)
     */
    public void update() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastTime) / 1000f;
        lastTime = currentTime;
        
        if (deltaTime > 0.033f) deltaTime = 0.033f; // ограничиваем максимальный шаг
        
        // Плавное движение углов
        currentYaw = currentYaw * smoothFactor + targetYaw * (1 - smoothFactor);
        currentPitch = currentPitch * smoothFactor + targetPitch * (1 - smoothFactor);
        
        // Обновляем векторы и матрицы
        updateVectors();
        updateViewMatrix();
        
        // Движение камеры
        if (moveForward != 0 || moveStrafe != 0) {
            float speed = moveSpeed * deltaTime;
            
            // Движение вперед/назад
            eyeX += forward[0] * moveForward * speed;
            eyeZ += forward[2] * moveForward * speed;
            
            // Движение вправо/влево (стрейф)
            eyeX += right[0] * moveStrafe * speed;
            eyeZ += right[2] * moveStrafe * speed;
            
            // Обновляем точку взгляда
            lookX = eyeX + forward[0];
            lookY = eyeY + forward[1];
            lookZ = eyeZ + forward[2];
        }
    }
    
    /**
     * Обновление векторов направления
     */
    private void updateVectors() {
        float yawRad = (float) Math.toRadians(currentYaw);
        float pitchRad = (float) Math.toRadians(currentPitch);
        
        // Вектор вперед
        forward[0] = (float) (Math.cos(yawRad) * Math.cos(pitchRad));
        forward[1] = (float) Math.sin(pitchRad);
        forward[2] = (float) (Math.sin(yawRad) * Math.cos(pitchRad));
        
        // Нормализация
        float len = (float) Math.sqrt(forward[0]*forward[0] + forward[1]*forward[1] + forward[2]*forward[2]);
        forward[0] /= len;
        forward[1] /= len;
        forward[2] /= len;
        
        // Вектор вправо
        right[0] = (float) Math.cos(Math.toRadians(currentYaw + 90));
        right[1] = 0;
        right[2] = (float) Math.sin(Math.toRadians(currentYaw + 90));
        
        // Вектор вверх (перпендикуляр)
        up[0] = 0;
        up[1] = 1;
        up[2] = 0;
    }
    
    /**
     * Обновление матрицы вида
     */
    private void updateViewMatrix() {
        lookX = eyeX + forward[0];
        lookY = eyeY + forward[1];
        lookZ = eyeZ + forward[2];
        
        Matrix.setLookAtM(viewMatrix, 0,
            eyeX, eyeY, eyeZ,
            lookX, lookY, lookZ,
            0f, 1f, 0f);
    }
    
    /**
     * Обработка касаний для поворота камеры
     */
    public void handleTouch(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Сбрасываем фильтры при начале касания
                lastDeltaX = 0;
                lastDeltaY = 0;
                filteredDeltaX = 0;
                filteredDeltaY = 0;
                break;
                
            case MotionEvent.ACTION_MOVE:
                if (event.getHistorySize() > 0) {
                    // Используем историю для более плавного трекинга
                    float dx = 0;
                    float dy = 0;
                    
                    for (int i = 0; i < event.getHistorySize(); i++) {
                        dx += event.getHistoricalX(i + 1) - event.getHistoricalX(i);
                        dy += event.getHistoricalY(i + 1) - event.getHistoricalY(i);
                    }
                    
                    dx = (dx / event.getHistorySize()) * sensitivity;
                    dy = (dy / event.getHistorySize()) * sensitivity;
                    
                    // Фильтр низких частот для сглаживания
                    filteredDeltaX = filteredDeltaX * 0.7f + dx * 0.3f;
                    filteredDeltaY = filteredDeltaY * 0.7f + dy * 0.3f;
                    
                    targetYaw += filteredDeltaX;
                    targetPitch -= filteredDeltaY;
                    
                    // Ограничение вертикального угла (чтобы не переворачиваться)
                    if (targetPitch > 89f) targetPitch = 89f;
                    if (targetPitch < -89f) targetPitch = -89f;
                }
                break;
        }
    }
    
    /**
     * Установка движения камеры (от джойстика)
     */
    public void setMovement(float forwardAmount, float strafeAmount) {
        this.moveForward = forwardAmount;
        this.moveStrafe = strafeAmount;
    }
    
    /**
     * Остановка движения
     */
    public void stopMovement() {
        this.moveForward = 0f;
        this.moveStrafe = 0f;
    }
    
    /**
     * Установка чувствительности
     */
    public void setSensitivity(float sens) {
        this.sensitivity = sens;
    }
    
    /**
     * Установка плавности
     */
    public void setSmoothness(float smooth) {
        this.smoothFactor = smooth;
    }
    
    /**
     * Установка скорости движения
     */
    public void setMoveSpeed(float speed) {
        this.moveSpeed = speed;
    }
    
    /**
     * Получение матрицы вида
     */
    public float[] getViewMatrix() {
        return viewMatrix;
    }
    
    /**
     * Получение позиции камеры
     */
    public float getEyeX() { return eyeX; }
    public float getEyeY() { return eyeY; }
    public float getEyeZ() { return eyeZ; }
    
    /**
     * Установка позиции камеры (для спавна)
     */
    public void setPosition(float x, float y, float z) {
        this.eyeX = x;
        this.eyeY = y;
        this.eyeZ = z;
        updateViewMatrix();
    }
    
    /**
     * Установка углов (для загрузки сохранения)
     */
    public void setAngles(float yaw, float pitch) {
        this.targetYaw = yaw;
        this.targetPitch = pitch;
        this.currentYaw = yaw;
        this.currentPitch = pitch;
        updateVectors();
        updateViewMatrix();
    }
    
    /**
     * Получение направления взгляда (для ray casting)
     */
    public float[] getLookDirection() {
        return forward.clone();
    }
    
    /**
     * Получение позиции для ray casting
     */
    public float[] getEyePosition() {
        return new float[]{eyeX, eyeY, eyeZ};
    }
    
    /**
     * Сброс камеры в начальное положение
     */
    public void reset() {
        eyeX = 0f;
        eyeY = 1.6f;
        eyeZ = 5f;
        targetYaw = -90f;
        targetPitch = 0f;
        currentYaw = -90f;
        currentPitch = 0f;
        moveForward = 0f;
        moveStrafe = 0f;
        updateVectors();
        updateViewMatrix();
    }
    
    /**
     * Обновление пропорций экрана (для FOV)
     */
    public float[] getProjectionMatrix(float aspectRatio) {
        float[] projectionMatrix = new float[16];
        Matrix.perspectiveM(projectionMatrix, 0, 70f, aspectRatio, 0.1f, 1000f);
        return projectionMatrix;
    }
    
    /**
     * Получение комбинированной MVP матрицы для объекта
     */
    public float[] getMVPMatrix(float[] modelMatrix, float aspectRatio) {
        float[] projectionMatrix = getProjectionMatrix(aspectRatio);
        float[] viewProjectionMatrix = new float[16];
        float[] mvpMatrix = new float[16];
        
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
        
        return mvpMatrix;
    }
          }
