package com.nirixx.app;

import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.os.Bundle;
import android.app.Dialog;

public class VehicleListActivity extends BaseActivity {
    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Vehicle List");
        wireBack();
        content = (LinearLayout) findViewById(R.id.content);
        build();
    }

    private void build() {
        content.removeAllViews();

        LinearLayout search = new LinearLayout(this);
        search.setOrientation(LinearLayout.HORIZONTAL);
        search.setGravity(android.view.Gravity.CENTER_VERTICAL);
        search.setBackgroundResource(R.drawable.bg_card);
        search.setPadding(Ui.dp(this, 14), 0, Ui.dp(this, 14), 0);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 48));
        sp.setMargins(0, 0, 0, Ui.dp(this, 12));
        content.addView(search, sp);
        ImageView si = new ImageView(this);
        si.setImageResource(R.drawable.ic_search);
        search.addView(si, new LinearLayout.LayoutParams(Ui.dp(this, 20), Ui.dp(this, 20)));
        EditText q = new EditText(this);
        q.setHint("Search by model or VIN");
        q.setTextSize(13.5f);
        q.setBackgroundColor(0x00000000);
        q.setPadding(Ui.dp(this, 10), 0, 0, 0);
        search.addView(q, new LinearLayout.LayoutParams(0, -1, 1f));

        content.addView(Ui.section(this, "RECENT VEHICLES (" + Session.vehicles.size() + ")"));
        for (int i = 0; i < Session.vehicles.size(); i++) {
            final String[] veh = Session.vehicles.get(i);
            LinearLayout card = Ui.card(this);
            card.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14));
            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(android.view.Gravity.CENTER_VERTICAL);
            ImageView bike = new ImageView(this);
            bike.setImageResource(R.drawable.motorcycle);
            bike.setColorFilter(0xFF0B8376);
            top.addView(bike, new LinearLayout.LayoutParams(Ui.dp(this, 30), Ui.dp(this, 30)));
            android.widget.TextView t = Ui.tv(this, veh[0], 15f, 0xFF141B2E, true);
            t.setPadding(Ui.dp(this, 12), 0, 0, 0);
            top.addView(t, new LinearLayout.LayoutParams(0, -2, 1f));
            card.addView(top);
            card.addView(Ui.tv(this, veh[1], 12f, 0xFF5A6472, false));
            android.widget.TextView vin = Ui.tv(this, veh[2], 12.5f, 0xFF0B8376, true);
            vin.setPadding(0, Ui.dp(this, 6), 0, 0);
            card.addView(vin);
            android.widget.TextView last = Ui.tv(this, "Last serviced: " + veh[3], 11.5f, 0xFF9AA6B4, false);
            last.setPadding(0, Ui.dp(this, 2), 0, 0);
            card.addView(last);
            card.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Session.selectedVehicle = veh[0];
                    Session.selectedVin = veh[2];
                    go(SelectECUActivity.class);
                }
            });
            content.addView(card);
        }

        Button add = new Button(this);
        add.setText("＋  Add Vehicle");
        add.setTextColor(0xFFFFFFFF);
        add.setTextSize(15f);
        add.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        add.setBackgroundResource(R.drawable.bg_button_blue);
        add.setAllCaps(false);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, Ui.dp(this, 50));
        ap.setMargins(0, Ui.dp(this, 8), 0, Ui.dp(this, 8));
        content.addView(add, ap);
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { showAddDialog(); }
        });
    }

    private void showAddDialog() {
        final Dialog d = new Dialog(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_dialog);
        int p = Ui.dp(this, 22);
        root.setPadding(p, p, p, Ui.dp(this, 16));
        root.addView(Ui.tv(this, "Add Vehicle", 17, 0xFF141B2E, true));
        final android.util.Pair<LinearLayout, EditText> model = field("Model (e.g. TVS Ronin 225)");
        final android.util.Pair<LinearLayout, EditText> variant = field("Variant code");
        final android.util.Pair<LinearLayout, EditText> vin = field("VIN (17 characters)");
        root.addView(model.first); root.addView(variant.first); root.addView(vin.first);
        Button b = new Button(this);
        b.setText("Add");
        b.setTextColor(0xFFFFFFFF);
        b.setBackgroundResource(R.drawable.bg_button_blue);
        b.setAllCaps(false);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 46));
        bp.setMargins(0, Ui.dp(this, 16), 0, 0);
        root.addView(b, bp);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String m = model.second.getText().toString().trim();
                String va = variant.second.getText().toString().trim();
                String vn = vin.second.getText().toString().trim();
                if (m.length() == 0 || vn.length() != 17) { toast("Enter model and a valid 17-char VIN"); return; }
                Session.vehicles.add(0, new String[]{m, va + " · BSVI", vn, "Just now"});
                d.dismiss();
                build();
                toast("Vehicle added");
            }
        });
        d.setContentView(root);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            d.getWindow().setLayout(Ui.dp(this, 310), android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        d.show();
    }

    private android.util.Pair<LinearLayout, EditText> field(String hint) {
        LinearLayout wrap = new LinearLayout(this);
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextSize(13.5f);
        e.setInputType(InputType.TYPE_CLASS_TEXT);
        e.setBackgroundResource(R.drawable.bg_edittext);
        e.setPadding(Ui.dp(this, 12), 0, Ui.dp(this, 12), 0);
        wrap.addView(e, new LinearLayout.LayoutParams(-1, Ui.dp(this, 46)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, Ui.dp(this, 10), 0, 0);
        wrap.setLayoutParams(lp);
        return new android.util.Pair<LinearLayout, EditText>(wrap, e);
    }
}
