package com.nirixx.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ScrollView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Support chatbot ("Welcome to NirixX Assistant!") with canned technician Q&A. */
public class SupportChatActivity extends BaseActivity {
    private LinearLayout chat;
    private ScrollView scroll;
    private final Handler h = new Handler();

    private static final String[][] QNA = {
        {"flash", "For ECU flashing: connect the VCI, pick ECU Flashing → your supplier module, confirm the IMAGE type, and keep the app in foreground until 'ECU Flashing completed'. Never abort mid-program — retry instead."},
        {"dtc", "To read fault codes: VIN Based Diagnosis → Get Vehicle Details → Select ECU → Read DTCs. Use 'Clear DTCs' only after noting freeze-frame data."},
        {"vci", "If the VCI won't pair: power-cycle the dongle, enable Location for BLE scanning, then Add Device → scan. Firmware is updated under VCI Firmware Update."},
        {"firmware", "VCI firmware: Home → VCI Firmware Update → pick your dongle generation (TZ VCI / Mini / New / NRX Pro). Recovery uses the bundled Renesas image."},
        {"report", "Reports: Health Reports tile → Diagnostic / VHR / Battery. Export as PDF, then Upload to DMS for sync."},
        {"iupr", "IUPR runs from the ECU diagnosis screen (Primary/Secondary). Collect counters after a ride cycle for AIS-137 compliance."},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("NirixX Assistant");
        wireBack();

        LinearLayout content = (LinearLayout) findViewById(R.id.content);
        chat = new LinearLayout(this);
        chat.setOrientation(LinearLayout.VERTICAL);
        content.addView(chat, new LinearLayout.LayoutParams(-1, Ui.dp(this, 300)));

        botSay("Welcome to NirixX Assistant!\nAsk me about flashing, DTCs, VCI pairing, firmware, reports or IUPR.");
        suggest("How do I flash an ECU?", "flash");
        suggest("My VCI is not pairing", "vci");
        suggest("Export health reports", "report");

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        final EditText in = new EditText(this);
        in.setHint("Type a question…");
        in.setTextSize(13.5f);
        in.setSingleLine(true);
        in.setBackgroundResource(R.drawable.bg_edittext);
        in.setPadding(Ui.dp(this, 12), 0, Ui.dp(this, 12), 0);
        row.addView(in, new LinearLayout.LayoutParams(0, Ui.dp(this, 46), 1f));
        android.widget.Button send = new android.widget.Button(this);
        send.setText("Send");
        send.setTextColor(0xFFFFFFFF);
        send.setAllCaps(false);
        send.setBackgroundResource(R.drawable.bg_button_blue);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(Ui.dp(this, 84), Ui.dp(this, 46));
        sp.setMargins(Ui.dp(this, 8), 0, 0, 0);
        row.addView(send, sp);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, -2);
        rp.setMargins(0, Ui.dp(this, 8), 0, Ui.dp(this, 8));
        content.addView(row, rp);

        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String q = in.getText().toString().trim();
                if (q.length() == 0) return;
                in.setText("");
                userSay(q);
                answer(q.toLowerCase(java.util.Locale.US));
            }
        });
    }

    private void suggest(final String label, final String key) {
        TextView chip = Ui.chip(this, label, R.drawable.bg_chip, 0xFF0B8376);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-2, -2);
        cp.setMargins(Ui.dp(this, 4), Ui.dp(this, 6), 0, 0);
        chat.addView(chip, cp);
        chip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                userSay(label);
                answer(key);
            }
        });
    }

    private void botSay(String text) { bubble(text, true); }
    private void userSay(String text) { bubble(text, false); }

    private void bubble(String text, boolean bot) {
        TextView t = Ui.tv(this, text, 13f, bot ? 0xFF141B2E : 0xFFFFFFFF, false);
        t.setBackground(Ui.roundRect(bot ? 0xFFE4F5FB : 0xFF0B8376, 12, this));
        int hp = Ui.dp(this, 12), vp = Ui.dp(this, 8);
        t.setPadding(hp, vp, hp, vp);
        t.setLineSpacing(1.25f, 1f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMargins(bot ? 0 : Ui.dp(this, 48), Ui.dp(this, 6), bot ? Ui.dp(this, 48) : 0, 0);
        lp.gravity = bot ? android.view.Gravity.LEFT : android.view.Gravity.RIGHT;
        chat.addView(t, lp);
    }

    private void answer(final String q) {
        h.postDelayed(new Runnable() {
            public void run() {
                for (int i = 0; i < QNA.length; i++) {
                    if (q.contains(QNA[i][0])) { botSay(QNA[i][1]); return; }
                }
                botSay("I'm the in-app helper — try keywords like: flash, dtc, vci, firmware, report, iupr. For anything else, raise a ticket on the DMS portal.");
            }
        }, 700);
    }
}
