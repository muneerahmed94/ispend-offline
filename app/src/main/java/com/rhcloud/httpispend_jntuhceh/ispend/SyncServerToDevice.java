package com.rhcloud.httpispend_jntuhceh.ispend;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Muneer on 25-05-2016.
 */
public class SyncServerToDevice {

    Context context;
    UserLocalStore userLocalStore;

    public SyncServerToDevice(Context context) {
        this.context = context;
        userLocalStore = new UserLocalStore(context);
    }

    void syncBudget() {
        SyncServerToDeviceServerRequests syncServerToDeviceServerRequests = new SyncServerToDeviceServerRequests(context);
        syncServerToDeviceServerRequests.fetchBudgetInBackground(userLocalStore.getLoggedInUser().email, new GetBudgetCallback() {
            @Override
            public void done(Budget returnedBudget) {
                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                databaseHelper.insertOrUpdateBudgetFromServerToDevice(returnedBudget);
            }
        });
    }

    void syncPurchases() {
        SyncServerToDeviceServerRequests syncServerToDeviceServerRequests = new SyncServerToDeviceServerRequests(context);
        syncServerToDeviceServerRequests.fetchPurchasesInBackground(new GetObjectCallback() {
            @Override
            public void done(Object returnedObject) {
                //Toast.makeText(context, returnedObject.toString(), Toast.LENGTH_LONG).show();
                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                databaseHelper.insertPurchasesFromServer(returnedObject.toString());
                context.startActivity(new Intent(context, WelcomeActivity.class));
            }
        });
    }
}
