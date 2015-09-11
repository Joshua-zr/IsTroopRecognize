package com.istroop.istrooprecognize.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.istroop.istrooprecognize.IstroopConstants;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
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


    public static String toString( String uri, int type ) throws IOException {
        return toString( uri, null, METHOD_POST );
    }

    public static String toString( String uri ) throws IOException {
        return toString( uri, null, METHOD_GET );
    }

    public static String toString( String uri, ArrayList<BasicNameValuePair> params, int method ) throws IOException {
        StringBuffer sbResult = null;
        StringBuilder sb = new StringBuilder( uri );
        if ( params != null && !params.isEmpty() ) {
            sb.append( '?' );
            for ( BasicNameValuePair pair : params ) {

                sb.append( pair.getName() )
                        .append( '=' )
                        .append( URLEncoder.encode( pair.getValue(), "UTF-8" ) )
                        .append( '&' );

            }
            sb.deleteCharAt( sb.length() - 1 );
        }

        HttpURLConnection http;
        System.out.println( "访问网址：" + sb.toString() );
        URL url = new URL( sb.toString() );
        if ( url.getProtocol().toLowerCase().equals( "https" ) ) {
            trustAllHosts();
            HttpsURLConnection https = ( HttpsURLConnection ) url.openConnection();
            https.setHostnameVerifier( DO_NOT_VERIFY );
            http = https;
        } else {
            http = ( HttpURLConnection ) url.openConnection();
        }

        http.setDoInput( true );

        http.setConnectTimeout( 60000 );

        if ( method == METHOD_GET ) {
            http.setRequestMethod( "GET" );
        } else {
            http.setRequestMethod( "POST" );
        }

        http.setRequestProperty( "accept", "*/*" );
        int resCode = http.getResponseCode();
        http.connect();

        if ( resCode == 200 ) {
            InputStream input = http.getInputStream();
            BufferedReader data = new BufferedReader( new InputStreamReader(
                    input, HTTP.UTF_8 ) );
            String strLine;
            while ( ( strLine = data.readLine() ) != null ) {
                if ( null == sbResult ) {
                    sbResult = new StringBuffer();
                }
                sbResult.append( strLine );
            }
            input.close();
            http.disconnect();
            if ( sbResult != null ) {
                return sbResult.toString();
            }
        }
        return null;
    }

    public static String login( String url, CookieStore cookieStore ) {
        url = Utils.remove( url );
        url = HttpTools.htmlEncode( url );
        Locale locale = Locale.getDefault();
        String country = locale.getCountry();
        // US HK 英语 CN 中文 KR 韩语
        if ( "US".equals( country ) || "HK".equals( country )
                || "EN".equals( country ) ) {
            url += "&lang=en";
        } else if ( "KR".equals( country ) ) {
            url += "&lang=ko";
        } else {
            url += "&lang=cn";
        }
        System.out.println( "访问网址：" + url );
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget;
        String result;
        if ( cookieStore != null ) {
            httpclient.setCookieStore( cookieStore );
            httpget = new HttpGet( url );
        } else {
            httpget = new HttpGet( url );
        }
        try {
            HttpResponse response = httpclient.execute( httpget );
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader( new InputStreamReader(
                    response.getEntity().getContent() ) );
            for ( String s = reader.readLine(); s != null; s = reader.readLine() ) {
                builder.append( s );
            }
            result = builder.toString();
            Log.i( TAG, "cookiestore" + httpclient.getCookieStore() );
            IstroopConstants.cookieStore = httpclient.getCookieStore();
            return result;
        } catch ( IOException e ) {
            e.printStackTrace();
            result = "联网失败";
        }
        return result;
    }

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

    public static String userInfo( String url, CookieStore cookieStore ) {
        url = remove( url );
        url = htmlEncode( url );
        System.out.println( "访问网址：" + url );
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = null;
        String result = null;
        if ( cookieStore != null ) {
            httpclient.setCookieStore( cookieStore );
            httpget = new HttpGet( url );
            try {
                HttpResponse response = httpclient.execute( httpget );
                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader( response.getEntity().getContent() ) );
                for ( String s = reader.readLine(); s != null; s = reader
                        .readLine() ) {
                    builder.append( s );
                }
                result = builder.toString();
                return result;
            } catch ( Exception e ) {
                e.printStackTrace();
                result = "联网失败";
            }
        }
        return result;
    }

}
