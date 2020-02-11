package com.carusto.ReactNativePjSip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import com.carusto.ReactNativePjSip.utils.ArgumentUtils;
import com.carusto.ReactNativePjSip.PjSipBroadcastReceiver;
import android.net.NetworkInfo;
import android.util.Log;

public class PjSipConnectivityReceiver extends BroadcastReceiver {

        public PjSipConnectivityReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent _intent) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                String json = _intent.getStringExtra("data");
                if (netInfo != null && netInfo.isConnected()) {
                        String ns = context.getApplicationContext().getPackageName();
                        json = "{\"connectivity\":true}";
                        Log.d("ReactNative", "Internet is up!");
                        String cls = ns + ".MainActivity";

                        Intent intent = new Intent();
                        intent.setClassName(ns, cls);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.EXTRA_DOCK_STATE_CAR);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.putExtra("foreground", true);
                        context.startActivity(intent);
                } else {
                        json = "{\"connectivity\":false}";
                        Log.d("ReactNative", "Internet is down!");
                }
                Object params = ArgumentUtils.fromJson(json);
                PjSipBroadcastReceiver.emit("pjSipConnectStateChanged", params);

        }

}