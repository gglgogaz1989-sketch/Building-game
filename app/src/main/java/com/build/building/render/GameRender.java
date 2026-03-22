package com.build.building.render;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GameRenderer implements GLSurfaceView.Renderer {
    
    private Context context;
    private Camera camera;
    private float[] projectionMatrix = new float[16];
    private float[] viewProjectionMatrix = new float[16];
    private float aspectRatio;
    
    // Режимы
    private boolean buildMode = false;
    private boolean deleteMode = false;
    
    // Слушатели
    private BuildModeListener buildModeListener;
    private CoinListener coinListener;
    
    public interface BuildModeListener {
        void onBuildModeChanged(boolean isBuildMode);
    }
    
    public interface CoinListener {
        void onCoinEarned(int amount);
    }
    
    public GameRenderer(Context context) {
        this.context = context;
        this.camera = new Camera();
        camera.setPosition(0, 1.6f, 5);
    }
    
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES30.glClearColor(0.2f, 0.3f, 0.4f, 1.0f);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glEnable(GLES30.GL_CULL_FACE);
    }
    
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        aspectRatio = (float) width / height;
        Matrix.perspectiveM(projectionMatrix, 0, 70f, aspectRatio, 0.1f, 1000f);
    }
    
    @Override
    public void onDrawFrame(GL10 unused) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        
        camera.update();
        float[] viewMatrix = camera.getViewMatrix();
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        
        // Здесь будет отрисовка объектов
    }
    
    public void handleTouch(MotionEvent event) {
        camera.handleTouch(event);
    }
    
    public void movePlayer(float x, float y) {
        camera.setMovement(-y, x);
    }
    
    public void stopPlayer() {
        camera.stopMovement();
    }
    
    public void onInteract() {
        // Взаимодействие с объектами
    }
    
    public void onStartDrag() {
        // Начало перемещения блока
    }
    
    public void onEndDrag() {
        // Конец перемещения
    }
    
    public void setBuildMode(boolean mode) {
        this.buildMode = mode;
        if (buildModeListener != null) {
            buildModeListener.onBuildModeChanged(mode);
        }
    }
    
    public boolean isBuildMode() {
        return buildMode;
    }
    
    public void setDeleteMode(boolean mode) {
        this.deleteMode = mode;
    }
    
    public void setBuildModeListener(BuildModeListener listener) {
        this.buildModeListener = listener;
    }
    
    public void setCoinListener(CoinListener listener) {
        this.coinListener = listener;
    }
    
    public void cleanup() {
        // Очистка ресурсов
    }
}
