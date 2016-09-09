package com.example.a111.game.model.column;

import android.os.Handler;
import android.os.Message;

import com.example.a111.game.R;
import com.example.a111.game.model.ModelEvent;
import com.example.a111.game.model.ModelListener;
import com.example.a111.game.model.ball.BaseBall;
import com.example.a111.game.util.BitmapUtil;
import com.example.a111.game.view.BaseGLSurfaceView;

import java.util.ArrayList;


public class BallColumn {

    int mBallTextureId;
    public boolean show =false;

    //球
    ArrayList<BaseBall> mBaseBalls = new ArrayList<>();
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


    public BallColumn(BaseGLSurfaceView mv, ModelListener listener) {

        initBall(listener, mv);
    }

    void initBall(ModelListener listener, BaseGLSurfaceView mv) {
        mBallTextureId = BitmapUtil.initTexture(mv, R.drawable.aaa);
        mBall = new BaseBall(2, 1.6f, 15, 0, 0);
        mBall1 = new BaseBall(2, 1.6f, 15, 100, 1);
        mBall2 = new BaseBall(2, 1.6f, 15, 200, 2);
        mBall3 = new BaseBall(2, 1.6f, 15, 300, 3);
        mBall4 = new BaseBall(2, 1.6f, 15, 400, 4);
        mBall5 = new BaseBall(2, 1.6f, 15, 500, 5);
        mBall6 = new BaseBall(2, 1.6f, 15, 600, 6);
        mBall7 = new BaseBall(2, 1.6f, 15, 700, 7);
        mBall8 = new BaseBall(2, 1.6f, 15, 800, 8);
        mBall9 = new BaseBall(2, 1.6f, 15, 900, 9);
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

        for (BaseBall ball :
                mBaseBalls) {
            ball.addListener(listener);
        }
    }

    public void drawBall(float[] mHeadView, Handler handler) {
        if (show){
        for (BaseBall ball : mBaseBalls) {
            ball.setCamera(mHeadView);
            ball.drawSelf(mBallTextureId);
            if (ball.collision) {
                Message message = new Message();
                message.what = 1111;
                handler.sendMessage(message);
                ball.reStartMove();
            }
        }}
    }

    public void setProjectFrustum(float mLeft, float mRight, float mBottom, float mTop, float mNear, float mFar) {
        for (BaseBall ball : mBaseBalls) {
            ball.setProjectFrustum(mLeft, mRight, mBottom, mTop, mNear, mFar);
        }
    }


    public void onClick(int id) {
        for (BaseBall ball : mBaseBalls) {
            if (ball.id == id) {
                ball.reStartMove();
            }
        }

    }
}
