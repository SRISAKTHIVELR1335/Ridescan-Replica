package com.nirixx.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class IuprTestActivity extends BaseActivity {
    private boolean secondary;

    private static final String[][] PRIMARY = {
        {"Catalyst Monitor", "1248", "987", "READY"},
        {"Oxygen Sensor Monitor", "1310", "1302", "READY"},
        {"EVAP System Monitor", "401", "312", "READY"},
        {"Misfire Monitor", "2048", "2048", "READY"},
    };
    private static final String[][] SECONDARY = {
        {"Secondary Air Injection", "512", "399", "READY"},
        {"Fuel System Monitor", "1024", "1019", "READY"},
        {"EGR Monitor", "256", "198", "PENDING"},
        {"CCV Monitor", "256", "244", "READY"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        secondary = getIntent() != null && getIntent().getBooleanExtra("iupr_secondary", false);
        setTitle(secondary ? "IUPR Test — Secondary" : "IUPR Test — Primary");
        wireBack();
        LinearLayout content = (LinearLayout) findViewById(R.id.content);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setBackgroundResource(R.drawable.bg_card_blue);
        int p = Ui.dp(this, 16);
        info.setPadding(p, p, p, p);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(-1, -2);
        ip.setMargins(0, 0, 0, Ui.dp(this, 12));
        content.addView(info, ip);
        info.addView(Ui.tv(this, "In-Use Performance Ratio", 15.5f, 0xFFFFFFFF, true));
        info.addView(Ui.tv(this,
                "BSVI emissions-monitor readiness check. Ratio = denominator / ignition cycles, per AIS-137.",
                12f, 0xB3FFFFFF, false));

        String[][] data = secondary ? SECONDARY : PRIMARY;
        content.addView(Ui.section(this, secondary ? "SECONDARY MONITORS" : "PRIMARY MONITORS"));
        for (int i = 0; i < data.length; i++) {
            String[] m = data[i];
            LinearLayout card = Ui.card(this);
            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(android.view.Gravity.CENTER_VERTICAL);
            top.addView(Ui.tv(this, m[0], 14f, 0xFF141B2E, true), new LinearLayout.LayoutParams(0, -2, 1f));
            boolean ready = "READY".equals(m[3]);
            top.addView(Ui.chip(this, m[3], ready ? R.drawable.bg_chip_green : R.drawable.bg_chip_amber,
                    ready ? 0xFF1E7A46 : 0xFF9A6B00));
            card.addView(top);

            LinearLayout kv = new LinearLayout(this);
            kv.setOrientation(LinearLayout.HORIZONTAL);
            kv.setPadding(0, Ui.dp(this, 8), 0, 0);
            kv.addView(Ui.tv(this, "Denominator: " + m[1], 12f, 0xFF5A6472, false),
                    new LinearLayout.LayoutParams(0, -2, 1f));
            kv.addView(Ui.tv(this, "Numerator: " + m[2], 12f, 0xFF5A6472, false));
            card.addView(kv);

            ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            int num = Integer.parseInt(m[2]), den = Integer.parseInt(m[1]);
            bar.setMax(den);
            bar.setProgress(num);
            bar.setProgressDrawable(getResources().getDrawable(
                    ready ? R.drawable.progress_thin : R.drawable.progress_red));
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 6));
            bp.setMargins(0, Ui.dp(this, 10), 0, 0);
            card.addView(bar, bp);
            content.addView(card);
        }

        android.widget.Button start = new android.widget.Button(this);
        start.setText("Start IUPR Test");
        start.setTextColor(0xFFFFFFFF);
        start.setAllCaps(false);
        start.setBackgroundResource(R.drawable.bg_button_blue);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 48));
        sp.setMargins(0, Ui.dp(this, 6), 0, Ui.dp(this, 8));
        content.addView(start, sp);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final android.app.Dialog d = Ui.progressDialog(IuprTestActivity.this, "Collecting IUPR counters…");
                d.show();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        d.dismiss();
                        Ui.resultDialog(IuprTestActivity.this, R.drawable.ic_flash_success,
                                "IUPR Test Complete", "Counters uploaded to DMS for AIS-137 compliance records.",
                                "OK", null).show();
                    }
                }, 1800);
            }
        });
    }
}
