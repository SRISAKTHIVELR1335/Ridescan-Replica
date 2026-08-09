package com.nirixx.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** App-side link monitoring (VCI/transport/DMS health) — mirrors SystemMonitoring. */
public class SystemMonitoringActivity extends BaseActivity {
    private final Handler h = new Handler();
    private boolean running = true;
    private TextView[] vals;
    private int tx = 4821, rx = 4790, drops = 3;
    private long start = System.currentTimeMillis();

    private static final String[] NAMES = {
        "BT frames TX", "BT frames RX", "Dropped frames", "ISO-TP errors",
        "Session uptime", "DMS sync status", "VCI signal", "Flash ops today",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("System Monitoring");
        wireBack();
        LinearLayout content = (LinearLayout) findViewById(R.id.content);

        content.addView(Ui.section(this, "LINK HEALTH"));
        vals = new TextView[NAMES.length];
        LinearLayout card = Ui.card(this);
        for (int i = 0; i < NAMES.length; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            int pv = Ui.dp(this, 9);
            row.setPadding(0, pv, 0, pv);
            row.addView(Ui.tv(this, NAMES[i], 13f, 0xFF5A6472, false), new LinearLayout.LayoutParams(0, -2, 1.1f));
            vals[i] = Ui.tv(this, "—", 13f, 0xFF141B2E, true);
            vals[i].setGravity(android.view.Gravity.RIGHT);
            row.addView(vals[i], new LinearLayout.LayoutParams(0, -2, 0.9f));
            card.addView(row);
        }
        content.addView(card);
        tick();
    }

    private void tick() {
        if (!running) return;
        updateVals();
        h.postDelayed(new Runnable() { public void run() { tick(); } }, 1000);
    }

    private void updateVals() {
        if (Session.vciConnected) {
            tx += 2 + new java.util.Random().nextInt(6);
            rx += 2 + new java.util.Random().nextInt(5);
        }
        long secs = (System.currentTimeMillis() - start) / 1000;
        String[] now = {
            String.valueOf(tx), String.valueOf(rx), String.valueOf(drops), "0",
            String.format(java.util.Locale.US, "%02d:%02d:%02d", secs / 3600, (secs / 60) % 60, secs % 60),
            "TTL expired — queued", Session.vciConnected ? "-52 dBm" : "—",
            String.valueOf(Session.reportGenerated ? 1 : 0),
        };
        for (int i = 0; i < vals.length; i++) vals[i].setText(now[i]);
    }

    @Override
    protected void onDestroy() {
        running = false;
        super.onDestroy();
    }
}
