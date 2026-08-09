package com.nirixx.app;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Raw artifact browser for reports, logs and firmware assets. */
public class FileViewerActivity extends BaseActivity {
    private static final String[][] FILES = {
        {"/NirixX/Reports/VHR_MD634NF4XRCL12345.pdf", "412 KB · Today"},
        {"/NirixX/Reports/Diagnostic_RTR1604V.pdf", "288 KB · Yesterday"},
        {"/NirixX/Reports/BatteryHealth_NTORQ125.pdf", "154 KB · Mon"},
        {"/NirixX/Logs/nirixx_session.log", "61 KB · Today"},
        {"/NirixX/Flash/RR310_REFRESH_NEW.hex", "214 KB · bundled"},
        {"/NirixX/Firmware/vcf-renesas_78k0r.s19", "96 KB · bundled asset"},
        {"/NirixX/Config/flash_variant.json", "13 KB · bundled asset"},
        {"/NirixX/Recordings/REC_20260809_094213.csv", "8 KB · Today"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("File Viewer");
        wireBack();
        LinearLayout content = (LinearLayout) findViewById(R.id.content);
        content.addView(Ui.section(this, "APP STORAGE"));
        for (int i = 0; i < FILES.length; i++) {
            final String[] f = FILES[i];
            LinearLayout row = Ui.listRow(this, f[0].endsWith(".pdf") ? R.drawable.vehicle_health_report
                    : (f[0].endsWith(".hex") || f[0].endsWith(".s19") ? R.drawable.firmwarecloud : R.drawable.dtclibrary),
                    f[0].substring(f[0].lastIndexOf('/') + 1), f[0] + "\n" + f[1], false);
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Ui.dialog(FileViewerActivity.this, f[0].substring(f[0].lastIndexOf('/') + 1),
                            f[0] + "\n" + f[1] + "\n\nPreview is stubbed in this build.",
                            "Close", null, null, null).show();
                }
            });
            content.addView(row);
        }
    }
}
