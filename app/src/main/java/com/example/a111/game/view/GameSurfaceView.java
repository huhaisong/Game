package com.example.a111.game.view;

import java.util.ArrayList;

import android.opengl.GLSurfaceView;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.Log;

import com.example.a111.game.model.BaseBall;
import com.example.a111.game.R;
import com.example.a111.game.model.BaseCircle;
import com.example.a111.game.model.BaseSector;
import com.example.a111.game.model.SphereBG;
import com.example.a111.game.util.IntersectantUtil;

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

    private class SceneRenderer implements GLSurfaceView.Renderer {

        private boolean showMenu = true;
        private boolean showBall = false;
        private boolean showGameLevel = false;

        //可触控物体列表
        ArrayList<BaseBall> mBaseBalls = new ArrayList<>();
        //被选中物体的索引值，即id，没有被选中时索引值为-1
        int onPickupId = -1;
        long animationtimes = 1000;

        //背景
        private SphereBG mSphereBG;

        //球
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

        //圆
        private BaseCircle mResetCircle;
        float[] cameraMatrix = new float[16];

        //菜单
        ArrayList<BaseSector> mMenus = new ArrayList<>();
        BaseSector mSectorBG;
        BaseSector mStartMenu;
        BaseSector mSelectLevelMenu;
        BaseSector mSetMenu;
        BaseSector mTeamInformationMenu;

        //关卡
        ArrayList<BaseSector> mGameLevels = new ArrayList<>();
        BaseSector mGameLevel0;
        BaseSector mGameLevel1;
        BaseSector mGameLevel2;
        BaseSector mGameLevel3;

        //纹理
        int mSphereBGTextureID;
        //球
        int mBallTextureId;
        //菜单
        int mMenuBGTextureId;
        int mStartTextureId;
        int mSelectLevelTextureId;
        int mSetTextureId;
        int mTeamInformationTextureId;
        //关卡
        int mGameLevel0TextureId;
        int mGameLevel1TextureId;
        int mGameLevel2TextureId;
        int mGameLevel3TextureId;

        long startTime;

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDisable(GLES20.GL_CULL_FACE);
            //背景
            mSphereBGTextureID = initTexture(R.drawable.bg);
            mSphereBG = new SphereBG();
            //复位按钮
            initCircle();
            //菜单
            initMenu();
            //球
            initBall();
            //关卡
            initGameLevel();
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mWidth = width;
            mHeight = height;
            //计算GLSurfaceView的宽高比
            float ratio = (float) width / height / 2;
            mLeft = mRight = ratio;
            mTop = mBottom = 1;
            mNear = 2;
            mFar = 500;
            //设置投影矩阵
            for (BaseBall ball : mBaseBalls) {
                ball.setProjectFrustum(-mLeft, mRight, -mBottom, mTop, mNear, mFar);
            }
            for (BaseSector menu : mMenus) {
                menu.setProjectFrustum(-mLeft, mRight, -mBottom, mTop, mNear, mFar);
            }
            for (BaseSector gameLevel : mGameLevels) {
                gameLevel.setProjectFrustum(-mLeft, mRight, -mBottom, mTop, mNear, mFar);
            }
            mSectorBG.setProjectFrustum(-mLeft, mRight, -mBottom, mTop, mNear, mFar);
            mSphereBG.setProjectFrustum(-mLeft, mRight, -mBottom, mTop, mNear, mFar);
            mResetCircle.setProjectFrustum(-mLeft, mRight, -mBottom, mTop, mNear, mFar);
        }

        public void onDrawFrame(GL10 gl) {

            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

            onPicked();

            mHeadTracker.getLastHeadView(mHeadView, 0);
            //左边
            GLES20.glViewport(0, 0, mWidth / 2, mHeight);
            draw();

            //右边
            GLES20.glViewport(mWidth / 2, 0, mWidth / 2, mHeight);
            draw();
        }

        void initCircle() {
            mResetCircle = new BaseCircle(1.0f, 2.0f, 36, 100);
            Matrix.setLookAtM(cameraMatrix, 0, 0, 0, 3f, 0, 0, 0f, 0f, 1.0f, 0.0f);
            mResetCircle.setCamera(cameraMatrix);
            mResetCircle.translate(0, -20, -50);
        }

        void initMenu() {
            int left = 1950;
            int top = 900;
            int widthSpan = 0;
            int heightSpan = 45;
            int width = 100;
            int height = 40;
            mMenuBGTextureId = initTexture();
            mStartTextureId = initTexture(R.drawable.aa, "开始游戏");
            mSelectLevelTextureId = initTexture(R.drawable.aa, "选择关卡");
            mSetTextureId = initTexture(R.drawable.aa, "设置选项");
            mTeamInformationTextureId = initTexture(R.drawable.aa, "制作团队");
            mSectorBG = new BaseSector(left - 10, top - 20, 120, 220, 10, 1, 3.6f, 10);

            mStartMenu = new BaseSector(left, top, width, height, 10, 1, 3.0f, 11);
            mSelectLevelMenu = new BaseSector(left + widthSpan, top + heightSpan, width, height, 10, 1, 3.0f, 12);
            mSetMenu = new BaseSector(left + widthSpan * 2, top + heightSpan * 2, width, height, 10, 1, 3.0f, 13);
            mTeamInformationMenu = new BaseSector(left + widthSpan * 3, top + heightSpan * 3, width, height, 10, 1, 3.0f, 14);
            mMenus.add(mStartMenu);
            mMenus.add(mSelectLevelMenu);
            mMenus.add(mSetMenu);
            mMenus.add(mTeamInformationMenu);
        }

        void initGameLevel() {
            mGameLevel0TextureId = initTexture(R.drawable.aa, "关卡0");
            mGameLevel1TextureId = initTexture(R.drawable.aa, "关卡1");
            mGameLevel2TextureId = initTexture(R.drawable.aa, "关卡2");
            mGameLevel3TextureId = initTexture(R.drawable.aa, "关卡3");
            mGameLevel0 = new BaseSector(1950, 900, 100, 40, 10, 1, 3.0f, 21);
            mGameLevel1 = new BaseSector(1950, 945, 100, 40, 10, 1, 3.0f, 22);
            mGameLevel2 = new BaseSector(1950, 990, 100, 40, 10, 1, 3.0f, 23);
            mGameLevel3 = new BaseSector(1950, 1035, 100, 40, 10, 1, 3.0f, 24);
            mGameLevels.add(mGameLevel0);
            mGameLevels.add(mGameLevel1);
            mGameLevels.add(mGameLevel2);
            mGameLevels.add(mGameLevel3);
        }

        void initBall() {
            mBallTextureId = initTexture(R.drawable.aaa);
            mBall = new BaseBall(GameSurfaceView.this, 2, 1.6f, 15, 0, 0);
            mBall1 = new BaseBall(GameSurfaceView.this, 2, 1.6f, 15, 100, 1);
            mBall2 = new BaseBall(GameSurfaceView.this, 2, 1.6f, 15, 200, 2);
            mBall3 = new BaseBall(GameSurfaceView.this, 2, 1.6f, 15, 300, 3);
            mBall4 = new BaseBall(GameSurfaceView.this, 2, 1.6f, 15, 400, 4);
            mBall5 = new BaseBall(GameSurfaceView.this, 2, 1.6f, 15, 500, 5);
            mBall6 = new BaseBall(GameSurfaceView.this, 2, 1.6f, 15, 600, 6);
            mBall7 = new BaseBall(GameSurfaceView.this, 2, 1.6f, 15, 700, 7);
            mBall8 = new BaseBall(GameSurfaceView.this, 2, 1.6f, 15, 800, 8);
            mBall9 = new BaseBall(GameSurfaceView.this, 2, 1.6f, 15, 900, 9);
            mBaseBalls.add(mBall);
            mBaseBalls.add(mBall1);
            mBaseBalls.add(mBall2);
            mBaseBalls.add(mBall3);
            mBaseBalls.add(mBall4);
            mBaseBalls.add(mBall5);
            mBaseBalls.add(mBall6);
            mBaseBalls.add(mBall7);
            mBaseBalls.add(mBall8);
            mBaseBalls.add(mBall9);
        }

        void draw() {

            drawResetCircle();
            mSphereBG.drawSelf(mHeadView, mSphereBGTextureID);
            if (showBall) {
                drawBall();
            }
            if (showMenu) {
                drawMenu();
            }
            if (showGameLevel) {
                drawGameLevel();
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
            mResetCircle.translateByHeadView(0, move_v / 6, 0);
            mResetCircle.drawSelf(mSphereBGTextureID);
            mResetCircle.popMatrix();
        }

        void drawMenu() {
            mSectorBG.setCamera(mHeadView);
            for (BaseSector menu : mMenus) {
                menu.setCamera(mHeadView);
            }
            mSectorBG.draw(mMenuBGTextureId);
            mStartMenu.draw(mStartTextureId);
            mSelectLevelMenu.draw(mSelectLevelTextureId);
            mSetMenu.draw(mSetTextureId);
            mTeamInformationMenu.draw(mTeamInformationTextureId);
        }

        void drawGameLevel() {
            for (BaseSector gameLevel : mGameLevels) {
                gameLevel.setCamera(mHeadView);
            }
            mGameLevel0.draw(mGameLevel0TextureId);
            mGameLevel1.draw(mGameLevel1TextureId);
            mGameLevel2.draw(mGameLevel2TextureId);
            mGameLevel3.draw(mGameLevel3TextureId);
        }

        void drawBall() {
            for (BaseBall ball : mBaseBalls) {
                ball.setCamera(mHeadView);
                ball.drawSelf(mBallTextureId);
                if (ball.collision) {
                    Message message = new Message();
                    message.what = 1111;
                    mHandler.sendMessage(message);
                    ball.reStartMove();
                }
            }
        }

        void onPicked() {
            //计算出AB射线
            float[] AB = IntersectantUtil.calculateABPosition(mWidth / 2, mHeight / 2,
                    mWidth, mHeight, mLeft, mTop, mNear, mFar, mHeadView);

            int tempId = -1;
            if (showBall) {
                for (BaseBall ball : mBaseBalls) {

                    if (ball.isPickup(AB)) {
                        tempId = ball.id;
                        break;
                    }
                }
            }
            if (showMenu) {
                for (BaseSector menu : mMenus) {
                    if (menu.isPickup(AB)) {
                        tempId = menu.id;
                    }
                }
            }
            if (showGameLevel) {
                for (BaseSector gameLevel : mGameLevels) {
                    if (gameLevel.isPickup(AB)) {
                        tempId = gameLevel.id;
                    }
                }
            }

            //计算出AB射线
            AB = IntersectantUtil.calculateABPosition(mWidth / 2, mHeight / 2,
                    mWidth, mHeight, mLeft, mTop, mNear, mFar, cameraMatrix);
            if (mResetCircle.isPickup(AB)) {
                tempId = mResetCircle.id;
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

            if (t > animationtimes) {
                for (BaseBall ball : mBaseBalls) {
                    if (ball.id == onPickupId) {
                        ball.reStartMove();
                    }
                }
                if (mStartMenu.id == onPickupId) {
                    showMenu = false;
                    showBall = true;
                    showGameLevel = false;
                }
                if (mSelectLevelMenu.id == onPickupId) {
                    showMenu = false;
                    showBall = false;
                    showGameLevel = true;
                }
                if (mGameLevel0.id == onPickupId) {
                    showMenu = false;
                    showBall = true;
                    showGameLevel = false;
                }
                if (mGameLevel1.id == onPickupId) {
                    showMenu = false;
                    showBall = true;
                    showGameLevel = false;
                }
                if (mGameLevel2.id == onPickupId) {
                    showMenu = false;
                    showBall = true;
                    showGameLevel = false;
                }
                if (mGameLevel3.id == onPickupId) {
                    showMenu = false;
                    showBall = true;
                    showGameLevel = false;
                }
                for (BaseSector menu : mMenus) {
                    if (menu.id == onPickupId) {
                        Log.i("aaaa", "onPicked: " + onPickupId + "is on picked");
                    }
                }
                if (mResetCircle.id == onPickupId) {
                    resetHeadView();
                    Log.i("aaa", "onPicked: reset");
                }
            }
        }
    }
}
