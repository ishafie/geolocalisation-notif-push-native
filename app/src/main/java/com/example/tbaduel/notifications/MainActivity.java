package com.example.tbaduel.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static android.provider.Settings.Global.getString;

public class MainActivity extends AppCompatActivity {

    public static final String CHANNEL_ID = "1234";

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "NotificationChannel", importance);
            channel.setDescription("Channel of Notification app");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager == null) return ;
            notificationManager.createNotificationChannel(channel);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // /usr/local/apps/alternatives/android-sdk/android-sdk/platform-tools
        // adb reverse tcp:1818 tcp:1818
        // pour proxy
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
        Intent serviceNotification = new Intent(this, MessageNotificationService.class);
        serviceNotification.setAction("MessageNotificationService.STOP_WATCHING");

        Button btnSetting = findViewById(R.id.btnReturn);
        btnSetting.setOnClickListener((View w)-> {
            Intent i = new Intent(this, SettingActivity.class);
            startActivityForResult(i,1);
        });

        Button btnSendActivity = findViewById(R.id.btnSendActivity);
        btnSendActivity.setOnClickListener((View w) ->{
          Intent i = new Intent(this, SenderActivity.class);
          startActivityForResult(i,1);
        });

        Intent i = new Intent(this, SettingActivity.class);
        startActivityForResult(i,1);

        sp.registerOnSharedPreferenceChangeListener((SharedPreferences sharedPreferences, String key)-> {
            System.out.println("key: " + key);
            System.out.println(sharedPreferences.getAll());
            if (sharedPreferences.contains("active") && sharedPreferences.contains("url")
                    && sharedPreferences.getBoolean("active", false) && !serviceNotification.getAction().equals("MessageNotificationService.START_WATCHING")) {
                System.out.println("MAIN ACTIVITY: starting service");
                serviceNotification.setAction(MessageNotificationService.START_WATCH_ACTION);
                startService(serviceNotification);
            }
            if (sharedPreferences.contains("active") && sharedPreferences.contains("url")
                    && !sharedPreferences.getBoolean("active", false) && serviceNotification.getAction().equals("MessageNotificationService.START_WATCHING")) {
                serviceNotification.setAction(MessageNotificationService.STOP_WATCH_ACTION);
                startService(serviceNotification);
            }
        });
    }

    public void setText(String text) {
        TextView notifText = findViewById(R.id.notifText);
        notifText.setText(text);
    }



}
