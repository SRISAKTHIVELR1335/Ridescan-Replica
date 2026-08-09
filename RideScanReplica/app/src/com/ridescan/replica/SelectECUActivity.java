package com.ridescan.replica;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class SelectECUActivity extends BaseActivity {

    private static final String[][] ECUS = {
        {"EMS — Sedemac (UDS)", "EMS", "Engine Management System · BSVI · ISO 14229"},
        {"ABS — Continental MK100", "ABS", "Anti-lock Braking · 1-channel"},
        {"EMS-OBDII — Mikuni CAN", "EMS-OBDII", "OBD Stage II · CAN KWP2000"},
        {"TPMS — Pricol", "TPMS", "Tyre Pressure Monitoring System"},
        {"Keyless ECU", "KEYLESS", "Smart Key / Keyless Go Controller"},
        {"Instrument Cluster U732 TFT", "CLUSTER", "Connected Cluster · TFT Display"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Select ECU");
        wireBack();
        LinearLayout content = (LinearLayout) findViewById(R.id.content);

        LinearLayout veh = Ui.card(this);
        android.widget.TextView t = Ui.tv(this, Session.selectedVehicle, 16f, 0xFF141B2E, true);
        android.widget.TextView s = Ui.tv(this, "VIN: " + Session.selectedVin, 12.5f, 0xFF5A6472, false);
        s.setPadding(0, Ui.dp(this, 4), 0, 0);
        veh.addView(t);
        veh.addView(s);
        content.addView(veh);

        content.addView(Ui.section(this, "CHOOSE ECU TO DIAGNOSE"));
        for (int i = 0; i < ECUS.length; i++) {
            final String[] ecu = ECUS[i];
            int icon = i == 0 ? R.drawable.ecuf : (i == 3 ? R.drawable.battery : (i == 4 ? R.drawable.keyless_ecu_ic : R.drawable.vci));
            LinearLayout row = Ui.listRow(this, icon, ecu[0], ecu[2], true);
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Session.selectedEcu = ecu[0];
                    Session.selectedEcuShort = ecu[1];
                    go(ECUDiagnosisActivity.class);
                }
            });
            content.addView(row);
        }
    }
}
