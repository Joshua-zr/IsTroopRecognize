package com.istroop.istrooprecognize.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.istroop.istrooprecognize.IstroopConstants;
import com.istroop.istrooprecognize.MainHandler;
import com.istroop.istrooprecognize.R;
import com.istroop.istrooprecognize.WMDetectorThread;
import com.istroop.istrooprecognize.utils.CameraManager;
import com.istroop.istrooprecognize.utils.CameraPreview;
import com.istroop.istrooprecognize.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RecoFragment extends BaseFragment implements SurfaceHolder.Callback {

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
    @Bind( R.id.s_preview )
    SurfaceView sPreview;
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
    private CameraPreview    mPreview;
    private WMDetectorThread mDetectorThd;
    private boolean          menu_icon_notClickable;    //防止扫描等待的过程中，可以进入相册
    private boolean          flashIsOpen;
    private boolean          hasFlashLight;

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

    private CameraManager cameraManager;
    private boolean       hasSurface;

    public RecoFragment() {}

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        Utils.log( TAG, "onCreate", 6 );
        if ( main_handler == null ) {
            main_handler = new MainHandler( this );
//            mDetectorThd = new WMDetectorThread( "wmdetector", main_handler, this );
            mDetectorThd.start();
        }
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_reco, container, false );
        Utils.log( TAG, "onCreateView", 6 );
        ButterKnife.bind( this, view );
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
                Toast.makeText( getActivity(), "您的设备不支持闪光灯",
                                Toast.LENGTH_LONG ).show();
            }
        } );
        return view;
    }

/*    private void releaseCameraAndPreview() {
        if ( mCamera != null ) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if ( mPreview != null ) {
            mPreview.destroyDrawingCache();
        }
    }*/

    @Override
    public void onStart() {
        super.onStart();
        Utils.log( TAG, "onStart", 6 );
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.log( TAG, "onResume", 6 );
        if ( cameraManager == null ) {
            cameraManager = new CameraManager( getActivity(), main_handler, mDetectorThd );
        }

        SurfaceHolder holder = sPreview.getHolder();
        if ( hasSurface ) {
            initCamera( holder );
        } else {
            holder.addCallback( this );
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.log( TAG, "onPause", 6 );
        cameraManager.stopPreview();
        cameraManager.closeDriver();
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.log( TAG, "onDestory", 6 );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind( this );
    }

    @Override
    public void surfaceCreated( SurfaceHolder holder ) {
        if ( !hasSurface ) {
            hasSurface = true;
            initCamera( holder );
        }
    }

    @Override
    public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {

    }

    @Override
    public void surfaceDestroyed( SurfaceHolder holder ) {

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
        String picResult = null;

        try {
            if ( IstroopConstants.isLogin ) {
//                picResult = HttpTools.userInfo( picurlStr,
//                                                IstroopConstants.cookieStore );
            } else {
//                picResult = HttpTools.toString( picurlStr );
            }
//			LogUtil.i(TAG, "picResult:" + picResult);
            try {
//                if ( picResult == null ) {
//                    // 重新扫描数据
//                    Message message = main_handler.obtainMessage();
//                    message.what = IstroopConstants.IAMessages_RESULT_NULL;
//                    main_handler.sendMessage( message );
//                    return;
//                } else if ( "联网失败".equals( picResult ) ) {
//                    Message message = main_handler.obtainMessage();
//                    message.what = IstroopConstants.IAMessages_NETWORK_ERROR;
//                    main_handler.sendMessage( message );
//                    return;
//                }

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

    public void centerProVisibility( int state ) {
        centerPro.setVisibility( state );
    }

    public void recoLineVisibility( int state ) {
        recoLine.setVisibility( state );
    }

    public void requestPreviewFrame() {
        cameraManager.requestPreviewFrame();
    }

}
