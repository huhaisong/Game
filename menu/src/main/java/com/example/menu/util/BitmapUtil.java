package com.example.menu.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

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
}
