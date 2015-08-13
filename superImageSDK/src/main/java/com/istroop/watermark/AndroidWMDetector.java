package com.istroop.watermark;

import android.graphics.Bitmap;

/**
 * Created by joshua-zr on 15-3-18.
 */
public class AndroidWMDetector {
    static {
        System.loadLibrary("wmprintable");
    }

    /**
     * 需要app验证，对外提供的相机水印检测方法
     *
     * @param data
     * @param width
     * @param height
     * @return
     */
    public static native int detect(byte[] data, int width,
                                    int height);

    /**
     * 需要app验证，对外提供的相册水印检测方法
     *
     * @param bmp
     * @return
     */
    public static native int bmpdetect(Bitmap bmp);
}
