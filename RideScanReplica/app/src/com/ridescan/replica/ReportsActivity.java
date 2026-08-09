package com.ridescan.replica;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class ReportsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Health Reports");
        wireBack();
        LinearLayout content = (LinearLayout) findViewById(R.id.content);

        android.widget.ImageView banner = new android.widget.ImageView(this);
        banner.setImageResource(R.drawable.motoshield_report_1);
        banner.setAdjustViewBounds(true);
        banner.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 180));
        bp.setMargins(0, 0, 0, Ui.dp(this, 12));
        banner.setBackgroundResource(R.drawable.bg_card);
        content.addView(banner, bp);

        content.addView(Ui.section(this, "GENERATE NEW REPORT"));
        Object[][] reps = new Object[][]{
            {"Diagnostic Report", "DTCs, live data snapshot and flash feedback for the current session", Integer.valueOf(R.drawable.rdtc), DiagnosticReportActivity.class},
            {"Vehicle Health Report (VHR)", "Customer-facing summary: dealer info, diagnostics, IO tests", Integer.valueOf(R.drawable.vehicle_health_report), VhrActivity.class},
            {"Battery Health Report", "State of health, voltage and CCA analysis", Integer.valueOf(R.drawable.battery_health_report), BatteryHealthActivity.class},
        };
        for (int i = 0; i < reps.length; i++) {
            final Object[] rp = reps[i];
            LinearLayout row = Ui.listRow(this, ((Integer) rp[2]).intValue(), (String) rp[0], (String) rp[1], true);
            row.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { go((Class<?>) rp[3]); }
            });
            content.addView(row);
        }

        content.addView(Ui.section(this, "RECENT REPORTS"));
        String[][] recent = new String[][]{
            {"VHR_Ronin_MD634NF4XRCL12345.pdf", "Today 09:52 · 412 KB"},
            {"Diagnostic_RTR1604V_report.pdf", "Yesterday 17:03 · 288 KB"},
            {"BatteryHealth_NTORQ125.pdf", "Mon 11:31 · 154 KB"},
        };
        for (int i = 0; i < recent.length; i++) {
            final String[] r = recent[i];
            LinearLayout row = Ui.listRow(this, 0, r[0], r[1], false);
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { toast("Opening " + r[0] + " in File Viewer…"); }
            });
            content.addView(row);
        }
    }
}
