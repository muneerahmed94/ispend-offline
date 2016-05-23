package com.rhcloud.httpispend_jntuhceh.ispend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
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

    static final String CREATE_USERS_TABLE_QUERY = "CREATE TABLE Users (Email varchar(100) PRIMARY KEY, Mobile varchar(20) UNIQUE, Name varchar(100), Password varchar(50))";
    static final String CREATE_BUDGET_TABLE_QUERY = "CREATE TABLE Budget (Email varchar(100) PRIMARY KEY, Food int(11), Entertainment int(11), Electronics int(11), Fashion int(11), Other int(11), Total int(11), FOREIGN KEY (Email) REFERENCES Users(Email))";
    static final String CREATE_PURCHASES_TABLE_QUERY = "CREATE TABLE Purchases (PurchaseID INTEGER PRIMARY KEY AUTOINCREMENT, Buyer varchar(100), ItemName varchar(100) DEFAULT NULL, ItemPrice int(11), ItemCategory varchar(100), PurchaseTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (Buyer) REFERENCES Users(Email))";

    private final Context context;
    ArrayList<String> categories;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;

        categories = new ArrayList<>();
        categories.add("Food");
        categories.add("Entertainment");
        categories.add("Electronics");
        categories.add("Fashion");
        categories.add("Other");
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
            db.insert(BUDGET_TABLE_NAME, null, contentValues);


            for(String category : categories) {
                contentValues = new ContentValues();
                contentValues.put("Buyer", user.email);
                contentValues.put("ItemPrice", 0);
                contentValues.put("ItemCategory", category);
                db.insert(PURCHASES_TABLE_NAME, null, contentValues);
            }
            return true;
        }
    }

    public Cursor loginUser(User user) {
        Cursor res = null;

        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String loginQuery = "SELECT * FROM Users WHERE Email = '" + user.email + "' AND Password = '" + user.password + "'";
            res = db.rawQuery(loginQuery, null);
            return res;
        }
        catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
        }

        return res;
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

        SQLiteDatabase db = this.getWritableDatabase();
        int status = db.update(BUDGET_TABLE_NAME, contentValues, "Email = '" + email + "'", null);
        if(status > 0)
            return true;
        else
            return false;
    }
}