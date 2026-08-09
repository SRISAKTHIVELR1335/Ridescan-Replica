package com.nirixx.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/** Cleanup hook on app task removal — releases the mock session/BT link (like AppCloseService). */
public class AppCloseService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Session.vciConnected = false;
        stopSelf();
        stopService(new Intent(this, ClientService.class));
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
