package com.nirixx.app;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nirixx.app.db.Db;
import com.nirixx.app.sim.UdsLog;
import java.util.List;

/** Diagnostic Trouble Codes (reference): Read / Clear actions over UDS 19 02 /
 *  14 FF FF FF, DTC cards with code + description + status, powering the
 *  "Faults Codes Found / Not Found" tile of the Diagnostic Section page. */
public class ReadDTCsActivity extends BaseActivity {

    private LinearLayout content;
    private Db db;
    private boolean hasDtcs;
    private final Handler h = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Read DTCs");
        showEcuChip(Session.selectedEcuCode, true);
        db = Db.get(this);
        Session.ensureSession(this);
        content = (LinearLayout) findViewById(R.id.content);
        hasDtcs = Session.faultsFound;
        render();
    }

    private void render() {
        content.removeAllViews();
        content.addView(Ui.crumbs(this, new String[]{"Home", Session.selectedVehicle,
                Session.selectedEcuCode, "Diagnostic Trouble Codes"}));

        LinearLayout act = new LinearLayout(this);
        act.setOrientation(LinearLayout.HORIZONTAL);
        TextView read = Ui.navyBtn(this, "READ DTC");
        TextView clear = Ui.navyBtn(this, "CLEAR DTC");
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(0, -2, 1f);
        hp.setMargins(0, 0, Ui.dp(this, 6), 0);
        LinearLayout.LayoutParams hp2 = new LinearLayout.LayoutParams(0, -2, 1f);
        hp2.setMargins(Ui.dp(this, 6), 0, 0, 0);
        act.addView(read, hp);
        act.addView(clear, hp2);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, -2);
        ap.setMargins(0, 0, 0, Ui.dp(this, 10));
        content.addView(act, ap);

        if (!hasDtcs) {
            LinearLayout card = Ui.card(this);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.addView(Ui.dot(this, true));
            TextView t = Ui.tv(this, "  No Diagnostic Trouble Codes", 15f, 0xFF2E9E43, true);
            row.addView(t);
            card.addView(row);
            card.addView(Ui.tv(this,
                    "19 02 08 returned 59 02 FF 00 — no stored, pending or confirmed DTCs on "
                            + Session.selectedEcuCode + ".", 12.5f, 0xFF5A6472, false));
            content.addView(card);
        } else {
            List<Db.Dtc> list = db.dtcs(Session.ecuId);
            String[] status = {"Confirmed", "Pending", "Stored"};
            for (int i = 0; i < list.size() && i < 4; i++) {
                Db.Dtc d = list.get(i);
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.HORIZONTAL);
                card.setBackgroundResource(R.drawable.bg_card);
                card.setGravity(android.view.Gravity.CENTER_VERTICAL);
                card.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12));
                android.widget.ImageView ic = new android.widget.ImageView(this);
                ic.setImageResource(R.drawable.ic_dtc_tri);
                card.addView(ic, new LinearLayout.LayoutParams(Ui.dp(this, 26), Ui.dp(this, 26)));
                LinearLayout mid = new LinearLayout(this);
                mid.setOrientation(LinearLayout.VERTICAL);
                mid.setPadding(Ui.dp(this, 12), 0, 0, 0);
                mid.addView(Ui.tv(this, d.code, 15f, 0xFF1A2138, true));
                mid.addView(Ui.tv(this, d.descr, 12.5f, 0xFF5A6472, false));
                card.addView(mid, new LinearLayout.LayoutParams(0, -2, 1f));
                card.addView(Ui.chip(this, status[i % status.length],
                        R.drawable.bg_chip_red, 0xFFD32F2F));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
                lp.setMargins(0, 0, 0, Ui.dp(this, 8));
                content.addView(card, lp);
            }
        }

        read.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { doRead(); }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { doClear(); }
        });
    }

    private void doRead() {
        final Dialog d = Ui.progressDialog(this, "Reading DTCs (19 02 FF)…");
        d.show();
        UdsLog.log(this, "TX", Session.ecuTx + " -> 1902FF");
        h.postDelayed(new Runnable() {
            public void run() {
                d.dismiss();
                hasDtcs = Math.random() < 0.7;
                Session.faultsFound = hasDtcs;
                UdsLog.log(ReadDTCsActivity.this, "RX",
                        Session.ecuRx + " -> " + (hasDtcs ? "5902FF8CD3013501" : "5902FF00"));
                toast(hasDtcs ? "DTCs detected on " + Session.selectedEcuCode : "No DTCs present");
                render();
            }
        }, 1200);
    }

    private void doClear() {
        final Dialog d = Ui.progressDialog(this, "Clearing DTCs (14 FF FF FF)…");
        d.show();
        UdsLog.log(this, "TX", Session.ecuTx + " -> 14FFFFFF");
        h.postDelayed(new Runnable() {
            public void run() {
                d.dismiss();
                hasDtcs = false;
                Session.faultsFound = false;
                Session.dtcsCleared++;
                UdsLog.log(ReadDTCsActivity.this, "RX", Session.ecuRx + " -> 54");
                Ui.resultDialog(ReadDTCsActivity.this, R.drawable.ic_flash_success,
                        "DTCs Cleared", "All trouble codes erased from " + Session.selectedEcuCode + ".",
                        "OK", new Runnable() { public void run() { render(); } }).show();
            }
        }, 1100);
    }
}
