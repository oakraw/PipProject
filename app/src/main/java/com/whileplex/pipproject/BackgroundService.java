package com.whileplex.pipproject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by Rawipol on 8/30/15 AD.
 */
public class BackgroundService extends Service {

    public static int update_time = 0;
    private Location currentLocation;
    protected NotificationManager notificationManager;
    private static final int NOTI_ID = 123801;
    private Firebase myFirebaseRef;
    private String userID;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Firebase.setAndroidContext(this);
        userID = intent.getStringExtra(AppController.UIDTAG);

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


        myFirebaseRef = new Firebase("https://whileplex-pip.firebaseio.com/");
        createNotification(0,0, "Updating...");
        SmartLocation.with(getApplication()).location()
                .start(new OnLocationUpdatedListener() {
                           @Override
                           public void onLocationUpdated(Location location) {
                               currentLocation = location;
                               sendData(location.getLatitude(), location.getLongitude());
                               update_time++;
                           }
                       }
                );

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Toast.makeText(getApplication(), "Stop tracking", Toast.LENGTH_LONG).show();
        if(notificationManager != null){
            notificationManager.cancel(NOTI_ID);
        }
        SmartLocation.with(getApplication()).location().stop();
    }

    private void sendData(double lat, double lng){

        if(userID == null){
            userID = AppController.uid;
        }

        if(userID != null) {

            myFirebaseRef.child("users").child(userID).child("latestUpdate").setValue(System.currentTimeMillis() / 1000L);
            myFirebaseRef.child("users").child(userID).child("lat").setValue(lat);
            myFirebaseRef.child("users").child(userID).child("lng").setValue(lng, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if(firebaseError == null)
                        createNotification(currentLocation.getLatitude(), currentLocation.getLongitude(), "Update:"+update_time+" "+"Update successful");
                    else
                        createNotification(currentLocation.getLatitude(), currentLocation.getLongitude(), "Error: please re-login and try again.");

                }


            });

        }
    }

    public void createNotification(double lat, double lng,String message) {
        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        Intent cancelIntent = new Intent(this, CancelButtonListener.class);
        PendingIntent cancelPIntent = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        // Actions are just fake
        Notification noti = new Notification.Builder(this)
                .setContentTitle("Lat:"+lat+" Lng:"+lng)
                .setContentText(message).setSmallIcon(R.drawable.female188)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_MAX)
                .addAction(R.drawable.ic_cancel_white_24dp, "Stop tracking", cancelPIntent)
                .setContentIntent(pIntent).build();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags = Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(NOTI_ID, noti);

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

}

