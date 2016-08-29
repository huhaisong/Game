package com.example.test.ball;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.test.MySurfaceView;
import com.example.test.TouchableObject;
import com.example.test.util.AABB3;
import com.example.test.util.MatrixState;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class BaseBall extends TouchableObject {

    private float[] mProjMatrix = new float[16];//4x4矩阵 投影用
    private float[] mVMatrix = new float[16];//摄像机位置朝向9参数矩阵
    private float[] currMatrix;//当前变换矩阵
    private float[] mMVPMatrix = new float[16];
    //保护变换矩阵的栈
    private static float[][] mStack = new float[10][16];
    private static int stackTop = -1;

    public boolean collision = false;

    public BallProgram mBallProgram;

    FloatBuffer mVertexBuffer;//顶点坐标数据缓冲
    FloatBuffer mTexCoorBuffer;//顶点纹理坐标数据缓冲
    FloatBuffer mNormalBuffer;//顶点法向量数据缓冲
    int vCount = 0;
    float bHalf = 0;//黄金长方形的宽
    float r = 0;//球的半径


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

    //设置正交投影参数
    public void setProjectOrtho
    (
            float left,        //near面的left
            float right,    //near面的right
            float bottom,   //near面的bottom
            float top,      //near面的top
            float near,        //near面距离
            float far       //far面距离
    ) {
        Matrix.orthoM(mProjMatrix, 0, left, right, bottom, top, near, far);
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

    public BaseBall(MySurfaceView mv, float scale, float aHalf, int n, long time) {
        //调用初始化顶点数据的initVertexData方法
        initVertexData(scale, aHalf, n);
        //调用初始化着色器的intShader方法
        mBallProgram = BallProgram.getInstance();
        setCamera(0, 0, 8.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        setInitStack();
        scale(0.1f, 0.1f, 0.1f);
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        reStartMove();
    }

    public void reStartMove() {
        collision = false;
        setInitStack();

        translate((float) (-50 + Math.random() * 100), (float) (-30 + Math.random() * 60), -250);
        new Thread() {
            public void run() {
                int translateZ = 0;
                while (translateZ < 140) {
                    translateZ += 1;
                    translate(0, 0, 1);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                collision = true;
            }
        }.start();
    }

    //自定义的初始化顶点数据的方法
    public void initVertexData(float scale, float aHalf, int n) //大小，黄金长方形长边的一半，分段数
    {
        aHalf *= scale;        //长边的一半
        bHalf = aHalf * 0.618034f;        //短边的一半
        r = (float) Math.sqrt(aHalf * aHalf + bHalf * bHalf); //半径
        vCount = 3 * 20 * n * n;//顶点个数，共有20个三角形，每个三角形都有三个顶点
        //正20面体坐标数据初始化
        ArrayList<Float> alVertix20 = new ArrayList<>();//正20面体的顶点列表（未卷绕）
        ArrayList<Integer> alFaceIndex20 = new ArrayList<>();//正20面体组织成面的顶点的索引值列表（按逆时针卷绕）
        //正20面体顶点
        initAlVertix20(alVertix20, aHalf, bHalf);
        //正20面体索引
        initAlFaceIndex20(alFaceIndex20);
        //计算卷绕顶点
        float[] vertices20 = VectorUtil.cullVertex(alVertix20, alFaceIndex20);//只计算顶点

        //坐标数据初始化
        ArrayList<Float> alVertix = new ArrayList<>();//原顶点列表（未卷绕）
        ArrayList<Integer> alFaceIndex = new ArrayList<>();//组织成面的顶点的索引值列表（按逆时针卷绕）
        int vnCount = 0;//前i-1行前所有顶点数的和
        for (int k = 0; k < vertices20.length; k += 9)//对正20面体每个大三角形循环
        {
            float[] v1 = new float[]{vertices20[k], vertices20[k + 1], vertices20[k + 2]};
            float[] v2 = new float[]{vertices20[k + 3], vertices20[k + 4], vertices20[k + 5]};
            float[] v3 = new float[]{vertices20[k + 6], vertices20[k + 7], vertices20[k + 8]};
            //顶点
            for (int i = 0; i <= n; i++) {
                float[] viStart = VectorUtil.devideBall(r, v1, v2, n, i);
                float[] viEnd = VectorUtil.devideBall(r, v1, v3, n, i);
                for (int j = 0; j <= i; j++) {
                    float[] vi = VectorUtil.devideBall(r, viStart, viEnd, i, j);
                    alVertix.add(vi[0]);
                    alVertix.add(vi[1]);
                    alVertix.add(vi[2]);
                }
            }
            //索引
            for (int i = 0; i < n; i++) {
                if (i == 0) {//若是第0行，直接加入卷绕后顶点索引012
                    alFaceIndex.add(vnCount);
                    alFaceIndex.add(vnCount + 1);
                    alFaceIndex.add(vnCount + 2);
                    vnCount += 1;
                    if (i == n - 1) {//如果是每个大三角形的最后一次循环，将下一列的顶点个数也加上
                        vnCount += 2;
                    }
                    continue;
                }
                int iStart = vnCount;//第i行开始的索引
                int viCount = i + 1;//第i行顶点数
                int iEnd = iStart + viCount - 1;//第i行结束索引

                int iStartNext = iStart + viCount;//第i+1行开始的索引
                int viCountNext = viCount + 1;//第i+1行顶点数
                int iEndNext = iStartNext + viCountNext - 1;//第i+1行结束的索引
                //前面的四边形
                for (int j = 0; j < viCount - 1; j++) {
                    int index0 = iStart + j;//四边形的四个顶点索引
                    int index1 = index0 + 1;
                    int index2 = iStartNext + j;
                    int index3 = index2 + 1;
                    alFaceIndex.add(index0);
                    alFaceIndex.add(index2);
                    alFaceIndex.add(index3);//加入前面的四边形
                    alFaceIndex.add(index0);
                    alFaceIndex.add(index3);
                    alFaceIndex.add(index1);
                }// j
                alFaceIndex.add(iEnd);
                alFaceIndex.add(iEndNext - 1);
                alFaceIndex.add(iEndNext); //最后一个三角形
                vnCount += viCount;//第i行前所有顶点数的和
                if (i == n - 1) {//如果是每个大三角形的最后一次循环，将下一列的顶点个数也加上
                    vnCount += viCountNext;
                }
            }// i
        }// k

        //计算卷绕顶点
        float[] vertices = VectorUtil.cullVertex(alVertix, alFaceIndex);//只计算顶点
        float[] normals = vertices;//顶点就是法向量

        //纹理
        //正20面体纹理坐标数据初始化
        ArrayList<Float> alST20 = new ArrayList<>();//正20面体的纹理坐标列表（未卷绕）
        ArrayList<Integer> alTexIndex20 = new ArrayList<>();//正20面体组织成面的纹理坐标的索引值列表（按逆时针卷绕）
        //正20面体纹理坐标
        float sSpan = 1 / 5.5f;//每个纹理三角形的边长
        float tSpan = 1 / 3.0f;//每个纹理三角形的高
        //按正二十面体的平面展开图计算纹理坐标
        for (int i = 0; i < 5; i++) {
            alST20.add(sSpan + sSpan * i);
            alST20.add(0f);
        }
        for (int i = 0; i < 6; i++) {
            alST20.add(sSpan / 2 + sSpan * i);
            alST20.add(tSpan);
        }
        for (int i = 0; i < 6; i++) {
            alST20.add(sSpan * i);
            alST20.add(tSpan * 2);
        }
        for (int i = 0; i < 5; i++) {
            alST20.add(sSpan / 2 + sSpan * i);
            alST20.add(tSpan * 3);
        }
        //正20面体索引
        initAlTexIndex20(alTexIndex20);

        //计算卷绕纹理坐标
        float[] st20 = VectorUtil.cullTexCoor(alST20, alTexIndex20);//只计算纹理坐标
        ArrayList<Float> alST = new ArrayList<>();//原纹理坐标列表（未卷绕）
        for (int k = 0; k < st20.length; k += 6) {
            float[] st1 = new float[]{st20[k], st20[k + 1], 0};//三角形的纹理坐标
            float[] st2 = new float[]{st20[k + 2], st20[k + 3], 0};
            float[] st3 = new float[]{st20[k + 4], st20[k + 5], 0};
            for (int i = 0; i <= n; i++) {
                float[] stiStart = VectorUtil.devideLine(st1, st2, n, i);
                float[] stiEnd = VectorUtil.devideLine(st1, st3, n, i);
                for (int j = 0; j <= i; j++) {
                    float[] sti = VectorUtil.devideLine(stiStart, stiEnd, i, j);
                    //将纹理坐标加入列表
                    alST.add(sti[0]);
                    alST.add(sti[1]);
                }
            }
        }
        //计算卷绕后纹理坐标
        float[] textures = VectorUtil.cullTexCoor(alST, alFaceIndex);

        preBox = new AABB3(vertices);
        //顶点坐标数据初始化
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);//创建顶点坐标数据缓冲
        vbb.order(ByteOrder.nativeOrder());//设置字节顺序为本地操作系统顺序
        mVertexBuffer = vbb.asFloatBuffer();//转换为float型缓冲
        mVertexBuffer.put(vertices);//向缓冲区中放入顶点坐标数据
        mVertexBuffer.position(0);//设置缓冲区起始位置
        //法向量数据初始化
        ByteBuffer nbb = ByteBuffer.allocateDirect(normals.length * 4);//创建顶点法向量数据缓冲
        nbb.order(ByteOrder.nativeOrder());//设置字节顺序为本地操作系统顺序
        mNormalBuffer = nbb.asFloatBuffer();//转换为float型缓冲
        mNormalBuffer.put(normals);//向缓冲区中放入顶点法向量数据
        mNormalBuffer.position(0);//设置缓冲区起始位置
        //st坐标数据初始化
        ByteBuffer tbb = ByteBuffer.allocateDirect(textures.length * 4);//创建顶点纹理数据缓冲
        tbb.order(ByteOrder.nativeOrder());//设置字节顺序为本地操作系统顺序
        mTexCoorBuffer = tbb.asFloatBuffer();//转换为float型缓冲
        mTexCoorBuffer.put(textures);//向缓冲区中放入顶点纹理数据
        mTexCoorBuffer.position(0);//设置缓冲区起始位置
    }

    //12个顶点
    public void initAlVertix20(ArrayList<Float> alVertix20, float aHalf, float bHalf) {

        alVertix20.add(0f);
        alVertix20.add(aHalf);
        alVertix20.add(-bHalf);//顶正棱锥顶点

        alVertix20.add(0f);
        alVertix20.add(aHalf);
        alVertix20.add(bHalf);//棱柱上的点
        alVertix20.add(aHalf);
        alVertix20.add(bHalf);
        alVertix20.add(0f);
        alVertix20.add(bHalf);
        alVertix20.add(0f);
        alVertix20.add(-aHalf);
        alVertix20.add(-bHalf);
        alVertix20.add(0f);
        alVertix20.add(-aHalf);
        alVertix20.add(-aHalf);
        alVertix20.add(bHalf);
        alVertix20.add(0f);

        alVertix20.add(-bHalf);
        alVertix20.add(0f);
        alVertix20.add(aHalf);
        alVertix20.add(bHalf);
        alVertix20.add(0f);
        alVertix20.add(aHalf);
        alVertix20.add(aHalf);
        alVertix20.add(-bHalf);
        alVertix20.add(0f);
        alVertix20.add(0f);
        alVertix20.add(-aHalf);
        alVertix20.add(-bHalf);
        alVertix20.add(-aHalf);
        alVertix20.add(-bHalf);
        alVertix20.add(0f);

        alVertix20.add(0f);
        alVertix20.add(-aHalf);
        alVertix20.add(bHalf);//底棱锥顶点

    }

    //20个面
    public void initAlFaceIndex20(ArrayList<Integer> alFaceIndex20) { //初始化正二十面体的顶点索引数据

        alFaceIndex20.add(0);
        alFaceIndex20.add(1);
        alFaceIndex20.add(2);
        alFaceIndex20.add(0);
        alFaceIndex20.add(2);
        alFaceIndex20.add(3);
        alFaceIndex20.add(0);
        alFaceIndex20.add(3);
        alFaceIndex20.add(4);
        alFaceIndex20.add(0);
        alFaceIndex20.add(4);
        alFaceIndex20.add(5);
        alFaceIndex20.add(0);
        alFaceIndex20.add(5);
        alFaceIndex20.add(1);

        alFaceIndex20.add(1);
        alFaceIndex20.add(6);
        alFaceIndex20.add(7);
        alFaceIndex20.add(1);
        alFaceIndex20.add(7);
        alFaceIndex20.add(2);
        alFaceIndex20.add(2);
        alFaceIndex20.add(7);
        alFaceIndex20.add(8);
        alFaceIndex20.add(2);
        alFaceIndex20.add(8);
        alFaceIndex20.add(3);
        alFaceIndex20.add(3);
        alFaceIndex20.add(8);
        alFaceIndex20.add(9);
        alFaceIndex20.add(3);
        alFaceIndex20.add(9);
        alFaceIndex20.add(4);
        alFaceIndex20.add(4);
        alFaceIndex20.add(9);
        alFaceIndex20.add(10);
        alFaceIndex20.add(4);
        alFaceIndex20.add(10);
        alFaceIndex20.add(5);
        alFaceIndex20.add(5);
        alFaceIndex20.add(10);
        alFaceIndex20.add(6);
        alFaceIndex20.add(5);
        alFaceIndex20.add(6);
        alFaceIndex20.add(1);

        alFaceIndex20.add(6);
        alFaceIndex20.add(11);
        alFaceIndex20.add(7);
        alFaceIndex20.add(7);
        alFaceIndex20.add(11);
        alFaceIndex20.add(8);
        alFaceIndex20.add(8);
        alFaceIndex20.add(11);
        alFaceIndex20.add(9);
        alFaceIndex20.add(9);
        alFaceIndex20.add(11);
        alFaceIndex20.add(10);
        alFaceIndex20.add(10);
        alFaceIndex20.add(11);
        alFaceIndex20.add(6);
    }

    //纹理索引
    public void initAlTexIndex20(ArrayList<Integer> alTexIndex20) {
        alTexIndex20.add(0);
        alTexIndex20.add(5);
        alTexIndex20.add(6);
        alTexIndex20.add(1);
        alTexIndex20.add(6);
        alTexIndex20.add(7);
        alTexIndex20.add(2);
        alTexIndex20.add(7);
        alTexIndex20.add(8);
        alTexIndex20.add(3);
        alTexIndex20.add(8);
        alTexIndex20.add(9);
        alTexIndex20.add(4);
        alTexIndex20.add(9);
        alTexIndex20.add(10);

        alTexIndex20.add(5);
        alTexIndex20.add(11);
        alTexIndex20.add(12);
        alTexIndex20.add(5);
        alTexIndex20.add(12);
        alTexIndex20.add(6);
        alTexIndex20.add(6);
        alTexIndex20.add(12);
        alTexIndex20.add(13);
        alTexIndex20.add(6);
        alTexIndex20.add(13);
        alTexIndex20.add(7);
        alTexIndex20.add(7);
        alTexIndex20.add(13);
        alTexIndex20.add(14);
        alTexIndex20.add(7);
        alTexIndex20.add(14);
        alTexIndex20.add(8);
        alTexIndex20.add(8);
        alTexIndex20.add(14);
        alTexIndex20.add(15);
        alTexIndex20.add(8);
        alTexIndex20.add(15);
        alTexIndex20.add(9);
        alTexIndex20.add(9);
        alTexIndex20.add(15);
        alTexIndex20.add(16);
        alTexIndex20.add(9);
        alTexIndex20.add(16);
        alTexIndex20.add(10);

        alTexIndex20.add(11);
        alTexIndex20.add(17);
        alTexIndex20.add(12);
        alTexIndex20.add(12);
        alTexIndex20.add(18);
        alTexIndex20.add(13);
        alTexIndex20.add(13);
        alTexIndex20.add(19);
        alTexIndex20.add(14);
        alTexIndex20.add(14);
        alTexIndex20.add(20);
        alTexIndex20.add(15);
        alTexIndex20.add(15);
        alTexIndex20.add(21);
        alTexIndex20.add(16);

    }

    public void drawSelf(int texId) {

        copyM();
        //制定使用某套shader程序
        GLES20.glUseProgram(mBallProgram.mProgram);
        //将最终变换矩阵传入shader程序
        GLES20.glUniformMatrix4fv(mBallProgram.muMVPMatrixHandle, 1, false, getFinalMatrix(), 0);

        //传送顶点位置数据
        GLES20.glVertexAttribPointer(mBallProgram.maPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer);
        //传送顶点纹理坐标数据
        GLES20.glVertexAttribPointer(mBallProgram.maTexCoorHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, mTexCoorBuffer);

        //启用顶点位置数据
        GLES20.glEnableVertexAttribArray(mBallProgram.maPositionHandle);
        //启用顶点纹理数据
        GLES20.glEnableVertexAttribArray(mBallProgram.maTexCoorHandle);

        //绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);

        //绘制纹理矩形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);
    }

    public void copyM() {
        for (int i = 0; i < 16; i++) {
            m[i] = currMatrix[i];
        }
    }
}
