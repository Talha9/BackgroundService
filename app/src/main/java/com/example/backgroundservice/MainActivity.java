package com.example.backgroundservice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String TAG="MyTag";
    TextView txt;
    Button btn,startBtn;
    String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txt=findViewById(R.id.textView);
        btn=findViewById(R.id.btnView);
        startBtn=findViewById(R.id.btnStart);
        BroadcastReceiver();
        String message=getIntent().getStringExtra(LocationService.EXTRA_ACTION);
        if(message!=null){
            Log.d("IntentData",message);
        }
        else {
            Log.d("IntentData","Empty Intent");
        }


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               stopService();

            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService();

            }
        });


    }

    public void startService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        ContextCompat.startForegroundService(this, serviceIntent);

    }
    public void stopService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);
    }





    private void BroadcastReceiver(){
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Get extra data included in the Intent
                String lats = intent.getStringExtra(LocationService.EXTRA_LATITUDE);
                String longs=intent.getStringExtra(LocationService.EXTRA_LONGITUDE);
                txt.setText("Latitude: "+lats+"\n"+"Longitude: "+longs);

            }
        }, new IntentFilter(LocationService.ACTION_LOCATION_BROADCAST));
    }


    }