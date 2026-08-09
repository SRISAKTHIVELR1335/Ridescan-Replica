package com.ridescan.replica;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/** Minimal overlay control service (companion to ScreenRecordOverlayService). */
public class OverlayService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
