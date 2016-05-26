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

import java.util.HashMap;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Patterns;
import android.widget.Toast;

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

    public HashMap<String, String> getPurchaseDetails(String message) {
        HashMap<String, String> purchaseDetails = new HashMap<String, String>();

        Pattern amountPattern = Pattern.compile("(?i)(?:(?:RS|INR|MRP)\\.?\\s?)(\\d+(:?\\,\\d+)?(\\,\\d+)?(\\.\\d{1,2})?)");
        Matcher amountMatcher = amountPattern.matcher(message);

        if(amountMatcher.find()) {
            purchaseDetails.put("Amount", amountMatcher.group(1));
        }

        Pattern merchantNamePattern = Pattern.compile("(?i)(?:\\sat\\s|in\\*)([A-Za-z0-9]*\\s?-?\\s?[A-Za-z0-9]*\\s?-?\\.?)");
        Matcher merchantNameMatcher = merchantNamePattern.matcher(message);

        if(merchantNameMatcher.find()) {
            purchaseDetails.put("MerchantName", merchantNameMatcher.group(1));
        }

        Pattern cardPattern = Pattern.compile("(?i)(?:\\smade on|ur|made a\\s|in\\*)([A-Za-z]*\\s?-?\\s[A-Za-z]*\\s?-?\\s[A-Za-z]*\\s?-?)");
        Matcher cardMatcher = cardPattern.matcher(message);


        if(cardMatcher.find()) {
            purchaseDetails.put("CardType", cardMatcher.group(1));
        }

        return purchaseDetails;
    }

    public boolean isNumeric(String string) {
        try {
            double d = Double.parseDouble(string);
        }
        catch(Exception e) {
            return false;
        }
        return true;
    }

    public boolean isPurchaseSMS(String message) {

        HashMap<String, String> purchaseDetails = getPurchaseDetails(message);
        if(purchaseDetails.get("Amount") != null && isNumeric(purchaseDetails.get("Amount")) && purchaseDetails.get("MerchantName") != null) {
            return true;
        }
        else {
            return false;
        }
    }

    public String getEmailAccount() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(context).getAccounts();
        String possibleEmail = "";
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                possibleEmail = account.name;
                //Toast.makeText(context, possibleEmail, Toast.LENGTH_SHORT).show();
            }
        }
        return possibleEmail;
    }

    public String getCategory(String merchantName) {
        String itemCategory = "";

        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put("Dominos", "Food");
        hashMap.put("Mc Donalds", "Food");
        hashMap.put("PVR Cinemas", "Entertainment");
        hashMap.put("Reliance Digital", "Electronics");
        hashMap.put("TMC", "Electronics");
        hashMap.put("Hyderabad Central", "Fashion");
        hashMap.put("Pepe Jeans", "Fashion");

        for(String s : hashMap.keySet()) {
            if(merchantName.contains(s) || s.contains(merchantName)) {
                return hashMap.get(s);
            }
        }

        return new String("Other");
    }
}
