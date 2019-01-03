package com.example.tbaduel.notifications;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
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
    public static final int LOCATION_TIMEOUT = 20000;
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



        public void getLocation(String currentMessage) {
            handler.post(() -> {
                System.out.println("getLocation start");
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MessageNotificationService.this);
                if (sharedPreferences.contains("filterLocalization") && sharedPreferences.getBoolean("filterLocalization", false) && ContextCompat.checkSelfPermission(MessageNotificationService.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Passed authorization");
                    LocationManager locationManager = (LocationManager)MessageNotificationService.this.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                    //Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // no effort since we use an already know location
                    // getLastKnownLocation is not precise enough, it could register a location from more than 10 hours ago.

                    /*if (loc != null) {
                        treatMessage(currentMessage, loc);
                        return ;
                    }*/
                    final boolean[] treatedMessage = {false};
                    // we must ask Android to watch for the location
                    LocationListener listener = new LocationListener() {

                        @Override
                        public void onLocationChanged(Location location) {
                            System.out.println("Location changed!!");
                            if (! treatedMessage[0]) { // if the message has not already been treated
                                treatMessage(currentMessage, location);
                                treatedMessage[0] = true;
                            }
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                            System.out.println("status changed");
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                            System.out.println("provider enabled");
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                            System.out.println("provider disabled");
                        }
                    };

                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null /* using main thread */);
                    handler.postDelayed( () -> {
                        System.out.println("DELAYED 1");
                        if (! treatedMessage[0]) {
                            System.out.println("NO TREATED MESSAGE");
                            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, null /* using main thread */);
                            System.out.println("STARTING NETWORK PROVIDER");
                            handler.postDelayed( () -> {
                                if (!treatedMessage[0])
                                    treatMessage(currentMessage, null);
                                treatedMessage[0] = true;
                            }, LOCATION_TIMEOUT);
                        }
                        },  LOCATION_TIMEOUT);

                }
            });
        }

        public void treatMessage(String text, Location loc) {
            System.out.println("treatMessage text");

            String sentFrom;
            if (loc != null)
                sentFrom = "Sent from: Lat(" + loc.getLatitude() + "), Lon(" + loc.getLongitude() + "):\n";
            else
                sentFrom = "";
            handler.post(() -> {
                try {
                    JSONObject jsonText = new JSONObject(text);
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MessageNotificationService.this, MainActivity.CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle("Notification app")
                            .setContentText(sentFrom + jsonText.getString("content"))
                            .setPriority(NotificationCompat.PRIORITY_HIGH);
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MessageNotificationService.this);
                    notificationManager.notify(notificationId, mBuilder.build());
                    makeToast(sentFrom + jsonText.getString("content"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            });
        }

        public void onMessage(WebSocket webSocket, String text) {
            System.out.println("onmessage text");
            System.out.println("MESSAGE: " + text);
            getLocation(text);
            /*handler.post(() -> {
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

            });*/

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
