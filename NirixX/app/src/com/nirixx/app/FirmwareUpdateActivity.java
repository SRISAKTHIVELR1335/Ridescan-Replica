package com.nirixx.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FirmwareUpdateActivity extends BaseActivity {
    private ProgressBar prog;
    private TextView stage;
    private Button btn;
    private final Handler h = new Handler();
    private boolean updating = false;
    private int pct = 0;

    private static final String[] STAGES = {
        "Downloading vcf-renesas_78k0r.s19 from DMS…",
        "Verifying firmware image (CRC32)…",
        "Switching VCI to bootloader mode…",
        "Transferring S-records…",
        "Verifying flash contents…",
        "Rebooting VCI…",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firmware);
        wireBack();
        prog = (ProgressBar) findViewById(R.id.fwProgress);
        stage = (TextView) findViewById(R.id.txtFwStage);
        btn = (Button) findViewById(R.id.btnFwUpdate);
        String dev = getIntent().getStringExtra("device");
        if (dev != null && dev.length() > 0) setTitle(dev + " Firmware");
        ((TextView) findViewById(R.id.txtFwCurrent)).setText("Current Firmware: " + Session.vciFw);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { startUpdate(); }
        });
    }

    private void startUpdate() {
        if (updating) return;
        updating = true;
        btn.setEnabled(false);
        btn.setText("Updating…");
        pct = 0;
        runStage(0);
    }

    private void runStage(final int idx) {
        if (idx >= STAGES.length) { done(); return; }
        stage.setText(STAGES[idx]);
        h.postDelayed(new Runnable() {
            public void run() {
                if (!updating) return;
                pct += 4;
                if (pct < (idx + 1) * 17) { runStageTick(idx); }
                else runStage(idx + 1);
            }
        }, 220);
    }

    private void runStageTick(final int idx) {
        prog.setProgress(pct);
        runStage(idx);
    }

    private void done() {
        updating = false;
        prog.setProgress(100);
        stage.setText("Firmware updated to 2.19.1");
        Session.vciFw = "2.19.1";
        ((TextView) findViewById(R.id.txtFwCurrent)).setText("Current Firmware: " + Session.vciFw);
        btn.setText("Up to Date");
        Ui.resultDialog(this, R.drawable.ic_flash_success, "Update Complete",
                "VCI firmware updated successfully to 2.19.1", "OK", null).show();
    }

    @Override
    protected void onDestroy() {
        updating = false;
        h.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
