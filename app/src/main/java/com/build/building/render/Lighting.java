package com.build.building.render;

import android.opengl.GLES30;

public class Lighting {
    
    // Позиция источника света
    public float lightX = 5f;
    public float lightY = 10f;
    public float lightZ = 5f;
    
    // Цвета
    public float[] ambientLight = {0.3f, 0.3f, 0.3f, 1.0f};
    public float[] diffuseLight = {0.8f, 0.8f, 0.8f, 1.0f};
    public float[] specularLight = {0.5f, 0.5f, 0.5f, 1.0f};
    
    // Материал
    public float[] materialAmbient = {0.5f, 0.5f, 0.5f, 1.0f};
    public float[] materialDiffuse = {0.8f, 0.8f, 0.8f, 1.0f};
    public float[] materialSpecular = {0.5f, 0.5f, 0.5f, 1.0f};
    public float materialShininess = 32.0f;
    
    // Включено ли освещение
    public boolean enabled = true;
    
    // Направление света (для directional light)
    public float[] lightDirection = {-1f, -1f, -1f};
    public boolean isDirectional = true;
    
    /**
     * Установка uniform'ов в шейдере
     */
    public void setUniforms(int program) {
        if (!enabled) return;
        
        int ambientLoc = GLES30.glGetUniformLocation(program, "u_AmbientLight");
        int diffuseLoc = GLES30.glGetUniformLocation(program, "u_DiffuseLight");
        int specularLoc = GLES30.glGetUniformLocation(program, "u_SpecularLight");
        int lightPosLoc = GLES30.glGetUniformLocation(program, "u_LightPosition");
        int lightDirLoc = GLES30.glGetUniformLocation(program, "u_LightDirection");
        
        int matAmbientLoc = GLES30.glGetUniformLocation(program, "u_MaterialAmbient");
        int matDiffuseLoc = GLES30.glGetUniformLocation(program, "u_MaterialDiffuse");
        int matSpecularLoc = GLES30.glGetUniformLocation(program, "u_MaterialSpecular");
        int matShininessLoc = GLES30.glGetUniformLocation(program, "u_MaterialShininess");
        
        if (ambientLoc >= 0) GLES30.glUniform4fv(ambientLoc, 1, ambientLight, 0);
        if (diffuseLoc >= 0) GLES30.glUniform4fv(diffuseLoc, 1, diffuseLight, 0);
        if (specularLoc >= 0) GLES30.glUniform4fv(specularLoc, 1, specularLight, 0);
        
        if (isDirectional) {
            if (lightDirLoc >= 0) GLES30.glUniform3fv(lightDirLoc, 1, lightDirection, 0);
        } else {
            float[] lightPos = {lightX, lightY, lightZ};
            if (lightPosLoc >= 0) GLES30.glUniform3fv(lightPosLoc, 1, lightPos, 0);
        }
        
        if (matAmbientLoc >= 0) GLES30.glUniform4fv(matAmbientLoc, 1, materialAmbient, 0);
        if (matDiffuseLoc >= 0) GLES30.glUniform4fv(matDiffuseLoc, 1, materialDiffuse, 0);
        if (matSpecularLoc >= 0) GLES30.glUniform4fv(matSpecularLoc, 1, materialSpecular, 0);
        if (matShininessLoc >= 0) GLES30.glUniform1f(matShininessLoc, materialShininess);
    }
    
    /**
     * Шейдер с освещением (вершинный)
     */
    public static final String LIT_VERTEX_SHADER =
        "#version 300 es\n" +
        "uniform mat4 u_MVP;\n" +
        "uniform mat4 u_ModelMatrix;\n" +
        "uniform mat3 u_NormalMatrix;\n" +
        "layout(location = 0) in vec3 a_Position;\n" +
        "layout(location = 1) in vec2 a_TexCoord;\n" +
        "layout(location = 2) in vec3 a_Normal;\n" +
        "out vec2 v_TexCoord;\n" +
        "out vec3 v_Normal;\n" +
        "out vec3 v_FragPos;\n" +
        "void main() {\n" +
        "    gl_Position = u_MVP * vec4(a_Position, 1.0);\n" +
        "    v_TexCoord = a_TexCoord;\n" +
        "    v_Normal = u_NormalMatrix * a_Normal;\n" +
        "    v_FragPos = vec3(u_ModelMatrix * vec4(a_Position, 1.0));\n" +
        "}\n";
    
    /**
     * Шейдер с освещением (фрагментный)
     */
    public static final String LIT_FRAGMENT_SHADER =
        "#version 300 es\n" +
        "precision mediump float;\n" +
        "uniform sampler2D u_Texture;\n" +
        "uniform vec4 u_AmbientLight;\n" +
        "uniform vec4 u_DiffuseLight;\n" +
        "uniform vec4 u_SpecularLight;\n" +
        "uniform vec3 u_LightDirection;\n" +
        "uniform vec4 u_MaterialAmbient;\n" +
        "uniform vec4 u_MaterialDiffuse;\n" +
        "uniform vec4 u_MaterialSpecular;\n" +
        "uniform float u_MaterialShininess;\n" +
        "in vec2 v_TexCoord;\n" +
        "in vec3 v_Normal;\n" +
        "in vec3 v_FragPos;\n" +
        "out vec4 outColor;\n" +
        "void main() {\n" +
        "    vec4 texColor = texture(u_Texture, v_TexCoord);\n" +
        "    vec3 normal = normalize(v_Normal);\n" +
        "    vec3 lightDir = normalize(-u_LightDirection);\n" +
        "    float diff = max(dot(normal, lightDir), 0.0);\n" +
        "    vec4 ambient = u_AmbientLight * u_MaterialAmbient;\n" +
        "    vec4 diffuse = u_DiffuseLight * u_MaterialDiffuse * diff;\n" +
        "    outColor = (ambient + diffuse) * texColor;\n" +
        "}\n";
}
