package com.ridescan.replica;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class BatteryHealthActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);
        wireBack();

        LinearLayout rows = (LinearLayout) findViewById(R.id.statRows);
        String[][] data = new String[][]{
            {"Terminal Voltage (key ON)", "12.64 V"},
            {"Cranking Voltage Drop", "9.8 V — within limits"},
            {"Cold Cranking Amps (CCA)", "210 A (rated 220 A)"},
            {"Internal Resistance", "8.2 mΩ"},
            {"Charging System Output", "14.2 V @ 3000 rpm"},
            {"Ripple", "62 mV — healthy"},
            {"Estimated SoH", "92%"},
            {"Recommendation", "No action required"},
        };
        for (int i = 0; i < data.length; i++) {
            rows.addView(Ui.kvRow(this, data[i][0], data[i][1], i == data.length - 1));
            rows.setPadding(Ui.dp(this, 10), 0, Ui.dp(this, 10), 0);
        }

        findViewById(R.id.btnGenReport).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Ui.resultDialog(BatteryHealthActivity.this, R.drawable.ic_flash_success,
                        "Report Generated",
                        "BatteryHealth_" + Session.selectedVin + ".pdf\nsaved to /RideScan/Reports/",
                        "OK", null).show();
            }
        });
    }
}
