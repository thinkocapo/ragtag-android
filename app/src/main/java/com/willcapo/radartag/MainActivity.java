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
import com.google.android.gms.location.LocationServices;

//import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.plus.Plus;

import org.w3c.dom.Text;

// extends ApplicationContext // protected void onStart();
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    // http://stackoverflow.com/questions/40353706/lacking-privileges-to-access-camera-service-in-android-6-0
//    private static final int REQUEST_CAMERA_RESULT = 1;

    private GoogleApiClient mGoogleApiClient;

//    private LocationServices mLastLocation;// = new Location("");

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent myIntent = new Intent(getBaseContext(), CameraActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // myIntent.putExtra("key", "theValue"); //Optional parameters
        getBaseContext().startActivity(myIntent);

        // 4/22 18:11
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
//                    .addApi(Plus.API) // doesn't help 18:39p
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // taken from 'onStart' method 18:48p https://developer.android.com/training/location/retrieve-current.html
        mGoogleApiClient.connect(); // could add to super's .onStart() method? or better to make a local self.onStart()
        super.onStart();

        // need .onStart. need to extend ApplicationContext instead of AppCompatActivity
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


        @Override
    public void onConnected(Bundle bundle) {

            // 4/22 19:06p
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                TextView mLatitudeText = (TextView) findViewById((R.id.latitude_text));
                mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude())); // not a double or a string

                TextView mLongitudeText = (TextView) findViewById((R.id.longitude_text));
                mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude())); // not TextView, Location
            }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
