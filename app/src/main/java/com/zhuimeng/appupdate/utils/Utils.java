package com.zhuimeng.appupdate.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Created by 追梦 on 2017/4/23.
 */

public class Utils {
    /**
     * 获取屏幕宽度
     * @param context
     * @return  int
     */
    public static int getScreenW(Context context){
        return getScreenSize(context, true);
    }

    /**
     * 获取屏幕高度
     * @param context
     * @param b
     * @return int
     */
    private static int getScreenSizeH(Context context, boolean b) {
        return getScreenSize(context, false);
    }
    private static int getScreenSize(Context context, boolean isWidth){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return isWidth ? dm.widthPixels : dm.heightPixels;
    }
}
