package com.istroop.istrooprecognize.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

public class BitmapUtil {

    public static Bitmap getPicFromBytes( byte[] bytes,
                                          BitmapFactory.Options opts ) {
        if ( bytes != null )
            if ( opts != null )
                return BitmapFactory.decodeByteArray( bytes, 0, bytes.length,
                                                      opts );
            else
                return BitmapFactory.decodeByteArray( bytes, 0, bytes.length );
        return null;
    }

    public static Bitmap getCircleBitmap( Bitmap bitmap ) {
        int x = bitmap.getWidth();
        int y = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap( x, y, Config.ARGB_8888 );
        Canvas canvas = new Canvas( output );

        final int color = 0xff424242;
        final Paint paint = new Paint();
        // 根据原来图片大小画一个矩形
        final Rect rect = new Rect( 0, 0, x, y );
        paint.setAntiAlias( true );
        paint.setColor( color );
        // 画出一个圆
        canvas.drawCircle( x / 2, x / 2, x / 2 - 5, paint );
        // canvas.translate(-25, -6);
        // 取两层绘制交集,显示上层
        paint.setXfermode( new PorterDuffXfermode( Mode.SRC_IN ) );

        // 将图片画上去
        canvas.drawBitmap( bitmap, rect, rect, paint );
        // 返回Bitmap对象
        return output;
    }

}
