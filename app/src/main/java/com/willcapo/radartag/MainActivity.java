package com.willcapo.radartag;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
        //startActivity(myIntent);


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

    @Override
    public void onLocationChanged(Location location) {
        Log.v(LOG_TAG, " onLocationChanged()");

        // 4/23 8:45p
        // Attempt 2
        final Location aLocation = location;
        txtOutput.post(new Runnable(){ // mHandler or txtOutput works
//        txtOutput.post(new Runnable(){
            @Override
            public void run(){
                Log.i(LOG_TAG, "Runnable is running....");
                txtOutput.setText(aLocation.toString());
                txtOutput.invalidate();
                Log.i(LOG_TAG, "HELLO: " + txtOutput.getText());

                // 4/28 7:12p
                // Why, what is CharSequence?
                // Convert to JSON structure?
                // method for preparing as JSON. may need to import a package
                CharSequence text = txtOutput.getText();
                sendGeoUpdate(text);
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
    // ragtag methods
    // 4/22 7:16p
     */

    // or send an object (<HashMap>) of the latitude, longitude
    public void sendGeoUpdate(CharSequence text) {
        Log.i(LOG_TAG, "sendGeoUpdate" + text);
        // Setup Firebase App
        // Call Firebase
        // 8:24p
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("message");

        // 8:25p
        // convert CharSequenc (in previous func invocation to String type)
//        myRef.setValue("Hello, World!");

        // *
        // 8:26p
        // Want users (authenticated) connecting to App, using the default Database Rules in Firebase Console
        // 8:27p
        // Made .read: true, .write: true
        // 8:28p
        // User Authentication Guide https://firebase.google.com/docs/database/security/quickstart?utm_source=studio#sample-rules

    }
}
