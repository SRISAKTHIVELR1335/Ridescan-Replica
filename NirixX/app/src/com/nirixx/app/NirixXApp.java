package com.nirixx.app;

import android.app.Application;
import java.io.PrintWriter;
import java.io.StringWriter;

/** Application shell: captures any uncaught crash so the next launch can show
 *  exactly what failed and where — the first thing a device-only bug needs. */
public class NirixXApp extends Application {

    public static final String PREFS = "nirixx_crash";

    @Override
    public void onCreate() {
        super.onCreate();
        final Thread.UncaughtExceptionHandler upstream = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable error) {
                try {
                    StringWriter sw = new StringWriter();
                    error.printStackTrace(new PrintWriter(sw));
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                            .putString("trace", "thread: " + thread.getName() + "\n" + sw.toString())
                            .putLong("at", System.currentTimeMillis())
                            .commit();
                } catch (Exception ignored) { }
                if (upstream != null) {
                    upstream.uncaughtException(thread, error);
                } else {
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(10);
                }
            }
        });
    }
}
