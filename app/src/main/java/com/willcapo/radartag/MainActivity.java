package com.willcapo.radartag;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    // http://stackoverflow.com/questions/40353706/lacking-privileges-to-access-camera-service-in-android-6-0
//    private static final int REQUEST_CAMERA_RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        int permissionCheck = ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA);
//
//        // Check if permission was Granted
//        // Ask for permission, if permission was not granted (probably due to higher api level)
//        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
//            Log.v(null, "+++++++++++ MainActivity - CAMERA permission GRANTED +++++++++++");
//        } else {
//            Log.v(null, "+++++++++++ MainACtivity CAMERA permission NOT - GRANTED +++++++++++");
////            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL); // didn't do anything...
//            if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)){
//                Toast.makeText(this,"No Permission to use the Camera services", Toast.LENGTH_SHORT).show();
//            }
//            requestPermissions(new String[] {android.Manifest.permission.CAMERA},REQUEST_CAMERA_RESULT);
//        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent myIntent = new Intent(getBaseContext(), CameraActivity.class);
//        myIntent.putExtra("key", "theValue"); //Optional parameters
        getBaseContext().startActivity(myIntent);

// TOOLBAR
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

// EMAIL BUTTON "Replace with your own action"
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
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
}
