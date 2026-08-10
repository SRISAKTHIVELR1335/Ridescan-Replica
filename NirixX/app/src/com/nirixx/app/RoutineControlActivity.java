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

/** Routine Control — reference style list of executable routines with
 *  Start/Result handling (UDS 31 01 … 71 01). */
public class RoutineControlActivity extends BaseActivity {

    private final Handler h = new Handler();
    private Db db;
    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Routine Control");
        showEcuChip(Session.selectedEcuCode, true);
        db = Db.get(this);
        Session.ensureSession(this);
        content = (LinearLayout) findViewById(R.id.content);
        render();
    }

    private void render() {
        content.removeAllViews();
        content.addView(Ui.crumbs(this, new String[]{"Home", Session.selectedVehicle,
                Session.selectedEcuCode, "Routine Control"}));

        List<Db.TestDef> routines = db.tests(Session.ecuId, "routine");
        for (final Db.TestDef t : routines) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(android.view.Gravity.CENTER_VERTICAL);
            card.setBackgroundResource(R.drawable.bg_card);
            card.setPadding(Ui.dp(this, 14), Ui.dp(this, 10), Ui.dp(this, 10), Ui.dp(this, 10));
            LinearLayout mid = new LinearLayout(this);
            mid.setOrientation(LinearLayout.VERTICAL);
            mid.addView(Ui.tv(this, t.name, 14.5f, 0xFF1A2138, true));
            final TextView st = Ui.tv(this, "Ready", 12.5f, 0xFF5A6472, false);
            mid.addView(st);
            card.addView(mid, new LinearLayout.LayoutParams(0, -2, 1f));
            TextView run = Ui.navyBtn(this, "RUN");
            card.addView(run, new LinearLayout.LayoutParams(Ui.dp(this, 92), Ui.dp(this, 40)));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.setMargins(0, 0, 0, Ui.dp(this, 10));
            content.addView(card, lp);

            run.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    final Dialog d = Ui.progressDialog(RoutineControlActivity.this, "Running 31 01 …");
                    d.show();
                    st.setText("Running…");
                    UdsLog.log(RoutineControlActivity.this, "TX",
                            Session.ecuTx + " -> 3101F0" + String.format("%02X", t.sort));
                    h.postDelayed(new Runnable() {
                        public void run() {
                            d.dismiss();
                            st.setText("Completed — Ok");
                            st.setTextColor(0xFF2E9E43);
                            UdsLog.log(RoutineControlActivity.this, "RX", Session.ecuRx + " -> 7101F000");
                            Session.vhrData.put("routine|" + t.name, "Ok");
                            db.putInput(Session.sessionKey, "routine", t.name, "Ok");
                            Ui.resultDialog(RoutineControlActivity.this, R.drawable.ic_flash_success,
                                    "Routine Completed", t.name + " finished with result Ok.",
                                    "OK", null).show();
                        }
                    }, 1400);
                }
            });
        }
    }
}
