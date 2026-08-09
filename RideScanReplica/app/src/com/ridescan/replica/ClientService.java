package com.ridescan.replica;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

/** Foreground session coordinator — mirrors the original ClientService/SerialService pairing:
 *  keeps a persistent notification while a diagnostic session is active. */
public class ClientService extends Service {
    public static final String CHANNEL = "ridescan_session";
    private static final int ID = 41;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(new NotificationChannel(CHANNEL, "RIDE Scan session",
                    NotificationManager.IMPORTANCE_LOW));
        }
        PendingIntent pi = PendingIntent.getActivity(this, 0,
                new Intent(this, HomeActivity.class), PendingIntent.FLAG_IMMUTABLE);
        Notification n = new Notification.Builder(this,
                Build.VERSION.SDK_INT >= 26 ? CHANNEL : null)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("RIDE Scan session active")
                .setContentText(Session.vciConnected ? "VCI: " + Session.vciName : "No VCI connected")
                .setContentIntent(pi)
                .setOngoing(true)
                .build();
        startForeground(ID, n);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
