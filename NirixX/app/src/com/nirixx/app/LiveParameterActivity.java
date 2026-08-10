package com.nirixx.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.nirixx.app.db.Db;
import com.nirixx.app.sim.SimEcu;
import com.nirixx.app.sim.UdsLog;
import java.util.ArrayList;
import java.util.List;

/** Live Parameter screen (reference): category filter dropdown + parameter
 *  cards (title, ⓘ info, category, big value) refreshed live from the ECU
 *  stream; every refresh is also sampled into the database. */
public class LiveParameterActivity extends BaseActivity {

    private final Handler h = new Handler();
    private boolean tick = false;
    private LinearLayout list;
    private Db db;
    private List<Db.TestDef> defs;
    private String filter = "ALL";
    private final List<TextView> valSlots = new ArrayList<TextView>();
    private final List<Db.TestDef> slotDefs = new ArrayList<Db.TestDef>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Live Parameter");
        showEcuChip(Session.selectedEcuCode, true);
        db = Db.get(this);
        Session.ensureSession(this);

        LinearLayout content = (LinearLayout) findViewById(R.id.content);
        content.removeAllViews();
        content.addView(Ui.crumbs(this, new String[]{"Home", Session.selectedVehicle,
                Session.selectedEcuCode, "Live Parameter"}));

        defs = db.tests(Session.ecuId, "live");

        // category filter, like the ALL dropdown in the reference
        final List<String> cats = new ArrayList<String>();
        cats.add("ALL");
        for (Db.TestDef t : defs) if (t.category != null && !cats.contains(t.category)) cats.add(t.category);
        Spinner spin = new Spinner(this);
        ArrayAdapter<String> ad = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, cats);
        spin.setAdapter(ad);
        spin.setBackgroundResource(R.drawable.bg_box_outline);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 44));
        sp.setMargins(0, 0, 0, Ui.dp(this, 10));
        content.addView(spin, sp);
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filter = cats.get(position);
                render();
            }
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        content.addView(list);
        render();
    }

    private void render() {
        list.removeAllViews();
        valSlots.clear();
        slotDefs.clear();
        for (Db.TestDef t : defs) {
            if (!"ALL".equals(filter) && !filter.equals(t.category)) continue;
            LinearLayout card = Ui.paramCard(this, t.name, t.category, SimEcu.liveValue(t),
                    t.vmax == t.vmin ? "—" : String.valueOf(t.vmin),
                    t.vmax == t.vmin ? "—" : String.valueOf(t.vmax),
                    "Sampled over " + Session.ecuTx + "/" + Session.ecuRx + " · " + Session.connectivity);
            TextView val = (TextView) card.getTag();
            valSlots.add(val);
            slotDefs.add(t);
            list.addView(card);
        }
        tick = true;
        stream();
    }

    private void stream() {
        h.postDelayed(new Runnable() {
            public void run() {
                if (!tick) return;
                for (int i = 0; i < slotDefs.size(); i++) {
                    Db.TestDef t = slotDefs.get(i);
                    String v = SimEcu.liveValue(t);
                    valSlots.get(i).setText(v);
                    valSlots.get(i).setTextColor(SimEcu.inRange(t, v) ? 0xFF1A2138 : 0xFFE53935);
                    db.sample(Session.sessionKey, t.id, v);
                }
                UdsLog.log(LiveParameterActivity.this, "TX", Session.ecuTx + " -> 22B000");
                UdsLog.log(LiveParameterActivity.this, "RX", Session.ecuRx + " -> 62B000…");
                h.postDelayed(this, 1500);
            }
        }, 1500);
    }

    @Override
    protected void onPause() { tick = false; super.onPause(); }
}
