package com.ridescan.replica;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class ECUDiagnosisActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle(Session.selectedEcu);
        wireBack();
        LinearLayout content = (LinearLayout) findViewById(R.id.content);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(android.view.Gravity.CENTER_VERTICAL);
        head.setBackgroundResource(R.drawable.bg_card_blue);
        int p = Ui.dp(this, 16);
        head.setPadding(p, p, p, p);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2);
        hp.setMargins(0, 0, 0, Ui.dp(this, 12));
        content.addView(head, hp);
        android.widget.ImageView iv = new android.widget.ImageView(this);
        iv.setImageResource(R.drawable.ecuf);
        iv.setColorFilter(0xFFFFFFFF);
        head.addView(iv, new LinearLayout.LayoutParams(Ui.dp(this, 36), Ui.dp(this, 36)));
        LinearLayout mt = new LinearLayout(this);
        mt.setOrientation(LinearLayout.VERTICAL);
        mt.setPadding(Ui.dp(this, 14), 0, 0, 0);
        mt.addView(Ui.tv(this, Session.selectedEcu, 15.5f, 0xFFFFFFFF, true));
        mt.addView(Ui.tv(this, Session.selectedVehicle + "  ·  " + Session.selectedVin, 11.5f, 0xB3FFFFFF, false));
        head.addView(mt);

        content.addView(Ui.section(this, "DIAGNOSTIC FUNCTIONS"));
        Object[][] fns = new Object[][]{
            {"Read DTCs", "Read & clear Diagnostic Trouble Codes", Integer.valueOf(R.drawable.rdtc), ReadDTCsActivity.class},
            {"Live Parameters", "Real-time ECU data monitoring & recording", Integer.valueOf(R.drawable.vci), LiveParameterActivity.class},
            {"IO Control", "Actuator tests — injectors, coils, relays", Integer.valueOf(R.drawable.setting_1), IOControlActivity.class},
            {"Routine Control", "Supplier service routines & adaptations", Integer.valueOf(R.drawable.firmwarecloud), RoutineControlActivity.class},
            {"IUPR Test — Primary", "In-use performance ratio (primary monitors)", Integer.valueOf(R.drawable.flash), IuprTestActivity.class},
            {"IUPR Test — Secondary", "In-use performance ratio (secondary monitors)", Integer.valueOf(R.drawable.flash), IuprTestActivity.class},
            {"Gear Learning", "Transmission gear position learning", Integer.valueOf(R.drawable.manualdiagnostic), RoutineControlActivity.class},
        };
        for (int i = 0; i < fns.length; i++) {
            final Object[] fn = fns[i];
            final boolean secondary = ((String) fn[0]).contains("Secondary");
            LinearLayout row = Ui.listRow(this, ((Integer) fn[2]).intValue(), (String) fn[0], (String) fn[1], true);
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    android.content.Intent it = new android.content.Intent(ECUDiagnosisActivity.this, (Class<?>) fn[3]);
                    it.putExtra("iupr_secondary", secondary);
                    startActivity(it);
                }
            });
            content.addView(row);
        }

        android.widget.Button flashBtn = new android.widget.Button(this);
        flashBtn.setText("Proceed to Flashing");
        flashBtn.setTextColor(0xFF00347E);
        flashBtn.setTextSize(14.5f);
        flashBtn.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        flashBtn.setBackgroundResource(R.drawable.bg_button_outline);
        flashBtn.setAllCaps(false);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 48));
        bp.setMargins(0, Ui.dp(this, 6), 0, Ui.dp(this, 8));
        content.addView(flashBtn, bp);
        flashBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { go(SelectFlashVariantActivity.class); }
        });
    }
}
