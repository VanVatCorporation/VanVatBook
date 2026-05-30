package com.vanvatcorporation.vanvatsach.stuff;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.vanvatcorporation.vanvatsach.activities.LoginActivity;
import com.vanvatcorporation.vanvatsach.activities.MainActivity;
import com.vanvatcorporation.vanvatsach.constants.DynamicConstants;
import com.vanvatcorporation.vanvatsach.helper.DateHelper;
import com.vanvatcorporation.vanvatsach.helper.NetworkHelper;
import com.vanvatcorporation.vanvatsach.helper.ObjectPrimitiveHelper;
import com.vanvatcorporation.vanvatsach.manager.LoggingManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Account implements Serializable {
    /**
     * The uuid is get from login process, in which it save to the storage of the device. If I can read the uuid then it retrieve only the username of the login process.
     * Hacker modifies the code to retrieve password instead? Change the server-side code to only send username
     */
    public String uuid;
    public String username;

    public String firstName;
    public String lastName;
    public long legalBirthday;
    public AccountInformation.GenderType gender;


    //Account only, no need to fetch so a no in AccountInformation
    public int age;


    public Map<Integer, MainActivity.BookListData> downloadedBook = new HashMap<>();

    boolean valid;

    public AccountInformation.AccountData data;


    public boolean isValid()
    {
        return valid;
    }


    public static class SharedPreferencesKeys
    {
        public static final String ACCOUNT_USERNAME = "ACCOUNT_USERNAME";
        public static final String ACCOUNT_PASSWORD = "ACCOUNT_PASSWORD";
        public static final String ACCOUNT_EMAIL = "ACCOUNT_EMAIL";
        public static final String ACCOUNT_UUID = "ACCOUNT_UUID";
        public static final String ACCOUNT_DATA_JSON = "ACCOUNT_DATA_JSON";

        public static final String ACCOUNT_FIRST_NAME = "ACCOUNT_FIRST_NAME";
        public static final String ACCOUNT_LAST_NAME = "ACCOUNT_LAST_NAME";
        public static final String ACCOUNT_LEGAL_BIRTHDAY = "ACCOUNT_LEGAL_BIRTHDAY";
        public static final String ACCOUNT_GENDER = "ACCOUNT_GENDER";
    }
    public void setupBasicInformation(AccountInformation accountInformation)
    {
        firstName = accountInformation.firstName;
        lastName = accountInformation.lastName;
        legalBirthday = accountInformation.legalBirthday;
        gender = AccountInformation.GenderType.values()[accountInformation.gender];
    }


    public static Account setupAccount(Context context) {
        AccountInformation accountInformation = new AccountInformation();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs.contains(SharedPreferencesKeys.ACCOUNT_USERNAME))
            accountInformation.accountUsername = prefs.getString(SharedPreferencesKeys.ACCOUNT_USERNAME, "");
        if(prefs.contains(SharedPreferencesKeys.ACCOUNT_PASSWORD))
            accountInformation.accountPassword = prefs.getString(SharedPreferencesKeys.ACCOUNT_PASSWORD, "");
        if(prefs.contains(SharedPreferencesKeys.ACCOUNT_EMAIL))
            accountInformation.accountEmail = prefs.getString(SharedPreferencesKeys.ACCOUNT_EMAIL, "");
        if(prefs.contains(SharedPreferencesKeys.ACCOUNT_UUID))
            accountInformation.accountUUID = prefs.getString(SharedPreferencesKeys.ACCOUNT_UUID, "");
        if(prefs.contains(SharedPreferencesKeys.ACCOUNT_DATA_JSON))
            accountInformation.accountDataJson = prefs.getString(SharedPreferencesKeys.ACCOUNT_DATA_JSON, "");


        if(prefs.contains(SharedPreferencesKeys.ACCOUNT_FIRST_NAME))
            accountInformation.firstName = prefs.getString(SharedPreferencesKeys.ACCOUNT_FIRST_NAME, "");
        if(prefs.contains(SharedPreferencesKeys.ACCOUNT_LAST_NAME))
            accountInformation.lastName = prefs.getString(SharedPreferencesKeys.ACCOUNT_LAST_NAME, "");
        if(prefs.contains(SharedPreferencesKeys.ACCOUNT_LEGAL_BIRTHDAY))
            accountInformation.legalBirthday = prefs.getLong(SharedPreferencesKeys.ACCOUNT_LEGAL_BIRTHDAY, 1);
        if(prefs.contains(SharedPreferencesKeys.ACCOUNT_GENDER))
            accountInformation.gender = prefs.getInt(SharedPreferencesKeys.ACCOUNT_GENDER, 0);


        if(accountInformation.accountUUID == null || accountInformation.accountUsername == null || accountInformation.accountPassword == null || accountInformation.accountEmail == null)
            return null;
        if(accountInformation.accountUUID.isEmpty() || accountInformation.accountUsername.isEmpty() || accountInformation.accountPassword.isEmpty() || accountInformation.accountEmail.isEmpty())
            return null;

        return Account.login(context, accountInformation);
    }
    public static Account setupAccount(Context context, AccountInformation accountInformation) {
        Account account = new Account();
        account.username = accountInformation.accountUsername;
        account.uuid = accountInformation.accountUUID;
        account.data = new Gson().fromJson(accountInformation.accountDataJson, AccountInformation.AccountData.class);

        account.firstName = accountInformation.firstName;
        account.lastName = accountInformation.lastName;
        account.legalBirthday = accountInformation.legalBirthday;
        account.gender = AccountInformation.GenderType.values()[accountInformation.gender];

        account.age = DateHelper.getDeltaOfYearTimeWithCurrentYearTime(DateHelper.getYearOfTimestamp(account.legalBirthday));


        account.valid = LoginActivity.checkAccountValid(context, account, new Gson().toJson(accountInformation));//!Objects.equals(accountInformation.accountPassword, "-");

//        if(account.data == null)
//        {
//            account.data = new AccountInformation.AccountData();
//
//
//            AccountInformation.AccountData dataLoaded = loadAccountDataOnline(context);
//
//            account.data = dataLoaded != null ? dataLoaded : account.data;
//        }

        if(NetworkHelper.isNetworkAvailable(context))
        {
            AccountInformation.AccountData dataLoaded = account.loadAccountDataOnline(context);

            if(dataLoaded != null)
            {
                if(account.data == null || dataLoaded.timeSaved >= account.data.timeSaved)
                    account.data = dataLoaded;
            }
        }


        return account;
    }
    public static void saveAccount(Context context, AccountInformation account)
    {
        // Obtain the shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Create an editor to write to the shared preferences
        SharedPreferences.Editor editor = prefs.edit();

        // Add some key-value pairs to the editor
        editor.putString(SharedPreferencesKeys.ACCOUNT_USERNAME, account.accountUsername);
        editor.putString(SharedPreferencesKeys.ACCOUNT_PASSWORD, account.accountPassword);
        editor.putString(SharedPreferencesKeys.ACCOUNT_EMAIL, account.accountEmail);
        editor.putString(SharedPreferencesKeys.ACCOUNT_UUID, account.accountUUID);


        editor.putString(SharedPreferencesKeys.ACCOUNT_FIRST_NAME, account.firstName);
        editor.putString(SharedPreferencesKeys.ACCOUNT_LAST_NAME, account.lastName);
        editor.putLong(SharedPreferencesKeys.ACCOUNT_LEGAL_BIRTHDAY, account.legalBirthday);
        editor.putInt(SharedPreferencesKeys.ACCOUNT_GENDER, account.gender);

        // Apply the changes
        editor.apply();
    }
    public void saveAccountData(Context context)
    {
        // Obtain the shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Create an editor to write to the shared preferences
        SharedPreferences.Editor editor = prefs.edit();

        data.timeSaved = Calendar.getInstance().getTimeInMillis();
        // Add some key-value pairs to the editor
        editor.putString(SharedPreferencesKeys.ACCOUNT_DATA_JSON, new Gson().toJson(data));

        // Apply the changes
        editor.apply();

        saveAccountDataOnline(context);
    }
    void saveAccountDataOnline(Context context)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    //Client bought assets should be deleted
                    Account.AccountInformation.AccountData dataClone = new Gson().fromJson(new Gson().toJson(data), AccountInformation.AccountData.class);
                    dataClone.boughtAssets = null;


                    JsonObject accountStatistics = new JsonObject();

                    accountStatistics.add("accountStatistics", JsonParser.parseString((new Gson().toJson(dataClone.accountStatistics))));

                    JsonObject object = new JsonObject();
                    object.add("accountUsername", new JsonPrimitive(DynamicConstants.temporaryAccountInformation.accountUsername));
                    object.add("accountPassword", new JsonPrimitive(DynamicConstants.temporaryAccountInformation.accountPassword));
                    object.add("accountDataJson", new JsonPrimitive(new Gson().toJson(dataClone)));
                    object.add("accountStatisticsJson", new JsonPrimitive(new Gson().toJson(accountStatistics)));

                    URL url = new URL("https://books.vanvatcorp.com/api/upload-account-data");
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

            }
        };
        NetworkHelper.networkRunnable(context, runnable);
    }





    AccountInformation.AccountData loadAccountDataOnline(Context context)
    {
        AccountInformation.AccountData dataLoaded;
        try
        {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<AccountInformation.AccountData> future = executor.submit(() -> {

                AccountInformation.AccountData data = null;
                try {
                    JsonObject object = new JsonObject();
                    object.add("accountUsername", new JsonPrimitive(DynamicConstants.temporaryAccountInformation.accountUsername));
                    object.add("accountPassword", new JsonPrimitive(DynamicConstants.temporaryAccountInformation.accountPassword));

                    URL url = new URL("https://books.vanvatcorp.com/api/download-account-data");
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
                        data = new Gson().fromJson(str, AccountInformation.AccountData.class);
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

                return data;

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


        return dataLoaded;
    }

    public static Account login(Context context, AccountInformation account)
    {
        return setupAccount(context, account);
    }

    public static class AccountInformation implements Serializable
    {
        public String accountUUID = "";
        public String accountUsername = "";
        public String accountPassword = "";
        public String accountEmail = "";
        public String accountDataJson = "";


        public String firstName = "nerds";
        public String lastName = "fellow";
        public long legalBirthday = new Date().getTime();
        public int gender;


        public enum GenderType {
            MALE, FEMALE, OTHER
        }

        public static class AccountData implements Serializable
        {
            public static class AccountReadingProgress implements Serializable
            {
                public static class LastBookChapter
                {

                    public static class BookChapterInfo
                    {
                        public int lastPage = 1;
                        public int[] starredPages = new int[1];

                        public BookChapterInfo(int lastPage, int[] starredPages)
                        {
                            this.lastPage = lastPage;
                            this.starredPages = starredPages;
                        }
                        public BookChapterInfo(int lastPage, boolean[] starredPages)
                        {
                            this.lastPage = lastPage;
                            ArrayList<Integer> starredP = new ArrayList<>();
                            for (int i = 0; i < starredPages.length; i++) {
                                boolean starred = starredPages[i];
                                if(starred)
                                    starredP.add(i);


                            }
                            this.starredPages = ObjectPrimitiveHelper.toPrimitives(starredP.toArray(new Integer[0]));
                        }
                        public BookChapterInfo()
                        {

                        }
                    }
                    public Map<Integer, BookChapterInfo> chapterMap = new HashMap<>();

                    public LastBookChapter(int lastReadingChapterId, BookChapterInfo chapterInfo)
                    {
                        chapterMap.put(lastReadingChapterId, chapterInfo);
                    }
                    public  LastBookChapter()
                    {

                    }
                }
                public Map<Integer, LastBookChapter> bookChapterMap = new HashMap<>();
            }

            public static class BoughtAssets implements Serializable
            {
                public Map<Integer, Integer[]> bookChapterBought = new HashMap<>();
            }
            public static class AccountStatistics implements Serializable
            {
                public boolean isReceivedStreak = false;
                public int streakCount = 0;
                public int highestStreakCount = 0;
                public int lastReadingDay = 0;
                public long todayReadTimeMilli = 0;
                public long totalTimeReadLifetimeMilli = 0;
            }
            public AccountReadingProgress accountReadingProgress = new AccountReadingProgress();

            public BoughtAssets boughtAssets = new BoughtAssets();

            public AccountStatistics accountStatistics = new AccountStatistics();
            public long timeSaved = 0;
        }
    }
}
