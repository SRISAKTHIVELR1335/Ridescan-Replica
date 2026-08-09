package com.nirixx.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/** In-app self-update: "Downloading File..." → install package flow stub. */
public class UpdateActivity extends BaseActivity {
    private ProgressBar bar;
    private TextView state;
    private Button action;
    private final Handler h = new Handler();
    private int pct = 0;
    private boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("App Update");
        wireBack();
        LinearLayout content = (LinearLayout) findViewById(R.id.content);

        android.widget.ImageView art = new android.widget.ImageView(this);
        art.setImageResource(R.drawable.report_cover);
        art.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        art.setBackgroundResource(R.drawable.bg_console);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, Ui.dp(this, 150));
        ap.setMargins(0, 0, 0, Ui.dp(this, 10));
        content.addView(art, ap);

        LinearLayout card = Ui.card(this);
        card.addView(Ui.tv(this, "NirixX 1.1.0", 17f, 0xFF141B2E, true));
        card.addView(Ui.tv(this, "nirixx.io/updates/nirixx-1.1.0.apk", 12f, 0xFF5A6472, false));
        state = Ui.tv(this, "Downloading File...", 13.5f, 0xFF0B8376, true);
        state.setPadding(0, Ui.dp(this, 14), 0, 0);
        card.addView(state);
        bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_thin));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 8));
        bp.setMargins(0, Ui.dp(this, 8), 0, 0);
        card.addView(bar, bp);
        TextView pct_ = Ui.tv(this, "0 / 21.4 MB", 11.5f, 0xFF5A6472, false);
        pct_.setTag("pct");
        card.addView(pct_);
        content.addView(card);

        action = new Button(this);
        action.setText("Download & Install");
        action.setTextColor(0xFFFFFFFF);
        action.setAllCaps(false);
        action.setTextSize(15f);
        action.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        action.setBackgroundResource(R.drawable.bg_button_blue);
        content.addView(action, new LinearLayout.LayoutParams(-1, Ui.dp(this, 50)));
        action.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { start(); }
        });

        TextView skip = Ui.tv(this, "Skip (5)", 13f, 0xFF5A6472, true);
        skip.setGravity(android.view.Gravity.CENTER);
        skip.setPadding(0, Ui.dp(this, 16), 0, 0);
        content.addView(skip);
        skip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Ui.dialog(UpdateActivity.this, "Skip update?",
                        "If the update is not required this time, you can skip the upgrade now. But you are only allowed to skip the upgrade 5 times.",
                        "Skip", new Runnable() { public void run() { finish(); } }, "Cancel", null).show();
            }
        });
    }

    private void start() {
        running = true;
        action.setEnabled(false);
        action.setText("Downloading…");
        h.postDelayed(new Runnable() {
            public void run() {
                if (!running) return;
                pct += 2;
                bar.setProgress(pct);
                View card = ((LinearLayout) findViewById(R.id.content)).getChildAt(0);
                TextView pct_ = (TextView) card.findViewWithTag("pct");
                if (pct_ != null) pct_.setText(String.format(java.util.Locale.US, "%.1f / 21.4 MB", pct * 0.214f));
                if (pct < 100) start();
                else {
                    running = false;
                    state.setText("Download complete");
                    action.setText("Install");
                    action.setEnabled(true);
                    action.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Ui.resultDialog(UpdateActivity.this, R.drawable.ic_flash_success,
                                    "Ready to Install", "REQUEST_INSTALL_PACKAGES granted.\nPackage installer will take over on a real deployment.",
                                    "OK", null).show();
                        }
                    });
                }
            }
        }, 90);
    }
}
