package com.example.menu.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

import com.example.menu.R;
import com.example.menu.model.BaseSector;
import com.example.menu.util.IntersectantUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by 111 on 2016/8/30.
 */
public class MenuGLSurfaceView extends BaseGLSurfaceView {

    private Handler mHandler;
    private MyRenderer mRenderer;//场景渲染器
    int textureId;      //系统分配的纹理id

    private int mWidth;
    private int mHeight;
    float left;
    float right;
    float top;
    float bottom;
    float near;
    float far;

    int onPickupId;

    public MenuGLSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public MenuGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context) {

        mRenderer = new MyRenderer();    //创建场景渲染器
        setRenderer(mRenderer);                //设置渲染器
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//设置渲染模式为主动渲染
    }


    ArrayList<BaseSector> mMenus = new ArrayList<>();
    BaseSector mBaseSector;
    BaseSector mStartMenu;
    BaseSector mSelectLevelMenu;
    BaseSector mSetMenu;
    BaseSector mTeamInformationMenu;
    long startTime;

    class MyRenderer implements Renderer {

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDisable(GLES20.GL_CULL_FACE);
            textureId = initTexture(R.drawable.aa);

            mBaseSector = new BaseSector(1950, 800, 100, 40, 10, 1, 3.6f, 0);
            mStartMenu = new BaseSector(1950, 900, 100, 40, 10, 1, 3.0f, 1);
            mSelectLevelMenu = new BaseSector(1950, 945, 100, 40, 10, 1, 3.0f, 2);
            mSetMenu = new BaseSector(1950, 990, 100, 40, 10, 1, 3.0f, 3);
            mTeamInformationMenu = new BaseSector(1950, 1035, 100, 40, 10, 1, 3.0f, 4);
            mMenus.add(mStartMenu);
            mMenus.add(mSelectLevelMenu);
            mMenus.add(mSetMenu);
            mMenus.add(mTeamInformationMenu);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mWidth = width;
            mHeight = height;
            //计算GLSurfaceView的宽高比
            float ratio = (float) width / height / 2;
            left = right = ratio;
            top = bottom = 1;
            near = 2;
            far = 500;
            for (BaseSector menu : mMenus) {
                menu.setProjectFrustum(-left, right, -bottom, top, near, far);
            }
            mBaseSector.setProjectFrustum(-left, right, -bottom, top, near, far);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            mHeadTracker.getLastHeadView(mHeadView, 0);
            onPicked();
            GLES20.glViewport(0, 0, mWidth, mHeight);
            mBaseSector.setCamera(mHeadView);
            for (BaseSector menu : mMenus) {
                menu.setCamera(mHeadView);
            }
           // mBaseSector.draw(textureId);
            for (BaseSector menu : mMenus) {
                menu.draw(textureId);
            }
        }




        private void onPicked() {
            //计算出AB射线
            float[] AB = IntersectantUtil.calculateABPosition(mWidth / 2, mHeight / 2,
                    mWidth, mHeight, left, top, near, far, mHeadView);

            int tempId = -1;

            for (BaseSector menu : mMenus) {
                if (menu.isPickup(AB)) {
                    tempId = menu.id;
                }
            }
            if (tempId != -1) {
                if (onPickupId == tempId) {

                } else {
                    onPickupId = tempId;
                    startTime = System.currentTimeMillis();
                }
            } else {
                startTime = System.currentTimeMillis();
            }

            long t = System.currentTimeMillis() - startTime;
            if (t > 500) {
                switch (onPickupId) {
                    case 1:
                        Log.i("aaa", "onPicked: one is picked up!");
                        break;
                    case 2:
                        Log.i("aaa", "onPicked: two is picked up!");
                        break;
                    case 3:
                        Log.i("aaa", "onPicked: three is picked up!");
                        break;
                    case 4:
                        Log.i("aaa", "onPicked: four is picked up!");
                        break;
                }
            }
        }
    }
}
