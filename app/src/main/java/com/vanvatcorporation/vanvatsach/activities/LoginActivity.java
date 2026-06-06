package com.vanvatcorporation.vanvatsach.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.Gson;
import com.vanvatcorporation.vanvatsach.R;
import com.vanvatcorporation.vanvatsach.constants.DynamicConstants;
import com.vanvatcorporation.vanvatsach.constants.MethodConstants;
import com.vanvatcorporation.vanvatsach.helper.ActivityHelper;
import com.vanvatcorporation.vanvatsach.impl.AppCompatActivityImpl;
import com.vanvatcorporation.vanvatsach.impl.ProgressBarImpl;
import com.vanvatcorporation.vanvatsach.manager.LoggingManager;
import com.vanvatcorporation.vanvatsach.stuff.Account;
import com.vanvatcorporation.vanvatsach.dynamiclibs.auth.AuthRepository;
import com.vanvatcorporation.vanvatsach.dynamiclibs.auth.User;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoginActivity extends AppCompatActivityImpl {
    ConstraintLayout loadingLayout;
    Button loginButton, registerButton;
    TextView textWarning;
    CheckBox savePasswordCheckBox;
    LinearProgressIndicator loadingProgressBar;
    CircularProgressIndicator progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MethodConstants.statusBarNoTitleTransition(this);

        setContentView(R.layout.layout_login);

        loadingLayout = findViewById(R.id.constraintLayoutLoading);
        loadingLayout.setVisibility(View.GONE);

        TextView[] warningTextView = {
                findViewById(R.id.textViewWarning),
                findViewById(R.id.textViewWarningPassword),
                findViewById(R.id.textViewWarningUsername)
        };

        if (!DynamicConstants.IS_PROCEED_UNKNOWN_OTHER_INSTALLATION) {
            for (TextView tw : warningTextView) {
                tw.setVisibility(View.GONE);
            }
        }

        /*
         *
         * CRITICAL WARNING! Username and password MUST be handled in the server side
         * code, not download entirely list and use client side to compare!
         *
         */

        EditText usernameText = findViewById(R.id.editTextTextEmailAddress);
        EditText passwordText = findViewById(R.id.editTextTextPassword);

        textWarning = findViewById(R.id.textViewWarning);
        savePasswordCheckBox = findViewById(R.id.savePasswordCheckBox);

        loginButton = findViewById(R.id.loginButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        progressBar = findViewById(R.id.progressBar);

        loginButton.setOnClickListener(v -> {
            loginButton.requestFocus();
            String usernameTextStr = usernameText.getText().toString();
            String passwordTextStr = passwordText.getText().toString();
            if (usernameTextStr.isEmpty() || passwordTextStr.isEmpty()) {
                return;
            }
            loginButton.setEnabled(false);

            loadingLayout.setVisibility(View.VISIBLE);
            loadingLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_fade_in));

            AuthRepository authRepository = AuthRepository.getInstance(this);
            authRepository.login(usernameTextStr, passwordTextStr, new AuthRepository.AuthCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    loadingProgressBar.setProgress(100);

                    Account.AccountInformation accountInformation = new Account.AccountInformation();
                    accountInformation.accountUsername = usernameTextStr;
                    accountInformation.accountEmail = user.getEmail();
                    accountInformation.accountUUID = user.getId();

                    DynamicConstants.account = Account.setupAccount(LoginActivity.this, accountInformation);
                    DynamicConstants.account.user = user;
                    DynamicConstants.account.valid = true;

                    if (savePasswordCheckBox.isChecked()) {
                        accountInformation.accountPassword = passwordTextStr;
                    }
                    Account.saveAccount(LoginActivity.this, accountInformation);

                    runOnUiThread(() -> {
                        Animation animation = AnimationUtils.loadAnimation(LoginActivity.this,
                                R.anim.anim_splash_progress_bar_hide);
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                Animation animation1 = AnimationUtils.loadAnimation(LoginActivity.this,
                                        R.anim.anim_fade_out);
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
                                loadingLayout.startAnimation(animation1);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        loadingProgressBar.startAnimation(animation);
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        loadingProgressBar.setProgress(80);
                        loginButton.setEnabled(true);
                        textWarning.setVisibility(View.VISIBLE);
                        textWarning.setText(message);
                    });
                }
            });

            ActivityHelper.hideKeyboard(this);
        });
        registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://account.vanvatcorp.com/register"));
            startActivity(browserIntent);
        });

    }

    void transitionToNextActivity() {
        Intent intent;
        intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Finish SplashActivity so it's removed from the back stack
    }

    // Redundant now as logic moved into click listener
    void animatingLoadingBar(ProgressBarImpl loadingProgressBar, String json) {
    }

    public static boolean checkAccountValid(Context context, Account account, String json) {

        String returnCode = "";
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            Future<Boolean> future = executor.submit(() -> {

                Looper.prepare();
                try {
                    boolean returnFlag;

                    URL url = new URL("https://books.vanvatcorp.com/api/login");
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json");

                    DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                    os.writeBytes(json);
                    os.flush();

                    // Get response code and handle response
                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        // HTTP_OK or 200 response code generally means that the server ran successfully
                        // without any errors
                        StringBuilder response = new StringBuilder();

                        // Read response content
                        // connection.getInputStream() purpose is to obtain an input stream for reading
                        // the server's response.
                        try (
                                BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(connection.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line); // Adds every line to response till the end of file.
                            }
                        }
                        System.out.println("LoginActivity checkAccountValid 1 Response: " + response.toString());

                        DynamicConstants.temporaryAccountInformation = new Gson().fromJson(json,
                                Account.AccountInformation.class);

                        Account.AccountInformation accountInformation = new Gson().fromJson(response.toString(),
                                Account.AccountInformation.class);
                        account.setupBasicInformation(accountInformation);

                        returnFlag = true;
                    } else {
                        System.out.println("LoginActivity Error: HTTP Response code - " + responseCode);

                        // HTTP_OK or 200 response code generally means that the server ran successfully
                        // without any errors
                        StringBuilder response = new StringBuilder();

                        // Read response content
                        // connection.getInputStream() purpose is to obtain an input stream for reading
                        // the server's response.
                        try (
                                BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(connection.getErrorStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line); // Adds every line to response till the end of file.
                            }
                        }
                        System.out.println("LoginActivity checkAccountValid 2 Response: " + response.toString());

                        returnFlag = false;
                    }
                    connection.disconnect();

                    return returnFlag;

                } catch (Exception e) {
                    LoggingManager.LogExceptionToNoteOverlay(context, e);

                    // Offline return
                    return true;
                }

            });

            return future.get();
        } catch (Exception e) {
            LoggingManager.LogExceptionToNoteOverlay(context, e);

            // Offline return
            return true;
        }
    }
}
