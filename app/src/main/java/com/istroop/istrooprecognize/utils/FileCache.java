package com.istroop.istrooprecognize.utils;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class FileCache {

    private File cacheDir;

    /**
     * @param context 环境变量
     */
    public FileCache( Context context ) {
        if ( android.os.Environment.getExternalStorageState().equals( android.os.Environment.MEDIA_MOUNTED ) ) {
            cacheDir = new File( android.os.Environment.getExternalStorageDirectory(), "ltcImageCache" );
        } else {
            cacheDir = context.getCacheDir();
        }
        if ( !cacheDir.exists() ) {
            cacheDir.mkdir();
        }
    }

    /**
     * @param url         链接
     * @param inputStream 文件输入流
     */
    public void addToFileCache( String url, InputStream inputStream ) {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream( getFromFileCache( url ) );
            copyStream( inputStream, outputStream );
        } catch ( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if ( outputStream != null ) {
                try {
                    outputStream.close();
                } catch ( IOException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public File getFromFileCache( String url ) {
        String fileName = urlToFileName( url );
        return new File( cacheDir, fileName );
    }

    /**
     * 清空文件缓存
     */
    public void clearCache() {
        File[] files = cacheDir.listFiles();
        if ( files == null )
            return;
        for ( File f : files )
            f.delete();
    }


    /**
     * 通过文件地址返回文件名
     *
     * @param url 链接地址
     * @return 文件名
     */
    private String urlToFileName( String url ) {
        return String.valueOf( url.hashCode() );
    }

    private void copyStream( InputStream is, OutputStream os ) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read( bytes, 0, buffer_size );
                if ( count == -1 )
                    break;
                os.write( bytes, 0, count );
            }
        } catch ( Exception ignored ) {}
    }

}
