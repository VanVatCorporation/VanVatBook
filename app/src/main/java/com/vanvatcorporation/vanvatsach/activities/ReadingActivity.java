package com.vanvatcorporation.vanvatsach.activities;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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

import com.github.barteksc.pdfviewer.PDFView;
import com.vanvatcorporation.vanvatsach.R;
import com.vanvatcorporation.vanvatsach.constants.Constants;
import com.vanvatcorporation.vanvatsach.constants.DynamicConstants;
import com.vanvatcorporation.vanvatsach.constants.MethodConstants;
import com.vanvatcorporation.vanvatsach.impl.AppCompatActivityImpl;
import com.vanvatcorporation.vanvatsach.stuff.Account;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReadingActivity extends AppCompatActivityImpl {
    ConstraintLayout loadingLayout;
    PDFView pdfView;
    CountDownTimer countDownTimer;
    int lastReadingPage = 1;
    long readingTimestamp = 0;
    MainActivity.BookListData bookData;
    MainActivity.BookListData.BookChapter selectedChapter;
    DetailedBookActivity.ReadingSettings readingSettings;
    Account.AccountInformation.AccountData.AccountReadingProgress.LastBookChapter.BookChapterInfo bookChapterInfo;
    ImageView starButton;

    boolean[] starredPages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        readingTimestamp = new Date().getTime();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        MethodConstants.fullScreenNoTitleTransition(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        if(prefs.getBoolean(getString(R.string.settings_reading_keep_awake_while_reading), false))
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.layout_reading);


        pdfView = findViewById(R.id.pdfView);
        RelativeLayout bottomView = findViewById(R.id.navigationLayout);
        TextView pageText = findViewById(R.id.pagesText);
        starButton = findViewById(R.id.starred_page);


        String overridePdfPath = "";
        if(getIntent() != null)
        {
            MainActivity.BookListData data = (MainActivity.BookListData) getIntent().getSerializableExtra("BookListElement");
            if(data != null)
            {
                //fetchAndDisplayPdf(data.getBookUrl());
                bookData = data;
            }
            //else finish();
            //Handle offline mode temporarily



            MainActivity.BookListData.BookChapter selectedChapter = (MainActivity.BookListData.BookChapter) getIntent().getSerializableExtra("SelectedChapter");
            if(selectedChapter != null)
            {
                //fetchAndDisplayPdf(data.getBookUrl());
                this.selectedChapter = selectedChapter;
            }


            DetailedBookActivity.ReadingSettings readingSettings = (DetailedBookActivity.ReadingSettings) getIntent().getSerializableExtra("ReadingSettings");
            if(readingSettings != null)
            {
                //fetchAndDisplayPdf(data.getBookUrl());
                this.readingSettings = readingSettings;
            }

            if(getIntent().hasExtra("OverridePdfPath"))
                overridePdfPath = getIntent().getStringExtra("OverridePdfPath");

        }
        //else finish();
        //Handle offline mode temporarily





        starButton.setOnClickListener(v -> {
            if(starredPages[pdfView.getCurrentPage()]) {
                starButton.setImageResource(R.drawable.baseline_star_border_24);
                starButton.setColorFilter(0xFF000000, PorterDuff.Mode.SRC_ATOP);
                starredPages[pdfView.getCurrentPage()] = false;
            }
            else {
                starButton.setImageResource(R.drawable.baseline_star_24);
                starButton.setColorFilter(0xFFFFFF00, PorterDuff.Mode.SRC_ATOP);
                starredPages[pdfView.getCurrentPage()] = true;
            }
        });

        pdfView.fromFile(new File((overridePdfPath == null || overridePdfPath.isEmpty()) ? Constants.getTemporaryBookPath(this) : overridePdfPath))
                .swipeHorizontal(prefs.getBoolean(getString(R.string.settings_reading_horizontal_mode), false))
                .nightMode(prefs.getBoolean(getString(R.string.settings_reading_night_mode), false))
                .enableAntialiasing(prefs.getBoolean(getString(R.string.settings_reading_antialiasing), true))
                .pageFling(prefs.getBoolean(getString(R.string.settings_reading_page_fling), true))
                .pageSnap(prefs.getBoolean(getString(R.string.settings_reading_page_snap), true))
                .enableSwipe(true)
                .defaultPage(readingSettings.pages)
                .onPageChange(
                (v, a) -> {

                    Runnable countdownRetractBottomBar = () -> countDownTimer = new CountDownTimer(3000, 3000) {
                        @Override
                        public void onTick(long l) {

                        }

                        @Override
                        public void onFinish() {
                            Animation animation1 = AnimationUtils.loadAnimation(ReadingActivity.this, R.anim.anim_scale_down_pivot_bottom_fade_out);
                            animation1.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    bottomView.setFocusable(false);
                                    bottomView.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });

                            bottomView.startAnimation(animation1);
                            countDownTimer = null;
                        }
                    }.start();


                    if(bottomView.getVisibility() == View.GONE)
                    {

                        bottomView.setVisibility(View.VISIBLE);
                        bottomView.setFocusable(true);
                        //Text view accelerate up and 3s will fall down
                        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_scale_up_pivot_bottom_fade_in);
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                countdownRetractBottomBar.run();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        bottomView.startAnimation(animation);
                    }
                    else {
                        if(countDownTimer != null)
                        {
                            countDownTimer.cancel();
                            countdownRetractBottomBar.run();
                        }
                    }

                    pageText.setText((v + 1) + "/" + a);

                    lastReadingPage = v;


                    if(starredPages[pdfView.getCurrentPage()]) {
                        starButton.setImageResource(R.drawable.baseline_star_24);
                        starButton.setColorFilter(0xFFFFFF00, PorterDuff.Mode.SRC_ATOP);
                    }
                    else {
                        starButton.setImageResource(R.drawable.baseline_star_border_24);
                        starButton.setColorFilter(0xFF000000, PorterDuff.Mode.SRC_ATOP);
                    }
                }
        ).onLoad(v -> {
                    if(bookData != null && selectedChapter != null)
                    {
                        bookChapterInfo = new Account.AccountInformation.AccountData.AccountReadingProgress.LastBookChapter.BookChapterInfo(lastReadingPage, new boolean[pdfView.getPageCount()]);

                        Account.AccountInformation.AccountData.AccountReadingProgress.LastBookChapter bookChapter;
                        if(DynamicConstants.account.data.accountReadingProgress.bookChapterMap.containsKey(bookData.getBookId()))
                        {
                            bookChapter = DynamicConstants.account.data.accountReadingProgress.bookChapterMap.get(bookData.getBookId());
                            if(bookChapter != null)
                                if(bookChapter.chapterMap.get(selectedChapter.getChapterId()) != null)
                                    bookChapterInfo = bookChapter.chapterMap.get(selectedChapter.getChapterId());
                        }
                    }
                    starredPages = new boolean[pdfView.getPageCount()];
                    for (int i : bookChapterInfo.starredPages) {
                        starredPages[i] = true;
                    }
                }).load();



    }

    @Override
    public void finish() {
        super.finish();
        saveProgress();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveProgress();
    }

    void saveProgress()
    {
        long timeNow = new Date().getTime();
        long readingTimeElapsedMilli = timeNow - readingTimestamp;

        readingTimestamp = timeNow;

        if(bookData != null && selectedChapter != null)
        {
            Account.AccountInformation.AccountData.AccountReadingProgress.LastBookChapter.BookChapterInfo info =
                    new Account.AccountInformation.AccountData.AccountReadingProgress.LastBookChapter.BookChapterInfo(lastReadingPage, starredPages);

            Account.AccountInformation.AccountData.AccountReadingProgress.LastBookChapter bookChapter = null;
            if(DynamicConstants.account.data.accountReadingProgress.bookChapterMap.containsKey(bookData.getBookId()))
            {
                bookChapter = DynamicConstants.account.data.accountReadingProgress.bookChapterMap.get(bookData.getBookId());
                if(bookChapter != null)
                    bookChapter.chapterMap.put(selectedChapter.getChapterId(), info);
            }
            if(bookChapter == null)
                bookChapter = new Account.AccountInformation.AccountData.AccountReadingProgress.LastBookChapter(selectedChapter.getChapterId(), info);

            DynamicConstants.account.data.accountReadingProgress.bookChapterMap.put(bookData.getBookId(), bookChapter);

            int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            if(day == DynamicConstants.account.data.accountStatistics.lastReadingDay)
            {
                DynamicConstants.account.data.accountStatistics.todayReadTimeMilli += readingTimeElapsedMilli;
                DynamicConstants.account.data.accountStatistics.totalTimeReadLifetimeMilli += readingTimeElapsedMilli;

                if(DynamicConstants.account.data.accountStatistics.todayReadTimeMilli > Constants.MAX_TIME_TO_GET_STREAK_MILLI
                        && !DynamicConstants.account.data.accountStatistics.isReceivedStreak)
                {
                    DynamicConstants.account.data.accountStatistics.isReceivedStreak = true;
                    DynamicConstants.account.data.accountStatistics.streakCount += 1;
                    if(DynamicConstants.account.data.accountStatistics.streakCount > DynamicConstants.account.data.accountStatistics.highestStreakCount)
                    {
                        DynamicConstants.account.data.accountStatistics.highestStreakCount = DynamicConstants.account.data.accountStatistics.streakCount;
                    }
                }
            }
            else {
                DynamicConstants.account.data.accountStatistics.lastReadingDay = day;

                DynamicConstants.account.data.accountStatistics.isReceivedStreak = false;
                DynamicConstants.account.data.accountStatistics.todayReadTimeMilli = readingTimeElapsedMilli;
                DynamicConstants.account.data.accountStatistics.totalTimeReadLifetimeMilli += readingTimeElapsedMilli;
            }
        }

        DynamicConstants.account.saveAccountData(this);
    }

    private void fetchAndDisplayPdf(String pdfUrl) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // Open connection to the server
                URL url = new URL(pdfUrl);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                // Check if the connection is successful
                if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    InputStream inputStream = new BufferedInputStream(connection.getInputStream());

                    // Update the UI on the main thread
                    handler.post(() -> pdfView.fromStream(inputStream).load());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
