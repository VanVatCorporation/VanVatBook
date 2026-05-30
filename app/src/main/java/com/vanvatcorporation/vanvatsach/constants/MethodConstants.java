package com.vanvatcorporation.vanvatsach.constants;

import android.app.Activity;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;

import com.vanvatcorporation.vanvatsach.R;
import com.vanvatcorporation.vanvatsach.impl.java.RunnableImpl;

public class MethodConstants {

    public static void fullScreenNoTitleTransition(Activity activity)
    {
        //EdgeToEdge.enable();
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, R.anim.anim_background_entry, R.anim.anim_background_exit);
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, R.anim.anim_background_entry, R.anim.anim_background_exit);
        }
        else {
            activity.overridePendingTransition(R.anim.anim_background_entry, R.anim.anim_background_exit);
        }
    }
    public static void statusBarNoTitleTransition(Activity activity)
    {
        //EdgeToEdge.enable();
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, R.anim.anim_background_entry, R.anim.anim_background_exit);
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, R.anim.anim_background_entry, R.anim.anim_background_exit);
        }
        else {
            activity.overridePendingTransition(R.anim.anim_background_entry, R.anim.anim_background_exit);
        }
    }
}
