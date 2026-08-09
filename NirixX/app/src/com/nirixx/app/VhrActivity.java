package com.nirixx.app;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VhrActivity extends BaseActivity {
    private static final String[] TABS = {"Dealer Information", "Diagnostic Report", "IO Control", "Summary"};
    private LinearLayout strip, body;
    private final TextView[] tabViews = new TextView[TABS.length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vhr);
        wireBack();
        strip = (LinearLayout) findViewById(R.id.tabStrip);
        body = (LinearLayout) findViewById(R.id.vhrContent);
        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            TextView t = Ui.tv(this, TABS[i], 13f, 0xFF5A6472, true);
            int h = Ui.dp(this, 12);
            t.setPadding(h, 0, h, 0);
            t.setGravity(android.view.Gravity.CENTER);
            t.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { select(idx); }
            });
            tabViews[i] = t;
            strip.addView(t, new LinearLayout.LayoutParams(-2, -1));
        }
        select(0);

        findViewById(R.id.btnExportPdf).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Dialog d = Ui.progressDialog(VhrActivity.this, "Rendering PDF (iText)…");
                d.show();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        d.dismiss();
                        Session.reportGenerated = true;
                        Ui.resultDialog(VhrActivity.this, R.drawable.ic_flash_success,
                                "VHR Generated", "VehicleHealthReport_" + Session.selectedVin + ".pdf\nsaved to /NirixX/Reports/",
                                "OK", null).show();
                    }
                }, 1500);
            }
        });
    }

    private void select(int idx) {
        for (int i = 0; i < TABS.length; i++) {
            tabViews[i].setTextColor(i == idx ? 0xFF252E66 : 0xFF5A6472);
            tabViews[i].setBackgroundResource(0);
        }
        tabViews[idx].setTextColor(0xFF252E66);
        body.removeAllViews();
        switch (idx) {
            case 0: dealerTab(); break;
            case 1: diagTab(); break;
            case 2: ioTab(); break;
            default: summaryTab();
        }
    }

    private void dealerTab() {
        body.addView(Ui.section(this, "DEALER DETAILS"));
        LinearLayout card = Ui.card(this);
        card.addView(Ui.kvRow(this, "Dealership", Session.dealerName, false));
        card.addView(Ui.kvRow(this, "Dealer Code", "NRX-TN-CHE-0417", false));
        card.addView(Ui.kvRow(this, "Technician", Session.dealerEmail.length() > 0 ? Session.dealerEmail : "tech@srsakthimotors.in", false));
        card.addView(Ui.kvRow(this, "Region", "Chennai — Tamil Nadu", false));
        card.addView(Ui.kvRow(this, "Report Date", "09 Aug 2026, 10:14 IST", true));
        body.addView(card);
    }

    private void diagTab() {
        body.addView(Ui.section(this, "DIAGNOSTIC RESULTS"));
        LinearLayout card = Ui.card(this);
        card.addView(Ui.kvRow(this, "Vehicle", Session.selectedVehicle, false));
        card.addView(Ui.kvRow(this, "VIN", Session.selectedVin, false));
        card.addView(Ui.kvRow(this, "ECU", Session.selectedEcu, false));
        card.addView(Ui.kvRow(this, "DTCs Found", Session.dtcsCleared > 0 ? "0 (cleared)" : "5", false));
        card.addView(Ui.kvRow(this, "Live Parameters", "8 channels monitored — nominal", false));
        card.addView(Ui.kvRow(this, "IUPR Monitors", "READY", true));
        body.addView(card);
    }

    private void ioTab() {
        body.addView(Ui.section(this, "ACTUATOR TEST RESULTS"));
        String[][] rows = new String[][]{
            {"Fuel Pump Relay", "PASS"}, {"Injector — Cylinder 1", "PASS"},
            {"Ignition Coil", "PASS"}, {"Cooling Fan", "PASS"},
            {"MIL Lamp", "PASS"}, {"EVAP Purge Valve", "SKIPPED"},
        };
        LinearLayout card = Ui.card(this);
        for (int i = 0; i < rows.length; i++) {
            card.addView(Ui.kvRow(this, rows[i][0], rows[i][1], i == rows.length - 1));
        }
        body.addView(card);
    }

    private void summaryTab() {
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.VERTICAL);
        head.setBackgroundResource(R.drawable.bg_card_blue);
        int p = Ui.dp(this, 18);
        head.setPadding(p, p, p, p);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2);
        hp.setMargins(0, 0, 0, Ui.dp(this, 12));
        body.addView(head, hp);
        TextView big = Ui.tv(this, "VEHICLE HEALTH: GOOD", 18f, 0xFF7DE3A8, true);
        head.addView(big);
        TextView sub = Ui.tv(this, "All systems nominal. One stored DTC cleared during this session.",
                12.5f, 0xB3FFFFFF, false);
        sub.setPadding(0, Ui.dp(this, 6), 0, 0);
        head.addView(sub);

        LinearLayout card = Ui.card(this);
        card.addView(Ui.kvRow(this, "Overall Score", "92 / 100", false));
        card.addView(Ui.kvRow(this, "Battery Health", "92% — GOOD", false));
        card.addView(Ui.kvRow(this, "Emissions Readiness", "READY", false));
        card.addView(Ui.kvRow(this, "Flash Status", Session.reportGenerated ? "UP TO DATE" : "UPDATE AVAILABLE", true));
        body.addView(card);
    }
}
