package com.istroop.istrooprecognize.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.istroop.istrooprecognize.BuildConfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 常用工具类，静态变量，全局变量
 */
public class Utils {

    /**
     * 验证邮件
     *
     * @param email
     * @return
     */
    public static boolean isNotEmail( Object email ) {

        String format = "\\w+([-+.]\\w+)*@\\w{2,}+([-.]\\w+)*\\.\\w{2,}+([-.]\\w+)*";
        // w{2,15}: 2~15个[a-zA-Z_0-9]字符；w{}内容是必选的。 如：dyh@152.com是合法的。
        // [a-z0-9]{3,}：至少三个[a-z0-9]字符,[]内的是必选的；如：dyh200896@16.com是不合法的。
        // [.]:'.'号时必选的； 如：dyh200896@163com是不合法的。
        // p{Lower}{2,}小写字母，两个以上。如：dyh200896@163.c是不合法的。
        if ( email != null && email.toString().trim().matches( format ) ) {
            return false;// 邮箱名合法，返回false
        } else {
            return true;// 邮箱名不合法，返回true
        }
    }

    public static boolean isNotIndex( Object email ) {

        //String format = "([\\w-]+\\.)+[\\w-]+.([^a-z])(\\[\\w- .\\?%&=]*)?|[a-zA-Z0-9\\-\\.][\\w-]+.([^a-z])(\\[/w- .\\?%&=]*)?";
        String format = "(http://|ftp://|https://|www){0,1}[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)[^\u4e00-\u9fa5\\s]*";
        // w{2,15}: 2~15个[a-zA-Z_0-9]字符；w{}内容是必选的。 如：dyh@152.com是合法的。
        // [a-z0-9]{3,}：至少三个[a-z0-9]字符,[]内的是必选的；如：dyh200896@16.com是不合法的。
        // [.]:'.'号时必选的； 如：dyh200896@163com是不合法的。
        // p{Lower}{2,}小写字母，两个以上。如：dyh200896@163.c是不合法的。
        if ( email != null && email.toString().trim().matches( format ) ) {
            return false;// 邮箱名合法，返回false
        } else {
            return true;// 邮箱名不合法，返回true
        }
    }

    /**
     * 校验字符串是否是数字
     *
     * @param str
     * @return
     */
    public static boolean isNotPhoneNumber( String str ) {
        /*
         * try { Integer.parseInt(str); return false; } catch
		 * (NumberFormatException e) { return true; }
		 */

        // 正则表达式
        // “123”.matches("[0-9]*") 返回值：true
        // “123a”.matches("[0-9]*") 返回值：false

        if ( str != null ) {
            return !str.matches( "^(13[0-9]|15[0-9]|18[0-9])[0-9]{8}$" );
        }
        return true;

    }


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px( Context context, float dpValue ) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return ( int ) ( dpValue * scale + 0.5f );
    }

    /**
     * 只有在debug版本下才会打印log
     * @param tag  日志标签
     * @param msg  日志内容
     * @param type 日志类型 { VERBOSE = 2, DEBUG = 3, INFO = 4, WARN = 5, ERROR = 6}
     */
    public static void log( String tag, String msg, int type ) {
        if ( BuildConfig.DEBUG )
            switch ( type ) {
                case 2:
                    Log.v( tag, msg );
                    break;
                case 3:
                    Log.d( tag, msg );
                    break;
                case 4:
                    Log.i( tag, msg );
                    break;
                case 5:
                    Log.w( tag, msg );
                    break;
                case 6:
                    Log.e( tag, msg );
                    break;
            }
    }

}
