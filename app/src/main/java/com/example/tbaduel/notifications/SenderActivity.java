package com.example.tbaduel.notifications;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

public class SenderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);


        Button btnSend = findViewById(R.id.editSend);
        btnSend.setOnClickListener((View w) -> {
            SendMessage message = new SendMessage();
            message.execute();
        });
    }


    private class SendMessage extends AsyncTask<Void, Integer, Void> {

        TextView urlView = findViewById(R.id.editUrl);
        TextView passwordView = findViewById(R.id.editPassword);
        TextView latView = findViewById(R.id.editLat);
        TextView longView = findViewById(R.id.editLong);
        TextView radiusView = findViewById(R.id.editRadius);
        TextView messageView = findViewById(R.id.editMessage);



        @Override
        protected Void doInBackground(Void ... args) {
            try {
                System.out.println("Prepare sending ...");
                URL url = new URL(urlView.getText().toString()
                    + "?id=" + String.valueOf(354354)
                    + "&latitude=" + latView.getText().toString()
                    + "&longitude=" + longView.getText().toString()
                    + "&radius=" + radiusView.getText().toString()

                );
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("X-Password", passwordView.getText().toString());
                conn.setDoOutput(true);
                conn.setDoInput(true);

                /*JSONObject jsonParam = new JSONObject();
                conn.addRequestProperty("timestamp",String.valueOf(System.currentTimeMillis()));
                conn.addRequestProperty("content", messageView.getText().toString());
                conn.addRequestProperty("id",String.valueOf(354354));
                conn.addRequestProperty("password", passwordView.getText().toString());
                conn.addRequestProperty("latitude", latView.getText().toString());
                conn.addRequestProperty("longitude", longView.getText().toString());
                conn.addRequestProperty("radius", radiusView.getText().toString());

                */

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.write(messageView.getText().toString().getBytes("UTF-8"));
                //os.writeBytes(jsonParam.toString());
                //System.out.println("sending : " + jsonParam.toString());

               // os.flush();
               // os.close();

                Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                Log.i("MSG" , conn.getResponseMessage());

                conn.disconnect();
                System.out.println("Send and disconnected");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }





}
