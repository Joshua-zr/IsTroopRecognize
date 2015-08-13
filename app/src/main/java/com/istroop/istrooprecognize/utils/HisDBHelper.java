package com.istroop.istrooprecognize.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class HisDBHelper extends SQLiteOpenHelper {

    SQLiteDatabase db;

    public HisDBHelper( Context context ) {
        super( context, "history.db", null, 1 );
    }

    @Override
    public void onCreate( SQLiteDatabase db ) {

        db.execSQL( "create table history (his_wm_id integer,his_fileurl varchar,his_tag_type integer,his_tag_url varchar,his_tag_title varchar,his_tag_desc varchar,his_mtime long,his_location varchar)" );

    }

    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {

    }


    public boolean insertIntoDB( Integer his_wm_id, String his_fileurl, Integer his_tag_type, String his_tag_url, String his_tag_title, String his_tag_desc, long his_mtime, String his_location ) {
        db = this.getWritableDatabase();
        String sql = "insert into history(his_wm_id,his_fileurl,his_tag_type,his_tag_url,his_tag_title,his_tag_desc,his_mtime,his_location) values ('" + his_wm_id + "','" + his_fileurl + "','" + his_tag_type + "','" + his_tag_url + "','" + his_tag_title + "','" + his_tag_desc + "','" + his_mtime + "','" + his_location + "')";
        try {
            db.execSQL( sql );

            db.close();
            return true;
        } catch ( SQLException ex ) {

            ex.printStackTrace();
            db.close();
            return false;
        }
    }

    public boolean updateDB( Integer his_wm_id, String his_fileurl, Integer his_tag_type, String his_tag_url, String his_tag_title, String his_tag_desc, long his_mtime, String his_location ) {
        db = this.getWritableDatabase();
        try {
            db.execSQL( "update history set his_fileurl=?, his_tag_type=?, his_tag_url=?, his_tag_title=?, his_tag_desc=?, his_mtime=?,his_location=?WHERE his_wm_id = ?", new Object[] { his_fileurl, his_tag_type, his_tag_url, his_tag_title, his_tag_desc, his_mtime, his_location, his_wm_id } );

            db.close();
            return true;
        } catch ( SQLException ex ) {
            ex.printStackTrace();
            db.close();
            return false;
        }
    }


    public boolean deleteFromDB( Integer his_wm_id ) {
        db = this.getWritableDatabase();
        try {
            db.execSQL( "delete from history WHERE his_wm_id = ?", new Object[] { his_wm_id } );

            db.close();
            return true;
        } catch ( SQLException ex ) {

            db.close();
            return false;
        }

    }

    public boolean isInDB( Integer his_wm_id ) {
        db = this.getWritableDatabase();
        try {
            //TODO 这他妈的写的什么！！！！！！
            Cursor cursor = db.rawQuery( "select * FROM history WHERE his_wm_id = ?", new String[] { String.valueOf( his_wm_id ) } );
            while ( cursor.moveToNext() ) {
                db.close();
                return true;
            }
            db.close();
            return false;
        } catch ( SQLException ex ) {

            db.close();
            return false;
        }
    }


    public ArrayList<HashMap<String, Object>> queryALL() {
        ArrayList<HashMap<String, Object>> array = new ArrayList<>();
        db = this.getWritableDatabase();

        try {

            Cursor cursor = db.rawQuery( "select * FROM history Order By his_mtime Desc", null );

            while ( cursor.moveToNext() ) {

                int his_wm_id = cursor.getInt( cursor.getColumnIndex( "his_wm_id" ) );
                String his_fileurl = cursor.getString( cursor.getColumnIndex( "his_fileurl" ) );
                int his_tag_type = cursor.getInt( cursor.getColumnIndex( "his_tag_type" ) );
                String his_tag_title = cursor.getString( cursor.getColumnIndex( "his_tag_title" ) );
                String his_tag_url = cursor.getString( cursor.getColumnIndex( "his_tag_url" ) );
                String his_tag_desc = cursor.getString( cursor.getColumnIndex( "his_tag_desc" ) );
                long his_mtime = cursor.getLong( cursor.getColumnIndex( "his_mtime" ) );
                String his_location = cursor.getString( cursor.getColumnIndex( "his_location" ) );

                HashMap<String, Object> picInfoMap = new HashMap<>();
                picInfoMap.put( "his_wm_id", his_wm_id );
                picInfoMap.put( "his_fileurl", his_fileurl );
                picInfoMap.put( "his_tag_type", his_tag_type );
                picInfoMap.put( "his_tag_title", his_tag_title );
                picInfoMap.put( "his_tag_url", his_tag_url );
                picInfoMap.put( "his_tag_desc", his_tag_desc );
                picInfoMap.put( "his_mtime", his_mtime );
                picInfoMap.put( "his_location", his_location );
                array.add( picInfoMap );
            }
            cursor.close();
            db.close();
        } catch ( SQLException ex ) {
            db.close();
        }
        return array;
    }

}
