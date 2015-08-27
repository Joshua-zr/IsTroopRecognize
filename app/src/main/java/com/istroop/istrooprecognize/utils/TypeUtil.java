package com.istroop.istrooprecognize.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeUtil {

    public static boolean isPassword(String password) {
        //[0-9a-zA-Z!@#$%^&*()_+]{6,16}
        //(?=^.{6,48}$)(?=.*\\d)(?=.*\\W+)(?=.*[A-Z])(?=.*[a-z])(?!.*\\n).*$
        String str = "[0-9a-zA-Z!@#$%^&*()_+]{6,48}";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(password);
        return m.matches();
    }

    @TargetApi( Build.VERSION_CODES.KITKAT )
    public static String remove(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        String str2 = ""; //去空格后的字符串
        String[] str1 = str.split(" "); //把原字符串按空格分割
        for (int i = 0; i < str1.length; i++) {
            Log.i("空格", i + ":" + str1[i]);
            if ( !Objects.equals( str1[i], "" ) ) {
                str2 += str1[i]; //
            }
        }
        return str2;
    }

}
