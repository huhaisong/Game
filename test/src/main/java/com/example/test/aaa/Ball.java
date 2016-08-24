package com.example.test.aaa;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

public class Ball {
    private IntBuffer vertexBuffer;  //顶点坐标数据缓冲
    private ByteBuffer indexBuffer; //顶点构建索引数据缓冲
    private FloatBuffer mColorBuffer;
    public float angleX;  //沿x轴旋转角度
    int vCount = 0;
    int iCount = 0;

    private String mVertexShader, mFragmentShader;
    private int maPositionHandle;
    private int maColorHandle;
    private int mProgram;
    private int mMVPMatrixHandle;

    public Ball(Context context, int scale) {

        initData(scale);
        initShader(context);
    }

    private void initShader(Context context) {

        //设置屏幕背景色RGBA
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        mVertexShader = ShaderUtil.loadFromAssetsFile("vertex.sh", context.getResources());
        mFragmentShader = ShaderUtil.loadFromAssetsFile("frag.sh", context.getResources());
        mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        maColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
    }

    private void initData(int scale) {
        //顶点坐标初始化数据
        final int UNIT_SIZE = 10000;
        ArrayList<Integer> alVertex = new ArrayList<Integer>();
        final int angleSpan = 18;          //将小球进行单位切分的角度
        for (int vAngle = -90; vAngle <= 90; vAngle = vAngle + angleSpan) {  //垂直方向angleSpan度一份
            for (int hAngle = 0; hAngle < 360; hAngle = hAngle + angleSpan) { //水平方向angleSpan度一份
                //纵向横向各到一个角度后计算对应的此点在球面上的坐标
                double xozLength = scale * UNIT_SIZE * Math.cos(Math.toRadians(vAngle));
                int x = (int) (xozLength * Math.cos(Math.toRadians(hAngle)));
                int y = (int) (xozLength * Math.sin(Math.toRadians(hAngle)));
                int z = (int) (scale * UNIT_SIZE * Math.sin(Math.toRadians(vAngle)));
                alVertex.add(x);
                alVertex.add(y);
                alVertex.add(z);
            }
        }
        vCount = alVertex.size() / 3;  //顶点数量为坐标值数量的三分之一，因为一个顶点有三个坐标
        //将alVertix中的坐标值转存到一个int数组中
        int vertices[] = new int[alVertex.size()];
        for (int i = 0; i < alVertex.size(); i++) {
            vertices[i] = alVertex.get(i);
        }

        //创建顶点坐标数据缓冲
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder()); //设置字节顺序
        vertexBuffer = vbb.asIntBuffer();  //转换成int型缓冲
        vertexBuffer.put(vertices);   //向缓冲区放入顶点坐标数据
        vertexBuffer.position(0);  //设置缓冲区起始位置
/*
        //创建顶点坐标数据缓冲
        ByteBuffer nbb = ByteBuffer.allocateDirect(vertices.length * 4); //一个整型是4个字节
        nbb.order(ByteOrder.nativeOrder());  //设置字节顺序   由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
        nomalBuffer = nbb.asIntBuffer(); //转换成int型缓冲
        nomalBuffer.put(vertices);      //想缓冲区放入顶点坐标数据
        nomalBuffer.position(0);         //设置缓冲区起始位置*/

        ArrayList<Integer> alIndex = new ArrayList<Integer>();
        int row = (180 / angleSpan) + 1; //球面切分的行数
        int col = 360 / angleSpan;  //球面切分的列数
        for (int i = 0; i < row; i++) {  //对每一行循环
            if (i > 0 && i < row - 1) {
                //中间行
                for (int j = -1; j < col; j++) {
                    //中间行的两个相邻点与下一行的对应点构成三角形
                    int k = i * col + j;
                    alIndex.add(k + col);
                    alIndex.add(k + 1);
                    alIndex.add(k);
                }
                for (int j = 0; j < col + 1; j++) {
                    //中间行的两个相邻点与上一行的对应点构成三角形
                    int k = i * col + j;
                    alIndex.add(k - col);
                    alIndex.add(k - 1);
                    alIndex.add(k);
                }
            }
        }
        iCount = alIndex.size();
        byte indices[] = new byte[iCount];

        for (int i = 0; i < iCount; i++) {
            indices[i] = alIndex.get(i).byteValue();

        }
        //三角形构造数据索引缓冲
        indexBuffer = ByteBuffer.allocateDirect(iCount);  //由于indices是byte型的，索引不用乘以4
        indexBuffer.put(indices);
        indexBuffer.position(0);

        float colors[] = new float[iCount * 4 / 3];

        for (int i = 0; i < iCount / 3; i++) {

            colors[i] = 1.0f;
            colors[i + 1] = 0.0f;
            colors[i + 2] = 0.0f;
            colors[i + 3] = 1.0f;
        }

        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        mColorBuffer = cbb.asFloatBuffer();
        mColorBuffer.put(colors);
        mColorBuffer.position(0);
    }

    public void drawSelf(int textureID) {

        GLES20.glUseProgram(mProgram);
        //GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, temp, 0);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glVertexAttribPointer(maColorHandle, 4, GLES20.GL_FLOAT, false, 4 * 4, mColorBuffer);
        GLES20.glEnableVertexAttribArray(maColorHandle);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, iCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
    }

}
