package com.example.ClapClapToFindPhone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RestartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("YourServiceRestartAction")) {
            Intent serviceIntent = new Intent(context, VocalService.class);
            context.startService(serviceIntent);
        }
    }
}
