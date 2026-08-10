package com.nirixx.app;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nirixx.app.db.Db;
import com.nirixx.app.sim.UdsLog;
import java.util.List;

/** IUPR Test — Primary page (VIN, CVN, CAL ID, IUPR ratio + History) then the
 *  Secondary page (model, kms, city, state, ambient conditions, sold date),
 *  submitted into the iupr_history table. Mirrors the reference flow. */
public class IuprTestActivity extends BaseActivity {

    private LinearLayout content;
    private Db db;
    private boolean secondary = false;

    private EditText edtKms, edtCity, edtState, edtSold;
    private TextView txtModel, boxMap, boxIat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("IUPR Test");
        showEcuChip(Session.selectedEcuCode, true);
        db = Db.get(this);
        Session.ensureSession(this);
        content = (LinearLayout) findViewById(R.id.content);
        renderPrimary();
    }

    // ------------------------------------------------------------- primary
    private void renderPrimary() {
        secondary = false;
        content.removeAllViews();
        content.addView(Ui.crumbs(this, new String[]{"Home", Session.selectedVehicle,
                Session.selectedEcuCode, "IUPR Test"}));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 4));

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(android.view.Gravity.CENTER_VERTICAL);
        head.addView(Ui.tv(this, "IUPR Primary", 15.5f, 0xFF1A2138, true),
                new LinearLayout.LayoutParams(0, -2, 1f));
        TextView hist = new TextView(this);
        hist.setText("\u27F2  History");
        hist.setTextSize(14f);
        hist.setTextColor(0xFFFFFFFF);
        hist.setTypeface(null, android.graphics.Typeface.BOLD);
        hist.setGravity(android.view.Gravity.CENTER);
        hist.setBackgroundResource(R.drawable.bg_bar_light);
        hist.setPadding(Ui.dp(this, 18), Ui.dp(this, 8), Ui.dp(this, 18), Ui.dp(this, 8));
        hist.setBackgroundColor(0xFF63A6E8);
        head.addView(hist);
        card.addView(head);

        View spacer = new View(this);
        card.addView(spacer, new LinearLayout.LayoutParams(1, Ui.dp(this, 8)));

        card.addView(Ui.kvBox(this, "VIN", Session.selectedVin));
        card.addView(Ui.kvBox(this, "CVN", "47F2DBD1"));
        card.addView(Ui.kvBox(this, "CAL ID", "U279EBS6V0A3a903"));

        String ratio = "Primary O2:\nNum : 892\nDen : 41\nRatio : 21.756098\n\n"
                + "Secondary O2: NA\nCat Monitoring: NA\nEVAP: NA";
        card.addView(Ui.kvBox(this, "IUPR Ratio", ratio));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, Ui.dp(this, 8));
        content.addView(card, lp);

        TextView next = Ui.navyBtn(this, "Next");
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(Ui.dp(this, 150), -2);
        np.gravity = android.view.Gravity.RIGHT;
        content.addView(next, np);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                UdsLog.log(IuprTestActivity.this, "TX", Session.ecuTx + " -> 22F8B0");
                UdsLog.log(IuprTestActivity.this, "RX", Session.ecuRx + " -> 62F8B0…");
                renderSecondary();
            }
        });

        hist.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { showHistory(); }
        });
    }

    // ------------------------------------------------------------- secondary
    private void renderSecondary() {
        secondary = true;
        content.removeAllViews();
        content.addView(Ui.crumbs(this, new String[]{"Home", Session.selectedVehicle,
                Session.selectedEcuCode, "IUPR Test"}));
        content.addView(Ui.section(this, "IUPR SECONDARY"));

        content.addView(Ui.tv(this, "1. Model details", 13.5f, 0xFF5A6472, false));
        txtModel = boxLike(Session.selectedVehicle, null);
        content.addView(txtModel);

        content.addView(Ui.tv(this, "2. Kilometers driven", 13.5f, 0xFF5A6472, false));
        edtKms = editBox(InputType.TYPE_CLASS_NUMBER);
        content.addView(edtKms);

        content.addView(Ui.tv(this, "3. City", 13.5f, 0xFF5A6472, false));
        edtCity = editBox(InputType.TYPE_CLASS_TEXT);
        edtCity.setText("Chennai");
        content.addView(edtCity);

        content.addView(Ui.tv(this, "4. State", 13.5f, 0xFF5A6472, false));
        edtState = editBox(InputType.TYPE_CLASS_TEXT);
        edtState.setText("Tamil Nadu");
        content.addView(edtState);

        content.addView(Ui.tv(this, "5. Ambient conditions", 13.5f, 0xFF5A6472, false));
        boxMap = boxLike("91.0 kPa", "MAP");
        content.addView(boxMap);
        boxIat = boxLike("55.0 °C", "Intake Air Temperature");
        content.addView(boxIat);

        content.addView(Ui.tv(this, "6. Vehicle sold date (DD/MM/YYYY)", 13.5f, 0xFF5A6472, false));
        edtSold = editBox(InputType.TYPE_CLASS_DATETIME);
        edtSold.setHint("17/03/2026");
        content.addView(edtSold);

        TextView submit = Ui.navyBtn(this, "Submit IUPR Report");
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.setMargins(0, Ui.dp(this, 10), 0, Ui.dp(this, 12));
        content.addView(submit, sp);
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                db.saveIupr(Session.sessionKey, Session.vehicleId, Session.selectedVin,
                        "47F2DBD1", "U279EBS6V0A3a903", "21.756098",
                        edtKms.getText().toString(), edtCity.getText().toString(),
                        edtState.getText().toString());
                Ui.resultDialog(IuprTestActivity.this, R.drawable.ic_flash_success,
                        "IUPR Submitted",
                        "IUPR report for " + Session.selectedVehicle + " saved to the NirixX database "
                                + "and queued for DMS sync.", "Done", new Runnable() {
                            public void run() { finish(); }
                        }).show();
            }
        });
    }

    private EditText editBox(int type) {
        EditText e = new EditText(this);
        e.setInputType(type);
        e.setBackgroundResource(R.drawable.bg_box_outline);
        e.setPadding(Ui.dp(this, 14), 0, Ui.dp(this, 14), 0);
        e.setTextSize(15f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 50));
        lp.setMargins(0, Ui.dp(this, 4), 0, Ui.dp(this, 12));
        e.setLayoutParams(lp);
        return e;
    }

    /** Outlined display box: value left, right-aligned suffix (MAP / IAT …). */
    private TextView boxLike(String value, String suffix) {
        TextView t = new TextView(this);
        t.setText(suffix == null ? value : value + "     " + suffix);
        t.setTextSize(15f);
        t.setTextColor(0xFF1A2138);
        t.setGravity(android.view.Gravity.CENTER_VERTICAL);
        t.setBackgroundResource(R.drawable.bg_box_outline);
        t.setPadding(Ui.dp(this, 14), 0, Ui.dp(this, 14), 0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 50));
        lp.setMargins(0, Ui.dp(this, 4), 0, Ui.dp(this, 12));
        t.setLayoutParams(lp);
        return t;
    }

    private void showHistory() {
        content.removeAllViews();
        content.addView(Ui.crumbs(this, new String[]{"Home", Session.selectedVehicle,
                Session.selectedEcuCode, "IUPR History"}));
        content.addView(Ui.section(this, "SUBMITTED IUPR REPORTS"));
        List<String[]> rows = db.iuprHistory();
        if (rows.isEmpty()) {
            LinearLayout card = Ui.card(this);
            card.addView(Ui.tv(this, "No IUPR reports submitted yet.", 13.5f, 0xFF5A6472, false));
            content.addView(card);
        }
        for (String[] r : rows) {
            LinearLayout card = Ui.card(this);
            card.addView(Ui.tv(this, r[0] + "   ·   ratio " + r[3], 14.5f, 0xFF1A2138, true));
            card.addView(Ui.tv(this, "CVN " + r[1] + " · CAL ID " + r[2], 12.5f, 0xFF5A6472, false));
            card.addView(Ui.tv(this, r[4] + " km · " + r[5] + ", " + r[6], 12.5f, 0xFF5A6472, false));
            content.addView(card);
        }
        TextView back = Ui.navyBtn(this, "Back to IUPR Primary");
        content.addView(back);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { renderPrimary(); }
        });
    }

    @Override
    public void onBackPressed() {
        if (secondary) { renderPrimary(); return; }
        super.onBackPressed();
    }
}
