package com.example.tbaduel.notifications;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SenderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);
    }


/*    private class sendMessage extends AsyncTask<URL, String> {

        TextView urlView = findViewById(R.id.editUrl);
        TextView passwordView = findViewById(R.id.editPassword);
        TextView latView = findViewById(R.id.editLat);
        TextView longView = findViewById(R.id.editLong);
        TextView radiusView = findViewById(R.id.editRadius);
        TextView messageView = findViewById(R.id.editMessage);

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                URL url = new URL(urlView.getText().toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("message", messageView.getText().toString());
                jsonParam.put("latitude", latView.getText().toString());
                jsonParam.put("longitude", longView.getText().toString());

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                os.writeBytes(jsonParam.toString());

                os.flush();
                os.close();


                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
    */

}
