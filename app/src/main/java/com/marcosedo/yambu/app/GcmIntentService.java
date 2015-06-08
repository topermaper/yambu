package com.marcosedo.yambu.app;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.marcosedo.yambu.R;


public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = "GCM intent service";
    private NotificationManager mNotificationManager;

    public static final String ACTION_SENDCOMMENT = "com.marcosedo.yambu.app.GcmIntentService.action.SENDCOMMENT";
    public static final String EXTRA_COMMENT = "EXTRA_COMMENT";
    //NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent");

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if ((extras != null) && (!extras.isEmpty())) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString(), null);
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString(), null);
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                Log.i(TAG, "Received: " + extras.toString());

                String mode = extras.getString("mode");

                switch (mode) {
                    case "new_comment":
                        Comment comment = new Comment(Base64.decode(extras.getString("image"), Base64.DEFAULT), extras.getString("msg"),
                                extras.getString("username"), extras.getString("timestamp"));
                        Intent intentSendComment = new Intent();
                        intentSendComment.setAction(ACTION_SENDCOMMENT);
                        intentSendComment.addCategory(Intent.CATEGORY_DEFAULT);
                        intentSendComment.putExtra(EXTRA_COMMENT,comment);
                        sendBroadcast(intentSendComment);
                        break;
                    case "send_notification":
                        sendNotification(extras.getString("texto"), extras.getString("idevento"));//si no hay idevento se envia null
                        break;
                }
            }

        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message. 
    private void sendNotification(String msg, String idevento) {

        byte[] cartel = null;

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("msg", msg);

        if (idevento != null) intent.putExtra("idevento", idevento);

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.yambu90)
                .setContentTitle("YAMBÚ")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(msg))
                .setContentText(msg)
                .setTicker("Nueva notificación de YAMBÚ").setAutoCancel(true);

        // Uso en API 11 o mayor Patrón de vibración: 1 segundo vibra, 0.5 segundos para, 1 segundo vibra
        long[] pattern = new long[]{10, 60, 70, 500};
        mBuilder.setVibrate(pattern);

        mBuilder.setContentIntent(contentIntent);

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());//enviamos la notificacion
    }
}