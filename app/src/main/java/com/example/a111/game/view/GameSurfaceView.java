package com.example.a111.game.view;

import java.io.File;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.Log;

import com.example.a111.game.model.ModelEvent;
import com.example.a111.game.model.ModelListener;
import com.example.a111.game.model.TouchableObject;
import com.example.a111.game.model.ball.BaseBall;
import com.example.a111.game.R;
import com.example.a111.game.model.circle.BaseCircle;
import com.example.a111.game.model.column.BallColumn;
import com.example.a111.game.model.column.LevelColumn;
import com.example.a111.game.model.column.MenuColumn;
import com.example.a111.game.model.sector.BaseSector;
import com.example.a111.game.model.SphereBG;
import com.example.a111.game.util.BitmapUtil;
import com.example.a111.game.util.IntersectantUtil;
import com.example.a111.game.video.MediaBean;
import com.example.a111.game.video.video2d.Constants;
import com.example.a111.game.video.video2d.VR2DVideoActivity;
import com.example.a111.game.video.video360.VRVideo360Activity;

public class GameSurfaceView extends BaseGLSurfaceView {

    private Handler mHandler;
    private Context mContext;

    private SceneRenderer mRenderer;//场景渲染器

    private int mWidth;
    private int mHeight;
    float mLeft;
    float mRight;
    float mTop;
    float mBottom;
    float mNear;
    float mFar;
    boolean startActivity = false;

    public GameSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        mRenderer = new SceneRenderer();    //创建场景渲染器
        setRenderer(mRenderer);                //设置渲染器
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//设置渲染模式为主动渲染
    }

    public void setHandler(Handler handler) {

        this.mHandler = handler;
    }

    private class SceneRenderer implements GLSurfaceView.Renderer, ModelListener {

        BallColumn mBallColumn;
        LevelColumn mLevelColumn;
        MenuColumn mMenuColumn;

        int onPickupId = -1;
        //背景
        private SphereBG mSphereBG;
        //圆
        private BaseCircle mResetCircle;
        float[] cameraMatrix = new float[16];

        //纹理
        private int mResetTextureId;
        int mSphereBGTextureID;

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            startActivity = true;
            getList();
            GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDisable(GLES20.GL_CULL_FACE);
            //背景
            mSphereBGTextureID = BitmapUtil.initTexture(GameSurfaceView.this, R.drawable.bg);
            mSphereBG = new SphereBG();
            //复位按钮
            initCircle();
            //菜单
            mMenuColumn = new MenuColumn(GameSurfaceView.this, this);
            //球
            mBallColumn = new BallColumn(GameSurfaceView.this, this);
            //关卡
            mLevelColumn = new LevelColumn(GameSurfaceView.this, this);
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mWidth = width;
            mHeight = height;
            //计算GLSurfaceView的宽高比
            float ratio = (float) width / height / 2;
            mLeft = mRight = ratio;
            mTop = mBottom = 1;
            mNear = 2;
            mFar = 1000;
            //设置投影矩阵
            mBallColumn.setProjectFrustum(-mLeft, mRight, -mBottom, mTop, mNear, mFar);
            mLevelColumn.setProjectFrustum(-mLeft, mRight, -mBottom, mTop, mNear, mFar);
            mMenuColumn.setProjectFrustum(-mLeft, mRight, -mBottom, mTop, mNear, mFar);

            mSphereBG.setProjectFrustum(-mLeft, mRight, -mBottom, mTop, mNear, mFar);
            mResetCircle.setProjectFrustum(-mLeft, mRight, -mBottom, mTop, mNear, mFar);
        }

        public void onDrawFrame(GL10 gl) {

            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

            mHeadTracker.getLastHeadView(mHeadView, 0);
            //左边
            GLES20.glViewport(0, 0, mWidth / 2, mHeight);
            draw();

            //右边
            GLES20.glViewport(mWidth / 2, 0, mWidth / 2, mHeight);
            draw();
        }

        void initCircle() {
            mResetTextureId = BitmapUtil.initTexture();
            mResetCircle = new BaseCircle(1.0f, 2.0f, 36, 100);
            Matrix.setLookAtM(cameraMatrix, 0, 0, 0, 3f, 0, 0, 0f, 0f, 1.0f, 0.0f);
            mResetCircle.setCamera(cameraMatrix);
            mResetCircle.translate(0, -20, -50);

            mResetCircle.addListener(this);
        }

        void draw() {

            drawResetCircle();
            mSphereBG.drawSelf(mHeadView, mSphereBGTextureID);
            if (mBallColumn.show) {
                mBallColumn.drawBall(mHeadView, mHandler);
            }
            if (mMenuColumn.show) {
                mMenuColumn.drawMenu(mHeadView);
            }
            if (mLevelColumn.show) {
                mLevelColumn.drawGameLevel(mHeadView);
            }
        }

        void drawResetCircle() {
            float[] EulerAngles = new float[3];
            mHeadTracker.getLastHeadView(mHeadTransform.getHeadView(), 0);
            mHeadTransform.getEulerAngles(EulerAngles, 0);
            float move_v = -(int) (EulerAngles[0] / Math.PI * 4 * 302);
            if (move_v >= 340)
                move_v = 340;
            mResetCircle.pushMatrix();
            //mResetCircle.translate(0,move_v/8,0);
            mResetCircle.translateByHeadView(0, move_v / 8, 0);
            mResetCircle.drawSelf(mResetTextureId);
            mResetCircle.popMatrix();
        }


        @Override
        public void onClick(ModelEvent event) {

            TouchableObject touchableObject = (TouchableObject) event;
            if (touchableObject.isPickedUp) {
                onPickupId = touchableObject.id;
                touchableObject.isPickedUp = false;
            }

            mBallColumn.onClick(onPickupId);

            mLevelColumn.onClick(onPickupId, mMenuColumn, mBallColumn);

            mMenuColumn.onClick(onPickupId, mContext, mBallColumn, mLevelColumn);

            if (mResetCircle.id == onPickupId) {
                mHeadTracker.resetTracker();
            }
        }
    }

    private void getList() {
        ArrayList<MediaBean> mPlayVideoList360 = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        ContentResolver mContentResolver = mContext.getContentResolver();
        Cursor mCursor = mContentResolver.query(uri, null, null, null, null);
        assert mCursor != null;
        mCursor.moveToFirst();
        int num = mCursor.getCount();
        if (num > 0) {
            do {
                String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                File f = new File(path);
                path = f.getPath();
                long id = mCursor.getLong(mCursor.getColumnIndex("_ID"));
                mPlayVideoList360.add(new MediaBean(path, id, true));
            } while (mCursor.moveToNext());
        }
        mCursor.close();

        Log.i("aaa", "getList: " + mPlayVideoList360.toString());
    }
}
