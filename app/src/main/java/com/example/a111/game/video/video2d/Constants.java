package com.example.a111.game.video.video2d;

import android.graphics.Canvas;

/**
 * @author shenxiaolei
 *
 */
public class Constants {

    /**
     * 记录播放位置
     */
    public static int playPosition=-1;
    
    private static Canvas canvas;

    public static Canvas getCanvas() {
        return canvas;
    }

    public static void setCanvas(Canvas canvas) {
        Constants.canvas = canvas;
    }


   // public static boolean showMenu = true;
   // public static boolean showBall = false;
   // public static boolean showGameLevel = false;
    
    
}
