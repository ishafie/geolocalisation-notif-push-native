package com.example.tbaduel.notifications;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MessageNotificationService extends Service {
    public static final String START_WATCH_ACTION = "MessageNotificationService.START_WATCHING";
    public static final String STOP_WATCH_ACTION = "MessageNotificationService.STOP_WATCHING";
    OkHttpClient client = new OkHttpClient();
    WebSocket ws;
    private Handler handler;
    private int notificationId = 0;
    private boolean networkServiceStarted = false;

    @Override
    public void onCreate() {
        this.handler = new Handler();
    }

    public static void start(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean activated = prefs.getBoolean("active", false);
        String url = prefs.getString("url", "none");
        if (activated && url != "none") {
            Intent i = new Intent(context, MessageNotificationService.class);
            i.setAction(MessageNotificationService.START_WATCH_ACTION);
            i.putExtra("url", url);
            context.startService(i);
        }

    }

    public static void stop(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean activated = prefs.getBoolean("active", false);
        System.out.println("activated for stop: " + activated);
        if (!activated) {
            Intent i = new Intent(context, MessageNotificationService.class);
            i.setAction(MessageNotificationService.STOP_WATCH_ACTION);
            context.startService(i);
        }
    }

    private void registerMyReceiver() {
        if (!networkServiceStarted) {
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (cm != null) {
                        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                        System.out.println("CHANGE NETWORK ISCONNECTED:" + isConnected);
                        if (isConnected) {
                            networkServiceStarted = true;
                            MessageNotificationService.start(context);
                        } else {
                            MessageNotificationService.stop(context);
                        }
                    } else {
                        networkServiceStarted = false;
                    }
                }
            }, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int id){
        this.registerMyReceiver();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        switch(intent.getAction()){
            case START_WATCH_ACTION :
                System.out.println("STARTED WATCH ACTION ON: " + sp.getString("url", "none"));
                this.startWatching(sp.getString("url", "undefined"));
                break;
            case STOP_WATCH_ACTION:
                this.ws.close(1000, "Closing...");
                break;
        }

        return Service.START_NOT_STICKY;
    }

    public void startWatching(String url) {
        // the URL must start with ws:// or wss:// (and not http:// or https://)
        Request request = new Request.Builder().url(url).build();
        MessageListener listener = new MessageListener(this);
        ws = client.newWebSocket(request, listener);
    }

    public void makeToast(String text) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class MessageListener extends WebSocketListener
    {
        MessageNotificationService service;

        public MessageListener(MessageNotificationService service) {
            this.service = service;
        }

        public void onOpen(WebSocket webSocket, Response response) {
            System.out.println("onOpen");
            webSocket.send("pushSince:" + System.currentTimeMillis() / 1000);
        }



        public void onMessage(WebSocket webSocket, String text) {
            System.out.println("onmessage text");
            System.out.println("MESSAGE: " + text);
            handler.post(() -> {
                try {
                    JSONObject jsonText = new JSONObject(text);
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MessageNotificationService.this, MainActivity.CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle("Notification app")
                            .setContentText(jsonText.getString("content"))
                                    .setPriority(NotificationCompat.PRIORITY_HIGH);
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MessageNotificationService.this);
                    notificationManager.notify(notificationId, mBuilder.build());
                    makeToast(jsonText.getString("content"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            });

        }

        public void onMessage(WebSocket webSocket, ByteString bytes) {

            System.out.println("onmessage");
        }

        public void onClosing(WebSocket webSocket, int code, String reason) {
            System.out.println("onclosing");

            webSocket.close(code, reason);
        }

        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            System.out.println("onfailure");

            webSocket.cancel();
            t.printStackTrace();
        }
    }
}
