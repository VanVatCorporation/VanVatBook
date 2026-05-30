package com.vanvatcorporation.vanvatsach.impl;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.vanvatcorporation.vanvatsach.R;

public class AppCompatActivityImpl extends AppCompatActivity {
    protected Bundle createrBundle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createrBundle = getIntent().getExtras();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isMonochromeEnabled = prefs.getBoolean(getString(R.string.settings_general_monochrome_mode), false);

        // Apply grayscale effect
        View rootView = getWindow().getDecorView();
        Paint grayscalePaint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(isMonochromeEnabled ? 0 : 1); // Fully grayscale
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        grayscalePaint.setColorFilter(filter);
        rootView.setLayerType(View.LAYER_TYPE_HARDWARE, grayscalePaint);

    }
}
