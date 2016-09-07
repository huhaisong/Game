package com.example.a111.game.video.video2d;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.view.Surface;

import com.example.a111.game.util.ShaderUtil;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.sensors.HeadTracker;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.Matrix.perspectiveM;

public class VideoView extends GLSurfaceView {
    private static HeadTransform mHeadTransform;
    private static HeadTracker mHeadTracker;
    private MyRenderer mRenderer;
   // private float[] mHeadView = new float[16];

    //private String pathString = "/storage/sdcard1/VRResources/1.mp4";
    private String pathString = "/storage/emulated/0/tencent/MicroMsg/vproxy/y0321fj6iu2.100701.mp4";
    private SurfaceTexture mSurface;
    private MediaPlayer mediaPlayer;

    private Context mContext;

    public VideoView(Context context) {
        super(context);
        init(context);
    }

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.setEGLContextClientVersion(2); //设置使用OPENGL ES2.0
        mRenderer = new MyRenderer();       //创建场景渲染器
        setRenderer(mRenderer);             //设置渲染器
        mHeadTracker = HeadTracker.createFromContext(context);
        mHeadTransform = new HeadTransform();
       // Matrix.setIdentityM(mHeadView, 0);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
        mContext = context;
    }

    public void setPath(String path) {

        pathString = path;
    }

    public void onPause() {
        super.onPause();
        mHeadTracker.stopTracking();

        try {
            if (null != mediaPlayer && mediaPlayer.isPlaying()) {
                Constants.playPosition = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public void onResume() {
        super.onResume();
        mHeadTracker.startTracking();

        if (Constants.playPosition >= 0) {

            if (null != mediaPlayer) {
                mediaPlayer.seekTo(Constants.playPosition);
                mediaPlayer.start();
            } else {
                mRenderer.openVideo();
            }
        }
    }

    public void onStop() {
        mRenderer.stopPlayback();
    }


    public class MyRenderer implements Renderer, MediaPlayer.OnCompletionListener,
            MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {


        Bitmap add;
        Bitmap subtract;
        private VRBaseView mVideoView;
        private VRBaseView mAddVolume, mSubtractVolume;

        private int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
        private int mWidth, mHeight;
        private String mVertexShader, mFragmentShader;
        private int maPositionHandle, maPositionHandle1, maTexCoorHandle, maTexCoorHandle1;
        private int mProgram, mProgram1;
        private int mMVPMatrixHandle, mMVPMatrixHandle1;

        float[] projectionMatrix = new float[16];   //投影矩阵
        float[] modelViewMatrix = new float[16];    //变换矩阵
        float[] mVMatrix = new float[16];           //摄像机矩阵
        final float[] temp = new float[16];         //总矩阵

        private int mTextureID;

        public MyRenderer() {
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            mTextureID = textures[0];
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);

            GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            ShaderUtil.checkGlError("glTexParameteri mTextureID");

            mSurface = new SurfaceTexture(mTextureID);
        }

        public void openVideo() {

            stopPlayback();
            // 初始化MediaPlayer
            mediaPlayer = new MediaPlayer();
            // 重置mediaPaly,建议在初始滑mediaplay立即调用。
            mediaPlayer.reset();
            // 设置播放完成监听
            mediaPlayer.setOnCompletionListener(this);
            // 设置媒体加载完成以后回调函数。
            mediaPlayer.setOnPreparedListener(this);
            // 错误监听回调函数
            mediaPlayer.setOnErrorListener(this);
            // 设置缓存变化监听
            try {
                mediaPlayer.setDataSource(pathString);
                Surface surface = new Surface(mSurface);

                mediaPlayer.setSurface(surface);
                //surface.release();
                // 设置声音效果
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setScreenOnWhilePlaying(true);
                // mediaPlayer.setDataSource(this, uri);
                // mediaPlayer.setDataSource(SurfaceViewTestActivity.this, uri);
                // 设置异步加载视频，包括两种方式 prepare()同步，prepareAsync()异步
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void stopPlayback() {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }

//        private Listener mListener;
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            //设置屏幕背景色RGBA
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            mVertexShader = ShaderUtil.loadFromAssetsFile("vertex1.sh", getResources());
            mFragmentShader = ShaderUtil.loadFromAssetsFile("frag1.sh", getResources());
            mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
            maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
            maTexCoorHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoor");
            mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

            mVertexShader = ShaderUtil.loadFromAssetsFile("vertex.sh", getResources());
            mFragmentShader = ShaderUtil.loadFromAssetsFile("frag.sh", getResources());
            mProgram1 = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
            maPositionHandle1 = GLES20.glGetAttribLocation(mProgram1, "aPosition");
            maTexCoorHandle1 = GLES20.glGetAttribLocation(mProgram1, "aTexCoor");
            mMVPMatrixHandle1 = GLES20.glGetUniformLocation(mProgram1, "uMVPMatrix");

            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDisable(GLES20.GL_CULL_FACE);

            Matrix.setLookAtM(mVMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
            openVideo();

            mVideoView = new VRBaseView(this, 1600, 700, 800, 600, 10, 1, 3.0f, mContext, 1);
            mAddVolume = new VRBaseView(this, 1540, 1150, 60, 60, 1, 1, 3.0f, mContext, 2);
            mSubtractVolume = new VRBaseView(this, 1530, 1250, 60, 60, 1, 1, 3.0f, mContext, 3);
         /*   mListener = new Listener();*/
/*
            mVideoView.addListener(mListener);
            mAddVolume.addListener(mListener);
            mSubtractVolume.addListener(mListener);*/

            add = Base.loadImageAssets(mContext, "add.png");
            subtract = Base.loadImageAssets(mContext, "subtract.png");

            mAddVolume.setBmp(add, add);
            mSubtractVolume.setBmp(subtract, subtract);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mWidth = width;
            mHeight = height;
        }

        @Override
        public void onDrawFrame(GL10 gl) {

            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            GLES20.glEnable(GLES20.GL_BLEND);

            mSurface.updateTexImage();

            update();
            //清除深度缓冲与颜色缓冲
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

            float[] EulerAngles = new float[3];
            mHeadTransform.getEulerAngles(EulerAngles, 0);

            GLES20.glViewport(0, 0, mWidth / 2, mHeight);
            GLES20.glUseProgram(mProgram1);
            mAddVolume.Draw(maPositionHandle1, maTexCoorHandle1, EulerAngles, temp, mMVPMatrixHandle1);
            mSubtractVolume.Draw(maPositionHandle1, maTexCoorHandle1, EulerAngles, temp, mMVPMatrixHandle1);

            GLES20.glUseProgram(mProgram);
            mVideoView.DrawRight(maPositionHandle, maTexCoorHandle, EulerAngles, temp, mMVPMatrixHandle, mTextureID);

            GLES20.glViewport(mWidth / 2, 0, mWidth / 2, mHeight);
            GLES20.glUseProgram(mProgram);
            mVideoView.DrawLeft(maPositionHandle, maTexCoorHandle, EulerAngles, temp, mMVPMatrixHandle, mTextureID);
            GLES20.glUseProgram(mProgram1);
            mAddVolume.Draw(maPositionHandle1, maTexCoorHandle1, EulerAngles, temp, mMVPMatrixHandle1);
            mSubtractVolume.Draw(maPositionHandle1, maTexCoorHandle1, EulerAngles, temp, mMVPMatrixHandle1);
        }

        public void update() {
            mHeadTracker.getLastHeadView(mHeadTransform.getHeadView(), 0);
            perspectiveM(projectionMatrix, 0, 75.0f, mWidth / mHeight / 2.0f, 0.1f, 400.0f);
            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.multiplyMM(temp, 0, projectionMatrix, 0, mHeadTransform.getHeadView(), 0);
            Matrix.multiplyMM(temp, 0, temp, 0, mVMatrix, 0);
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            // 判断是否有保存的播放位置,防止屏幕旋转时，界面被重新构建，播放位置丢失。
            if (Constants.playPosition >= 0) {
                mediaPlayer.seekTo(Constants.playPosition);
                Constants.playPosition = -1;
            }
            mediaPlayer.start();
        }
    }


   /* private class Listener implements VRWindowListener<VRBaseView> {

        @Override
        public void handleEvent(VRBaseView event) {
            switch (event.id) {
                case 1:
                    Toast.makeText(mContext, "我的ID为1", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(mContext, "我的ID为2", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(mContext, "我的ID为3", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }*/
}
