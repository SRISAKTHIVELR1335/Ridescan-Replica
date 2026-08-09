package com.nirixx.app;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

/** Manual diagnostic path when VIN auto-identification is unavailable. */
public class ManualDiagnosticActivity extends BaseActivity {
    private static final String[][] MODELS = {
        {"TVS Ronin 225", "RR310_REFRESH · Sedemac EMS (UDS)"},
        {"TVS Apache RTR 160 4V", "RTR160_4V_1CH_EFI_BSVI"},
        {"TVS Apache RTR 200 4V", "RTR200_BTO variants"},
        {"TVS Raider 125", "RTR160_2V family · KEMS / Sedemac"},
        {"TVS NTORQ 125", "NTQ125_EFI · Connected cluster"},
        {"TVS Jupiter 110", "JUP110_ISG"},
        {"TVS XL100", "XL100_OBD2B · Sedemac"},
        {"TVS iQube", "EV platform · multiple ECUs"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Manual Diagnostic");
        wireBack();
        LinearLayout content = (LinearLayout) findViewById(R.id.content);

        android.widget.TextView note = Ui.tv(this,
                "VIN auto-identification unavailable? Pick the model manually — the app will route to the correct ECU flow.",
                12.5f, 0xFF5A6472, false);
        note.setBackgroundResource(R.drawable.bg_card);
        note.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12));
        content.addView(note);

        content.addView(Ui.section(this, "SELECT MODEL"));
        for (int i = 0; i < MODELS.length; i++) {
            final String[] m = MODELS[i];
            LinearLayout row = Ui.listRow(this, R.drawable.motorcycle, m[0], m[1], true);
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Session.selectedVehicle = m[0];
                    Session.selectedVariant = m[1].split(" ")[0];
                    go(SelectECUActivity.class);
                }
            });
            content.addView(row);
        }
    }
}
