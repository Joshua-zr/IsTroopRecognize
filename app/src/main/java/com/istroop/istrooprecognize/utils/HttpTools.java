package com.istroop.istrooprecognize.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpTools {
    static {
        if ( Integer.parseInt( Build.VERSION.SDK ) <= Build.VERSION_CODES.FROYO ) {
            System.setProperty( "http.keepAlive", "false" );
        }
    }

    private static final String TAG         = "HttpTools";
    public static final  int    METHOD_GET  = 1;
    public static final  int    METHOD_POST = 2;

    public static Bitmap imageload( Context context, String url ) {
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            URL url_image = new URL( url );
            HttpURLConnection conn = ( HttpURLConnection ) url_image
                    .openConnection();
            conn.setReadTimeout( 5000 );
            conn.setConnectTimeout( 5000 );
            conn.setRequestMethod( "GET" );
            conn.setDoInput( true );
            conn.setRequestProperty( "accept", "*/*" );
            conn.connect();
            int code = conn.getResponseCode();
            if ( 200 == code ) {
                inputStream = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream( inputStream );
            } else {
                Log.i( TAG, "网络返回异常,返回图片不正确" );
            }
            if ( inputStream != null ) {
                inputStream.close();
            }
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
            Log.i( TAG, "url格式不正确" + e.toString() );
        } catch ( IOException e ) {
            e.printStackTrace();
            Log.i( TAG, "输入输出异常" + e.toString() );
        }
        return bitmap;
    }

    final static HostnameVerifier DO_NOT_VERIFY = ( hostname, session ) -> true;

    private static void trustAllHosts() {

        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }

            public void checkClientTrusted( X509Certificate[] chain, String authType ) throws CertificateException {
            }

            public void checkServerTrusted( X509Certificate[] chain, String authType ) throws CertificateException {
            }
        }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance( "TLS" );
            sc.init( null, trustAllCerts, new java.security.SecureRandom() );
            HttpsURLConnection.setDefaultSSLSocketFactory( sc.getSocketFactory() );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public static String remove( String str ) {
        if ( TextUtils.isEmpty( str ) ) {
            return str;
        }
        String str2 = ""; //去空格后的字符串
        String[] str1 = str.split( " " ); //把原字符串按空格分割
        for ( String aStr1 : str1 ) {
            if ( !aStr1.equals( "" ) ) {
                str2 += aStr1;
            }
        }
        return str2;
    }

    /**
     * Html-encode the string.
     *
     * @param s the string to be encoded
     * @return the encoded string
     */
    public static String htmlEncode( String s ) {
        StringBuilder sb = new StringBuilder();
        char c;
        for ( int i = 0; i < s.length(); i++ ) {
            c = s.charAt( i );
            switch ( c ) {
                case '"':
                    sb.append( "\u201c" );
                    break;
                case '#':
                    sb.append( "%23" );
                    break;
                default:
                    sb.append( c );
            }
        }
        return sb.toString();
    }

}
