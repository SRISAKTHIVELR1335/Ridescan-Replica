package com.ridescan.replica;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class AccountActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Account Details");
        wireBack();
        LinearLayout content = (LinearLayout) findViewById(R.id.content);

        content.addView(Ui.section(this, "DEALER PROFILE"));
        LinearLayout card = Ui.card(this);
        card.addView(Ui.kvRow(this, "Dealership", Session.dealerName, false));
        card.addView(Ui.kvRow(this, "Dealer Code", "TVS-TN-CHE-0417", false));
        card.addView(Ui.kvRow(this, "Email", Session.dealerEmail.length() > 0 ? Session.dealerEmail : "—", false));
        card.addView(Ui.kvRow(this, "Branch", "Chennai — Anna Salai", false));
        card.addView(Ui.kvRow(this, "Designation", "Senior Technician", true));
        content.addView(card);

        content.addView(Ui.section(this, "APPLICATION"));
        LinearLayout app = Ui.card(this);
        app.addView(Ui.kvRow(this, "Version", "2.3.6 (replica)", false));
        app.addView(Ui.kvRow(this, "VCI Firmware", Session.vciFw, false));
        app.addView(Ui.kvRow(this, "Flash config", "flash_variant.json · bundled", true));
        content.addView(app);

        Button out = new Button(this);
        out.setText("Logout");
        out.setTextColor(0xFFFFFFFF);
        out.setAllCaps(false);
        out.setBackgroundResource(R.drawable.bg_button_red);
        LinearLayout.LayoutParams op = new LinearLayout.LayoutParams(-1, Ui.dp(this, 48));
        op.setMargins(0, Ui.dp(this, 8), 0, 0);
        content.addView(out, op);
        out.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Ui.dialog(AccountActivity.this, "Logout?", "You will need to sign in again to use diagnostic functions.",
                        "Logout", new Runnable() {
                            public void run() {
                                Session.dealerEmail = "";
                                Session.vciConnected = false;
                                Session.vciName = "";
                                Intent it = new Intent(AccountActivity.this, LoginActivity.class);
                                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(it);
                                finish();
                            }
                        }, "Cancel", null).show();
            }
        });
    }
}
