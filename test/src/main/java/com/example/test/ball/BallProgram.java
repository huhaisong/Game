package com.example.test.ball;

import android.opengl.GLES20;

import com.example.test.util.ShaderUtil;

/**
 * Created by 111 on 2016/8/23.
 */
public class BallProgram {

    private static BallProgram mBallProgram;

    public int mProgram;//自定义渲染管线着色器程序id
    public int muMVPMatrixHandle;//总变换矩阵引用
    public int maPositionHandle; //顶点位置属性引用
    public int maTexCoorHandle; //顶点纹理坐标属性引用

    private String mVertexShader = "uniform mat4 uMVPMatrix; //总变换矩阵\n" +
            "attribute vec3 aPosition;  //顶点位置\n" +
            "attribute vec2 aTexCoor;    //顶点纹理坐标\n" +
            "varying vec2 vTextureCoord;  //用于传递给片元着色器的变量\n" +
            "\n" +
            "void main(){\n" +
            "   gl_Position = uMVPMatrix * vec4(aPosition,1); //根据总变换矩阵计算此次绘制此顶点位置\n" +
            "   vTextureCoord = aTexCoor;//将接收的纹理坐标传递给片元着色器\n" +
            "}                      ";
    private String mFragmentShader = "precision mediump float;\n" +
            "uniform sampler2D sTexture;//纹理内容数据\n" +
            "varying vec2 vTextureCoord; //接收从顶点着色器过来的参数\n" +
            "varying vec4 vambient;\n" +
            "varying vec4 vdiffuse;\n" +
            "varying vec4 vspecular;\n" +
            "void main()\n" +
            "{\n" +
            "   //将计算出的颜色给此片元\n" +
            "   gl_FragColor=texture2D(sTexture, vTextureCoord);\n" +
            "   //给此片元颜色值\n" +
            "}              ";

    private BallProgram() {
        mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
        //获取程序中顶点位置属性引用id
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        //获取程序中顶点纹理坐标属性引用id
        maTexCoorHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoor");
        //获取程序中总变换矩阵引用id
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
    }

    public static BallProgram getInstance() {

        if (mBallProgram == null) {
            mBallProgram = new BallProgram();
        }
        return mBallProgram;
    }
}
