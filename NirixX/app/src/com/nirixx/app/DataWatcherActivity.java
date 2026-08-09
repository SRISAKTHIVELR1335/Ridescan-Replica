package com.nirixx.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

/** Big-value watch panel on a few live channels (DataWatcher). */
public class DataWatcherActivity extends BaseActivity {
    private final Handler h = new Handler();
    private boolean running = true;
    private TextView v1, v2, v3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Data Watcher");
        wireBack();
        LinearLayout content = (LinearLayout) findViewById(R.id.content);
        content.addView(Ui.section(this, "WATCH CHANNELS"));

        LinearLayout card = Ui.card(this);
        LinearLayout[] rows = new LinearLayout[3];
        v1 = watchRow(card, 0, "Engine Speed", "rpm");
        v2 = watchRow(card, 1, "Battery Voltage", "V");
        v3 = watchRow(card, 2, "Vehicle Speed", "km/h");
        content.addView(card);
        tick();
    }

    private TextView watchRow(LinearLayout parent, int idx, String name, String unit) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(0, Ui.dp(this, 14), 0, Ui.dp(this, 14));
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(Ui.tv(this, name, 13f, 0xFF5A6472, true));
        row.addView(ll, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView v = Ui.tv(this, "—", 30f, 0xFF0B8376, true);
        row.addView(v);
        TextView u = Ui.tv(this, " " + unit, 13f, 0xFF9AA6B4, false);
        row.addView(u);
        parent.addView(row);
        return v;
    }

    private void tick() {
        if (!running) return;
        java.util.Random r = new java.util.Random();
        v1.setText(String.valueOf(1400 + r.nextInt(260)));
        v2.setText(String.format(Locale.US, "%.2f", 12.4f + r.nextFloat() * 0.5f));
        v3.setText("0");
        h.postDelayed(new Runnable() { public void run() { tick(); } }, 900);
    }

    @Override
    protected void onDestroy() {
        running = false;
        super.onDestroy();
    }
}
