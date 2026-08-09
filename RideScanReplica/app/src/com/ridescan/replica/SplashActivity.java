package com.ridescan.replica;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(0xFF000000);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                boolean seen = getSharedPreferences("ridescan", MODE_PRIVATE).getBoolean("tutorial_seen", false);
                startActivity(new Intent(SplashActivity.this,
                        seen ? WelcomeActivity.class : TutorialActivity.class));
                finish();
            }
        }, 1600);
    }
}
