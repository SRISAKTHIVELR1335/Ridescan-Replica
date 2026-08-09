package com.ridescan.replica;

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
        {"Troubleshooting", Integer.valueOf(R.drawable.manualdiagnostic), VinDiagnosisActivity.class},
        {"ECU\nFlashing", Integer.valueOf(R.drawable.flash_blue), SelectFlashVariantActivity.class},
        {"Health\nReports", Integer.valueOf(R.drawable.vehicle_health_report), ReportsActivity.class},
        {"Battery\nHealth", Integer.valueOf(R.drawable.battery_health_report), BatteryHealthActivity.class},
        {"VCI Firmware\nUpdate", Integer.valueOf(R.drawable.firmwarecloud), FirmwareUpdateActivity.class},
        {"Vehicle\nList", Integer.valueOf(R.drawable.motorcycle), VehicleListActivity.class},
        {"Service\nManual", Integer.valueOf(R.drawable.dtclibrary), ServiceManualActivity.class},
        {"Notifications", Integer.valueOf(R.drawable.notification2), NotificationActivity.class},
        {"Account", Integer.valueOf(R.drawable.setting_1), AccountActivity.class},
        {"Health\nScanner", Integer.valueOf(R.drawable.vintroubleshooting), SelectECUActivity.class},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        GridLayout grid = (GridLayout) findViewById(R.id.gridTiles);
        LayoutInflater inf = getLayoutInflater();
        for (int i = 0; i < TILES.length; i++) {
            final Object[] tile = TILES[i];
            View cell = inf.inflate(R.layout.tile, grid, false);
            ImageView icon = (ImageView) cell.findViewById(R.id.tileIcon);
            icon.setImageResource(((Integer) tile[1]).intValue());
            icon.setColorFilter(0xFF00347E);
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
        Ui.dialog(this, "Exit RIDE Scan?", "Do you want to close the application?",
                "Exit", new Runnable() { public void run() { finishAffinity(); } },
                "Cancel", null).show();
    }
}
