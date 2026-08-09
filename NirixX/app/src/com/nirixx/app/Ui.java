package com.nirixx.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/** Programmatic UI builders (cards, rows, chips, dialogs) for a consistent look. */
public final class Ui {
    private Ui() {}

    public static int dp(Context c, float v) {
        return (int) (v * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static TextView tv(Context c, String text, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(text);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    public static TextView chip(Context c, String text, int bgRes, int textColor) {
        TextView t = tv(c, text, 11.5f, textColor, true);
        t.setBackgroundResource(bgRes);
        int h = dp(c, 10), v = dp(c, 4);
        t.setPadding(h, v, h, v);
        return t;
    }

    public static LinearLayout card(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackgroundResource(R.drawable.bg_card);
        int p = dp(c, 16);
        l.setPadding(p, p, p, p);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(c, 12));
        l.setLayoutParams(lp);
        return l;
    }

    /** A row: leading icon + title + subtitle + trailing chevron. */
    public static LinearLayout listRow(Context c, int iconRes, String title, String sub, boolean chevron) {
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.bg_card);
        int p = dp(c, 14);
        row.setPadding(p, p, p, p);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(c, 10));
        row.setLayoutParams(lp);

        if (iconRes != 0) {
            ImageView iv = new ImageView(c);
            iv.setImageResource(iconRes);
            int s = dp(c, 34);
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(s, s);
            ip.setMargins(0, 0, dp(c, 14), 0);
            row.addView(iv, ip);
        }
        LinearLayout mid = new LinearLayout(c);
        mid.setOrientation(LinearLayout.VERTICAL);
        mid.addView(tv(c, title, 14.5f, 0xFF141B2E, true));
        if (sub != null && sub.length() > 0) {
            TextView s = tv(c, sub, 12f, 0xFF5A6472, false);
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-2, -2);
            sp.setMargins(0, dp(c, 2), 0, 0);
            mid.addView(s, sp);
        }
        row.addView(mid, new LinearLayout.LayoutParams(0, -2, 1f));
        if (chevron) {
            ImageView cv = new ImageView(c);
            cv.setImageResource(R.drawable.ic_chev);
            row.addView(cv, new LinearLayout.LayoutParams(dp(c, 22), dp(c, 22)));
        }
        return row;
    }

    /** Key / value row used inside cards. */
    public static LinearLayout kvRow(Context c, String k, String v, boolean last) {
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int pv = dp(c, 9);
        row.setPadding(0, pv, 0, pv);
        if (!last) {
            GradientDrawable div = new GradientDrawable();
            div.setColor(0x00000000);
            row.setPadding(0, pv, 0, pv);
        }
        row.addView(tv(c, k, 13f, 0xFF5A6472, false),
                new LinearLayout.LayoutParams(0, -2, 1.1f));
        TextView val = tv(c, v, 13f, 0xFF141B2E, true);
        val.setGravity(Gravity.RIGHT);
        row.addView(val, new LinearLayout.LayoutParams(0, -2, 0.9f));
        return row;
    }

    /** Section header label. */
    public static TextView section(Context c, String text) {
        TextView t = tv(c, text, 12f, 0xFF5A6472, true);
        t.setLetterSpacing(0.08f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMargins(dp(c, 4), dp(c, 4), 0, dp(c, 8));
        t.setLayoutParams(lp);
        return t;
    }

    public static Drawable roundRect(int color, float radiusDp, Context c) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, radiusDp));
        return g;
    }

    /** Simple card-style dialog matching the app's modals. */
    public static Dialog dialog(final Activity a, String title, CharSequence body,
                                String primary, final Runnable onPrimary,
                                String secondary, final Runnable onSecondary) {
        final Dialog d = new Dialog(a);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout root = new LinearLayout(a);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_dialog);
        int p = dp(a, 22);
        root.setPadding(p, p, p, dp(a, 16));
        root.addView(tv(a, title, 17f, 0xFF141B2E, true));
        if (body != null && body.length() > 0) {
            TextView b = tv(a, body.toString(), 13.5f, 0xFF5A6472, false);
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2);
            bp.setMargins(0, dp(a, 10), 0, 0);
            b.setLineSpacing(1.2f, 1f);
            root.addView(b, bp);
        }
        LinearLayout btnRow = new LinearLayout(a);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.RIGHT);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, -2);
        rp.setMargins(0, dp(a, 18), 0, 0);

        if (secondary != null) {
            TextView s = tv(a, secondary, 14f, 0xFF5A6472, true);
            s.setPadding(dp(a, 16), dp(a, 8), dp(a, 16), dp(a, 8));
            s.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { d.dismiss(); if (onSecondary != null) onSecondary.run(); }
            });
            btnRow.addView(s);
        }
        TextView pv = tv(a, primary, 14f, 0xFF0B8376, true);
        pv.setPadding(dp(a, 16), dp(a, 8), dp(a, 16), dp(a, 8));
        pv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { d.dismiss(); if (onPrimary != null) onPrimary.run(); }
        });
        btnRow.addView(pv);
        root.addView(btnRow, rp);
        d.setContentView(root);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            d.getWindow().setLayout(dp(a, 300), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return d;
    }

    /** Loading/progress dialog (dialog_erase_loader style). */
    public static Dialog progressDialog(Context c, String title) {
        Dialog d = new Dialog(c);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout root = new LinearLayout(c);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundResource(R.drawable.bg_dialog);
        int p = dp(c, 24);
        root.setPadding(p, p, p, p);
        ProgressBar pb = new ProgressBar(c);
        root.addView(pb, new LinearLayout.LayoutParams(dp(c, 44), dp(c, 44)));
        TextView t = tv(c, title, 14f, 0xFF141B2E, true);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-2, -2);
        tp.setMargins(0, dp(c, 14), 0, 0);
        root.addView(t, tp);
        d.setContentView(root);
        d.setCancelable(false);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return d;
    }

    /** Success/failure dialog with icon (ic_flash_success style). */
    public static Dialog resultDialog(final Activity a, int iconRes, String title, String body,
                                      String primary, final Runnable onPrimary) {
        final Dialog d = new Dialog(a);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout root = new LinearLayout(a);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundResource(R.drawable.bg_dialog);
        int p = dp(a, 24);
        root.setPadding(p, p, p, dp(a, 18));
        ImageView iv = new ImageView(a);
        iv.setImageResource(iconRes);
        root.addView(iv, new LinearLayout.LayoutParams(dp(a, 52), dp(a, 52)));
        TextView t = tv(a, title, 17f, 0xFF141B2E, true);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-2, -2);
        tp.setMargins(0, dp(a, 12), 0, 0);
        root.addView(t, tp);
        if (body != null) {
            TextView b = tv(a, body, 13f, 0xFF5A6472, false);
            b.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2);
            bp.setMargins(0, dp(a, 6), 0, 0);
            root.addView(b, bp);
        }
        TextView pv = tv(a, primary, 14f, 0xFF0B8376, true);
        pv.setPadding(dp(a, 16), dp(a, 10), dp(a, 16), dp(a, 10));
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-2, -2);
        pp.setMargins(0, dp(a, 14), 0, 0);
        root.addView(pv, pp);
        pv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { d.dismiss(); if (onPrimary != null) onPrimary.run(); }
        });
        d.setContentView(root);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            d.getWindow().setLayout(dp(a, 300), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return d;
    }

    public static FrameLayout divider(Context c) {
        FrameLayout f = new FrameLayout(c);
        f.setBackgroundResource(R.color.line_grey);
        return f;
    }
}
