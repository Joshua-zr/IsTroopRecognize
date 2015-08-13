package com.istroop.openapi;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by juzhu on 2015/3/3.
 */
public class UILApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Constant.context = getApplicationContext();
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        Constant.imei = tm.getDeviceId();
        Constant.model = Build.MODEL;
        Constant.manufacture = Build.MANUFACTURER;
        Constant.coordinate = getCoordinate();
        if (Constant.coordinate.longitude == 0.0 && Constant.coordinate.latitude == 0.0) {
        Constant.coordinate = PositionUtil
                .gps84_To_bd09(Constant.coordinate.latitude, Constant.coordinate.longitude);
        }
    }

    public Coordinate getCoordinate() {
        final Coordinate coordinate = new Coordinate(0, 0);
        final LocationManager locationManager = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                coordinate.latitude = location.getLatitude();
                coordinate.longitude = location.getLongitude();
            }
        } else {
            LocationListener locationListener = new LocationListener() {

                // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                // Provider被enable时触发此函数，比如GPS被打开
                @Override
                public void onProviderEnabled(String provider) {
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        coordinate.latitude = location.getLatitude();
                        coordinate.longitude = location.getLongitude();
                    }
                }

                // Provider被disable时触发此函数，比如GPS被关闭
                @Override
                public void onProviderDisabled(String provider) {

                }

                //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        Log.e("Map", "Location changed : Lat: "
                                + location.getLatitude() + " Lng: "
                                + location.getLongitude());
                    }
                }
            };
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0,
                                                   locationListener);
            Location location = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                coordinate.latitude = location.getLatitude(); //经度
                coordinate.longitude = location.getLongitude(); //纬度
            }
        }
        return coordinate;
    }
}
