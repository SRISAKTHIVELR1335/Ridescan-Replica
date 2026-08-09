package com.nirixx.app;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class VinDiagnosisActivity extends BaseActivity {
    protected boolean forFlashing() { return false; }

    private EditText edtVin;
    private View vehicleCard;
    private final Handler h = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vin);
        setTitle(forFlashing() ? "VIN Based Flashing" : "VIN Based Diagnosis");
        wireBack();

        edtVin = (EditText) findViewById(R.id.edtVin);
        vehicleCard = findViewById(R.id.vehicleCard);
        edtVin.setText(Session.selectedVin);

        findViewById(R.id.btnScanVin).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Ui.dialog(VinDiagnosisActivity.this, "Scan VIN",
                        "Barcode scanner requires camera permission in the replica build.\n\nUse a sample VIN instead?",
                        "Use Sample", new Runnable() {
                            public void run() { edtVin.setText("MD634NF4XRCL12345"); }
                        }, "Cancel", null).show();
            }
        });

        findViewById(R.id.btnGetDetails).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String vin = edtVin.getText().toString().trim();
                if (vin.length() < 17) { toast("VIN must be 17 characters"); return; }
                Session.selectedVin = vin;
                final Dialog d = Ui.progressDialog(VinDiagnosisActivity.this, "Identifying vehicle…");
                d.show();
                h.postDelayed(new Runnable() {
                    public void run() { d.dismiss(); showVehicle(); }
                }, 1000);
            }
        });

        findViewById(R.id.btnProceed).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                go(forFlashing() ? SelectFlashVariantActivity.class : SelectECUActivity.class);
            }
        });
    }

    private void showVehicle() {
        TextView name = (TextView) findViewById(R.id.txtVehName);
        TextView meta = (TextView) findViewById(R.id.txtVehMeta);
        name.setText(Session.selectedVehicle);
        meta.setText("VIN: " + Session.selectedVin
                + "\nVariant: " + Session.selectedVariant
                + "\nEngine No: R2CN4X118893\nNorm: BSVI\nECU: Sedemac EMS (UDS)");
        vehicleCard.setVisibility(View.VISIBLE);
    }
}
