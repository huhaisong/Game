package com.example.pickup.ball;


import android.opengl.Matrix;

import com.example.pickup.util.AABB3;

/*
 * 可以被触控到的抽象类，
 * 物体继承了该类可以被触控到
 */
public abstract class TouchableObject {
    public AABB3 preBox;//仿射变换之前的包围盒
    public float[] m = new float[16];//仿射变换的矩阵
    //顶点颜色
    float[] color=new float[]{1,1,1,1};
    public float size = 1.5f;;//放大的尺寸
    //获得中心点位置和长宽高的方法
    public AABB3 getCurrBox(){
        return preBox.setToTransformedBox(currMatrix);//获取变换后的包围盒
    }

    //触控后的动作，根据需要要做相应改动
    public void changeOnTouch(boolean flag){
        if (flag) {
            color = new float[] { 0, 1, 0, 1 };
            size = 3f;
        } else {
            color = new float[] { 1, 1, 1, 1 };
            size = 1.5f;
        }
    }


    private float[] mProjMatrix = new float[16];//4x4矩阵 投影用
    private float[] mVMatrix = new float[16];//摄像机位置朝向9参数矩阵
    protected float[] currMatrix;//当前变换矩阵
    private float[] mMVPMatrix = new float[16];
    //保护变换矩阵的栈
    private static float[][] mStack = new float[10][16];
    private static int stackTop = -1;

    public void setInitStack()//获取不变换初始矩阵
    {
        currMatrix = new float[16];
        Matrix.setRotateM(currMatrix, 0, 0, 1, 0, 0);
    }

    //设置透视投影参数
    public void setProjectFrustum
    (
            float left,     //near面的left
            float right,    //near面的right
            float bottom,   //near面的bottom
            float top,      //near面的top
            float near,     //near面距离
            float far       //far面距离
    ) {
        Matrix.frustumM(mProjMatrix, 0, left, right, bottom, top, near, far);
    }

    public void setCamera
            (
                    float cx,   //摄像机位置x
                    float cy,   //摄像机位置y
                    float cz,   //摄像机位置z
                    float tx,   //摄像机目标点x
                    float ty,   //摄像机目标点y
                    float tz,   //摄像机目标点z
                    float upx,  //摄像机UP向量X分量
                    float upy,  //摄像机UP向量Y分量
                    float upz   //摄像机UP向量Z分量
            ) {
        Matrix.setLookAtM(mVMatrix, 0, cx, cy, cz, tx, ty, tz, upx, upy, upz);
    }

    public void scale(float x, float y, float z) {
        Matrix.scaleM(currMatrix, 0, x, y, z);
    }

    public void translate(float x, float y, float z)//设置沿xyz轴移动
    {
        Matrix.translateM(currMatrix, 0, x, y, z);
    }

    public void rotate(float angle, float x, float y, float z)//设置绕xyz轴移动
    {
        Matrix.rotateM(currMatrix, 0, angle, x, y, z);
    }

    public float[] getFinalMatrix() {
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, currMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
        return mMVPMatrix;
    }

    public void pushMatrix()//保护变换矩阵
    {
        stackTop++;
        System.arraycopy(currMatrix, 0, mStack[stackTop], 0, 16);
    }

    public void popMatrix()//恢复变换矩阵
    {
        for (int i = 0; i < 16; i++) {
            currMatrix[i] = mStack[stackTop][i];
        }
        stackTop--;
    }

    public  float[] getVMatrix() {
        return mVMatrix;
    }
}
