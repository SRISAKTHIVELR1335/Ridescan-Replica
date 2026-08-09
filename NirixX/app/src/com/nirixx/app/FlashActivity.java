package com.nirixx.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

/** Simulated UDS flashing pipeline: connect → battery check → erase → program → verify → flash date. */
public class FlashActivity extends BaseActivity {
    private TextView txtPct, txtStage, txtStageSub;
    private ProgressBar ring, bar;
    private Button btnAction, btnCancel;
    private final Handler h = new Handler();
    private int pct = 0;
    private int stage = -1;
    private boolean flashing = false;

    private static final String[][] STAGES = {
        {"Starting flashing process", "Requesting diagnostic session (0x10 03)"},
        {"Connecting to ECU", "Security access (0x27) — seed & key"},
        {"Reading Battery Voltage", "12.64 V — OK to proceed"},
        {"Erasing ECU memory", "Routine 0x31 — erase blocks 0..F"},
        {"Programming flash blocks", "TransferData (0x36) — 214 KB image"},
        {"Verifying checksum", "RequestTransferExit (0x37)"},
        {"Write Flash Date", "WriteDataByIdentifier (0x2E)"},
        {"ECU Flashing completed", "ECU reset — finalising session"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash);
        wireBack();
        setTitle("ECU Flashing");

        txtPct = (TextView) findViewById(R.id.txtPct);
        txtStage = (TextView) findViewById(R.id.txtStage);
        txtStageSub = (TextView) findViewById(R.id.txtStageSub);
        ring = (ProgressBar) findViewById(R.id.ringProgress);
        bar = (ProgressBar) findViewById(R.id.barProgress);
        btnAction = (Button) findViewById(R.id.btnFlashAction);
        btnCancel = (Button) findViewById(R.id.btnFlashCancel);

        TextView target = (TextView) findViewById(R.id.txtFlashTarget);
        target.setText(Session.selectedEcu.contains("EMS") ? "EMS — Sedemac (UDS)" : Session.selectedEcu);
        ((TextView) findViewById(R.id.txtFlashVariant))
                .setText("Flash File: " + Session.selectedFlashFile + "  ·  " + Session.selectedVariant);

        btnAction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { startFlash(); }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { abortFlash(false); }
        });
        btnCancel.setVisibility(View.GONE);
    }

    private void startFlash() {
        flashing = true;
        pct = 0;
        stage = 0;
        btnAction.setEnabled(false);
        btnAction.setText("Flashing…");
        btnCancel.setVisibility(View.VISIBLE);
        nextStage();
    }

    private void nextStage() {
        if (!flashing) return;
        if (stage >= STAGES.length) { flashDone(); return; }
        txtStage.setText(STAGES[stage][0]);
        txtStageSub.setText(STAGES[stage][1]);
        final int base = stage * 14;
        final boolean isProgram = stage == 4;
        final int span = isProgram ? 30 : 14;
        tick(base, span, isProgram ? 190 : 330, new Runnable() {
            public void run() { stage++; nextStage(); }
        });
    }

    private void tick(final int base, final int span, final int delay, final Runnable done) {
        if (!flashing) return;
        h.postDelayed(new Runnable() {
            public void run() {
                if (!flashing) return;
                int local = pct - base;
                if (local >= span) { done.run(); return; }
                pct++;
                ring.setProgress(pct);
                bar.setProgress(pct);
                txtPct.setText(String.format(Locale.US, "%d%%", pct));
                tick(base, span, delay, done);
            }
        }, delay);
    }

    private void flashDone() {
        flashing = false;
        pct = 100;
        ring.setProgress(100);
        bar.setProgress(100);
        txtPct.setText("100%");
        txtStage.setText("ECU Flashing completed");
        txtStageSub.setText(Session.selectedFlashFile + " written successfully");
        btnCancel.setVisibility(View.GONE);
        btnAction.setText("Flash Again");
        btnAction.setEnabled(true);
        Session.reportGenerated = true;
        Ui.resultDialog(this, R.drawable.ic_flash_success,
                "ECU Flashing completed",
                Session.selectedFlashFile + " flashed to " + Session.selectedEcu
                        + "\n" + Session.selectedVehicle + " · " + Session.selectedVin,
                "View Diagnostic Report",
                new Runnable() { public void run() { go(DiagnosticReportActivity.class); } }).show();
    }

    private void abortFlash(boolean system) {
        if (!flashing) { onBackPressed(); return; }
        Ui.dialog(this, "Abort flashing?",
                "Interrupting a flash can leave the ECU in boot mode.\nDo you want to retry or abort the session?",
                "Abort", new Runnable() {
                    public void run() {
                        flashing = false;
                        h.removeCallbacksAndMessages(null);
                        txtStage.setText("Flash aborted by user");
                        txtStageSub.setText("Re-run flashing to recover the ECU session");
                        btnCancel.setVisibility(View.GONE);
                        btnAction.setText("Retry Flashing");
                        btnAction.setEnabled(true);
                    }
                }, "Continue", null).show();
    }

    @Override
    public void onBackPressed() {
        if (flashing) abortFlash(true); else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        flashing = false;
        h.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
