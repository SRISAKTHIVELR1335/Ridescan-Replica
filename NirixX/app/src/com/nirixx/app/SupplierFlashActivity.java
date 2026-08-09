package com.nirixx.app;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/** Generic per-supplier flashing screen (one engine — supplier branding + image/bootloader pickers). */
public class SupplierFlashActivity extends BaseActivity {
    private String module, imageType, family;
    private final Handler h = new Handler();
    private ProgressBar bar;
    private TextView stage, pct;
    private android.widget.Button start;
    private int progress = 0;
    private int phase = 0;
    private boolean flashing = false;

    private static final String[] PHASES = {
        "Downloading HEX file", "Erase Request", "Programming", "Verifying", "Write Flash Date",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        module = getIntent().getStringExtra("module");
        imageType = getIntent().getStringExtra("image_type");
        family = getIntent().getStringExtra("family");
        if (module == null) module = "ECU Flashing";
        setContentView(R.layout.activity_screen);
        setTitle(module);
        wireBack();
        build();
    }

    private void build() {
        LinearLayout content = (LinearLayout) findViewById(R.id.content);

        LinearLayout pick = Ui.card(this);
        pick.addView(Ui.tv(this, imageType != null && imageType.startsWith("Select") ? imageType : "Select IMAGE type", 14.5f, 0xFF141B2E, true));
        pick.addView(Ui.tv(this, family != null ? family : "", 12f, 0xFF5A6472, false));
        LinearLayout radios = new LinearLayout(this);
        radios.setOrientation(LinearLayout.VERTICAL);
        radios.setPadding(0, Ui.dp(this, 6), 0, 0);
        final android.widget.RadioGroup rg = new android.widget.RadioGroup(this);
        String[] opts = new String[]{ imageType != null && imageType.startsWith("Select Booloader") ? "Primary Bootloader" : "Application Image (.hex)",
                imageType != null && imageType.startsWith("Select Booloader") ? "Backup Bootloader" : "Bootloader Image (.hex)" };
        for (int i = 0; i < opts.length; i++) {
            android.widget.RadioButton rb = new android.widget.RadioButton(this);
            rb.setText(opts[i]);
            rb.setId(i + 1);
            rg.addView(rb);
        }
        rg.check(1);
        radios.addView(rg);
        pick.addView(radios);
        pick.addView(Ui.kvRow(this, "Flash File Name", Session.selectedFlashFile, true));
        content.addView(pick);

        LinearLayout progCard = Ui.card(this);
        stage = Ui.tv(this, "Ready to flash", 15f, 0xFF141B2E, true);
        pct = Ui.tv(this, "0%", 26f, 0xFF252E66, true);
        pct.setGravity(android.view.Gravity.RIGHT);
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.addView(stage, new LinearLayout.LayoutParams(0, -2, 1f));
        top.addView(pct);
        progCard.addView(top);
        bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_thin));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 8));
        bp.setMargins(0, Ui.dp(this, 10), 0, 0);
        progCard.addView(bar, bp);
        content.addView(progCard);

        TextView warn = Ui.tv(this, getString(R.string.flash_warning_msg), 12.5f, 0xFFC41230, true);
        warn.setBackgroundResource(R.drawable.bg_chip_red);
        warn.setGravity(android.view.Gravity.CENTER);
        warn.setPadding(Ui.dp(this, 12), Ui.dp(this, 10), Ui.dp(this, 12), Ui.dp(this, 10));
        content.addView(warn);

        start = new android.widget.Button(this);
        start.setText("Start Flashing");
        start.setTextColor(0xFFFFFFFF);
        start.setAllCaps(false);
        start.setTextSize(15f);
        start.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        start.setBackgroundResource(R.drawable.bg_button_blue);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 50));
        sp.setMargins(0, Ui.dp(this, 10), 0, Ui.dp(this, 8));
        content.addView(start, sp);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { askOdometer(); }
        });
    }

    /** dialog_odometer — original app asks for ODO reading before flashing. */
    private void askOdometer() {
        final Dialog d = new Dialog(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_dialog);
        int p = Ui.dp(this, 22);
        root.setPadding(p, p, p, Ui.dp(this, 16));
        root.addView(Ui.tv(this, "Odometer Reading", 17f, 0xFF141B2E, true));
        root.addView(Ui.tv(this, "Enter the current odometer value. It is recorded with the flash report.", 12.5f, 0xFF5A6472, false));
        final EditText odo = new EditText(this);
        odo.setHint("e.g. 12840 km");
        odo.setInputType(InputType.TYPE_CLASS_NUMBER);
        odo.setBackgroundResource(R.drawable.bg_edittext);
        odo.setPadding(Ui.dp(this, 12), 0, Ui.dp(this, 12), 0);
        LinearLayout.LayoutParams op = new LinearLayout.LayoutParams(-1, Ui.dp(this, 48));
        op.setMargins(0, Ui.dp(this, 12), 0, 0);
        root.addView(odo, op);
        android.widget.Button go = new android.widget.Button(this);
        go.setText("Continue");
        go.setTextColor(0xFFFFFFFF);
        go.setAllCaps(false);
        go.setBackgroundResource(R.drawable.bg_button_blue);
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 46));
        gp.setMargins(0, Ui.dp(this, 14), 0, 0);
        root.addView(go, gp);
        go.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Session.odometer = odo.getText().toString().trim().length() == 0 ? "—" : odo.getText().toString().trim() + " km";
                d.dismiss();
                beginFlash();
            }
        });
        d.setContentView(root);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            d.getWindow().setLayout(Ui.dp(this, 300), android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        d.show();
    }

    private void beginFlash() {
        flashing = true;
        start.setEnabled(false);
        start.setText("Flashing…");
        progress = 0;
        phase = 0;
        tickPhase();
    }

    private void tickPhase() {
        if (!flashing) return;
        if (phase >= PHASES.length) { complete(); return; }
        stage.setText(phase == 1 ? "Erase Request for " + module : PHASES[phase]);
        h.postDelayed(new Runnable() {
            public void run() {
                if (!flashing) return;
                progress++;
                if (progress > 100) { phase++; tickPhase(); return; }
                bar.setProgress(progress);
                pct.setText(progress + "%");
                if (progress == (phase + 1) * 20) { phase++; }
                tickPhase();
            }
        }, 120);
    }

    private void complete() {
        flashing = false;
        bar.setProgress(100);
        pct.setText("100%");
        stage.setText("ECU Flashing completed");
        start.setText("Flash Again");
        start.setEnabled(true);
        Session.reportGenerated = true;
        Ui.resultDialog(this, R.drawable.ic_flash_success, "ECU Flashing completed",
                module + " flashed successfully.\nOdometer: " + Session.odometer,
                "Done", null).show();
    }

    @Override
    public void onBackPressed() {
        if (flashing) {
            Ui.dialog(this, "Abort flashing?", "dialog_retry_confirmation", "Abort",
                    new Runnable() {
                        public void run() { flashing = false; start.setText("Retry Flashing"); start.setEnabled(true); }
                    }, "Continue", null).show();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        flashing = false;
        h.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
