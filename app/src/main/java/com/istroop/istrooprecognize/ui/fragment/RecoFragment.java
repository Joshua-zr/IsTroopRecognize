package com.istroop.istrooprecognize.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.R;
import com.istroop.istrooprecognize.WMDetectorThread;
import com.istroop.istrooprecognize.ui.activity.ICardTagPreview;
import com.istroop.istrooprecognize.ui.activity.RecoDetailEditionActivity;
import com.istroop.istrooprecognize.ui.activity.RecoDetailMapActivity;
import com.istroop.istrooprecognize.ui.activity.RecoDetailPicActivity;
import com.istroop.istrooprecognize.ui.activity.RecoDetailTextActivity;
import com.istroop.istrooprecognize.ui.activity.RecoDetailWebActivity;
import com.istroop.istrooprecognize.utils.CameraManager;
import com.istroop.istrooprecognize.utils.CameraPreview2;
import com.istroop.istrooprecognize.utils.HisDBHelper;
import com.istroop.istrooprecognize.utils.HttpTools;
import com.istroop.istrooprecognize.utils.Utils;
import com.istroop.openapi.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RecoFragment extends BaseFragment implements Camera.PreviewCallback {

    private static final String TAG = "RecoFragment";

    public static final int RECO_ALBUM_FAIL                  = 0;
    public static final int RECO_ALBUM_SUCCESS               = 1;
    public static final int IAMessages_SUB_FLAG_NO_WATERMARK = 2;
    public static final int IAMessages_SUB_WATERMARK_ID      = 3;
    public static final int IAMessages_SHOW_PROGRESS         = 4;
    public static final int IAMessages_NETWORK_ERROR         = 5;
    public static final int IAMessages_SERVICE_ERROR         = 6;
    public static final int IAMessages_UNKNOWN_ERROR         = 7;
    public static final int IAMessages_NETSWORK_SLOW         = 8;
    public static final int IAMessages_RESULT_NULL           = 9;
    public static final int HIS_ADD_SUCCESS                  = 10;
    public static final int HIS_ADD_FAIL                     = 11;
    public static final int MESAGE_SCAN_DATA                 = 12;

    @Bind( R.id.menu_icon )
    TextView    menuIcon;
    @Bind( R.id.reco_line )
    ImageView   recoLine;
    @Bind( R.id.fl_camera_preview )
    FrameLayout flCameraPreview;
    @Bind( R.id.flashBtn )
    Button      flashBtn;
    @Bind( R.id.reco_desText )
    TextView    recoDesText;
    @Bind( R.id.reco_downText )
    TextView    recoDownText;
    @Bind( R.id.centerPro )
    ProgressBar centerPro;
    @Bind( R.id.reco_detail )
    TextView    recoDetail;

    private Camera           mCamera;
    private CameraPreview2   mPreview;
    private WMDetectorThread mDetectorThd;
    private boolean          menu_icon_notClickable;    //防止扫描等待的过程中，可以进入相册
    private boolean          flashIsOpen;

    private Integer DB_tag_type;   //标签类型标记
    private String  url;   //web标签链接
    private long    DB_mtime;   //数据库录入时间
    private String  DB_location;    //地址数据
    private String  dB_is_history;
    private String  dB_pid;
    private int     DB_wm_id;
    private String  DB_fileurl;
    private String  DB_tag_title;
    private String  DB_tag_desc;
    private String  DB_tag_url;

    private MainHandler main_handler;

    private int mPreviewWidth;
    private int mPreviewHeight;

    public RecoFragment() {}

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        Utils.log( TAG, "onCreate", 6 );
        if ( !Utils.chackCameraHardware( getActivity() ) ) {
            Toast.makeText( getActivity(), "您的设备没有摄像头！", Toast.LENGTH_LONG ).show();
        }
        if ( main_handler == null ) {
            main_handler = new MainHandler( this );
            mDetectorThd = new WMDetectorThread( "wmdetector", main_handler, getActivity() );
            mDetectorThd.start();
        }
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_reco, container, false );
        Utils.log( TAG, "onCreateView", 6 );
        ButterKnife.bind( this, view );
        if ( !safeCameraOpenInView( flCameraPreview ) ) {
            Utils.log( TAG, "Error, Camera failed to open", 6 );
            return view;
        }
        flashBtn.setOnClickListener( v -> {
            if ( getActivity().getPackageManager().hasSystemFeature( PackageManager.FEATURE_CAMERA_FLASH ) ) {
                Camera.Parameters params = mCamera.getParameters();
                if ( Camera.Parameters.FLASH_MODE_OFF.equals( params.getFlashMode() ) ) {
                    params.setFlashMode( Camera.Parameters.FLASH_MODE_TORCH );
                    flashBtn.setBackgroundResource( R.drawable.torch_off );
                    flashIsOpen = true;
                } else {
                    params.setFlashMode( Camera.Parameters.FLASH_MODE_OFF );
                    flashBtn.setBackgroundResource( R.drawable.torch_on );
                    flashIsOpen = false;
                }
                //IstroopConstants.mCamera.setParameters( params );
            } else {
                Toast.makeText( getActivity(), "您的设备不支持闪光灯",
                                Toast.LENGTH_LONG ).show();
            }
        } );
        return view;
    }

    private boolean safeCameraOpenInView( ViewGroup group ) {
        boolean qOpened;
        releaseCameraAndPreview();
        mCamera = CameraManager.getCameraInstance();
        qOpened = ( mCamera != null );
        CameraManager.initCamera( mCamera );
        mPreview = new CameraPreview2( getActivity().getBaseContext(), mCamera );
        group.addView( mPreview );
        return qOpened;
    }

    private void releaseCameraAndPreview() {
        if ( mCamera != null ) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if ( mPreview != null ) {
            mPreview.destroyDrawingCache();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.log( TAG, "onStart", 6 );
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.log( TAG, "onResume", 6 );
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.log( TAG, "onPause", 6 );
    }

    @Override
    public void onStop() {
        super.onStop();
        Utils.log( TAG, "onStop", 6 );
    }

    @Override
    public void onAttach( Activity activity ) {
        super.onAttach( activity );
        Utils.log( TAG, "onAttach", 6 );
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Utils.log( TAG, "onDetach", 6 );
        mCamera.setOneShotPreviewCallback( this );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.log( TAG, "onDestory", 6 );
        releaseCameraAndPreview();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind( this );
    }

    @Override
    public void onPreviewFrame( byte[] data, Camera camera ) {
        Handler handler = mDetectorThd.getHandler();
        if ( handler != null ) {
            Message msg = Message.obtain( handler,
                                          IstroopConstants.IAMessages_MAIN_WM_DETECT_REQ,
                                          mPreviewWidth, mPreviewHeight, data );
            handler.sendMessage( msg );
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
            Utils.log( TAG, result + "`````````````", 6 );
        } ).start();
    }

    class MainHandler extends Handler {

        private RecoFragment mFragment = null;
        private String hisInfo;

        public MainHandler( RecoFragment fragment ) {
            this.mFragment = fragment;
        }

        public void handleMessage( Message msg ) {
            switch ( msg.what ) {
                case MESAGE_SCAN_DATA:
                    scandata();
                    menu_icon_notClickable = false;
                    break;
                case RECO_ALBUM_FAIL:
                    centerPro.setVisibility( View.INVISIBLE );
                    menu_icon_notClickable = false;
                    Toast.makeText( getActivity(),
                                    getResources().getString( R.string.reco_error ),
                                    Toast.LENGTH_SHORT ).show();
                    break;
                case RECO_ALBUM_SUCCESS:
                    // centerPro.setVisibility(View.INVISIBLE);
                    final int wm_id = ( Integer ) msg.obj;
                    new Thread() {
                        public void run() {
                            loadPicInfo( wm_id );
                        }
                    }.start();
                    break;
                case IAMessages_RESULT_NULL:
                    // 当url正确 但返回结果为空的时候,重新扫描
                    mCamera.startPreview();
                    mCamera.setOneShotPreviewCallback( mFragment );
                    centerPro.setVisibility( View.INVISIBLE );
                    break;
                case IAMessages_UNKNOWN_ERROR:
                    String str = ( String ) msg.obj;
                    if ( !TextUtils.isEmpty( str ) ) {
                        Toast.makeText( getActivity(), str, Toast.LENGTH_LONG ).show();
                    }
                    mCamera.startPreview();
                    mCamera.setOneShotPreviewCallback( mFragment );
                    centerPro.setVisibility( View.INVISIBLE );
                    break;
                case IAMessages_NETSWORK_SLOW:
                    Toast.makeText( getActivity(), "无网络反应，请检查网络配置", Toast.LENGTH_LONG )
                            .show();
                    mCamera.startPreview();
                    mCamera.setOneShotPreviewCallback( mFragment );
                    centerPro.setVisibility( View.INVISIBLE );
                    break;
                case IAMessages_SERVICE_ERROR:

                    Dialog serviceDialog = new AlertDialog.Builder( getActivity() )
                            .setTitle( "服务器异常" )
                            .setMessage( "服务器异常,请联系服务器负责人" )
                            .setPositiveButton( "确定",
                                                ( dialog, which ) -> {
                                                    mCamera.startPreview();
                                                    mCamera.setOneShotPreviewCallback(
                                                            mFragment );
                                                    centerPro.setVisibility( View.INVISIBLE );
                                                } ).create();
                    serviceDialog.show();
                    break;
                case IAMessages_NETWORK_ERROR:
                    Dialog networkDialog = new AlertDialog.Builder( getActivity() )
                            .setTitle( "网络错误" )
                            .setMessage( "无法连接到网络，请检查网络配置" )
                            .setPositiveButton( "确定",
                                                ( dialog, which ) -> {
                                                    mCamera.startPreview();
                                                    mCamera
                                                            .setOneShotPreviewCallback(
                                                                    mFragment );
                                                    centerPro.setVisibility( View.INVISIBLE );
                                                } ).create();
                    networkDialog.show();
                    break;
                case IAMessages_SHOW_PROGRESS:
                    mCamera.stopPreview();
                    centerPro.setVisibility( View.VISIBLE );
                    recoLine.setVisibility( View.INVISIBLE );
                    break;
                case IAMessages_SUB_FLAG_NO_WATERMARK:
                    if ( mCamera != null ) {
                        mCamera.setOneShotPreviewCallback( mFragment );
                    }
                    break;
                case IAMessages_SUB_WATERMARK_ID:
                    if ( DB_tag_type == 8 ) {
                        centerPro.setVisibility( View.INVISIBLE );
                        Intent webIntent = new Intent( getActivity(), RecoDetailWebActivity.class );
                        webIntent.putExtra( "picUrl", url );
                        startActivity( webIntent );
                        break;
                    } else if ( DB_tag_type < 0 ) {
                        Dialog serviceDialog2 = new AlertDialog.Builder( getActivity() )
                                .setMessage( "这张超级图片里没有更多信息噢~" )
                                .setPositiveButton( "确定",
                                                    ( dialog, which ) -> {
                                                        mCamera.startPreview();
                                                        mCamera.setOneShotPreviewCallback(
                                                                mFragment );
                                                        centerPro.setVisibility( View.INVISIBLE );
                                                    } ).create();
                        serviceDialog2.show();
                        break;
                    }

                    HisDBHelper dbHelper = new HisDBHelper( getActivity() );
                    DB_mtime = System.currentTimeMillis();

                    String time = DB_mtime + "";
                    time = time.substring( 0, 10 );
                    DB_mtime = Long.parseLong( time );

                    if ( DB_location == null ) {
                        DB_location = "暂无";
                    }
                    if ( IstroopConstants.isLogin ) {
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
                    recoLine.setVisibility( View.VISIBLE );
                    if ( DB_tag_type == 1 || DB_tag_type == 2 || DB_tag_type == 5
                            || DB_tag_type == 6 ) {
                        Intent webIntent = new Intent( getActivity(),
                                                       RecoDetailWebActivity.class );
                        webIntent.putExtra( "picUrl", DB_tag_url );
                        startActivity( webIntent );
                    } else if ( DB_tag_type == 0 ) {
                        Intent textIntent = new Intent( getActivity(),
                                                        RecoDetailTextActivity.class );
                        textIntent.putExtra( "DB_tag_title", DB_tag_title );
                        textIntent.putExtra( "DB_tag_desc", DB_tag_desc );
                        startActivity( textIntent );

                    } else if ( DB_tag_type == 4 ) {
                        Intent piciIntent = new Intent( getActivity(),
                                                        RecoDetailPicActivity.class );
                        piciIntent.putExtra( "DB_tag_title", DB_tag_title );
                        piciIntent.putExtra( "DB_tag_url", DB_tag_url );
                        startActivity( piciIntent );

                    } else if ( DB_tag_type == 7 ) {
                        Intent mapIntent = new Intent( getActivity(),
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
                                        getActivity(),
                                        RecoDetailEditionActivity.class );
                                editionIntent.putExtra( "DB_tag_url", DB_tag_url );
                                editionIntent
                                        .putExtra( "DB_tag_title", DB_tag_title );
                                startActivity( editionIntent );
                            } else {
                                Intent editionIntent = new Intent(
                                        getActivity(), ICardTagPreview.class );
                                Bundle bundle = new Bundle();
                                bundle.putStringArray( "cardInfos", split );
                                bundle.putString( "headurl", DB_fileurl );
                                editionIntent.putExtras( bundle );
                                startActivity( editionIntent );
                            }
                        } else {
                            Intent editionIntent = new Intent( getActivity(),
                                                               ICardTagPreview.class );
                            Bundle bundle = new Bundle();
                            bundle.putStringArray( "cardInfos", split );
                            bundle.putString( "card_type", "reco" );
                            editionIntent.putExtras( bundle );
                            startActivity( editionIntent );
                        }
                        mCamera.startPreview();
                    }
                    break;
                case HIS_ADD_FAIL:
                    Toast.makeText( getActivity(), "保存失败", Toast.LENGTH_SHORT )
                            .show();
                    break;
                default:
                    Dialog serviceDialog2 = new AlertDialog.Builder( getActivity() )
                            .setMessage( "这张超级图片里没有更多信息噢~" )
                            .setPositiveButton( "确定",
                                                ( dialog, which ) -> {
                                                    mCamera.startPreview();
                                                    mCamera
                                                            .setOneShotPreviewCallback(
                                                                    mFragment );
                                                    centerPro.setVisibility( View.INVISIBLE );
                                                } ).create();
                    serviceDialog2.show();
                    break;
            }
        }
    }

    private void setVibrator() {
        Vibrator vibrator = ( Vibrator ) getActivity().getSystemService( Context.VIBRATOR_SERVICE );
        long[] pattern = { 100, 300, 100, 300 };
        vibrator.vibrate( pattern, -1 );
    }

    public void loadPicInfo( final int recoResult ) {
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

        if ( !Utils.isConn( getActivity().getApplicationContext() ) ) {
            Message message = main_handler.obtainMessage();
            message.what = IAMessages_NETWORK_ERROR;
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
                        url = contentObject.getString( "url" );
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

    /**
     * TODO 这个方法是否合理有待商榷
     *
     * @param str
     * @return
     */
    public static String replaceBlank( String str ) {
        String dest = "";
        if ( str != null ) {
            Pattern p = Pattern.compile( "\\s*|\t|\r|\n" );
            Matcher m = p.matcher( str );
            dest = m.replaceAll( "" );
        }
        return dest;
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

}
