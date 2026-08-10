package com.nirixx.app;

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nirixx.app.db.Db;
import com.nirixx.app.vci.VciManager;
import com.nirixx.app.vci.VciTransport;

/** Add / Pair VCI — the reference connectivity experience. Three transport
 *  tiles (BT / WIFI / USB); each shows its own panel. BT runs the real
 *  Bluetooth discovery (merged with the simulation pool), WIFI lists VCI
 *  access points, USB detects wired adapters. */
public class AddDeviceActivity extends BaseActivity implements VciManager.ScanCallback {

    private LinearLayout panel, content;
    private TextView status;
    private Dialog scanDialog;
    private View tileBT, tileWIFI, tileUSB;
    private String mode = "BLUETOOTH";
    private final Handler h = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Add / Pair VCI");
        content = (LinearLayout) findViewById(R.id.content);

        content.addView(Ui.section(this, "SELECT CONNECTIVITY TYPE"));
        LinearLayout tiles = new LinearLayout(this);
        tiles.setOrientation(LinearLayout.HORIZONTAL);
        tileBT = connTile(tiles, R.drawable.bluetooth, "BT");
        tileWIFI = connTile(tiles, R.drawable.ic_wifi, "WIFI");
        tileUSB = connTile(tiles, R.drawable.ic_usb, "USB");
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, -2);
        tp.setMargins(0, 0, 0, Ui.dp(this, 12));
        content.addView(tiles, tp);

        panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        content.addView(panel);

        mode = Db.get(this).config("connectivity", "BLUETOOTH");
        showMode();
    }

    private View connTile(LinearLayout parent, int iconRes, final String name) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(android.view.Gravity.CENTER);
        android.widget.ImageView iv = new android.widget.ImageView(this);
        iv.setImageResource(iconRes);
        tile.addView(iv, new LinearLayout.LayoutParams(Ui.dp(this, 24), Ui.dp(this, 24)));
        tile.addView(Ui.tv(this, name, 12.5f, 0xFF3A4663, true));
        tile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mode = name;
                Db.get(AddDeviceActivity.this).setConfig("connectivity", name);
                Session.connectivity = name;
                showMode();
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 74), 1f);
        lp.setMargins(0, 0, Ui.dp(this, 8), 0);
        parent.addView(tile, lp);
        return tile;
    }

    private void showMode() {
        tileBT.setBackgroundResource("BLUETOOTH".equals(mode) ? R.drawable.bg_tile_sel : R.drawable.bg_tile_def);
        tileWIFI.setBackgroundResource("WIFI".equals(mode) ? R.drawable.bg_tile_sel : R.drawable.bg_tile_def);
        tileUSB.setBackgroundResource("USB".equals(mode) ? R.drawable.bg_tile_sel : R.drawable.bg_tile_def);
        Session.connectivity = mode;
        if ("WIFI".equals(mode)) showWifi();
        else if ("USB".equals(mode)) showUsb();
        else startPairingFlow();
    }

    // ------------------------------------------------------------------ BT
    private void startPairingFlow() {
        panel.removeAllViews();
        content.addView(Ui.section(this, "SCANNING FOR VCI DEVICES…"));
        if (Perms.bluetoothGranted(this)) { scan(); return; }
        scan();
        Ui.dialog(this, "Allow nearby-device access",
                "NirixX finds and pairs with your VCI over Bluetooth.\n\n"
                        + "On Android 12 and above this needs the Nearby devices permission. "
                        + "It is used only to discover diagnostic hardware.\n\n"
                        + "You can also skip: the full app works with the built-in simulation.",
                "Allow & Scan", new Runnable() {
                    public void run() { Perms.ensureBluetooth(AddDeviceActivity.this); }
                },
                "Continue in demo mode", null).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != Perms.REQ_BT && requestCode != Perms.REQ_BLE_LEGACY) return;
        boolean allOk = grantResults.length > 0;
        for (int i = 0; i < grantResults.length; i++)
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) { allOk = false; break; }
        toast(allOk ? "Bluetooth access granted — rescanning live devices"
                : "Using simulation (Bluetooth access not granted)");
        panel.removeAllViews();
        scan();
    }

    private void scan() {
        panel.removeAllViews();
        status = Ui.tv(this, "Looking for NirixX hardware and compatible adapters…", 12.5f, 0xFF5A6472, false);
        panel.addView(status);
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
            public void onClick(View v) { connect(label, "BLUETOOTH"); }
        });
        panel.addView(row);
    }

    public void onFinished(boolean bluetoothActive) {
        if (scanDialog != null) scanDialog.dismiss();
        if (status != null) status.setText(bluetoothActive
                ? "Tap a device to pair. LIVE rows came from this phone's Bluetooth radio."
                : "Bluetooth is off or not granted — showing the simulation pool. Pairing still works (simulated).");
    }

    // ------------------------------------------------------------------ WIFI
    private void showWifi() {
        VciManager.get().stopScan(this);
        panel.removeAllViews();
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.addView(Ui.section(this, "AVAILABLE NETWORKS"), new LinearLayout.LayoutParams(0, -2, 1f));
        TextView refresh = Ui.tv(this, "Refresh", 12.5f, 0xFF3A4663, true);
        refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { showWifi(); toast("Scanning Wi-Fi…"); }
        });
        head.addView(refresh);
        panel.addView(head);

        final LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        panel.addView(list);
        h.postDelayed(new Runnable() {
            public void run() {
                final String[][] nets = {
                        {"NirixiLINK_504856", "VCI hotspot · secured", "-48 dBm"},
                        {"Workshop_DMS", "Dealer network", "-61 dBm"},
                        {"Service_Bay_2", "Hidden VCI network", "-70 dBm"}};
                for (int i = 0; i < nets.length; i++) {
                    final String[] n = nets[i];
                    LinearLayout row = Ui.listRow(AddDeviceActivity.this, R.drawable.ic_wifi,
                            n[0], n[1] + "  ·  " + n[2], false);
                    row.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) { connect(n[0], "WIFI"); }
                    });
                    list.addView(row);
                }
            }
        }, 900);
    }

    // ------------------------------------------------------------------ USB
    private void showUsb() {
        VciManager.get().stopScan(this);
        panel.removeAllViews();
        panel.addView(Ui.section(this, "WIRED VCI (USB OTG)"));
        LinearLayout card = Ui.card(this);
        card.addView(Ui.tv(this, "Looking for a wired VCI on the USB host port…", 13f, 0xFF5A6472, false));
        panel.addView(card);
        h.postDelayed(new Runnable() {
            public void run() {
                panel.removeAllViews();
                panel.addView(Ui.section(AddDeviceActivity.this, "WIRED VCI (USB OTG)"));
                LinearLayout row = Ui.listRow(AddDeviceActivity.this, R.drawable.ic_usb,
                        "NirixX USB-Link", "FTDI 11.3V bus · enumerated on /dev/bus/usb/001/003 · SIM", true);
                row.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) { connect("NirixX USB-Link", "USB"); }
                });
                panel.addView(row);
                panel.addView(Ui.tv(AddDeviceActivity.this,
                        "USB mode is used when the tablet is docked on the bench cable.", 12f, 0xFF5A6472, false));
            }
        }, 1200);
    }

    // ------------------------------------------------------------------ shared
    private void connect(final String name, final String link) {
        final Dialog d = Ui.progressDialog(this, "Pairing with " + name + " over " + link + " …");
        d.show();
        VciManager.get().connect(this, null, name, new VciTransport.Listener() {
            public void onState(final int state, final String message) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (state != VciTransport.STATE_CONNECTED) return;
                        d.dismiss();
                        Session.vciConnected = true;
                        Session.vciName = name;
                        Session.connectivity = link;
                        Db.get(AddDeviceActivity.this).setConfig("last_vci", name);
                        Db.get(AddDeviceActivity.this).setConfig("connectivity", link);
                        Ui.resultDialog(AddDeviceActivity.this, R.drawable.ic_flash_success,
                                "VCI Connected",
                                name + " is paired over " + link + ". Firmware " + Session.vciFw + ".",
                                "Continue", new Runnable() {
                                    public void run() { finish(); }
                                }).show();
                    }
                });
            }
            public void onBytes(byte[] data, int length) { }
            public void onError(String message) { }
        });
    }

    @Override
    protected void onDestroy() {
        VciManager.get().stopScan(this);
        super.onDestroy();
    }
}
