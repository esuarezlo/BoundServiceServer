package org.idnp.boundserviceserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeBoundService extends Service {
    private final static int NOTIFICATION_ID = 123456;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static final String START_SERVICE = "START_SERVICE";
    public static final String START_FOREGROUND = "START_FOREGROUND";
    public static final String STOP_FOREGROUND = "STOP_FOREGROUND";
    private static final String TAG = "MathBoundService";

    private final IBinder binder = new LocalBinder();
    private static Messenger mMessenger;
    private Context context;

    @Override
    public void onCreate() {
        context = getApplicationContext();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent.getAction().equals(START_SERVICE)) {

        } else if (intent.getAction().equals(START_FOREGROUND)) {
            startForegroundService();
        } else if (intent.getAction().equals(STOP_FOREGROUND)) {
            stopForegroundService();
        }

        return START_REDELIVER_INTENT;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //return binder;
        mMessenger = new Messenger(new IncomingHandler(context));
        return mMessenger.getBinder();

    }


    public String getTimeSydney() {
        String fromTimeZone = "UTC+10";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        format.setTimeZone(TimeZone.getTimeZone(fromTimeZone));
        String sydneyDate = format.format(date);

        return sydneyDate;
    }

    private void startForegroundService() {

        createNotificationChannel();
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);

        builder.setContentTitle("BoundServiceServer is running")
                .setContentText("Touch for open the application")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSound(null)
                .setAutoCancel(true)
                .build();

        Notification notification = builder.build();

        startForeground(NOTIFICATION_ID, notification);//iniciar el servicio como ForegroundService
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void stopForegroundService() {
        stopForeground(true);
        stopSelf();
    }

    public class LocalBinder extends Binder {
        TimeBoundService getService() {
            // Return this instance of LocalService so clients can call public methods
            return TimeBoundService.this;
        }
    }

    ;

    /**
     * Command to the service to display a message
     */
    static final int MSG_SAY_HELLO = 1;

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        private static final String TAG = "IncomingHandler";
        private Context applicationContext;


        IncomingHandler(Context context) {
            applicationContext = context.getApplicationContext();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    String msje = msg.getData().getString("HELLO_CLIENT");
                    Log.d(TAG, "Server> " + msje);

                    Message message = Message.obtain(null, 2);
                    Bundle bundle = new Bundle();
                    bundle.putString("SERVER_HELLO", "Hola soy el servidor. En sydney es " + getTimeSydney());
                    message.setData(bundle);
                    try {
                        Messenger messenger = msg.replyTo;
                        if (messenger != null) {
                            messenger.send(message);
                        } else
                            Log.d(TAG, "Server> null replyto");

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


}
