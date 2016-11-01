package com.whileplex.pipproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
//import com.firebase.client.Firebase;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView latTv;
    private TextView lngTv;
    private TextView updateTime;
    private Button startBtn;
    private Button stopBtn;
    private Button resetBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latTv = (TextView)findViewById(R.id.lat_tv);
        lngTv = (TextView)findViewById(R.id.lng_tv);
        updateTime = (TextView)findViewById(R.id.update_time);
        startBtn = (Button)findViewById(R.id.start_service_btn);
        stopBtn = (Button)findViewById(R.id.stop_service_btn);
        resetBtn = (Button)findViewById(R.id.reset_btn);

        startBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
        resetBtn.setOnClickListener(this);

        stopBtn.setVisibility(View.GONE);


        SmartLocation.with(this).location()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        latTv.setText("Lat: "+location.getLatitude());
                        lngTv.setText("Lng: "+location.getLongitude());
                        updateTime.setText(BackgroundService.update_time+"");
                    }
                });

    }

    private void resetData(){
        AppController.getFirebase().child("users").child(AppController.uid).child("lat").setValue(0);
        AppController.getFirebase().child("users").child(AppController.uid).child("lng").setValue(0, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if(firebaseError == null)
                    Toast.makeText(MainActivity.this, "Reset successful", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.start_service_btn:
                Intent bgService = new Intent(getApplicationContext(), BackgroundService.class);
                bgService.putExtra(AppController.UIDTAG,AppController.uid);
                startService(bgService);
                Log.d("oakTag",AppController.uid);
                break;
            case R.id.stop_service_btn:
                Toast.makeText(this, "stop tracking", Toast.LENGTH_SHORT).show();
                stopService(new Intent(getApplicationContext(), BackgroundService.class));
                break;
            case R.id.reset_btn:
                resetData();
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logout) {
            AppController.getFirebase().unauth();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    /*check internet connection*/
    public boolean hasConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null && wifiNetwork.isConnected()) {
            return true;
        }

        NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null && mobileNetwork.isConnected()) {
            return true;
        }

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        }

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(hasConnection()){
            if (!((LocationManager)getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)){
                LocationAlertDialog();
            }
        }
        else{
            Toast.makeText(this, "Please connect internet.", Toast.LENGTH_SHORT).show();
        }
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
}
