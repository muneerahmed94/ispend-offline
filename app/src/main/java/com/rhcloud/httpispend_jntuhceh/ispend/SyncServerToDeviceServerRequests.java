package com.rhcloud.httpispend_jntuhceh.ispend;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
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
 * Created by Muneer on 25-05-2016.
 */
public class SyncServerToDeviceServerRequests {

    Context context;
    UserLocalStore userLocalStore;

    public SyncServerToDeviceServerRequests(Context context) {
        this.context = context;
        userLocalStore = new UserLocalStore(context);
    }

    public void fetchUserInBackground(User user, GetUserCallback userCallBack)
    {
        new fetchUserAsync(user, userCallBack).execute();
    }

    public class fetchUserAsync extends AsyncTask<Void, Void, User> {
        User user;
        GetUserCallback userCallBack;

        public fetchUserAsync(User user, GetUserCallback userCallBack) {
            this.user = user;
            this.userCallBack = userCallBack;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected User doInBackground(Void... params)
        {
            User returnedUser = null;
            String login_url = "http://ispend-jntuhceh.rhcloud.com/offline/sync/servertodevice/user.php";
            try
            {
                URL url = new URL(login_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder().appendQueryParameter("Email", user.email).appendQueryParameter("Password", user.password);
                String query = builder.build().getEncodedQuery();
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String response = IOUtils.toString(in, "UTF-8");
                JSONObject jResponse = new JSONObject(response);

                if (jResponse.length() == 0)
                {
                    returnedUser = null;
                }
                else
                {
                    String mobile = jResponse.getString("Mobile");
                    String name = jResponse.getString("Name");

                    returnedUser = new User(user.email, mobile, name, user.password);
                }

                return returnedUser;

            } catch (IOException | JSONException e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
            return returnedUser;
        }

        @Override
        protected void onPostExecute(User returnedUser) {
            userCallBack.done(returnedUser);
        }
    }

    void fetchBudgetInBackground(String email, GetBudgetCallback budgetCallback) {
        new fetchBudgetAsync(email, budgetCallback).execute();
    }

    class fetchBudgetAsync extends AsyncTask<Void, Void, Budget> {

        String email;
        GetBudgetCallback budgetCallback;

        public fetchBudgetAsync(String email, GetBudgetCallback budgetCallback) {
            this.email = email;
            this.budgetCallback = budgetCallback;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Budget doInBackground(Void... params) {
            Budget returnedBudget;
            String server_url = "http://ispend-jntuhceh.rhcloud.com/offline/sync/servertodevice/get_budget.php";
            try
            {

                URL url = new URL(server_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder().appendQueryParameter("Email", email);
                String query = builder.build().getEncodedQuery();
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String response = IOUtils.toString(in, "UTF-8");
                JSONObject jResponse = new JSONObject(response);

                if (jResponse.length() == 0)
                {
                    returnedBudget = null;
                }
                else
                {
                    String food = jResponse.getString("Food");
                    String entertainment = jResponse.getString("Entertainment");
                    String electronics = jResponse.getString("Electronics");
                    String fashion = jResponse.getString("Fashion");
                    String other = jResponse.getString("Other");
                    String total = jResponse.getString("Other");
                    String budgetSetAt = jResponse.getString("BudgetSetAt");
                    String uploadedTime = jResponse.getString("UploadedTime");
                    String uploaderMAC = jResponse.getString("UploaderMAC");

                    returnedBudget = new Budget(email, food, entertainment, electronics, fashion, other, total, budgetSetAt, uploadedTime, uploaderMAC);
                }

                return returnedBudget;

            }
            catch (Exception e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Budget returnedBudget) {
            if(returnedBudget != null) {
                budgetCallback.done(returnedBudget);
            }
            else {
                Toast.makeText(context, "unable to retrieve budget", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void fetchPurchasesInBackground(GetObjectCallback objectCallback) {
        new fetchPurchasesAsync(objectCallback).execute();
    }

    class fetchPurchasesAsync extends AsyncTask<Void, Void, String> {

        GetObjectCallback objectCallback;

        public fetchPurchasesAsync(GetObjectCallback objectCallback) {
            this.objectCallback = objectCallback;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... params) {
            String JSON_STRING = "";
            try
            {
                MySharedPreferences mySharedPreferences = new MySharedPreferences(context);
                String email = userLocalStore.getLoggedInUser().email;
                String uploaderMAC = new HelperClass(context).getMacAddress();
                String lastDownloadTime = mySharedPreferences.getLastDownloadTime();


                URL url = new URL("https://ispend-jntuhceh.rhcloud.com/offline/sync/servertodevice/get_purchases.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                OutputStream os = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter((new OutputStreamWriter(os, "UTF-8")));
                String data = URLEncoder.encode("Email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8") + "&" +
                                 URLEncoder.encode("UploaderMAC", "UTF-8") + "=" + URLEncoder.encode(uploaderMAC, "UTF-8") + "&" +
                                    URLEncoder.encode("LastDownloadTime", "UTF-8") + "=" + URLEncoder.encode(lastDownloadTime, "UTF-8");

                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                os.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                while((JSON_STRING = bufferedReader.readLine()) != null)
                {
                    stringBuilder.append(JSON_STRING + "\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            objectCallback.done(s);
        }
    }
}
