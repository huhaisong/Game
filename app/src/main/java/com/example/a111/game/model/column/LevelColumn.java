package com.example.a111.game.model.column;

import com.example.a111.game.R;
import com.example.a111.game.model.ModelListener;
import com.example.a111.game.model.sector.BaseSector;
import com.example.a111.game.util.BitmapUtil;
import com.example.a111.game.video.video2d.Constants;
import com.example.a111.game.view.BaseGLSurfaceView;

import java.util.ArrayList;

/**
 * Created by 111 on 2016/9/9.
 */
public class LevelColumn {

    public boolean show = false;

    ArrayList<BaseSector> mGameLevels = new ArrayList<>();
    BaseSector mGameLevel0;
    BaseSector mGameLevel1;
    BaseSector mGameLevel2;
    BaseSector mGameLevel3;
    int mGameLevel0TextureId;
    int mGameLevel1TextureId;
    int mGameLevel2TextureId;
    int mGameLevel3TextureId;

    private BaseGLSurfaceView mGLSurfaceView;


    public LevelColumn(BaseGLSurfaceView mv, ModelListener listener) {

        this.mGLSurfaceView = mv;
        initGameLevel(mv, listener);
    }

    void initGameLevel(BaseGLSurfaceView mv, ModelListener listener) {
        int left = 2150;
        int top = 800;
        int widthSpan = 0;
        int heightSpan = 90;
        int width = 220;
        int height = 80;
        mGameLevel0TextureId = BitmapUtil.initTexture(mv, R.drawable.aa, "关卡0");
        mGameLevel1TextureId = BitmapUtil.initTexture(mv, R.drawable.aa, "关卡1");
        mGameLevel2TextureId = BitmapUtil.initTexture(mv, R.drawable.aa, "关卡2");
        mGameLevel3TextureId = BitmapUtil.initTexture(mv, R.drawable.aa, "关卡3");
        mGameLevel0 = new BaseSector(left, top, width, height, 10, 1, 5.0f, 21);
        mGameLevel1 = new BaseSector(left + widthSpan, top + heightSpan, width, height, 10, 1, 5.0f, 22);
        mGameLevel2 = new BaseSector(left + widthSpan * 2, top + heightSpan * 2, width, height, 10, 1, 5.0f, 23);
        mGameLevel3 = new BaseSector(left + widthSpan * 3, top + heightSpan * 3, width, height, 10, 1, 5.0f, 24);
        mGameLevels.add(mGameLevel0);
        mGameLevels.add(mGameLevel1);
        mGameLevels.add(mGameLevel2);
        mGameLevels.add(mGameLevel3);

        for (BaseSector gameLevel : mGameLevels) {
            gameLevel.addListener(listener);
        }
    }

    public void drawGameLevel(float[] mHeadView) {
        if (show) {
            for (BaseSector gameLevel : mGameLevels) {
                gameLevel.setCamera(mHeadView);
            }
            mGameLevel0.draw(mGameLevel0TextureId);
            mGameLevel1.draw(mGameLevel1TextureId);
            mGameLevel2.draw(mGameLevel2TextureId);
            mGameLevel3.draw(mGameLevel3TextureId);
        }
    }

    public void setProjectFrustum(float mLeft, float mRight, float mBottom, float mTop, float mNear, float mFar) {

        for (BaseSector gameLevel : mGameLevels) {
            gameLevel.setProjectFrustum(mLeft, mRight, mBottom, mTop, mNear, mFar);
        }
    }

    public void onClick(int id, MenuColumn menuColumn, BallColumn ballColumn) {
        if (mGameLevel0.id == id) {
            setBoolean(menuColumn, ballColumn);
            menuColumn.mSelectLevelTextureId = BitmapUtil.initTexture(mGLSurfaceView, R.drawable.aa, "第0关");
        }

        if (mGameLevel1.id == id) {
            setBoolean(menuColumn, ballColumn);
            menuColumn.mSelectLevelTextureId = BitmapUtil.initTexture(mGLSurfaceView, R.drawable.aa, "第1关");

        }
        if (mGameLevel2.id == id) {
            setBoolean(menuColumn, ballColumn);
            menuColumn.mSelectLevelTextureId = BitmapUtil.initTexture(mGLSurfaceView, R.drawable.aa, "第2关");
        }
        if (mGameLevel3.id == id) {
            setBoolean(menuColumn, ballColumn);
            menuColumn.mSelectLevelTextureId = BitmapUtil.initTexture(mGLSurfaceView, R.drawable.aa, "第3关");
        }
    }

    private void setBoolean(MenuColumn menuColumn, BallColumn ballColumn) {
        menuColumn.show = true;
        ballColumn.show = false;
        this.show = false;
    }


}
