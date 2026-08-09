package com.ridescan.replica;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public abstract class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setTitle(String title) {
        TextView t = (TextView) findViewById(R.id.tvTitle);
        if (t != null) t.setText(title);
    }

    protected void wireBack() {
        View b = findViewById(R.id.btnBack);
        if (b != null) {
            b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { onBackPressed(); }
            });
        }
    }

    protected void go(Class<?> target) {
        startActivity(new Intent(this, target));
    }

    protected void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
