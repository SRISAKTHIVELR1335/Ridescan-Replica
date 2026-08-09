package com.ridescan.replica;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;

/** Pair with a VCI dongle (TZ VCI / TZ Mini VCI / TechPRO). */
public class AddDeviceActivity extends BaseActivity {
    private LinearLayout content;
    private final Handler h = new Handler();

    private static final String[][] DEVICES = {
        {"TVS-TZ-VCI-24F1", "TZ VCI · Classic BT · RSSI -48 dBm"},
        {"TZ-MINI-VCI-8DA2", "TZ Mini VCI · Classic BT · RSSI -57 dBm"},
        {"TZ-NEW-VCI-31C7", "TZ New VCI · BLE · RSSI -63 dBm"},
        {"TECHPRO-VCI-1193", "TechPRO VCI · Classic BT · RSSI -71 dBm"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Add Device");
        wireBack();
        content = (LinearLayout) findViewById(R.id.content);
        scan();
    }

    private void scan() {
        content.removeAllViews();
        content.addView(Ui.section(this, "SCANNING FOR VCI DEVICES…"));
        final Dialog d = Ui.progressDialog(this, "Scanning nearby devices…");
        d.show();
        h.postDelayed(new Runnable() {
            public void run() {
                d.dismiss();
                showDevices();
            }
        }, 1200);
    }

    private void showDevices() {
        content.removeAllViews();
        content.addView(Ui.section(this, "NEARBY DEVICES"));
        for (int i = 0; i < DEVICES.length; i++) {
            final String[] dev = DEVICES[i];
            LinearLayout row = Ui.listRow(this, R.drawable.vci, dev[0], dev[1], true);
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { connect(dev[0]); }
            });
            content.addView(row);
        }
        LinearLayout foot = Ui.listRow(this, R.drawable.scan, "Device not listed?",
                "Make sure the VCI is plugged in and in pairing mode, then rescan", false);
        foot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { scan(); }
        });
        content.addView(foot);
    }

    private void connect(final String name) {
        final Dialog d = Ui.progressDialog(this, "Pairing with " + name + " …");
        d.show();
        h.postDelayed(new Runnable() {
            public void run() {
                d.dismiss();
                Session.vciConnected = true;
                Session.vciName = name;
                Ui.resultDialog(AddDeviceActivity.this, R.drawable.ic_flash_success,
                        "VCI Connected", name + " is now paired over Bluetooth Classic.\nFirmware: " + Session.vciFw,
                        "Done", new Runnable() {
                            public void run() { finish(); }
                        }).show();
            }
        }, 1500);
    }
}
