package com.example.a111.game.model.sector;

import android.opengl.GLES20;

import com.example.a111.game.util.ShaderUtil;


/**
 * Created by 111 on 2016/8/30.
 */
public class SectorProgram {

    private final String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_position;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec2 v_texCoord;" +
                    "void main()" +
                    "{" +
                    "    gl_Position = u_MVPMatrix * a_position;" +//"   gl_Position = modelViewProjectionMatrix * position;
                    "    v_texCoord = a_texCoord;" +
                    "}";


    private final String fragmentShaderCode =
            "precision highp float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D u_samplerTexture;" +
                    "void main()" +
                    "{" +
                    "    gl_FragColor = texture2D(u_samplerTexture, v_texCoord);" +
                    "}";


    private static SectorProgram mSectorProgram;

    public int mProgram;//自定义渲染管线着色器程序id
    public int muMVPMatrixHandle;//总变换矩阵引用
    public int maPositionHandle; //顶点位置属性引用
    public int maTexCoorHandle; //顶点纹理坐标属性引用

    private SectorProgram() {
        mProgram = ShaderUtil.createProgram(vertexShaderCode, fragmentShaderCode);
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_position");
        maTexCoorHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
    }

    public static SectorProgram getInstance() {
        if (mSectorProgram == null) {
            mSectorProgram = new SectorProgram();
        }

        return mSectorProgram;
    }
}
