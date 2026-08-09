package com.ridescan.replica;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class IOControlActivity extends BaseActivity {

    private static final String[] ACTUATORS = {
        "Fuel Pump Relay", "Injector — Cylinder 1", "Ignition Coil",
        "Cooling Fan", "Malfunction Indicator Lamp (MIL)", "EVAP Purge Valve"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("IO Control — " + Session.selectedEcuShort);
        wireBack();
        LinearLayout content = (LinearLayout) findViewById(R.id.content);

        content.addView(Ui.section(this, "ACTUATOR TESTS (UDS 0x2F — INPUTOUTPUTCONTROLBYIDENTIFIER)"));

        for (int i = 0; i < ACTUATORS.length; i++) {
            final String name = ACTUATORS[i];
            LinearLayout card = Ui.card(this);
            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(android.view.Gravity.CENTER_VERTICAL);
            top.addView(Ui.tv(this, name, 14.5f, 0xFF141B2E, true), new LinearLayout.LayoutParams(0, -2, 1f));
            final Switch sw = new Switch(this);
            top.addView(sw);
            card.addView(top);
            final TextView status = Ui.tv(this, "INACTIVE", 11.5f, 0xFF5A6472, true);
            status.setPadding(0, Ui.dp(this, 4), 0, 0);
            card.addView(status);

            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton btn, boolean on) {
                    status.setText(on ? "ACTIVE — actuator energised" : "INACTIVE");
                    status.setTextColor(on ? 0xFF1E7A46 : 0xFF5A6472);
                    if (on) {
                        toast(name + ": activation requested");
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                if (sw.isChecked()) {
                                    sw.setChecked(false);
                                    status.setText("AUTO-OFF after 5 s (safety)");
                                }
                            }
                        }, 5000);
                    }
                }
            });
            content.addView(card);
        }

        TextView note = Ui.tv(this,
                "Caution: actuator tests are momentary. Verify DTCs and re-run live parameters after IO tests.",
                12f, 0xFF5A6472, false);
        note.setBackgroundResource(R.drawable.bg_card);
        note.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12));
        content.addView(note);
    }
}
