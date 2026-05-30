package com.vanvatcorporation.vanvatsach.constants;

import android.content.Context;

import com.vanvatcorporation.vanvatsach.helper.IOHelper;

public class Constants {

    public static final long MAX_TIME_TO_GET_STREAK_MILLI = 900000; // 15 minutes



    public static String getTemporaryBookPath(Context context){
        return IOHelper.CombinePath(IOHelper.getPersistentDataPath(context), "bookTemp.pdf");
    }
    public static String getTemporaryDownloadedBookDataPath(Context context){
        return IOHelper.CombinePath(IOHelper.getPersistentDataPath(context), "downloaded.txt");
    }
}
