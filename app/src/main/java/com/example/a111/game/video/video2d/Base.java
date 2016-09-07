package com.example.a111.game.video.video2d;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.IOException;

public class Base {
	public static final float MaxWidth=4000.0f;
	public static Bitmap loadImageAssets(Context context, String ImgName)
     {
     	Bitmap bmp=null;
     	try {
				BufferedInputStream bis = new BufferedInputStream(context.getAssets().open(ImgName));
				bmp = BitmapFactory.decodeStream(bis);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
     	return bmp;
     }
}
