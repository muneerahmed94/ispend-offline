package com.rhcloud.httpispend_jntuhceh.ispend;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Muneer on 25-05-2016.
 */
public class MySharedPreferences {

    public static final String SP_NAME = "mySharedPreferencesDatabase";
    SharedPreferences mySharedPreferencesDatabase;

    public MySharedPreferences(Context context)
    {
        mySharedPreferencesDatabase = context.getSharedPreferences(SP_NAME, 0);
    }

    public void setLastDownloadTime(String lastDownloadTime)
    {
        SharedPreferences.Editor userLocalDatabaseEditor = mySharedPreferencesDatabase.edit();
        userLocalDatabaseEditor.putString("LastDownloadTime", lastDownloadTime);
        userLocalDatabaseEditor.commit();
    }

    public String getLastDownloadTime() {
        String lastDownloadTime = mySharedPreferencesDatabase.getString("LastDownloadTime", "2016-01-21 00:00:00");
        return  lastDownloadTime;
    }
}
