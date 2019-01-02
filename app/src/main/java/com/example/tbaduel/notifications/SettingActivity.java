package com.example.tbaduel.notifications;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        TextView url = findViewById(R.id.editTextURL);
        CheckBox cb = findViewById(R.id.checkBoxNotifs);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();

        Button btn = findViewById(R.id.btnValidate);
        btn.setOnClickListener((View w)-> {

            editor.putString("url",url.getText().toString());
            editor.putBoolean("active", cb.isChecked());
            Intent i = new Intent();
            System.out.println(sp.getAll());
            editor.apply();
            setResult(RESULT_OK, i);
            finish();
        });
    }




}
