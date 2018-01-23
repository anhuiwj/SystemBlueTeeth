package com.example.wangjie.systemblueteeth.util;

import android.graphics.Point;
import android.text.TextUtils;

/**
 * Created by wangjie on 2017/6/12.
 */

public class MathUtils_bak {
    //获取两点间直线距离
    public static int getLength(float x1,float y1,float x2,float y2) {
        return (int)Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2));
    }
    /**
     * 获取线段上某个点的坐标，长度为a.x - cutRadius
     * @param a 点A
     * @param b 点B
     * @param cutRadius 截断距离
     * @return 截断点
     */
    public static Point getBorderPoint(Point a, Point b, int cutRadius) {
        float radian = getRadian(a, b);
        return new Point(a.x + (int)(cutRadius * Math.cos(radian)), a.x + (int)(cutRadius * Math.sin(radian)));
    }

    //获取水平线夹角弧度
    public static float getRadian (Point a, Point b) {
        float lenA = b.x-a.x;
        float lenB = b.y-a.y;
        float lenC = (float)Math.sqrt(lenA*lenA+lenB*lenB);
        float ang = (float)Math.acos(lenA/lenC);
        ang = ang * (b.y < a.y ? -1 : 1);
        return ang;
    }

    /**
     * 16进制的字符串表示转成字节数组
     *
     * @param hexString 16进制格式的字符串
     * @return 转换后的字节数组
     **/
    public static byte[] toByteArray(String hexString) {
        if (TextUtils.isEmpty(hexString))
            throw new IllegalArgumentException("this hexString must not be empty");

        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {//因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
            byteArray[i] = (byte) (high << 4 | low);
            k += 2;
        }
        return byteArray;
    }
}
