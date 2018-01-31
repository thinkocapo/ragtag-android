package com.willcapo.radartag

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationListener

// Firebase Assistant https://firebase.google.com/docs/android/setup#next_steps
// previous time was done with https://cloud.google.com/solutions/mobile/firebase-app-engine-android-studio#adding_a_user_interface_to_your_android_app

// ran Firebase Assistant and now these packages are recognized
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener


// extends ApplicationContext
class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private var txtOutput: TextView? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null

    private var latlong: String? = null

    private var deviceId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()

        txtOutput = findViewById(R.id.txtOutput) as TextView

        // super.onStart(); // need .onStart. need to extend ApplicationContext instead of AppCompatActivity
        // onStart(); // something else is calling this?

        // Disabling Camera Activity because its not needed for basic app
        //Intent myIntent = new Intent(getBaseContext(), CameraActivity.class);
        //myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //startActivity(myIntent);
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }

    override fun onStart() {
        Log.i(LOG_TAG, "onStart()")
        super.onStart()
        mGoogleApiClient!!.connect()
    }

    // Get permission result - never gets called?
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(LOG_TAG, "onRequestPermissionResult(cb) called...")

        when (requestCode) {
            MY_PERMISSIONS_REQUEST_FINE_LOCATION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    Log.i(LOG_TAG, "Permission was granted")
                    // 4/28 9:34p included mGoogleApiClient.connect() again because fixed 10.0.1 issue in build.gradle
                    mGoogleApiClient!!.connect()

                } else {
                    // permission was denied
                    mGoogleApiClient!!.connect()

                }
                return
            }
        }
    }

    override fun onStop() {
        //Disconnect the client
        // ...write code
        mGoogleApiClient!!.disconnect()
        super.onStop()
    }

    // Run-Time Permission http://stackoverflow.com/questions/38508352/client-must-have-access-fine-location-permission-to-request-priority-high-accura
    override fun onConnected(bundle: Bundle?) {
        Log.v(LOG_TAG, " onConnected()")
        mLocationRequest = LocationRequest.create()
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest!!.interval = 10000

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(LOG_TAG, "onConnected() location permission was not granted")
            ActivityCompat.requestPermissions(
                    this, // Activity
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION)
        } else {
            Log.i(LOG_TAG, "onConnected() location permission was granted")
            // mGoogleApiClient.connect();
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)

        }
    }

    override fun onLocationChanged(location: Location) {
        Log.v(LOG_TAG, " onLocationChanged()")

        // final Location aLocation = location;
        latlong = prepareLatLongString(location) // Jva CharaSequence


        txtOutput!!.post {
            // txtOutput.setText(aLocation.toString());
            txtOutput!!.text = latlong
            txtOutput!!.invalidate()

            Log.i(LOG_TAG, "onLocationChanged() | Latitude/Longitude | " + latlong!!)

            // CharSequence text = txtOutput.getText();
            // String latlong = text.toString();
            sendGeoUpdate(latlong)
        }

    }

    override fun onConnectionSuspended(i: Int) {
        Log.i(LOG_TAG, "GoogleApiClient connection has been suspended")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.i(LOG_TAG, "GoogleApiClient connection has failed")
    }

    fun prepareLatLongString(location: Location): String {
        val latitude = location.latitude
        val longitude = location.longitude
        return latitude.toString() + "," + longitude
    }

    fun sendGeoUpdate(latlong: String?) {
        Log.i(LOG_TAG, "sendGeoUpdate() | latlong " + latlong!!)

        val database = FirebaseDatabase.getInstance()
        val users = database.getReference("users")

        // reference 'latLong' is the Key (table) underneath root ragtag db
        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        Log.i(LOG_TAG, "sendGeoUpdate() | deviceId " + deviceId!!)
        users.child(deviceId!!).setValue(latlong) // target (destino, user2), latlong de user1,
    }

    companion object {
        private val LOG_TAG = "MainActivity"


        private val MY_PERMISSIONS_REQUEST_FINE_LOCATION = 111
    }
}
