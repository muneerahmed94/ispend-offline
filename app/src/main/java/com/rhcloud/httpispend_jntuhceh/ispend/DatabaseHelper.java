package com.rhcloud.httpispend_jntuhceh.ispend;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.widget.AdapterView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Muneer on 23-05-2016.
 */
public class DatabaseHelper  extends SQLiteOpenHelper {

    static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "local.db";

    static final String USERS_TABLE_NAME = "Users";
    static final String BUDGET_TABLE_NAME = "Budget";
    static final String PURCHASES_TABLE_NAME = "Purchases";

    static final String CREATE_USERS_TABLE_QUERY = "CREATE TABLE Users (Email varchar(100) PRIMARY KEY, Mobile varchar(20) UNIQUE, Name varchar(100), Password varchar(50), IsDirty INTEGER DEFAULT 1)";
    static final String CREATE_BUDGET_TABLE_QUERY = "CREATE TABLE Budget (Email varchar(100) PRIMARY KEY, Food int(11), Entertainment int(11), Electronics int(11), Fashion int(11), Other int(11), Total int(11), BudgetSetAt TIMESTAMP, IsDirty INTEGER DEFAULT 1, FOREIGN KEY (Email) REFERENCES Users(Email))";
    static final String CREATE_PURCHASES_TABLE_QUERY = "CREATE TABLE Purchases (PurchaseID INTEGER PRIMARY KEY AUTOINCREMENT, Buyer varchar(100), ItemName varchar(100) DEFAULT NULL, ItemPrice int(11), ItemCategory varchar(100), PurchaseTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP, IsDirty INTEGER DEFAULT 1, FOREIGN KEY (Buyer) REFERENCES Users(Email))";

    private final Context context;
    ArrayList<String> categories;
    UserLocalStore userLocalStore;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;

        categories = new ArrayList<>();
        categories.add("Food");
        categories.add("Entertainment");
        categories.add("Electronics");
        categories.add("Fashion");
        categories.add("Other");

        userLocalStore = new UserLocalStore(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE_QUERY);
        db.execSQL(CREATE_BUDGET_TABLE_QUERY);
        db.execSQL(CREATE_PURCHASES_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + BUDGET_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PURCHASES_TABLE_NAME);
        onCreate(db);
    }

    public void showErrorMessage()
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setMessage("The email and password you entered don't match.");
        dialogBuilder.setPositiveButton("OK", null);
        dialogBuilder.show();
    }

    public void logUserIn(User returnedUser)
    {
        userLocalStore.storeUserData(returnedUser);
        userLocalStore.setUserLoggedIn(true);
        if(new HelperClass(context).isNetworkAvailable()) {
            new SyncServerToDevice(context).syncBudget();
        }
        else {
            Toast.makeText(context, "No Internet connection detected - to Sync Budget from Server", Toast.LENGTH_SHORT).show();
            context.startActivity(new Intent(context, WelcomeActivity.class));
        }
    }

    public void getUserFromServer(User user)
    {
        SyncServerToDeviceServerRequests syncServerToDeviceServerRequests = new SyncServerToDeviceServerRequests(context);
        syncServerToDeviceServerRequests.fetchUserInBackground(user, new GetUserCallback() {
            @Override
            public void done(User returnedUser) {
                if (returnedUser == null) {
                    showErrorMessage();
                } else {
                    insertUserFromServerOnLocalDbAndLogin(returnedUser);
                }
            }
        });
    }

    void insertUserFromServerOnLocalDbAndLogin(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Email", user.email);
        contentValues.put("Mobile", user.mobile);
        contentValues.put("Name", user.name);
        contentValues.put("Password", user.password);
        contentValues.put("IsDirty", 0);
        long res = db.insert(USERS_TABLE_NAME, null, contentValues);
        if(res == -1) {
            Toast.makeText(context, "Unable to insert the User retrieved from server into local db", Toast.LENGTH_SHORT).show();
        }
        else {
            logUserIn(user);
        }
    }

    public boolean registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Email", user.email);
        contentValues.put("Mobile", user.mobile);
        contentValues.put("Name", user.name);
        contentValues.put("Password", user.password);
        long res = db.insert(USERS_TABLE_NAME, null, contentValues);
        if(res == -1)
            return false;
        else {

            contentValues = new ContentValues();
            contentValues.put("Email", user.email);
            contentValues.put("Food", 0);
            contentValues.put("Entertainment", 0);
            contentValues.put("Electronics", 0);
            contentValues.put("Fashion", 0);
            contentValues.put("Other", 0);
            contentValues.put("Total", 0);
            contentValues.put("BudgetSetAt", new HelperClass(context).getTimeStamp());
            db.insert(BUDGET_TABLE_NAME, null, contentValues);


            for(String category : categories) {
                contentValues = new ContentValues();
                contentValues.put("Buyer", user.email);
                contentValues.put("ItemPrice", 0);
                contentValues.put("ItemCategory", category);
                contentValues.put("PurchaseTime", new HelperClass(context).getTimeStamp());
                db.insert(PURCHASES_TABLE_NAME, null, contentValues);
            }
            return true;
        }
    }

    public void loginUser(User user) {
        Cursor res = null;
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            String loginQuery = "SELECT * FROM Users WHERE Email = '" + user.email +  "'";
            res = db.rawQuery(loginQuery, null);
            if(res == null || res.getCount() == 0) {
                Toast.makeText(context, "User not found on device...attempting to find User online", Toast.LENGTH_LONG).show();
                if(new HelperClass(context).isNetworkAvailable()) {
                    getUserFromServer(user);
                }
                else {
                    Toast.makeText(context, "No Internet connection detected - to Sync User from Server", Toast.LENGTH_LONG).show();
                }
            }
            else {
                loginQuery = "SELECT * FROM Users WHERE Email = '" + user.email + "' AND Password = '" + user.password + "'";
                res = db.rawQuery(loginQuery, null);
                if(res == null || res.getCount() == 0) {
                    showErrorMessage();
                }
                else {
                    StringBuffer buff = new StringBuffer();
                    if(res.moveToNext()) {
                        user.name = res.getString(2);
                        user.mobile = res.getString(1);
                        logUserIn(user);
                    }
                }
            }
        }
        catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public HashMap<String, String> getPurchaseSummary(String email) {

        Cursor cursor = null;

        HashMap<String, String> hm = new HashMap<>();

        try {
            SQLiteDatabase db = this.getWritableDatabase();

            for(String category : categories) {
                String query = "SELECT SUM(ItemPrice) FROM Purchases WHERE Buyer = '" + email + "' AND ItemCategory = '" + category + "'";
                cursor = db.rawQuery(query, null);
                if(cursor.moveToNext()) {
                    hm.put(category, new Integer(cursor.getInt(0)).toString());
                }
            }

            for(String category : categories) {
                String query = "SELECT SUM("+category+") FROM Budget WHERE Email = '" + email + "'";
                cursor = db.rawQuery(query, null);
                if(cursor.moveToNext()) {
                    hm.put("T"+category, new Integer(cursor.getInt(0)).toString());
                }
            }
        }
        catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
        }

        return hm;
    }

    public boolean updateBudget(String email, Budget budget) {
        ContentValues contentValues = new ContentValues();

        contentValues.put("Food", budget.food);
        contentValues.put("Entertainment", budget.entertainment);
        contentValues.put("Electronics", budget.electronics);
        contentValues.put("Fashion", budget.fashion);
        contentValues.put("Other", budget.other);
        contentValues.put("Total", budget.total);
        contentValues.put("BudgetSetAt", new HelperClass(context).getTimeStamp());
        contentValues.put("IsDirty", 1);

        SQLiteDatabase db = this.getWritableDatabase();
        int status = db.update(BUDGET_TABLE_NAME, contentValues, "Email = '" + email + "'", null);
        if(status > 0)
            return true;
        else
            return false;
    }

    public boolean purchaseItem(Purchase purchase) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put("Buyer", purchase.buyer);
        contentValues.put("ItemName", purchase.itemName);
        contentValues.put("ItemCategory", purchase.itemCategory);
        contentValues.put("ItemPrice", purchase.itemPrice);
        contentValues.put("PurchaseTime", new HelperClass(context).getTimeStamp());
        long res = db.insert(PURCHASES_TABLE_NAME, null, contentValues);

        if(res == -1)
            return false;
        else {
            return true;
        }
    }

    public String getMyItemsJSON(String email) {
        String myItemsJSON = "";
        Cursor cursor;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String loginQuery = "SELECT * FROM Purchases WHERE Buyer = '" + email + "' AND ItemName IS NOT NULL AND ItemName NOT LIKE ''" ;

            cursor = db.rawQuery(loginQuery, null);

            if(cursor == null || cursor.getCount() == 0) {
                return null;
            }
            else {
                JSONArray jsonArray = new JSONArray();

                while (cursor.moveToNext()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("ItemName", cursor.getString(2));
                    jsonObject.put("ItemPrice", cursor.getString(3));
                    jsonObject.put("ItemCategory", cursor.getString(4));
                    jsonObject.put("PurchaseTime", cursor.getString(5));

                    jsonArray.add(jsonObject);
                }

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("server_response", jsonArray);

                myItemsJSON = jsonObject.toString();
            }
        }
        catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
        }

        return myItemsJSON;
    }

    public Cursor getDirtyUser(String email) {
        Cursor res = null;

        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "SELECT * FROM Users WHERE Email = '" + email + "' AND IsDirty = 1";
            res = db.rawQuery(query, null);

            return res;
        }
        catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
        }
        return res; //because java says missing return statement
    }

    public Cursor getDirtyBudget(String email) {
        Cursor res = null;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "SELECT * FROM Budget WHERE Email = '" + email + "' AND IsDirty = 1";
            res = db.rawQuery(query, null);

            return res;
        }
        catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
        }
        return res; //because java says missing return statement
    }

    public Cursor getDirtyPurchases(String email) {
        Cursor res = null;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "SELECT * FROM Purchases WHERE Buyer = '" + email + "' AND IsDirty = 1";
            res = db.rawQuery(query, null);

            return res;
        }
        catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
        }
        return res; //because java says missing return statement
    }

    public void makeUserNotDirty(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("IsDirty", 0);
        db.update(USERS_TABLE_NAME, contentValues, "Email = '" + email + "'", null);
    }

    public void makeBudgetNotDirty(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("IsDirty", 0);
        db.update(BUDGET_TABLE_NAME, contentValues, "Email = '" + email + "'", null);
    }
    public void makePurchasesNotDirty(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("IsDirty", 0);
        db.update(PURCHASES_TABLE_NAME, contentValues, "Buyer = '" + email + "'", null);
    }

    /*-----------------------------------------------------------------Server To Device-------------------------------------------------------------------*/

    void insertOrUpdateBudgetFromServerToDevice(Budget budgetFromServer) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res;

        String query = "SELECT * FROM Budget WHERE Email = '" + budgetFromServer.email +  "'";
        res = db.rawQuery(query, null);
        if(res == null || res.getCount() == 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("Email", budgetFromServer.email);
            contentValues.put("Food", budgetFromServer.food);
            contentValues.put("Entertainment", budgetFromServer.entertainment);
            contentValues.put("Electronics", budgetFromServer.electronics);
            contentValues.put("Fashion", budgetFromServer.fashion);
            contentValues.put("Other", budgetFromServer.other);
            contentValues.put("Total", budgetFromServer.total);
            contentValues.put("BudgetSetAt", budgetFromServer.budgetSetAt);
            contentValues.put("IsDirty", 0);

            db.insert(BUDGET_TABLE_NAME, null, contentValues);
            Toast.makeText(context, "Budget not yet set...Budget on server inserted into Budget on device", Toast.LENGTH_LONG).show();
        }
        else {
            HelperClass helperClass = new HelperClass(context);
            Budget budgetOnDevice = getBudget(res);

            Date serverBudgetSetAt = helperClass.getDateObjectFromDateString(budgetFromServer.budgetSetAt);
            Date deviceBudgetSetAt = helperClass.getDateObjectFromDateString(budgetOnDevice.budgetSetAt);


            if(deviceBudgetSetAt.after(serverBudgetSetAt) || deviceBudgetSetAt.compareTo(serverBudgetSetAt) == 0) {
                Toast.makeText(context, "Budget on device is the most recent budget", Toast.LENGTH_LONG).show();
            }
            else {
                ContentValues contentValues = new ContentValues();
                contentValues.put("Food", budgetFromServer.food);
                contentValues.put("Entertainment", budgetFromServer.entertainment);
                contentValues.put("Electronics", budgetFromServer.electronics);
                contentValues.put("Fashion", budgetFromServer.fashion);
                contentValues.put("Other", budgetFromServer.other);
                contentValues.put("Total", budgetFromServer.total);
                contentValues.put("BudgetSetAt", budgetFromServer.budgetSetAt);
                contentValues.put("IsDirty", 0);

                db.update(BUDGET_TABLE_NAME, contentValues, "Email = '" + budgetOnDevice.email + "'", null);
                Toast.makeText(context, "Budget on device updated with that of Budget on server", Toast.LENGTH_LONG).show();
            }
        }

        if(new HelperClass(context).isNetworkAvailable()) {
            SyncServerToDevice syncServerToDevice = new SyncServerToDevice(context);
            syncServerToDevice.syncPurchases();
        }
        else {
            Toast.makeText(context, "No Internet connection detected - to Sync Purchases from Server", Toast.LENGTH_LONG).show();
            context.startActivity(new Intent(context, WelcomeActivity.class));
        }
    }

    void insertPurchasesFromServer(String json_string) {
        SQLiteDatabase db = this.getWritableDatabase();
        String buyer = userLocalStore.getLoggedInUser().email;
        org.json.JSONObject jsonObject;
        org.json.JSONArray jsonArray;
        boolean arePurchasesSyncedFromServer = false;
        if(json_string != null) { // to avoid null pointer exception
            try {
                jsonObject = new org.json.JSONObject(json_string);
                jsonArray = jsonObject.getJSONArray("server_response");

                int count = 0;
                String itemName, itemCategory, itemPrice, purchaseTime;
                while(count < jsonArray.length())
                {
                    org.json.JSONObject jo = jsonArray.getJSONObject(count);

                    itemName = jo.getString("ItemName");
                    itemCategory = jo.getString("ItemCategory");
                    itemPrice = jo.getString("ItemPrice");
                    purchaseTime = jo.getString("PurchaseTime");


                    ContentValues contentValues = new ContentValues();
                    contentValues.put("Buyer", buyer);
                    contentValues.put("ItemName", itemName);
                    contentValues.put("ItemPrice", itemPrice);
                    contentValues.put("ItemCategory", itemCategory);
                    contentValues.put("PurchaseTime", purchaseTime);
                    contentValues.put("IsDirty", 0);
                    long res = db.insert(PURCHASES_TABLE_NAME, null, contentValues);
                    if(res == -1)
                        Toast.makeText(context, "Unable to insert purchase", Toast.LENGTH_SHORT).show();
                    else {
                        count++;
                        arePurchasesSyncedFromServer = true;
                    }
                }
            }catch (Exception e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }

        MySharedPreferences mySharedPreferences = new MySharedPreferences(context);
        mySharedPreferences.setLastDownloadTime(new HelperClass(context).getTimeStamp());

        if(arePurchasesSyncedFromServer) {
            Toast.makeText(context, "Some Purchases have been synced from server", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, "No new purchases found on server", Toast.LENGTH_SHORT).show();
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

            budget = new Budget(email, food, entertainment, electronics, fashion, other, total, budgetSetAt);
        }
        return budget;
    }

    void insertPurchaseFromSMS(HashMap<String, String> purchaseDetails) {
        UserLocalStore userLocalStore = new UserLocalStore(context);
        HelperClass helperClass = new HelperClass(context);

        String email = helperClass.getEmailAccount();

        Cursor res = null;
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            String loginQuery = "SELECT * FROM Users WHERE Email = '" + email +  "'";
            res = db.rawQuery(loginQuery, null);
            if(res == null || res.getCount() == 0) {
                Toast.makeText(context, "User not found on device...attempting to find User online", Toast.LENGTH_LONG).show();
            }
            else {
                Purchase purchase = new Purchase(email, purchaseDetails.get("MerchantName"), purchaseDetails.get("Amount"), helperClass.getCategory(purchaseDetails.get("MerchantName")));
                purchaseItem(purchase);


                User user = new User(email, "");
                userLocalStore.storeUserData(user);
                userLocalStore.setUserLoggedIn(true);
                SyncDeviceToServer syncDeviceToServer = new SyncDeviceToServer(context, user);
                syncDeviceToServer.syncPurchases();
            }
        }
        catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
        }


    }
}
