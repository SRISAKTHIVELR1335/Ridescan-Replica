package com.ridescan.replica;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Raw diagnostic/service log viewer with level filters. */
public class LogViewerActivity extends BaseActivity {
    private static final String[][] LOG = {
        {"D", "SerialService", "RFCOMM socket opened — TZ-NEW-VCI-31C7"},
        {"D", "btlibrary", "TX 02 10 03  diagnosticSessionControl(extended)"},
        {"D", "btlibrary", "RX 06 50 03 00 32 01 F4  positiveResponse"},
        {"V", "UDS", "Session switch OK, P2=50ms P2*=1500ms"},
        {"D", "btlibrary", "TX 04 27 01  securityAccess(requestSeed)"},
        {"D", "btlibrary", "RX 08 67 01 9A 44 11 02  seed received"},
        {"D", "Security", "key derived (native module) — send 27 02"},
        {"D", "btlibrary", "RX 02 67 02  access granted"},
        {"I", "Flash", "Erase request for blocks 0..F → routine FF00"},
        {"D", "btlibrary", "TX 3E 00  testerPresent"},
        {"W", "btlibrary", "ISO-TP WFT limiter hit — flow control stmin adjusted"},
        {"I", "Flash", "TransferData 214 KB complete — CRC verified"},
        {"E", "DMS", "bootstrap sync deferred — network unreachable (retry queued)"},
        {"D", "AppCloseService", "state persisted, BT link released"},
    };
    private LinearLayout list;
    private String filter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Log Viewer");
        wireBack();
        LinearLayout content = (LinearLayout) findViewById(R.id.content);

        LinearLayout filters = new LinearLayout(this);
        filters.setOrientation(LinearLayout.HORIZONTAL);
        final String[] lv = {"ALL", "V", "D", "I", "W", "E"};
        for (int i = 0; i < lv.length; i++) {
            final String l = lv[i];
            TextView chip = Ui.chip(this, l, R.drawable.bg_chip, 0xFF00347E);
            chip.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { filter = l; render(); }
            });
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-2, -2);
            cp.setMargins(0, 0, Ui.dp(this, 8), 0);
            filters.addView(chip, cp);
        }
        content.addView(filters);

        LinearLayout cons = new LinearLayout(this);
        cons.setOrientation(LinearLayout.VERTICAL);
        cons.setBackgroundResource(R.drawable.bg_console);
        cons.setPadding(Ui.dp(this, 12), Ui.dp(this, 10), Ui.dp(this, 12), Ui.dp(this, 10));
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        cons.addView(list);
        content.addView(cons);
        render();
    }

    private void render() {
        list.removeAllViews();
        for (int i = 0; i < LOG.length; i++) {
            String[] l = LOG[i];
            if (!"ALL".equals(filter) && !l[0].equals(filter)) continue;
            int color = l[0].equals("E") ? 0xFFFF8A80 : (l[0].equals("W") ? 0xFFFFD54F : 0xFF8FD6A4);
            TextView t = Ui.tv(this, l[0] + "/" + l[1] + ": " + l[2], 11f, color, false);
            t.setTypeface(android.graphics.Typeface.MONOSPACE);
            t.setPadding(0, Ui.dp(this, 3), 0, Ui.dp(this, 3));
            list.addView(t);
        }
    }
}
