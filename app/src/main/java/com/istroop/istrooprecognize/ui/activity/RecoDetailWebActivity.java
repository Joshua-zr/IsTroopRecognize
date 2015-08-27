package com.istroop.istrooprecognize.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.istroop.istrooprecognize.BaseActivity;
import com.istroop.istrooprecognize.R;
import com.istroop.istrooprecognize.utils.Utils;

import java.io.File;

public class RecoDetailWebActivity extends BaseActivity {

    private static final String TAG                = "WebActivity";
    private static final String APP_CACAHE_DIRNAME = "Istroop_web_catch";

    private WebView     myWeb;
    private String      urlString;
    private ProgressBar progressBar;
    private boolean isPlayed = false;   //是否为第一次加载播放

    @SuppressLint( "SetJavaScriptEnabled" )
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.detail_web );

        //返回按键设置
        TextView leftBtn_web = ( TextView ) findViewById( R.id.leftBtn_web );
        leftBtn_web.setOnClickListener( v -> finish() );

        urlString = ( String ) getIntent().getSerializableExtra( "picUrl" );
        Log.i( "urlStirng", "网页的地址:" + urlString );
        if ( !urlString.startsWith( "http://" ) ) {
            urlString = "http://" + urlString;
        }
        progressBar = ( ProgressBar ) findViewById( R.id.progressBar );
        myWeb = ( WebView ) findViewById( R.id.webView );
        initWebView();
        myWeb.setWebViewClient( new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading( WebView view, String url ) {
                view.loadUrl( url );
                Utils.log( TAG, "shouldOverrideUrlLoading", 4 );
                return super.shouldOverrideUrlLoading( view, url );
            }

            @Override
            public void onPageFinished( WebView view, String url ) {
                progressBar.setVisibility( View.INVISIBLE );
                if ( !isPlayed ) {
                    Utils.log( TAG, "onPageFinished", 6 );
                    myWeb.loadUrl( "javascript:ichaotuAudioPlay()" );
                }
                isPlayed = true;
            }

        } );

        myWeb.setWebChromeClient( new WebChromeClient() {
            @Override
            public void onReceivedTitle( WebView view, String title ) {
                super.onReceivedTitle( view, title );
            }

        } );

        getWebHtml();
    }

    private void initWebView() {

        myWeb.getSettings().setJavaScriptEnabled( true );
        myWeb.requestFocus();
        myWeb.getSettings().setRenderPriority( WebSettings.RenderPriority.HIGH );
//        myWeb.getSettings().setCacheMode( WebSettings.LOAD_DEFAULT );  //设置 缓存模式
        myWeb.getSettings().setCacheMode( WebSettings.LOAD_NO_CACHE );
        myWeb.getSettings().setUseWideViewPort( true );
        myWeb.getSettings().setLoadWithOverviewMode( true );
        myWeb.getSettings().setAppCacheEnabled( false );
        // 开启 DOM storage API 功能  
//        myWeb.getSettings().setDomStorageEnabled( true );
        //开启 database storage API 功能  
//        myWeb.getSettings().setDatabaseEnabled( true );
//        String cacheDirPath = getFilesDir().getAbsolutePath() + APP_CACAHE_DIRNAME;

//        Log.i( TAG, "cacheDirPath=" + cacheDirPath );
        //设置数据库缓存路径  
//        myWeb.getSettings().setDatabasePath( cacheDirPath );
        //设置  Application Caches 缓存目录  
//        myWeb.getSettings().setAppCachePath( cacheDirPath );
        //开启 Application Caches 功能  
//        myWeb.getSettings().setAppCacheEnabled( true );
    }

    private void getWebHtml() {
        try {
            Log.i( "loadurl", "加载开始" );
            myWeb.loadUrl( urlString );
            Log.i( "loadurl", "加载完成" );
            // progressBar.setVisibility(View.INVISIBLE);
        } catch ( Exception ex ) {
            Log.i( "urlStirng", "在网页访问发生异常" );
            ex.printStackTrace();

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utils.log( TAG, "onStop", 6 );
        myWeb.loadUrl( "javascript:ichaotuAudioStop()" );
        isPlayed = false;   //
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        clearWebViewCache();
    }

    /**
     * 清除WebView缓存
     */
    public void clearWebViewCache() {

        //清理Webview缓存数据库
        try {
            deleteDatabase( "webview.db" );
            deleteDatabase( "webviewCache.db" );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        //WebView 缓存文件
        File appCacheDir = new File( getFilesDir().getAbsolutePath() + APP_CACAHE_DIRNAME );
        Log.e( TAG, "appCacheDir path=" + appCacheDir.getAbsolutePath() );

        File webviewCacheDir = new File( getCacheDir().getAbsolutePath() + "/webviewCache" );
        Log.e( TAG, "webviewCacheDir path=" + webviewCacheDir.getAbsolutePath() );

        //删除webview 缓存目录
        if ( webviewCacheDir.exists() ) {
            deleteFile( webviewCacheDir );
        }
        //删除webview 缓存 缓存目录
        if ( appCacheDir.exists() ) {
            deleteFile( appCacheDir );
        }
    }

    /**
     * 递归删除 文件/文件夹
     *
     * @param file
     */
    public void deleteFile( File file ) {

        Log.i( TAG, "delete file path=" + file.getAbsolutePath() );

        if ( file.exists() ) {
            if ( file.isFile() ) {
                file.delete();
            } else if ( file.isDirectory() ) {
                File files[] = file.listFiles();
                for ( File file1 : files ) {
                    deleteFile( file1 );
                }
            }
            file.delete();
        } else {
            Log.e( TAG, "delete file no exists " + file.getAbsolutePath() );
        }
    }

    @Override
    public boolean onKeyDown( int keyCode, @NonNull KeyEvent event ) {
        if ( myWeb.canGoBack() && keyCode == KeyEvent.KEYCODE_BACK ) {
            myWeb.goBack();
            return true;
        }
        return false;
    }
}
