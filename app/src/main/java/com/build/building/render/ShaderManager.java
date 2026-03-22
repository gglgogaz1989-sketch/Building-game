package com.build.building.render;

import android.opengl.GLES30;
import java.util.HashMap;

public class ShaderManager {
    
    private static ShaderManager instance;
    private HashMap<String, Integer> shaderPrograms = new HashMap<>();
    
    public static ShaderManager getInstance() {
        if (instance == null) {
            instance = new ShaderManager();
        }
        return instance;
    }
    
    private ShaderManager() {}
    
    /**
     * Создание шейдерной программы
     */
    public int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = compileShader(GLES30.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource);
        
        if (vertexShader == 0 || fragmentShader == 0) {
            return 0;
        }
        
        int program = GLES30.glCreateProgram();
        GLES30.glAttachShader(program, vertexShader);
        GLES30.glAttachShader(program, fragmentShader);
        GLES30.glLinkProgram(program);
        
        int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
        
        if (linkStatus[0] == 0) {
            String error = GLES30.glGetProgramInfoLog(program);
            android.util.Log.e("ShaderManager", "Linking error: " + error);
            GLES30.glDeleteProgram(program);
            return 0;
        }
        
        GLES30.glDeleteShader(vertexShader);
        GLES30.glDeleteShader(fragmentShader);
        
        return program;
    }
    
    /**
     * Компиляция шейдера
     */
    private int compileShader(int type, String source) {
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, source);
        GLES30.glCompileShader(shader);
        
        int[] compiled = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
        
        if (compiled[0] == 0) {
            String error = GLES30.glGetShaderInfoLog(shader);
            android.util.Log.e("ShaderManager", "Shader compile error: " + error);
            GLES30.glDeleteShader(shader);
            return 0;
        }
        
        return shader;
    }
    
    /**
     * Получение программы из кэша
     */
    public int getProgram(String name) {
        return shaderPrograms.getOrDefault(name, 0);
    }
    
    /**
     * Сохранение программы в кэш
     */
    public void putProgram(String name, int program) {
        shaderPrograms.put(name, program);
    }
    
    /**
     * Использование программы
     */
    public void useProgram(int program) {
        GLES30.glUseProgram(program);
    }
    
    /**
     * Удаление всех программ
     */
    public void clear() {
        for (int program : shaderPrograms.values()) {
            GLES30.glDeleteProgram(program);
        }
        shaderPrograms.clear();
    }
    
    // Базовый вершинный шейдер (с текстурой)
    public static final String BASIC_VERTEX_SHADER =
        "#version 300 es\n" +
        "uniform mat4 u_MVP;\n" +
        "layout(location = 0) in vec3 a_Position;\n" +
        "layout(location = 1) in vec2 a_TexCoord;\n" +
        "out vec2 v_TexCoord;\n" +
        "void main() {\n" +
        "    gl_Position = u_MVP * vec4(a_Position, 1.0);\n" +
        "    v_TexCoord = a_TexCoord;\n" +
        "}\n";
    
    // Базовый фрагментный шейдер (с текстурой)
    public static final String BASIC_FRAGMENT_SHADER =
        "#version 300 es\n" +
        "precision mediump float;\n" +
        "uniform sampler2D u_Texture;\n" +
        "in vec2 v_TexCoord;\n" +
        "out vec4 outColor;\n" +
        "void main() {\n" +
        "    outColor = texture(u_Texture, v_TexCoord);\n" +
        "}\n";
    
    // Вершинный шейдер без текстур (только цвет)
    public static final String COLOR_VERTEX_SHADER =
        "#version 300 es\n" +
        "uniform mat4 u_MVP;\n" +
        "layout(location = 0) in vec3 a_Position;\n" +
        "void main() {\n" +
        "    gl_Position = u_MVP * vec4(a_Position, 1.0);\n" +
        "}\n";
    
    // Фрагментный шейдер без текстур (цвет)
    public static final String COLOR_FRAGMENT_SHADER =
        "#version 300 es\n" +
        "precision mediump float;\n" +
        "uniform vec4 u_Color;\n" +
        "out vec4 outColor;\n" +
        "void main() {\n" +
        "    outColor = u_Color;\n" +
        "}\n";
}
