package com.rhcloud.httpispend_jntuhceh.ispend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Muneer on 26-05-2016.
 */
public class SMSBroadcastReceiver extends BroadcastReceiver {

    UserLocalStore userLocalStore;

    @Override
    public void onReceive(Context context, Intent intent) {

        final String SMS_BUNDLE = "pdus";
        final Bundle bundle = intent.getExtras();

        try {
            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get(SMS_BUNDLE);

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    String senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();

                    //Log.i("SmsReceiver", "senderNum: " + senderNum + "; message: " + message);

                    // Show Alert
//                    int duration = Toast.LENGTH_SHORT;
//                    Toast toast = Toast.makeText(context,
//                            "senderNum: "+ senderNum + ", message: " + message, duration);
//                    toast.show();
                    //Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    HelperClass helperClass = new HelperClass(context);
                    HashMap<String, String> purchaseDetails = helperClass.getPurchaseDetails(message);
                    if(helperClass.isPurchaseSMS(message)) {
                        String toastString = "Amount: " + purchaseDetails.get("Amount") + "\n" + "Merchant Name: " + purchaseDetails.get("MerchantName");
                        Toast.makeText(context, toastString, Toast.LENGTH_LONG).show();

                        DatabaseHelper databaseHelper = new DatabaseHelper(context);
                        databaseHelper.insertPurchaseFromSMS(purchaseDetails);
                    }
                } // end for loop
            } // bundle is null
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e);
        }
    }
}
