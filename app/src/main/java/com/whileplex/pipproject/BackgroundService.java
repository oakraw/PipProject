package com.whileplex.pipproject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;


import java.util.Calendar;
import java.util.Date;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by Rawipol on 8/30/15 AD.
 */
public class BackgroundService extends Service implements LocationListener{

    private static int updateTime = 1;
    protected NotificationManager notificationManager;
    private static final int NOTI_ID = 123801;
    private String userID;
    private LocationManager locationManager;
    private String provider;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(this, "Start tracking", Toast.LENGTH_SHORT).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(provider, 5000, 1, this);


        createNotification(0, 0, "Updating...");
        if (location != null) {
            onLocationChanged(location);
        }
    }

    private String getTime(){
        Date date = Calendar.getInstance().getTime();
        return date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplication(), "Stop tracking", Toast.LENGTH_LONG).show();
        if (notificationManager != null) {
            notificationManager.cancel(NOTI_ID);
        }
        locationManager.removeUpdates(this);
    }

    public void createNotification(double lat, double lng, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        Intent cancelIntent = new Intent(this, CancelButtonListener.class);
        PendingIntent cancelPIntent = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification noti = new Notification.Builder(this)
                .setContentTitle("Lat:" + lat + " Lng:" + lng)
                .setContentText(message)
                .setSmallIcon(R.drawable.female188)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_MAX)
                .addAction(R.drawable.ic_cancel_white_24dp, "Stop tracking", cancelPIntent)
                .setContentIntent(pIntent).build();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        noti.flags = Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(NOTI_ID, noti);

    }

    @Override
    public void onLocationChanged(Location location) {
        updateTime += 1;
        createNotification(location.getLatitude(), location.getLongitude(), "Updated at " + getTime());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public static class CancelButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTI_ID);
            Intent stopIntent = new Intent(context,
                    BackgroundService.class);
            context.stopService(stopIntent);
        }
    }

    public static int getUpdateTime() {
        return updateTime;
    }
}

