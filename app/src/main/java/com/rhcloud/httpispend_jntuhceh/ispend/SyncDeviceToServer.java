package com.rhcloud.httpispend_jntuhceh.ispend;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Muneer on 24-05-2016.
 */
public class SyncDeviceToServer {
    Context context;
    User user;

    SyncDeviceToServer(Context context, User user) {
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
            if(new HelperClass(context).isNetworkAvailable()) {
                SyncDeviceToServerServerRequests syncClassServerRequests = new SyncDeviceToServerServerRequests(context);
                syncClassServerRequests.syncUserInBackground(user, new GetUserCallback() {
                    @Override
                    public void done(User returnedUser) {
                        databaseHelper.makeUserNotDirty(email);
                        syncBudget();
                    }
                });
            }
            else {
                Toast.makeText(context, "No Internet connection detected - to Sync User to Server", Toast.LENGTH_LONG).show();
                new HelperClass(context).logout();
            }
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
            if(new HelperClass(context).isNetworkAvailable()) {
                SyncDeviceToServerServerRequests syncDeviceToServerServerRequests = new SyncDeviceToServerServerRequests(context);
                syncDeviceToServerServerRequests.syncBudgetInBackground(getBudget(cursor), new GetBudgetCallback() {
                    @Override
                    public void done(Budget returnedBudget) {
                        databaseHelper.makeBudgetNotDirty(email);
                        syncPurchases();
                    }
                });
            }
            else {
                Toast.makeText(context, "No Internet connection detected - to Sync Budget to Server", Toast.LENGTH_LONG).show();
                new HelperClass(context).logout();
            }
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

            if(new HelperClass(context).isNetworkAvailable()) {
                SyncDeviceToServerServerRequests syncClassServerRequests = new SyncDeviceToServerServerRequests(context);
                syncClassServerRequests.syncPurchasesInBackground(user.email, json_string, new GetObjectCallback() {
                    @Override
                    public void done(Object returnedObject) {
                        databaseHelper.makePurchasesNotDirty(user.email);
                    }
                });
            }
            else {
                Toast.makeText(context, "No Internet connection detected - to Sync Purchases to Server", Toast.LENGTH_LONG).show();
                new HelperClass(context).logout();
            }
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
            String budgetSetAt = cursor.getString(7);

            HelperClass helperClass = new HelperClass(context);

            budget = new Budget(email, food, entertainment, electronics, fashion, other, total, budgetSetAt, helperClass.getTimeStamp(), helperClass.getMacAddress());
        }
        return budget;
    }
}
