package com.istroop.istrooprecognize.utils;

import android.text.TextUtils;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeUtil {

    public static boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    public static boolean isUser(String user) {
        String str = "[\u4e00-\u9fa5a-zA-Z0-9\\-]{4,32}";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(user);
        return m.matches();
    }

    public static boolean isPassword(String password) {
        //[0-9a-zA-Z!@#$%^&*()_+]{6,16}
        //(?=^.{6,48}$)(?=.*\\d)(?=.*\\W+)(?=.*[A-Z])(?=.*[a-z])(?!.*\\n).*$
        String str = "[0-9a-zA-Z!@#$%^&*()_+]{6,48}";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(password);
        return m.matches();
    }

    /**
     * 去掉字符串中的空格
     *
     * @param str
     * @return String
     */
    public static String removeBlank(String str) {
        StringBuilder sb = new StringBuilder();
        char c = ' ';
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch != c) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String remove(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        String str2 = ""; //去空格后的字符串
        String[] str1 = str.split(" "); //把原字符串按空格分割
        for (int i = 0; i < str1.length; i++) {
            Log.i("空格", i + ":" + str1[i]);
            if (str1[i] != "") {
                str2 += str1[i]; //
            }
        }
        return str2;
    }

    public static String html(String content) {
        if (content == null)
            return "";
        String html = content;


        html = html.replace("\t", "");// 替换跳格
        html = html.replace(" ", ",");// 替换空格
        html = html.replace("　", ",");// 替换空格  //TODO

        return html;
    }
}
