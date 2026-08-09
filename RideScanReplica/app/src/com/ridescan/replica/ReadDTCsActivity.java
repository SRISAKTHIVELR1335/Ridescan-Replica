package com.ridescan.replica;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ReadDTCsActivity extends BaseActivity {
    private LinearLayout content;
    private boolean cleared = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Read DTCs");
        wireBack();
        content = (LinearLayout) findViewById(R.id.content);
        addBottomBar();
        build();
    }

    private void build() {
        content.removeAllViews();

        LinearLayout summary = new LinearLayout(this);
        summary.setOrientation(LinearLayout.HORIZONTAL);
        summary.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.setMargins(0, 0, 0, Ui.dp(this, 10));
        content.addView(summary, sp);
        TextView chip;
        if (!cleared && Session.dtcsCleared == 0) {
            chip = Ui.chip(this, "VCI: " + (Session.vciConnected ? Session.vciName : "demo session"),
                    R.drawable.bg_chip, 0xFF00347E);
            summary.addView(chip);
            TextView count = Ui.chip(this, Session.DTC_DATA.length + " DTCs found",
                    R.drawable.bg_chip_red, 0xFFC41230);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-2, -2);
            cp.setMargins(Ui.dp(this, 8), 0, 0, 0);
            summary.addView(count, cp);
        } else {
            chip = Ui.chip(this, " " , R.drawable.bg_chip, 0xFF00347E);
            chip.setVisibility(View.GONE);
            summary.addView(chip);
            TextView ok = Ui.chip(this, "No DTCs — system healthy", R.drawable.bg_chip_green, 0xFF1E7A46);
            summary.addView(ok);
        }

        if (!cleared && Session.dtcsCleared == 0) {
            content.addView(Ui.section(this, "DIAGNOSTIC TROUBLE CODES"));
            for (int i = 0; i < Session.DTC_DATA.length; i++) {
                final String[] dtc = Session.DTC_DATA[i];
                LinearLayout card = Ui.card(this);
                LinearLayout top = new LinearLayout(this);
                top.setOrientation(LinearLayout.HORIZONTAL);
                top.setGravity(android.view.Gravity.CENTER_VERTICAL);
                TextView code = Ui.tv(this, dtc[0], 16f, 0xFF141B2E, true);
                top.addView(code, new LinearLayout.LayoutParams(0, -2, 1f));
                int chipBg = "Active".equals(dtc[2]) ? R.drawable.bg_chip_red
                        : ("Stored".equals(dtc[2]) ? R.drawable.bg_chip_amber : R.drawable.bg_chip);
                int chipFg = "Active".equals(dtc[2]) ? 0xFFC41230
                        : ("Stored".equals(dtc[2]) ? 0xFF9A6B00 : 0xFF5A6472);
                top.addView(Ui.chip(this, dtc[2], chipBg, chipFg));
                card.addView(top);
                TextView desc = Ui.tv(this, dtc[1], 13f, 0xFF5A6472, false);
                desc.setPadding(0, Ui.dp(this, 4), 0, 0);
                card.addView(desc);
                card.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Ui.dialog(ReadDTCsActivity.this, dtc[0], dtc[1]
                                        + "\n\nFreeze frame:\nEngine RPM: 1488 rpm\nVehicle speed: 0 km/h\nCoolant temp: 87 °C",
                                "Close", null, null, null).show();
                    }
                });
                content.addView(card);
            }
        } else {
            LinearLayout empty = new LinearLayout(this);
            empty.setOrientation(LinearLayout.VERTICAL);
            empty.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
            empty.setPadding(0, Ui.dp(this, 48), 0, 0);
            android.widget.ImageView iv = new android.widget.ImageView(this);
            iv.setImageResource(R.drawable.ic_flash_success);
            empty.addView(iv, new LinearLayout.LayoutParams(Ui.dp(this, 64), Ui.dp(this, 64)));
            TextView t = Ui.tv(this, "No DTCs found", 16f, 0xFF141B2E, true);
            t.setPadding(0, Ui.dp(this, 14), 0, 0);
            empty.addView(t);
            empty.addView(Ui.tv(this, "All monitored systems report OK", 12.5f, 0xFF5A6472, false));
            content.addView(empty);
        }
    }

    private void addBottomBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(android.view.Gravity.CENTER);
        bar.setPadding(Ui.dp(this, 16), Ui.dp(this, 10), Ui.dp(this, 16), Ui.dp(this, 16));

        Button refresh = new Button(this);
        refresh.setText("Refresh");
        refresh.setTextColor(0xFF00347E);
        refresh.setAllCaps(false);
        refresh.setBackgroundResource(R.drawable.bg_button_outline);
        bar.addView(refresh, new LinearLayout.LayoutParams(0, Ui.dp(this, 46), 1f));

        View gap = new View(this);
        bar.addView(gap, new LinearLayout.LayoutParams(Ui.dp(this, 10), 1));

        Button clear = new Button(this);
        clear.setText("Clear DTCs");
        clear.setTextColor(0xFFFFFFFF);
        clear.setAllCaps(false);
        clear.setBackgroundResource(R.drawable.bg_button_red);
        bar.addView(clear, new LinearLayout.LayoutParams(0, Ui.dp(this, 46), 1f));

        ((android.widget.FrameLayout) findViewById(R.id.bottomBar)).addView(bar);

        refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Dialog d = Ui.progressDialog(ReadDTCsActivity.this, "Reading DTCs…");
                d.show();
                v.postDelayed(new Runnable() { public void run() { d.dismiss(); build(); } }, 900);
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Ui.dialog(ReadDTCsActivity.this, "Clear all DTCs?",
                        "This issues UDS service 0x14 (ClearDiagnosticInformation) to the ECU.\nStored and pending codes will be erased.",
                        "Clear", new Runnable() {
                            public void run() {
                                cleared = true;
                                Session.dtcsCleared++;
                                build();
                                toast("DTCs cleared");
                            }
                        }, "Cancel", null).show();
            }
        });
    }
}
