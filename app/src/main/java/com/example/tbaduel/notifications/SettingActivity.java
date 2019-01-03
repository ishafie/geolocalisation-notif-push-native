package com.example.tbaduel.notifications;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity {

    public static final int GEOLOCATION_REQUEST_CODE = 1;

    /*@Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case GEOLOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                return;
            }
        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        TextView url = findViewById(R.id.editTextURL);
        CheckBox cb = findViewById(R.id.checkBoxNotifs);
        CheckBox filterLocalization = findViewById(R.id.activateFilterLocalization);

        filterLocalization.setOnClickListener((View w) -> {
            if (filterLocalization.isChecked()) {
                System.out.println("CHECKING PERMISSION!");
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GEOLOCATION_REQUEST_CODE);
                    filterLocalization.toggle();
                }
                //check permission
            }
        });
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();

        boolean active = sp.getBoolean("active",false);
        if (active){
            cb.setChecked(true);
        }
        Button btn = findViewById(R.id.btnValidate);
        btn.setOnClickListener((View w)-> {

            editor.putString("url",url.getText().toString());
            editor.putBoolean("active", cb.isChecked());
            editor.putBoolean("filterLocalization", filterLocalization.isChecked());
            Intent i = new Intent();
            System.out.println(sp.getAll());
            editor.apply();
            setResult(RESULT_OK, i);
            finish();
        });
    }




}
