package com.istroop.istrooprecognize.ui.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.istroop.istrooprecognize.BaseActivity;
import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.Log.Logger;
import com.istroop.istrooprecognize.MyApplication;
import com.istroop.istrooprecognize.R;
import com.istroop.istrooprecognize.WMDetectorThread;
import com.istroop.istrooprecognize.utils.CameraManager;
import com.istroop.istrooprecognize.utils.HisDBHelper;
import com.istroop.istrooprecognize.utils.HttpTools;
import com.istroop.istrooprecognize.utils.Utils;
import com.istroop.openapi.Constant;
import com.istroop.watermark.AndroidWMDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecoActivity extends BaseActivity implements SurfaceHolder.Callback {

    private static final String TAG = "RECOAcitvity";

    private static final   int MESSAGE_RECOACTIVITY_EXIT = 0;
    protected static final int HIS_ADD_SUCCESS           = 21;
    protected static final int HIS_ADD_FAIL              = 22;
    private static final   int PHOTO_REQUEST_GALLERY     = 23;
    protected static final int RECO_ALBUM_FAIL           = 24;
    protected static final int RECO_ALBUM_SUCCESS        = 25;
    private static final   int MESAGE_SCAN_DATA          = 100;

    private ProgressBar centerPro;
    private Button      flashBtn;

    private Handler main_handler;
    private WMDetectorThread mDetectorThd = null;

    private boolean flashIsOpen = false;        //闪光灯是否打开
    private boolean isExit      = false;
    private boolean hasSurface;
    private boolean menu_icon_notClickable;

    private String    shopping_url;
    private ImageView reco_line;

    public int     DB_wm_id;
    public int     DB_tag_type;
    public long    DB_mtime;
    public String  DB_fileurl;
    public String  DB_tag_title;
    public String  DB_tag_url;
    public String  DB_tag_desc;
    public String  DB_location;
    public boolean hasFlashLight;


    private String dB_is_history;
    private String dB_pid;

    private TranslateAnimation ta;
    private CameraManager      cameraManager;

    public RecoActivity() {}

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        Logger.e( "onCreate" );
        setContentView( R.layout.reco );
        hasFlashLight = hasFlashLight();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics( metrics );
        IstroopConstants.density = metrics.density;
        if ( main_handler == null ) {
            main_handler = new MainHandler( this );
            mDetectorThd = new WMDetectorThread( "wmdetector", main_handler, this );
            mDetectorThd.start();
        }

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ) {
            setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
        }

        flashBtn = ( Button ) findViewById( R.id.flashBtn );
        TextView menu_icon = ( TextView ) findViewById( R.id.menu_icon );
        menu_icon.setOnClickListener( v -> {
            if ( !menu_icon_notClickable ) {
                gotoAlbum();
            }
        } );
        flashBtn.setOnClickListener( v -> {
            if ( hasFlashLight ) {
                if ( cameraManager.flashModeSwitch() ) {
                    Utils.log( TAG, "turn_on", 6 );
                    flashIsOpen = true;
                    flashBtn.setBackgroundResource( R.drawable.torch_on );
                } else {
                    Utils.log( TAG, "turn_off", 6 );
                    flashIsOpen = false;
                    flashBtn.setBackgroundResource( R.drawable.torch_off );
                }
            } else {
                Toast.makeText( getApplicationContext(), "您的设备不支持闪光灯",
                                Toast.LENGTH_LONG ).show();
            }
        } );
        centerPro = ( ProgressBar ) findViewById( R.id.centerPro );
        centerPro.setVisibility( View.INVISIBLE );

        reco_line = ( ImageView ) findViewById( R.id.reco_line );
        reco_line.setVisibility( View.VISIBLE );
        trans();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.e( "onStart" );
        if ( flashIsOpen ) {
            flashIsOpen = false;
            flashBtn.setBackgroundResource( R.drawable.torch_off );
        }
    }

    private void initCamera( SurfaceHolder surfaceHolder ) {
        if ( surfaceHolder == null ) {
            throw new IllegalStateException( "No SurfaceHolder provided" );
        }
        if ( cameraManager.isOpen() ) {
            Log.w( TAG, "Camera is already open" );
            return;
        }
        try {
            cameraManager.openDriver( surfaceHolder );
            cameraManager.startPreview();
            cameraManager.requestPreviewFrame();
        } catch ( RuntimeException ignored ) {

        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * 去相册
     */
    protected void gotoAlbum() {
        Intent intent = new Intent( Intent.ACTION_PICK, null );
        intent.setDataAndType( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                               "image/*" );
        intent.putExtra( "return-data", true );
        startActivityForResult( intent, PHOTO_REQUEST_GALLERY );
    }

    /**
     * 相册返回的结果
     */
    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        if ( data == null ) {
            return;
        }
        switch ( requestCode ) {
            case PHOTO_REQUEST_GALLERY:
                //这时禁止再次进入相册
                menu_icon_notClickable = true;
                Uri uri = data.getData();
                detectPic( uri );
                centerPro.setVisibility( View.VISIBLE );
                break;
            default:
                break;
        }
    }

    /**
     * 相册获取的图片进行识别
     */
    private void detectPic( @NonNull final Uri uri ) {
        new Thread() {
            public void run() {
                try {
                    Bitmap photo = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), uri );
                    if ( photo != null ) {
                        int wm_id = AndroidWMDetector.bmpdetect( photo );
                        if ( wm_id < 0 ) {
                            Message msg = Message.obtain();
                            msg.what = RECO_ALBUM_FAIL;
                            main_handler.sendMessage( msg );
                        } else {
                            Message msg = Message.obtain();
                            msg.what = RECO_ALBUM_SUCCESS;
                            msg.obj = wm_id;
                            main_handler.sendMessage( msg );
                        }
                    }
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void trans() {
        ta = new TranslateAnimation( Animation.RELATIVE_TO_PARENT, 0,
                                     Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT,
                                     0, Animation.RELATIVE_TO_PARENT, 1.0f );
        ta.setDuration( 3000 );
        ta.setRepeatCount( Animation.INFINITE );
        ta.setFillAfter( false );
        ta.setRepeatMode( Animation.RESTART );
        reco_line.startAnimation( ta );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ( cameraManager == null ) {
            cameraManager = new CameraManager( this, main_handler, mDetectorThd );
        }
        SurfaceView surfaceView = ( SurfaceView ) findViewById( R.id.camera_preview );
        SurfaceHolder holder = surfaceView.getHolder();
        if ( hasSurface ) {
            initCamera( holder );
        } else {
            holder.addCallback( this );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e( TAG, "onPause" );
        cameraManager.stopPreview();
        cameraManager.closeDriver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e( TAG, "onStop" );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e( TAG, "onDestory" );
        if ( ta != null ) {
            ta.cancel();
        }

        Handler handler = mDetectorThd.getHandler();
        if ( handler != null ) {
            Message msg = Message.obtain( handler,
                                          IstroopConstants.IAMessages_MAIN_QUIT );
            handler.sendMessage( msg );
        }

        try {
            mDetectorThd.join();
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        }
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

    private boolean hasFlashLight() {
        FeatureInfo[] feature = RecoActivity.this.getPackageManager()
                .getSystemAvailableFeatures();
        for ( FeatureInfo featureInfo : feature ) {
            if ( PackageManager.FEATURE_CAMERA_FLASH.equals( featureInfo.name ) ) {
                return true;
            }
        }
        return false;
    }

    public void loadPicInfo( final int recoResult ) {

//		LogUtil.i(TAG, "是否声音:" + IstroopConstants.isSound + "是否震动:"
//				+ IstroopConstants.isVibrator);
        Message msg = new Message();
        msg.what = MESAGE_SCAN_DATA;
        main_handler.sendMessage( msg );
        if ( IstroopConstants.isSound && IstroopConstants.isVibrator ) {
            setVibrator();
        } else if ( !IstroopConstants.isSound && IstroopConstants.isVibrator ) {
            setVibrator();
        } else {
            setVibrator();
        }
        DB_wm_id = recoResult;

        if ( !isConn( getApplicationContext() ) ) {
            Message message = main_handler.obtainMessage();
            message.what = IstroopConstants.IAMessages_NETWORK_ERROR;
            main_handler.sendMessage( message );
            return;
        }

        // http://tapi.tujoin.com/ICard/getInfo/?wmid=2232
        // String picurlStr="http://print.ichaotu.com/api/info/?wmid="+DB_wm_id;
        String picurlStr = IstroopConstants.URL_PATH + "/ICard/getInfo/?wmid="
                + DB_wm_id;
//		LogUtil.i(TAG, "picurlStr:" + picurlStr);
        String picResult;

        try {
            if ( IstroopConstants.isLogin ) {
                picResult = HttpTools.userInfo( picurlStr,
                                                IstroopConstants.cookieStore );
            } else {
                picResult = HttpTools.toString( picurlStr );
            }
//			LogUtil.i(TAG, "picResult:" + picResult);
            try {
                if ( picResult == null ) {
                    // 重新扫描数据
                    Message message = main_handler.obtainMessage();
                    message.what = IstroopConstants.IAMessages_RESULT_NULL;
                    main_handler.sendMessage( message );
                    return;
                } else if ( "联网失败".equals( picResult ) ) {
                    Message message = main_handler.obtainMessage();
                    message.what = IstroopConstants.IAMessages_NETWORK_ERROR;
                    main_handler.sendMessage( message );
                    return;
                }

                JSONObject resultObject = new JSONObject( picResult );
//				LogUtil.i(TAG, resultObject + "resultObject");
                boolean success = resultObject.getBoolean( "success" );
//				LogUtil.i(TAG, success + "状态成功与否");
                // {"data":{"3596":"无权限访问的超图"},"success":true}resultObject
                if ( success ) {
                    JSONObject temObject = resultObject.getJSONObject( "data" );
                    if ( temObject.optJSONObject( DB_wm_id + "" ) == null ) {
                        String data = temObject.getString( DB_wm_id + "" );
                        Message message = main_handler.obtainMessage();
                        message.what = IstroopConstants.IAMessages_UNKNOWN_ERROR;
                        message.obj = data;
                        main_handler.sendMessage( message );
                        return;
                    }
                    JSONObject dataObject = temObject.getJSONObject( DB_wm_id
                                                                             + "" );
                    DB_fileurl = dataObject.getString( "fileid" );
                    if ( TextUtils.isEmpty( DB_fileurl )
                            || "null".equals( DB_fileurl ) ) {
                        Message message = main_handler.obtainMessage();
                        message.what = IstroopConstants.IAMessages_UNKNOWN_ERROR;
                        message.obj = "图片没有更多的信息!";
                        main_handler.sendMessage( message );
                        return;
                    }
                    if ( dataObject.optJSONObject( "tags" ) == null ) {
                        Message message = main_handler.obtainMessage();
                        message.what = IstroopConstants.IAMessages_UNKNOWN_ERROR;
                        message.obj = "图片没有更多的信息!";
                        main_handler.sendMessage( message );
                        return;
                    }
                    JSONObject tagObject = dataObject.getJSONObject( "tags" );
                    if ( dataObject.isNull( "is_history" ) ) {
                        dB_is_history = "";
                    } else {
                        dB_is_history = dataObject.getString( "is_history" );
                    }
                    if ( dataObject.isNull( "pid" ) ) {
                        dB_pid = "";
                    } else {
                        dB_pid = dataObject.getString( "pid" );
                    }
//					LogUtil.i(TAG, "是否为历史记录:" + dB_is_history + "pid:" + dB_pid);
                    Iterator keys = tagObject.keys();
                    JSONObject contentObject;
                    ArrayList<String> arr = new ArrayList<>();
                    while ( keys.hasNext() ) {
                        String key = ( String ) keys.next();
                        arr.add( key );
                    }
//					LogUtil.i(TAG, "标记号码:" + arr.get(arr.size() - 1));
                    JSONObject jsonObject = tagObject.getJSONObject( arr.get( arr
                                                                                      .size() - 1 ) );
                    String typeString = jsonObject.getString( "type" );
                    contentObject = jsonObject.getJSONObject( "content" );
                    DB_tag_type = tagTypewithString( typeString );
                    if ( DB_tag_type == 3 ) {// copyright
//						LogUtil.i(TAG, "内容的数量:" + contentObject.length());
                        if ( contentObject.length() == 4 ) {
                            DB_tag_title = "copy";
                            DB_tag_url = "copy";
                            DB_tag_desc = "copy";
                        } else if ( contentObject.length() != 10
                                && contentObject.length() != 9 ) {
                            String realname = contentObject
                                    .getString( "realname" );
                            String company = contentObject.getString( "company" );
                            String job = contentObject.getString( "job" );
                            String companyUrl = contentObject
                                    .getString( "companyUrl" );
                            String email = contentObject.getString( "email" );
                            String phone = contentObject.getString( "phone" );
                            String weixin = contentObject.getString( "weixin" );
                            String introduce = contentObject
                                    .getString( "introduce" );
                            DB_tag_title = realname;
                            DB_tag_url = company + "==" + job + "=="
                                    + companyUrl + "==" + email + "==" + phone
                                    + "==" + weixin + "==" + introduce;
                            DB_tag_desc = "copyright";
                        } else {
                            String name = contentObject.getString( "name" );
                            String phone = contentObject.getString( "phone" );
                            String mail = contentObject.getString( "mail" );
                            String company = contentObject.getString( "company" );
                            String department = contentObject
                                    .getString( "department" );
                            String position = contentObject
                                    .getString( "position" );
                            String companyweb = contentObject
                                    .getString( "companyweb" );
                            String address = contentObject.getString( "address" );
                            String sign = contentObject.getString( "sign" );
                            String weixin = contentObject.getString( "weixin" );
                            DB_tag_title = name;
                            DB_tag_url = phone + "==" + mail + "==" + company
                                    + "==" + department + "==" + position
                                    + "==" + companyweb + "==" + address + "=="
                                    + sign + "==" + weixin;
                            DB_tag_desc = "";
//							LogUtil.i(TAG, "标题:" + DB_tag_title);
//							LogUtil.i(TAG, "描述:" + DB_tag_url);
                        }
                    } else if ( DB_tag_type == 4 || DB_tag_type == 5 ) {// pic
                        // personage
                        DB_tag_url = contentObject.getString( "url" );
                        DB_tag_title = contentObject.getString( "desc" );
                        DB_tag_desc = contentObject.getString( "desc" );
                    } else if ( DB_tag_type == 8 ) {// shopping
                        shopping_url = contentObject.getString( "url" );
//						LogUtil.i(TAG, "购物车:" + shopping_url);
                    } else if ( DB_tag_type == 0 ) {// text
                        DB_tag_title = contentObject.getString( "title" );
//						LogUtil.i(TAG, "标题" + DB_tag_title);
                        DB_tag_desc = contentObject.getString( "desc" );
//						LogUtil.i(TAG, "描述" + DB_tag_desc);
                    } else {// link
                        DB_tag_url = contentObject.getString( "url" );
//						LogUtil.i(TAG, "地址url:" + DB_tag_url);
                        DB_tag_title = contentObject.getString( "title" );
//						LogUtil.i(TAG, "标题" + DB_tag_title);
                        if ( !contentObject.isNull( "desc" ) ) {
                            DB_tag_desc = contentObject.getString( "desc" );
                        } else {
                            DB_tag_desc = "";
                        }
//						LogUtil.i(TAG, "描述" + DB_tag_desc);
                    }
                    // if(DB_tag_type==0||DB_tag_type==1||DB_tag_type==2||DB_tag_type==6||DB_tag_type==7||DB_tag_type==-1)
                    // shopping_url = shopping_url.trim();
                    // DB_tag_url = DB_tag_url.trim();
                    Message message = main_handler.obtainMessage();
                    message.arg1 = 1;
                    message.what = IstroopConstants.IAMessages_SUB_WATERMARK_ID;
                    main_handler.sendMessage( message );
                } else {
                    Message message = main_handler.obtainMessage();
                    message.what = IstroopConstants.IAMessages_SERVICE_ERROR;
                    main_handler.sendMessage( message );
                }

            } catch ( JSONException e ) {
                e.printStackTrace();
//				LogUtil.i(TAG, e.toString());
                Message message = main_handler.obtainMessage();
                message.what = IstroopConstants.IAMessages_UNKNOWN_ERROR;
                message.obj = picResult;
                main_handler.sendMessage( message );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            Message message = main_handler.obtainMessage();
            message.what = IstroopConstants.IAMessages_NETSWORK_SLOW;
            main_handler.sendMessage( message );

        }

    }

    private void scandata() {
        new Thread( () -> {
            String path = Constant.URL_PATH + "stat.gif?plat=android&type=scan&gps=" + Constant.coordinate.latitude + "," + Constant.coordinate.longitude + "&device_id=" + Constant.imei + "&device_type=" + Constant.model + "&appkey=" + Constant.appKey;
            String result = null;
            try {
                result = HttpTools.toString( path );
            } catch ( IOException e ) {
                e.printStackTrace();
            }
            Log.e( TAG, result + "`````````````" );
        } ).start();
    }

    private void setVibrator() {
        Vibrator vibrator = ( Vibrator ) getSystemService( VIBRATOR_SERVICE );
        long[] pattern = { 100, 300, 100, 300 };
        vibrator.vibrate( pattern, -1 );
    }

    public int tagTypewithString( String aString ) {
        switch ( aString ) {
            case "text":
                return 0;
            case "link":
                return 1;
            case "music":
                return 2;
            case "copyright":
                return 3;
            case "pic":
                return 4;
            case "personage":
                return 5;
            case "video":
                return 6;
            case "map":
                return 7;
            case "shopping":
                return 8;
            default:
                return -1;
        }
    }

    @Override
    public void surfaceCreated( SurfaceHolder holder ) {
        if ( !hasSurface ) {
            hasSurface = true;
            initCamera( holder );
        }
    }

    @Override
    public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {}

    @Override
    public void surfaceDestroyed( SurfaceHolder holder ) {
        hasSurface = false;
    }

    class MainHandler extends Handler {
        private RecoActivity mActivity = null;
        private String hisInfo;

        public MainHandler( RecoActivity activity ) {
            this.mActivity = activity;
        }

        public void handleMessage( Message msg ) {
            switch ( msg.what ) {
                case MESSAGE_RECOACTIVITY_EXIT:
                    isExit = false;
                    break;
                case MESAGE_SCAN_DATA:
                    scandata();
                    menu_icon_notClickable = false;
                    break;
                case RECO_ALBUM_FAIL:
                    centerPro.setVisibility( View.INVISIBLE );
                    menu_icon_notClickable = false;
                    Toast.makeText( RecoActivity.this,
                                    getResources().getString( R.string.reco_error ),
                                    Toast.LENGTH_SHORT ).show();
                    break;
                case RECO_ALBUM_SUCCESS:
                    // centerPro.setVisibility(View.INVISIBLE);
                    final int wm_id = ( Integer ) msg.obj;
                    new Thread() {
                        public void run() {
//                            loadPicInfo( wm_id );
                        }
                    }.start();
                    break;
                case IstroopConstants.IAMessages_RESULT_NULL:
                    centerPro.setVisibility( View.INVISIBLE );
                    break;
                case IstroopConstants.IAMessages_UNKNOWN_ERROR:
                    String str = ( String ) msg.obj;
                    if ( !TextUtils.isEmpty( str ) ) {
                        Toast.makeText( mActivity, str, Toast.LENGTH_LONG ).show();
                    }
                    centerPro.setVisibility( View.INVISIBLE );
                    break;
                case IstroopConstants.IAMessages_NETSWORK_SLOW:
                    Toast.makeText( mActivity, "无网络反应，请检查网络配置", Toast.LENGTH_LONG )
                            .show();
                    centerPro.setVisibility( View.INVISIBLE );
                    break;
                case IstroopConstants.IAMessages_SERVICE_ERROR:

                    Dialog serviceDialog = new AlertDialog.Builder( mActivity )
                            .setTitle( "服务器异常" )
                            .setMessage( "服务器异常,请联系服务器负责人" )
                            .setPositiveButton( "确定",
                                                ( dialog, which ) -> {
                                                    centerPro.setVisibility( View.INVISIBLE );
                                                } ).create();
                    serviceDialog.show();
                    break;
                case IstroopConstants.IAMessages_NETWORK_ERROR:
                    Dialog networkDialog = new AlertDialog.Builder( mActivity )
                            .setTitle( "网络错误" )
                            .setMessage( "无法连接到网络，请检查网络配置" )
                            .setPositiveButton( "确定",
                                                ( dialog, which ) -> {
                                                    centerPro.setVisibility( View.INVISIBLE );
                                                } ).create();
                    networkDialog.show();
                    break;
                case IstroopConstants.IAMessages_SHOW_PROGRESS:
                    centerPro.setVisibility( View.VISIBLE );
                    reco_line.setVisibility( View.INVISIBLE );
                    break;
                case IstroopConstants.IAMessages_SUB_FLAG_NO_WATERMARK:
                    cameraManager.requestPreviewFrame();
                    break;

                case IstroopConstants.IAMessages_SUB_WATERMARK_ID:
//				Log.i("Main", "info got:" + msg.arg1);
                    if ( DB_tag_type == 8 ) {
                        centerPro.setVisibility( View.INVISIBLE );
                        Intent webIntent = new Intent( RecoActivity.this,
                                                       RecoDetailWebActivity.class );
                        webIntent.putExtra( "picUrl", shopping_url );
                        // webIntent.putExtra("picType", "shooping");
                        // Log.i("shopping_url", "处理后的地址:"+shopping_url.trim());
                        startActivity( webIntent );
                        break;
                    } else if ( DB_tag_type < 0 ) {
                        Dialog serviceDialog2 = new AlertDialog.Builder( mActivity )
                                // .setTitle("服务器异常")
                                .setMessage( "这张超级图片里没有更多信息噢~" )
                                .setPositiveButton( "确定",
                                                    ( dialog, which ) -> {
                                                        centerPro
                                                                .setVisibility( View.INVISIBLE );
                                                    } ).create();
                        serviceDialog2.show();
                        break;
                    }

                    HisDBHelper dbHelper = new HisDBHelper( RecoActivity.this );
                    DB_mtime = System.currentTimeMillis();
//				LogUtil.i(TAG, "当前的时间:" + DB_mtime);
                    String time = DB_mtime + "";
                    time = time.substring( 0, 10 );
                    DB_mtime = Long.parseLong( time );
//				LogUtil.i(TAG, "处理后的当前的时间:" + DB_mtime);
                    if ( DB_location == null ) {
                        DB_location = "暂无";
                    }
                    if ( IstroopConstants.isLogin ) {
//					LogUtil.i(TAG, "是否存进数据库:" + dB_is_history);
                        hisInfo = null;
                        if ( "1".equals( dB_is_history ) ) {
                            if ( dB_pid != null ) {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        hisInfo = HttpTools.userInfo(
                                                IstroopConstants.URL_PATH
                                                        + "/ICard/setHistory/?pid="
                                                        + dB_pid + "&params[wmid]="
                                                        + DB_wm_id
                                                        + "&params[Type]="
                                                        + DB_tag_type
                                                        + "&params[Title]="
                                                        + DB_tag_title.trim()
                                                        + "&params[Createtime]="
                                                        + DB_mtime
                                                        + "&params[Desc]="
                                                        + DB_tag_desc
                                                        + "&params[Link]="
                                                        + DB_tag_url
                                                        + "&params[Address]="
                                                        + DB_location
                                                        + "&params[PicUrl]="
                                                        + DB_fileurl,
                                                IstroopConstants.cookieStore );
//									LogUtil.i(TAG, "历史记录更新服务器" + hisInfo);
                                        if ( hisInfo != null ) {
                                            try {
                                                JSONObject jsonObject = new JSONObject(
                                                        hisInfo );
                                                if ( jsonObject
                                                        .getBoolean( "success" ) ) {
                                                    Message message = Message
                                                            .obtain();
                                                    message.what = HIS_ADD_SUCCESS;
                                                    main_handler
                                                            .sendMessage( message );
                                                } else {
                                                    Message message = Message
                                                            .obtain();
                                                    message.what = HIS_ADD_FAIL;
                                                    main_handler
                                                            .sendMessage( message );
                                                }
                                            } catch ( JSONException e ) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }.start();
                            }
                        } else {
//						LogUtil.i(TAG, "正在进行存入数据库操作");
                            new Thread() {
                                @Override
                                public void run() {
                                    DB_tag_title = replaceBlank( DB_tag_title );
                                    DB_tag_desc = replaceBlank( DB_tag_desc );
                                    DB_location = replaceBlank( DB_location );
                                    hisInfo = HttpTools.userInfo(
                                            IstroopConstants.URL_PATH
                                                    + "/ICard/setHistory/?"
                                                    + "params[wmid]=" + DB_wm_id
                                                    + "&params[Type]="
                                                    + DB_tag_type
                                                    + "&params[Title]="
                                                    + DB_tag_title.split( " " )[0]
                                                    + "&params[Createtime]="
                                                    + DB_mtime + "&params[Desc]="
                                                    + DB_tag_desc
                                                    + "&params[Link]=" + DB_tag_url
                                                    + "&params[Address]="
                                                    + DB_location
                                                    + "&params[PicUrl]="
                                                    + DB_fileurl,
                                            IstroopConstants.cookieStore );
//								LogUtil.i(TAG, "历史记录传入服务器" + hisInfo);
                                    if ( hisInfo != null ) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(
                                                    hisInfo );
                                            if ( jsonObject.getBoolean( "success" ) ) {
                                                Message message = Message.obtain();
                                                message.what = HIS_ADD_SUCCESS;
                                                main_handler.sendMessage( message );
                                            } else {
                                                Message message = Message.obtain();
                                                message.what = HIS_ADD_FAIL;
                                                main_handler.sendMessage( message );
                                            }
                                        } catch ( JSONException e ) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }.start();
                        }
                    } else {
                        boolean isIn = dbHelper.isInDB( DB_wm_id );
                        if ( isIn ) {
                            dbHelper.updateDB( DB_wm_id, DB_fileurl, DB_tag_type,
                                               DB_tag_url, DB_tag_title, DB_tag_desc,
                                               DB_mtime, DB_location );
                        } else {
                            dbHelper.insertIntoDB( DB_wm_id, DB_fileurl,
                                                   DB_tag_type, DB_tag_url, DB_tag_title,
                                                   DB_tag_desc, DB_mtime, DB_location );
                        }
                        Message message = Message.obtain();
                        message.what = HIS_ADD_SUCCESS;
                        main_handler.sendMessage( message );
                    }

                    break;
                case HIS_ADD_SUCCESS:
                    centerPro.setVisibility( View.INVISIBLE );
                    reco_line.setVisibility( View.VISIBLE );
                    if ( DB_tag_type == 1 || DB_tag_type == 2 || DB_tag_type == 5
                            || DB_tag_type == 6 ) {
                        Intent webIntent = new Intent( RecoActivity.this,
                                                       RecoDetailWebActivity.class );
//					LogUtil.i(TAG, "url:" + DB_tag_url);
                        webIntent.putExtra( "picUrl", DB_tag_url );
                        startActivity( webIntent );
                    }/*
                 * else if(DB_tag_type == 8){ Intent webIntent= new
				 * Intent(RecoActivity.this, RecoDetailWebActivity.class);
				 * webIntent.putExtra("picUrl", shopping_url);
				 * startActivity(webIntent); }
				 */ else if ( DB_tag_type == 0 ) {
                        Intent textIntent = new Intent( RecoActivity.this,
                                                        RecoDetailTextActivity.class );
                        textIntent.putExtra( "DB_tag_title", DB_tag_title );
                        textIntent.putExtra( "DB_tag_desc", DB_tag_desc );
                        startActivity( textIntent );

                    } else if ( DB_tag_type == 4 ) {
                        Intent piciIntent = new Intent( RecoActivity.this,
                                                        RecoDetailPicActivity.class );
                        piciIntent.putExtra( "DB_tag_title", DB_tag_title );
                        piciIntent.putExtra( "DB_tag_url", DB_tag_url );
                        startActivity( piciIntent );

                    } else if ( DB_tag_type == 7 ) {
                        Intent mapIntent = new Intent( RecoActivity.this,
                                                       RecoDetailMapActivity.class );
                        mapIntent.putExtra( "DB_tag_url", DB_tag_url );
                        mapIntent.putExtra( "DB_tag_title", DB_tag_title );
                        startActivity( mapIntent );

                    } else if ( DB_tag_type == 3 ) {
                        DB_tag_title = DB_tag_title + "==" + DB_tag_url;
                        String[] split = DB_tag_title.split( "==" );
//					LogUtil.i(TAG, "描述信息:" + DB_tag_desc);
                        if ( !TextUtils.isEmpty( DB_tag_desc ) ) {
                            if ( "copy".equals( DB_tag_desc ) ) {
                                Intent editionIntent = new Intent(
                                        RecoActivity.this,
                                        RecoDetailEditionActivity.class );
                                editionIntent.putExtra( "DB_tag_url", DB_tag_url );
                                editionIntent
                                        .putExtra( "DB_tag_title", DB_tag_title );
                                startActivity( editionIntent );
                            } else {
                                Intent editionIntent = new Intent(
                                        RecoActivity.this, ICardTagPreview.class );
                                Bundle bundle = new Bundle();
                                bundle.putStringArray( "cardInfos", split );
                                bundle.putString( "headurl", DB_fileurl );
                                editionIntent.putExtras( bundle );
                                startActivity( editionIntent );
                            }
                        } else {
                            Intent editionIntent = new Intent( RecoActivity.this,
                                                               ICardTagPreview.class );
                            // editionIntent.putExtra("DB_tag_url", DB_tag_url);
                            Bundle bundle = new Bundle();
                            bundle.putStringArray( "cardInfos", split );
                            bundle.putString( "card_type", "reco" );
                            editionIntent.putExtras( bundle );
                            // editionIntent.putExtra("DB_tag_title", DB_tag_title);
                            startActivity( editionIntent );
                        }
                    }
                    break;
                case HIS_ADD_FAIL:
                    Toast.makeText( RecoActivity.this, "保存失败", Toast.LENGTH_SHORT )
                            .show();
                    break;
                default:
                    Dialog serviceDialog2 = new AlertDialog.Builder( mActivity )
                            .setMessage( "这张超级图片里没有更多信息噢~" )
                            .setPositiveButton( "确定",
                                                ( dialog, which ) -> {
                                                    centerPro.setVisibility( View.INVISIBLE );
                                                } ).create();
                    serviceDialog2.show();
                    break;
            }
        }
    }

    public static String replaceBlank( String str ) {
        String dest = "";
        if ( str != null ) {
            Pattern p = Pattern.compile( "\\s*|\t|\r|\n" );
            Matcher m = p.matcher( str );
            dest = m.replaceAll( "" );
        }
        return dest;
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    private void exit() {
        if ( !isExit ) {
            isExit = true;
            Toast.makeText( this, "再按一次退出程序", Toast.LENGTH_SHORT ).show();
            // 利用handler延迟发送更改状态信息
            main_handler.sendEmptyMessageDelayed( 7, 2000 );
        } else {
            MyApplication.getInstance().exit();
        }
    }

}