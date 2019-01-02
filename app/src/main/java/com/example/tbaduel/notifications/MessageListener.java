package com.example.tbaduel.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static android.provider.Settings.Global.getString;

/**
 * Created by tbaduel on 30/11/18.
 */

