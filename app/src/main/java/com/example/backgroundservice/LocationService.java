package com.example.backgroundservice;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import org.greenrobot.eventbus.EventBus;

import java.security.KeyStore;
import java.util.Random;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class LocationService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static final String TAG = "MyTag";
    LocationRequest request;
    FusedLocationProviderClient client;
    Location userLoc;
    Location loc;
    Notification notification;
    String ACTION_STOP_SERVICE = "STOP";
    public static final String ACTION_LOCATION_BROADCAST = LocationService.class.getName() + "LocationBroadcast";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    public static final String EXTRA_ACTION = "extra_action";


    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        userLoc = new Location("service Provider");
        client=new FusedLocationProviderClient(this);



//        listener = new LocationListener() {
//            @Override
//            public void onLocationChanged(@NonNull Location location) {
//
//                userLoc.setLatitude(location.getLatitude());
//                userLoc.setLongitude(location.getLongitude());
//            }
//
//        };
//        manager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
//        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, listener);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
            stopSelf();
        }

        LocationPermission();




        return START_NOT_STICKY;
    }





    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void LocationPermission() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


        request = new LocationRequest();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(5000);
        request.setFastestInterval(5000);
        client.requestLocationUpdates(request, new LocationCallback() {
            @SuppressLint("WrongConstant")
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                loc=locationResult.getLastLocation();
                Log.d(TAG,loc.toString());
                userLoc.setLatitude(loc.getLatitude());
                userLoc.setLongitude(loc.getLongitude());

                createNotificationChannel();
                Intent stopSelf = new Intent(LocationService.this, LocationService.class);
                stopSelf.setAction(ACTION_STOP_SERVICE);
                PendingIntent pStopSelf = PendingIntent.getService(LocationService.this, 0, stopSelf
                        , PendingIntent.FLAG_CANCEL_CURRENT);
                notification= new NotificationCompat.Builder(LocationService.this, CHANNEL_ID)
                        .setContentTitle("Foreground Service")
                        .setContentText("Latitude: "+userLoc.getLatitude()+"\n"+"Longitude: "+userLoc.getLongitude())
                        .setSmallIcon(R.drawable.ic_android_black_24dp)
                        .addAction(R.drawable.ic_android_black_24dp,"Close",pStopSelf)
                        .setOnlyAlertOnce(true)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .setOngoing(true)
                        .build();
                startForeground(1, notification);


              //  Toast.makeText(LocationService.this, userLoc.getLatitude()+"::"+userLoc.getLongitude(), Toast.LENGTH_SHORT).show();

                sendMessageToUI(String.valueOf(userLoc.getLatitude()),String.valueOf(userLoc.getLongitude()));
            }
        },Looper.myLooper());


    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public void fun(){
        Toast.makeText(this, "Hello Pakistan", Toast.LENGTH_SHORT).show();
    }

    private void sendMessageToUI(String lat, String lng) {

        Log.d(TAG, "Sending info...");

        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_LATITUDE, lat);
        intent.putExtra(EXTRA_LONGITUDE, lng);
       // Toast.makeText(this, lat+"::"+lng, Toast.LENGTH_SHORT).show();
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);





    }


}
