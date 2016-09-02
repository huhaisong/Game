package com.example.a111.game.model;

import android.opengl.GLES20;

import com.example.a111.game.util.AABB3;
import com.example.a111.game.util.MemUtil;
import com.example.a111.game.util.ShaderUtil;

import java.nio.FloatBuffer;

/**
 * Created by 111 on 2016/7/1.
 */
public class BaseCircle extends TouchableObject {

    int mProgram;
    int muMVPMatrixHandle;
    int maPositionHandle;
    int maTexCoorHandle;


    String mVertexShader = "uniform mat4 uMVPMatrix;\n" +
            "attribute vec3 aPosition;\n" +
            "attribute vec3 aNormal;\n" +
            "attribute vec2 aTexCoor;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main()     \n" +
            "{\n" +
            "   gl_Position = uMVPMatrix * vec4(aPosition,1);\n" +
            "   vTextureCoord = aTexCoor;\n" +
            "}    ";
    String mFragmentShader = "precision mediump float;\n" +
            "uniform sampler2D sTexture;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main()                         \n" +
            "{\n" +
            "   vec4 finalColor=texture2D(sTexture, vTextureCoord);\n" +
            "   gl_FragColor = vec4(1.0,0.0,0.0,1.0);"+//finalColor;\n" +
            "}        ";

    FloatBuffer mVertexBuffer;
    FloatBuffer mTexCoorBuffer;
    FloatBuffer mNormalBuffer;

    int vCount;

    public BaseCircle(float scale, float r, int n,int id) {
        this.id = id;
        setInitStack();
        initVertexData(scale, r, n);
        initShader();
    }

    //自定义的初始化顶点数据的方法
    public void initVertexData(
            float scale,    //大小
            float r,        //半径
            int n)        //切分的份数
    {
        r = r * scale;
        float angdegSpan = 360.0f / n;    //顶角的度数
        vCount = 3 * n;//顶点个数，共有n个三角形，每个三角形都有三个顶点

        float[] vertices = new float[vCount * 3];//坐标数据
        float[] textures = new float[vCount * 2];//顶点纹理S、T坐标值数组
        //坐标数据初始化
        int count = 0;
        int stCount = 0;
        for (float angdeg = 0; Math.ceil(angdeg) < 360; angdeg += angdegSpan) {
            double angrad = Math.toRadians(angdeg);//当前弧度
            double angradNext = Math.toRadians(angdeg + angdegSpan);//下一弧度
            //中心点
            vertices[count++] = 0;//顶点坐标
            vertices[count++] = 0;
            vertices[count++] = 0;

            textures[stCount++] = 0.5f;//st坐标
            textures[stCount++] = 0.5f;
            //当前点
            vertices[count++] = (float) (-r * Math.sin(angrad));//顶点坐标
            vertices[count++] = (float) (r * Math.cos(angrad));
            vertices[count++] = 0;

            textures[stCount++] = (float) (0.5f - 0.5f * Math.sin(angrad));//st坐标
            textures[stCount++] = (float) (0.5f - 0.5f * Math.cos(angrad));
            //下一点
            vertices[count++] = (float) (-r * Math.sin(angradNext));//顶点坐标
            vertices[count++] = (float) (r * Math.cos(angradNext));
            vertices[count++] = 0;

            textures[stCount++] = (float) (0.5f - 0.5f * Math.sin(angradNext));//st坐标
            textures[stCount++] = (float) (0.5f - 0.5f * Math.cos(angradNext));
        }

        preBox = new AABB3(vertices);

        mVertexBuffer = MemUtil.makeFloatBuffer(vertices);

        mTexCoorBuffer = MemUtil.makeFloatBuffer(textures);
    }

    private void initShader() {

        mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        maTexCoorHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoor");
    }

    public void drawSelf(int texId) {
        //制定使用某套shader程序
        GLES20.glUseProgram(mProgram);

        //将最终变换矩阵传入shader程序
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, getFinalMatrix(), 0);

        //传送顶点位置数据
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer);
        //传送顶点纹理坐标数据
        GLES20.glVertexAttribPointer(maTexCoorHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, mTexCoorBuffer);

        //启用顶点位置数据
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        //启用顶点纹理数据
        GLES20.glEnableVertexAttribArray(maTexCoorHandle);

        //绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);

        //绘制纹理矩形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vCount);
    }
}
