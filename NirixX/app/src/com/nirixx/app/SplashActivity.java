package com.nirixx.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends BaseActivity {

    private SharedPreferences crash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(0xFF000000);
        setContentView(R.layout.activity_splash);

        // If the previous run died, show the exact trace instead of the onboarding flow —
        // this is how a device-only failure gets reported back with proof.
        crash = getSharedPreferences(NirixXApp.PREFS, MODE_PRIVATE);
        String trace = crash.getString("trace", null);
        if (trace != null) {
            String show = trace.length() > 1400 ? trace.substring(0, 1400) + "\n…(truncated)" : trace;
            Ui.dialog(this, "NirixX stopped unexpectedly",
                    "The app closed with an error last time. Please share this trace when reporting:\n\n" + show,
                    "Clear & open", new Runnable() {
                        public void run() {
                            crash.edit().remove("trace").apply();
                            advance();
                        }
                    }, null, null).show();
            return;
        }
        advance();
    }

    private void advance() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                boolean seen = getSharedPreferences("nirixx", MODE_PRIVATE).getBoolean("tutorial_seen", false);
                startActivity(new Intent(SplashActivity.this,
                        seen ? WelcomeActivity.class : TutorialActivity.class));
                finish();
            }
        }, 1600);
    }
}
