package com.nirixx.app;

import android.os.Bundle;
import android.os.Handler;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import com.nirixx.app.db.Db;
import com.nirixx.app.sim.UdsLog;
import java.util.List;

/** Input Output Control (reference): toggle rows inside a card — each toggle
 *  actuates the output over UDS IOControl (2F) and the Ok/result is collected
 *  for the Vehicle Health Report and stored as a test input. */
public class IOControlActivity extends BaseActivity {

    private final Handler h = new Handler();
    private Db db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("IO Control");
        showEcuChip(Session.selectedEcuCode, true);
        db = Db.get(this);
        Session.ensureSession(this);

        LinearLayout content = (LinearLayout) findViewById(R.id.content);
        content.removeAllViews();
        content.addView(Ui.crumbs(this, new String[]{"Home", Session.selectedVehicle,
                Session.selectedEcuCode, "IO Control"}));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setPadding(Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 2));

        List<Db.TestDef> ios = db.tests(Session.ecuId, "io");
        for (final Db.TestDef t : ios) {
            LinearLayout row = Ui.ioRow(this, t.name.toUpperCase(),
                    new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                            UdsLog.log(IOControlActivity.this, "TX",
                                    Session.ecuTx + " -> 2F10" + String.format("%02X", t.sort) + (isChecked ? "03" : "00"));
                            UdsLog.log(IOControlActivity.this, "RX", Session.ecuRx + " -> 6F1003");
                            String verdict = "Ok";
                            Session.vhrData.put("io|" + Session.selectedEcuCode + "|" + t.name, verdict);
                            db.putInput(Session.sessionKey, "io",
                                    Session.selectedEcuCode + "|" + t.name, verdict);
                            toast(t.name + (isChecked ? " actuated — Ok" : " released"));
                        }
                    });
            card.addView(row);
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, Ui.dp(this, 8));
        content.addView(card, lp);

        LinearLayout note = Ui.card(this);
        note.addView(Ui.tv(this,
                "Actuations run for a bounded time and are echoed to the session log. "
                        + "Results feed the IO CONTROL tab of the Vehicle Health Report.",
                12.5f, 0xFF5A6472, false));
        content.addView(note);
    }
}
