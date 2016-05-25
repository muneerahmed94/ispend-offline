package com.rhcloud.httpispend_jntuhceh.ispend;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import java.util.TimeZone;

/**
 * Created by Muneer on 23-05-2016.
 */
public class HelperClass {

    Context context;
    UserLocalStore userLocalStore;

    public HelperClass() {
        this.context = null;
    }

    public HelperClass(Context context) {
        this.context = context;
        userLocalStore = new UserLocalStore(context);
    }

    String getTimeStamp() {
        DateFormat insertFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        insertFormat.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        Date currentDateTimeObject = new Date();
        String currentDateTimeStringInsert = insertFormat.format(currentDateTimeObject).toString();
        return currentDateTimeStringInsert;
    }

    String getMacAddress() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();
        return macAddress;
    }

    Date getDateObjectFromDateString(String dateString) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date parsedDate = null;
        try {
            parsedDate = formatter.parse(dateString);
        } catch (ParseException e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        } finally {
            return parsedDate;
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void logout() {
        userLocalStore.clearUserData();
        userLocalStore.setUserLoggedIn(false);
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
