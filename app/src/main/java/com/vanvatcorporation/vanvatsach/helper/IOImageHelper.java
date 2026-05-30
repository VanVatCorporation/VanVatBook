package com.vanvatcorporation.vanvatsach.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.vanvatcorporation.vanvatsach.manager.LoggingManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class IOImageHelper extends IOHelper {
    public static void SaveFileAsPNGImage(Context context, String path, Bitmap bm)
    {
        File file = new File(path);
        try {
            FileOutputStream outStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            LoggingManager.LogExceptionToNoteOverlay(context, e);
        }
    }
    public static Bitmap LoadFileAsPNGImage(Context context, String path)
    {
        File file = new File(path);
        try {
            InputStream in = new FileInputStream(file);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize=1;
            return BitmapFactory.decodeStream(in,null,options);
        } catch (FileNotFoundException e) {
            LoggingManager.LogExceptionToNoteOverlay(context, e);
        }
        return null;
    }
}
