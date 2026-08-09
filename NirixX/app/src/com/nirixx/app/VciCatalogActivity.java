package com.nirixx.app;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nirixx.app.vci.VciManager;
import com.nirixx.app.vci.VciModel;
import com.nirixx.app.vci.VciTransport;

/** "Supported VCIs" — every VCI NirixX can talk to: own hardware family plus
 *  compatible third-party adapters that work with Android. */
public class VciCatalogActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Supported VCIs");
        wireBack();
        LinearLayout content = (LinearLayout) findViewById(R.id.content);

        TextView intro = Ui.tv(this,
                "NirixX works with the full VCI family below. Real Bluetooth discovery is live — pair any device and the session upgrades from simulation to a genuine SPP/BLE link automatically.",
                12.5f, 0xFF5A6472, false);
        intro.setPadding(0, 0, 0, Ui.dp(this, 8));
        content.addView(intro);

        VciModel[] all = VciModel.all();
        content.addView(Ui.section(this, "NIRIXX HARDWARE"));
        for (int i = 0; i < all.length; i++) {
            if (!all[i].thirdParty) content.addView(row(all[i]));
        }
        content.addView(Ui.section(this, "COMPATIBLE THIRD-PARTY ADAPTERS"));
        for (int i = 0; i < all.length; i++) {
            if (all[i].thirdParty) content.addView(row(all[i]));
        }
    }

    private LinearLayout row(final VciModel m) {
        LinearLayout card = Ui.card(this);
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        int iconRes = getResources().getIdentifier(m.drawable, "drawable", getPackageName());
        ImageView iv = new ImageView(this);
        iv.setImageResource(iconRes > 0 ? iconRes : R.drawable.vci);
        iv.setBackgroundResource(R.drawable.bg_console);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(Ui.dp(this, 74), Ui.dp(this, 74));
        ip.setMargins(0, 0, Ui.dp(this, 12), 0);
        top.addView(iv, ip);

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.addView(Ui.tv(this, m.name, 15.5f, 0xFF141B2E, true));
        text.addView(Ui.tv(this, m.linkType, 11.5f, 0xFF35507A, false));
        top.addView(text, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        card.addView(top);
        card.addView(Ui.tv(this, m.blurb, 12f, 0xFF5A6472, false));
        card.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { detail(m); }
        });
        return card;
    }

    private void detail(final VciModel m) {
        int iconRes = getResources().getIdentifier(m.drawable, "drawable", getPackageName());
        Ui.resultDialog(this, iconRes > 0 ? iconRes : R.drawable.vci,
                m.name,
                m.blurb + "\n\nLink: " + m.linkType + "\nFirmware: " + m.firmware,
                "Use this VCI",
                new Runnable() {
                    public void run() { connect(m); }
                }).show();
    }

    private void connect(final VciModel m) {
        final android.app.Dialog d = Ui.progressDialog(this, "Opening session with " + m.name + "…");
        d.show();
        VciManager.get().connect(this, null, m.name, new VciTransport.Listener() {
            public void onState(final int state, final String message) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (state != VciTransport.STATE_CONNECTED) return;
                        d.dismiss();
                        Session.vciConnected = true;
                        Session.vciName = m.name;
                        Ui.resultDialog(VciCatalogActivity.this, R.drawable.ic_flash_success,
                                "VCI Ready",
                                m.name + " session established.\nTransport: "
                                        + VciManager.get().transport().describe(),
                                "Done", new Runnable() {
                                    public void run() { finish(); }
                                }).show();
                    }
                });
            }
            public void onBytes(byte[] data, int length) { }
            public void onError(String message) { }
        });
    }
}
