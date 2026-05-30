package com.vanvatcorporation.vanvatsach.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.vanvatcorporation.vanvatsach.R;
import com.vanvatcorporation.vanvatsach.constants.Constants;
import com.vanvatcorporation.vanvatsach.constants.DynamicConstants;
import com.vanvatcorporation.vanvatsach.constants.MethodConstants;
import com.vanvatcorporation.vanvatsach.helper.IOHelper;
import com.vanvatcorporation.vanvatsach.helper.IOImageHelper;
import com.vanvatcorporation.vanvatsach.helper.ImageHelper;
import com.vanvatcorporation.vanvatsach.impl.AppCompatActivityImpl;
import com.vanvatcorporation.vanvatsach.impl.ProgressBarImpl;
import com.vanvatcorporation.vanvatsach.impl.java.RunnableImpl;
import com.vanvatcorporation.vanvatsach.manager.LoggingManager;
import com.vanvatcorporation.vanvatsach.stuff.Account;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DetailedBookActivity extends AppCompatActivityImpl {

    ProgressBarImpl loadingBookProgressBar;
    TextView loadingBookProgressText;
    RelativeLayout progressBarLayout;
    Button readButton;
    Button buyButton;
    Button downloadButton;
    Button continueReadButton;
    Button deleteButton;
    SwipeRefreshLayout swipeRefreshLayout;

    DecimalFormat formatter;


    Map<Integer, String> bookChaptersName = new HashMap<>();


    boolean flagStopHttpConnection = false;
    boolean flagTerminate = false;

    MainActivity.BookListData.BookChapter selectingChapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        formatter = new DecimalFormat("#.#");

        MethodConstants.statusBarNoTitleTransition(this);

        setContentView(R.layout.layout_detailed);

        TextView title = findViewById(R.id.title);
        TextView bookInfo = findViewById(R.id.bookInfo);
        TextView description = findViewById(R.id.description);
        ShapeableImageView icon = findViewById(R.id.icon);
        ImageView background = findViewById(R.id.background);

        Spinner chapterSpinner = findViewById(R.id.chapterSpinner);

        loadingBookProgressBar = findViewById(R.id.loadingBookProgressBar);
        loadingBookProgressText = findViewById(R.id.loadingBookProgressText);

        progressBarLayout = findViewById(R.id.progressBarLayout);

        progressBarLayout.setVisibility(View.GONE);


        readButton = findViewById(R.id.readButton);
        continueReadButton = findViewById(R.id.continueReadButton);

        buyButton = findViewById(R.id.buyButton);
        downloadButton = findViewById(R.id.downloadButton);
        deleteButton = findViewById(R.id.deleteButton);


        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);


        if(getIntent() != null)
        {
            MainActivity.BookListData data = (MainActivity.BookListData) getIntent().getSerializableExtra("BookListElement");
            if(data != null)
            {
                if(data.getBookChapters() == null || data.getBookChapters().length == 0)
                {
                    data.setBookChapters(new MainActivity.BookListData.BookChapter[] { new MainActivity.BookListData.BookChapter()});
                }
                //String[] bookChaptersName = new String[data.getBookChapters().length];
//                for (int i = 0; i < bookChaptersName.length; i++) {
//                    bookChaptersName[i] = data.getBookChapters()[i].getChapterName();
//                }

                //Sort list once again for sure
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    ArrayList<MainActivity.BookListData.BookChapter> chapters = new ArrayList<>(Arrays.asList(data.getBookChapters()));
                    chapters.sort(Comparator.comparingInt(MainActivity.BookListData.BookChapter::getChapterId));
                    data.setBookChapters(chapters.toArray(new MainActivity.BookListData.BookChapter[0]));
                    }

                for (MainActivity.BookListData.BookChapter chapter : data.getBookChapters()) {
                    bookChaptersName.put(chapter.getChapterId(), chapter.getChapterName());
                }
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bookChaptersName.values().toArray(new String[0])); //selected item will look like a spinner set from XML
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                chapterSpinner.setAdapter(spinnerArrayAdapter);
                chapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        StringBuilder sb = new StringBuilder();
                        selectingChapter = data.getBookChapters()[position];
                        bookInfo.setText(sb.append(getString(R.string.detailed_pages))
                                .append(": ").append(selectingChapter.getChapterPages()).append("\n")
                                .append(getString(R.string.detailed_size)).append(": ").append(formatter.format(selectingChapter.getChapterSize() / 1048576D)).append("MB").toString());

                        reloadContinueReadingBook(data, selectingChapter);

                        readButton.setOnClickListener(v -> {
                            if(!data.isOffline())
                            {
                                readButton.setEnabled(false);
                                continueReadButton.setEnabled(false);
                            }
                            startReadingBook(data, selectingChapter);
                        });


                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                selectingChapter = data.getBookChapters()[0];

                title.setText(data.getBookTitle());
                description.setText(data.getBookDescription());
                if(data.isOffline())
                {
                    Bitmap iconBitmap = IOImageHelper.LoadFileAsPNGImage(this, IOHelper.CombinePath(IOHelper.getPersistentDataPath(this),
                            String.valueOf(data.getBookId()), "icon.png"));
                    if(iconBitmap != null)
                    {
                        icon.setImageBitmap((iconBitmap));
                    }



                    Bitmap backgroundBitmap = IOImageHelper.LoadFileAsPNGImage(this, IOHelper.CombinePath(IOHelper.getPersistentDataPath(this),
                            String.valueOf(data.getBookId()), "background.png"));
                    if(backgroundBitmap != null)
                    {
                        background.setImageBitmap((backgroundBitmap));
                    }
                }
                else {
                    ImageHelper.getImageBitmapFromNetwork(this, data.getBookIconUrl(), new RunnableImpl() {
                        @Override
                        public <T> void runWithParam(T param) {
                            Bitmap bitmap = (Bitmap) param;

                            // Set the upper view's background
                            icon.setImageBitmap((bitmap));
                        }
                    });
                    ImageHelper.getImageBitmapFromNetwork(this, data.getBookBackgroundUrl(), new RunnableImpl() {
                        @Override
                        public <T> void runWithParam(T param) {

                            Bitmap bitmap = (Bitmap) param;

                            // Set the upper view's background
                            background.setImageBitmap((bitmap));

                            // Generate synthesized colors for the lower view
//
//                        Palette.from(bitmap).generate(palette -> {
//                            // Extract colors from the Palette
//                            int dominantColor = palette.getDominantColor(Color.BLACK);
//                            int vibrantColor = palette.getVibrantColor(Color.GRAY);
//                            int mutedColor = palette.getMutedColor(Color.DKGRAY);
//
//                            // Create a gradient drawable programmatically
//                            GradientDrawable gradient = new GradientDrawable(
//                                    GradientDrawable.Orientation.TOP_BOTTOM,
//                                    new int[]{dominantColor, vibrantColor, mutedColor}
//                            );
//                            gradient.setGradientType(GradientDrawable.LINEAR_GRADIENT);
//
//                            // Set the gradient as the background of the lower view
//                            findViewById(R.id.backgroundDescription).setBackground(gradient);
//                        });
                        }

                    });
                }




                reloadContinueReadingBook(data, selectingChapter);



                StringBuilder sb = new StringBuilder();
                bookInfo.setText(sb.append(getString(R.string.detailed_pages))
                        .append(": ").append(selectingChapter.getChapterPages()).append("\n")
                        .append(getString(R.string.detailed_size)).append(": ").append(formatter.format(selectingChapter.getChapterSize() / 1048576D)).append("MB").toString());



                swipeRefreshLayout.setOnRefreshListener(() -> {
                    reloadContinueReadingBook(data, selectingChapter);
                });


                sb = new StringBuilder();
                buyButton.setText(sb.append(getString(R.string.detailed_buy)).append(": ").append(selectingChapter.getChapterPriceVND()).append("VND").toString());


            }
            else finish();
        }
        else finish();



    }

    void reloadContinueReadingBook(MainActivity.BookListData data, MainActivity.BookListData.BookChapter selectedChapter)
    {

        swipeRefreshLayout.setRefreshing(false);
        if(DynamicConstants.account.data.accountReadingProgress.bookChapterMap.containsKey(data.getBookId()))
        {
            Account.AccountInformation.AccountData.AccountReadingProgress.LastBookChapter lastBookChapter =
                    DynamicConstants.account.data.accountReadingProgress.bookChapterMap.get(data.getBookId());

            if(lastBookChapter != null)
            {
                for (int d : lastBookChapter.chapterMap.keySet()) {
                    if(d == selectedChapter.getChapterId())
                    {
                        Account.AccountInformation.AccountData.AccountReadingProgress.LastBookChapter.BookChapterInfo
                                bookChapterInfo = lastBookChapter.chapterMap.get(d);

                        if(bookChapterInfo != null)
                        {
                            int bookChapterLastRead = selectedChapter.getChapterId();

                            StringBuilder sb = new StringBuilder();
                            continueReadButton.setText(sb.append(getString(R.string.detailed_continue_reading)).append(": \n")
                                    .append(getString(R.string.detailed_chapter)).append(": ").append(bookChaptersName.get(bookChapterLastRead)).append("\n")
                                    .append(getString(R.string.detailed_pages)).append(": ").append(bookChapterInfo.lastPage + 1));

                            continueReadButton.setVisibility(View.VISIBLE);

                            continueReadButton.setOnClickListener(v -> {
                                if(!data.isOffline())
                                {
                                    readButton.setEnabled(false);
                                    continueReadButton.setEnabled(false);
                                }
                                ReadingSettings readingSettings = new ReadingSettings();
                                readingSettings.pages = bookChapterInfo.lastPage;
                                continueReadingBook(data, selectedChapter, readingSettings);
                            });
                            break;
                        }
                    }
                    else
                        continueReadButton.setVisibility(View.GONE);
                }
            }

        }
        else
            continueReadButton.setVisibility(View.GONE);


        if(!data.isOffline())
        {
            if(DynamicConstants.account.data.boughtAssets.bookChapterBought.containsKey(data.getBookId()))
            {
                boolean flag = false;
                for (int boughtBookChapter : DynamicConstants.account.data.boughtAssets.bookChapterBought.get(data.getBookId())) {

                    if(boughtBookChapter == selectedChapter.getChapterId())
                    {
                        downloadButton.setVisibility(View.VISIBLE);
                        deleteButton.setVisibility(View.GONE);
                        buyButton.setVisibility(View.GONE);

                        downloadButton.setOnClickListener(v -> {
                            downloadButton.setEnabled(false);
                            downloadChapter(this, data, selectedChapter);
                        });

                        flag = true;
                        break;
                    }
                }
                if(!flag)
                {
                    buyButton.setVisibility(View.VISIBLE);
                    downloadButton.setVisibility(View.GONE);
                    deleteButton.setVisibility(View.GONE);
                }

            }
            else
            {
                buyButton.setVisibility(View.VISIBLE);
                downloadButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
            }
            buyButton.setText(getString(R.string.detailed_buy) + ": " + selectingChapter.getChapterPriceVND() + "VND");
            buyButton.setOnClickListener(v -> {
                buyChapter(this, data, selectedChapter);
            });
        }
        else {
            buyButton.setVisibility(View.GONE);
            downloadButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.VISIBLE);

            deleteButton.setOnClickListener(v -> {
                deleteChapter(this, data);
            });
        }


    }



    @Override
    public void finish() {
        super.finish();
        flagStopHttpConnection = true;
        flagTerminate = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        flagStopHttpConnection = true;
        flagTerminate = true;
    }






    public void buyChapter(Context context, MainActivity.BookListData data, MainActivity.BookListData.BookChapter selectedChapter)
    {
        Account.AccountInformation.AccountData dataLoaded;
        try
        {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Account.AccountInformation.AccountData> future = executor.submit(() -> {

                Account.AccountInformation.AccountData accountData = null;
                try {
                    JsonObject object = new JsonObject();
                    object.add("accountUsername", new JsonPrimitive(DynamicConstants.temporaryAccountInformation.accountUsername));
                    object.add("accountPassword", new JsonPrimitive(DynamicConstants.temporaryAccountInformation.accountPassword));

                    URL url = new URL("https://books.vanvatcorp.com/api/buy-assets/" + data.getBookId() + "/" + selectedChapter.getChapterId());
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json");

                    DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                    os.writeBytes(object.toString());
                    os.flush();


                    // Get response code and handle response
                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        // HTTP_OK or 200 response code generally means that the server ran successfully without any errors
                        StringBuilder response = new StringBuilder();

                        // Read response content
                        // connection.getInputStream() purpose is to obtain an input stream for reading the server's response.
                        try (
                                BufferedReader reader = new BufferedReader( new InputStreamReader(connection.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line); // Adds every line to response till the end of file.
                            }
                        }
                        System.out.println("Response: " + response.toString());


                        JsonObject jsonObject = (JsonObject) JsonParser.parseString(String.valueOf(response));
                        String str = jsonObject.get("accountData").toString();
                        accountData = new Gson().fromJson(str, Account.AccountInformation.AccountData.class);
                    }
                    else {
                        System.out.println("Error: HTTP Response code - " + responseCode);
                        // HTTP_OK or 200 response code generally means that the server ran successfully without any errors
                        StringBuilder response = new StringBuilder();

                        // Read response content
                        // connection.getInputStream() purpose is to obtain an input stream for reading the server's response.
                        try (
                                BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getErrorStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line); // Adds every line to response till the end of file.
                            }
                        }
                        System.out.println("Response: " + response.toString());
                    }
                    connection.disconnect();
                }
                catch (Exception e)
                {
                    LoggingManager.LogExceptionToNoteOverlay(context, e);
                }

                runOnUiThread(() -> reloadContinueReadingBook(data, selectingChapter));
                return accountData;

            });

            // Main thread waits for result
            dataLoaded = future.get(); // Blocks until the background thread finishes



            executor.shutdown();
        }
        catch (Exception e)
        {
            dataLoaded = null;
            LoggingManager.LogExceptionToNoteOverlay(context, e);
        }



        DynamicConstants.account.data = dataLoaded != null ? dataLoaded : DynamicConstants.account.data;
    }



    public void downloadChapter(Context context, MainActivity.BookListData data, MainActivity.BookListData.BookChapter selectedChapter)
    {
        Account.AccountInformation.AccountData dataLoaded;
        try
        {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Account.AccountInformation.AccountData> future = executor.submit(() -> {

                Account.AccountInformation.AccountData accountData = null;
                try {
                    JsonObject object = new JsonObject();
                    object.add("accountUsername", new JsonPrimitive(DynamicConstants.temporaryAccountInformation.accountUsername));
                    object.add("accountPassword", new JsonPrimitive(DynamicConstants.temporaryAccountInformation.accountPassword));

                    URL url = new URL("https://books.vanvatcorp.com/api/download-assets/" + data.getBookId() + "/" + selectedChapter.getChapterId());
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json");

                    DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                    os.writeBytes(object.toString());
                    os.flush();


                    // Get response code and handle response
                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        // HTTP_OK or 200 response code generally means that the server ran successfully without any errors
                        StringBuilder response = new StringBuilder();

                        // Read response content
                        // connection.getInputStream() purpose is to obtain an input stream for reading the server's response.
                        try (
                                BufferedReader reader = new BufferedReader( new InputStreamReader(connection.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line); // Adds every line to response till the end of file.
                            }
                        }
                        System.out.println("Response: " + response.toString());

                        IOHelper.writeToFile(this, IOHelper.CombinePath(IOHelper.getPersistentDataPath(context),
                                String.valueOf(data.getBookId()), String.valueOf(selectedChapter.getChapterId()), "book.pdf"), "1");
                        IOHelper.writeToFile(this, IOHelper.CombinePath(IOHelper.getPersistentDataPath(context),
                                String.valueOf(data.getBookId()), String.valueOf(selectedChapter.getChapterId()), "data.txt"), "1");
                        IOHelper.writeToFile(this, IOHelper.CombinePath(IOHelper.getPersistentDataPath(context),
                                String.valueOf(data.getBookId()), String.valueOf(selectedChapter.getChapterId()), "chapter.txt"), "1");


                        IOHelper.writeToFile(this, IOHelper.CombinePath(IOHelper.getPersistentDataPath(context),
                                String.valueOf(data.getBookId()), "icon.png"), "1");
                        IOHelper.writeToFile(this, IOHelper.CombinePath(IOHelper.getPersistentDataPath(context),
                                String.valueOf(data.getBookId()), "background.png"), "1");


                        ImageHelper.getImageBitmapFromNetwork(this, data.getBookIconUrl(), new RunnableImpl() {
                            @Override
                            public <T> void runWithParam(T param) {

                                Bitmap bitmap = (Bitmap) param;
                                IOImageHelper.SaveFileAsPNGImage(context, IOHelper.CombinePath(IOHelper.getPersistentDataPath(context),
                                        String.valueOf(data.getBookId()), "icon.png"), bitmap);
                            }
                        });
                        ImageHelper.getImageBitmapFromNetwork(this, data.getBookBackgroundUrl(), new RunnableImpl() {
                            @Override
                            public <T> void runWithParam(T param) {

                                Bitmap bitmap = (Bitmap) param;

                                // Set the upper view's background
                                IOImageHelper.SaveFileAsPNGImage(context, IOHelper.CombinePath(IOHelper.getPersistentDataPath(context),
                                        String.valueOf(data.getBookId()), "background.png"), bitmap);

                                // Generate synthesized colors for the lower view
//
//                        Palette.from(bitmap).generate(palette -> {
//                            // Extract colors from the Palette
//                            int dominantColor = palette.getDominantColor(Color.BLACK);
//                            int vibrantColor = palette.getVibrantColor(Color.GRAY);
//                            int mutedColor = palette.getMutedColor(Color.DKGRAY);
//
//                            // Create a gradient drawable programmatically
//                            GradientDrawable gradient = new GradientDrawable(
//                                    GradientDrawable.Orientation.TOP_BOTTOM,
//                                    new int[]{dominantColor, vibrantColor, mutedColor}
//                            );
//                            gradient.setGradientType(GradientDrawable.LINEAR_GRADIENT);
//
//                            // Set the gradient as the background of the lower view
//                            findViewById(R.id.backgroundDescription).setBackground(gradient);
//                        });
                            }

                        });

                        //TODO: Temporary
                        fetchBookFromServer(selectedChapter, IOHelper.CombinePath(IOHelper.getPersistentDataPath(context),
                                String.valueOf(data.getBookId()), String.valueOf(selectedChapter.getChapterId()), "book.pdf"), data, new ReadingSettings(), () -> {
                            runOnUiThread(() -> {
                                loadingBookProgressBar.setProgress(0);
                                loadingBookProgressText.setText("-");
                                downloadProgress = 0;
                                totalBytesRead = 0;
                                readButton.setEnabled(true);
                                continueReadButton.setEnabled(true);
                                downloadButton.setEnabled(true);

                                progressBarLayout.startAnimation(AnimationUtils.loadAnimation(DetailedBookActivity.this, R.anim.anim_scale_down_pivot_bottom_fade_out));
                            });
                        });

                        if(DynamicConstants.account.downloadedBook.containsKey(data.getBookId()))
                        {
                            ArrayList<MainActivity.BookListData.BookChapter> chapters = new ArrayList<>(Arrays.asList(DynamicConstants.account.downloadedBook.get(data.getBookId()).getBookChapters()));

                            if(!chapters.contains(selectedChapter))
                                chapters.add(selectedChapter);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                chapters.sort(Comparator.comparingInt(MainActivity.BookListData.BookChapter::getChapterId));
                            }

                            if(DynamicConstants.account.downloadedBook.get(data.getBookId()) != null)
                                DynamicConstants.account.downloadedBook.get(data.getBookId()).setBookChapters(chapters.toArray(new MainActivity.BookListData.BookChapter[0]));
                        }
                        else {
                            MainActivity.BookListData dataClone = (MainActivity.BookListData) data.clone();
                            dataClone.setBookChapters(new MainActivity.BookListData.BookChapter[]{selectedChapter});
                            DynamicConstants.account.downloadedBook.put(dataClone.getBookId(), dataClone);
                        }

                        IOHelper.writeToFile(this, Constants.getTemporaryDownloadedBookDataPath(this), new Gson().toJson(DynamicConstants.account.downloadedBook));
                        IOHelper.writeToFile(this, IOHelper.CombinePath(IOHelper.getPersistentDataPath(context),
                                String.valueOf(data.getBookId()), String.valueOf(selectedChapter.getChapterId()), "data.txt"), new Gson().toJson(data));
                        IOHelper.writeToFile(this, IOHelper.CombinePath(IOHelper.getPersistentDataPath(context),
                                String.valueOf(data.getBookId()), String.valueOf(selectedChapter.getChapterId()), "chapter.txt"), new Gson().toJson(selectedChapter));
                        //LoggingManager.LogToToast(this, IOHelper.CombinePath(String.valueOf(data.getBookId()), String.valueOf(selectedChapter.getChapterId())));
                    }
                    else {
                        System.out.println("Error: HTTP Response code - " + responseCode);
                        // HTTP_OK or 200 response code generally means that the server ran successfully without any errors
                        StringBuilder response = new StringBuilder();

                        // Read response content
                        // connection.getInputStream() purpose is to obtain an input stream for reading the server's response.
                        try (
                                BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getErrorStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line); // Adds every line to response till the end of file.
                            }
                        }
                        System.out.println("Response: " + response.toString());
                    }
                    connection.disconnect();
                }
                catch (Exception e)
                {
                    LoggingManager.LogExceptionToNoteOverlay(context, e);
                }

                return accountData;

            });

            // Main thread waits for result
            dataLoaded = future.get(); // Blocks until the background thread finishes


            reloadContinueReadingBook(data, selectingChapter);

            executor.shutdown();
        }
        catch (Exception e)
        {
            LoggingManager.LogExceptionToNoteOverlay(context, e);
        }
    }
    public void deleteChapter(Context context, MainActivity.BookListData data)
    {
        try {
            if(DynamicConstants.account.downloadedBook.containsKey(data.getBookId()))
            {
                ArrayList<MainActivity.BookListData.BookChapter> chapters = new ArrayList<>(Arrays.asList(DynamicConstants.account.downloadedBook.get(data.getBookId()).getBookChapters()));

                for (MainActivity.BookListData.BookChapter chapter : chapters) {
                    if(chapter.getChapterId() == selectingChapter.getChapterId())
                    {
                        chapters.remove(chapter);
                        break;
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    chapters.sort(Comparator.comparingInt(MainActivity.BookListData.BookChapter::getChapterId));
                }

                if(DynamicConstants.account.downloadedBook.get(data.getBookId()) != null)
                    DynamicConstants.account.downloadedBook.get(data.getBookId()).setBookChapters(chapters.toArray(new MainActivity.BookListData.BookChapter[0]));


                IOHelper.writeToFile(this, Constants.getTemporaryDownloadedBookDataPath(this), new Gson().toJson(DynamicConstants.account.downloadedBook));
                IOHelper.deleteDir(IOHelper.CombinePath(IOHelper.getPersistentDataPath(context),
                                String.valueOf(data.getBookId()), String.valueOf(selectingChapter.getChapterId())));
                LoggingManager.LogToToast(this, "Done!");
            }
            else {
                //Nah, we didn't downloaded the chapter in the first place!

                LoggingManager.LogToToast(this, "Invalid chapter!");
            }

            reloadContinueReadingBook(data, selectingChapter);
        }
        catch (Exception e)
        {
            LoggingManager.LogExceptionToNoteOverlay(context, e);
        }
    }















    void startReadingBook(MainActivity.BookListData data, MainActivity.BookListData.BookChapter selectedChapter)
    {
        if(data.isOffline())
            transitionToNextActivity(data, selectedChapter, new ReadingSettings());
        else
            fetchBookFromServer(selectedChapter, Constants.getTemporaryBookPath(this), data, new ReadingSettings());
    }
    void continueReadingBook(MainActivity.BookListData data, MainActivity.BookListData.BookChapter selectedChapter, ReadingSettings readingSettings)
    {
        if(data.isOffline())
            transitionToNextActivity(data, selectedChapter, readingSettings);
        else
            fetchBookFromServer(selectedChapter, Constants.getTemporaryBookPath(this), data, readingSettings);
    }


    double downloadProgress = 0;
    int totalBytesRead = 0;
    private void fetchBookFromServer(MainActivity.BookListData.BookChapter selectedChapter, String savePath, MainActivity.BookListData data, ReadingSettings settings) {
        fetchBookFromServer(selectedChapter, savePath, data, settings, () -> runOnUiThread(() -> {

            Animation animation = AnimationUtils.loadAnimation(DetailedBookActivity.this, R.anim.anim_scale_down_pivot_bottom_fade_out);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    progressBarLayout.setVisibility(View.GONE);
                    transitionToNextActivity(data, selectedChapter, settings);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            progressBarLayout.startAnimation(animation);
        }));
    }

        private void fetchBookFromServer(MainActivity.BookListData.BookChapter selectedChapter, String savePath, MainActivity.BookListData data, ReadingSettings settings, Runnable onDoneAction) {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());


        runOnUiThread(() -> {
            progressBarLayout.setVisibility(View.VISIBLE);
            progressBarLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_scale_up_pivot_bottom_fade_in));

        });
        executor.execute(() -> {
            Looper.prepare();
            try {
                // Open connection to the server
                URL url = new URL(selectedChapter.getChapterPdfUrl());
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                // Get the content length
                int contentLength = connection.getContentLength();
                if (contentLength == -1) {
                    //System.out.println("Unable to determine file size.");
                    loadingBookProgressText.setText("Unable to determine file size.");
                }

                connection.setRequestMethod("GET");
                connection.connect();

                // Check if the connection is successful
                if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(savePath);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    Runnable runnable = new Runnable() {
                        int tick = 0;
                        @Override
                        public void run() {
                            tick++;
                            loadingBookProgressBar.setProgressAnimate((int) downloadProgress);
                            loadingBookProgressText.setText(String.format("%1$sMB/%2$sMB (%3$skB/%4$skB) [%5$sMB/s (%6$skB/s)]", formatter.format(totalBytesRead / 1048576D), formatter.format(contentLength / 1048576D), (totalBytesRead / 1024), (contentLength / 1024), formatter.format(totalBytesRead / 1048576D / (tick / 2d)), formatter.format(totalBytesRead / 1024d / (tick / 2d))));
                            handler.postDelayed(this, 500);
                        }
                    };
                    handler.post(runnable);

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        if(flagStopHttpConnection) break;
                        totalBytesRead += bytesRead;
                        outputStream.write(buffer, 0, bytesRead);

                        // Calculate progress
                        downloadProgress = ((double) totalBytesRead / contentLength) * 100;
                        //System.out.printf("Progress: %.2f%%%n", progress);
                    }
                    //System.out.println("Download complete!");
                    inputStream.close();
                    outputStream.close();
                    handler.removeCallbacks(runnable);

                    if(flagTerminate) return;

                    onDoneAction.run();


                }
            } catch (Exception e) {
                onDoneAction.run();
                LoggingManager.LogExceptionToNoteOverlay(this, e);
            }
        });
    }





    void transitionToNextActivity(MainActivity.BookListData data, MainActivity.BookListData.BookChapter selectedChapter, ReadingSettings readingSettings)
    {
        loadingBookProgressBar.setProgress(0);
        loadingBookProgressText.setText("-");
        downloadProgress = 0;
        totalBytesRead = 0;
        readButton.setEnabled(true);
        continueReadButton.setEnabled(true);
        Intent intent = new Intent(this, ReadingActivity.class);
        intent.putExtra("BookListElement", data);
        intent.putExtra("SelectedChapter", selectedChapter);
        intent.putExtra("ReadingSettings", readingSettings);
        if(data.isOffline())
        {
            intent.putExtra("OverridePdfPath", selectedChapter.getChapterOfflineUrl());
        }
        startActivity(intent);
    }



    public static class ReadingSettings implements Serializable {
        public int pages = 0;
    }

}


