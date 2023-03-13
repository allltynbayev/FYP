package com.text.textr01.locationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class UserActivty extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;

    private int permissionCode = 1;


    String phone;
    RelativeLayout relativeLayout;
    TextView trackModetv, Taptv;
    String date;

    ProgressDialog loadingBar;
    String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_activty);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


        Intent i = getIntent();
        phone = i.getStringExtra("phone");

        loadingBar = new ProgressDialog(this);

        loadingBar.setTitle("Openning..");
        loadingBar.setMessage("Please wait...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        trackModetv = (TextView) findViewById(R.id.trackmodetext);
        Taptv = (TextView) findViewById(R.id.tap);

        relativeLayout = (RelativeLayout) findViewById(R.id.userlayout);

        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.child("Users").child(phone).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mode = dataSnapshot.child("mode").getValue().toString();

                if (mode.equals("on")) {
                    loadingBar.dismiss();
                    relativeLayout.setBackgroundResource(R.drawable.green);
                    trackModetv.setText("Track Mode is On");
                    Taptv.setText("Tap On Screen to Turn Off");

                } else if (mode.equals("off")) {
                    loadingBar.dismiss();
                    relativeLayout.setBackgroundResource(R.drawable.red);
                    trackModetv.setText("Track Mode is off");
                    Taptv.setText("Tap On Screen to Turn On");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode.equals("on")) {
                    RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            RootRef.child("Users").child(phone).child("mode").setValue("off");
                            String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());

                            RootRef.child("Users").child(phone).child(date).child("modeoffdatetime").setValue(currentDateTimeString);


                            if (ActivityCompat.checkSelfPermission(UserActivty.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserActivty.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                return;
                            }
                            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                            if (location == null) {
                                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, UserActivty.this);

                            } else {
                                currentLatitude = location.getLatitude();
                                currentLongitude = location.getLongitude();

                                Toast.makeText(UserActivty.this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();

                                saveLocationToDB(currentLatitude, currentLongitude);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else if (mode.equals("off")) {
                    RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            RootRef.child("Users").child(phone).child("mode").setValue("on");
                            date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                            String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());

                            HashMap<String, Object> userDataMap = new HashMap<>();
                            userDataMap.put("modeondatetime", currentDateTimeString);
                            userDataMap.put("locationname", "");
                            userDataMap.put("modeoffdatetime", "");

                            RootRef.child("Users").child(phone).child(date).updateChildren(userDataMap);

                            if (ActivityCompat.checkSelfPermission(UserActivty.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserActivty.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                return;
                            }
                            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                            if (location == null) {
                                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, UserActivty.this);

                            } else {
                                currentLatitude = location.getLatitude();
                                currentLongitude = location.getLongitude();

                                Toast.makeText(UserActivty.this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();

                                saveLocationToDB(currentLatitude, currentLongitude);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.logout2:

                SharedPreferences sharedPreferences
                        = getSharedPreferences("MySharedPref",
                        MODE_PRIVATE);

                SharedPreferences.Editor myEdit
                        = sharedPreferences.edit();

                myEdit.putString(
                        "active",
                        "newuser");
                myEdit.commit();
                Intent intent = new Intent(UserActivty.this, MainActivity.class);
                startActivity(intent);
                return true;

            case R.id.updatepassword2:
                Intent intent2 = new Intent(UserActivty.this, UserUpdatePassword.class);
                intent2.putExtra("phone", phone);
                startActivity(intent2);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Now lets connect to the API
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }


    }

    /**
     * If connected get lat and long
     *
     */
    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            AlertDialog.Builder builder = new AlertDialog.Builder(UserActivty.this);
            builder.setTitle("Grant permissions");
            builder.setMessage("please give ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permission");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(
                            UserActivty.this,
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            },
                            permissionCode
                    );
                }
            })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                            Toast.makeText(UserActivty.this, "Please give permission to use this application..", Toast.LENGTH_SHORT).show();

                            ActivityCompat.requestPermissions(
                                    UserActivty.this,
                                    new String[]{
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                    },
                                    permissionCode
                            );
                        }
                    })
                    .create();
            builder.show();

        } else {

            final DatabaseReference RootRef;
            RootRef = FirebaseDatabase.getInstance().getReference();

            RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("Users").child(phone).child("mode").getValue().equals("on")) {

                        if (ActivityCompat.checkSelfPermission(UserActivty.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserActivty.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                        if (location == null) {
                            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, UserActivty.this);

                        } else {
                            //If everything went fine lets get latitude and longitude
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();

                            Toast.makeText(UserActivty.this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();

                            saveLocationToDB(currentLatitude,currentLongitude);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



        }

    }

    private void saveLocationToDB(double currentLatitude, double currentLongitude) {
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());

        HashMap<String, Object> userDataMap = new HashMap<>();
        userDataMap.put("locationname", String.valueOf(currentLatitude)+String.valueOf(currentLongitude));

        RootRef.child("Users").child(phone).child(date).updateChildren(userDataMap);

    }


    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    /**
     * If locationChanges change lat and long
     *
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();

        saveLocationToDB(currentLatitude,currentLongitude);
    }

}
