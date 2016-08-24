package com.example.a111.game.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.a111.game.model.BaseBall;
import com.example.a111.game.R;

public class MySurfaceView extends GLSurfaceView {
    private Handler mHandler;

    private SceneRenderer mRenderer;//场景渲染器
    int textureId;      //系统分配的纹理id

    public MySurfaceView(Context context) {
        super(context);
        init();
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.setEGLContextClientVersion(2); //设置使用OPENGL ES2.0
        mRenderer = new SceneRenderer();    //创建场景渲染器
        setRenderer(mRenderer);                //设置渲染器
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//设置渲染模式为主动渲染
    }

    public void setHandler(Handler handler) {

        this.mHandler = handler;
    }

    private class SceneRenderer implements GLSurfaceView.Renderer {

        private BaseBall mBall;
        private BaseBall mBall1;
        private BaseBall mBall2;
        private BaseBall mBall3;
        private BaseBall mBall4;
        private ArrayList<BaseBall> baseBalls = new ArrayList<>();

        public void onDrawFrame(GL10 gl) {
            //清除深度缓冲与颜色缓冲
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

            for (BaseBall ball : baseBalls) {
                //保护现场
                ball.pushMatrix();
                ball.drawSelf(textureId);
                ball.popMatrix();

                if (ball.collision) {
                    Message message = new Message();
                    message.what = 1111;
                    mHandler.sendMessage(message);
                    ball.reStartMove();
                }
            }
        }


        public void onSurfaceChanged(GL10 gl, int width, int height) {
            //设置视窗大小及位置
            GLES20.glViewport(0, 0, width, height);
            //计算GLSurfaceView的宽高比
            float ratio = (float) width / height;

            //调用此方法计算产生透视投影矩阵
            for (BaseBall ball : baseBalls) {
                ball.setProjectFrustum(-ratio, ratio, -1, 1, 4f, 300);
            }
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //设置屏幕背景色RGBA
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            //启用深度测试
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            //设置为打开背面剪裁
            GLES20.glEnable(GLES20.GL_CULL_FACE);
            //加载纹理
            textureId = initTexture(R.drawable.aaa);

            mBall = new BaseBall(MySurfaceView.this, 2, 1.6f, 15,0);
            mBall1 = new BaseBall(MySurfaceView.this, 2, 1.6f, 15,200);
            mBall2 = new BaseBall(MySurfaceView.this, 2, 1.6f, 15,400);
            mBall3 = new BaseBall(MySurfaceView.this, 2, 1.6f, 15,600);
            mBall4 = new BaseBall(MySurfaceView.this, 2, 1.6f, 15,800);

            baseBalls.add(mBall);
            baseBalls.add(mBall1);
            baseBalls.add(mBall2);
            baseBalls.add(mBall3);
            baseBalls.add(mBall4);
        }
    }

    public int initTexture(int drawableId)//textureId
    {
        //生成纹理ID
        int[] textures = new int[1];
        GLES20.glGenTextures
                (
                        1,          //产生的纹理id的数量
                        textures,   //纹理id的数组
                        0           //偏移量
                );
        int textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        //通过输入流加载图片===============begin===================
        InputStream is = this.getResources().openRawResource(drawableId);
        Bitmap bitmapTmp;
        try {
            bitmapTmp = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //通过输入流加载图片===============end=====================

        //实际加载纹理
        GLUtils.texImage2D
                (
                        GLES20.GL_TEXTURE_2D,   //纹理类型，在OpenGL ES中必须为GL10.GL_TEXTURE_2D
                        0,                      //纹理的层次，0表示基本图像层，可以理解为直接贴图
                        bitmapTmp,              //纹理图像
                        0                      //纹理边框尺寸
                );
        bitmapTmp.recycle();          //纹理加载成功后释放图片

        return textureId;
    }
}
