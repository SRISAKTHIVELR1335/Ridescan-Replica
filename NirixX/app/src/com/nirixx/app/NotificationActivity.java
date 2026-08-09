package com.nirixx.app;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NotificationActivity extends BaseActivity {
    private static final String[][] NOTIFS = {
        {"VCI Firmware Updates Available!", "Version 2.19.1 is available for your TZ VCI and NRX Pro devices.", "2h ago", "true"},
        {"App Update — NirixX 1.3.3", "Adds Raider 125 ISG flash support and U796 cluster coverage.", "Yesterday", "true"},
        {"New flash files released", "U732 TFT cluster images (RLCD & TFT) pushed to flash_variant config.", "Mon", "false"},
        {"Scheduled DMS maintenance", "DMS sync will be unavailable Sun 01:00–03:00 IST.", "Sun", "false"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Notifications");
        wireBack();
        LinearLayout content = (LinearLayout) findViewById(R.id.content);
        content.addView(Ui.section(this, "DEALER ALERTS"));
        for (int i = 0; i < NOTIFS.length; i++) {
            final String[] n = NOTIFS[i];
            LinearLayout card = Ui.card(this);
            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(android.view.Gravity.CENTER_VERTICAL);
            View dot = new View(this);
            if ("true".equals(n[3])) {
                dot.setBackground(Ui.roundRect(0xFFE63946, 20, this));
                LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(Ui.dp(this, 8), Ui.dp(this, 8));
                dp.setMargins(0, 0, Ui.dp(this, 8), 0);
                top.addView(dot, dp);
            }
            top.addView(Ui.tv(this, n[0], 14f, 0xFF141B2E, true), new LinearLayout.LayoutParams(0, -2, 1f));
            top.addView(Ui.tv(this, n[2], 11f, 0xFF9AA6B4, false));
            card.addView(top);
            TextView b = Ui.tv(this, n[1], 12.5f, 0xFF5A6472, false);
            b.setPadding(0, Ui.dp(this, 6), 0, 0);
            card.addView(b);
            content.addView(card);
        }
    }
}
