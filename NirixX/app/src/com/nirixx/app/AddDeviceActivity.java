package com.nirixx.app;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nirixx.app.vci.VciManager;
import com.nirixx.app.vci.VciTransport;

/** Pair with a VCI — real Bluetooth discovery (when the radio allows) merged
 *  with the simulation pool, plus the full supported-VCI catalog. */
public class AddDeviceActivity extends BaseActivity implements VciManager.ScanCallback {
    private LinearLayout content;
    private TextView status;
    private Dialog scanDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Add Device");
        wireBack();
        content = (LinearLayout) findViewById(R.id.content);
        scan();
    }

    @Override
    protected void onDestroy() {
        VciManager.get().stopScan(this);
        super.onDestroy();
    }

    private void scan() {
        Perms.ensureBluetooth(this);
        content.removeAllViews();
        content.addView(Ui.section(this, "SCANNING FOR VCI DEVICES…"));
        status = Ui.tv(this, "Looking for NirixX hardware and compatible adapters…",
                12.5f, 0xFF5A6472, false);
        content.addView(status);
        scanDialog = Ui.progressDialog(this, "Scanning nearby devices…");
        scanDialog.show();
        VciManager.get().scan(this, this);
    }

    public void onFound(final String label, final String detail, final String linkType,
                        final String drawable, final boolean live) {
        int iconRes = getResources().getIdentifier(drawable, "drawable", getPackageName());
        LinearLayout row = Ui.listRow(this, iconRes > 0 ? iconRes : R.drawable.vci,
                label, detail + "  ·  " + linkType + (live ? "  ·  LIVE" : "  ·  SIM"), true);
        row.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { connect(label); }
        });
        content.addView(row);
    }

    public void onFinished(boolean bluetoothActive) {
        if (scanDialog != null) scanDialog.dismiss();
        status.setText(bluetoothActive
                ? "Tap a device to pair. LIVE rows came from this phone's Bluetooth radio."
                : "Bluetooth is off or unavailable — showing the simulation pool. Pairing still works (simulated).");
        LinearLayout cat = Ui.listRow(this, R.drawable.vci, "Browse all supported VCIs",
                "Full hardware catalog: NirixX family + third-party Android adapters", true);
        cat.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new android.content.Intent(AddDeviceActivity.this, VciCatalogActivity.class));
            }
        });
        content.addView(cat);
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
        VciManager.get().connect(this, null, name, new VciTransport.Listener() {
            public void onState(final int state, final String message) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (state != VciTransport.STATE_CONNECTED) return;
                        d.dismiss();
                        Session.vciConnected = true;
                        Session.vciName = name;
                        Ui.resultDialog(AddDeviceActivity.this, R.drawable.ic_flash_success,
                                "VCI Connected",
                                name + " is now paired.\nTransport: "
                                        + VciManager.get().transport().describe()
                                        + "\nFirmware: " + Session.vciFw,
                                "Done", new Runnable() {
                                    public void run() { finish(); }
                                }).show();
                    }
                });
            }
            public void onBytes(byte[] data, int length) { }
            public void onError(String message) { }
        });
    }
}
