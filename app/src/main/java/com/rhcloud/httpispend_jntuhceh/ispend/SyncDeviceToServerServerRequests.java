package com.rhcloud.httpispend_jntuhceh.ispend;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.content.Context;
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Muneer on 24-05-2016.
 */
public class SyncDeviceToServerServerRequests {

    private static final int TIMEOUT_MILLISEC = 3000;
    Context context;

    SyncDeviceToServerServerRequests(Context context) {
        this.context = context;
    }

    public void syncUserInBackground(User user, GetUserCallback userCallBack)
    {
        //progressDialog.show();
        Toast.makeText(context, "Sync User started", Toast.LENGTH_SHORT).show();
        new SyncUserAsyncTask(user, userCallBack).execute();
    }

    public class SyncUserAsyncTask extends AsyncTask<Void, Void, String>
    {
        User user;
        GetUserCallback userCallBack;

        public SyncUserAsyncTask(User user, GetUserCallback userCallBack) {
            this.user = user;
            this.userCallBack = userCallBack;
        }

        @Override
        protected String doInBackground(Void... params)
        {
            String register_url = "http://ispend-jntuhceh.rhcloud.com/offline/sync/user.php";
            try
            {
                URL url = new URL(register_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                OutputStream os = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter((new OutputStreamWriter(os, "UTF-8")));
                String data = URLEncoder.encode("Email", "UTF-8") + "=" + URLEncoder.encode(user.email, "UTF-8") + "&" +
                        URLEncoder.encode("Mobile", "UTF-8") + "=" + URLEncoder.encode(user.mobile, "UTF-8") + "&" +
                        URLEncoder.encode("Name", "UTF-8") + "=" + URLEncoder.encode(user.name, "UTF-8") + "&" +
                        URLEncoder.encode("Password", "UTF-8") + "=" + URLEncoder.encode(user.password, "UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                os.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
                String response = "";
                String line = "";
                while ((line = bufferedReader.readLine())!=null)
                {
                    response += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return response;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //progressDialog.dismiss();

            if(result.equals("Registration Successfull"))
            {
                Toast.makeText(context, "Sync User completed successfully", Toast.LENGTH_SHORT).show();
//                Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                userCallBack.done(null);
            }
            else
            {
                Toast.makeText(context, "Sync User failed: " + result, Toast.LENGTH_SHORT).show();
//                Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            }
        }
    }


    public void syncBudgetInBackground(Budget budget, GetBudgetCallback budgetCallback)
    {
        //progressDialog.show();
        new SyncBudgetAsyncTask(budget, budgetCallback).execute();
    }

    public class SyncBudgetAsyncTask extends AsyncTask<Void, Void, String> {

        Budget budget;
        GetBudgetCallback budgetCallback;

        public SyncBudgetAsyncTask(Budget budget, GetBudgetCallback budgetCallback) {
            this.budget = budget;
            this.budgetCallback = budgetCallback;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(context, "Sync Budget started", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String register_url = "http://ispend-jntuhceh.rhcloud.com/offline/sync/budget.php";
            try
            {
                URL url = new URL(register_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                OutputStream os = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter((new OutputStreamWriter(os, "UTF-8")));
                String data = URLEncoder.encode("Email", "UTF-8") + "=" + URLEncoder.encode(budget.email, "UTF-8") + "&" +
                        URLEncoder.encode("Food", "UTF-8") + "=" + URLEncoder.encode(budget.food, "UTF-8") + "&" +
                        URLEncoder.encode("Entertainment", "UTF-8") + "=" + URLEncoder.encode(budget.entertainment, "UTF-8") + "&" +
                        URLEncoder.encode("Electronics", "UTF-8") + "=" + URLEncoder.encode(budget.electronics, "UTF-8") + "&" +
                        URLEncoder.encode("Fashion", "UTF-8") + "=" + URLEncoder.encode(budget.fashion, "UTF-8") + "&" +
                        URLEncoder.encode("Other", "UTF-8") + "=" + URLEncoder.encode(budget.other, "UTF-8") + "&" +
                        URLEncoder.encode("Total", "UTF-8") + "=" + URLEncoder.encode(budget.total, "UTF-8") + "&" +
                        URLEncoder.encode("BudgetSetAt", "UTF-8") + "=" + URLEncoder.encode(budget.budgetSetAt, "UTF-8") + "&" +
                        URLEncoder.encode("UploadedTime", "UTF-8") + "=" + URLEncoder.encode(budget.uploadedTime, "UTF-8") + "&" +
                        URLEncoder.encode("UploaderMAC", "UTF-8") + "=" + URLEncoder.encode(budget.uploaderMAC, "UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                os.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
                String response = "";
                String line = "";
                while ((line = bufferedReader.readLine())!=null)
                {
                    response += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return response;
            } catch (MalformedURLException e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
//            progressDialog.dismiss();
            if(result.equals("Budget set")) {
//                Toast.makeText(context, "Budget updated", Toast.LENGTH_LONG).show();
                Toast.makeText(context, "Sync Budget completed successfully", Toast.LENGTH_LONG).show();
                budgetCallback.done(null);
            }
            else {
//                Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                Toast.makeText(context, "Sync Budget failed: " + result, Toast.LENGTH_LONG).show();
            }
        }
    }

    void syncPurchasesInBackground(String email, String json_string, GetObjectCallback getObjectCallback) {
        SyncPurchasesAsync syncPurchasesAsync = new SyncPurchasesAsync(email, json_string, getObjectCallback);
        syncPurchasesAsync.execute();
    }

    class SyncPurchasesAsync extends AsyncTask<Void, Void, String> {

        String email, json_string;
        GetObjectCallback getObjectCallback;

        SyncPurchasesAsync(String email, String json_string, GetObjectCallback getObjectCallback) {
            this.email = email;
            this.json_string = json_string;
            this.getObjectCallback = getObjectCallback;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(context, "Sync Purchases started", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
                HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
                HttpClient client = new DefaultHttpClient(httpParams);

                String url = "https://ispend-jntuhceh.rhcloud.com/offline/sync/purchases.php";

                HttpPost request = new HttpPost(url);
                try {
                    request.setEntity(new ByteArrayEntity(json_string.getBytes("UTF8")));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                request.setHeader("json", json_string);
                HttpResponse response = null;
                try {
                    response = client.execute(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return response.getStatusLine().toString();
            }
            catch (Exception e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
            if(result.contains("OK")) {
                Toast.makeText(context, "Sync Purchases completed successfully", Toast.LENGTH_SHORT).show();
                getObjectCallback.done(null);
            }
            else {
                Toast.makeText(context, "Sync Purchases failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
