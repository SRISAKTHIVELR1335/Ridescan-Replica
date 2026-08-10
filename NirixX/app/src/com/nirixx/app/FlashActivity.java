package com.nirixx.app;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.nirixx.app.db.Db;
import com.nirixx.app.sim.SimEcu;
import com.nirixx.app.sim.UdsLog;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** ECU Flashing — pixel-level replica of the reference flow:
 *  preconditions dialog (5 numbered checks + instruction-video box + helpline
 *  pill + CANCEL/CONTINUE), 4-segment progress (Download → Load → Flashing →
 *  Reset), VIN/CVN/CAL/Batt/File bullet card and the numbered timestamped log
 *  stream (green Success lines). Everything lands in the session .txt + DB. */
public class FlashActivity extends BaseActivity {

    private LinearLayout content, logBox;
    private final Handler h = new Handler();
    private final View[] seg = new View[4];
    private final TextView[] segCheck = new TextView[4];
    private int logNo = 1;
    private boolean flashing = false;
    private Db db;

    private static final String[] PHASES = {"Download", "Load", "Flashing", "Reset"};

    private static final String[] SCRIPT = {
            "Success", "Memory check for Top Area", "Success", "Erase Request for Bottom Area",
            "Success", "Request Transfer Data for Bottom Area", "Chunk data chunk transferred… 391/391 for Bottom Area",
            "Success", "Transfer exit for Bottom Area", "Success", "Memory check for Bottom Area",
            "Success", "ECU Reset", "Success", "ECU Flashing completed"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        setTitle("ECU Flashing");
        showEcuChip(Session.selectedEcuCode, true);
        db = Db.get(this);
        Session.ensureSession(this);
        content = (LinearLayout) findViewById(R.id.content);
        preconditions();
    }

    // ---------------------------------------------------------- preconditions
    private void preconditions() {
        final Dialog d = new Dialog(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_dialog);
        int p = Ui.dp(this, 20);
        root.setPadding(p, p, p, Ui.dp(this, 12));
        TextView title = Ui.tv(this, "Check Following Condition", 17f, 0xFF1A2138, true);
        title.setGravity(android.view.Gravity.CENTER);
        root.addView(title);
        String conditions =
                "1. Don't Switch off the Ignition Key During the Flashing.\n\n"
                        + "2. Don't Switch off the Engine Kill Switch During the Flashing.\n\n"
                        + "3. Don't Press Back Button in Device till Completion of Flashing Process.\n\n"
                        + "4. Battery Voltage Should be above 12 Volts.\n\n"
                        + "5. Diagnostic Device(Tab) Charge Should be 30%.";
        TextView body = Ui.tv(this, conditions, 13.5f, 0xFF1A2138, false);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2);
        bp.setMargins(0, Ui.dp(this, 10), 0, 0);
        root.addView(body, bp);

        LinearLayout video = new LinearLayout(this);
        video.setOrientation(LinearLayout.VERTICAL);
        video.setGravity(android.view.Gravity.CENTER);
        video.setBackgroundResource(R.drawable.bg_box_outline);
        video.setPadding(0, Ui.dp(this, 14), 0, Ui.dp(this, 14));
        android.widget.ImageView play = new android.widget.ImageView(this);
        play.setImageResource(R.drawable.ic_play);
        video.addView(play, new LinearLayout.LayoutParams(Ui.dp(this, 46), Ui.dp(this, 46)));
        video.addView(Ui.tv(this, "Watch the full instruction video to\ncontinue", 13.5f, 0xFF1A2138, true));
        LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(-1, -2);
        vp.setMargins(0, Ui.dp(this, 12), 0, 0);
        root.addView(video, vp);

        TextView cc = Ui.tv(this, "If any App Crashes Please Contact.", 14.5f, 0xFF1A2138, true);
        cc.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(0, Ui.dp(this, 12), 0, 0);
        root.addView(cc, cp);
        LinearLayout pill = Ui.phonePill(this, db.config("support_number", "+917969478770"));
        LinearLayout.LayoutParams pp2 = new LinearLayout.LayoutParams(-2, -2);
        pp2.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        pp2.setMargins(0, Ui.dp(this, 8), 0, 0);
        root.addView(pill, pp2);
        pill.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + db.config("support_number", "+917969478770"))));
            }
        });

        LinearLayout btns = new LinearLayout(this);
        btns.setOrientation(LinearLayout.HORIZONTAL);
        TextView cancel = Ui.tv(this, "CANCEL", 14.5f, 0xFF5A6472, true);
        cancel.setGravity(android.view.Gravity.CENTER);
        cancel.setPadding(0, Ui.dp(this, 12), 0, Ui.dp(this, 12));
        final TextView cont = Ui.tv(this, "CONTINUE", 14.5f, 0xFF5A6472, true);
        cont.setGravity(android.view.Gravity.CENTER);
        cont.setBackgroundResource(R.drawable.bg_segment_grey);
        cont.setPadding(0, Ui.dp(this, 12), 0, Ui.dp(this, 12));
        btns.addView(cancel, new LinearLayout.LayoutParams(0, -2, 1f));
        LinearLayout.LayoutParams cnt = new LinearLayout.LayoutParams(0, -2, 1f);
        cnt.setMargins(Ui.dp(this, 10), 0, 0, 0);
        btns.addView(cont, cnt);
        LinearLayout.LayoutParams bpr = new LinearLayout.LayoutParams(-1, -2);
        bpr.setMargins(0, Ui.dp(this, 14), 0, 0);
        root.addView(btns, bpr);

        d.setContentView(root);
        d.setCancelable(true);
        d.show();
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { d.dismiss(); finish(); }
        });

        // CONTINUE arms only after the instruction video box was played through
        video.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Dialog pv = Ui.progressDialog(FlashActivity.this, "Playing instruction video… 0:00 / 0:42");
                pv.show();
                h.postDelayed(new Runnable() { public void run() { pv.setTitle("Playing instruction video… 0:21 / 0:42"); } }, 1200);
                h.postDelayed(new Runnable() {
                    public void run() {
                        pv.dismiss();
                        cont.setTextColor(0xFFFFFFFF);
                        cont.setBackgroundResource(R.drawable.bg_button_navy);
                        cont.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v2) { d.dismiss(); buildFlashUi(); }
                        });
                    }
                }, 2400);
            }
        });
    }

    // ---------------------------------------------------------- flash screen
    private void buildFlashUi() {
        content.removeAllViews();
        content.addView(Ui.crumbs(this, new String[]{"Home", Session.selectedVehicle,
                Session.selectedEcuCode, "EMSC Flashing"}));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14));

        // 4-segment progress
        LinearLayout prog = new LinearLayout(this);
        prog.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < 4; i++) {
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            TextView label = Ui.tv(this, PHASES[i], 12.5f, 0xFF1A2138, false);
            label.setGravity(android.view.Gravity.CENTER);
            cell.addView(label);
            android.widget.FrameLayout track = new android.widget.FrameLayout(this);
            track.setBackgroundResource(R.drawable.bg_segment_grey);
            TextView check = Ui.tv(this, "\u2713", 12f, 0xFFFFFFFF, true);
            check.setGravity(android.view.Gravity.CENTER);
            track.addView(check, new android.widget.FrameLayout.LayoutParams(-1, Ui.dp(this, 16)));
            check.setVisibility(View.INVISIBLE);
            LinearLayout.LayoutParams tp2 = new LinearLayout.LayoutParams(-1, Ui.dp(this, 16));
            tp2.setMargins(0, Ui.dp(this, 4), 0, 0);
            cell.addView(track, tp2);
            seg[i] = track; segCheck[i] = check;
            LinearLayout.LayoutParams cl = new LinearLayout.LayoutParams(0, -2, 1f);
            cl.setMargins(Ui.dp(this, 2), 0, Ui.dp(this, 2), 0);
            prog.addView(cell, cl);
        }
        card.addView(prog);

        // info bullets
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setBackgroundResource(R.drawable.bg_box_outline);
        info.setPadding(Ui.dp(this, 14), Ui.dp(this, 10), Ui.dp(this, 14), Ui.dp(this, 10));
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(-1, -2);
        ip.setMargins(0, Ui.dp(this, 12), 0, 0);
        card.addView(info, ip);
        info.addView(bullet("VIN", Session.selectedVin));
        info.addView(bullet("CVN", "47F2DBD1"));
        info.addView(bullet("CAL ID", "U279EBS6V0A3a903"));
        info.addView(bullet("Battery Voltage", String.format("%.6f V", Session.batteryVolts)));
        info.addView(bullet("Flash File", Session.selectedFlashFile));

        final TextView start = Ui.navyBtn(this, "\u26A1  START FLASH");
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(Ui.dp(this, 210), Ui.dp(this, 48));
        sp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        sp.setMargins(0, Ui.dp(this, 14), 0, 0);
        card.addView(start, sp);

        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(0, 0, 0, Ui.dp(this, 10));
        content.addView(card, cp);

        // Logs
        logBox = new LinearLayout(this);
        logBox.setOrientation(LinearLayout.VERTICAL);
        logBox.setBackgroundResource(R.drawable.bg_box_outline);
        logBox.setPadding(Ui.dp(this, 12), Ui.dp(this, 10), Ui.dp(this, 12), Ui.dp(this, 10));
        logBox.addView(Ui.tv(this, "Logs", 13.5f, 0xFF1A2138, true));
        LinearLayout.LayoutParams lgp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 320));
        content.addView(logBox, lgp);

        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (flashing) return;
                if (Session.batteryVolts < 12.0) {
                    toast("Battery below 12 V — connect a charger before flashing");
                    return;
                }
                runFlash();
                start.setTextColor(0xFFAAB2C4);
                start.setBackgroundResource(R.drawable.bg_button_navy_dim);
            }
        });
    }

    private TextView bullet(String k, String v) {
        TextView t = Ui.tv(this, "\u2022  " + k + "        " + v, 13.5f, 0xFF1A2138, false);
        return t;
    }

    private void runFlash() {
        flashing = true;
        UdsLog.log(this, "TX", Session.ecuTx + " -> 1003");
        UdsLog.log(this, "RX", Session.ecuRx + " -> 5003003200C8");
        final int total = SCRIPT.length;
        for (int i = 0; i < total; i++) {
            final int k = i;
            h.postDelayed(new Runnable() {
                public void run() {
                    addLog(SCRIPT[k]);
                    int phase = k < 1 ? 0 : k < 6 ? 1 : k < total - 3 ? 2 : 3;
                    markPhase(phase);
                    if (k == total - 1) flashDone();
                }
            }, 700L * (k + 1) + (k == 6 ? 2600 : 0));
        }
    }

    private void markPhase(int upto) {
        for (int i = 0; i <= upto && i < 4; i++) {
            seg[i].setBackgroundResource(R.drawable.bg_segment_green);
            segCheck[i].setVisibility(View.VISIBLE);
        }
    }

    private void addLog(String msg) {
        TextView line = Ui.tv(this,
                String.format(Locale.US, "%d", logNo++) + "   "
                        + new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date())
                        + "   " + msg,
                12.5f, "Success".equals(msg) ? 0xFF2E9E43 : 0xFF1A2138, false);
        logBox.addView(line);
        UdsLog.log(this, "INFO", msg);
        final ScrollView sc = (ScrollView) findViewById(R.id.scroll);
        if (sc != null) sc.post(new Runnable() { public void run() { sc.fullScroll(View.FOCUS_DOWN); } });
    }

    private void flashDone() {
        flashing = false;
        db.putInput(Session.sessionKey, "flash", Session.selectedEcuCode + "|" + Session.selectedFlashFile, "Success");
        Ui.resultDialog(this, R.drawable.ic_flash_success, "ECU Flashing Completed",
                Session.selectedFlashFile + " programmed to " + Session.selectedEcuCode
                        + " successfully.\nCVN & session evidence stored; do not turn the ignition off for 30 s.",
                "Done", new Runnable() { public void run() { } }).show();
    }
}
