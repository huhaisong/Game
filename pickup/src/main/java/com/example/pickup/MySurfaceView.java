package com.example.pickup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.view.MotionEvent;


import com.example.pickup.ball.BaseBall;
import com.example.pickup.util.AABB3;
import com.example.pickup.util.IntersectantUtil;
import com.example.pickup.util.Vector3f;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MySurfaceView extends GLSurfaceView {

    int textureId;      //系统分配的纹理id
    private SceneRenderer mRenderer;//场景渲染器

    float left;
    float right;
    float top;
    float bottom;
    float near;
    float far;

    //可触控物体列表
    ArrayList<BaseBall> lovnList = new ArrayList<>();
    //被选中物体的索引值，即id，没有被选中时索引值为-1
    int checkedIndex = -1;

    public MySurfaceView(Context context) {
        super(context);
        this.setEGLContextClientVersion(2); //设置使用OPENGL ES2.0
        mRenderer = new SceneRenderer();    //创建场景渲染器
        setRenderer(mRenderer);                //设置渲染器
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//设置渲染模式为主动渲染
    }

    //触摸事件回调方法
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float y = e.getY();
        float x = e.getX();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //计算仿射变换后AB两点的位置
                float[] AB = IntersectantUtil.calculateABPosition
                        (
                                Sample19_1_Activity.screenWidth/2, //触控点X坐标
                                Sample19_1_Activity.screenHeight/2, //触控点Y坐标
                                Sample19_1_Activity.screenWidth, //屏幕宽度
                                Sample19_1_Activity.screenHeight, //屏幕长度
                                left, //视角left、top值
                                top,
                                near, //视角near、far值
                                far,
                                lovnList.get(0).getVMatrix()
                        );
                //射线AB
                Vector3f start = new Vector3f(AB[0], AB[1], AB[2]);//起点
                Vector3f end = new Vector3f(AB[3], AB[4], AB[5]);//终点
                Vector3f dir = end.minus(start);//长度和方向
            /*
             * 计算AB线段与每个物体包围盒的最佳交点(与A点最近的交点)，
			 * 并记录有最佳交点的物体在列表中的索引值
			 */
                //记录列表中时间最小的索引值
                checkedIndex = -1;//标记为没有选中任何物体
                int tmpIndex = -1;//记录与A点最近物体索引的临时值
                float minTime = 1;//记录列表中所有物体与AB相交的最短时间
                for (int i = 0; i < lovnList.size(); i++) {//遍历列表中的物体
                    AABB3 box = lovnList.get(i).getCurrBox(); //获得物体AABB包围盒
                    float t = box.rayIntersect(start, dir, null);//计算相交时间
                    if (t <= minTime) {
                        minTime = t;//记录最小值
                        tmpIndex = i;//记录最小值索引
                    }
                }
                checkedIndex = tmpIndex;//将索引保存在checkedIndex中
                changeObj(checkedIndex);//改变被选中物体
                break;
        }
        return true;
    }

    //改变列表中下标为index的物体
    public void changeObj(int index) {
        if (index != -1) {//如果有物体被选中
            for (int i = 0; i < lovnList.size(); i++) {
                if (i == index) {//改变选中的物体
                    lovnList.get(i).changeOnTouch(true);
                } else {//恢复其他物体
                    lovnList.get(i).changeOnTouch(false);
                }
            }
        } else {//如果没有物体被选中
            for (int i = 0; i < lovnList.size(); i++) {//恢复其他物体
                lovnList.get(i).changeOnTouch(false);
            }
        }
    }

    private class SceneRenderer implements Renderer {

        private BaseBall ball;

        public void onDrawFrame(GL10 gl) {
            //清除深度缓冲与颜色缓冲
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            ball.pushMatrix();
            ball.scale(ball.size, ball.size, ball.size);
            ball.drawSelf(textureId);
            ball.popMatrix();
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            //设置视窗大小及位置
            GLES20.glViewport(0, 0, width, height);
            //计算GLSurfaceView的宽高比
            float ratio = (float) width / height;
            //调用此方法计算产生透视投影矩阵
            left = right = ratio;
            top = bottom = 1;
            near = 2;
            far = 500;
            ball.setProjectFrustum(-left, right, -bottom, top, near, far);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            textureId = initTexture(R.drawable.aaa);

            //设置屏幕背景色RGBA
            GLES20.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
            //打开深度检测
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            //关闭背面剪裁
            GLES20.glDisable(GLES20.GL_CULL_FACE);
            ball = new BaseBall(MySurfaceView.this,2f, 1.6f, 15, 0);

            ball.setCamera(0, 0, 50.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
            lovnList.add(ball);
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


      /* public void getOnFocusId(float[] AB) {

        //射线AB
        Vector3f start = new Vector3f(AB[0], AB[1], AB[2]);//起点
        Vector3f end = new Vector3f(AB[3], AB[4], AB[5]);//终点
        Vector3f dir = end.minus(start);//长度和方向
            *//*
             * 计算AB线段与每个物体包围盒的最佳交点(与A点最近的交点)，
			 * 并记录有最佳交点的物体在列表中的索引值
			 *//*
        //记录列表中时间最小的索引值
        int tmpIndex = -1;//记录与A点最近物体索引的临时值
        float minTime = 1;//记录列表中所有物体与AB相交的最短时间


        for (int i = 0; i < mBaseBalls.size(); i++) {//遍历列表中的物体
            AABB3 box = mBaseBalls.get(i).getCurrBox(); //获得物体AABB包围盒
            float t = box.rayIntersect(start, dir, null);//计算相交时间
            if (t <= minTime) {
                minTime = t;//记录最小值

                tmpIndex = i;//记录最小值索引
            }
        }
        if (tmpIndex != -1) {
            if (onPickupId == tmpIndex) {

            } else {
                onPickupId = tmpIndex;
                startTime = System.currentTimeMillis();
            }
        } else {
            startTime = System.currentTimeMillis();
        }
    }*/

    //改变列表中下标为index的物体
  /*  public void changeObj() {
        long t = System.currentTimeMillis() - startTime;
        if (t > animationtimes) {
            for (int i = 0; i < mBaseBalls.size(); i++) {
                if (i == onPickupId) {//改变选中的物体
                    mBaseBalls.get(i).reStartMove();
                }
            }
        }
    }*/
}
