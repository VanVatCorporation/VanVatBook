package com.vanvatcorporation.vanvatsach.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.vanvatcorporation.vanvatsach.R;
import com.vanvatcorporation.vanvatsach.constants.DynamicConstants;
import com.vanvatcorporation.vanvatsach.constants.MethodConstants;
import com.vanvatcorporation.vanvatsach.helper.AppSourceChecker;
import com.vanvatcorporation.vanvatsach.helper.EnumHelper;
import com.vanvatcorporation.vanvatsach.impl.AppCompatActivityImpl;
import com.vanvatcorporation.vanvatsach.impl.ProgressBarImpl;
import com.vanvatcorporation.vanvatsach.manager.LoggingManager;
import com.vanvatcorporation.vanvatsach.stuff.Account;

import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class SplashActivity extends AppCompatActivityImpl {

    ProgressBarImpl loadingProgressBar;
    ImageView icon;

    AppSourceChecker.InstallerMethod installerMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MethodConstants.statusBarNoTitleTransition(this);

        setContentView(R.layout.layout_splash);

        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        loadingProgressBar.setVisibility(View.GONE);

        icon = findViewById(R.id.imageView);

        animatingSplashActivity();

        installerMethod = AppSourceChecker.getSelfInstallerPackageMethod(this);

        DynamicConstants.account = Account.setupAccount(this);

    }

    void transitionToNextActivity() {
        Intent intent;
        if (installerMethod == AppSourceChecker.InstallerMethod.OTHER) {
            intent = new Intent(SplashActivity.this, WarningActivity.class);
            intent.putExtra("AccountValidate",
                    (DynamicConstants.account != null && DynamicConstants.account.isValid()));
        } else if (installerMethod == AppSourceChecker.InstallerMethod.UNKNOWN) {
            intent = new Intent(SplashActivity.this, WarningActivity.class);
            intent.putExtra("WarningType", EnumHelper.enumToString(installerMethod));
            intent.putExtra("AccountValidate",
                    (DynamicConstants.account != null && DynamicConstants.account.isValid()));
        } else if (DynamicConstants.account == null || !DynamicConstants.account.isValid(this)) {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        }
        startActivity(intent);
        finish(); // Finish SplashActivity so it's removed from the back stack
    }

    void animatingSplashActivity() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        loadingProgressBar.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_splash_progress_bar_unhide));

        // Delay for 2 seconds (2000 milliseconds) before starting the MainActivity
        new Handler().postDelayed(() -> {

            new CountDownTimer(1140, 200) {
                int tick = 0;

                @Override
                public void onTick(long millisUntilFinished) {
                    tick++;

                    switch (tick) {
                        case 1:
                            loadingProgressBar.setProgressAnimate(15);
                            break;
                        case 2:
                            loadingProgressBar.setProgressAnimate(67);
                            break;
                        case 3:
                            loadingProgressBar.setProgressAnimate(90);
                            break;
                        case 4:
                            loadingProgressBar.setProgressAnimate(99);
                            break;
                        case 5:
                            loadingProgressBar.setProgressAnimate(100);
                            break;
                    }

                }

                @Override
                public void onFinish() {
                    Animation animation = AnimationUtils.loadAnimation(SplashActivity.this,
                            R.anim.anim_splash_progress_bar_hide);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                            Animation animation1 = AnimationUtils.loadAnimation(SplashActivity.this,
                                    R.anim.anim_splash);

                            animation1.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    transitionToNextActivity();
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            icon.startAnimation(animation1);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    loadingProgressBar.startAnimation(animation);

                }
            }.start();

        }, 1000); // 2000 milliseconds

    }
}
