package com.carusto.ReactNativePjSip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.util.Log;
import com.carusto.ReactNativePjSip.utils.ArgumentUtils;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

public class PjSipBroadcastReceiver extends BroadcastReceiver {

    private static String TAG = "PjSipBroadcastReceiver";

    private int seq = 0;

    public static ReactApplicationContext cnContext;
    private HashMap<Integer, Callback> callbacks = new HashMap<>();

    public PjSipBroadcastReceiver(ReactApplicationContext context) {

        cnContext = context;
    }

    public void setContext(ReactApplicationContext context) {

        cnContext = context;
    }

    public int register(Callback callback) {
        int id = ++seq;
        callbacks.put(id, callback);
        return id;
    }

    public IntentFilter getFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PjActions.EVENT_STARTED);
        filter.addAction(PjActions.EVENT_ACCOUNT_CREATED);
        filter.addAction(PjActions.EVENT_REGISTRATION_CHANGED);
        filter.addAction(PjActions.EVENT_CALL_RECEIVED);
        filter.addAction(PjActions.EVENT_CALL_CHANGED);
        filter.addAction(PjActions.EVENT_CALL_TERMINATED);
        filter.addAction(PjActions.EVENT_CALL_SCREEN_LOCKED);
        filter.addAction(PjActions.EVENT_MESSAGE_RECEIVED);
        filter.addAction(PjActions.EVENT_HANDLED);

        return filter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // Log.d("ReactNative", "Received \"" + action + "\" response from service ("
        // + ArgumentUtils.dumpIntentExtraParameters(intent) + ")");

        switch (action) {
        case PjActions.EVENT_STARTED:
            onCallback(intent);
            break;
        case PjActions.EVENT_ACCOUNT_CREATED:
            onCallback(intent);
            break;
        case PjActions.EVENT_REGISTRATION_CHANGED:
            onRegistrationChanged(intent);
            break;
        case PjActions.EVENT_MESSAGE_RECEIVED:
            onMessageReceived(intent);
            break;
        case PjActions.EVENT_CALL_RECEIVED:
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> services = activityManager.getRunningTasks(Integer.MAX_VALUE);

            String ns = context.getApplicationContext().getPackageName();
            boolean isServiceFound = false;
            // for (RunningTaskInfo task : tasks) {
            // Log.d("ReactNative", "Running: " + task.baseActivity.getPackageName() + ns);
            // if (ns.equalsIgnoreCase(task.baseActivity.getPackageName())) {
            // isServiceFound = true;
            // break;
            // }
            // }
            if (services.size() > 0 && services.get(0).topActivity.getPackageName().toString()
                    .equalsIgnoreCase(context.getApplicationContext().getPackageName().toString())) {
                isServiceFound = true;
            }
            Log.d("ReactNative", "Running: " + services.get(0).topActivity.getPackageName().toString());
            if (isServiceFound == false) {
                String cls = ns + ".MainActivity";
                Intent _intent = new Intent();
                _intent.setClassName(ns, cls);
                _intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.EXTRA_DOCK_STATE_CAR);
                _intent.addCategory(Intent.CATEGORY_LAUNCHER);
                _intent.putExtra("foreground", true);
                context.startActivity(_intent);
            }
            onCallReceived(intent);
            break;
        case PjActions.EVENT_CALL_CHANGED:
            onCallChanged(intent);
            break;
        case PjActions.EVENT_CALL_TERMINATED:
            onCallTerminated(intent);
            break;
        default:
            onCallback(intent);
            break;
        }
    }

    private void onRegistrationChanged(Intent intent) {
        String json = intent.getStringExtra("data");
        Object params = ArgumentUtils.fromJson(json);
        emit("pjSipRegistrationChanged", params);
    }

    private void onMessageReceived(Intent intent) {
        String json = intent.getStringExtra("data");
        Object params = ArgumentUtils.fromJson(json);
        emit("pjSipMessageReceived", params);
    }

    private void onCallReceived(Intent intent) {
        String json = intent.getStringExtra("data");
        Object params = ArgumentUtils.fromJson(json);
        emit("pjSipCallReceived", params);
    }

    private void onCallChanged(Intent intent) {
        String json = intent.getStringExtra("data");
        Object params = ArgumentUtils.fromJson(json);
        emit("pjSipCallChanged", params);
    }

    private void onCallTerminated(Intent intent) {
        String json = intent.getStringExtra("data");
        Object params = ArgumentUtils.fromJson(json);
        emit("pjSipCallTerminated", params);
    }

    private void onCallback(Intent intent) {
        // Define callback
        Callback callback = null;

        if (intent.hasExtra("callback_id")) {
            int id = intent.getIntExtra("callback_id", -1);
            if (callbacks.containsKey(id)) {
                callback = callbacks.remove(id);
            } else {
                Log.w(TAG, "Callback with \"" + id + "\" identifier not found (\"" + intent.getAction() + "\")");
            }
        }

        if (callback == null) {
            return;
        }

        // -----
        if (intent.hasExtra("exception")) {
            Log.w(TAG, "Callback executed with exception state: " + intent.getStringExtra("exception"));
            callback.invoke(false, intent.getStringExtra("exception"));
        } else if (intent.hasExtra("data")) {
            Object params = ArgumentUtils.fromJson(intent.getStringExtra("data"));
            callback.invoke(true, params);
        } else {
            callback.invoke(true, true);
        }
    }

    public static void emit(String eventName, @Nullable Object data) {
        Log.d("ReactNative", "emit " + eventName + " / " + data);
        if (cnContext != null)
            cnContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, data);
    }
}
