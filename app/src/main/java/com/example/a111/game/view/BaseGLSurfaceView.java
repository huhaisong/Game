package com.example.a111.game.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.AttributeSet;

import com.example.a111.game.util.BitmapUtil;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.sensors.HeadTracker;

import java.io.IOException;
import java.io.InputStream;

public class BaseGLSurfaceView extends GLSurfaceView {

    protected HeadTracker mHeadTracker;
    protected float[] mHeadView = new float[16];
    protected HeadTransform mHeadTransform;

    public BaseGLSurfaceView(Context context) {
        super(context);
        TInit(context);
    }

    public BaseGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TInit(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHeadTracker.startTracking();
    }

    @Override
    public void onPause() {
        super.onPause();
        mHeadTracker.stopTracking();
    }

    protected void TInit(Context context) {
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        mHeadTracker = HeadTracker.createFromContext(context);
        Matrix.setIdentityM(mHeadView, 0);
        mHeadTransform = new HeadTransform();
    }
}



