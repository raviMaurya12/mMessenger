package com.example.lenovo.mmessenger;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import java.io.Console;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG ="message2" ;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);



        String notTitle=remoteMessage.getNotification().getTitle();
        String notText=remoteMessage.getNotification().getBody();
        String from_user_id=remoteMessage.getData().get("FromUserId");
        String click_action=remoteMessage.getNotification().getClickAction();


        Log.w(TAG, "The from_user_id before passing is : "+from_user_id);
        Log.w(TAG, "The click_action before passing is : "+click_action);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(notTitle)
                .setContentText(notText);

        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("from_user_id",from_user_id);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int notificationId=(int)System.currentTimeMillis();

        notificationManager.notify(notificationId, mBuilder.build());


    }
}
