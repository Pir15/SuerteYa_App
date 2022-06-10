package com.example.suerteyachat.Firebase;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.suerteyachat.MainActivity;
import com.example.suerteyachat.R;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    String CHANNEL_ID="NOTIFICACION";
    int NOTIFICACION_IO=0;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.trisqueta_not)
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(alarmSound)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIFICACION_IO, builder.build());
    }

    @Override
    public void onNewToken(@NonNull String s) {
    }
}
