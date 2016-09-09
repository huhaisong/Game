package com.example.a111.game.model.column;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.a111.game.R;
import com.example.a111.game.model.ModelListener;
import com.example.a111.game.model.sector.BaseSector;
import com.example.a111.game.util.BitmapUtil;
import com.example.a111.game.video.video2d.VR2DVideoActivity;
import com.example.a111.game.video.video360.VRVideo360Activity;
import com.example.a111.game.view.BaseGLSurfaceView;

import java.util.ArrayList;

/**
 * Created by 111 on 2016/9/9.
 */
public class MenuColumn {

    public boolean show = true;

    //菜单
    ArrayList<BaseSector> mMenus = new ArrayList<>();
    BaseSector mSectorBG;
    BaseSector mStartMenu;
    BaseSector mSelectLevelMenu;
    BaseSector mSetMenu;
    BaseSector mTeamInformationMenu;
    //菜单
    int mMenuBGTextureId;
    int mStartTextureId;
    public int mSelectLevelTextureId;
    int mSetTextureId;
    int mTeamInformationTextureId;

    public MenuColumn(BaseGLSurfaceView mv, ModelListener listener) {

        initMenu(mv, listener);
    }

    void initMenu(BaseGLSurfaceView mv, ModelListener listener) {
        int left = 1890;
        int top = 800;
        int widthSpan = 0;
        int heightSpan = 90;
        int width = 220;
        int height = 80;
        mMenuBGTextureId = BitmapUtil.initTexture();
        mStartTextureId = BitmapUtil.initTexture(mv, R.drawable.aa, "开始游戏");
        mSelectLevelTextureId = BitmapUtil.initTexture(mv, R.drawable.aa, "选择关卡");
        mSetTextureId = BitmapUtil.initTexture(mv, R.drawable.aa, "3d视频");
        mTeamInformationTextureId = BitmapUtil.initTexture(mv, R.drawable.aa, "全景视频");
        mSectorBG = new BaseSector(left - 20, top - 20, width + 40, heightSpan * 4 + 40, 10, 1, 5.6f, 10);

        mStartMenu = new BaseSector(left, top, width, height, 10, 1, 5.0f, 11);
        mSelectLevelMenu = new BaseSector(left + widthSpan, top + heightSpan, width, height, 10, 1, 5.0f, 12);
        mSetMenu = new BaseSector(left + widthSpan * 2, top + heightSpan * 2, width, height, 10, 1, 5.0f, 13);
        mTeamInformationMenu = new BaseSector(left + widthSpan * 3, top + heightSpan * 3, width, height, 10, 1, 5.0f, 14);
        mMenus.add(mStartMenu);
        mMenus.add(mSelectLevelMenu);
        mMenus.add(mSetMenu);
        mMenus.add(mTeamInformationMenu);


        for (BaseSector menu :
                mMenus) {
            menu.addListener(listener);
        }
    }

    public void setProjectFrustum(float mLeft, float mRight, float mBottom, float mTop, float mNear, float mFar) {

        for (BaseSector menu : mMenus) {
            menu.setProjectFrustum(mLeft, mRight, mBottom, mTop, mNear, mFar);
        }
        mSectorBG.setProjectFrustum(mLeft, mRight, mBottom, mTop, mNear, mFar);
    }


    public void drawMenu(float[] mHeadView) {
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


    public void onClick(int id, Context mContext, BallColumn ballColumn,LevelColumn levelColumn) {
        if (mStartMenu.id == id) {
            this.show = false;
            ballColumn.show = true;
            levelColumn.show= false;
        }
        if (mSelectLevelMenu.id == id) {
            this.show = true;
            ballColumn.show = false;
            levelColumn.show = true;
        }
        if (mSetMenu.id == id) {
            Intent intent = new Intent(mContext, VR2DVideoActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("path", null);
            intent.putExtra("content", bundle);
            mContext.startActivity(intent);
        }
        if (mTeamInformationMenu.id == id) {
            Intent intent = new Intent(mContext, VRVideo360Activity.class);
            Bundle bundle = new Bundle();
            bundle.putString("path", null);
            intent.putExtra("content", bundle);
            mContext.startActivity(intent);
        }
    }
}
