package com.istroop.istrooprecognize.utils;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;

import okio.ByteString;

/**
 * Created by joshua-zr on 8/20/15.
 */
public class Okhttps {

    public static final MediaType JSON = MediaType.parse( "application/json; charset=utf-8" );

    private static Okhttps okhttps = new Okhttps();

    private OkHttpClient  client        = new OkHttpClient();
    private CookieManager cookieManager = new CookieManager();

    private Okhttps() {}

    public static Okhttps getInstance() {
        return okhttps;
    }

    public String get( String url ) throws IOException {
        cookieManager.setCookiePolicy( CookiePolicy.ACCEPT_ALL );
        client.setCookieHandler( cookieManager );
        Request request = new Request
                .Builder()
                .url( url )
                .build();
        Response response = client.newCall( request ).execute();
        return response.body().string();
    }

    public String post( String url, Object object ) throws IOException {
        RequestBody body = null;
        if ( object instanceof File ) {
            body = RequestBody.create( JSON, ( File ) object );
        } else if ( object instanceof String ) {
            body = RequestBody.create( JSON, ( String ) object );
        } else if ( object instanceof ByteString ) {
            body = RequestBody.create( JSON, ( ByteString ) object );
        } else if ( object instanceof Byte[] ) {
            body = RequestBody.create( JSON, String.valueOf( object ) );
        }
        Request request = new Request
                .Builder()
                .url( url )
                .post( body )
                .build();
        Response execute = client.newCall( request ).execute();
        return execute.body().toString();
    }

}
