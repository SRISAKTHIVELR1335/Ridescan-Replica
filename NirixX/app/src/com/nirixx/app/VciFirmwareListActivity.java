package com.nirixx.app;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

/** VCI dongle firmware flows, one per hardware generation (as in the original app). */
public class VciFirmwareListActivity extends BaseActivity {
    private static final String[][] DEVICES = {
        {"NRX Pro VCI", "FirmwareUpdate", "PC tool + over-app update available"},
        {"TZ 24V VCI", "TZ24vVCIFirmwareUpdate", "24-volt commercial dongle"},
        {"TZ Mini VCI", "TZMiniVCIFirmwareUpdate", "compact classic-BT dongle"},
        {"TZ New VCI", "TZNewVCIFirmwareUpdate", "BLE-capable new generation"},
        {"TZ VCI", "TZVCIFirmwareUpdate", "original TZ VCI hardware"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("VCI Firmware Update");
        wireBack();
        LinearLayout content = (LinearLayout) findViewById(R.id.content);

        content.addView(Ui.section(this, "SELECT VCI HARDWARE"));
        for (int i = 0; i < DEVICES.length; i++) {
            final String[] d = DEVICES[i];
            LinearLayout row = Ui.listRow(this, R.drawable.vci, d[0], d[2] + "  ·  " + d[1], true);
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    android.content.Intent it = new android.content.Intent(VciFirmwareListActivity.this, FirmwareUpdateActivity.class);
                    it.putExtra("device", d[0]);
                    startActivity(it);
                }
            });
            content.addView(row);
        }

        android.widget.TextView tip = Ui.tv(this,
                "Tip: Keep the dongle plugged into the OBD port during the update. Recovery uses the bundled Renesas 78K0R image (vcf-renesas_78k0r.s19).",
                12f, 0xFF5A6472, false);
        tip.setBackgroundResource(R.drawable.bg_card);
        tip.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12));
        content.addView(tip);
    }
}
