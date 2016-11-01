package com.whileplex.pipproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, LocationListener {

    private TextView latTv;
    private TextView lngTv;
    private TextView updateTime;
    private Button startBtn;
    private LocationManager locationManager;
    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latTv = (TextView) findViewById(R.id.lat_tv);
        lngTv = (TextView) findViewById(R.id.lng_tv);
        updateTime = (TextView) findViewById(R.id.update_time);
        startBtn = (Button) findViewById(R.id.start_service_btn);

        startBtn.setOnClickListener(this);
        updateTime.setVisibility(View.INVISIBLE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);

        if(location != null){
            onLocationChanged(location);
        }

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_service_btn:
                Toast.makeText(this, "Start tracking", Toast.LENGTH_SHORT).show();
                Intent bgService = new Intent(getApplicationContext(), BackgroundService.class);
                startService(bgService);
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            LocationAlertDialog();
        }else{
            locationManager.requestLocationUpdates("gps", 5000, 1, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    /*show dialog to enable GPS*/
    public void LocationAlertDialog() {

        AlertDialog.Builder d = new AlertDialog.Builder(this);

        d.setTitle(getResources().getString(R.string.gps_dialog_header));
        d.setMessage(getResources().getString(R.string.gps_dialog_content));

        d.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        d.setPositiveButton(getResources().getString(R.string.accept), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(i, 0);
            }
        });

        d.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        latTv.setText("Lat: " + location.getLatitude());
        lngTv.setText("Lng: " + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(getClass().getName(), "onStatusChanged() called with: provider = [" + provider + "], status = [" + status + "], extras = [" + extras + "]");
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
