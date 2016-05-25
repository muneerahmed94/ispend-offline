package com.rhcloud.httpispend_jntuhceh.ispend;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import java.util.TimeZone;

/**
 * Created by Muneer on 23-05-2016.
 */
public class HelperClass {

    Context context;

    public HelperClass() {
        this.context = null;
    }

    public HelperClass(Context context) {
        this.context = context;
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
}
