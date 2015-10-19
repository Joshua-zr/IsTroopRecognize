package com.istroop.istrooprecognize.utils;

import com.istroop.istrooprecognize.IstroopConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * 使用本类，别忘了修改 URL
 * leo 2013-3-28
 */
public class UploadClient {

    //	public static String server_url = "http://10.0.2.2:8080/fileUploadServer/upload";//fileUploadServer/
    //public static String server_url = "http://192.168.0.101:8080/fileUploadServer/upload";
    //http://api.ichaotu.com/ICard/upload/?dpi=300
    public static String server_url = IstroopConstants.URL_PATH + "/ICard/upload/?dpi=300&app=ichaotu";
    //public static String server_url = "http://tapi.tujoin.com/base/upload/add/?save=1";

    public static void upload( final String path, final IUploadListener listener ) {

        new Thread( () -> {
            uploadAsyn( path, listener );
        } ).start();
    }

    private static void uploadAsyn( String path, IUploadListener listener ) {
//        try {
//            DefaultHttpClient client = new DefaultHttpClient();
//            if ( IstroopConstants.cookieStore != null ) {
//                System.out.println( "cookiestroe" + IstroopConstants.cookieStore );
//                client.setCookieStore( IstroopConstants.cookieStore );
//            }
//            HttpParams params = client.getParams();
//            params.setParameter( CoreConnectionPNames.SO_TIMEOUT, 60000 );
//            params.setParameter( CoreConnectionPNames.CONNECTION_TIMEOUT, 60000 );
//            params.setParameter( CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1 );
//
//            HttpPost post = new HttpPost( server_url );
//
//            FileBody body = new FileBody( new File( path ) );
//
//            MultipartEntity reqEntity = new MultipartEntity();
//            //reqEntity.addPart("name", name);
//            reqEntity.addPart( "file1", body );
//            System.out.println( "reqentity:" + reqEntity );
//
//            post.setEntity( reqEntity );
//            System.out.println( "完成设置" + post );
//
//            HttpResponse response = client.execute( post );
//            System.out.println( "得到返回的信息:" + response );
//            if ( response.getStatusLine().getStatusCode() == 200 ) {
//                System.out.println( "服务器正常响应....." );
//                HttpEntity resEntity = response.getEntity();
//                InputStream ins = resEntity.getContent();
//                String result = convertStream( ins );
//                listener.upLoadResult( result );
//                System.out.println( "得到返回信息的string形式:" + result );
//                //System.out.println(EntityUtils.toString(resEntity,"utf-8"));//httpclient自带的工具类读取返回数据
//            }
//
//        } catch ( IOException e ) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

    }

    private static String convertStream( InputStream input ) {
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        try {
            byte[] buf = new byte[512];
            int c = input.read( buf );
            while ( c != -1 ) {
                bais.write( buf, 0, c );
                c = input.read( buf );
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return new String( bais.toByteArray() );
    }

}
