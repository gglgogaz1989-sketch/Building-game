package com.build.building.render;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.MotionEvent;

import com.build.building.loadermodels.ModelCache;
import com.build.building.loadermodels.LoadedModel;
import com.build.building.worlds.WorldManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.util.ArrayList;
import java.util.List;

public class GameRenderer implements GLSurfaceView.Renderer {
    
    private Context context;
    private Camera camera;
    private WorldManager worldManager;
    private ModelCache modelCache;
    
    // Матрицы
    private float[] projectionMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] viewProjectionMatrix = new float[16];
    private float aspectRatio;
    
    // Шейдеры
    private int shaderProgram;
    private int positionHandle;
    private int texCoordHandle;
    private int mvpHandle;
    
    // Модели
    private LoadedModel mapModel;
    private LoadedModel brickModel;
    private List<GameObject> gameObjects = new ArrayList<>();
    
    // Режимы
    private boolean buildMode = false;
    private boolean deleteMode = false;
    private boolean isDragging = false;
    
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
        this.worldManager = WorldManager.getInstance(context);
        this.modelCache = ModelCache.getInstance(context);
        
        // Настройки камеры
        camera.setPosition(0, 1.6f, 5);
        camera.setSensitivity(0.12f);
        camera.setSmoothness(0.88f);
        camera.setMoveSpeed(5.5f);
    }
    
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES30.glClearColor(0.2f, 0.3f, 0.4f, 1.0f);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glEnable(GLES30.GL_CULL_FACE);
        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
        
        initShaders();
        loadModels();
        loadWorldObjects();
    }
    
    private void initShaders() {
        String vertexShaderCode =
            "#version 300 es\n" +
            "uniform mat4 u_MVP;\n" +
            "layout(location = 0) in vec3 a_Position;\n" +
            "layout(location = 1) in vec2 a_TexCoord;\n" +
            "out vec2 v_TexCoord;\n" +
            "void main() {\n" +
            "    gl_Position = u_MVP * vec4(a_Position, 1.0);\n" +
            "    v_TexCoord = a_TexCoord;\n" +
            "}\n";
        
        String fragmentShaderCode =
            "#version 300 es\n" +
            "precision mediump float;\n" +
            "uniform sampler2D u_Texture;\n" +
            "in vec2 v_TexCoord;\n" +
            "out vec4 outColor;\n" +
            "void main() {\n" +
            "    outColor = texture(u_Texture, v_TexCoord);\n" +
            "}\n";
        
        int vertexShader = compileShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode);
        
        shaderProgram = GLES30.glCreateProgram();
        GLES30.glAttachShader(shaderProgram, vertexShader);
        GLES30.glAttachShader(shaderProgram, fragmentShader);
        GLES30.glLinkProgram(shaderProgram);
        
        GLES30.glDeleteShader(vertexShader);
        GLES30.glDeleteShader(fragmentShader);
        
        positionHandle = GLES30.glGetAttribLocation(shaderProgram, "a_Position");
        texCoordHandle = GLES30.glGetAttribLocation(shaderProgram, "a_TexCoord");
        mvpHandle = GLES30.glGetUniformLocation(shaderProgram, "u_MVP");
    }
    
    private int compileShader(int type, String source) {
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, source);
        GLES30.glCompileShader(shader);
        
        int[] compiled = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            String error = GLES30.glGetShaderInfoLog(shader);
            android.util.Log.e("GameRenderer", "Shader compile error: " + error);
            GLES30.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }
    
    private void loadModels() {
        mapModel = modelCache.getModel("models/map.obj", "map");
        brickModel = modelCache.getModel("models/brick.obj", "brick");
        
        if (mapModel != null) {
            GameObject mapObject = new GameObject(mapModel, "map", 0, -0.5f, 0);
            mapObject.setScale(50f);
            gameObjects.add(mapObject);
        }
    }
    
    private void loadWorldObjects() {
        if (worldManager.getCurrentWorld() != null) {
            for (var objData : worldManager.getCurrentWorld().objects) {
                if ("brick".equals(objData.type) && brickModel != null) {
                    GameObject brick = new GameObject(brickModel, "brick", objData.x, objData.y, objData.z);
                    brick.material = objData.material != null ? objData.material : "brick";
                    brick.rotY = objData.rotY;
                    gameObjects.add(brick);
                }
            }
        }
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
        
        GLES30.glUseProgram(shaderProgram);
        
        for (GameObject obj : gameObjects) {
            obj.draw(positionHandle, texCoordHandle, -1, mvpHandle, viewProjectionMatrix);
        }
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
        // Взаимодействие с объектом под прицелом
    }
    
    public void onStartDrag() {
        isDragging = true;
    }
    
    public void onEndDrag() {
        isDragging = false;
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
    
    public void addBrick(float x, float y, float z) {
        if (brickModel == null) return;
        
        GameObject brick = new GameObject(brickModel, "brick", x, y, z);
        brick.material = "brick";
        gameObjects.add(brick);
        
        if (worldManager.getCurrentWorld() != null) {
            worldManager.addObject("brick", x, y, z);
        }
        
        if (coinListener != null) {
            coinListener.onCoinEarned(1);
        }
    }
    
    public void removeBrick(float x, float y, float z) {
        for (int i = 0; i < gameObjects.size(); i++) {
            GameObject obj = gameObjects.get(i);
            if ("brick".equals(obj.type) && 
                Math.abs(obj.x - x) < 0.1f && 
                Math.abs(obj.y - y) < 0.1f && 
                Math.abs(obj.z - z) < 0.1f) {
                gameObjects.remove(i);
                if (worldManager.getCurrentWorld() != null) {
                    worldManager.removeObject(x, y, z);
                }
                break;
            }
        }
    }
    
    public void setBuildModeListener(BuildModeListener listener) {
        this.buildModeListener = listener;
    }
    
    public void setCoinListener(CoinListener listener) {
        this.coinListener = listener;
    }
    
    public void cleanup() {
        modelCache.clear();
    }
}
