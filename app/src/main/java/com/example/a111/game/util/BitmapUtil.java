package com.example.a111.game.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.example.a111.game.view.BaseGLSurfaceView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by 111 on 2016/8/30.
 */
public class BitmapUtil {

    public static Bitmap getBGImage(int length, int width, int a, int r, int g, int b) {
        Bitmap bitmap = Bitmap.createBitmap(length, width, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setTypeface(Typeface.SERIF);
        canvas.drawARGB(a, r, g, b);
        return bitmap;
    }

    public static Bitmap getFontImage(Bitmap srcBitmap, String str) {

        Bitmap bitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAlpha(0xff);
        canvas.drawBitmap(srcBitmap,0,0,paint);
        paint.setTypeface(Typeface.SERIF);
        paint.setTextSize(130);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.BLACK);
        canvas.drawText(str, srcBitmap.getWidth()/2, srcBitmap.getHeight()/2, paint);
        return bitmap;
    }

    public static int initTexture(BaseGLSurfaceView mv,int drawableId)
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
        InputStream is = mv.getResources().openRawResource(drawableId);
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

    public static int initTexture(BaseGLSurfaceView mv, int drawableId, String content)
    {
        //生成纹理ID
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        //通过输入流加载图片===============begin===================
        InputStream is = mv.getResources().openRawResource(drawableId);
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


        bitmapTmp = BitmapUtil.getFontImage(bitmapTmp, content);
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

    public static int initTexture()
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
        Bitmap bitmapTmp;
     /*   InputStream is = this.getResources().openRawResource(drawableId);
        try {
            bitmapTmp = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        //通过输入流加载图片===============end=====================

        bitmapTmp = BitmapUtil.getBGImage(100, 100, 100, 0, 0, 0);
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
}
