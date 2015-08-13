package com.istroop.istrooprecognize.bean;

import android.os.Parcel;
import android.os.Parcelable;

/** 用户属性
 * Created by joshua-zr on 15-3-25.
 */
public class User implements Parcelable {
    private String avater;
    private String token;
    private String userInfoUid;
    private String sex;
    private String address;
    private String mobile;
    private String color;
    private String sign;
    private String picCount;
    private String followCount;
    private String fansCount;
    private String oauthId;
    private int    isfollowed;
    private String userSign;

    public String getOauthId() {
        return oauthId;
    }

    public void setOauthId( String oauthId ) {
        this.oauthId = oauthId;
    }

    private String zanCount;

    public void setAddress( String address ) {
        this.address = address;
    }

    public String getColor() {
        return color;
    }

    public void setColor( String color ) {
        this.color = color;
    }

    public String getAvater() {
        return avater;
    }

    public void setAvater( String avater ) {
        this.avater = avater;
    }

    public void setToken( String token ) {
        this.token = token;
    }

    public void setUserInfoUid( String userInfoUid ) {
        this.userInfoUid = userInfoUid;
    }

    public String getSex() {
        return sex;
    }

    public void setSex( String sex ) {
        this.sex = sex;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername( String username ) {
        this.username = username;
    }

    private String username;

    public User() {

    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel( Parcel dest, int flags ) {
        dest.writeString( this.avater );
        dest.writeString( this.token );
        dest.writeString( this.userInfoUid );
        dest.writeString( this.sex );
        dest.writeString( this.address );
        dest.writeString( this.mobile );
        dest.writeString( this.color );
        dest.writeString( this.sign );
        dest.writeString( this.picCount );
        dest.writeString( this.followCount );
        dest.writeString( this.fansCount );
        dest.writeString( this.oauthId );
        dest.writeInt( this.isfollowed );
        dest.writeString( this.userSign );
        dest.writeString( this.zanCount );
        dest.writeString( this.username );
    }

    protected User( Parcel in ) {
        this.avater = in.readString();
        this.token = in.readString();
        this.userInfoUid = in.readString();
        this.sex = in.readString();
        this.address = in.readString();
        this.mobile = in.readString();
        this.color = in.readString();
        this.sign = in.readString();
        this.picCount = in.readString();
        this.followCount = in.readString();
        this.fansCount = in.readString();
        this.oauthId = in.readString();
        this.isfollowed = in.readInt();
        this.userSign = in.readString();
        this.zanCount = in.readString();
        this.username = in.readString();
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel( Parcel source ) {return new User( source );}

        public User[] newArray( int size ) {return new User[size];}
    };
}
