package org.idnp.boundserviceserver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Messenger mMessenger;
    private Messenger replyMessenger = new Messenger(new HandlerReplyMsg());
    private TimeBoundService mathService;
    boolean bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(getApplicationContext(), TimeBoundService.class);
        intent.setAction("bindservice");
        startService(intent);
//        Intent ssmIntent = new Intent().setClassName("org.idnp.boundserviceserver",
//                "org.idnp.boundserviceserver.TimeBoundService");
        Intent ssmIntent = new Intent(getApplicationContext(), TimeBoundService.class);
        bindService(ssmIntent, connection2, Context.BIND_AUTO_CREATE);
        //bindService(intent, connection, getApplicationContext().BIND_AUTO_CREATE);

        Button btnService = findViewById(R.id.btnService);
        Button btnForeground = findViewById(R.id.btnForeground);
        Button btnStopForeground = findViewById(R.id.btnStopForeground);
        Button btnGetTime = findViewById(R.id.btnGetTime);

        btnService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TimeBoundService.class);
                intent.setAction(TimeBoundService.START_SERVICE);
                startService(intent);
            }
        });

        btnForeground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TimeBoundService.class);
                intent.setAction(TimeBoundService.START_FOREGROUND);
                startService(intent);
            }
        });

        btnStopForeground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TimeBoundService.class);
                intent.setAction(TimeBoundService.STOP_FOREGROUND);
                startService(intent);
            }
        });

        btnGetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!bound) return;
                // Create and send a message to the service, using a supported 'what' value

                try {
                    Message msg = Message.obtain(null, 1);
                    Bundle bundle = new Bundle();
                    bundle.putString("HELLO_CLIENT", "Hola, soy el cliente. Que hora es en Sydney?");
                    msg.setData(bundle);
                    msg.replyTo = replyMessenger;
                    mMessenger.send(msg);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            TimeBoundService.LocalBinder binder = (TimeBoundService.LocalBinder) service;
            mathService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    private ServiceConnection connection2 = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mMessenger = new Messenger(service);
            bound = true;
            Log.d(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mMessenger = null;
            bound = false;
            Log.d(TAG, "onServiceDisconnected");
        }
    };

    class HandlerReplyMsg extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String data = msg.getData().getString("SERVER_HELLO");
            //String recdMessage = "msg.obj.toString()";
            Log.d(TAG, "Client> " + data);


        }
    }

    ;


}