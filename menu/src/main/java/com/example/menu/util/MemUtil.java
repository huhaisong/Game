package com.example.menu.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Created by 111 on 2016/5/31.
 */
public class MemUtil {


    //将float[]数组转换成FloatBuffer；
    public static FloatBuffer makeFloatBuffer(float[] floats) {

        FloatBuffer mFloatBuffer;
        // 初始化字节缓冲  (坐标数 * 4)float占四字节
        ByteBuffer qbb = ByteBuffer.allocateDirect(floats.length * 4);
        // 设用字节顺序为本地操作系统顺序
        qbb.order(ByteOrder.nativeOrder());
        // 从ByteBuffer创建一个浮点缓冲
        mFloatBuffer = qbb.asFloatBuffer();
        // 在缓冲区中写入数据
        mFloatBuffer.put(floats);
        // 设置buffer，从第一个坐标开始读
        mFloatBuffer.position(0);
        //返回IntBuffer
        return mFloatBuffer;
    }

    //将int[]数组转换成intBuffer；
    public static IntBuffer makeIntBuffer(int[] ints) {
        IntBuffer mIntBuffer;
        // 初始化字节缓冲  (坐标数 * 4)int占四字节
        ByteBuffer qbb = ByteBuffer.allocateDirect(ints.length * 4);
        // 设用字节序
        qbb.order(ByteOrder.nativeOrder());
        // 从ByteBuffer创建一个浮点缓冲
        mIntBuffer = qbb.asIntBuffer();
        // 把坐标们加入IntBuffer中
        mIntBuffer.put(ints);
        // 设置buffer，从第一个坐标开始读
        mIntBuffer.position(0);
        //返回IntBuffer
        return mIntBuffer;
    }

    //将short[]数组转换成shortBuffer；
    public static ShortBuffer makeShortBuffer(short[] shorts) {
        ShortBuffer mShortBuffer;
        ByteBuffer dlb = ByteBuffer.allocateDirect(shorts.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        mShortBuffer = dlb.asShortBuffer();
        mShortBuffer.put(shorts);
        mShortBuffer.position(0);
        //返回ShortBuffer
        return mShortBuffer;
    }
}
