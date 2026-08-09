package com.ridescan.replica;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Live data recording sessions list + replay view. */
public class LiveDataRecordingActivity extends BaseActivity {
    private static final String[][] SESSIONS = {
        {"REC_20260809_094213.csv", "Ronin 225 · 42 s · 8 channels", "Today 09:42"},
        {"REC_20260808_170311.csv", "RTR 160 4V · 2 m 10 s · 8 channels", "Yesterday"},
        {"REC_20260807_110528.csv", "Raider 125 · 1 m 04 s · 6 channels", "Mon"},
    };
    private final Handler h = new Handler();
    private boolean playing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("Live Data Recording");
        wireBack();
        LinearLayout content = (LinearLayout) findViewById(R.id.content);

        content.addView(Ui.section(this, "RECORDED SESSIONS"));
        for (int i = 0; i < SESSIONS.length; i++) {
            final String[] s = SESSIONS[i];
            LinearLayout card = Ui.card(this);
            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(android.view.Gravity.CENTER_VERTICAL);
            top.addView(Ui.tv(this, s[0], 13.5f, 0xFF141B2E, true), new LinearLayout.LayoutParams(0, -2, 1f));
            top.addView(Ui.chip(this, "▶ Play", R.drawable.bg_chip, 0xFF00347E));
            card.addView(top);
            TextView d = Ui.tv(this, s[1] + "  ·  " + s[2], 12f, 0xFF5A6472, false);
            d.setPadding(0, Ui.dp(this, 4), 0, 0);
            card.addView(d);

            final LinearLayout graph = new LinearLayout(this);
            graph.setOrientation(LinearLayout.VERTICAL);
            graph.setBackgroundResource(R.drawable.bg_console);
            graph.setPadding(Ui.dp(this, 12), Ui.dp(this, 10), Ui.dp(this, 12), Ui.dp(this, 10));
            graph.setVisibility(View.GONE);
            final TextView gtxt = Ui.tv(this, "", 11f, 0xFF8FD6A4, false);
            gtxt.setTypeface(android.graphics.Typeface.MONOSPACE);
            graph.addView(gtxt);
            card.addView(graph);

            card.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (graph.getVisibility() == View.VISIBLE) {
                        graph.setVisibility(View.GONE);
                        playing = false;
                    } else {
                        graph.setVisibility(View.VISIBLE);
                        replay(gtxt);
                    }
                }
            });
            content.addView(card);
        }

        android.widget.TextView hint = Ui.tv(this, "Tap a session to replay the recorded stream.", 12f, 0xFF9AA6B4, false);
        hint.setPadding(Ui.dp(this, 4), 0, 0, Ui.dp(this, 8));
        content.addView(hint);
    }

    private void replay(final TextView out) {
        playing = true;
        final int[] t = {0};
        out.setText("t(s)  RPM    TPS%   BATTV   TEMP°C\n");
        h.postDelayed(new Runnable() {
            public void run() {
                if (!playing) return;
                java.util.Random r = new java.util.Random();
                out.append(String.format(java.util.Locale.US, "%3d  %5d  %4d  %5.2f   %3d\n",
                        t[0]++, 1400 + r.nextInt(220), 10 + r.nextInt(8),
                        12.5f + r.nextFloat() * 0.4f, 85 + r.nextInt(5)));
                if (t[0] < 14) replay(out);
                else out.append("— end of recording —");
            }
        }, 350);
    }

    @Override
    protected void onDestroy() {
        playing = false;
        super.onDestroy();
    }
}
