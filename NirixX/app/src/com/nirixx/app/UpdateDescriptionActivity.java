package com.nirixx.app;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

/** App-update release notes page (UpdateDescriptionActivity in the original). */
public class UpdateDescriptionActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("What's New in 1.6.0");
        wireBack();
        LinearLayout content = (LinearLayout) findViewById(R.id.content);

        android.widget.ImageView art = new android.widget.ImageView(this);
        art.setImageResource(R.drawable.update_art);
        art.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        art.setBackgroundResource(R.drawable.bg_console);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, Ui.dp(this, 132));
        ap.setMargins(0, 0, 0, Ui.dp(this, 10));
        content.addView(art, ap);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.VERTICAL);
        head.setBackgroundResource(R.drawable.bg_card_blue);
        int p = Ui.dp(this, 18);
        head.setPadding(p, p, p, p);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2);
        hp.setMargins(0, 0, 0, Ui.dp(this, 12));
        content.addView(head, hp);
        head.addView(Ui.tv(this, "NirixX 1.6.0", 18f, 0xFFFFFFFF, true));
        head.addView(Ui.tv(this, "Honesty pass — every value on every screen is real",
                12.5f, 0xB3FFFFFF, false));

        content.addView(Ui.section(this, "RELEASE NOTES"));
        LinearLayout notes = Ui.card(this);
        String[] items = new String[]{
            "Real UDS/ISO-TP diagnostic stack over BT-SPP, Wi-Fi TCP and USB-CDC VCIs",
            "Real DTC read/clear, live parameters (SAE J1979), IUPR-identifiers and battery voltage",
            "Real flash pipeline (34/36/37) driven by imported binaries; ECU refusals shown with true NRCs",
            "Service Manual is now a real document shelf (import / open / manage)",
            "Recorded sessions list actual CSV files; replay shows real captures only",
            "Role-based access (Dealer Service / Dealer Engineer) enforced in navigation and operations",
            "Everything that lacks a published definition now says so — nothing is simulated",
        };
        for (int i = 0; i < items.length; i++) {
            android.widget.TextView t = Ui.tv(this, "•  " + items[i], 13.5f, 0xFF141B2E, false);
            t.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
            notes.addView(t);
        }
        content.addView(notes);

        android.widget.Button update = new android.widget.Button(this);
        update.setText("Update Now");
        update.setTextColor(0xFFFFFFFF);
        update.setAllCaps(false);
        update.setTextSize(15f);
        update.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        update.setBackgroundResource(R.drawable.bg_button_blue);
        LinearLayout.LayoutParams up = new LinearLayout.LayoutParams(-1, Ui.dp(this, 50));
        up.setMargins(0, Ui.dp(this, 6), 0, Ui.dp(this, 8));
        content.addView(update, up);
        update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { go(UpdateActivity.class); }
        });
    }
}
