package com.willcapo.radartag;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationListener;

// extends ApplicationContext

// 4/23 7:07p video 0:20 says there's a 3rd to implement?
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String LOG_TAG ="MainActivity";
    private TextView txtOutput;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        txtOutput = (TextView) findViewById(R.id.txtOutput);

        //super.onStart(); // need .onStart. need to extend ApplicationContext instead of AppCompatActivity
//        onStart(); 4/23 7:54P something else is calling this?

        Intent myIntent = new Intent(getBaseContext(), CameraActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getBaseContext().startActivity(myIntent);


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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 111;
    @Override
    protected void onStart() {
        Log.i(LOG_TAG, "onStart() called");
        super.onStart();

        // 4/23 8:09p Had to add this code because it wants Run-Time Permission http://stackoverflow.com/questions/38508352/client-must-have-access-fine-location-permission-to-request-priority-high-accura
        // Check for permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(LOG_TAG, "onStart() location permission was not granted...");
            ActivityCompat.requestPermissions(
                    this, // Activity
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        } else {
            Log.i(LOG_TAG, "onStart() location permission was granted?");
            mGoogleApiClient.connect();
        }

    }

    // Get permission result
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.i(LOG_TAG, "onRequestPermissionResult(cb) called...");

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    Log.i(LOG_TAG, "Permission was granted");
                    mGoogleApiClient.connect();

                } else {
                    // permission was denied
                    mGoogleApiClient.connect();

                }
                return;
            }
        }
    }


//    private int i = 0;
//    public void runOnUiThread(new Runnable() {
//
//        @Override
//        public void run() {
//            TextView tv = (TextView) findViewById(R.id.txtOutput);//Text To be edited
//            tv.setText("test"+i);//Set the Text
//            i++;
//        }
//    });

    @Override
    protected void onStop() {
        //Disconnect the client
        // ...write code
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
            Log.v(LOG_TAG, " onConnected ");
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(1000);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    // *
    @Override
    public void onLocationChanged(Location location) {
        Log.v(LOG_TAG, " onLocationChanged(cb) 1 ");

        // 4/23 8:45p
        // Attempt 2
        final Location aLocation = location;
        txtOutput.post(new Runnable(){
            @Override
            public void run(){
                Log.i(LOG_TAG, "RUnnable is running....");
                txtOutput.setText(aLocation.toString()); // location.toString cannot access location due to inner class
            }
        });

        // [OR]

        // Attempt 1
//        Log.i(LOG_TAG, "onLocationChanged(cb) 22 " + location.toString());
//        txtOutput.setText(location.toString());
//        Log.i(LOG_TAG, "onLocationChanged(cb) 3 text that was set | " + txtOutput.getText());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "GoogleApiClient connection has been suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "GoogleApiClient connection has failed");
    }
}
