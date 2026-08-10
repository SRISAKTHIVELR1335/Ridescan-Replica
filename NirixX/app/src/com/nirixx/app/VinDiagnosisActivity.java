package com.nirixx.app;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nirixx.app.db.Db;
import com.nirixx.app.sim.SimEcu;
import com.nirixx.app.sim.UdsLog;

/** VIN Based Diagnosis — auto VIN read over UDS (22 F1 90) with a live console
 *  trace, then the vehicle is resolved from the database:
 *   • found   -> vehicle card + [Diagnostic Section] / [Vehicle Health Report]
 *   • missing -> explained state + [Vehicle Selection] grid. */
public class VinDiagnosisActivity extends BaseActivity {

    /** VinFlashingActivity overrides this to route into the flash-variant flow. */
    protected boolean forFlashing() { return false; }

    private LinearLayout content;
    private final Handler h = new Handler();
    private Db db;
    private Db.Vehicle matched;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle(forFlashing() ? "VIN Based Flashing" : "VIN Based Diagnosis");
        db = Db.get(this);
        content = (LinearLayout) findViewById(R.id.content);
        landing();
    }

    private void landing() {
        content.removeAllViews();

        // vehicle hero + read trigger
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        hero.setBackgroundResource(R.drawable.bg_card);
        hero.setPadding(0, Ui.dp(this, 16), 0, Ui.dp(this, 16));
        ImageView iv = new ImageView(this);
        iv.setImageResource(Ui.imgRes(this, Session.vehicleImage));
        hero.addView(iv, new LinearLayout.LayoutParams(-1, Ui.dp(this, 170)));
        hero.addView(Ui.tv(this, Session.selectedVehicle, 16f, 0xFF1A2138, true));
        content.addView(hero);

        if (!Session.vciConnected) {
            LinearLayout warn = Ui.card(this);
            warn.addView(Ui.tv(this, "No VCI connected", 14f, 0xFF1A2138, true));
            warn.addView(Ui.tv(this,
                    "Pair a VCI over BT / Wi-Fi / USB first — the read below runs on the simulated link meanwhile.",
                    12.5f, 0xFF5A6472, false));
            content.addView(warn);
        }

        TextView read = Ui.navyBtn(this, "READ VIN");
        read.setText("READ VIN");
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, -2);
        rp.setMargins(0, Ui.dp(this, 12), 0, 0);
        content.addView(read, rp);
        read.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { runVinRead(); }
        });

        LinearLayout hint = Ui.card(this);
        hint.addView(Ui.tv(this, "Auto VIN detection", 13.5f, 0xFF1A2138, true));
        hint.addView(Ui.tv(this,
                "The tool opens an extended diagnostic session (10 01) and reads DID F190. "
                        + "The 17-character VIN is then resolved against the NirixX vehicle database.",
                12.5f, 0xFF5A6472, false));
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2);
        hp.setMargins(0, Ui.dp(this, 12), 0, 0);
        content.addView(hint, hp);
    }

    private void runVinRead() {
        content.removeAllViews();
        Session.ensureSession(this);

        content.addView(Ui.crumbs(this, new String[]{"Home", "VIN Based Diagnosis"}));
        content.addView(Ui.section(this, "READING VIN FROM ECU"));

        final LinearLayout console = new LinearLayout(this);
        console.setOrientation(LinearLayout.VERTICAL);
        console.setBackgroundResource(R.drawable.bg_console);
        int p = Ui.dp(this, 12);
        console.setPadding(p, p, p, p);
        content.addView(console);

        final Dialog prog = Ui.progressDialog(this, "Opening UDS session…");
        prog.show();
        h.postDelayed(new Runnable() { public void run() { prog.dismiss(); } }, 900);

        final String useVin = pickVin();
        SimEcu.readVin(useVin, new SimEcu.Listener() {
            public void onLine(final String dir, final String payload) {
                TextView line = Ui.tv(VinDiagnosisActivity.this,
                        SimEcu.stamp() + "  " + dir + ": --> " + payload,
                        11.5f, "TX".equals(dir) ? 0xFF9AD0FF : 0xFF9BEFC4, false);
                console.addView(line);
                UdsLog.log(VinDiagnosisActivity.this, dir, payload);
            }
        }, new SimEcu.VinListener() {
            public void onVin(String vin) { showResult(vin); }
        });
    }

    /** Simulated bus answer: first read resolves a database VIN, every second
     *  read returns an unknown VIN so the manual vehicle-grid branch is visible
     *  too (mirrors the reference behaviour on unrecognised vehicles). */
    private int reads = 0;
    private String pickVin() {
        reads++;
        if (reads % 2 == 0) return "MD638AN2100000000";     // not in the DB
        if (Session.selectedVin != null && Session.selectedVin.length() == 17) return Session.selectedVin;
        return "MD637AN11R2D01275";
    }

    private void showResult(final String vin) {
        matched = db.vehicleByVin(vin);

        content.addView(Ui.section(this, "AUTO VIN READ"));
        LinearLayout box = Ui.kvBox(this, "VIN", vin);
        content.addView(box);

        if (matched == null) {
            LinearLayout miss = Ui.card(this);
            miss.addView(Ui.tv(this, "Vehicle not recognised", 15f, 0xFF1A2138, true));
            miss.addView(Ui.tv(this,
                    "VIN " + vin + " does not match any model in the NirixX database. "
                            + "Pick the model manually from the vehicle home screen.",
                    12.5f, 0xFF5A6472, false));
            content.addView(miss);
            TextView vehicles = Ui.navyBtn(this, "OPEN VEHICLE SELECTION");
            LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(-1, -2);
            vp.setMargins(0, Ui.dp(this, 10), 0, 0);
            content.addView(vehicles, vp);
            vehicles.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { go(VehicleListActivity.class); finish(); }
            });
            return;
        }

        Session.selectVehicle(matched.id, matched.model, matched.variant, matched.type,
                vin, matched.image);
        db.saveSession(Session.sessionKey, matched.id, vin, System.currentTimeMillis(),
                Session.connectivity, Session.vciFw, Session.vciName);

        // resolved vehicle card — image straight from the DB row
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setGravity(android.view.Gravity.CENTER_VERTICAL);
        card.setPadding(Ui.dp(this, 12), Ui.dp(this, 10), Ui.dp(this, 12), Ui.dp(this, 10));
        ImageView iv = new ImageView(this);
        iv.setImageResource(Ui.imgRes(this, matched.image));
        card.addView(iv, new LinearLayout.LayoutParams(0, Ui.dp(this, 96), 1.1f));
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.addView(Ui.tv(this, matched.model, 16f, 0xFF1A2138, true));
        info.addView(Ui.tv(this, matched.type + " · " + matched.obd + " · " + matched.protocol,
                12.5f, 0xFF14276F, true));
        info.addView(Ui.tv(this, matched.description, 12f, 0xFF5A6472, false));
        card.addView(info, new LinearLayout.LayoutParams(0, -2, 1.4f));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        content.addView(card, cp);

        TextView diag = Ui.navyBtn(this, forFlashing() ? "OPEN FLASH SELECTION" : "OPEN DIAGNOSTIC SECTION");
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(-1, -2);
        dp.setMargins(0, Ui.dp(this, 8), 0, 0);
        content.addView(diag, dp);
        diag.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { go(forFlashing() ? SelectFlashVariantActivity.class : SelectECUActivity.class); finish(); }
        });

        TextView vhr = Ui.navyBtn(this, "VEHICLE HEALTH REPORT");
        vhr.setBackgroundResource(R.drawable.bg_button_outline);
        vhr.setTextColor(0xFF14276F);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2);
        hp.setMargins(0, Ui.dp(this, 8), 0, Ui.dp(this, 12));
        content.addView(vhr, hp);
        vhr.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { go(VhrActivity.class); finish(); }
        });
    }
}
