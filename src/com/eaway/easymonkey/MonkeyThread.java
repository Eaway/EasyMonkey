package com.eaway.easymonkey;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MonkeyThread implements Runnable {
    public static final String TAG = "EasyMonkey.Thread";
    private static String sParameter = "";
    private String mFilePath;
    private FileWriter mLogWriter;

    List<MonkeyApp> mAppList;

    public MonkeyThread(List<MonkeyApp> appList, String parameter) {
        mAppList = appList;
        sParameter = parameter;
    }

    @Override
    public void run() {
        String date = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
        if (isExternalStorageWritable()) {
            mFilePath = Environment.getExternalStorageDirectory() + "/" + "EasyMonkey-" + date + ".log";
        } else {
            mFilePath = Environment.getDataDirectory() + "/" + "EasyMonkey-" + date + ".log";
            Log.e(MainActivity.TAG, "isExternalStorageWritable = false");
        }

        Log.i(MainActivity.TAG, mAppList.size() + " App waiting for test");
        Log.i(MainActivity.TAG, "Log: " + mFilePath);

        try {
            File LogFile = new File(mFilePath);
            LogFile.createNewFile();
            mLogWriter = new FileWriter(LogFile);

            for (MonkeyApp app : mAppList) {
                String command = "monkey -p " + app.pkg + sParameter;
                Log.i(MainActivity.TAG, command);
                execCommand(command);
            }

            mLogWriter.close();
        } catch (IOException e) {
            Log.d(TAG, "Log file error: " + e.getMessage());
        }
    }

    public boolean execCommand(String cmd) {

        try {

            mLogWriter.write(cmd + "\r\n\r\n");

            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                line += "\r\n";
                mLogWriter.write(line += "\r\n");
            }
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "execCommand error");
            e.printStackTrace();
        }
        return true;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
