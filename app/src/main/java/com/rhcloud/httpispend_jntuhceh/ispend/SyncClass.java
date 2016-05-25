package com.rhcloud.httpispend_jntuhceh.ispend;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by Muneer on 24-05-2016.
 */
public class SyncClass {
    Context context;
    User user;

    SyncClass(Context context, User user) {
        this.context = context;
        this.user = user;
    }

    void syncUser() {
        final String email = user.email;

        final DatabaseHelper databaseHelper = new DatabaseHelper(context);
        Cursor cursor = databaseHelper.getDirtyUser(email);

        if(cursor == null || cursor.getCount() == 0) {
            Toast.makeText(context, "Sync User not required", Toast.LENGTH_SHORT).show();
            syncBudget();
        }
        else {
            SyncClassServerRequests syncClassServerRequests = new SyncClassServerRequests(context);
            syncClassServerRequests.syncUserInBackground(user, new GetUserCallback() {
                @Override
                public void done(User returnedUser) {
                    databaseHelper.makeUserNotDirty(email);
                    syncBudget();
                }
            });
        }
    }

    void syncBudget() {
        final String email = user.email;

        final DatabaseHelper databaseHelper = new DatabaseHelper(context);
        Cursor cursor = databaseHelper.getDirtyBudget(email);

        if(cursor == null || cursor.getCount() == 0) {
            Toast.makeText(context, "Sync Budget not required", Toast.LENGTH_SHORT).show();
            syncPurchases();
        }
        else {
            SyncClassServerRequests syncClassServerRequests = new SyncClassServerRequests(context);
            syncClassServerRequests.syncBudgetInBackground(getBudget(cursor), new GetBudgetCallback() {
                @Override
                public void done(Budget returnedBudget) {
                    databaseHelper.makeBudgetNotDirty(email);
                    syncPurchases();
                }
            });
        }
    }

    void syncPurchases() {
        final DatabaseHelper databaseHelper = new DatabaseHelper(context);
        Cursor cursor = databaseHelper.getDirtyPurchases(user.email);
        if(cursor == null || cursor.getCount() == 0) {
            Toast.makeText(context, "Sync Purchases not required", Toast.LENGTH_SHORT).show();
        }
        else {
            HelperClass helperClass = new HelperClass(context);
            String uploadedTime = helperClass.getTimeStamp();
            String uploaderMAC = helperClass.getMacAddress();

            String json_string ="{\"purchases\":[";

            int noOfDirtyPurchases = cursor.getCount();
            int i = 1;
            while(cursor.moveToNext()) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("Buyer", cursor.getString(1));
                    jsonObject.put("ItemName", cursor.getString(2));
                    jsonObject.put("ItemPrice", cursor.getString(3));
                    jsonObject.put("ItemCategory", cursor.getString(4));
                    jsonObject.put("PurchaseTime", cursor.getString(5));
                    jsonObject.put("UploadedTime", uploadedTime);
                    jsonObject.put("UploaderMAC", uploaderMAC);

                    if(i != noOfDirtyPurchases)
                        json_string = json_string + jsonObject.toString() + ",";
                    else
                        json_string = json_string + jsonObject.toString() + "}";

                    i++;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
//Close JSON string
            json_string = json_string.substring(0, json_string.length()-1);
            json_string += "]}";

            SyncClassServerRequests syncClassServerRequests = new SyncClassServerRequests(context);
            syncClassServerRequests.syncPurchasesInBackground(user.email, json_string, new GetObjectCallback() {
                @Override
                public void done(Object returnedObject) {
                    databaseHelper.makePurchasesNotDirty(user.email);

                }
            });
        }
    }

    Budget getBudget(Cursor cursor) {
        Budget budget = null;
        if(cursor.moveToNext()) {
            String email = cursor.getString(0);
            String food = cursor.getString(1);
            String entertainment = cursor.getString(2);
            String electronics = cursor.getString(3);
            String fashion = cursor.getString(4);
            String other = cursor.getString(5);
            String total = cursor.getString(6);

            HelperClass helperClass = new HelperClass(context);

            budget = new Budget(email, food, entertainment, electronics, fashion, other, total, helperClass.getTimeStamp(), helperClass.getMacAddress());
        }
        return budget;
    }
}
