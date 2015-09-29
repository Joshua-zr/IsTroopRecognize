package com.istroop.istrooprecognize;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.istroop.istrooprecognize.utils.OnBarStateChangeListener;
import com.istroop.istrooprecognize.utils.Utils;

public class ColorBarView extends View {

    private static final String TAG = "ColorBarView";
    private Bitmap backgroundBitmap; // 背景图片
    private Bitmap slideButtonBitmap; // 滑动按钮图片
    private Matrix matrix; // 矩阵
    private Paint  paint; // 画笔
    private int    currentX; // 当我们手指移动时x轴的偏移量
    private boolean isSliding = false; // 是否正在滑动中
    private OnBarStateChangeListener barStateChangeListener;
    private Bitmap                   createBitmap;
    private int                      window_width;

    public ColorBarView( Context context ) {
        super( context );
    }

    public ColorBarView( Context context, AttributeSet attrs ) {
        super( context, attrs );
    }

    public ColorBarView( Context context, AttributeSet attrs, int defStyleAttr ) {
        super( context, attrs, defStyleAttr );
    }

    public void setImageResID() {
        backgroundBitmap = BitmapFactory.decodeResource( getResources(),
                                                         R.drawable.icard_color_bar );
        slideButtonBitmap = BitmapFactory.decodeResource( getResources(),
                                                          R.drawable.icard_color_circle );
        int height = backgroundBitmap.getHeight();
        int width = backgroundBitmap.getWidth();
        Matrix matrixScale = new Matrix();
        float scale = window_width / width;
        Utils.log( TAG, "scale: " + scale, 4 );
        if ( scale < 1 ) {
            Utils.log( TAG, "scale<1" + scale, 4 );
            matrix.postScale( ( float ) window_width / width, 1 );
        } else if ( scale > 1 ) {
            Utils.log( TAG, "scale>1", 4 );
            matrix.preScale( ( float ) window_width / width, 1 );
        }
        createBitmap = Bitmap.createBitmap( backgroundBitmap, 0, 0, width,
                                            height, matrixScale, true );
        Utils.log( TAG, "create bitmap success", 4 );
        Utils.log( TAG,
                   createBitmap.getWidth() + "产生的宽度:高度" + createBitmap.getHeight(), 4 );
        Utils.log( TAG, backgroundBitmap.getWidth() + "背景的宽度:高度"
                + backgroundBitmap.getHeight(), 4 );
        Utils.log( TAG, slideButtonBitmap.getWidth() + "滚动的宽度:高度"
                + slideButtonBitmap.getHeight(), 4 );
    }

    @Override
    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {

        setMeasuredDimension( backgroundBitmap.getHeight(),
                              backgroundBitmap.getHeight() );
        super.onMeasure( widthMeasureSpec, heightMeasureSpec );
    }

    @Override
    protected void onDraw( Canvas canvas ) {
        super.onDraw( canvas );
        canvas.drawBitmap( backgroundBitmap, matrix, paint );
        int left = currentX;
        Utils.log( TAG, "当前位置" + currentX, 4 );
        if ( isSliding ) {// 当前正在滑动状态
            // 当前点击的值减去滑动按钮宽度的一半, 这样触摸事件的x轴偏移量就指向了滑动开关的中间位置
            if ( left <= 0 ) {
                // 如果超出了左边边界, 重新赋值为0
                left = 0;
            } else if ( left > window_width - slideButtonBitmap.getWidth() ) {
                // 如果超出了右边边界, 重新赋值为开关开启状态的左边位置
                left = window_width - slideButtonBitmap.getWidth();
            }
            /**
             * 根据滑动按钮的left值,来绘制滑动按钮
             */
            Utils.log( TAG, "滑动按钮的左边界+滑动状态:" + left, 4 );
            canvas.drawBitmap(
                    slideButtonBitmap,
                    left,
                    backgroundBitmap.getHeight() / 2
                            - slideButtonBitmap.getHeight() / 2 - 20, paint );
        } else {
            if ( left <= 0 ) {
                left = 0;
            } else if ( left > window_width - slideButtonBitmap.getWidth() ) {
                left = window_width - slideButtonBitmap.getWidth();
            }
            /**
             * 根据滑动按钮的left值,来绘制滑动按钮
             */
            Utils.log( TAG, "滑动按钮的左边界+静止状态:" + left, 4 );
            canvas.drawBitmap(
                    slideButtonBitmap,
                    left,
                    backgroundBitmap.getHeight() / 2
                            - slideButtonBitmap.getHeight() / 2 - 20, paint );
        }
    }

    @Override
    public boolean onTouchEvent( @NonNull MotionEvent event ) {
        switch ( event.getAction() ) {
            case MotionEvent.ACTION_DOWN:
                isSliding = true;
                currentX = ( int ) event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                currentX = ( int ) event.getX();
                break;
            case MotionEvent.ACTION_UP:
                isSliding = false;
                currentX = ( int ) event.getX();
                if ( currentX > 0
                        && currentX < createBitmap.getWidth()
                        - slideButtonBitmap.getWidth()
                        && barStateChangeListener != null ) {
                    barStateChangeListener.onBarStateChange( currentX );
                }
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }

}
