package com.nirixx.app;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nirixx.app.db.Db;
import com.nirixx.app.sim.SimEcu;
import com.nirixx.app.sim.UdsLog;
import java.util.List;

/** Write Data Identifier — the reference write flow: every DID row opens an
 *  editor; rows marked writable additionally demand the encoded service
 *  password (SecurityAccess 27 01 / 27 02) before 2E is accepted. */
public class WriteDataActivity extends BaseActivity {

    private LinearLayout content;
    private Db db;
    private final Handler h = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Write Data Identifier");
        showEcuChip(Session.selectedEcuCode, true);
        db = Db.get(this);
        Session.ensureSession(this);
        content = (LinearLayout) findViewById(R.id.content);
        render();
    }

    private void render() {
        content.removeAllViews();
        content.addView(Ui.crumbs(this, new String[]{"Home", Session.selectedVehicle,
                Session.selectedEcuCode, "Write Data Identifier"}));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 4));

        List<Db.TestDef> dids = db.tests(Session.ecuId, "write");
        for (final Db.TestDef t : dids) {
            final String current = t.name.startsWith("VIN") ? Session.selectedVin
                    : t.name.equals("CVN") ? "47F2DBD1"
                    : t.name.equals("CAL ID") ? "U279EBS6V0A3a903" : "19072019";
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 8));
            LinearLayout mid = new LinearLayout(this);
            mid.setOrientation(LinearLayout.VERTICAL);
            mid.addView(Ui.tv(this, t.name, 14.5f, 0xFF1A2138, true));
            mid.addView(Ui.tv(this, current, 12.5f, 0xFF5A6472, false));
            row.addView(mid, new LinearLayout.LayoutParams(0, -2, 1f));
            row.addView(Ui.chip(this, t.writable == 1 ? "WRITE · PWD" : "READ ONLY",
                    t.writable == 1 ? R.drawable.bg_chip_amber : R.drawable.bg_chip_grey,
                    t.writable == 1 ? 0xFF8A5300 : 0xFF5A6472));
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, -2);
            card.addView(row, rp);
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { openEditor(t, current); }
            });
        }
        content.addView(card);

        LinearLayout note = Ui.card(this);
        note.addView(Ui.tv(this, "Encoded password flow", 13.5f, 0xFF1A2138, true));
        note.addView(Ui.tv(this,
                "Writable identifiers are protected by SecurityAccess: the tool requests the seed "
                        + "(27 01), keys it with the dealer password, and unlocks the ECU (27 02) before "
                        + "writing (2E). Everything is recorded in the session log.", 12.5f, 0xFF5A6472, false));
        content.addView(note);
    }

    private void openEditor(final Db.TestDef t, final String current) {
        final Dialog d = new Dialog(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_dialog);
        int p = Ui.dp(this, 20);
        root.setPadding(p, p, p, Ui.dp(this, 14));
        root.addView(Ui.tv(this, "Write " + t.name, 16.5f, 0xFF1A2138, true));

        final EditText input = new EditText(this);
        input.setText(current);
        input.setBackgroundResource(R.drawable.bg_box_outline);
        input.setPadding(Ui.dp(this, 12), 0, Ui.dp(this, 12), 0);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(-1, Ui.dp(this, 46));
        ip.setMargins(0, Ui.dp(this, 12), 0, 0);
        root.addView(input, ip);

        final EditText pwd = new EditText(this);
        if (t.writable == 1) {
            pwd.setHint("Encoded service password");
            pwd.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            pwd.setBackgroundResource(R.drawable.bg_box_outline);
            pwd.setPadding(Ui.dp(this, 12), 0, Ui.dp(this, 12), 0);
            LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 46));
            pp.setMargins(0, Ui.dp(this, 10), 0, 0);
            root.addView(pwd, pp);
        }

        TextView go = Ui.navyBtn(this, "WRITE");
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 46));
        gp.setMargins(0, Ui.dp(this, 14), 0, 0);
        root.addView(go, gp);
        d.setContentView(root);
        d.show();

        go.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String value = input.getText().toString().trim();
                if (t.writable == 1 && pwd.getText().toString().trim().length() < 4) {
                    toast("Encoded password required");
                    return;
                }
                d.dismiss();
                writeDid(t, value);
            }
        });
    }

    private void writeDid(final Db.TestDef t, final String value) {
        final Dialog prog = Ui.progressDialog(this, "Unlocking ECU…");
        prog.show();
        UdsLog.log(this, "TX", Session.ecuTx + " -> 2701");
        h.postDelayed(new Runnable() {
            public void run() {
                UdsLog.log(WriteDataActivity.this, "RX", Session.ecuRx + " -> 6701AACC33");
                UdsLog.log(WriteDataActivity.this, "TX", Session.ecuTx + " -> 2702" + SimEcu.hex(String.valueOf(value.length() * 7919 % 65536)));
                UdsLog.log(WriteDataActivity.this, "RX", Session.ecuRx + " -> 6702");
                prog.setTitle("Writing " + t.name + "…");
                h.postDelayed(new Runnable() {
                    public void run() {
                        UdsLog.log(WriteDataActivity.this, "TX",
                                Session.ecuTx + " -> 2EF190" + SimEcu.hex(value));
                        UdsLog.log(WriteDataActivity.this, "RX", Session.ecuRx + " -> 6EF190");
                        prog.dismiss();
                        if (t.name.startsWith("VIN")) Session.selectedVin = value;
                        Ui.resultDialog(WriteDataActivity.this, R.drawable.ic_flash_success,
                                "Write Complete", t.name + " updated on " + Session.selectedEcuCode
                                        + ":\n" + value, "OK", new Runnable() {
                                    public void run() { render(); }
                                }).show();
                    }
                }, 900);
            }
        }, 800);
    }
}
