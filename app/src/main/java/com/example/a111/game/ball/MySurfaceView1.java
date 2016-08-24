package com.example.a111.game.ball;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MySurfaceView1 extends GLSurfaceView {

    private final float TOUCH_SCALE_FACTOR = 0.5f;
    private SceneRenderer myRenderer;   //场景渲染器
    public boolean openLightFlag = false;  //开灯标记，false为关灯，true为开灯
    private float previousX, previousY;  //上次触控的横纵坐标

    private Context mContext;

    public MySurfaceView1(Context context) {
        super(context);
        mContext = context;
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        myRenderer = new SceneRenderer();  //创建场景渲染器
        setRenderer(myRenderer);  //设置渲染器
        //setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY); //渲染模式为主动渲染
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        //float y=event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                //float dy=y-previousY;  //计算触控笔移动Y位移
                float dx = x - previousX;   //计算触控笔移动X位移
                myRenderer.ball.angleX += dx * TOUCH_SCALE_FACTOR;  //设置沿x轴旋转角度
                requestRender();                               //渲染画面
                break;
        }
        previousX = x;                                   //前一次触控位置x坐标
        return true;                                      //事件成功返回true
    }

    private class SceneRenderer implements Renderer {
        Ball ball;  //创建圆

        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            ball.drawSelf(0);
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            ball = new Ball(mContext, 4);
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDisable(GLES20.GL_CULL_FACE);
        }
    }
}