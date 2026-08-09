package com.ridescan.replica;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

/** Session screen-record overlay (abort-safe stub):
 *  tracks "recording" state like the original hbRecorder-backed overlay, without MediaProjection. */
public class ScreenRecordOverlayService extends Service {
    public static boolean recording = false;
    public static long startedAt = 0L;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "stop".equals(intent.getAction())) {
            recording = false;
            stopSelf();
            return START_NOT_STICKY;
        }
        recording = true;
        startedAt = System.currentTimeMillis();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                // keep-alive heartbeat for the fake overlay
            }
        }, 1000);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
