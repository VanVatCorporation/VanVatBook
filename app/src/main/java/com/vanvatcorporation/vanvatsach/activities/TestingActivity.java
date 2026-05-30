package com.vanvatcorporation.vanvatsach.activities;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.vanvatcorporation.vanvatsach.R;
import com.vanvatcorporation.vanvatsach.constants.Constants;
import com.vanvatcorporation.vanvatsach.constants.DynamicConstants;
import com.vanvatcorporation.vanvatsach.constants.MethodConstants;
import com.vanvatcorporation.vanvatsach.helper.IOHelper;
import com.vanvatcorporation.vanvatsach.impl.AppCompatActivityImpl;
import com.vanvatcorporation.vanvatsach.read.mobi.MobiReader;
import com.vanvatcorporation.vanvatsach.stuff.Account;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestingActivity extends AppCompatActivityImpl {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        MethodConstants.fullScreenNoTitleTransition(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        if(prefs.getBoolean(getString(R.string.settings_reading_keep_awake_while_reading), false))
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.layout_mobi_reader);


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

//        MobiReader.MobiParser parser = new MobiReader.MobiParser(IOHelper.CombinePath(IOHelper.getPersistentDataPath(this), "your_mobi_file.mobi"));
        MobiReader.MobiParser parser = new MobiReader.MobiParser("storage/emulated/0/Download/Conan-Vol01.mobi");
        List<MobiReader.MobiItem> items = new ArrayList<>();

        items.add(new MobiReader.MobiItem(parser.getTextContent()));
        for (byte[] imgData : parser.getImages()) {
            items.add(new MobiReader.MobiItem(imgData));
        }

        recyclerView.setAdapter(new MobiReader.MobiAdapter(items));


    }

}
