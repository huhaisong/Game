package com.example.a111.game.video.video360;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Message;
import android.view.Surface;

import com.artechnique.vrlauncher.Model.video.video2d.Constants;
import com.artechnique.vrlauncher.Model.video.video2d.ShaderUtil;
import com.google.vrtoolkit.cardboard.sensors.HeadTracker;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.Matrix.perspectiveM;


public class VRVideo360View extends GLSurfaceView {


    private HeadTracker mHeadTracker;
    private MyRenderer mRenderer;
    private float[] mHeadView = new float[16];
    private boolean isSended = false;

    private String pathString;
    private SurfaceTexture mSurface;
    /**
     * 播放视频
     */
    private MediaPlayer mediaPlayer;

    public VRVideo360View(Context context) {
        super(context);

        this.setEGLContextClientVersion(2); //设置使用OPENGL ES2.0
        mRenderer = new MyRenderer();       //创建场景渲染器
        setRenderer(mRenderer);             //设置渲染器
        mHeadTracker = HeadTracker.createFromContext(context);
        Matrix.setIdentityM(mHeadView, 0);
        setRenderMode(RENDERMODE_CONTINUOUSLY);

        //pathString = "/data/data/com.vstar3d.VRLauncher/1.mp4";//是这个路径吗 ？
    }

    public void setPath(String path ){
        pathString = path;
    }

    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                isSended = true;
                reStartTrack();
            }
        }
    };

    private void reStartTrack() {
        mHeadTracker.stopTracking();
        mHeadTracker.startTracking();
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

    class MyRenderer implements Renderer, MediaPlayer.OnCompletionListener,
            MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

        private int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

        private int mTextureID;

        MyRenderer() {
            // Create our texture. This has to be done each time the surface is created.
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            mTextureID = textures[0];
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);
            ShaderUtil.checkGlError("glBindTexture mTextureID");

            // Can't do mipmapping with mediaplayer source
            GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            // Clamp to edge is the only option
            GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            ShaderUtil.checkGlError("glTexParameteri mTextureID");

            mSurface = new SurfaceTexture(mTextureID);
        }
        /**
         * 播放视频
         */
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

        int numVertices = 0;
        int numIndices = 0;

        private FloatBuffer vertexBuffer, textureBuffer, texRighttureBuffer;
        private ShortBuffer IndicesBuffer;

        private int mWidth, mHeight;
        private String mVertexShader, mFragmentShader;
        private int maPositionHandle, maTexCoorHandle;
        private int mProgram;
        private int mMVPMatrixHandle;

        /**
         * @param numSlices 切分次数
         * @param d         半径
         */
        private int esGenSphere(int numSlices, float d) {
            int i;
            int j;
            int iidex = 0;
            int numParallels = numSlices / 2;
            numVertices = (numParallels + 1) * (numSlices + 1);
            numIndices = numParallels * numSlices * 6;
            float angleStep = (float) ((2.0f * Math.PI) / ((float) numSlices));
            float vertices[] = new float[numVertices * 3];
            float texCoords[] = new float[numVertices * 2];
            float texRightCoords[] = new float[numVertices * 2];

            short indices[] = new short[numIndices];
            for (i = 0; i < numParallels + 1; i++) {
                for (j = 0; j < numSlices + 1; j++) {
                    int vertex = (i * (numSlices + 1) + j) * 3;
                    vertices[vertex] = (float) (d * Math.sin(angleStep * (float) i) * Math.sin(angleStep * (float) j));
                    vertices[vertex + 1] = (float) (d * Math.cos(angleStep * (float) i));
                    vertices[vertex + 2] = (float) (d * Math.sin(angleStep * (float) i) * Math.cos(angleStep * (float) j));

                    int texIndex = (i * (numSlices + 1) + j) * 2;
                    texCoords[texIndex] = 1.0f - (float) j / (float) numSlices;
                    texCoords[texIndex + 1] = ((float) i / (float) numParallels);//((float)i/(float)numParallels);//

                    texRightCoords[texIndex] = 1.0f - (float) j / (float) numSlices;
                    texRightCoords[texIndex + 1] = ((float) i / (float) numParallels) / 2 + 0.5f;
                }
            }

            for (i = 0; i < numParallels; i++) {
                for (j = 0; j < numSlices; j++) {
                    indices[iidex++] = (short) (i * (numSlices + 1) + j);
                    indices[iidex++] = (short) ((i + 1) * (numSlices + 1) + j);
                    indices[iidex++] = (short) ((i + 1) * (numSlices + 1) + (j + 1));

                    indices[iidex++] = (short) (i * (numSlices + 1) + j);
                    indices[iidex++] = (short) ((i + 1) * (numSlices + 1) + (j + 1));
                    indices[iidex++] = (short) (i * (numSlices + 1) + (j + 1));
                }
            }

            texRighttureBuffer = MemUtil.makeFloatBuffer(texRightCoords);
            vertexBuffer = MemUtil.makeFloatBuffer(vertices);
            textureBuffer = MemUtil.makeFloatBuffer(texCoords);
            IndicesBuffer = MemUtil.makeShortBuffer(indices);
            return numIndices;
        }

        float[] projectionMatrix = new float[16];   //投影矩阵
        float[] modelViewMatrix = new float[16];    //变换矩阵
        float[] mVMatrix = new float[16];           //摄像机矩阵
        final float[] temp = new float[16];         //总矩阵

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            //设置屏幕背景色RGBA
            GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
            mVertexShader = ShaderUtil.loadFromAssetsFile("vertex1.sh", getResources());
            mFragmentShader = ShaderUtil.loadFromAssetsFile("frag1.sh", getResources());
            mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
            maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
            maTexCoorHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoor");
            mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

           // GLES20.glEnable(GLES20.GL_DEPTH_TEST);
           // GLES20.glDisable(GLES20.GL_CULL_FACE);

            //调用此方法产生摄像机9参数位置矩阵
            Matrix.setLookAtM(mVMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);



            openVideo();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mWidth = width;
            mHeight = height;
        }

        @Override
        public void onDrawFrame(GL10 gl) {

            mSurface.updateTexImage();

            update();
            int count = esGenSphere(100, 100);
            //清除深度缓冲与颜色缓冲
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glUseProgram(mProgram);
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, temp, 0);
            GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer);
            GLES20.glVertexAttribPointer(maTexCoorHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, textureBuffer);
            GLES20.glEnableVertexAttribArray(maPositionHandle);
            GLES20.glEnableVertexAttribArray(maTexCoorHandle);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);
            GLES20.glViewport(0, 0, mWidth / 2, mHeight);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, count, GLES20.GL_UNSIGNED_SHORT, IndicesBuffer);

            //设置视窗大小及位置
            GLES20.glViewport(mWidth / 2, 0, mWidth / 2, mHeight);
            GLES20.glUseProgram(mProgram);
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, temp, 0);
            GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer);
            GLES20.glVertexAttribPointer(maTexCoorHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, textureBuffer);
            GLES20.glEnableVertexAttribArray(maPositionHandle);
            GLES20.glEnableVertexAttribArray(maTexCoorHandle);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, count, GLES20.GL_UNSIGNED_SHORT, IndicesBuffer);
        }


        public void update() {
            mHeadTracker.getLastHeadView(mHeadView, 0);
            if (Float.isNaN(mHeadView[0])) {
                if (!isSended) {
                    myHandler.sendEmptyMessage(0);
                }
                return;
            }
            perspectiveM(projectionMatrix, 0, 75.0f, mWidth / mHeight / 2.0f, 0.1f, 400.0f);
            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.multiplyMM(temp, 0, projectionMatrix, 0, mHeadView, 0);
            Matrix.multiplyMM(temp, 0, temp, 0, mVMatrix, 0);
        }
    }

}
