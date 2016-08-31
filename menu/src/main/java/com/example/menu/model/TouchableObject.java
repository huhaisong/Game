package com.example.menu.model;


import android.opengl.Matrix;

import com.example.menu.util.AABB3;
import com.example.menu.util.Vector3f;

/*
 * 可以被触控到的抽象类，
 * 物体继承了该类可以被触控到
 */
public abstract class TouchableObject {

    public int id;

    public AABB3 preBox;//仿射变换之前的包围盒

    //获得中心点位置和长宽高的方法
    public AABB3 getCurrBox() {
        return preBox.setToTransformedBox(currMatrix);//获取变换后的包围盒
    }

    private float[] mProjMatrix = new float[16];//投影
    private float[] mVMatrix = new float[16];//摄像机位置朝向9参数矩阵
    protected float[] currMatrix;//当前变换矩阵
    private float[] mMVPMatrix = new float[16]; //总矩阵

    public void setInitStack()//获取不变换初始矩阵
    {
        currMatrix = new float[16];
        Matrix.setRotateM(currMatrix, 0, 0, 1, 0, 0);
    }

    //设置透视投影参数
    public void setProjectFrustum(float left, float right, float bottom, float top, float near, float far) {
        Matrix.frustumM(mProjMatrix, 0, left, right, bottom, top, near, far);
    }

    public void setCamera(float[] headView) {
        this.mVMatrix = headView;
    }

    public void scale(float x, float y, float z) {
        Matrix.scaleM(currMatrix, 0, x, y, z);
    }

    public void translate(float x, float y, float z) {
        Matrix.translateM(currMatrix, 0, x, y, z);
    }

    public void rotate(float angle, float x, float y, float z) {
        Matrix.rotateM(currMatrix, 0, angle, x, y, z);
    }

    public float[] getFinalMatrix() {
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, currMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
        return mMVPMatrix;
    }

    public boolean isPickup(float[] AB) {
        //射线AB
        Vector3f start = new Vector3f(AB[0], AB[1], AB[2]);//起点
        Vector3f end = new Vector3f(AB[3], AB[4], AB[5]);//终点
        Vector3f dir = end.minus(start);//长度和方向
        //判断是否相交  计算AB线段与物体包围盒的最佳交点(与A点最近的交点)
        AABB3 box = getCurrBox(); //获得物体AABB包围盒
        float t = box.rayIntersect(start, dir, null);//计算相交时间
        return t <= 1;
    }
}
