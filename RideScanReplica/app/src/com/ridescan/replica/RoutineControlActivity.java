package com.ridescan.replica;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RoutineControlActivity extends BaseActivity {
    private LinearLayout content;
    private TextView console;
    private final Handler h = new Handler();

    private static final String[][] ROUTINES = {
        {"ECU Reset", "0x31 01 FF00 — hard reset of ECU"},
        {"Idle Air Control Learn", "0x31 01 0210 — idle trim relearn"},
        {"Throttle Adaptation", "0x31 01 0222 — throttle body alignment"},
        {"O2 Sensor Heater Test", "0x31 01 0540 — heater circuit check"},
        {"Gear Position Learning", "0x31 01 0341 — transmission gear learn"},
        {"Injector Balance Test", "0x31 01 0570 — cylinder cut-out balance"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle(getIntent() != null && getIntent().hasExtra("iupr_secondary") ? "Routine Control" : "Routine Control — " + Session.selectedEcuShort);
        wireBack();
        content = (LinearLayout) findViewById(R.id.content);

        content.addView(Ui.section(this, "SERVICE ROUTINES (UDS 0x31 — ROUTINECONTROL)"));
        for (int i = 0; i < ROUTINES.length; i++) {
            final String[] rt = ROUTINES[i];
            LinearLayout card = Ui.card(this);
            top(card, rt[0], rt[1]);
            content.addView(card);
        }

        content.addView(Ui.section(this, "SESSION LOG"));
        LinearLayout cons = new LinearLayout(this);
        cons.setOrientation(LinearLayout.VERTICAL);
        cons.setBackgroundResource(R.drawable.bg_console);
        cons.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12));
        console = Ui.tv(this, "> RideScan routine console ready\n> ECU session: " + Session.selectedEcu + "\n", 11.5f, 0xFF8FD6A4, false);
        console.setTypeface(android.graphics.Typeface.MONOSPACE);
        cons.addView(console);
        content.addView(cons);
    }

    private void top(LinearLayout card, final String title, String desc) {
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout mid = new LinearLayout(this);
        mid.setOrientation(LinearLayout.VERTICAL);
        mid.addView(Ui.tv(this, title, 14.5f, 0xFF141B2E, true));
        TextView d = Ui.tv(this, desc, 11.5f, 0xFF5A6472, false);
        d.setPadding(0, Ui.dp(this, 2), 0, 0);
        mid.addView(d);
        top.addView(mid, new LinearLayout.LayoutParams(0, -2, 1f));
        Button run = new Button(this);
        run.setText("Run");
        run.setTextColor(0xFFFFFFFF);
        run.setAllCaps(false);
        run.setTextSize(13f);
        run.setBackgroundResource(R.drawable.bg_button_blue);
        top.addView(run, new LinearLayout.LayoutParams(Ui.dp(this, 84), Ui.dp(this, 40)));
        card.addView(top);

        run.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { runRoutine(title); }
        });
    }

    private void runRoutine(final String name) {
        log("> ────────────────────────────");
        log("> Starting: " + name);
        h.postDelayed(new Runnable() { public void run() { log("> Sending 0x31 routine request…"); } }, 500);
        h.postDelayed(new Runnable() { public void run() { log("> ECU routine control accepted"); } }, 1300);
        h.postDelayed(new Runnable() { public void run() { log("> Executing…  [03.2 s]"); } }, 2100);
        h.postDelayed(new Runnable() { public void run() {
            log("> ✔ " + name + " completed successfully");
            toast(name + " completed");
        } }, 3000);
    }

    private void log(String line) {
        console.setText(console.getText() + line + "\n");
    }
}
