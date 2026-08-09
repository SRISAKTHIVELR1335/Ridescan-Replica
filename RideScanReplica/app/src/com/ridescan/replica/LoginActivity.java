package com.ridescan.replica;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

public class LoginActivity extends BaseActivity {
    private EditText edtEmail, edtPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(0xFFB7C4D0);
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_login);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPin = (EditText) findViewById(R.id.edtPin);

        findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String email = edtEmail.getText().toString().trim();
                String pin = edtPin.getText().toString().trim();
                if (email.length() == 0 || !email.contains("@")) { toast("Enter a valid Email ID"); return; }
                if (pin.length() < 4) { toast("Enter your 4-digit PIN"); return; }
                Session.dealerEmail = email;
                final Dialog d = Ui.progressDialog(LoginActivity.this, "Authenticating…");
                d.show();
                edtEmail.postDelayed(new Runnable() {
                    public void run() { d.dismiss(); go(HomeActivity.class); finish(); }
                }, 900);
            }
        });

        findViewById(R.id.txtForgotPin).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { showForgotPin(); }
        });
    }

    private void showForgotPin() {
        final Dialog d = new Dialog(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_dialog);
        int p = Ui.dp(this, 22);
        root.setPadding(p, p, p, Ui.dp(this, 16));
        root.addView(Ui.tv(this, "Reset PIN", 17, 0xFF141B2E, true));
        root.addView(Ui.tv(this, "An OTP has been sent to your registered email.", 13, 0xFF5A6472, false));

        final EditText otp = new EditText(this);
        otp.setHint("Enter OTP (e.g. 482913)");
        otp.setInputType(InputType.TYPE_CLASS_NUMBER);
        otp.setBackgroundResource(R.drawable.bg_edittext);
        otp.setPadding(Ui.dp(this, 12), 0, Ui.dp(this, 12), 0);
        LinearLayout.LayoutParams op = new LinearLayout.LayoutParams(-1, Ui.dp(this, 46));
        op.setMargins(0, Ui.dp(this, 14), 0, 0);
        root.addView(otp, op);

        final EditText np = new EditText(this);
        np.setHint("New 4-Digit PIN");
        np.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        np.setBackgroundResource(R.drawable.bg_edittext);
        np.setPadding(Ui.dp(this, 12), 0, Ui.dp(this, 12), 0);
        LinearLayout.LayoutParams npp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 46));
        npp.setMargins(0, Ui.dp(this, 10), 0, 0);
        root.addView(np, npp);

        android.widget.Button b = new android.widget.Button(this);
        b.setText("Reset PIN");
        b.setTextColor(0xFFFFFFFF);
        b.setBackgroundResource(R.drawable.bg_button_blue);
        b.setAllCaps(false);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 46));
        bp.setMargins(0, Ui.dp(this, 16), 0, 0);
        root.addView(b, bp);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (otp.getText().toString().trim().length() < 6) { toast("Enter the 6-digit OTP"); return; }
                if (np.getText().toString().trim().length() < 4) { toast("PIN must be 4 digits"); return; }
                d.dismiss();
                toast("PIN reset successful");
            }
        });
        d.setContentView(root);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            d.getWindow().setLayout(Ui.dp(this, 305), android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        d.show();
    }
}
