package com.nirixx.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;

public class ServiceManualActivity extends BaseActivity {
    private static final String[][] DOCS = {
        {"Ronin 225 — Service Manual (Rev 4)", "PDF · 18.4 MB · EN"},
        {"Raider 125 — ECM Pinout & Wiring Diagram", "PDF · 6.1 MB · EN"},
        {"Apache RTR 160 4V — Service Manual", "PDF · 22.9 MB · EN"},
        {"NTORQ 125 — SmartXonnect Cluster Guide", "PDF · 4.7 MB · EN"},
        {"Jupiter 110 — ISG Charging System", "PDF · 3.2 MB · EN"},
        {"Common Rail Diagnostic Procedures (All Models)", "PDF · 9.8 MB · EN"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Service Manual");
        wireBack();
        LinearLayout content = (LinearLayout) findViewById(R.id.content);
        content.addView(Ui.section(this, "WORKSHOP DOCUMENTS (KETCH DOWNLOADER)"));
        for (int i = 0; i < DOCS.length; i++) {
            final String[] d = DOCS[i];
            LinearLayout row = Ui.listRow(this, R.drawable.dtclibrary, d[0], d[1], false);
            android.widget.TextView dl = Ui.chip(this, "Download", R.drawable.bg_chip, 0xFF0B8376);
            ((LinearLayout) row).addView(dl);
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { download(d[0]); }
            });
            content.addView(row);
        }
    }

    private void download(final String name) {
        final android.app.Dialog d = Ui.progressDialog(this, "Downloading " + name + " …");
        d.show();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                d.dismiss();
                toast("Saved to /NirixX/Manuals/ — opening viewer…");
            }
        }, 1400);
    }
}
