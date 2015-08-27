package com.istroop.istrooprecognize.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.istroop.istrooprecognize.BuildConfig;

/**
 * 常用工具类，静态变量，全局变量
 */
public class Utils {

    /**
     * 验证邮件
     *
     * @param email 邮件字符串
     * @return 判断是否为合格
     */
    public static boolean isNotEmail( Object email ) {

        String format = "\\w+([-+.]\\w+)*@\\w{2,}+([-.]\\w+)*\\.\\w{2,}+([-.]\\w+)*";
        // w{2,15}: 2~15个[a-zA-Z_0-9]字符；w{}内容是必选的。 如：dyh@152.com是合法的。
        // [a-z0-9]{3,}：至少三个[a-z0-9]字符,[]内的是必选的；如：dyh200896@16.com是不合法的。
        // [.]:'.'号时必选的； 如：dyh200896@163com是不合法的。
        // p{Lower}{2,}小写字母，两个以上。如：dyh200896@163.c是不合法的。
        return !( email != null && email.toString().trim().matches( format ) );
    }

    public static boolean isNotIndex( Object email ) {

        //String format = "([\\w-]+\\.)+[\\w-]+.([^a-z])(\\[\\w- .\\?%&=]*)?|[a-zA-Z0-9\\-\\.][\\w-]+.([^a-z])(\\[/w- .\\?%&=]*)?";
        String format = "(http://|ftp://|https://|www)?[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)[^\u4e00-\u9fa5\\s]*";
        // w{2,15}: 2~15个[a-zA-Z_0-9]字符；w{}内容是必选的。 如：dyh@152.com是合法的。
        // [a-z0-9]{3,}：至少三个[a-z0-9]字符,[]内的是必选的；如：dyh200896@16.com是不合法的。
        // [.]:'.'号时必选的； 如：dyh200896@163com是不合法的。
        // p{Lower}{2,}小写字母，两个以上。如：dyh200896@163.c是不合法的。
        return !( email != null && email.toString().trim().matches( format ) );
    }

    /**
     * 校验字符串是否是数字
     *
     * @param num 数字字符串
     * @return 是否为数字
     */
    public static boolean isNotPhoneNumber( String num ) {
        /*
         * try { Integer.parseInt(num); return false; } catch
		 * (NumberFormatException e) { return true; }
		 */

        // 正则表达式
        // “123”.matches("[0-9]*") 返回值：true
        // “123a”.matches("[0-9]*") 返回值：false

        return num == null || !num.matches( "^(13[0-9]|15[0-9]|18[0-9])[0-9]{8}$" );

    }


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px( Context context, float dpValue ) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return ( int ) ( dpValue * scale + 0.5f );
    }

    /**
     * 检测是否有摄像头
     */

    public static boolean chackCameraHardware( Context context ) {
        return context.getPackageManager().hasSystemFeature( PackageManager.FEATURE_CAMERA );
    }

    /**
     * 只有在debug版本下才会打印log
     *
     * @param tag  日志标签
     * @param msg  日志内容
     * @param type 日志类型 { VERBOSE = 2, DEBUG = 3, INFO = 4, WARN = 5, ERROR = 6}
     */
    public static void log( String tag, String msg, int type, Throwable tr ) {
        if ( BuildConfig.DEBUG )
            switch ( type ) {
                case 2:
                    if ( tr == null ) {
                        Log.v( tag, msg );
                    } else {
                        Log.v( tag, msg, tr );
                    }
                    break;
                case 3:
                    if ( tr == null ) {
                        Log.d( tag, msg );
                    } else {
                        Log.d( tag, msg, tr );
                    }
                    break;
                case 4:
                    if ( tr == null ) {
                        Log.i( tag, msg );
                    } else {
                        Log.i( tag, msg, tr );
                    }
                    break;
                case 5:
                    if ( tr == null ) {
                        Log.w( tag, msg );
                    } else {
                        Log.w( tag, msg, tr );
                    }
                    break;
                case 6:
                    if ( tr == null ) {
                        Log.e( tag, msg );
                    } else {
                        Log.e( tag, msg, tr );
                    }
                    break;
            }
    }

    /**
     * 只有在debug版本下才会打印log
     *
     * @param tag  日志标签
     * @param msg  日志内容
     * @param type 日志类型 { VERBOSE = 2, DEBUG = 3, INFO = 4, WARN = 5, ERROR = 6}
     */
    public static void log( String tag, String msg, int type ) {
        log( tag, msg, type, null );
    }

    public static boolean isConn( Context context ) {
        boolean bisConnFlag = false;
        ConnectivityManager conManager = ( ConnectivityManager ) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo network = conManager.getActiveNetworkInfo();
        if ( network != null ) {
            bisConnFlag = conManager.getActiveNetworkInfo().isAvailable();
        }
        return bisConnFlag;
    }

}
