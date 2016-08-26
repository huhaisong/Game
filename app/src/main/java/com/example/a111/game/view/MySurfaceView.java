package com.example.a111.game.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.GLES20;
import android.opengl.Matrix;
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
import com.example.a111.game.util.AABB3;
import com.example.a111.game.util.IntersectantUtil;
import com.example.a111.game.util.Vector3f;
import com.google.vrtoolkit.cardboard.sensors.HeadTracker;

public class MySurfaceView extends GLSurfaceView {

    private Handler mHandler;

    private HeadTracker mHeadTracker;
    private float[] mHeadView = new float[16];

    private SceneRenderer mRenderer;//场景渲染器
    int textureId;      //系统分配的纹理id

    private int mWidth;
    private int mHeight;
    float left;
    float right;
    float top;
    float bottom;
    float near;
    float far;

    //可触控物体列表
    ArrayList<BaseBall> baseBalls = new ArrayList<>();
    //被选中物体的索引值，即id，没有被选中时索引值为-1
    int onFocusId = -1;
    long startTime = 0;
    long animationtimes = 300;

    public MySurfaceView(Context context) {
        super(context);
        init(context);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.setEGLContextClientVersion(2); //设置使用OPENGL ES2.0
        mRenderer = new SceneRenderer();    //创建场景渲染器
        setRenderer(mRenderer);                //设置渲染器
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//设置渲染模式为主动渲染
        Matrix.setIdentityM(mHeadView, 0);
        mHeadTracker = HeadTracker.createFromContext(context);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHeadTracker.stopTracking();
    }

    @Override
    public void onResume() {
        super.onResume();
        mHeadTracker.startTracking();
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
        private BaseBall mBall5;
        private BaseBall mBall6;
        private BaseBall mBall7;
        private BaseBall mBall8;
        private BaseBall mBall9;

        public void onDrawFrame(GL10 gl) {
            //清除深度缓冲与颜色缓冲
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            getOnFocusId();
            changeObj();
            mHeadTracker.getLastHeadView(mHeadView, 0);

            GLES20.glViewport(0, 0, mWidth / 2, mHeight);
            for (BaseBall ball : baseBalls) {
                ball.setCamera(mHeadView);
                ball.drawSelf(textureId);
                if (ball.collision) {
                    Message message = new Message();
                    message.what = 1111;
                    mHandler.sendMessage(message);
                    ball.reStartMove();
                }
            }

            GLES20.glViewport(mWidth / 2, 0, mWidth / 2, mHeight);
            for (BaseBall ball : baseBalls) {
                ball.setCamera(mHeadView);
                ball.drawSelf(textureId);
                if (ball.collision) {
                    Message message = new Message();
                    message.what = 1111;
                    mHandler.sendMessage(message);
                    ball.reStartMove();
                }
            }


        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mWidth = width;
            mHeight = height;
            //计算GLSurfaceView的宽高比
            float ratio = (float) width / height / 2;

            left = right = ratio;
            top = bottom = 1;
            near = 2;
            far = 500;

            for (BaseBall ball : baseBalls) {
                ball.setProjectFrustum(-left, right, -bottom, top, near, far);
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

            mBall = new BaseBall(MySurfaceView.this, 2, 1.6f, 15, 0);
            mBall1 = new BaseBall(MySurfaceView.this, 2, 1.6f, 15, 100);
            mBall2 = new BaseBall(MySurfaceView.this, 2, 1.6f, 15, 200);
            mBall3 = new BaseBall(MySurfaceView.this, 2, 1.6f, 15, 300);
            mBall4 = new BaseBall(MySurfaceView.this, 2, 1.6f, 15, 400);
            mBall5 = new BaseBall(MySurfaceView.this, 2, 1.6f, 15, 500);
            mBall6 = new BaseBall(MySurfaceView.this, 2, 1.6f, 15, 600);
            mBall7 = new BaseBall(MySurfaceView.this, 2, 1.6f, 15, 700);
            mBall8 = new BaseBall(MySurfaceView.this, 2, 1.6f, 15, 800);
            mBall9 = new BaseBall(MySurfaceView.this, 2, 1.6f, 15, 900);

            baseBalls.add(mBall);
            baseBalls.add(mBall1);
            baseBalls.add(mBall2);
            baseBalls.add(mBall3);
            baseBalls.add(mBall4);
            baseBalls.add(mBall5);
            baseBalls.add(mBall6);
            baseBalls.add(mBall7);
            baseBalls.add(mBall8);
            baseBalls.add(mBall9);
        }
    }

    public void getOnFocusId() {

        float[] AB = IntersectantUtil.calculateABPosition(mWidth / 2, mHeight / 2,
                mWidth, mHeight, left, top, near, far, mHeadView);
        //射线AB
        Vector3f start = new Vector3f(AB[0], AB[1], AB[2]);//起点
        Vector3f end = new Vector3f(AB[3], AB[4], AB[5]);//终点
        Vector3f dir = end.minus(start);//长度和方向
            /*
             * 计算AB线段与每个物体包围盒的最佳交点(与A点最近的交点)，
			 * 并记录有最佳交点的物体在列表中的索引值
			 */
        //记录列表中时间最小的索引值
        int tmpIndex = -1;//记录与A点最近物体索引的临时值
        float minTime = 1;//记录列表中所有物体与AB相交的最短时间
        for (int i = 0; i < baseBalls.size(); i++) {//遍历列表中的物体
            AABB3 box = baseBalls.get(i).getCurrBox(); //获得物体AABB包围盒
            float t = box.rayIntersect(start, dir, null);//计算相交时间
            if (t <= minTime) {
                minTime = t;//记录最小值
                tmpIndex = i;//记录最小值索引
            }
        }
        if (tmpIndex != -1) {
            if (onFocusId == tmpIndex) {

            } else {
                onFocusId = tmpIndex;
                startTime = System.currentTimeMillis();
            }
        } else {
            startTime = System.currentTimeMillis();
        }
    }

    //改变列表中下标为index的物体
    public void changeObj() {
        long t = System.currentTimeMillis() - startTime;
        if (t > animationtimes) {
            for (int i = 0; i < baseBalls.size(); i++) {
                if (i == onFocusId) {//改变选中的物体
                    baseBalls.get(i).reStartMove();
                }
            }
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
