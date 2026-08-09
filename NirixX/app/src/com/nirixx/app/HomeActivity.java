package com.nirixx.app;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HomeActivity extends BaseActivity {

    private static final Object[][] TILES = new Object[][]{
        {"VIN Based\nDiagnosis", Integer.valueOf(R.drawable.vindiagnostic), VinDiagnosisActivity.class},
        {"VIN Based\nFlashing", Integer.valueOf(R.drawable.vinflashing), VinFlashingActivity.class},
        {"Diagnostics", Integer.valueOf(R.drawable.manualdiagnostic), SelectECUActivity.class},
        {"ECU\nFlashing", Integer.valueOf(R.drawable.flash_blue), SupplierFlashListActivity.class},
        {"Cluster\nFlashing", Integer.valueOf(R.drawable.racing_bike1), ClusterFlashListActivity.class},
        {"Manual\nDiagnostic", Integer.valueOf(R.drawable.vintroubleshooting), ManualDiagnosticActivity.class},
        {"Health\nReports", Integer.valueOf(R.drawable.vehicle_health_report), ReportsActivity.class},
        {"Battery\nHealth", Integer.valueOf(R.drawable.battery_health_report), BatteryHealthActivity.class},
        {"Data\nRecording", Integer.valueOf(R.drawable.vci), LiveDataRecordingActivity.class},
        {"VCI Firmware\nUpdate", Integer.valueOf(R.drawable.firmwarecloud), VciFirmwareListActivity.class},
        {"Vehicle\nList", Integer.valueOf(R.drawable.motorcycle), VehicleListActivity.class},
        {"Service\nManual", Integer.valueOf(R.drawable.dtclibrary), ServiceManualActivity.class},
        {"Logs &\nFiles", Integer.valueOf(R.drawable.dtc_blue), LogViewerActivity.class},
        {"Support\nChat", Integer.valueOf(R.drawable.notification2), SupportChatActivity.class},
        {"VCI\nCatalog", Integer.valueOf(R.drawable.ecu_module), VciCatalogActivity.class},
        {"System\nCheck", Integer.valueOf(R.drawable.setting_1), SystemCheckActivity.class},
        {"App\nUpdate", Integer.valueOf(R.drawable.flash), UpdateDescriptionActivity.class},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (!getSharedPreferences("nirixx", MODE_PRIVATE).getBoolean("notif_asked", false)) {
            Perms.ensureNotifications(this);
            getSharedPreferences("nirixx", MODE_PRIVATE).edit().putBoolean("notif_asked", true).apply();
        }

        GridLayout grid = (GridLayout) findViewById(R.id.gridTiles);
        LayoutInflater inf = getLayoutInflater();
        for (int i = 0; i < TILES.length; i++) {
            final Object[] tile = TILES[i];
            View cell = inf.inflate(R.layout.tile, grid, false);
            ImageView icon = (ImageView) cell.findViewById(R.id.tileIcon);
            icon.setImageResource(((Integer) tile[1]).intValue());
            icon.setColorFilter(0xFF0B8376);
            ((TextView) cell.findViewById(R.id.tileLabel)).setText((String) tile[0]);
            cell.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { go((Class<?>) tile[2]); }
            });
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = GridLayout.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            cell.setLayoutParams(lp);
            grid.addView(cell);
        }

        findViewById(R.id.btnBell).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { go(NotificationActivity.class); }
        });
        findViewById(R.id.btnConnect).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { go(AddDeviceActivity.class); }
        });
        findViewById(R.id.vciCard).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { go(AddDeviceActivity.class); }
        });

        TextView dealer = (TextView) findViewById(R.id.txtDealer);
        dealer.setText("Dealer: " + Session.dealerName
                + (Session.dealerEmail.length() > 0 ? " · " + Session.dealerEmail : ""));

        // Foreground session coordinator (like the original ClientService)
        startService(new android.content.Intent(this, ClientService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshVci();
    }

    private void refreshVci() {
        ImageView iv = (ImageView) findViewById(R.id.imgVciState);
        TextView state = (TextView) findViewById(R.id.txtVciState);
        TextView btn = (TextView) findViewById(R.id.btnConnect);
        if (Session.vciConnected) {
            iv.setImageResource(R.drawable.bluetooth);
            state.setText(Session.vciName + "  •  Connected");
            btn.setText("Manage");
        } else {
            iv.setImageResource(R.drawable.bluetooth_disconnect);
            state.setText("No VCI Connected");
            btn.setText(getString(R.string.select_vci));
        }
    }

    @Override
    public void onBackPressed() {
        Ui.dialog(this, "Exit NirixX?", "Do you want to close the application?",
                "Exit", new Runnable() { public void run() { finishAffinity(); } },
                "Cancel", null).show();
    }
}
