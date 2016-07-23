package com.devinpetersohn.autoconfigbackgroundservice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * Created by DevinPetersohn on 5/6/15.
 */
public class DismissNotification extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent();
        service.setComponent(new ComponentName(context,SurveyNotifier.class));
        context.stopService(service);
    }
}
