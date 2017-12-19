package com.willcapo.radartag;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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



// 4/28 8:23p trying Firebase Assistant https://firebase.google.com/docs/android/setup#next_steps
// previous time was done with https://cloud.google.com/solutions/mobile/firebase-app-engine-android-studio#adding_a_user_interface_to_your_android_app

 //8:41p ran Firebase Assistant and now these packages are recognized
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;



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
        startActivity(myIntent);


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
        mGoogleApiClient.connect();
        //4/29 11:28a moved this check permission code to onConnected
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Log.i(LOG_TAG, "onStart() location permission was not granted...");
//            ActivityCompat.requestPermissions(
//                    this, // Activity
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
//        } else {
//            Log.i(LOG_TAG, "onStart() location permission was granted?");
//            mGoogleApiClient.connect();
//        }

    }

    // Get permission result - never gets called?
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
                    // 4/28 9:34p included mGoogleApiClient.connect() again because fixed 10.0.1 issue in build.gradle
                    mGoogleApiClient.connect();

                } else {
                    // permission was denied
                    mGoogleApiClient.connect();

                }
                return;
            }
        }
    }

    @Override
    protected void onStop() {
        //Disconnect the client
        // ...write code
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(LOG_TAG, " onConnected... ");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(LOG_TAG, "onStart() location permission was not granted...");
            ActivityCompat.requestPermissions(
                    this, // Activity
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        } else {
            Log.i(LOG_TAG, "onConnected()... location permission was granted?");
//            mGoogleApiClient.connect();
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        }
    }

    private String latlong;
    @Override
    public void onLocationChanged(Location location) {
        Log.v(LOG_TAG, " onLocationChanged()");

        // 4/29 1:50p
//        final Location aLocation = location;
        latlong = prepareLatLongString(location); // Jva CharaSequence


        txtOutput.post(new Runnable(){
            @Override
            public void run(){
//                txtOutput.setText(aLocation.toString()); commented out 1:52p

                // 4/29 1:52p
                txtOutput.setText(latlong);
                txtOutput.invalidate(); // still displays 4/29 1:54p

                Log.i(LOG_TAG, "onLocationChanged() | Latitude/Longitude | " + latlong);

//                CharSequence text = txtOutput.getText();
//                String latlong = text.toString();

                sendGeoUpdate(latlong);
            }
        });

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "GoogleApiClient connection has been suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "GoogleApiClient connection has failed");
    }


    /*
    // ragtag variables
     */
    private String deviceId;

    /*
    // ragtag methods
    // 4/22 7:16p
     */

    public String prepareLatLongString(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        return latitude + "," + longitude;
    }

    public void sendGeoUpdate(String latlong) {
        Log.i(LOG_TAG, "sendGeoUpdate() | latlong " + latlong);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference users = database.getReference("users");
        // 4/29 12:31p
        // reference 'latLong' is the Key (table) underneath root ragtag db
        // 1:39p
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.i(LOG_TAG, "sendGeoUpdate() | deviceId " + deviceId);
        users.child(deviceId).setValue(latlong); // target (destino, user2), latlong de user1,
    }
}
