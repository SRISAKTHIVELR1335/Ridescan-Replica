package com.nirixx.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

/** Foreground session coordinator — keeps a persistent notification while a
 *  diagnostic session is active. Typed as a connected-device FGS for Android 14+. */
public class ClientService extends Service {
    public static final String CHANNEL = "nirixx_session";
    private static final int ID = 41;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(new NotificationChannel(CHANNEL, "NirixX session",
                    NotificationManager.IMPORTANCE_LOW));
        }
        PendingIntent pi = PendingIntent.getActivity(this, 0,
                new Intent(this, HomeActivity.class), PendingIntent.FLAG_IMMUTABLE);
        Notification n = new Notification.Builder(this,
                Build.VERSION.SDK_INT >= 26 ? CHANNEL : null)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("NirixX session active")
                .setContentText(Session.vciConnected ? "VCI: " + Session.vciName : "No VCI connected")
                .setContentIntent(pi)
                .setOngoing(true)
                .build();
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
        } else {
            startForeground(ID, n);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
