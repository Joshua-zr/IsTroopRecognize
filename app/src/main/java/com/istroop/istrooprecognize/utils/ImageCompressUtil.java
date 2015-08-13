package com.istroop.istrooprecognize.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * 图片压缩工具类
 *
 * @author 丶Life_
 */
public class ImageCompressUtil {

    /**
     * 通过压缩图片的尺寸来压缩图片大小
     *
     * @param pathName     图片的完整路径
     * @param targetWidth  缩放的目标宽度
     * @param targetHeight 缩放的目标高度
     * @return 缩放后的图片
     */
    public static Bitmap compressBySize( String pathName, int targetWidth,
                                         int targetHeight ) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;// 不去真的解析图片，只是获取图片的头部信息，包含宽高等；
        Bitmap bitmap;
        // 得到图片的宽度、高度；
        int imgWidth = opts.outWidth;
        int imgHeight = opts.outHeight;
        // 分别计算图片宽度、高度与目标宽度、高度的比例；取大于等于该比例的最小整数；
        int widthRatio = ( int ) Math.ceil( imgWidth / ( float ) targetWidth );
        int heightRatio = ( int ) Math.ceil( imgHeight / ( float ) targetHeight );
        if ( widthRatio > 1 || widthRatio > 1 ) {
            if ( widthRatio > heightRatio ) {
                opts.inSampleSize = widthRatio;
            } else {
                opts.inSampleSize = heightRatio;
            }
        }
        // 设置好缩放比例后，加载图片进内容；
        opts.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile( pathName, opts );
        return bitmap;
    }

}
