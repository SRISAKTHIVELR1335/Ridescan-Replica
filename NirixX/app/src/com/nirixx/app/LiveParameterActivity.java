package com.nirixx.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

public class LiveParameterActivity extends BaseActivity {
    private LinearLayout content;
    private final Handler h = new Handler();
    private boolean running = true;
    private boolean recording = false;
    private int recSecs = 0;
    private TextView recChip;

    private static class P {
        String name, unit; float min, max, value; int span;
        P(String n, String u, float mn, float mx, float v, int sp) { name=n; unit=u; min=mn; max=mx; value=v; span=sp; }
    }
    private final P[] params = new P[]{
        new P("Engine Speed", "rpm", 0, 6000, 1450, 140),
        new P("Throttle Position", "%", 0, 100, 12, 3),
        new P("Battery Voltage", "V", 10, 15, 12.64f, 2),
        new P("Engine Temperature", "°C", 20, 120, 87, 2),
        new P("Vehicle Speed", "km/h", 0, 120, 0, 0),
        new P("Intake Air Pressure", "kPa", 10, 110, 34, 6),
        new P("Lambda (λ)", "", 0.7f, 1.3f, 1.001f, 3),
        new P("Ignition Advance", "°BTDC", 0, 40, 12, 4),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Live Parameters — " + Session.selectedEcuShort);
        wireBack();
        content = (LinearLayout) findViewById(R.id.content);
        build();
        tick();
    }

    private void build() {
        content.removeAllViews();

        LinearLayout hdr = new LinearLayout(this);
        hdr.setOrientation(LinearLayout.HORIZONTAL);
        hdr.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2);
        hp.setMargins(0, 0, 0, Ui.dp(this, 8));
        content.addView(hdr, hp);
        hdr.addView(Ui.chip(this, "LIVE — updating via VCI", R.drawable.bg_chip_green, 0xFF1E7A46));
        recChip = Ui.chip(this, "● Recording 00:00", R.drawable.bg_chip_red, 0xFFC41230);
        recChip.setVisibility(View.GONE);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-2, -2);
        rp.setMargins(Ui.dp(this, 8), 0, 0, 0);
        hdr.addView(recChip, rp);

        content.addView(Ui.section(this, "ENGINE DATA STREAM (UDS 0x22)"));
        for (int i = 0; i < params.length; i++) {
            content.addView(paramRow(params[i]));
        }

        android.widget.Button rec = new android.widget.Button(this);
        rec.setText("Start Live Data Recording");
        rec.setTextColor(0xFFFFFFFF);
        rec.setAllCaps(false);
        rec.setBackgroundResource(R.drawable.bg_button_blue);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 48));
        bp.setMargins(0, Ui.dp(this, 6), 0, Ui.dp(this, 8));
        content.addView(rec, bp);
        rec.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                android.widget.Button b = (android.widget.Button) v;
                recording = !recording;
                recChip.setVisibility(recording ? View.VISIBLE : View.GONE);
                b.setText(recording ? "Stop Recording" : "Start Live Data Recording");
                if (!recording) {
                    toast("Recording saved — see File Viewer");
                    recSecs = 0;
                }
            }
        });
    }

    private LinearLayout paramRow(P pd) {
        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(android.view.Gravity.CENTER_VERTICAL);
        top.addView(Ui.tv(this, pd.name, 13.5f, 0xFF141B2E, true), new LinearLayout.LayoutParams(0, -2, 1f));
        TextView val = Ui.tv(this, "", 15f, 0xFF252E66, true);
        val.setTag("VAL");
        top.addView(val);
        card.addView(top);

        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(1000);
        bar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_thin));
        bar.setTag("BAR");
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 6));
        bp.setMargins(0, Ui.dp(this, 8), 0, 0);
        card.addView(bar, bp);
        card.setTag(pd);
        updateRow(card, pd);
        return card;
    }

    private void updateRow(View card, P pd) {
        TextView val = (TextView) card.findViewWithTag("VAL");
        ProgressBar bar = (ProgressBar) card.findViewWithTag("BAR");
        float norm = (pd.value - pd.min) / (pd.max - pd.min);
        if (norm < 0) norm = 0; if (norm > 1) norm = 1;
        bar.setProgress((int) (norm * 1000));
        if ("".equals(pd.unit)) {
            val.setText(String.format(Locale.US, "%.3f", pd.value));
        } else if ("V".equals(pd.unit)) {
            val.setText(String.format(Locale.US, "%.2f %s", pd.value, pd.unit));
        } else {
            val.setText(String.format(Locale.US, "%.0f %s", pd.value, pd.unit));
        }
    }

    private void tick() {
        if (!running) return;
        h.postDelayed(new Runnable() {
            public void run() {
                java.util.Random r = new java.util.Random();
                for (int i = 0; i < params.length; i++) {
                    P pd = params[i];
                    float jitter = (r.nextFloat() - 0.5f) * 2f * pd.span;
                    if ("V".equals(pd.unit)) jitter /= 100f;
                    if ("".equals(pd.unit)) { pd.value = 0.995f + r.nextFloat() * 0.012f; }
                    else pd.value += jitter;
                    if (pd.value > pd.max) pd.value = pd.max;
                    if (pd.value < pd.min) pd.value = pd.min;
                    View row = content.getChildAt(i + 2);
                    if (row != null && row.getTag() instanceof P) updateRow(row, pd);
                }
                if (recording) {
                    recSecs++;
                    recChip.setText("● Recording 00:" + String.format(Locale.US, "%02d", recSecs % 60));
                }
                tick();
            }
        }, 900);
    }

    @Override
    protected void onDestroy() {
        running = false;
        super.onDestroy();
    }
}
