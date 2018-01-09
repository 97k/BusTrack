package com.example.aditya.bustrack;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Alex on 1/9/2018.
 */

public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        if(null == bundle)
            return;

        String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);

        Log.i("OutgoingCallReceiver",phoneNumber);
        Log.i("OutgoingCallReceiver",bundle.toString());

        if(phoneNumber.equals("#000")) {
            //intent.setComponent(new ComponentName("com.example", "com.example.MyExampleActivity"));
            //Context context = getApplicationContext();
            CharSequence text = "Hello toast!";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }
}
