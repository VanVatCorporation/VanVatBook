package com.vanvatcorporation.vanvatsach.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vanvatcorporation.vanvatsach.R;
import com.vanvatcorporation.vanvatsach.constants.Constants;
import com.vanvatcorporation.vanvatsach.constants.DynamicConstants;
import com.vanvatcorporation.vanvatsach.constants.MethodConstants;
import com.vanvatcorporation.vanvatsach.helper.DateHelper;
import com.vanvatcorporation.vanvatsach.helper.IOHelper;
import com.vanvatcorporation.vanvatsach.helper.IOImageHelper;
import com.vanvatcorporation.vanvatsach.helper.ImageHelper;
import com.vanvatcorporation.vanvatsach.impl.AppCompatActivityImpl;
import com.vanvatcorporation.vanvatsach.impl.NavigationIconLayout;
import com.vanvatcorporation.vanvatsach.impl.ViewPagerImpl;
import com.vanvatcorporation.vanvatsach.impl.java.RunnableImpl;
import com.vanvatcorporation.vanvatsach.impl.java.RunnableImpl2;
import com.vanvatcorporation.vanvatsach.manager.LoggingManager;
import com.vanvatcorporation.vanvatsach.stuff.Account;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivityImpl {

    ViewPagerImpl viewPager;


    List<BookListData> bookList;
    RecyclerView bookListView;
    BookListAdapter adapter;
    //ProgressBar progressBarFetchingBook;
    SwipeRefreshLayout swipeRefreshLayout;


    List<BookListData> downloadedList;
    RecyclerView downloadedListView;
    BookListAdapter downloadedAdapter;
    SwipeRefreshLayout downloadedSwipeRefreshLayout;



    TextView readTimeTodayText;
    TextView readTimeForStreakTodayText;
    ProgressBar readTimeForStreakTodayBar;
    TextView streakText;
    ImageView streakIcon;
    SwipeRefreshLayout profileSwipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        MethodConstants.statusBarNoTitleTransition(this);

        setContentView(R.layout.layout_main);

        View homepageView = getLayoutInflater().inflate(R.layout.pager_main_homepage, null);
        View downloadedView = getLayoutInflater().inflate(R.layout.pager_main_downloaded, null);
        View View3 = getLayoutInflater().inflate(R.layout.pager_main_homepage, null);
        View View4 = getLayoutInflater().inflate(R.layout.pager_main_homepage, null);
        View profileView = getLayoutInflater().inflate(R.layout.pager_main_profile, null);


        View belowNavigationBar = findViewById(R.id.belowNavigationBar);

        View navigationButton1 = belowNavigationBar.findViewById(R.id.navigationElement1);
        View navigationButton2 = belowNavigationBar.findViewById(R.id.navigationElement2);
        View navigationButton3 = belowNavigationBar.findViewById(R.id.navigationElement3);
        View navigationButton4 = belowNavigationBar.findViewById(R.id.navigationElement4);
        View navigationButton5 = belowNavigationBar.findViewById(R.id.navigationElement5);

        View[] navigationButtons = {navigationButton1, navigationButton2, navigationButton3, navigationButton4, navigationButton5};





        ((NavigationIconLayout)navigationButton1).runAnimation(NavigationIconLayout.AnimationType.SELECTED);


        viewPager = findViewById(R.id.mainViewPager);
        viewPager.insertView(homepageView, downloadedView, View3, View4, profileView);
        viewPager.setupActions(
                new RunnableImpl2() {
                    @Override
                    public <T, T2> void runWithParam(T param, T2 param2) {
                        int lastPosition = (int) param;
                        int position = (int) param2;


                        ((NavigationIconLayout)navigationButtons[position]).runAnimation(NavigationIconLayout.AnimationType.SELECTED);
                        ((NavigationIconLayout)navigationButtons[lastPosition]).runAnimation(NavigationIconLayout.AnimationType.UNSELECTED);

                    }
                }
        );





        //Main View
        bookListView = homepageView.findViewById(R.id.bookList);
        //progressBarFetchingBook = view.findViewById(R.id.progressBarFetchingBook);
        swipeRefreshLayout = homepageView.findViewById(R.id.swipeRefreshLayout);
        bookListView.setLayoutManager(new LinearLayoutManager(this));

        bookList = new ArrayList<>();
        adapter = new BookListAdapter(this, bookList);
        bookListView.setAdapter(adapter);


        //Downloaded View
        downloadedListView = downloadedView.findViewById(R.id.bookList);
        //progressBarFetchingBook = view.findViewById(R.id.progressBarFetchingBook);
        downloadedSwipeRefreshLayout = downloadedView.findViewById(R.id.swipeRefreshLayout);
        downloadedListView.setLayoutManager(new LinearLayoutManager(this));

        downloadedList = new ArrayList<>();
        downloadedAdapter = new BookListAdapter(this, downloadedList);
        downloadedListView.setAdapter(downloadedAdapter);




        //Profile View
        readTimeTodayText = profileView.findViewById(R.id.readTimeToday);
        readTimeForStreakTodayText = profileView.findViewById(R.id.readTimeTodayForStreak);
        readTimeForStreakTodayBar = profileView.findViewById(R.id.readTimeProgress);
        streakText = profileView.findViewById(R.id.streakText);
        streakIcon = profileView.findViewById(R.id.streakFireImage);
        profileSwipeRefreshLayout = profileView.findViewById(R.id.swipeRefreshLayout);





        //Main View
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        swipeRefreshLayout.setRefreshing(true);
        refreshData();



        //Downloaded View
        downloadedSwipeRefreshLayout.setOnRefreshListener(this::refreshDataDownloaded);

        downloadedSwipeRefreshLayout.setRefreshing(true);
        refreshDataDownloaded();



        //Profile View
        profileSwipeRefreshLayout.setOnRefreshListener(this::refreshDataProfile);

        profileSwipeRefreshLayout.setRefreshing(true);
        refreshDataProfile();


        //Profile View
        TextView profileTitle = profileView.findViewById(R.id.textViewProfile);
        profileTitle.setText(DateHelper.getGreeting(this, (DynamicConstants.account.lastName + " " + DynamicConstants.account.firstName)));

        Button settingButton = profileView.findViewById(R.id.settingsButton);
        settingButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
        Button testingButton = profileView.findViewById(R.id.testingButton);
        testingButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, TestingActivity.class);
            startActivity(intent);
        });
        Button logOutButton = profileView.findViewById(R.id.logOutButton);
        logOutButton.setOnClickListener(v -> {
            Account.saveAccount(this, new Account.AccountInformation());
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });



        findViewById(R.id.navigationElement1).setOnClickListener(v -> {
            viewPager.setCurrentItem(0, true);
        });
        findViewById(R.id.navigationElement2).setOnClickListener(v -> {
            viewPager.setCurrentItem(1, true);
        });
        findViewById(R.id.navigationElement3).setOnClickListener(v -> {
            viewPager.setCurrentItem(2, true);
        });
        findViewById(R.id.navigationElement4).setOnClickListener(v -> {
            viewPager.setCurrentItem(3, true);
        });
        findViewById(R.id.navigationElement5).setOnClickListener(v -> {
            viewPager.setCurrentItem(4, true);
        });



    }

    @Override
    public void finish() {
        super.finish();
        DynamicConstants.account.saveAccountData(this);
    }

    @Override
    public void finishAffinity() {
        super.finishAffinity();
        DynamicConstants.account.saveAccountData(this);
    }

    void refreshData()
    {
        bookList.clear();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Looper.prepare();
            try {
                URL url = new URL("https://books.vanvatcorp.com/api/fetch-books");
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

//                BookListData[] bookListData = new Gson().fromJson(response.toString(), BookListData[].class);
//                bookList.addAll(Arrays.asList(bookListData));
                BookListData.BookDataFromServer[] serverData = new Gson().fromJson(response.toString(), BookListData.BookDataFromServer[].class);

                for (BookListData.BookDataFromServer data : serverData) {
                    if(data.bookAgeRestriction > DynamicConstants.account.age) continue;

                    bookList.add(new BookListData(
                            data.bookId,
                            data.bookTitle,
                            data.bookAuthor,
                            data.bookPublisher,
                            data.bookDescription,
                            data.bookUrl + "icon.png",
                            data.bookUrl + "background.png",
                            data.bookUrl,
                            data.bookChapters,
                            data.bookDate,
                            data.bookAgeRestriction
                    ));
                }

                runOnUiThread(() -> {
                    //progressBarFetchingBook.setVisibility(View.GONE);
                    //swipeRefreshLayout.setRefreshing(false);
                    adapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                LoggingManager.LogExceptionToNoteOverlay(this, e);
            }
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
            });
        });
    }


    void refreshDataDownloaded()
    {
        downloadedList.clear();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Looper.prepare();
            try {
                Map<Integer, BookListData> data = new Gson().fromJson(IOHelper.readFromFile(this, Constants.getTemporaryDownloadedBookDataPath(this)), new TypeToken<Map<Integer, BookListData>>() {}.getType());
                if(data != null)
                {
                    DynamicConstants.account.downloadedBook.clear();
                    for (BookListData dada : data.values()) {
                        dada.isOfflineResource = true;
                        for (BookListData.BookChapter chapter : dada.getBookChapters()) {
                            chapter.chapterOfflineUrl = IOHelper.CombinePath(IOHelper.getPersistentDataPath(this), String.valueOf(dada.getBookId()), String.valueOf(chapter.chapterId), "book.pdf");
                        }
                        downloadedList.add(dada);
                        DynamicConstants.account.downloadedBook.put(dada.getBookId(), dada);
                    }
                }
                runOnUiThread(() -> {
                    downloadedSwipeRefreshLayout.setRefreshing(false);
                    downloadedAdapter.notifyDataSetChanged();
                });

            } catch (Exception e) {

                runOnUiThread(() -> {
                    downloadedSwipeRefreshLayout.setRefreshing(false);
                });
                LoggingManager.LogExceptionToNoteOverlay(this, e);
            }
        });
    }

    void refreshDataProfile()
    {
        Account.AccountInformation.AccountData.AccountStatistics stats = DynamicConstants.account.data.accountStatistics;

        readTimeTodayText.setText((stats.todayReadTimeMilli / 60000) + " " + getString(R.string.unit_minutes_long));
        readTimeForStreakTodayText.setText((Constants.MAX_TIME_TO_GET_STREAK_MILLI / 60000) + " " + getString(R.string.unit_minutes_long));
        readTimeForStreakTodayBar.setMax((int) Constants.MAX_TIME_TO_GET_STREAK_MILLI);
        readTimeForStreakTodayBar.setProgress((int) stats.todayReadTimeMilli);

        if(stats.isReceivedStreak)
        {
            streakIcon.getDrawable().setColorFilter(0xFFFE8706, PorterDuff.Mode.SRC_ATOP);
        }
        else
        {
            streakIcon.getDrawable().setColorFilter(0xFF000000, PorterDuff.Mode.SRC_ATOP);
        }
        streakText.setText(getString(R.string.profile_streak) + ": " + stats.streakCount);

        profileSwipeRefreshLayout.setRefreshing(false);
    }

















    public static class BookListData implements Serializable {

        public static class BookChapter implements Serializable
        {
            private int chapterId = 0;
            private String chapterName = "-";
            private String chapterDescription = "-";
            private String chapterUrl = "-";
            private String chapterOfflineUrl = "-";
            private int chapterPages = 0;
            private int chapterSize = 0;
            private int chapterPriceVND = 0;

            public int getChapterId() {
                return chapterId;
            }
            public String getChapterName() {
                return chapterName;
            }
            public String getChapterDescription() {
                return chapterDescription;
            }
            public String getChapterPdfUrl() {
                return chapterUrl + "book.pdf";
            }
            public String getChapterUrl() {
                return chapterUrl;
            }
            public String getChapterOfflineUrl() {
                return chapterOfflineUrl;
            }
            public int getChapterPages() {
                return chapterPages;
            }
            public int getChapterSize() {
                return chapterSize;
            }
            public int getChapterPriceVND() {
                return chapterPriceVND;
            }

        }
        public static class BookDataFromServer {
            private int bookId;
            private String bookTitle;
            private String bookAuthor;
            private String bookPublisher;
            private String bookDescription;
            private BookChapter[] bookChapters;
            private String bookUrl;
            private long bookDate;
            private int bookAgeRestriction;
        }
        private boolean isOfflineResource = false;
        private int bookId;
        private String bookTitle;
        private String bookAuthor;
        private String bookPublisher;
        private String bookDescription;
        private String bookIconUrl;
        private String bookBackgroundUrl;
        private String bookUrl;
        private BookChapter[] bookChapters;
        private long bookDate;
        private int bookAgeRestriction;

        public BookListData(int bookId, String bookTitle, String bookAuthor, String bookPublisher, String bookDescription, String bookIconUrl, String bookBackgroundUrl, String bookUrl, BookChapter[] bookChapters, long bookDate, int bookAgeRestriction) {
            this.bookId = bookId;
            this.bookTitle = bookTitle;
            this.bookAuthor = bookAuthor;
            this.bookPublisher = bookPublisher;
            this.bookDescription = bookDescription;
            this.bookIconUrl = bookIconUrl;
            this.bookBackgroundUrl = bookBackgroundUrl;
            this.bookUrl = bookUrl;
            this.bookChapters = bookChapters;
            this.bookDate = bookDate;
            this.bookAgeRestriction = bookAgeRestriction;
        }


        public boolean isOffline() {
            return isOfflineResource;
        }
        public int getBookId() {
            return bookId;
        }
        public String getBookTitle() {
            return bookTitle;
        }

        public String getBookAuthor() {
            return bookAuthor;
        }
        public String getBookPublisher() {
            return bookPublisher;
        }

        public String getBookDescription() {
            return bookDescription;
        }

        public String getBookIconUrl() {
            return bookIconUrl;
        }
        public String getBookBackgroundUrl() {
            return bookBackgroundUrl;
        }
        public String getBookUrl() {
            return bookUrl;
        }

        public BookChapter[] getBookChapters() {
            return bookChapters;
        }
        public long getBookDate() {
            return bookDate;
        }




        public void setBookChapters(BookChapter[] bookChapters) {
            this.bookChapters = bookChapters;
        }


        @NonNull
        @Override
        protected Object clone() {
            return new BookListData(bookId, bookTitle, bookAuthor, bookPublisher, bookDescription, bookIconUrl, bookBackgroundUrl, bookUrl, bookChapters, bookDate, bookAgeRestriction);
        }
    }
    public static class BookListAdapter extends RecyclerView.Adapter<BookListViewHolder>
    {

        private List<BookListData> bookList;
        private Context context;

        // Constructor
        public BookListAdapter(Context context, List<BookListData> bookList) {
            this.context = context;
            this.bookList = bookList;
        }
        @Override
        public BookListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.cpn_book_list, parent, false);
            return new BookListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BookListViewHolder holder, int position) {

            BookListData bookItem = bookList.get(position);

            holder.bookTitle.setText(bookItem.getBookTitle());
            holder.bookAuthor.setText(bookItem.getBookAuthor());
            holder.bookDescription.setText(bookItem.getBookDescription());
            if(bookItem.isOffline()) {
                Bitmap iconBitmap = IOImageHelper.LoadFileAsPNGImage(context, IOHelper.CombinePath(IOHelper.getPersistentDataPath(context),
                        String.valueOf(bookItem.getBookId()), "icon.png"));
                if(iconBitmap != null)
                {
                    holder.bookIcon.setImageBitmap((iconBitmap));
                }
            }
            else {
                ImageHelper.getImageBitmapFromNetwork(context, bookItem.getBookIconUrl(), new RunnableImpl() {
                    @Override
                    public <T> void runWithParam(T param) {

                        Bitmap bitmap = (Bitmap) param;

                        // Set the upper view's background
                        holder.bookIcon.setImageBitmap((bitmap));

                    }
                });
            }
            holder.bookPublishingDate.setText(new Date(bookItem.getBookDate()).toString());

            holder.wholeView.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetailedBookActivity.class);
                intent.putExtra("BookListElement", bookItem);
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return bookList.size();
        }
    }
    public static class BookListViewHolder extends RecyclerView.ViewHolder {
        TextView bookTitle, bookAuthor, bookDescription, bookPublishingDate;
        ImageView bookIcon;
        View wholeView;
        public BookListViewHolder(@NonNull View itemView) {
            super(itemView);
            wholeView = itemView;

            bookTitle = itemView.findViewById(R.id.title);
            bookAuthor = itemView.findViewById(R.id.author);
            bookDescription = itemView.findViewById(R.id.description);
            bookPublishingDate = itemView.findViewById(R.id.date);
            bookIcon = itemView.findViewById(R.id.icon);
        }
    }
}


