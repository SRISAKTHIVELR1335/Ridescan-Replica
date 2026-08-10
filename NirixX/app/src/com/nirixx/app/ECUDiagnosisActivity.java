package com.nirixx.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nirixx.app.sim.SimEcu;

/** ECU Diagnosis page (reference): vehicle image + live battery + updates on
 *  top, "VIN - xxx | EMS-OBDII" section bar, then the 2-column function grid:
 *  Live parameters, DTCs, Write data identifier (password encoded), I/O
 *  control, ECU flashing, Routine control, IUPR test. */
public class ECUDiagnosisActivity extends BaseActivity {

    private TextView batt;
    private final Handler h = new Handler();
    private boolean tick = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("ECU Diagnosis");
        showEcuChip(Session.selectedEcuCode, true);
        build();
        streamVolts();
    }

    private void build() {
        LinearLayout content = (LinearLayout) findViewById(R.id.content);
        content.removeAllViews();

        content.addView(Ui.crumbs(this, new String[]{"Home", Session.selectedVehicle,
                Session.selectedEcuCode, "ECU Diagnosis"}));

        // ---- top strip: bike + live tiles ----------------------------------
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        ImageView iv = new ImageView(this);
        iv.setImageResource(Ui.imgRes(this, Session.vehicleImage));
        top.addView(iv, new LinearLayout.LayoutParams(0, Ui.dp(this, 110), 1.1f));
        LinearLayout tiles = new LinearLayout(this);
        tiles.setOrientation(LinearLayout.VERTICAL);
        LinearLayout b = Ui.statTile(this, R.drawable.ic_battery_sm, "Battery Voltage",
                String.format("%.6f V", Session.batteryVolts));
        batt = (TextView) b.getTag();
        LinearLayout.LayoutParams m = new LinearLayout.LayoutParams(-1, -2);
        m.setMargins(0, 0, 0, Ui.dp(this, 8));
        tiles.addView(b, new LinearLayout.LayoutParams(m));
        tiles.addView(Ui.statTile(this, R.drawable.ic_refresh, "New Updates", "Available"),
                new LinearLayout.LayoutParams(m));
        top.addView(tiles, new LinearLayout.LayoutParams(0, -2, 1f));
        content.addView(top);

        content.addView(Ui.sectionBar(this, "VIN - " + Session.selectedVin, Session.selectedEcuCode));

        // ---- function grid --------------------------------------------------
        Object[][] funcs = new Object[][]{
                {"Live parameters", Integer.valueOf(R.drawable.ic_waves), LiveParameterActivity.class},
                {"Diagnostic trouble codes", Integer.valueOf(R.drawable.ic_dtc_tri), ReadDTCsActivity.class},
                {"Write data identifier", Integer.valueOf(R.drawable.ic_pen), WriteDataActivity.class},
                {"Input output control", Integer.valueOf(R.drawable.ic_io), IOControlActivity.class},
                {"Ecu flashing", Integer.valueOf(R.drawable.ic_chip), FlashActivity.class},
                {"Routine control", Integer.valueOf(R.drawable.ic_refresh), RoutineControlActivity.class},
                {"Iupr test", Integer.valueOf(R.drawable.ic_monitor), IuprTestActivity.class},
        };

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        for (int i = 0; i < funcs.length; i++) {
            final Object[] f = funcs[i];
            LinearLayout tile = Ui.ecuTile(this, ((Integer) f[1]).intValue(), (String) f[0]);
            tile.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { go((Class<?>) f[2]); }
            });
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = GridLayout.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            lp.setMargins(Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4));
            grid.addView(tile, lp);
        }
        content.addView(grid);

        TextView vhr = Ui.navyBtn(this, "VEHICLE HEALTH REPORT");
        LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(-1, -2);
        vp.setMargins(Ui.dp(this, 4), Ui.dp(this, 10), Ui.dp(this, 4), 0);
        content.addView(vhr, vp);
        vhr.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { go(VhrActivity.class); }
        });
    }

    private void streamVolts() {
        h.postDelayed(new Runnable() {
            public void run() {
                if (!tick) return;
                if (batt != null) batt.setText(String.format("%.6f V", SimEcu.nextVolts()));
                h.postDelayed(this, 1000);
            }
        }, 1000);
    }

    @Override
    protected void onPause() { tick = false; super.onPause(); }
}
