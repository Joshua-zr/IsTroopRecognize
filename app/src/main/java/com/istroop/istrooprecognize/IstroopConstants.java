package com.istroop.istrooprecognize;

import org.apache.http.client.CookieStore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.support.v4.util.LruCache;
import android.widget.Checkable;

import com.istroop.istrooprecognize.bean.User;
import com.istroop.openapi.Coordinate;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;

import javax.sql.RowSet;


@SuppressLint( "SdCardPath" )
public class IstroopConstants {

    public final static int IAMessages_MAIN_WM_DETECT_REQ = 1;
    public final static int IAMessages_MAIN_QUIT          = 2;
    public final static int IAMessages_SHOW_PROGRESS      = 3;// show  progress
    public final static int IAMessages_NETWORK_ERROR      = 4;// NETWORK_ERROR
    public final static int IAMessages_SERVICE_ERROR      = 5;// SERVICE_ERROR
    public final static int IAMessages_UNKNOWN_ERROR      = 6;//UNKNOWN_ERROR
    public final static int IAMessages_NETSWORK_SLOW      = 7;// NETSWORK_SLOW
    public final static int IAMessages_RESULT_NULL        = 8;// result null

    public final static int    IAMessages_SUB_FLAG_NO_WATERMARK = 1;
    public final static int    IAMessages_SUB_WATERMARK_ID      = 2;
    public static final int    WINDOW_WIDTH                     = 0;
    public static final String ICHAOTU_URL                      = "http://api.ichaotu.com";
    public static final String URL_OTHER_LOGIN                  = "/Mobile/login";
    public static final String URL_PATH                         = "http://api.ichaotu.com";

    public static boolean     isSound;
    public static boolean     isVibrator;
    public static boolean     isLogin;
    public static CookieStore cookieStore;
    public static Coordinate  coordinate;
    public static String      imei;
    public static String      model;
    public static String      appKey;
    private final static int      maxMemory = ( int ) Runtime.getRuntime().maxMemory();
    private final static int      cacheSize = maxMemory / 5;//只分5分之一用来做图片缓存
    public static        LruCache mLruCache = new LruCache<String, Bitmap>(
            cacheSize ) {
        @Override
        protected int sizeOf( String key, Bitmap bitmap ) {//复写sizeof()方法
            // replaced by getByteCount() in API 12
            return bitmap.getRowBytes() * bitmap.getHeight() / 1024; //这里是按多少KB来算
        }
    };

    public static final String PICTURE_PATH = "/mnt/sdcard/istroop";
    public static float density;

    public final static int[] imageResIDs = new int[] { R.drawable.version_normal, R.drawable.link_normal,
            R.drawable.text_normal, R.drawable.location_normal, R.drawable.video_normal,
            R.drawable.pic_normal, R.drawable.person_normal };

    public final static int[] imageResIDPresseds = new int[] { R.drawable.version_pressed, R.drawable.link_pressed,
            R.drawable.text_pressed, R.drawable.location_pressed, R.drawable.video_pressed,
            R.drawable.pic_pressed, R.drawable.person_pressed };
    public static String absolutePath;
    public static Intent data;
    public static final String WHERE_PAGE_ACTION = "com.istroop.where_page_action";
    public static final String WHERE_PAGE_KEY    = "com.istroop.where_page_key";

    public final static String[] types = new String[] { "添加版权", "添加链接", "添加文字", "添加位置", "添加视频", "添加图片", "添加人物" };
    public static String       mobile;
    public static boolean      isMobile;
    public static String       user_id;
    public static String       user_name;
    public static String       user_image;
    public static String       cookie;
    public static CharSequence card_temp;

    public final static int ICARD_DESIGN_TAG_TYPE      = 13;
    public final static int ICARD_DESIGN_TAG_NAME      = 14;
    public final static int ICARD_DESIGN_TAG_DESC      = 15;
    public final static int ICARD_DESIGN_TAG_LINK      = 16;
    public final static int ICARD_DESIGN_TAG_TITLE     = 17;
    public final static int ICARD_DESIGN_TAG_VIDEO_MAP = 18;
    public static Checkable sm;
    public static String    downUrl;

    public static String access_token;
    public static String openid;

    public static User        admin;
    public static CookieStore cookiestore;
    public static IWXAPI      api;
    public static String APP_ID = "wxb272fda9c4623c5f";

    public static boolean isExit;   //程序是否要退出
}
